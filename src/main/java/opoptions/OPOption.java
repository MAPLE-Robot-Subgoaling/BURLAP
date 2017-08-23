package opoptions;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalBoundedOption;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.selected.SelectedHashableStateFactory;
import cleanup.Cleanup;
import cleanup.CleanupVisualizer;
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
        SADomain domain = trainer.setupDomainNoRFTF();
        HashableStateFactory hashingFactory = trainer.getHashingFactory();

        LearnedStateTest initiation = nameToStateTest.get(OPOption.NAME_STATE_TEST_INTERNAL);
        LearnedStateTest goal = nameToStateTest.get(OPOption.NAME_STATE_TEST_GOAL);

        opoInitationPF.setLearnedTest(initiation);
        opoGoalPF.setLearnedTest(goal);

        List<GroundedProp> goalGPs = opoGoalPF.allGroundings((OOState)initialState);
        int numberPossibleOptions = goalGPs.size();
        OPODriver.log(numberPossibleOptions + " options will be made (based on that many gps found)");


        List<State> states = StateReachability.getReachableStates(initialState, domain, hashingFactory);
        Set<State> goalStates = new HashSet<State>();
        for (State state : states) {
            for (GroundedProp gp : goalGPs) {
                if(gp.isTrue((OOState) state)) {
//                    OPODri/ver.log(gp.toString() + " " + state);
                    goalStates.add(state);
                }
            }
        }
        for (State state : goalStates) {
            OPODriver.log(state);
            List<Episode> episodeList = new ArrayList<>();
            episodeList.add(new Episode(state));
            trainer.runEpisodeVisualizer(episodeList);
        }
        OPODriver.log(goalStates.size());

        return null;
        /*

        HashSet<Option> options = new HashSet<Option>();
        for (int i = 0; i < numberPossibleOptions; i++) {
            String name = NAME_OPOPTION_DEFAULT + i;
            GroundedProp goalGP = goalGPs.get(i);
            // make a state condition test for initiation using same parameters as goal GP/PF
            OPODriver.log("Type Signature for " + name + ": " + Arrays.toString(goalGP.params));
            GroundedProp initiationGP = new GroundedProp(opoInitationPF, goalGP.params);
            GroundedPropSC specificInitiation = new GroundedPropSC(initiationGP);
            GroundedPropSC specificGoal = new GroundedPropSC(goalGP);

            Planner planner = (Planner) trainer.initializeOptionPlanner(specificGoal);
//            Policy optionPolicy = planner.planFromState(initialState);
//            Policy optionPolicy = new EpsilonGreedy((QProvider) planner, 0.1);
            Policy optionPolicy = new DDPlannerPolicy((DeterministicPlanner) planner);
//            Policy optionPolicy = null;
//            for (State state : states) {
//                optionPolicy = planner.planFromState(state);
//            }
//            for (int stateIdx = 0; stateIdx < states.size(); stateIdx++) {
//                State state = states.get(stateIdx);
//                if(specificGoal.satisfies(state)) { OPODriver.log(state); }
//            }

//            Episode episode = PolicyUtils.rollout(optionPolicy, initialState, ((OOSADomain)planner.getDomain()).getModel(), 256);
//            List<Episode> episodes = new ArrayList<Episode>();
//            episodes.add(episode);
//            EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(CleanupVisualizer.getVisualizer(
//                    ((Cleanup)trainer.getDomainGenerator()).getWidth(),
//                    ((Cleanup)trainer.getDomainGenerator()).getHeight()),
//                    planner.getDomain(), episodes);
//            esv.initGUI();

            SubgoalBoundedOption option = new SubgoalBoundedOption(name, optionPolicy, specificInitiation, specificGoal);
            options.add(option);
        }

        return options;
        */
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