package opoptions;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;

public class InStateTest implements StateConditionTest {

	private State state;
	
	public InStateTest(State state) {
		this.state = state;
	}
	
	@Override
	public boolean satisfies(State s) {
		return state.equals(s);
	}
	
}
