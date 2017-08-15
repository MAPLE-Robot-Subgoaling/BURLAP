package opoptions;

import java.util.*;

public class SelectedConfig {

    private Map<String, Set<String>> objectClassToSelectedVariables;

    public SelectedConfig() {
        this(new HashMap<String, Set<String>>());
    }

    public SelectedConfig(Map<String, Set<String>> objectClassToSelectedVariables) {
        this.objectClassToSelectedVariables = objectClassToSelectedVariables;
    }

    public static Map<String,Integer> getObjectCounts(List<String> attributeWhitelist) {

        Map<String, Integer> counts = new HashMap<String,Integer>();
        Set<String> knownNames = new HashSet<String>();
        for (String variable : attributeWhitelist) {
            String[] split = variable.split(":");
            String objectName = split[0];
            //String attributeName = split[1];
            if (!knownNames.contains(objectName)) {
                knownNames.add(objectName);
                // split on a digit character, assumes objectName in form "objectClass#" like "agent0"
                String objectClass = objectName.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0];
                if (!counts.containsKey(objectClass)) {
                    counts.put(objectClass, 0);
                }
                counts.put(objectClass, counts.get(objectClass) + 1);
            }
        }

        return counts;
    }

    // assumes all objectAttributes given in form "objectClass#:attributeName", e.g. "door1:y"
    public void setSelection(List<String> objectAttributes) {
        for (String variable : objectAttributes) {
            OPODriver.log(variable);

            String[] split = variable.split(":");
            String objectName = split[0];
            String attributeName = split[1];

            // split on a digit character, assumes objectName in form "objectClass#" like "agent0"
            String objectClass = objectName.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0];

            if (!objectClassToSelectedVariables.containsKey(objectClass)) {
                objectClassToSelectedVariables.put(objectClass, new HashSet<String>());
            }
            objectClassToSelectedVariables.get(objectClass).add(attributeName);
        }

        for (String objectClass : objectClassToSelectedVariables.keySet()) {
            OPODriver.log(objectClass);
            OPODriver.log(objectClassToSelectedVariables.get(objectClass));
        }
    }

    public SelectedConfig copy() {
        return new SelectedConfig(new HashMap<String, Set<String>>(objectClassToSelectedVariables));
    }

    public Set<String> getSelectedObjectClasses() {
        return objectClassToSelectedVariables.keySet();
    }

    public Set<String> getSelectedVariables(String objectClass) {
        return objectClassToSelectedVariables.get(objectClass);
    }

    public static String[] getParameterClasses(Map<String, Integer> objectClassCounts) {
        List<String> parameterClasses = new ArrayList<String>();
        for (String objectClass : objectClassCounts.keySet()) {
            int objectClassCount = objectClassCounts.get(objectClass);
            for (int i = 0; i < objectClassCount; i++) {
                parameterClasses.add(objectClass);
            }
        }
        return parameterClasses.toArray(new String[0]);
    }

    public boolean hasSelected(String objectAttribute) {

        String[] split = objectAttribute.split(":");
        String objectName = split[0];
        String attributeName = split[1];
        // split on a digit character, assumes objectName in form "objectClass#" like "agent0"
        String objectClass = objectName.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0];

        Set<String> selected = objectClassToSelectedVariables.get(objectClass);
        if (selected == null) {
            return false;
        }
        return selected.contains(attributeName);

    }

    public boolean isValidObjectAttribute(String objectAttribute) {
        return objectAttribute.matches("\\w+\\d+:.+");
    }
}
