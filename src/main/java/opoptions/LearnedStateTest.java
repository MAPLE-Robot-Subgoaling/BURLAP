package opoptions;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class LearnedStateTest implements StateConditionTest {

    private boolean includePFs;
    private StateFeaturizer featurizer;
    private Classifier classifier;
    private Instances instancesStructure;
    private String targetLabel;
    private String name;

    public LearnedStateTest(String name, Classifier classifier, Instances instancesStructure, String targetLabel, StateFeaturizer featurizer, boolean includePFs) {
        this.name = name;
        this.classifier = classifier;
        this.instancesStructure = instancesStructure;
        this.targetLabel = targetLabel;
        this.featurizer = featurizer;
        this.includePFs = includePFs;
    }

    public String getName() {
        return name;
    }

    protected Instance stateToInstance(State s, String[] params) {
        int numAttributes = instancesStructure.numAttributes();
        Instance instance = new DenseInstance(numAttributes);
        instance.setDataset(instancesStructure);

        boolean printWarning = false;
        OOState state = (OOState) s;
//        List<ObjectInstance> objects = state.objects();
        List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
        for(String param : params) { objects.add(state.object(param)); }
        Map<String, Integer> classCounter = new HashMap<String, Integer>();
        Map<String, String> objectRenaming = new HashMap<String, String>();
        for (ObjectInstance object : objects) {

            // idea here is to create an instance for all possible permutations of objectclass / name
            // basically to make this identifier-independent, by reordering the objectClasses
            String objectClass = object.className();
            String oldObjectName = object.name();
            if (classCounter.get(objectClass) == null) { classCounter.put(objectClass, 0); }
            int redoneObjectIndex = classCounter.get(objectClass);
            String reorderedObjectName = objectClass + redoneObjectIndex;
            objectRenaming.put(oldObjectName, reorderedObjectName);
            classCounter.put(objectClass, redoneObjectIndex+1); // increment counter

            for (Object variableKey : object.variableKeys()) {
                String objectKey = variableKey.toString();
                String val = object.get(objectKey).toString();

//                String attributeKey = object.name() + ":" + objectKey;
                String attributeKey = reorderedObjectName + ":" + objectKey;
                Attribute attribute = instancesStructure.attribute(attributeKey);
                if (attribute == null) {
//                    OPODriver.log("null attribute for key " + attributeKey + ", skipping...");
//                    printWarning = true;
                    continue;
                }
                if (attribute.isNumeric()) {
                    instance.setValue(attribute, Double.parseDouble(val));
                } else {
                    instance.setValue(attribute, val);
                }
            }
        }

//        if (printWarning) {
//            System.err.println("WARNING: at least one null attribute was found, meaning it was unused in the test");
//            System.err.println("likely due to target state including class/attributes not in the training domain");
//        }

        if (includePFs) {
//            List<GroundedProp> gpfs = featurizer.getAllGroundedProps(state);
            List<GroundedProp> gpfs = featurizer.getSubsetGroundedProps(state, params);
            for (GroundedProp gpf : gpfs) {
                String[] actualParams = gpf.params.clone();
                String[] remappedParams = new String[actualParams.length];
                for (int i = 0; i < remappedParams.length; i++) {
                    remappedParams[i] = objectRenaming.get(actualParams[i]);
                }
                gpf.params = remappedParams;
                String attributeKey = gpf.toString().replace(",", ";").replace(" ", "");

                Attribute attribute = instancesStructure.attribute(attributeKey);
                if (attribute == null) {
//                    OPODriver.log("null attribute for key " + attributeKey + ", skipping...");
                    continue;
                }
                gpf.params = actualParams;
                String value = gpf.isTrue(state) ? "true" : "false";
                instance.setValue(attribute, value);
            }
        }
//        OPODriver.log(instance.toString());
        return instance;
    }

    public boolean satisfies(OOState s, String[] params) {
        Instance instance = stateToInstance(s, params);
        double output = 0.0;
        try {
            output = classifier.classifyInstance(instance);
//            OPODriver.log("classifier output: " + output);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("not implemented");
        }

        String predictedLabel = instance.classAttribute().value(((Double) output).intValue());
//        OPODriver.log(StateFeaturizer.stateToStringBuilder(new StringBuilder(), (OOState)s));
//        OPODriver.log("target: " + targetLabel + ", predicted: " + predictedLabel + " out of " + instance.classAttribute() + ", using output " + output);

        return predictedLabel.equals(targetLabel);
    }


    @Override
    public boolean satisfies(State s) {
        throw new RuntimeException("Temp debug: don't call satisfies(s), use satisfies(s,params) instead");
    }

    public boolean isIncludePFs() {
        return includePFs;
    }

    public void setIncludePFs(boolean includePFs) {
        this.includePFs = includePFs;
    }

    public StateFeaturizer getFeaturizer() {
        return featurizer;
    }

    public void setFeaturizer(StateFeaturizer featurizer) {
        this.featurizer = featurizer;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public Instances getInstancesStructure() {
        return instancesStructure;
    }

    public void setInstancesStructure(Instances instancesStructure) {
        this.instancesStructure = instancesStructure;
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

}
