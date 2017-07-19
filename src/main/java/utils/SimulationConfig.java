package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.ExponentialDecayLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.BoltzmannQPolicy;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;

public class SimulationConfig {

	// specified by data file
	private int numEpisodes;
	protected int writeEvery;
	private int maxEpisodeSize;
	protected boolean useEpsilonDecay;
	protected double minEpsilonDecay;
	protected String outputPath;
	protected MDPSolver agent;
	
	// generated in code
	protected SADomain domain;
	protected State initialState;
	protected long seed;
	
	@SuppressWarnings("unchecked")
	public static SimulationConfig load(String filename, Class clasz) {

		RuntimeTypeAdapterFactory<MDPSolver> adapterAgent = RuntimeTypeAdapterFactory
				.of(MDPSolver.class, "type")
				.registerSubtype(QLearning.class, "QLearning")
				.registerSubtype(SarsaLam.class, "SarsaLam")
			;
		
		RuntimeTypeAdapterFactory<Policy> adapterPolicy = RuntimeTypeAdapterFactory
				.of(Policy.class, "type")
				.registerSubtype(BoltzmannQPolicy.class, "BoltzmannQPolicy")
				.registerSubtype(EpsilonGreedy.class, "EpsilonGreedy")
			;
		
		RuntimeTypeAdapterFactory<LearningRate> adapterLearningRate = RuntimeTypeAdapterFactory
				.of(LearningRate.class, "type")
				.registerSubtype(ExponentialDecayLR.class, "ExponentialDecayLR")
				.registerSubtype(ConstantLR.class, "ConstantLR")
			;
		
		RuntimeTypeAdapterFactory<QFunction> adapterQFunction = RuntimeTypeAdapterFactory
				.of(QFunction.class, "type")
				.registerSubtype(ConstantValueFunction.class, "ConstantValueFunction")
			;
		
		Gson configJson = new GsonBuilder()
				.registerTypeAdapterFactory(adapterAgent)
				.registerTypeAdapterFactory(adapterPolicy)
				.registerTypeAdapterFactory(adapterLearningRate)
				.registerTypeAdapterFactory(adapterQFunction)
				.create()
			;
	
		SimulationConfig config = null;
		try {
			config = configJson.fromJson(new FileReader(filename), clasz);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (config == null) {
			throw new RuntimeException("ERROR: failed to load config file for filename " + filename);
		}
		return config;
	}
	
	public int getNumEpisodes() {
		return numEpisodes;
	}

	public int getWriteEvery() {
		return writeEvery;
	}

	public int getMaxEpisodeSize() {
		return maxEpisodeSize;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public MDPSolver getAgent() {
		return agent;
	}
	
	public SADomain getDomain() {
		return domain;
	}
	
	public State getInitialState() {
		return initialState;
	}
	
	public long getSeed() {
		return seed;
	}

	public void setMaxEpisodeSize(int maxEpisodeSize) {
		this.maxEpisodeSize = maxEpisodeSize;
	}

	public void setNumEpisodes(int numEpisodes) {
		this.numEpisodes = numEpisodes;
	}

}
