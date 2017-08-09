package cleanup.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;
import cleanup.Cleanup;
import utils.MutableObject;

import java.util.Arrays;
import java.util.List;

@DeepCopyState
public class CleanupAgent extends MutableObject {

    private String name;

    public CleanupAgent() {

    }

    private final static List<Object> keys = Arrays.<Object>asList(
            Cleanup.ATT_X,
            Cleanup.ATT_Y,
            Cleanup.ATT_LEFT,
            Cleanup.ATT_RIGHT,
            Cleanup.ATT_BOTTOM,
            Cleanup.ATT_TOP,
            Cleanup.ATT_DIR,
            Cleanup.ATT_SHAPE,
            Cleanup.ATT_COLOR
    );

    public static String getDefaultName() {
        return Cleanup.CLASS_AGENT + "0";
    }

    public CleanupAgent(int x, int y, String direction) {
        this(getDefaultName(), x, y, direction, Cleanup.SHAPE_AGENT, Cleanup.COLOR_GRAY);
    }

    public CleanupAgent(String name, int x, int y, String direction, String shape, String color) {
        this(name, (Object) x, (Object) y, (Object) direction, (Object) shape, (Object) color);
    }

    public CleanupAgent(String name, Object x, Object y, Object direction, Object shape, Object color) {
        this.set(Cleanup.ATT_X, x);
        this.set(Cleanup.ATT_Y, y);
        this.set(Cleanup.ATT_LEFT, x);
        this.set(Cleanup.ATT_RIGHT, x);
        this.set(Cleanup.ATT_BOTTOM, y);
        this.set(Cleanup.ATT_TOP, y);
        this.set(Cleanup.ATT_DIR, direction);
        this.set(Cleanup.ATT_SHAPE, shape);
        this.set(Cleanup.ATT_COLOR, color);
        this.name = name;
    }

    @Override
    public String className() {
        return Cleanup.CLASS_AGENT;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public CleanupAgent copy() {
        return new CleanupAgent(name,
                get(Cleanup.ATT_X),
                get(Cleanup.ATT_Y),
                get(Cleanup.ATT_DIR),
                get(Cleanup.ATT_SHAPE),
                get(Cleanup.ATT_COLOR)
        );
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new CleanupAgent(objectName, get(Cleanup.ATT_X), get(Cleanup.ATT_Y), get(Cleanup.ATT_DIR), get(Cleanup.ATT_SHAPE), get(Cleanup.ATT_COLOR));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        CleanupAgent o = (CleanupAgent) other;
        for (Object key : keys) {
            if (!get(key).equals(o.get(key))) {
                return false;
            }
        }
        return name.equals(o.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
