package opoptions;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.selected.SelectedHashableStateFactory;

public class InSelectedStateTest implements StateConditionTest {

    private SelectedHashableStateFactory shsf;
    private HashableState state;

    public InSelectedStateTest(SelectedHashableStateFactory shsf, HashableState state) {
        this.shsf = shsf;
        this.state = state;
    }

    @Override
    public boolean satisfies(State s) {
        HashableState hs = shsf.hashState(s);
        return state.equals(hs);
    }

}
