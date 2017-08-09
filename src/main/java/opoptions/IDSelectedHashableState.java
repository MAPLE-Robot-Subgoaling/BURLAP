package opoptions;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IDSimpleHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IDSelectedHashableState extends IDSimpleHashableState {

    public SelectedConfig config;

    public IDSelectedHashableState() {
    }

    public IDSelectedHashableState(SelectedConfig config) {
        this.config = config;
    }

    public IDSelectedHashableState(State s, SelectedConfig config) {
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
                int nameHash = o.name().hashCode();
                int totalHash = oHash + 31 * classNameHash + 31 * 31 * nameHash;
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
                ObjectInstance oo = s2.object(o.name());
                if (oo == null || !flatStatesEqual(o, oo)) {
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
