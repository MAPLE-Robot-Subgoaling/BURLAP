package opoptions.trainers;

import burlap.behavior.singleagent.MDPSolver;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.oo.OOSADomain;
import cleanup.Cleanup;
import cleanup.CleanupGoal;
import cleanup.CleanupGoalDescription;
import cleanup.CleanupRF;
import cleanup.state.CleanupRandomStateGenerator;
import cleanup.state.CleanupState;
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
		CleanupRandomStateGenerator randomCleanup = new CleanupRandomStateGenerator();
		randomCleanup.setWidth(domainGenerator.getWidth());
		randomCleanup.setHeight(domainGenerator.getHeight());
		initialState = (OOState) randomCleanup.generateCentralRoomWithClosets(1); //generateTaxiInCleanup(1);//.generateCentralRoomWithClosets(1); //cw.getRandomState(domain, rng, numBlocks);
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
		PropositionalFunction blockInRoom = ((OOSADomain) domain).propFunction(Cleanup.PF_BLOCK_IN_ROOM);
		goalDescriptions = CleanupRandomStateGenerator.getGoalDescriptionBlockToRoomSameColor((CleanupState) initialState, numGoals, blockInRoom);
//		PropositionalFunction blockInDoor = ((OOSADomain) domain).propFunction(Cleanup.PF_BLOCK_IN_DOOR);
//		goalDescriptions = CleanupRandomStateGenerator.getGoalDescriptionBlockToDoorSameColor((CleanupState) initialState, numGoals, blockInDoor);
		goal.setGoals(goalDescriptions);
		System.out.println("Goal is: " + goalDescriptions[0]);
		
		return (OOSADomain) domain;
	}
	
}
