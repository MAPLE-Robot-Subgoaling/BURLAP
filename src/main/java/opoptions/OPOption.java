package opoptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.Planner;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;

public class OPOption implements OptionGenerator {

	public static final String NAME_STATE_TEST_INTERNAL = "internal";
	public static final String NAME_STATE_TEST_GOAL = "goal";
	public static final String NAME_OPOPTION_DEFAULT = "option_";

    protected HashMap<String, LearnedStateTest> nameToStateTest = new HashMap<String, LearnedStateTest>();
    
	public Set<Option> generateOptions(OPOTrainer trainer) {
		
		OPODriver.log("making options...");
		
		State initialState = trainer.getInitialState();
		SADomain domain = trainer.getDomain();
		HashableStateFactory hashingFactory = trainer.getHashingFactory();
		
		StateConditionTest initiation = nameToStateTest.get(OPOption.NAME_STATE_TEST_INTERNAL);
		StateConditionTest goal = nameToStateTest.get(OPOption.NAME_STATE_TEST_GOAL);
		
		List<State> states = StateReachability.getReachableStates(initialState, domain, hashingFactory);
        List<State> endStates = new ArrayList<State>();
        for (int i = 0; i < states.size(); i++) {
        	State state = states.get(i);
        	if (goal.satisfies(state)) {
        		endStates.add(state);
        	}
        }
		OPODriver.log("found " + states.size() + " states and " + endStates.size() + " endStates");
        
        HashSet<Option> options = new HashSet<Option>();
        for (int i = 0; i < endStates.size(); i++) {
        	State endState = endStates.get(i);
        	StateConditionTest specificGoal = new InStateTest(endState);
        	Planner planner = (Planner) trainer.initializeOptionPlanner(specificGoal);
            Policy optionPolicy = planner.planFromState(initialState);
    		SubgoalOption option = new SubgoalOption(NAME_OPOPTION_DEFAULT+i, optionPolicy, initiation, specificGoal);
    		options.add(option);
        }
		OPODriver.log("made " + options.size() + " options");
		
        return options;
	}

	public void addLearnedStateTest(LearnedStateTest test) {
		nameToStateTest.put(test.getName(), test);
	}

}
