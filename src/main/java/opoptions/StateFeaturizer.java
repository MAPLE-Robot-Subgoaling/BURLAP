package opoptions;

import java.util.ArrayList;
import java.util.List;

import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.singleagent.oo.OOSADomain;

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

    public static List<GroundedProp> getAllGroundedProps(OOState state, OOSADomain domain) {
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

    public List<GroundedProp> getAllGroundedProps(OOState state) {
        return getAllGroundedProps(state, domain);
    }

}
