package poption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalBoundedOption;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.masked.MaskedHashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import poption.domain.cleanup.CleanupWorld;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class PoptionsDriver {
	
	private PoptionsTrainer trainer;
	private String outputPath;
	private String outputPrefix;
	private Classifier model;
//	private Instances stateInstances;
	private SimpleHashableStateFactory hf;

	public String optionNamePrefix = "poption";
	public static final int RANDOM_FACTORY_SEED_COLLECT_DATA = 52033;
	public static final int RANDOM_FACTORY_SEED_TRAIN = 930241;
	public static final int NUM_FOLDS_CROSSVALIDATION = 5;
    public static final int DEBUG_CODE = 513249;
	
	public PoptionsDriver(PoptionsTrainer trainer, Classifier model, String outputPath, String csvPrefix) {
		this.trainer = trainer;
		this.outputPath = outputPath;
		this.outputPrefix = csvPrefix;
		this.model = model;
		this.hf = new SimpleHashableStateFactory(true);
	}
	
	public String getOutputFilename() {
		return outputPath + outputPrefix + trainer.getName();
	}
	
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}
	
	public boolean runTrial(Random rng, FileWriter writer, int trial) throws IOException {

		boolean trainingSuccess = false;
		while (!trainingSuccess) {
			long seed = rng.nextLong();
			DPrint.cl(DEBUG_CODE,"Trial " + trial + " using seed " + seed);
			trainer.initialize(seed, false);
			trainingSuccess = trainer.train(outputPath + trial + "_");
			if (!trainingSuccess) {
				DPrint.cl(DEBUG_CODE,"Failed to finish, choosing new seed");
			}
		}
		
		if (writer != null) {
			if (trial == 0) { writer.append(trainer.getFeatureVectorHeader()+"\n"); }
			List<double[]> fvs = trainer.getTrainingFeatureVectors();
			for (double[] fv : fvs) {
				StringBuilder sb = new StringBuilder();
				for (double d : fv) {
					sb.append(d);
					sb.append(',');
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append('\n');
				writer.append(sb.toString());
			}
		}

		return true;
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
	
	public void collectData(int numTrials, long seed) {
		try {
			File file = new File(getOutputFilename() + ".csv");
			file.getParentFile().mkdirs();
			FileWriter writer = new FileWriter(file);
			RandomFactory.seedMapped(RANDOM_FACTORY_SEED_COLLECT_DATA, seed);
			Random rng = RandomFactory.getMapped(RANDOM_FACTORY_SEED_COLLECT_DATA);

			DPrint.cl(DEBUG_CODE,"Beginning " + numTrials + " trials");
			for(int i = 0; i < numTrials; i++) {
				runTrial(rng, writer, i);
			}
			
			writer.flush();
			writer.close();
			
			csvToArff(getOutputFilename());
			
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public void train(Random rng) {
		try {

		DataSource source = new DataSource(getOutputFilename() + ".arff");
		Instances data;
		data = source.getDataSet();
		data.setClassIndex(data.numAttributes() - 1);
		
		SpreadSubsample filter = new SpreadSubsample();
		String[] options = new String[2];
		options[0] = "-M";
		options[1] = "1.0";
		filter.setOptions(options);
		filter.setInputFormat(data);
		data = Filter.useFilter(data, filter);

		long seed = rng.nextLong();
		RandomFactory.seedMapped(RANDOM_FACTORY_SEED_TRAIN, seed);
		Random trainRng = RandomFactory.getMapped(RANDOM_FACTORY_SEED_TRAIN);
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(model, data, NUM_FOLDS_CROSSVALIDATION, trainRng);
		DPrint.cl(DEBUG_CODE,eval.toSummaryString());
		DPrint.cl(DEBUG_CODE,eval.toMatrixString());
		model.buildClassifier(data);
		
		SerializationHelper.write(getOutputFilename() + "_" + model.getClass().getSimpleName() + ".model", model);
		
		} catch (Exception e) {
		e.printStackTrace();
		}
	}
	
	public List<State> test(long seed) {
		List<State> optionEndStates = new ArrayList<State>();
		
		try {
		
		ArffLoader.ArffReader headerReader = new ArffLoader.ArffReader(new FileReader(getOutputFilename() + ".arff"));
		Instances data = headerReader.getStructure();
		data.setClassIndex(data.numAttributes() - 1);
		
		// load the model
		model = (Classifier) SerializationHelper.read(getOutputFilename() + "_" + model.getClass().getSimpleName() + ".model");

		// initialize the trainer for a test environment
		boolean testing = true;
		trainer.initialize(seed, testing);
		List<double[]> fvs = trainer.getTestFeatureVectors();
		DPrint.cl(DEBUG_CODE,fvs.size() + " feature vectors!");

		for (int i = 0; i < fvs.size(); i++) {
			double[] fv = fvs.get(i);
			Instance instance = new Instance(1.0, fv);
			instance.setDataset(data);
			data.add(instance);
		}
		
		FileWriter writer = new FileWriter(getOutputFilename() + "_test.csv");
		writer.append(trainer.getFeatureVectorHeader()+"\n");
		for (double[] fv : fvs) {
			StringBuilder sb = new StringBuilder();
			for (double d : fv) {
				sb.append(d);
				sb.append(',');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append('\n');
			writer.append(sb.toString());
		}
		writer.flush();
		writer.close();
		
		csvToArff(getOutputFilename() + "_test");

		
		
		// The following code loads the ARFF file and evaluates the model
		// which is not especially relevant in this instance
		// since we only care about the states detected positive by model
//		ArffLoader loader = new ArffLoader();
//		loader.setFile(new File(getOutputFilename() + ".arff"));
//		loader.setFile(new File("./output/dwdoor.arff"));
//		Instances structure = loader.getDataSet();
//		structure.setClassIndex(structure.numAttributes() - 1);
//		ArffLoader loader2 = new ArffLoader();
//		loader2.setFile(new File(getOutputFilename() + "_test.arff"));
//		loader2.setFile(new File("./output/dwdoor_test.arff"));
//		Instances structure2 = loader2.getDataSet();
//		structure2.setClassIndex(structure2.numAttributes() - 1);
		
		// This is evaluating on the CLASS index
		// not necessarily the desired state property
		// (e.g., for DoorWorld we care about AgentInDoor, not AgentInLocation)
		// show the 'false positives' below are states the model things are terminal
//		Evaluation eval = new Evaluation(structure);
//		eval.evaluateModel(model, structure2);
//		DPrint.cl(DEBUG_CODE,eval.toSummaryString("\nResults\n=======\n", false));
//		DPrint.cl(DEBUG_CODE,eval.toMatrixString());

		final MaskedHashableStateFactory mhsf = trainer.getMaskedHashableStateFactory();
		
		List<HashableState> hashedStates = new ArrayList<HashableState>();
		
		for (int i = 0; i < data.numInstances(); i++) {
			Instance inst = data.instance(i);
			double clsLabel = model.classifyInstance(inst);
			if (clsLabel > 0.0) {
//				DPrint.cl(DEBUG_CODE,trainer.states.get(i));
//				CleanupRandomTrainer cwt = (CleanupRandomTrainer)trainer;
//				System.out.print(cwt.goalDescriptions[0]);
//				if(cwt.goalCondition.satisfies(trainer.states.get(i))) {
//					State is = trainer.states.get(i);
//					System.out.print(optionEndStates.size() + " ");
//					System.out.print(optionEndStates.contains(trainer.states.get(i)));
//					System.out.print(" " + is.getFirstObjectOfClass(CleanupWorld.CLASS_BLOCK).getStringValForAttribute(CleanupWorld.ATT_X));
//					System.out.print(" " + is.getFirstObjectOfClass(CleanupWorld.CLASS_BLOCK).getStringValForAttribute(CleanupWorld.ATT_Y) + " ");
//					System.out.print(new CleanupWorldRandom.CleanupGoal(cwt.goalDescriptions).satisfies(trainer.states.get(i)));
//				}
				HashableState hs = mhsf.hashState(trainer.states.get(i));
				if (!hashedStates.contains(hs)) {
					hashedStates.add(hs);
					optionEndStates.add(trainer.states.get(i));
				}
			}
		}

		} catch (FileNotFoundException e) {
		e.printStackTrace();
		} catch (IOException e) {
		e.printStackTrace();
		} catch (Exception e) {
		e.printStackTrace();
		}
		
		return optionEndStates;
	}


	public Option makeOption(State endState, String name) {

		final State end = endState;
		final MaskedHashableStateFactory mhsf = trainer.getMaskedHashableStateFactory();
		
		DPrint.cl(DEBUG_CODE,"make option");
		
		StateConditionTest initiationConditionTest = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				HashableState hEnd = mhsf.hashState(end);
				HashableState hs = mhsf.hashState(s);
				boolean test = hEnd.equals(hs);
				return !test;
			}
		};
		final StateConditionTest goal = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				HashableState hEnd = mhsf.hashState(end);
				HashableState hs = mhsf.hashState(s);
				boolean test = hEnd.equals(hs);
				return test;
			}
		};
		StateConditionTest terminationConditionTest = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return goal.satisfies(s);
			}
		};

		// create the grounded option
		Policy optionPolicy = trainer.createOptionPolicy(goal, hf);
		SubgoalBoundedOption option = new SubgoalBoundedOption(name, optionPolicy, initiationConditionTest, terminationConditionTest);
//		option.setExpectationHashingFactory(hf);

		return option;
	}
	
	public List<Option> makeOptions(List<State> optionEndStates) {
		DPrint.cl(DEBUG_CODE,optionEndStates.size() + " option end states");
		
		System.err.println("Temp debug: limiting to small number of options");
		Collections.shuffle(optionEndStates);
		optionEndStates = optionEndStates.subList(0, 2);
		
		List<Option> options = new ArrayList<Option>();
		for (int i = 0; i < optionEndStates.size(); i++) {
			System.out.println("making option " + i + " of " + optionEndStates.size());
			State s = optionEndStates.get(i);
			Option option = makeOption(s,optionNamePrefix+i);
			if (option != null) {
				options.add(option);
//				DPrint.cl(DEBUG_CODE,s);
//				System.exit(-1);
			}
		}
		return options;
	}
	
	public void evaluate(List<Option> options) {
		DPrint.cl(DEBUG_CODE,"Begin evaluation...");
		trainer.evaluate(options);
	}
	
	public void run(Random rng, int numTrials) {
		
		// data collection phase
//		collectData(numTrials, rng.nextLong());
		
		// training phase
		train(rng);
		//trainer.visualize(outputPath); // does not show accurate bounds/walls except for last domain
		
		// evaluation phase (ground parameterized options in RL environment)
		long testSeed = rng.nextLong();
		List<State> optionEndStates = test(testSeed);
		
		List<Option> options = makeOptions(optionEndStates);

		evaluate(options);
		trainer.visualize(trainer.getVisualizeFilePath());
	}

	public static void main (String[] args) {

        DPrint.toggleCode(DEBUG_CODE, false);
		
		RandomFactory.seedMapped(0, 3914836); //3914836 tough for cleanup
		Random rng = RandomFactory.getMapped(0);
		
		PoptionsTrainer trainer = new CleanupWorldTrainer(1, 1, rng.nextLong(), CleanupWorld.PF_BLOCK_IN_DOOR);
//		PoptionsTrainer trainer = new DoorWorldTrainer(5, 14, 5, 14, rng.nextLong());
		Classifier model = new J48();
		String outputPath = "output/";
		String outputPrefix = "driver_";
		
		PoptionsDriver driver = new PoptionsDriver(trainer, model, outputPath, outputPrefix);
		int numTrainingTrials = 1000;
		driver.run(rng, numTrainingTrials);
		
	}
	
}
