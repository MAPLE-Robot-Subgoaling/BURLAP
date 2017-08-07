package cleanup.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;
import cleanup.Cleanup;
import utils.MutableObject;
import utils.MutableObjectInstance;

@DeepCopyState
public class CleanupDoor extends MutableObject {

	private String name;
	
	public CleanupDoor() {
		
	}
	
	private final static List<Object> keys = Arrays.<Object>asList(
			Cleanup.ATT_X,
			Cleanup.ATT_Y,
			Cleanup.ATT_LEFT,
			Cleanup.ATT_RIGHT,
			Cleanup.ATT_BOTTOM,
			Cleanup.ATT_TOP,
			Cleanup.ATT_LOCKED,
			Cleanup.ATT_SHAPE,
			Cleanup.ATT_COLOR
	);
	
	public static String getDefaultName() {
		return Cleanup.CLASS_DOOR + "0";
	}

	public CleanupDoor(int x, int y) {
		this(getDefaultName(), x, x, y, y, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
	}
	
	public CleanupDoor(String name, int left, int right, int bottom, int top, String locked) {
		this(name, (Object)left, (Object)right, (Object)bottom, (Object)top, (Object)locked, Cleanup.SHAPE_DOOR, Cleanup.COLOR_GRAY);
	}

	public CleanupDoor(String name, int left, int right, int bottom, int top, String locked, String color) {
		this(name, (Object)left, (Object)right, (Object)bottom, (Object)top, (Object)locked, Cleanup.SHAPE_DOOR, color);
	}
	
	public CleanupDoor(String name, int left, int right, int bottom, int top, String locked, String shape, String color) {
		this(name, (Object)left, (Object)right, (Object)bottom, (Object)top, (Object)locked, (Object) shape, (Object) color);
	}
	
	public CleanupDoor(String name, Object left, Object right, Object bottom, Object top, Object locked, Object shape, Object color) {
		this.set(Cleanup.ATT_LEFT, left);
		this.set(Cleanup.ATT_X, left);
		this.set(Cleanup.ATT_RIGHT, right);
		this.set(Cleanup.ATT_BOTTOM, bottom);
		this.set(Cleanup.ATT_Y, bottom);
		this.set(Cleanup.ATT_TOP, top);
		this.set(Cleanup.ATT_LOCKED, locked);
		this.set(Cleanup.ATT_SHAPE, shape);
		this.set(Cleanup.ATT_COLOR, color);
		this.name = name;
	}

	@Override
	public String className() {
		return Cleanup.CLASS_DOOR;
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
	public CleanupDoor copy() {
		return (CleanupDoor) copyWithName(name);
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new CleanupDoor(objectName,
				get(Cleanup.ATT_LEFT),
				get(Cleanup.ATT_RIGHT),
				get(Cleanup.ATT_BOTTOM),
				get(Cleanup.ATT_TOP),
				get(Cleanup.ATT_LOCKED),
				get(Cleanup.ATT_SHAPE),
				get(Cleanup.ATT_COLOR)
		);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		CleanupDoor o = (CleanupDoor) other;
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
