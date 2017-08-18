package utils;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.ExponentialDecayLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.BoltzmannQPolicy;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class SimulationConfig {

    // specified by data file
    private int numEpisodes;
    protected int writeEvery;
    private int maxEpisodeSize;
    protected boolean useEpsilonDecay;
    protected double minEpsilonDecay;
    protected String outputPath;
    protected String episodeOutputPath;
    protected MDPSolver agent;
    protected Planner planner;

    // generated in code
    protected SADomain domain;
    protected State initialState;
    protected long seed;

    @SuppressWarnings("unchecked")
    public static SimulationConfig load(String filename, Class clasz) {

//		RuntimeTypeAdapterFactory<Planner> adapterPlanner = RuntimeTypeAdapterFactory
//				.of(Planner.class, "type")
//				.registerSubtype(ValueIteration.class, "ValueIteration")
//			;

        RuntimeTypeAdapterFactory<MDPSolver> adapterAgent = RuntimeTypeAdapterFactory
                .of(MDPSolver.class, "type")
                .registerSubtype(QLearning.class, "QLearning")
                .registerSubtype(SarsaLam.class, "SarsaLam")
                .registerSubtype(ValueIteration.class, "ValueIteration");

        RuntimeTypeAdapterFactory<Policy> adapterPolicy = RuntimeTypeAdapterFactory
                .of(Policy.class, "type")
                .registerSubtype(BoltzmannQPolicy.class, "BoltzmannQPolicy")
                .registerSubtype(EpsilonGreedy.class, "EpsilonGreedy");

        RuntimeTypeAdapterFactory<Classifier> adapterClassifier = RuntimeTypeAdapterFactory
                .of(Classifier.class, "type")
                .registerSubtype(J48.class, "J48")
                .registerSubtype(MultilayerPerceptron.class, "MultilayerPerceptron")
                .registerSubtype(ZeroR.class, "ZeroR");

        RuntimeTypeAdapterFactory<LearningRate> adapterLearningRate = RuntimeTypeAdapterFactory
                .of(LearningRate.class, "type")
                .registerSubtype(ExponentialDecayLR.class, "ExponentialDecayLR")
                .registerSubtype(ConstantLR.class, "ConstantLR");

        RuntimeTypeAdapterFactory<QFunction> adapterQFunction = RuntimeTypeAdapterFactory
                .of(QFunction.class, "type")
                .registerSubtype(ConstantValueFunction.class, "ConstantValueFunction");

        RuntimeTypeAdapterFactory<ValueFunction> adapterValueFunction = RuntimeTypeAdapterFactory
                .of(ValueFunction.class, "type")
                .registerSubtype(ConstantValueFunction.class, "ConstantValueFunction");

        ExclusionStrategy excludeJFrames = new ExclusionStrategy() {
            private final Class<?> excludedThisClass = JFrame.class;

            public boolean shouldSkipClass(Class<?> clazz) {
                return excludedThisClass.equals(clazz);
            }

            public boolean shouldSkipField(FieldAttributes f) {
                return excludedThisClass.equals(f.getDeclaredClass());
            }
        };

        Gson configJson = new GsonBuilder()
                .addDeserializationExclusionStrategy(excludeJFrames)
//				.registerTypeAdapterFactory(adapterPlanner)
                .registerTypeAdapterFactory(adapterAgent)
                .registerTypeAdapterFactory(adapterPolicy)
                .registerTypeAdapterFactory(adapterLearningRate)
                .registerTypeAdapterFactory(adapterQFunction)
                .registerTypeAdapterFactory(adapterValueFunction)
                .registerTypeAdapterFactory(adapterClassifier)
                .create();

        SimulationConfig config = null;
        try {
            config = (SimulationConfig) configJson.fromJson(new FileReader(filename), clasz);
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

    public String getEpisodeOutputPath() {
        return episodeOutputPath;
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

//	public Planner getPlanner() {
//		return planner;
//	}
//	
//	public void setPlanner(Planner planner) {
//		this.planner = planner;
//	}

}
