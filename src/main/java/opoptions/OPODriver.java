package opoptions;

import burlap.behavior.singleagent.Episode;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import cat.CATrajectory;
import cat.CreateActionModels;
import cat.VariableTree;
import opoptions.trainers.CleanupTrainer;
import utils.SimulationConfig;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddValues;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class OPODriver {

    public static final int DEBUG_CODE = 513249;
    private static final int NUM_FOLDS_CROSSVALIDATION = 10;

    public static boolean MOVE_FILE = true;

    private List<OPOTrainer> trainers;
    private List<Long> trainingSeeds;
    private List<Long> evaluationSeeds;

    public OPODriver() {
        trainers = new ArrayList<OPOTrainer>();
        trainingSeeds = new ArrayList<Long>();
        evaluationSeeds = new ArrayList<Long>();
    }

    public static class Transition {

        public OOState state;
        public Action action;
        public double reward;
//        public OOState statePrime;

        public Transition(OOState state, Action action, double reward) { //}, OOState statePrime) {
            this.state = state;
            this.action = action;
            this.reward = reward;
//            this.statePrime = statePrime;
        }

    }

    public String getOutputFilename(OPOTrainer trainer) {
        return trainer.getOutputPath() + trainer.getTrainerName();
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
            log("Seed " + (i + 1) + " / " + numTrajectories + ": " + seed);
            trainer.setSeed(seed);
            trainer.runTraining(null);
        }

    }

    public static void csvToArff(String filenamePrefix) throws Exception {
        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filenamePrefix + ".csv"));
        Instances data = loader.getDataSet();
        // post process to make sure all boolean attributes have a true and a false
        List<Integer> booleanAttributeIndexes = new ArrayList<Integer>();
        for (int i = 0; i < data.numAttributes(); i++) {
            // Weka is terrible and uses column indexes that start at 1 instead of 0
            int actualWekaIndex = i + 1;
            Attribute attribute = data.attribute(i);
            String value = attribute.value(0);
            if (attribute.numValues() < 2) {
                if (value.equals("true")) {
                    booleanAttributeIndexes.add(actualWekaIndex);
//                    attribute.addStringValue("false") ;
                } else if (value.equals("false")) {
                    booleanAttributeIndexes.add(actualWekaIndex);
//                    attribute.addStringValue("true");
                } else {
                    // do nothing
                }
            }
        }
        for (Integer index : booleanAttributeIndexes) {
            AddValues filter = new AddValues();
            String[] args = new String[5];
            args[0] = "-S";
            args[1] = "-C";
            args[2] = index.toString();
            args[3] = "-L";
            args[4] = "true,false";
            filter.setOptions(args);
            filter.setInputFormat(data);
            Instances newData = Filter.useFilter(data, filter);
            data = newData;
        }

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(filenamePrefix + ".arff"));
        saver.writeBatch();
    }

    public void runEvaluation(OPOTrainer trainer) {
        log("Beginning " + evaluationSeeds.size() + " evaluations");
        for (int i = 0; i < evaluationSeeds.size(); i++) {
            Long seed = evaluationSeeds.get(i);
            log("\nTrainer: " + trainer.getTrainerName() + " (" + trainer.getDomainName() + " domain)");
            log("Eval " + (i + 1) + " / " + evaluationSeeds.size() + ": " + seed);
            trainer.setSeed(seed);
            trainer.runEvaluation(null);
        }
    }

    public void runEvaluation() {
        for (OPOTrainer trainer : trainers) {
            runEvaluation(trainer);
        }
    }

    public void addTrainers() {
//        CleanupTrainer moveToDoor = (CleanupTrainer) SimulationConfig.load("./config/moveToDoor.yaml", CleanupTrainer.class);
//        addTrainer(moveToDoor);
        CleanupTrainer blockToDoor = (CleanupTrainer) SimulationConfig.load("./config/blockToDoor.yaml", CleanupTrainer.class);
        addTrainer(blockToDoor);
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

    private void buildClassifiers() {
        for (OPOTrainer trainer : trainers) {
            buildClassifier(trainer, OPOption.NAME_STATE_TEST_GOAL);
            buildClassifier(trainer, OPOption.NAME_STATE_TEST_INTERNAL);
        }
    }


    private List<Transition> getTransitions(OPOTrainer trainer) {
        List<Transition> transitions = new ArrayList<Transition>();
        List<Episode> episodes = Episode.readEpisodes(trainer.getEpisodeOutputPath());
        log(episodes.size());
        for (Episode episode : episodes) {
            List<State> states = episode.stateSequence;
            List<Action> actions = episode.actionSequence;
            List<Double> rewards = episode.rewardSequence;
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                double reward = rewards.get(i);
                OOState state = (OOState) states.get(i);
                Transition transition = new Transition(state, action, reward);
                transitions.add(transition);
            }
            // add the final state with no action and dummy reward
            transitions.add(new Transition((OOState) states.get(states.size() - 1), null, 0.0));

        }
        return transitions;
    }

    private static void writeFeatureVectorHeader(FileWriter writer, Transition transition, OPOTrainer trainer, List<GroundedProp> gpfs) throws IOException {
        StringBuilder sb = new StringBuilder();
        String label = "label";
        String actionName = "actionName";
        String reward = "reward";
        OOState state = transition.state;
        List<ObjectInstance> objects = state.objects();
        for (ObjectInstance object : objects) {
            for (Object variableKey : object.variableKeys()) {
                String key = variableKey.toString();
                sb.append(object.name());
                sb.append(":");
                sb.append(key);
                sb.append(",");
            }
        }
        if (trainer.getIncludePFs()) {
            for (GroundedProp gpf : gpfs) {
                sb.append(gpf.toString().replace(",", ";").replace(" ", ""));
                sb.append(",");
            }
        }
//        sb.append(actionName);
//        sb.append(",");
//        sb.append(reward);
//        sb.append(",");
        sb.append(label);
        sb.append('\n');
        writer.append(sb.toString());
    }

    private static void writeFeatureVector(FileWriter writer, Transition transition, OPOTrainer trainer, List<GroundedProp> gpfs) throws IOException {
        StringBuilder sb = new StringBuilder();
        OOState state = transition.state;
        boolean isGoal = trainer.satisfiesTrainingGoal(state);
        String label = isGoal ? OPOption.NAME_STATE_TEST_GOAL : OPOption.NAME_STATE_TEST_INTERNAL;
        sb = StateFeaturizer.stateToStringBuilder(sb, state);
        if (trainer.getIncludePFs()) {
            for (GroundedProp gpf : gpfs) {
                sb.append(gpf.isTrue(state));
                sb.append(",");
            }
        }
        Action action = transition.action;
        String actionName = action == null ? "null" : action.actionName();
        double reward = transition.reward;
//        sb.append(actionName);
//        sb.append(",");
//        sb.append(reward);
//        sb.append(",");
        sb.append(label);
        sb.append('\n');
        writer.append(sb.toString());
    }

    private void collectDataset(OPOTrainer trainer) {
        try {
            String path = getOutputFilename(trainer);
            String csvPath = path + ".csv";
            log("saving to " + csvPath);
            List<Transition> transitions = getTransitions(trainer);
            File file = new File(csvPath);
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            Transition exampleTransition = transitions.get(0);
            OOState exampleState = (OOState) exampleTransition.state;
            List<GroundedProp> gpfs = StateFeaturizer.getAllGroundedProps(exampleState, (OOSADomain) trainer.getDomain());
            writeFeatureVectorHeader(writer, exampleTransition, trainer, gpfs);
            for (Transition transition : transitions) {
                writeFeatureVector(writer, transition, trainer, gpfs);
            }
            writer.flush();
            writer.close();
            csvToArff(path);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void buildClassifier(OPOTrainer trainer, String targetLabel) {
        String path = getOutputFilename(trainer);
        String arffPath = path + ".arff";
        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(arffPath);
            Instances data;
            data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
//            OPODriver.log(data.classAttribute());

//			SpreadSubsample filter = new SpreadSubsample();
//			String[] options = new String[2];
//			options[0] = "-M";
//			options[1] = "1.0";
//			filter.setOptions(options);
//			filter.setInputFormat(data);
//			data = Filter.useFilter(data, filter);

            Classifier classifier = trainer.getClassifier();

            Evaluation evaluation = new Evaluation(data);
            Random classifierRng = RandomFactory.getMapped(OPOTrainer.DEFAULT_RNG_INDEX);
            evaluation.crossValidateModel(classifier, data, NUM_FOLDS_CROSSVALIDATION, classifierRng);
            classifier.buildClassifier(data);
            log(evaluation.toSummaryString());
            log(evaluation.toMatrixString());
            SerializationHelper.write(path + "_" + targetLabel + "_" + classifier.getClass().getSimpleName() + ".model", classifier);

            StateFeaturizer featurizer = new StateFeaturizer((OOSADomain) trainer.getDomain());
            String name = targetLabel.equals(OPOption.NAME_STATE_TEST_GOAL) ? targetLabel : OPOption.NAME_STATE_TEST_INTERNAL;
            LearnedStateTest test = new LearnedStateTest(name, classifier, data, targetLabel, featurizer, trainer.getIncludePFs());
            trainer.addLearnedStateTest(test);
//            List<Transition> transitions = getTransitions(trainer);
//            for (Transition t : transitions) {
//                OOState s = t.state;
//                boolean satisfied = test.satisfies(s);
//                log("classifier satisfied? " + satisfied + ", actual goal? " + trainer.satisfiesTrainingGoal(s) + " for state " + StateFeaturizer.stateToStringBuilder(new StringBuilder(), s));
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createCATs() {
        for (OPOTrainer trainer : trainers) {
            createCATs(trainer);
        }
    }

    // returns the object classes that have any attribute/variable/factor that is checked or changed
    // e.g., "agent" and "room"
    public void createCATs(OPOTrainer trainer) {

        Set<String> allCheckedChanged = new HashSet<String>();
        List<Episode> trajectories = Episode.readEpisodes(trainer.getEpisodeOutputPath());
        Map<String, Map<String, VariableTree>> models = CreateActionModels.createModels(trajectories);
        List<CATrajectory> caTrajectories = new ArrayList<CATrajectory>();
        for (Episode trajectory : trajectories) {
            CATrajectory cat = new CATrajectory();
            cat.annotateTrajectory(trajectory, models, (FullModel) trainer.getDomain().getModel());
            caTrajectories.add(cat);
//            OPODriver.log(cat);
            Set<String>[] checkedVariables = cat.getCheckedVariables();
            Set<String>[] changedVariables = cat.getChangedVariables();
            List<String> actions = cat.getActions();
            for (int i = 0; i < actions.size(); i++) {
                String action = actions.get(i);
                Set<String> checked = checkedVariables[i];
                Set<String> changed = changedVariables[i];
                if (action.equals("START") || action.equals("END")) {
                    continue;
                }
//                OPODriver.log("checked " + checked);
//                OPODriver.log("changed " + changed);
                allCheckedChanged.addAll(checked);
                allCheckedChanged.addAll(changed);
            }
        }
        List<String> attributeWhitelist = new ArrayList<String>(allCheckedChanged);
        trainer.initializeTypeSignature(attributeWhitelist);
    }

    public static void main(String[] args) {

//    	Long globalSeed = null;
//    	if (globalSeed != null) { log("using a global seed of " + globalSeed); }
        Random rng = new Random();

        boolean debugMode = true;
        DPrint.toggleCode(DEBUG_CODE, debugMode);

        OPODriver driver = new OPODriver();
        long initSeedTraining = rng.nextLong();
        int numSeedsTraining = 1;
        driver.addSeedsTo(driver.getTrainingSeeds(), initSeedTraining, numSeedsTraining);
        log(initSeedTraining + ": " + driver.getTrainingSeeds());
        long initSeedEvaluation = rng.nextLong();
        int numSeedsEvaluation = 1;
        driver.addSeedsTo(driver.getEvaluationSeeds(), initSeedEvaluation, numSeedsEvaluation);
        log(initSeedEvaluation + ": " + driver.getEvaluationSeeds());

        driver.addTrainers();

        driver.runTraining();

        driver.collectDataset();

        driver.createCATs();

        driver.buildClassifiers();

        driver.runEvaluation();

//        driver.runVisualizer();

    }

}
