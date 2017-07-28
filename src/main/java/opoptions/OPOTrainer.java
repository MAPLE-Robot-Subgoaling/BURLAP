package opoptions;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.auxiliary.performance.PerformancePlotter;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.vfa.ApproximateQLearning;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import utils.Simulation;
import utils.SimulationConfig;
import weka.classifiers.Classifier;

public abstract class OPOTrainer extends SimulationConfig {
	
	// by default, use 0 (BURLAP default)
	// to use different index, subclasses should override the getIndexForRandomFactory method
	public static final int DEFAULT_RNG_INDEX = 0;


	protected Classifier classifier;
	protected boolean identifierIndependentHashing = true;
	protected String trainerName = "unsetTrainer";
	protected String domainName = "unsetDomain";
	protected RewardFunction rf;
	protected TerminalFunction tf;
	protected HashableStateFactory hashingFactory;
	protected Environment env;
	
	private String lastTrainingSeedTimestamp = "unsetSeedTimestamp";

	public String getTrainerName() {
		return trainerName;
	}
	
	public void setTrainerName(String trainerName) {
		this.trainerName = trainerName;
	}
	
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	public boolean isIdentifierIndependentHashing() {
		return identifierIndependentHashing;
	}

	public void setIdentifierIndependentHashing(boolean identifierIndependentHashing) {
		this.identifierIndependentHashing = identifierIndependentHashing;
	}
	
	public abstract PropositionalFunction getGoalPF();
	
	public boolean satisfiesGoal(OOState s) {
		return getGoalPF().someGroundingIsTrue(s);
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

	public HashableStateFactory getHashingFactory() {
		return hashingFactory;
	}

	public void setHashingFactory(HashableStateFactory hashingFactory) {
		this.hashingFactory = hashingFactory;
	}

	public String getLastTrainingSeedTimestamp() {
		return lastTrainingSeedTimestamp;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
		RandomFactory.seedMapped(getIndexForRandomFactory(), this.seed);
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	private int getIndexForRandomFactory() {
		return DEFAULT_RNG_INDEX;
	}

	public abstract OOState setupStateTraining();
	
	public abstract OOSADomain setupDomain();
	
	public MDPSolver setupAgent() {
		agent.resetSolver();
		agent.setDomain(domain);
		hashingFactory = new SimpleHashableStateFactory(identifierIndependentHashing);
		agent.setHashingFactory(hashingFactory);
		return agent;
	}
	
	public String planAndRollout(PerformancePlotter plotter) {
		String seedTimestamp = Simulation.plan(this);
		return seedTimestamp;
	}

	public abstract void runEpisodeVisualizer(String filePrefix);
	
	public void runTraining(PerformancePlotter plotter) {

		// 1. setup the state
		// 2. setup the domain
		// 3. setup the agent
		// 4. run the simulation
		
		// 1. setup the state
		setupStateTraining();
		
		// 2. setup the domain
		setupDomain();
		
		// 3. setup the agent
		setupAgent();
		
		// 4. run the simulation
		String seedTimestamp = planAndRollout(plotter);
		lastTrainingSeedTimestamp = seedTimestamp;
		
	}
	
	public void runEvaluation(String serializationFile) {
		throw new RuntimeException("runEvaluation not implemented");
	}

}
