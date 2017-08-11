package cleanup.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.annotations.ShallowCopyState;
import cleanup.Cleanup;

import java.util.*;

@ShallowCopyState
public class CleanupState implements MutableOOState {

    private int width;
    private int height;
    private CleanupAgent agent;
    private Map<String, CleanupBlock> blocks = new HashMap<String, CleanupBlock>();
    ;
    private Map<String, CleanupRoom> rooms = new HashMap<String, CleanupRoom>();
    ;
    private Map<String, CleanupDoor> doors = new HashMap<String, CleanupDoor>();
    ;

    public CleanupState() {

    }

    public CleanupState(int w, int h, int ax, int ay, String agentDirection, int nBlocks, int nRooms, int nDoors) {
        this(w, h, new CleanupAgent(ax, ay, agentDirection), new HashMap<String, CleanupBlock>(nBlocks), new HashMap<String, CleanupRoom>(nRooms), new HashMap<String, CleanupDoor>(nDoors));
    }

    public CleanupState(int w, int h, CleanupAgent agent, Map<String, CleanupBlock> blocks, Map<String, CleanupRoom> rooms, Map<String, CleanupDoor> doors) {
        this.width = w;
        this.height = h;
        this.agent = agent;
        this.blocks = blocks;
        this.rooms = rooms;
        this.doors = doors;
    }

    @Override
    public MutableOOState addObject(ObjectInstance o) {
        if (o instanceof CleanupAgent || o.className().equals(Cleanup.CLASS_AGENT)) {
            //copy on write
            touchAgent();
            agent = (CleanupAgent) o;
            return this;
        } else if (o instanceof CleanupBlock || o.className().equals(Cleanup.CLASS_BLOCK)) {
            //copy on write
            touchBlocks().put(o.name(), (CleanupBlock) o);
            return this;
        } else if (o instanceof CleanupRoom || o.className().equals(Cleanup.CLASS_ROOM)) {
            //copy on write
            touchRooms().put(o.name(), (CleanupRoom) o);
            return this;
        } else if (o instanceof CleanupDoor || o.className().equals(Cleanup.CLASS_DOOR)) {
            //copy on write
            touchDoors().put(o.name(), (CleanupDoor) o);
            return this;
        }
        throw new RuntimeException("Can only add certain objects to state.");
    }

    @Override
    public MutableOOState removeObject(String oname) {
        throw new RuntimeException("Remove not implemented");
    }

    @Override
    public MutableOOState renameObject(String objectName, String newName) {
        throw new RuntimeException("Rename not implemented");
    }

    @Override
    public int numObjects() {
        int numObjects = blocks.size() + rooms.size() + doors.size();
        int hasAgent = agent == null ? 0 : 1;
        return numObjects + hasAgent;
    }

    @Override
    public ObjectInstance object(String oname) {
        if (oname.equals(agent.name())) {
            return agent;
        }
        ObjectInstance out = blocks.get(oname);
        if (out != null) {
            return out;
        }
        out = rooms.get(oname);
        if (out != null) {
            return out;
        }
        out = doors.get(oname);
        if (out != null) {
            return out;
        }
//		throw new RuntimeException("ERROR: unable to find object with name " + oname);
        return null;
    }

    @Override
    public List<ObjectInstance> objects() {
        ArrayList<ObjectInstance> obs = new ArrayList<ObjectInstance>();
        if (agent != null) obs.add(agent);
        obs.addAll(blocks.values());
        obs.addAll(rooms.values());
        obs.addAll(doors.values());
        return obs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String oclass) {
        if (oclass.equals(Cleanup.CLASS_AGENT)) {
            return Arrays.<ObjectInstance>asList(agent);
        } else if (oclass.equals(Cleanup.CLASS_BLOCK)) {
            return new ArrayList<ObjectInstance>(blocks.values());
        } else if (oclass.equals(Cleanup.CLASS_ROOM)) {
            return new ArrayList<ObjectInstance>(rooms.values());
        } else if (oclass.equals(Cleanup.CLASS_DOOR)) {
            return new ArrayList<ObjectInstance>(doors.values());
        }
        throw new RuntimeException("No object class " + oclass);
    }

    @Override
    public MutableState set(Object variableKey, Object value) {

        OOVariableKey key = OOStateUtilities.generateKey(variableKey);
        if (key.obName.equals(agent.name())) {
            touchAgent().set(variableKey, value);
        } else if (blocks.get(key.obName) != null) {
            touchBlock(key.obName).set(variableKey, value);
        } else if (rooms.get(key.obName) != null) {
            touchRoom(key.obName).set(variableKey, value);
        } else if (doors.get(key.obName) != null) {
            touchDoor(key.obName).set(variableKey, value);
        } else {
            throw new RuntimeException("ERROR: unable to set value for " + variableKey);
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return OOStateUtilities.flatStateKeys(this);
    }

    @Override
    public Object get(Object variableKey) {
        return OOStateUtilities.get(this, variableKey);
    }

    @Override
    public CleanupState copy() {
        return new CleanupState(width, height, agent, blocks, rooms, doors);
    }

    @Override
    public String toString() {
        return OOStateUtilities.ooStateToString(this);
    }

    public Map<String, CleanupBlock> touchBlocks() {
        this.blocks = new HashMap<String, CleanupBlock>(blocks);
        return blocks;
    }

    public CleanupBlock touchBlock(String name) {
        CleanupBlock n = blocks.get(name).copy();
        touchBlocks().remove(name);
        blocks.put(name, n);
        return n;
    }

    public CleanupAgent touchAgent() {
        if (agent == null) return null;
        this.agent = agent.copy();
        return agent;
    }

    public CleanupDoor touchDoor(String name) {
        CleanupDoor n = doors.get(name).copy();
        touchDoors().remove(name);
        doors.put(name, n);
        return n;
    }

    public Map<String, CleanupDoor> touchDoors() {
        this.doors = new HashMap<String, CleanupDoor>(doors);
        return doors;
    }

    public CleanupRoom touchRoom(String name) {
        CleanupRoom n = rooms.get(name).copy();
        touchRooms().remove(name);
        rooms.put(name, n);
        return n;
    }

    public Map<String, CleanupRoom> touchRooms() {
        this.rooms = new HashMap<String, CleanupRoom>(rooms);
        return rooms;
    }

    public CleanupBlock getBlockAtPoint(int x, int y) {
        List<ObjectInstance> blocks = objectsOfClass(Cleanup.CLASS_BLOCK);
        for (ObjectInstance b : blocks) {
            int bx = (Integer) b.get(Cleanup.ATT_X);
            int by = (Integer) b.get(Cleanup.ATT_Y);
            if (bx == x && by == y) {
                return (CleanupBlock) b;
            }
        }
        return null;
    }

    public boolean blockAt(int x, int y) {
        return getBlockAtPoint(x, y) != null;
    }

    public boolean wallAt(int x, int y) {
        // check if any room has a wall at x,y
        for (CleanupRoom room : rooms.values()) {
            if (wallAt(room, x, y)) {
                return true;
            }
        }
        return false;
    }

    private boolean wallAt(ObjectInstance r, int x, int y) {
        if (r == null) {
//			System.err.println("null room at " + x + ", " + y + ", treating as wall");
            return true;
        }
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        int top = (Integer) r.get(Cleanup.ATT_TOP);
        int left = (Integer) r.get(Cleanup.ATT_LEFT);
        int bottom = (Integer) r.get(Cleanup.ATT_BOTTOM);
        int right = (Integer) r.get(Cleanup.ATT_RIGHT);
        //agent along wall of room check
        if (((x == left || x == right) && y >= bottom && y <= top) || ((y == bottom || y == top) && x >= left && x <= right)) {
            //then only way for this to be a valid pos is if a door contains this point
            ObjectInstance door = doorContainingPoint(x, y);
            if (door == null) {
                return true;
            }
        }
        return false;
    }

    public boolean isOpen(int x, int y) {
        // must be open in all rooms
        for (CleanupRoom room : rooms.values()) {
            if (!isOpen(room, x, y)) {
                return false;
            }
        }
        return true;
    }

    private boolean isOpen(CleanupRoom room, int nx, int ny) {
        return !(isBlocked(room, nx, ny));
    }

    public ObjectInstance roomContainingPoint(int x, int y) {
        List<ObjectInstance> rooms = objectsOfClass(Cleanup.CLASS_ROOM);
        return regionContainingPoint(rooms, x, y, false);
    }

    public CleanupRoom roomContainingPointIncludingBorder(int x, int y) {
        List<ObjectInstance> rooms = objectsOfClass(Cleanup.CLASS_ROOM);
        return (CleanupRoom) regionContainingPoint(rooms, x, y, true);
    }

    public CleanupDoor doorContainingPoint(int x, int y) {
        List<ObjectInstance> doors = objectsOfClass(Cleanup.CLASS_DOOR);
        return (CleanupDoor) regionContainingPoint(doors, x, y, true);
    }

    protected static ObjectInstance regionContainingPoint(List<ObjectInstance> objects, int x, int y, boolean countBoundary) {
        for (ObjectInstance o : objects) {
            if (regionContainsPoint(o, x, y, countBoundary)) {
                return o;
            }
        }
        return null;
    }

    public static boolean regionContainsPoint(ObjectInstance o, int x, int y, boolean countBoundary) {
        int top = (Integer) o.get(Cleanup.ATT_TOP);
        int left = (Integer) o.get(Cleanup.ATT_LEFT);
        int bottom = (Integer) o.get(Cleanup.ATT_BOTTOM);
        int right = (Integer) o.get(Cleanup.ATT_RIGHT);

        if (countBoundary) {
            if (y >= bottom && y <= top && x >= left && x <= right) {
                return true;
            }
        } else {
            if (y > bottom && y < top && x > left && x < right) {
                return true;
            }
        }

        return false;
    }

    public boolean isBlocked(CleanupRoom room, int nx, int ny) {
        return wallAt(room, nx, ny) || blockAt(nx, ny);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (getClass() != other.getClass()) return false;
        CleanupState o = (CleanupState) other;
        if (o.agent != null && this.agent != null && !o.agent.equals(this.agent)) return false;
        if (!o.blocks.equals(this.blocks)) return false;
        if (!o.rooms.equals(this.rooms)) return false;
        if (!o.doors.equals(this.doors)) return false;
        return true;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public CleanupAgent getAgent() {
        return agent;
    }

    public void setAgent(CleanupAgent agent) {
        this.agent = agent;
    }

    public Map<String, CleanupBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(Map<String, CleanupBlock> blocks) {
        this.blocks = blocks;
    }

    public Map<String, CleanupRoom> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, CleanupRoom> rooms) {
        this.rooms = rooms;
    }

    public Map<String, CleanupDoor> getDoors() {
        return doors;
    }

    public void setDoors(Map<String, CleanupDoor> doors) {
        this.doors = doors;
    }

}