package burlap.domain.singleagent.bees.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.bees.Bees.CLASS_MAP;
import static burlap.domain.singleagent.bees.Bees.VAR_MAP;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class BeesMap implements ObjectInstance {

	public int [][] map;

	private final List<Object> keys = Arrays.<Object>asList(VAR_MAP);

	public BeesMap() {
	}

	public BeesMap(int w, int h) {
		this.map = new int[w][h];
	}

	public BeesMap(int[][] map) {
		this.map = map;
	}

	@Override
	public String className() {
		return CLASS_MAP;
	}

	@Override
	public String name() {
		return CLASS_MAP;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		throw new RuntimeException("The map must always be named map");
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		return map;
	}

	@Override
	public BeesMap copy() {
		return new BeesMap(map);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}

