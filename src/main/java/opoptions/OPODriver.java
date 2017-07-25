package opoptions;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.Episode;
import burlap.datastructures.AlphanumericSorting;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import opoptions.trainers.OPOCleanup;
import utils.SimulationConfig;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class OPODriver {

	public static final int DEBUG_CODE = 513249;

	public static boolean MOVE_FILE = true;
	
	private String outputPath;
	private String outputPrefix;
	
	private List<OPOTrainer> trainers;
	private List<Long> trainingSeeds;
	private List<Long> evaluationSeeds;
	
	public OPODriver(String outputPath, String outputPrefix) {
		this.outputPath = outputPath;
		this.outputPrefix = outputPrefix;
		trainers = new ArrayList<OPOTrainer>();
		trainingSeeds = new ArrayList<Long>();
		evaluationSeeds = new ArrayList<Long>();
	}
	
	public String getOutputFilename(OPOTrainer trainer) {
		return outputPath + outputPrefix + trainer.getTrainerName();
	}

	public void addTrainer(OPOTrainer trainer) {
		trainers.add(trainer);
	}
	
	public void addTrainingSeed(Long seed) {
		trainingSeeds.add(seed);
	}
	
	public void addEvaluationSeed(Long seed) {
		evaluationSeeds.add(seed);
	}
	
	public List<OPOTrainer> getTrainers() {
		return trainers;
	}
	
	public List<Long> getTrainingSeeds() {
		return trainingSeeds;
	}

	public List<Long> getEvaluationSeeds() {
		return evaluationSeeds;
	}

	public void addSeedsTo(List<Long> seeds, long initSeed, int numSeeds) {
		DPrint.cl(DEBUG_CODE, "adding " + numSeeds + " from initial seed " + initSeed);
		Random rng = new Random(initSeed); 
		for (int i = 0; i < numSeeds; i++) {
			seeds.add(rng.nextLong());
		}
	}
	
	public static void log(Object object) {
		if (object != null) {
			DPrint.cl(DEBUG_CODE, object.toString());
		} else {
			DPrint.cl(DEBUG_CODE, null);
		}
	}
	
	public void runTraining() {
		for (OPOTrainer trainer : trainers) {
			runTraining(trainer);
		}
	}

	public void runTraining(OPOTrainer trainer) {
		
		int numTrajectories = trainingSeeds.size();
		log("Beginning " + numTrajectories + " trajectories");
		for (int i = 0; i < numTrajectories; i++) {
			Long seed = trainingSeeds.get(i);
			log("\nTrainer: " + trainer.getTrainerName() + " (" + trainer.getDomainName() + " domain)");
			log("Seed " + (i+1) + " / " + numTrajectories + ": " + seed);
			trainer.setSeed(seed);
			trainer.runTraining(null);
		}
		
	}

	public static void csvToArff(String filenamePrefix) throws Exception {
	    // load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(filenamePrefix + ".csv"));
	    Instances data = loader.getDataSet();
	    NumericToNominal nm = new NumericToNominal();
		nm.setInputFormat(data);
	    String[] args = new String[2];
	    args[0] = "-R";
//	    //args[1] = trainer.getFirstPropositionalFunctionIndex() + "-last";
//	    //args[1] = "3-last";
	    args[1] = "last";
		nm.setOptions(args);
		data = Filter.useFilter(data, nm);
	    
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(filenamePrefix + ".arff"));
	    saver.writeBatch();
	}
	
//	public static String setupSerializationFile(String path, boolean moveFile) {
//		String prepend = path;
//		String outYaml = prepend + ".yaml";
//		log("looking for serialization file " + prepend + " and out " + outYaml);
//		if (moveFile) {
//			String moveYaml = prepend + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString() + ".yaml";
//			Path filePathSource = Paths.get(outYaml);
//			Path filePathDestination = Paths.get(moveYaml);
//			try {
//				Files.move(filePathSource, filePathDestination);
//			} catch (IOException e1) {
//				// do nothing
//			}
//		}
//		String serializationFile = outYaml;
//		return serializationFile;
//	}
	
//	public void runEvaluation(OPOTrainer trainer) {
//		boolean moveFile = true;
//		String serializationFile = trainer.setupSerializationFile(trainer.getOutputPath() + trainer.getDomainName(), moveFile);
//		for (int i = 0; i < evaluationSeeds.size(); i++) {
//			Long seed = evaluationSeeds.get(i);
//			log("\nTrainer: " + trainer.getTrainerName() + " (" + trainer.getDomainName() + " domain)");
//			log("Eval " + (i+1) + " / " + evaluationSeeds.size() + ": " + seed);
//			trainer.setSeed(seed);
//			trainer.runEvaluation(serializationFile);
//		}
//	}
//	
//	public void runEvaluation() {
//		for (OPOTrainer trainer : trainers) {
//			runEvaluation(trainer);
//		}
//	}
	
	public void addTrainers() {
		OPOCleanup moveToDoor = (OPOCleanup) SimulationConfig.load("./config/moveToDoor.yaml", OPOCleanup.class);
		addTrainer(moveToDoor);
	}

	private void runVisualizer() {
		for (OPOTrainer trainer : trainers) {
			trainer.runEpisodeVisualizer("");
		}
	}

	private void collectDataset() {
		for (OPOTrainer trainer : trainers) {
			collectDataset(trainer);
		}
	}
	
	private List<OOState> getStates(OPOTrainer trainer) {
		List<OOState> allStates = new ArrayList<OOState>();
		List<Episode> episodes = Episode.readEpisodes(trainer.getOutputPath());
		log(episodes.size());
		for (Episode episode : episodes) {
			List<State> states = episode.stateSequence;
			for (State state : states) {
				OOState s = (OOState) state;
				allStates.add(s);
			}
		}
		return allStates;
	}

	private void writeFeatureVectorHeader(FileWriter writer, OOState state) throws IOException {
		StringBuilder sb = new StringBuilder();
		String label = "label";
		List<ObjectInstance> objects = state.objects();
		for (ObjectInstance object : objects) {
			for (Object variableKey : object.variableKeys()) {
				String key = variableKey.toString();
				sb.append(object.className());
				sb.append(":");
				sb.append(key);
				sb.append(",");
			}
		}
		sb.append(label);
		sb.append('\n');
		writer.append(sb.toString());
	}
	
	private void writeFeatureVector(FileWriter writer, OOState state, OPOTrainer trainer) throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean isGoal = trainer.satisfiesGoal(state);
		String label = isGoal ? "goal" : "internal";
		List<ObjectInstance> objects = state.objects();
		for (ObjectInstance object : objects) {
			for (Object variableKey : object.variableKeys()) {
				String key = variableKey.toString();
				String val = object.get(key).toString();
				sb.append(val);
				sb.append(",");
			}
		}
		sb.append(label);
		sb.append('\n');
		writer.append(sb.toString());
	}
	
	private void collectDataset(OPOTrainer trainer) {
		try {
			String path = getOutputFilename(trainer);
			String csvPath = path + ".csv";
			log("saving to " + csvPath);
			List<OOState> states = getStates(trainer);
			File file = new File(csvPath);
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			writeFeatureVectorHeader(writer, states.get(0));
			for (OOState state : states) {
				writeFeatureVector(writer, state, trainer);
			}
			writer.flush();
			writer.close();
			csvToArff(path);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		
		Random rng = new Random();
		
		String outputPath = "./opoptions";
		String outputPrefix = "";
		
		boolean debugMode = true;
		DPrint.toggleCode(DEBUG_CODE, debugMode);
		
		OPODriver driver = new OPODriver(outputPath, outputPrefix);
		long initSeedTraining = rng.nextLong();//2103460911L;
		int numSeedsTraining = 100;
		driver.addSeedsTo(driver.getTrainingSeeds(), initSeedTraining, numSeedsTraining);
		log(initSeedTraining + ": " + driver.getTrainingSeeds());
//		long initSeedEvaluation = rng.nextLong();
//		int numSeedsEvaluation = 5;
//		driver.addSeedsTo(driver.getEvaluationSeeds(), initSeedEvaluation, numSeedsEvaluation);
//		log(initSeedEvaluation + ": " + driver.getEvaluationSeeds());
		
		driver.addTrainers();
		
		driver.runTraining();
		
		driver.collectDataset();
		
		driver.runVisualizer();
		
//		driver.runEvaluation();
		
	}
	
}
