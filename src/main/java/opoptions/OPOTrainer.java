package opoptions;

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
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import utils.Simulation;
import utils.SimulationConfig;

public abstract class OPOTrainer extends SimulationConfig {
	
	// by default, use 0 (BURLAP default)
	// to use different index, subclasses should override the getIndexForRandomFactory method
	public static final int DEFAULT_RNG_INDEX = 0;
	
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

	public String setupSerializationFile(String path, boolean moveFile) {
		String prepend = path;
		String outYaml = prepend + ".yaml";
		System.out.println("looking for serialization file " + prepend + " and out " + outYaml);
		if (moveFile) {
			String moveYaml = prepend + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString() + ".yaml";
			Path filePathSource = Paths.get(outYaml);
			Path filePathDestination = Paths.get(moveYaml);
			try {
				Files.move(filePathSource, filePathDestination);
			} catch (IOException e1) {
				// do nothing
			}
		}
		String serializationFile = outYaml;
		return serializationFile;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
		RandomFactory.seedMapped(getIndexForRandomFactory(), this.seed);
	}

	private int getIndexForRandomFactory() {
		return DEFAULT_RNG_INDEX;
	}
	
	public abstract OOState setupStateTraining();
	
	public abstract OOSADomain setupDomain();
	
	public MDPSolver setupAgent(String serializationFile) {
		agent.resetSolver();
		agent.setDomain(domain);
		hashingFactory = new SimpleHashableStateFactory(identifierIndependentHashing);
		agent.setHashingFactory(hashingFactory);
		Policy policy =  null;
		if (agent instanceof QLearning) {
			QLearning ql = (QLearning) agent;
			policy = ql.getLearningPolicy();
		} else {
			throw new RuntimeException("error: unknown agent type specified in setupAgent");
		}
		((SolverDerivedPolicy)policy).setSolver(agent);
		return agent;
	}
	
	public String runSimulation(PerformancePlotter plotter) {
		String seedTimestamp = Simulation.run(this, plotter);
		return seedTimestamp;
	}
	
	public void serializeAndWrite(String serializationFile) {
		System.err.println("serializeAndWrite not implemented");
	}

	public void runTraining(String serializationFile, PerformancePlotter plotter) {

		// 1. setup the state
		// 2. setup the domain
		// 3. setup the agent
		// 4. run the simulation
		// 5. serialize (if needed)
		
		// 1. setup the state
		setupStateTraining();
		
		// 2. setup the domain
		setupDomain();
		
		// 3. setup the agent
		setupAgent(serializationFile);
		
		// 4. run the simulation
		String seedTimestamp = runSimulation(plotter);
		lastTrainingSeedTimestamp = seedTimestamp;
		
		// 5. serialize if needed
		serializeAndWrite(serializationFile);
		
	}
	
	public void runEvaluation(String serializationFile) {
		// TODO Auto-generated method stub
		System.err.println("runEvaluation not implemented");
	}

}
