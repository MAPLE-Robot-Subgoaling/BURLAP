package cat;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import opoptions.OPODriver;
import utils.MurmurHash;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddValues;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateActionModels {

    private static Map<String, Map<String, VariableTree>> trees;

    public static Map<String, Map<String, VariableTree>> createModels(List<Episode> trajectories) {
        trees = new HashMap<String, Map<String, VariableTree>>();

        List<String> actions = new ArrayList<String>();
        for (Episode e : trajectories) {
            for (Action a : e.actionSequence) {
                if (!actions.contains(a.toString()))
                    actions.add(a.toString());
            }
        }

        for (String action : actions) {
            List<State> priorStates = new ArrayList<State>();
            List<State> postStates = new ArrayList<State>();
            List<Double> rewards = new ArrayList<Double>();
            for (Episode trajectory : trajectories) {
                for (int i = 0; i < trajectory.actionSequence.size(); i++) {
                    if (trajectory.actionSequence.get(i).toString().equals(action)) {
                        priorStates.add(trajectory.stateSequence.get(i));
                        postStates.add(trajectory.stateSequence.get(i + 1));
                        rewards.add(trajectory.rewardSequence.get(i));
                    }
                }
            }

            List<Object> variables = trajectories.get(0).stateSequence.get(0).variableKeys();
            for (Object var : variables) {
                //create tree for each state variable with class as the next state's variable
                ArrayList<Attribute> attributes = new ArrayList<Attribute>();
                attributes.add(new Attribute(var.toString() + "_prime"));

                for (Object variable : variables) {

                    attributes.add(new Attribute(variable.toString()));
                }
                Instances dataset = new Instances(action + "_" + var.toString(), attributes, priorStates.size());
                dataset.setClassIndex(0);

                //fill with data
                for (int i = 0; i < priorStates.size(); i++) {
                    Instance dataPoint = new DenseInstance(variables.size() + 1);
                    dataPoint.setDataset(dataset);

                    Object label = postStates.get(i).get(var);
                    if (label instanceof Number) {
                        dataPoint.setValue(0, ((Number) label).doubleValue());
                    } else {
                        dataPoint.setValue(0, MurmurHash.hash32(label.toString()));
                    }

                    State prior = priorStates.get(i);
                    addSStateVars(variables, dataPoint, prior);
                    dataset.add(dataPoint);
                }

                J48 tree = buildTree(dataset);
                writeTreeToFile(action, var.toString(), tree);
                addTree(action, var.toString(), tree);
            }

            //create tree for reward with claas as reward
            ArrayList<Attribute> attributes = new ArrayList<Attribute>();
            attributes.add(new Attribute("reward"));
            for (Object var : variables) {
                attributes.add(new Attribute(var.toString()));
            }
            Instances dataset = new Instances(action + "_Reward", attributes, priorStates.size());
            dataset.setClassIndex(0);

            for (int i = 0; i < priorStates.size(); i++) {
                Instance dataPoint = new DenseInstance(variables.size() + 1);
                dataPoint.setDataset(dataset);

                double label = rewards.get(i);
                dataPoint.setValue(0, label);

                State prior = priorStates.get(i);
                addSStateVars(variables, dataPoint, prior);
                dataset.add(dataPoint);
            }
            J48 tree = buildTree(dataset);
            writeTreeToFile(action, "R", tree);
            addTree(action, "R", tree);
        }
        return trees;
    }

    public static Map<String, Map<String, VariableTree>> readTreeFiles(String folderPath) {
        Map<String, Map<String, VariableTree>> parsedTrees = new HashMap<String, Map<String, VariableTree>>();
        File folder = new File(folderPath);
        File[] treeFiles = folder.listFiles();

        for (File tree : treeFiles) {
            if (tree.isFile()) {
                String name = tree.getName();
                String action = name.substring(0, name.indexOf("_"));
                String variable = name.substring(name.indexOf("_") + 1, name.indexOf("."));
                variable = variable.replace("_", ":");
                VariableTree parsedTree = new VariableTree(tree);

                Map<String, VariableTree> actionTrees = parsedTrees.get(action);
                if (actionTrees == null) {
                    actionTrees = new HashMap<String, VariableTree>();
                    parsedTrees.put(action, actionTrees);
                }

                actionTrees.put(variable, parsedTree);
            }
        }
        return parsedTrees;
    }

    private static void addSStateVars(List<Object> variables, Instance dataPoint, State prior) {
        int counter = 1;
        for (Object varKey : variables) {
            Object value = prior.get(varKey);
            if (value instanceof Number) {
                dataPoint.setValue(counter++, ((Number) value).doubleValue());
            } else {
                dataPoint.setValue(counter++, MurmurHash.hash32(value.toString()));
            }
        }
    }


    private static J48 buildTree(Instances dataset) {
        //apply filters
        try {
            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setInputFormat(dataset);
            dataset = Filter.useFilter(dataset, numericToNominal);

            AddValues addval = new AddValues();
            String[] argVals = {"-C", "first", "-L", "_"};
            addval.setOptions(argVals);
            addval.setInputFormat(dataset);
            dataset = Filter.useFilter(dataset, addval);

            StringToNominal stringToNominal = new StringToNominal();
            stringToNominal.setInputFormat(dataset);
            dataset = Filter.useFilter(dataset, stringToNominal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //train tree
        J48 tree = new J48();
        try {
            ArffSaver save = new ArffSaver();
            save.setFile(new File("data.arff"));
            save.setInstances(dataset);
            save.writeBatch();

            String[] options = {"-M", "1", "-U"};
            tree.setOptions(options);
            tree.buildClassifier(dataset);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tree;
    }

    private static void writeTreeToFile(String action, String variable, J48 tree) {
        String fname = "trees/" + action + "_" + variable.replace(":", "_") + ".txt";
        try {
//            OPODriver.log("Tree created for " + action + " and " + variable);
            String out = getCleanTreeString(tree);

            File file = new File(fname);
            file.getParentFile().mkdirs();
            BufferedWriter write = new BufferedWriter(new FileWriter(file));
            write.write(out);
            write.flush();
            write.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCleanTreeString(J48 tree) {
        String out = tree.toString();
        for (int i = 0; i < 2; i++) {
            out = out.substring(out.indexOf("\n") + 1);
        }
        out = out.trim();
        out = out.replaceAll(" \\(.*\\)", "");

        int end = out.indexOf("Number of Leaves");
        if (end >= 0)
            out = out.substring(0, end);
        return out;
    }

    private static void addTree(String action, String var, J48 tree) {
        String treeStr = getCleanTreeString(tree);
        VariableTree parsedTree = new VariableTree(treeStr);

        Map<String, VariableTree> actionTrees = trees.get(action);
        if (actionTrees == null) {
            actionTrees = new HashMap<String, VariableTree>();
            trees.put(action, actionTrees);
        }

        actionTrees.put(var, parsedTree);
    }
}
