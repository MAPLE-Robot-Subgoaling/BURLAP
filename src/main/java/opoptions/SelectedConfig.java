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
}
