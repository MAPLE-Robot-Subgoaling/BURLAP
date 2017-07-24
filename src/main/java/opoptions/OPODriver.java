package opoptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
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
	
	public void collectTrajectories() {
		for (OPOTrainer trainer : trainers) {
			collectTrajectories(trainer);
		}
	}

	public void collectTrajectories(OPOTrainer trainer) {
		boolean moveFile = MOVE_FILE;
		String path = getOutputFilename(trainer);
		String csvPath = path + ".csv";
//		String serializationFile = setupSerializationFile(path, moveFile);
		log(csvPath);
		try {
			File file = new File(csvPath);
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);

			int numTrajectories = trainingSeeds.size();
			log("Beginning " + numTrajectories + " trajectories");
			for (int i = 0; i < numTrajectories; i++) {
				Long seed = trainingSeeds.get(i);
				log("\nTrainer: " + trainer.getTrainerName() + " (" + trainer.getDomainName() + " domain)");
				log("Seed " + (i+1) + " / " + numTrajectories + ": " + seed);
				trainer.setSeed(seed);
				trainer.runTraining(writer, null);

			}
			
			writer.flush();
			writer.close();
			
			log("not calling csvToArff yet");
//			csvToArff(path);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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

	public static void main(String[] args) {
		
		Random rng = new Random();
		
		String outputPath = "./opoptions";
		String outputPrefix = "";
		
		boolean debugMode = true;
		DPrint.toggleCode(DEBUG_CODE, debugMode);
		
		OPODriver driver = new OPODriver(outputPath, outputPrefix);
		long initSeedTraining = rng.nextLong();//2103460911L;
		int numSeedsTraining = 1;
		driver.addSeedsTo(driver.getTrainingSeeds(), initSeedTraining, numSeedsTraining);
		log(initSeedTraining + ": " + driver.getTrainingSeeds());
//		long initSeedEvaluation = rng.nextLong();
//		int numSeedsEvaluation = 5;
//		driver.addSeedsTo(driver.getEvaluationSeeds(), initSeedEvaluation, numSeedsEvaluation);
//		log(initSeedEvaluation + ": " + driver.getEvaluationSeeds());
		
		driver.addTrainers();
		
		driver.collectTrajectories();
		
		driver.runVisualizer();
		
//		driver.runEvaluation();
		
	}

	
}
