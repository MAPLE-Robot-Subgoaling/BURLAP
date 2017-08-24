package opoptions.trainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.performance.PerformancePlotter;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.OptionType;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.visualizer.Visualizer;
import cleanup.Cleanup;
import cleanup.CleanupGoal;
import cleanup.CleanupGoalDescription;
import cleanup.CleanupRF;
import cleanup.CleanupVisualizer;
import cleanup.state.CleanupRandomStateGenerator;
import cleanup.state.CleanupState;
import opoptions.OPODriver;
import opoptions.OPOTrainer;

public class CleanupTrainer extends OPOTrainer {

    // specified by data file
    public Cleanup domainGenerator;
    public int numGoals;
    public int numBlocksTraining;
    public int numBlocksEvaluation;
    public String nameTrainingGoalPF;
    public String nameTrainingStateType;
    public String nameEvaluationStateType;
    public double rewardDefault;
    public double rewardGoal;
    public double rewardNoop;
    public double rewardPull;
//    private MDPSolver optionPlanner;

    private int visualizerWidth = 600;
    private int visualizerHeight = 600;

    public CleanupTrainer() {
        // use SimulationConfig to load the trainer, not this constructor
    }

    @Override
    public OOState setupStateTraining() {
        CleanupRandomStateGenerator.setDebugMode(true);
        CleanupRandomStateGenerator randomCleanup = new CleanupRandomStateGenerator();
        randomCleanup.setWidth(domainGenerator.getWidth());
        randomCleanup.setHeight(domainGenerator.getHeight());
        initialState = (OOState) randomCleanup.getStateFor(nameTrainingStateType, numBlocksTraining);  //randomCleanup.generateOneRoomOneDoor(); // generateTaxiInCleanup(1);//.generateCentralRoomWithClosets(1); //cw.getRandomState(domain, rng, numBlocks);
        return (OOState) initialState;
    }

    @Override
    public OOState setupStateEvaluation() {
        CleanupRandomStateGenerator.setDebugMode(true);
        CleanupRandomStateGenerator randomCleanup = new CleanupRandomStateGenerator();
        randomCleanup.setWidth(domainGenerator.getWidth());
        randomCleanup.setHeight(domainGenerator.getHeight());
        initialState = (OOState) randomCleanup.getStateFor(nameEvaluationStateType, numBlocksEvaluation);  // randomCleanup.generateCentralRoomWithFourDoors(0); //generateTaxiInCleanup(1);//.generateCentralRoomWithClosets(1); //cw.getRandomState(domain, rng, numBlocks);
        return (OOState) initialState;
    }

    @Override
    public OOSADomain setupDomainTraining() {
        CleanupGoal goal = new CleanupGoal();
        RewardFunction rf = new CleanupRF(goal, rewardGoal, rewardDefault, rewardNoop, rewardPull);
        TerminalFunction tf = new GoalConditionTF(goal);
        domainGenerator.setRf(rf);
        domainGenerator.setTf(tf);
        domain = (OOSADomain) domainGenerator.generateDomain();

        // setup the goal
        PropositionalFunction trainingGoalPF = getTrainingGoalPF();
        CleanupGoalDescription[] goalDescriptions = CleanupRandomStateGenerator.getRandomGoalDescription((CleanupState) initialState, numGoals, trainingGoalPF);
        goal.setGoals(goalDescriptions);
        OPODriver.log("Goal is: " + goalDescriptions[0]);

        return (OOSADomain) domain;
    }

    @Override
    public OOSADomain setupDomainNoRFTF() {
        RewardFunction rf = new NullRewardFunction();//new CleanupRF(goal, rewardGoal, rewardDefault, rewardNoop, rewardPull);
        TerminalFunction tf = new NullTermination();//GoalConditionTF(goal);
        domainGenerator.setRf(rf);
        domainGenerator.setTf(tf);
        domain = (OOSADomain) domainGenerator.generateDomain();

        return (OOSADomain) domain;
    }

    @Override
    public OOSADomain setupDomainEvaluation() {
        CleanupGoal goal = new CleanupGoal();
        RewardFunction rf = new CleanupRF(goal, rewardGoal, rewardDefault, rewardNoop, rewardPull);
        TerminalFunction tf = new GoalConditionTF(goal);
//		tf = new NullTermination();
        domainGenerator.setRf(rf);
        domainGenerator.setTf(tf);
        domain = (OOSADomain) domainGenerator.generateDomain();

        // setup the goal
        PropositionalFunction trainingGoalPF = getTrainingGoalPF();
        CleanupGoalDescription[] goalDescriptions = CleanupRandomStateGenerator.getRandomGoalDescription((CleanupState) initialState, numGoals, trainingGoalPF);
        goal.setGoals(goalDescriptions);
        OPODriver.log("Goal is: " + goalDescriptions[0]);

        return (OOSADomain) domain;
    }

    public PropositionalFunction getTrainingGoalPF() {
        PropositionalFunction goalPF = ((OOSADomain) domain).propFunction(nameTrainingGoalPF);
        return goalPF;
    }

    @Override
    public void runEpisodeVisualizer(String filePrefix) {
        Visualizer v = CleanupVisualizer.getVisualizer(domainGenerator.getWidth(), domainGenerator.getHeight());
        EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(v, domain, episodeOutputPath + "/" + filePrefix, visualizerWidth, visualizerHeight);
        esv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void runEpisodeVisualizer(List<Episode> episodes) {
        Visualizer v = CleanupVisualizer.getVisualizer(domainGenerator.getWidth(), domainGenerator.getHeight());
        EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(v, domain, episodes, visualizerWidth, visualizerHeight);
        esv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void runEvaluation(PerformancePlotter plotter) {
        super.runEvaluation(plotter);

        Set<Option> options = opoption.generateOptions(this);

        return;
        /*

        int maxEpisodeSize = 100;
        QLearning ql = new QLearning(domain, 0.9, hashingFactory, 0.0, 0.01, maxEpisodeSize);
        ql.setLearningPolicy(new EpsilonGreedy(ql, 0.1));
        for (Option option : options) {
            ql.addActionType(new OptionType(option));
        }
        VisualActionObserver observer = new VisualActionObserver((OOSADomain) domain,
                CleanupVisualizer.getVisualizer(domainGenerator.getWidth(), domainGenerator.getHeight()));
        observer.initGUI();
        env = new SimulatedEnvironment(domain, initialState);
        env = new EnvironmentServer(env, observer);
        int numEpisodes = 100;
        List<Episode> episodes = new ArrayList<Episode>();
        for (int i = 0; i < numEpisodes; i++) {
            Episode e = ql.runLearningEpisode(env, maxEpisodeSize);
            OPODriver.log(i + ": " + e.maxTimeStep() + " " + e.actionSequence.toString());
            episodes.add(e);
            env.resetEnvironment();
        }
        runEpisodeVisualizer(episodes);
//        for (LearnedStateTest test : tests) {
//            OPODriver.log("learnedStateTest for " + test.getTargetLabel());
//            OPODriver.log("predicted / actual: ");
//            for (State state : states) {
//                OOState s = (OOState) state;
//                boolean satisfied = test.satisfies(s);
//                OPODriver.log(satisfied + " / " + satisfiesTrainingGoal(s) + ", for state " + StateFeaturizer.stateToStringBuilder(new StringBuilder(), s));
//            }
//        }

//        episodeOutputPath = getEpisodeOutputPathEvaluation();
//        String seedTimestamp = planAndRollout(plotter);
//        lastSeedTimestampEvaluation = seedTimestamp;
*/
    }


//    public MDPSolver getOptionPlanner() {
//        return optionPlanner;
//    }
//
//    public void setOptionPlanner(MDPSolver optionPlanner) {
//        this.optionPlanner = optionPlanner;
//    }

    @Override
    public MDPSolverInterface initializeOptionPlanner(StateConditionTest specificGoal) {
        RewardFunction originalRF = domainGenerator.getRf();
        TerminalFunction originalTF = domainGenerator.getTf();
        RewardFunction optionRF = new GoalBasedRF(specificGoal);
        TerminalFunction optionTF = new GoalConditionTF(specificGoal);
        domainGenerator.setRf(optionRF);
        domainGenerator.setTf(optionTF);
        SADomain optionDomain = (SADomain) domainGenerator.generateDomain();
        domainGenerator.setRf(originalRF);
        domainGenerator.setTf(originalTF);
        double optionGamma = 0.95;
        HashableStateFactory optionHashingFactory = hashingFactory;
        double maxDelta = 0.001;
        int maxIterations = 10000;
        double vInit = 0.0;
        int numRollouts = 1000;
        int maxDepth = 2 * domainGenerator.getWidth() * domainGenerator.getHeight();
//        RTDP rtdp = new RTDP(optionDomain, optionGamma, optionHashingFactory, vInit, numRollouts, maxDelta, maxDepth);
//        return rtdp;
//        ValueIteration vi = new ValueIteration(optionDomain, optionGamma, optionHashingFactory, maxDelta, maxIterations);
//        return vi;
        BFS bfs = new BFS(optionDomain, specificGoal, hashingFactory);
        return bfs;
//		optionPlanner.setDomain(optionDomain);
//		optionPlanner.setHashingFactory(optionHashingFactory);
//		optionPlanner.resetSolver();
//		return optionPlanner;
//		Planner planner = new Planner(optionPlanner);
//		Planner planner = null;
//		try {
//			planner = (Planner) BeanUtils.cloneBean(optionPlanner);
//			planner.setDomain(optionDomain);
//			planner.setHashingFactory(optionHashingFactory);
//			planner.resetSolver();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return planner;
    }

    @Override
    public DomainGenerator getDomainGenerator() {
        return domainGenerator;
    }

}
