package cat;

import burlap.mdp.core.state.State;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableTree {

    private TreeNode root;
    private String treeStr;

    public VariableTree(String tree) {
        treeStr = tree;
        root = read();
    }

    public VariableTree(File tree) {
        root = read(tree);
    }

    protected TreeNode read(File file) {
        try {
            String tree = new String(Files.readAllBytes(file.toPath()));
            treeStr = tree;
            return read();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected TreeNode read() {

        int depth = firsltAlphaNumChar(treeStr);
        int lineDepth = depth;

        String line = treeStr.substring(0, treeStr.indexOf("\n"));
        if (line.indexOf("=") == -1) {
            String value = line.substring(line.indexOf(": ") + 2);
            return new TreeNode(value);
        }

        String test = line.substring(lineDepth, line.indexOf(" ", lineDepth));
        Map<String, TreeNode> children = new HashMap<String, TreeNode>();
        while (lineDepth == depth) {
            line = treeStr.substring(0, treeStr.indexOf("\n"));
            treeStr = treeStr.substring(treeStr.indexOf("\n") + 1);

            if (line.indexOf(": ") != -1) {
                String value = line.substring(line.indexOf(": ") + 2);
                String testVal = line.substring(line.indexOf("=") + 2, line.indexOf(": "));
                TreeNode leaf = new TreeNode(value);

                children.put(testVal, leaf);
            } else { //test node
                String testVal = line.substring(line.indexOf("=") + 2);
                TreeNode child = read();
                children.put(testVal, child);
            }
            lineDepth = firsltAlphaNumChar(treeStr);
        }

        return new TreeNode(test, children);
    }

    protected int firsltAlphaNumChar(String test) {
        Pattern p = Pattern.compile("[A-Za-z0-9]");
        Matcher m = p.matcher(test);
        if (m.find())
            return m.start();
        else
            return -1;
    }

    public String classify(State s) {
        return root.classify(s);
    }

    public List<String> getCheckedVariables(State s) {
        return root.getCheckedVariables(s);
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
