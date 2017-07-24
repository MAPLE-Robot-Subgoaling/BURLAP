package opoptions.trainers;

import javax.swing.JFrame;

import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.oo.OOSADomain;
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

public class OPOCleanup extends OPOTrainer {
	
	// specified by data file
	public Cleanup domainGenerator;
	public int numGoals;
	public double rewardDefault;
	public double rewardGoal;
	public double rewardNoop;
	public double rewardPull;
	
	// generated in code
	public CleanupGoal goal;
	public CleanupGoalDescription[] goalDescriptions;
	
	public OPOCleanup() {
		// use SimulationConfig to load the trainer, not this constructor
	}

	@Override
	public OOState setupStateTraining() {
		CleanupRandomStateGenerator.setDebugMode(true);
		CleanupRandomStateGenerator randomCleanup = new CleanupRandomStateGenerator();
		randomCleanup.setWidth(domainGenerator.getWidth());
		randomCleanup.setHeight(domainGenerator.getHeight());
		initialState = (OOState) randomCleanup.generateOneRoomOneDoor(); //generateTaxiInCleanup(1);//.generateCentralRoomWithClosets(1); //cw.getRandomState(domain, rng, numBlocks);
		return (OOState) initialState;
	}

	@Override
	public OOSADomain setupDomain() {
		goal = new CleanupGoal();
		rf = new CleanupRF(goal, rewardGoal, rewardDefault, rewardNoop, rewardPull);
		tf = new GoalConditionTF(goal);
//		tf = new NullTermination();
		domainGenerator.setRf(rf);
		domainGenerator.setTf(tf);
		domain = (OOSADomain) domainGenerator.generateDomain();
		
		// setup the goal
		PropositionalFunction agentInDoor = ((OOSADomain) domain).propFunction(Cleanup.PF_AGENT_IN_DOOR);
		goalDescriptions = CleanupRandomStateGenerator.getRandomGoalDescription((CleanupState) initialState, numGoals, agentInDoor);
		goal.setGoals(goalDescriptions);
		OPODriver.log("Goal is: " + goalDescriptions[0]);
		
		return (OOSADomain) domain;
	}


	@Override
	public void runEpisodeVisualizer(String filePrefix) {
		Visualizer v = CleanupVisualizer.getVisualizer(domainGenerator.getWidth(), domainGenerator.getHeight());
		EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(v, domain, outputPath + "/" + filePrefix);
		esv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
