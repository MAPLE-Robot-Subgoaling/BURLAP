package burlap.statehashing.selected;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IISimpleHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class IISelectedHashableState extends IISimpleHashableState {

    public SelectedConfig config;

    public IISelectedHashableState() {
    }

    public IISelectedHashableState(SelectedConfig config) {
        this.config = config;
    }

    public IISelectedHashableState(State s, SelectedConfig config) {
        super(s);
        this.config = config;
    }


    @Override
    protected int computeOOHashCode(OOState s) {
        List<Integer> hashCodes = new ArrayList<Integer>(s.numObjects());
        List<ObjectInstance> objects = s.objects();
        for (int i = 0; i < s.numObjects(); i++) {
            ObjectInstance o = objects.get(i);
            // if the objectInstance belongs to a selected objectClass, include it
            if (config.getSelectedObjectClasses().contains(o.className())) {
                int oHash = this.computeFlatHashCode(o);
                int classNameHash = o.className().hashCode();
                int totalHash = oHash + 31 * classNameHash;
                hashCodes.add(totalHash);
            }
        }

        //sort for invariance to order
        Collections.sort(hashCodes);

        //combine
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
        for (int hashCode : hashCodes) {
            hashCodeBuilder.append(hashCode);
        }

        return hashCodeBuilder.toHashCode();
    }

    @Override
    protected int computeFlatHashCode(State s) {

        // s should be an object in the OOState
        ObjectInstance objectInstance = (ObjectInstance) s;
        String objectClass = objectInstance.className();

        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);

        List<Object> keys = s.variableKeys();
        for (Object key : keys) {
            Object value = s.get(key);
            //only consider variables that have been selected
            if (config.getSelectedVariables(objectClass).contains(key)) {
                this.appendHashCodeForValue(hashCodeBuilder, key, value);
            }
        }

        return hashCodeBuilder.toHashCode();
    }

    @Override
    protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value) {
        // this should only be called when "key" is a variable in a selected object class
        super.appendHashCodeForValue(hashCodeBuilder, key, value);
    }

    @Override
    protected boolean ooStatesEqual(OOState s1, OOState s2) {
        if (s1 == s2) {
            return true;
        }

        Set<String> matchedObjects = new HashSet<String>();
        for (Map.Entry<String, List<ObjectInstance>> e1 : OOStateUtilities.objectsByClass(s1).entrySet()) {

            String oclass = e1.getKey();

            // skip it if it is not a selected object class
            if (!config.getSelectedObjectClasses().contains(oclass)) {
                continue;
            }

            List<ObjectInstance> objects = e1.getValue();

            List<ObjectInstance> oobjects = s2.objectsOfClass(oclass);
            if (objects.size() != oobjects.size()) {
                return false;
            }

            for (ObjectInstance o : objects) {
                boolean foundMatch = false;
                for (ObjectInstance oo : oobjects) {
                    String ooname = oo.name();
                    if (matchedObjects.contains(ooname)) {
                        continue;
                    }
                    if (flatStatesEqual(o, oo)) {
                        foundMatch = true;
                        matchedObjects.add(ooname);
                        break;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }

        }

        return true;
    }


    @Override
    protected boolean flatStatesEqual(State s1, State s2) {

        if (s1 == s2) {
            return true;
        }

        // should be an object in the OOState
        ObjectInstance objectInstance1 = (ObjectInstance) s1;
        String objectClass1 = objectInstance1.className();
        ObjectInstance objectInstance2 = (ObjectInstance) s2;
        String objectClass2 = objectInstance2.className();

        if (!objectClass1.equals(objectClass2)) {
            return false;
        }

        List<Object> keys1 = s1.variableKeys();
        List<Object> keys2 = s2.variableKeys();

        if (keys1.size() != keys2.size()) {
            return false;
        }

        for (Object key : keys1) {
            Object v1 = s1.get(key);
            Object v2 = s2.get(key);
            // if it is a selected variable, then test equality (otherwise, trivially true)
            if (config.getSelectedVariables(objectClass1).contains(key)) {
                if (!this.valuesEqual(key, v1, v2)) {
                    return false;
                }
            }
        }
        return true;

    }


    @Override
    protected boolean valuesEqual(Object key, Object v1, Object v2) {
        return super.valuesEqual(key, v1, v2);
    }

}
