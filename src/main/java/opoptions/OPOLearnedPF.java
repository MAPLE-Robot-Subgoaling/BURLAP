package opoptions;


import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;

public class OPOLearnedPF extends PropositionalFunction {

    protected LearnedStateTest learnedTest;

    public OPOLearnedPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
        super(name, parameterClasses, parameterOrderGroup);
    }

    @Override
    public boolean isTrue(OOState s, String... params) {
        return learnedTest.satisfies(s, params);
    }

    public LearnedStateTest getLearnedTest() {
        return learnedTest;
    }

    public void setLearnedTest(LearnedStateTest learnedTest) {
        this.learnedTest = learnedTest;
    }

}
