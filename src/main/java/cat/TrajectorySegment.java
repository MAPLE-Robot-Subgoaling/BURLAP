package cat;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;

public class TrajectorySegment{
	
	private int start, end;
	private StateConditionTest goal;
	
	public TrajectorySegment(int start, int end, StateConditionTest goal) {
		this.start = start;
		this.end = end;
		this.goal = goal;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public StateConditionTest getGoal() {
		return goal;
	}
}
