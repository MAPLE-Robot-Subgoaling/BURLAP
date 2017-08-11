package opoptions;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.Planner;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import cleanup.GroundedPropSC;

import java.util.*;

public class OPOption implements OptionGenerator {

    public static final String NAME_STATE_TEST_INTERNAL = "internal";
    public static final String NAME_STATE_TEST_GOAL = "goal";
    public static final String NAME_OPOPTION_DEFAULT = "option_";

    protected HashMap<String, LearnedStateTest> nameToStateTest = new HashMap<String, LearnedStateTest>();
    protected SelectedHashableStateFactory typeSignature;
    protected OPOGoalPF opoGoalPF;

    @Override
    public Set<Option> generateOptions(OPOTrainer trainer) {

        OPODriver.log("making options...");

        State initialState = trainer.getInitialState();
        SADomain domain = trainer.getDomain();
        HashableStateFactory hashingFactory = trainer.getHashingFactory();

        LearnedStateTest initiation = nameToStateTest.get(OPOption.NAME_STATE_TEST_INTERNAL);
        LearnedStateTest goal = nameToStateTest.get(OPOption.NAME_STATE_TEST_GOAL);
        List<State> states = StateReachability.getReachableStates(initialState, domain, hashingFactory);

        opoGoalPF.setGoalTest(goal);

        List<GroundedProp> gps = opoGoalPF.allGroundings((OOState)initialState);
        int numberPossibleOptions = gps.size();
        OPODriver.log(numberPossibleOptions + " gps found, so there are that many possible grounded options");

        HashSet<Option> options = new HashSet<Option>();
        for (int i = 0; i < numberPossibleOptions; i++) {
            GroundedProp gp = gps.get(i);
//            String[] params = gp.params;
            GroundedPropSC specificGoal = new GroundedPropSC(gp);
            Planner planner = (Planner) trainer.initializeOptionPlanner(specificGoal);
            Policy optionPolicy = planner.planFromState(initialState);
            SubgoalOption option = new SubgoalOption(NAME_OPOPTION_DEFAULT + i, optionPolicy, initiation, specificGoal);
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

    public void setGoalPF(OPOGoalPF opoGoalPF) {
        this.opoGoalPF = opoGoalPF;
    }
}
