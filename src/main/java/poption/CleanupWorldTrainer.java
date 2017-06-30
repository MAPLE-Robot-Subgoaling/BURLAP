package poption;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.support.PolicyUndefinedException;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.OptionType;
import burlap.behavior.singleagent.options.SubgoalBoundedOption;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.masked.MaskedHashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;
import poption.domain.cleanup.*;
import poption.domain.cleanup.state.*;

public class CleanupWorldTrainer extends PoptionsTrainer {

	public static final int BOUND_REACHABLE_STATES = 100000;

	private Environment env;
	private HashableStateFactory hashingFactory;
	private int width;
	private int height;
	private Random rng;
	private CleanupWorld cw;
	private RewardFunction rf;
	private TerminalFunction tf;
	private String goalPF;
	private List<String> classesInFeatureVector;
	private int numBlocks = 1;
	private int numGoals = 1;
	private int randomFactoryMapIndex = 4245;
	
	public double lockProb = 0.0;
	
	private Planner planner;
	private CleanupGoalDescription[] goalDescriptions;
	
	public CleanupWorldTrainer(int numBlocks, int numGoals, long seed, String goalPF) {
		super();
		this.width = 13;
		this.height = 13;
		this.name = "cleanup";
		this.numBlocks = numBlocks;
		this.numGoals = numGoals;
		classesInFeatureVector = Arrays.asList(CleanupWorld.CLASS_AGENT, CleanupWorld.CLASS_BLOCK, CleanupWorld.CLASS_ROOM, CleanupWorld.CLASS_DOOR);
		this.goalPF = goalPF;
		setStateHashingMasks();
	}

	@Override
	public void setStateHashingMasks(){
		// as a trainer, we know what can be ignored by the agent for hashing inside p. option
		// this should not be used during evaluation since agent is not supposed to know what to ignore
		mhsf = new MaskedHashableStateFactory(true);
		
		// temp debug
//		mhsf.addObjectClassMasks(CleanupWorld.CLASS_AGENT);
		
		if (goalPF.equals(CleanupWorld.PF_AGENT_IN_DOOR)) {
			mhsf.addObjectClassMasks(CleanupWorld.CLASS_ROOM, CleanupWorld.CLASS_BLOCK);
			mhsf.addVariableMasks(CleanupWorld.ATT_DIR);
		} else if (goalPF.equals(CleanupWorld.PF_BLOCK_IN_ROOM)){
			mhsf.addObjectClassMasks(CleanupWorld.CLASS_DOOR);
			mhsf.addVariableMasks(CleanupWorld.ATT_DIR);
		} else if (goalPF.equals(CleanupWorld.PF_BLOCK_IN_DOOR)){
			mhsf.addObjectClassMasks(CleanupWorld.CLASS_ROOM);
			mhsf.addVariableMasks(CleanupWorld.ATT_DIR);
		}
	}
	
	@Override
	public void initialize(long seed, boolean testing) {
		
		// do __NOT__ call setStateHashingMasks
		
		System.out.println("Using seed: " + seed);
		System.out.println(numBlocks + " blocks, " + numGoals + " goals");
		
		RandomFactory.seedMapped(randomFactoryMapIndex, seed);
		rng = RandomFactory.getMapped(randomFactoryMapIndex);
		
		if (testing) {
			// evaluation goal task is to move a block to a room
//			goalPF = CleanupWorld.PF_BLOCK_IN_ROOM;
			goalPF = CleanupWorld.PF_BLOCK_IN_DOOR;
		}
		
		cw = new CleanupWorld();
		cw.includeDirectionAttribute(true);
		cw.includeLockableDoors(false);
//		double lockProb = 0.0;
//		cw.setLockProbability(lockProb);
		
		CleanupRandomStateGenerator randomCleanup = new CleanupRandomStateGenerator();
		randomCleanup.setWidth(width);
		randomCleanup.setHeight(height);
		initialState = (OOState) randomCleanup.generateState(); //cw.getRandomState(domain, rng, numBlocks);
		CleanupGoal goal = new CleanupGoal();
		goalCondition = (StateConditionTest) goal;
		rf = new CleanupWorldRF(goalCondition, 1.0, 0.0);
		tf = new GoalConditionTF(goalCondition);
		cw.setRf(rf);
		cw.setTf(tf);
		domain = (OOSADomain) cw.generateDomain();
		
		goalDescriptions = CleanupRandomStateGenerator.getRandomGoalDescription(rng, (CleanupWorldState) initialState, numGoals, domain.propFunction(goalPF));
		goal.setGoals(goalDescriptions);
		
		System.out.println("Goal is: " + goalDescriptions[0]);
		
		hashingFactory = new SimpleHashableStateFactory(true);
		env = new SimulatedEnvironment(domain, initialState);
	}

	@Override
	public boolean train(String outputPath) {
		int maxSteps = 1000;
		double gamma = 0.99;
		int maxRolloutDepth = 50;
		double maxDiff = 0.01;
		int maxRollouts = 500;
		return train(outputPath, maxSteps, gamma, maxRolloutDepth, maxDiff, maxRollouts);
	}	

	public boolean train(String outputPath, int maxSteps, double gamma, int maxRolloutDepth, double maxDiff, int maxRollouts) {
		System.out.println("maxSteps: " + maxSteps);

		int maxTrajectoryLength = 10000;
		RewardFunction heuristicRF = new CleanupWorldRF(goalCondition, 1.0, 0.0);
		ValueFunction heuristic = CleanupWorld.getGroundHeuristic(initialState, heuristicRF, lockProb);
		BoundedRTDP brtd = new BoundedRTDP(domain, gamma,
				new SimpleHashableStateFactory(false),
				new ConstantValueFunction(0.),
				heuristic, maxDiff, maxRollouts);
		brtd.setMaxRolloutDepth(maxRolloutDepth);
		brtd.toggleDebugPrinting(true);
		
		boolean visualize = false;
		if (visualize) {
			VisualActionObserver observer = new VisualActionObserver(domain, CleanupVisualizer.getVisualizer(width, height));
			observer.initGUI();
			env = new EnvironmentServer(env, observer);
		}

		long startTime = System.nanoTime();
		Policy p = brtd.planFromState(initialState);
//		result = p.evaluateBehavior(env, maxSteps);
		result = PolicyUtils.rollout(p, env, maxTrajectoryLength);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		System.out.println("total time: " + duration);
		result.write(outputPath + "cwr_brtdp");
		System.out.println("total actions: " + result.actionSequence.size());
		System.out.println("number Bellman: " + brtd.getNumberOfBellmanUpdates());
		System.out.println("maxTimeStep: " + result.maxTimeStep()+ " " + !((result.maxTimeStep() + 1 >= maxSteps)) );

		return !(result.maxTimeStep()+1 >= maxSteps);
	}

	@Override
	public Policy createOptionPolicy(StateConditionTest goal, HashableStateFactory hf) {
//		Policy optionPolicy = createOptionPolicyBFS(goal, hf);
		Policy optionPolicy = createOptionPolicyBRTDP(goal, hf);
		return optionPolicy;
	}
	
	public Policy createOptionPolicyBRTDP(StateConditionTest goal, HashableStateFactory hf) {
		BoundedRTDP brtdp = (BoundedRTDP) getPlanner(goal, hf);
		Policy optionPolicy = brtdp.planFromState(initialState);
		Environment tempEnv = new SimulatedEnvironment(domain, initialState);
		boolean visOption  = false;
		if (visOption) {
			VisualActionObserver observer = new VisualActionObserver(domain, CleanupVisualizer.getVisualizer(width, height));
			observer.initGUI();
			tempEnv= new EnvironmentServer(tempEnv, observer);
		}
		return optionPolicy;
	}
		
	@Override
	public Planner getPlanner(StateConditionTest goal, HashableStateFactory hf){
//		cw.setRf(new GoalBasedRF(goal));
//		cw.setTf(tf);
		cw.setRf(new CleanupWorldRF(goal, 100.0, -1.0));
		cw.setTf(new NullTermination()); //GoalConditionTF(goal));
		OOSADomain planningDomain = (OOSADomain) cw.generateDomain();
//		planner = initializeBFS(planningDomain, goal, hf);
//		planner.resetSolverKeepStates();
		planner = initializeBRTDP(planningDomain, goal, hf);
		return planner;
	}
	
	public BoundedRTDP initializeBRTDP(OOSADomain planningDomain, StateConditionTest goal, HashableStateFactory hf) {
		double gamma = 0.9;
		ValueFunction lowerVInit = new ConstantValueFunction(0.0);
		ValueFunction upperVInit = new ConstantValueFunction(1.0);
//		RewardFunction heuristicRF = new CleanupWorldRF(goal, 100.0, -1.0);
//		ValueFunction heuristic = CleanupWorld.getGroundHeuristic(initialState, heuristicRF, lockProb);
//		ValueFunction heuristic = new CleanupSCTHeuristic(goal, 100.0, 0.0);
		double maxDiff = 0.001;
		int maxRollouts = 10000;
		int maxRolloutDepth = 100;
		BoundedRTDP brtdp = new BoundedRTDP(planningDomain, gamma, hf, lowerVInit, upperVInit, maxDiff, maxRollouts);
		brtdp.setMaxRolloutDepth(maxRolloutDepth);
		brtdp.toggleDebugPrinting(true);
		return brtdp;
	}
	

	/*
	public Policy createOptionPolicyBFS(StateConditionTest goal, HashableStateFactory hf) {
//		BoundedRTDP brtdp = initializeBRTDP(optionDomain, hf);
		int maxTrajectoryLength = 10000;
		BFS bfs = (BFS) getPlanner(goal, hf);
		bfs.planFromState(initialState);
		Policy optionPolicy = new DDPlannerPolicy((DeterministicPlanner)bfs);
		try {
			PolicyUtils.rollout(optionPolicy, initialState, domain.getModel(), maxTrajectoryLength);
		} catch (PolicyUndefinedException e) {
			System.err.println(e.getMessage());
		}
		return optionPolicy;
	}

	public BFS initializeBFS(OOSADomain planningDomain, StateConditionTest goal, HashableStateFactory hf) {
		BFS bfs = new BFS(planningDomain, goal, hf);
		bfs.toggleDebugPrinting(true);
		return bfs;
	}
	*/


	@Override
	public List<State> getReachableStates() {
		System.out.println("Warning: not using masked state hashable factory in state reachability");
		List<State> allStates = StateReachability.getReachableStates(initialState, domain, new SimpleHashableStateFactory(true), 10000);
		return allStates;
	}

	@Override
	public Visualizer getVisualizer() {
		return CleanupVisualizer.getVisualizer(width, height);
	}

	
	@Override
	public String getFeatureVectorHeader() {
		return getFeatureVectorNames(classesInFeatureVector, null);
	}

	@Override
	public List<double[]> getTrainingFeatureVectors() {
		return getTrainingFeatureVectors(classesInFeatureVector, null);
	}

	@Override
	public List<double[]> getTestFeatureVectors() {
		return getTestFeatureVectors(classesInFeatureVector, null);
	}
	
	@Override
	public void visualize(String outputPath) {
		Visualizer v = getVisualizer();
		EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(v, domain, outputPath);
		esv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		esv.initGUI();
	}
	
	@Override
	public String getVisualizeFilePath() {
		return "./output_eval/";
	}
	
	@Override
	public void evaluate(List<Option> options) {
		boolean visualize = false;
		boolean visOptionPolicies = false;
		double gamma = 0.99;
		double qInit = 0.;
		double learningRate = 0.025;
		int numEpisodes = 10;
		int writeEvery = 1;
		int maxEpisodeSize = 100;
		evaluateLearning(options, visualize, visOptionPolicies, gamma, qInit, learningRate, numEpisodes, writeEvery, maxEpisodeSize);
	}

	
	public void evaluateLearning(List<Option> options, boolean visualize, boolean visOptionPolicies, double gamma, double qInit, double learningRate, int numEpisodes, 
			int writeEvery, int maxEpisodeSize) {
		
		QLearning ql = new QLearning(domain, gamma, hashingFactory, qInit, learningRate, maxEpisodeSize);
		System.out.println("Giving the agent " + options.size() + " options");
		for (Option option : options) {
			ql.addActionType(new OptionType(option));
		}
		SubgoalBoundedOption.maxSteps = maxEpisodeSize;
	
		// visualize option policies
//		if (visOptionPolicies) {
//			List<ActionType> acts = ((QLearning)ql).getActionTypes();
//			for(ActionType act : acts) {
//				if(act.typeName().contains("option")) {
//					OptionType optionType = (OptionType)act;
//					SubgoalOption opt = (SubgoalOption)optionType.associatedAction(null);
//					Policy policy = opt.getPolicy();
//					ValueFunction vf = (ValueFunction) (((GreedyQPolicy)policy).getQPlanner());
//					visualizeValueFunction(vf, policy);
//				}
//			}
//		}
		
		
		if (visualize) {
			VisualActionObserver observer = new VisualActionObserver(domain, CleanupVisualizer.getVisualizer(width, height));
			observer.initGUI();
			env = new EnvironmentServer(env, observer);
		}
	
		System.out.println("Begin learning... (" + numEpisodes+" episodes)");
		for(int i = 0; i < numEpisodes; i++) {
			Episode ea = ql.runLearningEpisode(env, maxEpisodeSize);
			if (i % writeEvery == 0) {
				ea.write(getVisualizeFilePath() + "ql_" + i);
				System.out.println(i + ": " + ea.maxTimeStep());
			}
			env.resetEnvironment();
		}
	}
	
	
	/*
	public void evaluatePlanning(List<Option> options, boolean visualize, int maxSteps, double gamma, int maxRolloutDepth, double maxDiff, int maxRollouts) {
		
	}
	*/
	
    public static class CleanupSCTHeuristic implements ValueFunction {

    	public double goalR;
    	public double defaultR;
    	public StateConditionTest goal;
    	
    	public CleanupSCTHeuristic(StateConditionTest goal, double goalR, double defaultR) {
    		this.goal = goal;
    		this.goalR = goalR;
    		this.defaultR = defaultR;
    	}
    	
		@Override
		public double value(State s) {
			if (goal.satisfies(s)) {
				return goalR;
			} else {
				return defaultR;
			}
		}
    	
    }

	
	public static void main(String[] args) {

		/*
//		String functionToTrain = CleanupWorld.PF_AGENT_IN_DOOR;
		String functionToTrain = CleanupWorld.PF_BLOCK_IN_ROOM;
		
		String outputPath = "output_cw_main/";
		Random rng = RandomFactory.seedMapped(0, 11324);//42342343);
		long seed = rng.nextLong();
		System.out.println("Using seed " + seed);
		CleanupWorldTrainer trainer = new CleanupWorldTrainer(1, 1, seed, functionToTrain);
		
		trainer.initialize(seed, false);
		trainer.train(outputPath);
		trainer.visualize(outputPath);
		
		long startTime = System.nanoTime();
		trainer.train(outputPath + "main1_");
		long endTime = System.nanoTime();
		long duration = (endTime - startTime); 
		System.out.println("Duration: " + duration);
		*/
		
		PoptionsDriver.main(null);
	}
	
}
