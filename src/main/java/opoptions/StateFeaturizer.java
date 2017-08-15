package opoptions;

import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class StateFeaturizer {

    private OOSADomain domain;

    public StateFeaturizer(OOSADomain domain) {
        this.domain = domain;
    }

    public static StringBuilder stateToStringBuilder(StringBuilder sb, OOState state) {
        List<ObjectInstance> objects = state.objects();
        for (ObjectInstance object : objects) {
            for (Object variableKey : object.variableKeys()) {
                String key = variableKey.toString();
                String val = object.get(key).toString();
                sb.append(val);
                sb.append(",");
            }
        }
        return sb;
    }

    public static List<GroundedProp> getAllGroundedProps(OOSADomain domain, OOState state) {
        List<GroundedProp> all = new ArrayList<GroundedProp>();
        List<PropositionalFunction> pfs = domain.propFunctions();
        for (PropositionalFunction pf : pfs) {
            List<GroundedProp> gpfs = pf.allGroundings(state);
            for (GroundedProp gpf : gpfs) {
                all.add(gpf);
            }
        }
        return all;
    }

    public static List<GroundedProp> getSubsetGroundedProps(OOSADomain domain, OOState state, String[] objectNames) {
        HashSet<String> objectNamesSet = new HashSet<String>(Arrays.asList(objectNames));
        List<GroundedProp> all = new ArrayList<GroundedProp>();
        List<PropositionalFunction> pfs = domain.propFunctions();
        for (PropositionalFunction pf : pfs) {
            List<GroundedProp> gpfs = pf.allGroundings(state);
            for (GroundedProp gpf : gpfs) {
                HashSet<String> paramsSet = new HashSet<String>(Arrays.asList(gpf.params));
                paramsSet.removeAll(objectNamesSet);
                if (!paramsSet.isEmpty()) {
                    // skip this grounded pf if it contains params not in the objectNamesSet
                    continue;
                }
                all.add(gpf);
            }
        }
        return all;
    }


    public List<GroundedProp> getAllGroundedProps(OOState state) {
        return getAllGroundedProps(domain, state);
    }

    // get a subset of all groundings only involving the objects with the specified names
    public List<GroundedProp> getSubsetGroundedProps(OOState state, String[] objectNames) {
        return getSubsetGroundedProps(domain, state, objectNames);
    }

}
