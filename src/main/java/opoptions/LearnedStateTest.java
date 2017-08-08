package opoptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

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

    protected Instance stateToInstance(State s) {
        int numAttributes = instancesStructure.numAttributes();
        Instance instance = new DenseInstance(numAttributes);
        instance.setDataset(instancesStructure);
//        Enumeration<Attribute> as = instancesStructure.enumerateAttributes();
//        while(as.hasMoreElements()) {
//            Attribute a = as.nextElement();
//            OPODriver.log(a + " " + a.index());
//        }
        OOState state = (OOState) s;
        List<ObjectInstance> objects = state.objects();
        for (ObjectInstance object : objects) {
            for (Object variableKey : object.variableKeys()) {
                String objectKey = variableKey.toString();
                String val = object.get(objectKey).toString();
                String attributeKey = object.className() + ":" + objectKey;
                Attribute attribute = instancesStructure.attribute(attributeKey);
                if (attribute == null) {
//                    OPODriver.log("null attribute for key " + attributeKey + ", skipping...");
                    continue;
                }
                if (attribute.isNumeric()) {
                    instance.setValue(attribute, Double.parseDouble(val));
                } else {
                    instance.setValue(attribute, val);
                }
            }
        }
        if (includePFs) {
            List<GroundedProp> gpfs = featurizer.getAllGroundedProps(state);
            for (GroundedProp gpf : gpfs) {
                String attributeKey = gpf.toString().replace(",", ";").replace(" ", "");
                Attribute attribute = instancesStructure.attribute(attributeKey);
                if (attribute == null) {
//                    OPODriver.log("null attribute for key " + attributeKey + ", skipping...");
                    continue;
                }
                String value = gpf.isTrue(state) ? "true" : "false";
                instance.setValue(attribute, value);
            }
        }
//        OPODriver.log(instance.toString());
        return instance;
    }

    @Override
    public boolean satisfies(State s) {
        Instance instance = stateToInstance(s);
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
