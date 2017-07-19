package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.performance.PerformancePlotter;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.learning.tdmethods.vfa.ApproximateQLearning;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;

public class Simulation {
	
	public static EnvironmentServer environmentServer;
	public static SimulatedEnvironment env;
	public static MDPSolver agent;
	
	public static String run(SimulationConfig config, PerformancePlotter plotter) {
		
		long seed = config.getSeed();
		int numEpisodes = config.getNumEpisodes();
		int maxEpisodeSize = config.getMaxEpisodeSize();
		int writeEvery = config.getWriteEvery();
		String outputPath = config.getOutputPath();
		SADomain domain = config.getDomain();
		State initialState = config.getInitialState();
		agent = config.getAgent();
		Policy policy =  null;
		if (agent instanceof QLearning) {
			QLearning ql = (QLearning) agent;
			policy = ql.getLearningPolicy();
		} else {
			throw new RuntimeException("Error: unknown class specified for agent in Simulation.run");
		}
		
		env = new SimulatedEnvironment(domain, initialState);
		if (plotter != null) {
			environmentServer = new EnvironmentServer(env, plotter);
			plotter.toggleDataCollection(true);
			plotter.startNewTrial();
		}
		
		String timestamp = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString();
		String filePrepend = seed + "_" + timestamp;
		for(int i = 0; i < numEpisodes; i++){
			
			double decayedEpsilon = 0.0;
			if (config.useEpsilonDecay && policy instanceof EpsilonGreedy) {
				decayedEpsilon = (numEpisodes-i)/(1.0*numEpisodes);
				if (decayedEpsilon < config.minEpsilonDecay) { decayedEpsilon = config.minEpsilonDecay; }
				((EpsilonGreedy)policy).setEpsilon(decayedEpsilon);
			}
			Environment environment = env;
			if (plotter != null) {
				environment = environmentServer;
			}
			Episode e = ((LearningAgent) agent).runLearningEpisode(environment, maxEpisodeSize);
			if (plotter != null) { plotter.endEpisode(); }
			if (i % writeEvery == 0) {
				e.write(outputPath + filePrepend + "/" + i);
			}
			String extra = "";
			if (config.useEpsilonDecay) {
				System.out.println(i + ": " + e.maxTimeStep() + " steps, " + e.discountedReturn(agent.getGamma()) + " discounted return, epsilon: " + decayedEpsilon + extra);
			} else {
				System.out.println(i + ": " + e.maxTimeStep() + " steps, " + e.discountedReturn(agent.getGamma()) + " discounted return" + extra);
			}
			environment.resetEnvironment();
//			env.resetEnvironment();
//			environmentServer.resetEnvironment();
		}
		if (plotter != null) {
			plotter.endTrial();
		}

		Policy tempPolicy = null;
		if (agent instanceof QLearning) {
			QLearning ql = (QLearning) agent;
			tempPolicy = ql.getLearningPolicy();
			ql.setLearningPolicy(new EpsilonGreedy(ql, 0));
		} else {
			throw new RuntimeException("ERROR: learningPolicy setting not implemented for given algo");
		}
		Episode e = ((LearningAgent) agent).runLearningEpisode(env, maxEpisodeSize);
		System.out.println(e.actionSequence);
		System.out.println( "exploit: " + e.maxTimeStep() + " steps, " + e.discountedReturn(agent.getGamma()) + " discounted return");
		env.resetEnvironment();
//		environmentServer.resetEnvironment();

		if (agent instanceof QLearning) {
			QLearning ql = (QLearning) agent;
			ql.setLearningPolicy(tempPolicy);
		} else {
			throw new RuntimeException("ERROR: learningPolicy setting not implemented for given algo");
		}
		
		return filePrepend;
	}
	

}
