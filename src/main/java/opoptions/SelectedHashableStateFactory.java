package opoptions;


import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.simple.IDSimpleHashableState;
import burlap.statehashing.simple.IISimpleHashableState;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedHashableStateFactory extends SimpleHashableStateFactory {

    protected SelectedConfig config;

    public SelectedHashableStateFactory() {
        this(true);
    }

    public SelectedHashableStateFactory(boolean identifierIndependent) {
        super(identifierIndependent);
        config = new SelectedConfig();
    }

    // assumes all objectAttributes given in form "objectClass#:attributeName", e.g. "door1:y"
    public void setSelection(List<String> objectAttributes) {
        config.setSelection(objectAttributes);
    }

    public SelectedConfig getConfig() {
        return config;
    }

    public void setConfig(SelectedConfig config) {
        this.config = config;
    }

    @Override
    public HashableState hashState(State s) {
        if (s instanceof IISimpleHashableState || s instanceof IDSimpleHashableState) {
            return (HashableState) s;
        }
        if (identifierIndependent) {
            return new IISelectedHashableState(s, config);
        }
        return new IDSelectedHashableState(s, config);
    }

}