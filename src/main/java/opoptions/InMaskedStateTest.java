package opoptions;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.masked.MaskedHashableStateFactory;

public class InMaskedStateTest implements StateConditionTest {

	private MaskedHashableStateFactory mhsf;
	private HashableState state;
	
	public InMaskedStateTest(MaskedHashableStateFactory mhsf, HashableState state) {
		this.mhsf = mhsf;
		this.state = state;
	}
	
	@Override
	public boolean satisfies(State s) {
		HashableState hs = mhsf.hashState(s);
		return state.equals(hs);
	}

}
