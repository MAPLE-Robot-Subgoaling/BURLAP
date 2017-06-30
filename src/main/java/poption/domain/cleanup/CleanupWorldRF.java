package poption.domain.cleanup;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;

public class CleanupWorldRF extends GoalBasedRF{

	double noopCost = -1;
	double pullCost = -0.001;
	double pushCost = -0.001;
//	double turnCost = -0.001;

	public CleanupWorldRF(StateConditionTest gc) {
		super(gc);
	}

	public CleanupWorldRF(StateConditionTest gc, double goalReward) {
		super(gc, goalReward);
	}

	public CleanupWorldRF(StateConditionTest gc, double goalReward, double defaultReward) {
		super(gc, goalReward, defaultReward);
	}

	public CleanupWorldRF(TerminalFunction tf) {
		super(tf);
	}

	public CleanupWorldRF(TerminalFunction tf, double goalReward) {
		super(tf, goalReward);
	}

	public CleanupWorldRF(TerminalFunction tf, double goalReward, double defaultReward) {
		super(tf, goalReward, defaultReward);
	}

	public double getPullCost() {
		return pullCost;
	}

	public void setPullCost(double pullCost) {
		this.pullCost = pullCost;
	}
	
	public StateConditionTest getStateCondition(){
		return this.gc;
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		double superR = super.reward(s, a, sprime);
		double r = superR;
		if(a.actionName().equals(CleanupWorld.ACTION_PULL)){
			r += this.pullCost;
		}
		
		if (s.equals(sprime)) {
			r += noopCost;
		}
		
		return r;
	}
}
