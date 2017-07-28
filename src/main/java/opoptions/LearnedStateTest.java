package opoptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class LearnedStateTest implements StateConditionTest {

    private Classifier classifier;
    private Instances instancesStructure;

    public LearnedStateTest(Classifier classifier, Instances instancesStructure) {
        this.classifier = classifier;
        this.instancesStructure = instancesStructure;
    }

    @Override
    public boolean satisfies(State s) {
//        StringBuilder sb = new StringBuilder();
//        sb = OPODriver.stateToStringBuilder(sb, (OOState)s);
//
//        Instance instance = new Instance(1.0, sb);

        int numAttributes = instancesStructure.numAttributes();
        Instance instance = new DenseInstance(numAttributes);
        instance.setDataset(instancesStructure);
        OOState state = (OOState)s;
        List<ObjectInstance> objects = state.objects();
        for (ObjectInstance object : objects) {
            for (Object variableKey : object.variableKeys()) {
                String objectKey = variableKey.toString();
                String val = object.get(objectKey).toString();
                String attributeKey = object.className() + ":" + objectKey;
                Attribute attribute = instancesStructure.attribute(attributeKey);
                if (attribute.isNumeric()) {
                    instance.setValue(attribute, Double.parseDouble(val));
                } else {
                    instance.setValue(attribute, val);
                }
            }
        }
        double output = 0.0;
        try {
            output = classifier.classifyInstance(instance);
//            OPODriver.log("classifier output: " + output);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("not implemented");
        }

        return output > 0;

    }

}
