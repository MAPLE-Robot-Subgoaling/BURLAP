package opoptions;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import cleanup.GroundedPropSC;

import java.util.*;

public class OPOption implements OptionGenerator {

    public static final String NAME_STATE_TEST_INTERNAL = "internal";
    public static final String NAME_STATE_TEST_GOAL = "goal";
    public static final String NAME_OPOPTION_DEFAULT = "option_";

    protected HashMap<String, LearnedStateTest> nameToStateTest = new HashMap<String, LearnedStateTest>();
    protected SelectedHashableStateFactory typeSignature;
    protected OPOLearnedPF opoInitationPF;
    protected OPOLearnedPF opoGoalPF;

    @Override
    public Set<Option> generateOptions(OPOTrainer trainer) {

        OPODriver.log("making options...");

        State initialState = trainer.getInitialState();
        SADomain domain = trainer.getDomain();
        HashableStateFactory hashingFactory = trainer.getHashingFactory();

        LearnedStateTest initiation = nameToStateTest.get(OPOption.NAME_STATE_TEST_INTERNAL);
        LearnedStateTest goal = nameToStateTest.get(OPOption.NAME_STATE_TEST_GOAL);

        opoInitationPF.setLearnedTest(initiation);
        opoGoalPF.setLearnedTest(goal);

        List<GroundedProp> goalGPs = opoGoalPF.allGroundings((OOState)initialState);
        int numberPossibleOptions = goalGPs.size();
        OPODriver.log(numberPossibleOptions + " gps found, so there are that many possible grounded options");

        HashSet<Option> options = new HashSet<Option>();
        for (int i = 0; i < numberPossibleOptions; i++) {
            GroundedProp goalGP = goalGPs.get(i);
            // make a state condition test for initiation using same parameters as goal GP/PF
            GroundedProp initiationGP = new GroundedProp(opoInitationPF, goalGP.params);
            GroundedPropSC specificInitiation = new GroundedPropSC(initiationGP);
            GroundedPropSC specificGoal = new GroundedPropSC(goalGP);
            Planner planner = (Planner) trainer.initializeOptionPlanner(specificGoal);
            Policy optionPolicy = planner.planFromState(initialState);
            SubgoalOption option = new SubgoalOption(NAME_OPOPTION_DEFAULT + i, optionPolicy, specificInitiation, specificGoal);
            options.add(option);
        }

        return options;
    }

    public void addLearnedStateTest(LearnedStateTest test) {
        nameToStateTest.put(test.getName(), test);
    }

    public void setTypeSignature(SelectedHashableStateFactory typeSignature) {
        this.typeSignature = typeSignature;
    }

    public SelectedHashableStateFactory getTypeSignature() {
        return typeSignature;
    }

    public void setOPOGoalPF(OPOLearnedPF opoGoalPF) {
        this.opoGoalPF = opoGoalPF;
    }

    public void setOPOInitiationPF(OPOLearnedPF opoInitationPF) {
        this.opoInitationPF = opoInitationPF;
    }

}
