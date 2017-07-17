package burlap.behavior.singleagent.opoptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.opoptions.trainers.OPOCleanup;
import burlap.debugtools.DPrint;
import utils.SimulationConfig;

public class OPODriver {

	public static final int DEBUG_CODE = 513249;
	
	public static boolean MOVE_FILE = true;
	
	private List<OPOTrainer> trainers;
	private List<Long> trainingSeeds;
	private List<Long> evaluationSeeds;
	
	public OPODriver() {
		trainers = new ArrayList<OPOTrainer>();
		trainingSeeds = new ArrayList<Long>();
		evaluationSeeds = new ArrayList<Long>();
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
	
	public void runEvaluation() {
		for (OPOTrainer trainer : trainers) {
			runEvaluation(trainer);
		}
	}

	public void runTraining(OPOTrainer trainer) {
		boolean moveFile = MOVE_FILE;
		String serializationFile = trainer.setupSerializationFile(trainer.getOutputPath() + trainer.getDomainName(), moveFile);
		for (int i = 0; i < trainingSeeds.size(); i++) {
			Long seed = trainingSeeds.get(i);
			log("\nTrainer: " + trainer.getTrainerName() + " (" + trainer.getDomainName() + " domain)");
			log("Seed " + (i+1) + " / " + trainingSeeds.size() + ": " + seed);
			trainer.setSeed(seed);
			trainer.runTraining(serializationFile);
		}	
	}
	
	public void runEvaluation(OPOTrainer trainer) {
		boolean moveFile = true;
		String serializationFile = trainer.setupSerializationFile(trainer.getOutputPath() + trainer.getDomainName(), moveFile);
		for (int i = 0; i < evaluationSeeds.size(); i++) {
			Long seed = evaluationSeeds.get(i);
			log("\nTrainer: " + trainer.getTrainerName() + " (" + trainer.getDomainName() + " domain)");
			log("Eval " + (i+1) + " / " + evaluationSeeds.size() + ": " + seed);
			trainer.setSeed(seed);
			trainer.runEvaluation(serializationFile);
		}
	}
	
	public void addTrainers() {
		OPOCleanup moveToDoor = (OPOCleanup) SimulationConfig.load("./config/moveToDoor.json", OPOCleanup.class);
		addTrainer(moveToDoor);
	}

	public static void main(String[] args) {
		
		String output = "./output";
		
		boolean debugMode = true;
		DPrint.toggleCode(DEBUG_CODE, debugMode);
		
		OPODriver driver = new OPODriver();
		long initSeedTraining = 210340911L;
		long initSeedEvaluation = 939874L;
		int numSeedsTraining = 10;
		int numSeedsEvaluation = 5;
		driver.addSeedsTo(driver.getTrainingSeeds(), initSeedTraining, numSeedsTraining);
		driver.addSeedsTo(driver.getEvaluationSeeds(), initSeedEvaluation, numSeedsEvaluation);
		log(initSeedTraining + ": " + driver.getTrainingSeeds());
		log(initSeedEvaluation + ": " + driver.getEvaluationSeeds());
		
		driver.addTrainers();
		driver.runTraining();
		driver.runEvaluation();
		
	}
	
}
