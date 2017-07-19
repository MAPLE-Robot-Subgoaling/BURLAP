package cleanup.state;

import java.util.Arrays;
import java.util.List;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;
import cleanup.Cleanup;
import utils.MutableObject;
import utils.MutableObjectInstance;

@DeepCopyState
public class CleanupRoom extends MutableObject {

	private String name;
	
	public CleanupRoom() {
		
	}
	
	private final static List<Object> keys = Arrays.<Object>asList(
			Cleanup.ATT_LEFT,
			Cleanup.ATT_RIGHT,
			Cleanup.ATT_BOTTOM,
			Cleanup.ATT_TOP,
			Cleanup.ATT_COLOR,
			Cleanup.ATT_SHAPE
	);

	public CleanupRoom(String name, int left, int right, int bottom, int top, String color) {
		this(name, (Object)left, (Object)right, (Object)bottom, (Object)top, (Object)color, Cleanup.SHAPE_ROOM);
	}
	
	public CleanupRoom(String name, int left, int right, int bottom, int top, String color, String shape) {
		this(name, (Object)left, (Object)right, (Object)bottom, (Object)top, (Object)color, (Object)shape);
	}
	
	public CleanupRoom(String name, Object left, Object right, Object bottom, Object top, Object color, Object shape) {
		this.set(Cleanup.ATT_LEFT, left);
		this.set(Cleanup.ATT_RIGHT, right);
		this.set(Cleanup.ATT_BOTTOM, bottom);
		this.set(Cleanup.ATT_TOP, top);
		this.set(Cleanup.ATT_COLOR, color);
		this.set(Cleanup.ATT_SHAPE, shape);
		this.name = name;
	}

	@Override
	public String className() {
		return Cleanup.CLASS_ROOM;
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
	public CleanupRoom copy() {
		return (CleanupRoom) copyWithName(name);
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new CleanupRoom(objectName,
				get(Cleanup.ATT_LEFT),
				get(Cleanup.ATT_RIGHT), 
				get(Cleanup.ATT_BOTTOM),
				get(Cleanup.ATT_TOP),
				get(Cleanup.ATT_COLOR),
				get(Cleanup.ATT_SHAPE)
		);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		CleanupRoom o = (CleanupRoom) other;
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
