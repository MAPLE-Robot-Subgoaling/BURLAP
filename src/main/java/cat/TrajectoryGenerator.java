package cat;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.List;

public class TrajectoryGenerator {

    public static List<Episode> generateQLearnedTrajectories(StateGenerator stateGen, int numTrajectories,
                                                             SADomain domain, double gamma, HashableStateFactory hashingFactory) {

        LearningAgent qlearner = new QLearning(domain, gamma, hashingFactory, 0, 0.01);
        SimulatedEnvironment env = new SimulatedEnvironment(domain, stateGen);
        List<Episode> episodes = new ArrayList<Episode>();
        for (int i = 0; i < numTrajectories; i++) {
            Episode e = qlearner.runLearningEpisode(env);
            episodes.add(e);
            env.resetEnvironment();
        }

        return episodes;
    }

    public static List<Episode> generateVIPlannedTrajectories(StateGenerator stateGen, int numTrajectories,
                                                              SADomain domain, double gamma, HashableStateFactory hashingFactory,
                                                              double maxDelta, int maxIterations) {
        List<Episode> trajectories = new ArrayList<Episode>();

        ValueIteration vi = new ValueIteration(domain, gamma, hashingFactory, maxDelta, maxIterations);

        for (int i = 0; i < numTrajectories; i++) {
            State s = stateGen.generateState();
            Policy p = vi.planFromState(s);
            Episode e = PolicyUtils.rollout(p, s, domain.getModel());
            trajectories.add(e);
        }
        return trajectories;
    }
}
