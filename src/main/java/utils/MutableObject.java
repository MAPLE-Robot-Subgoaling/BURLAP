package utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.mdp.core.state.MutableState;


public abstract class MutableObject implements MutableObjectInstance, Serializable {

    public Map<String, Object> values = new HashMap<String, Object>();

    @Override
    public Object get(Object variableKey) {
        return this.values.get(variableKey);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {
        this.values.put(variableKey.toString(), value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(className()).append(":").append(name());
//        buf.append(" {\n");
        buf.append(" {");
        List<Object> keys = this.variableKeys();
        boolean first = true;
        for (Object key : keys) {
            if(!first) {
                buf.append(", ");
            }
            first = false;
            Object value = this.get(key);
//            buf.append("\t").append(key.toString()).append(": {");
            buf.append(key.toString()).append(": {");
            if (value == null) {
                buf.append("unset");
            } else {
                buf.append(value.toString());
            }
//            buf.append("}\n");
            buf.append("}");
        }
        buf.append("}");
        return buf.toString();
    }

}
