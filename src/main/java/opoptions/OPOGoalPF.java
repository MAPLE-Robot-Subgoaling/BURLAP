package opoptions;


import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class OPOGoalPF extends PropositionalFunction {

    protected LearnedStateTest goalTest;

    public OPOGoalPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return goalTest.satisfies(s);
    }

    public LearnedStateTest getGoalTest() {
        return goalTest;
    }

    public void setGoalTest(LearnedStateTest goalTest) {
        this.goalTest = goalTest;
    }

}
