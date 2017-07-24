package cleanup.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import cleanup.CleanupGoalDescription;
import cleanup.Cleanup;

public class CleanupRandomStateGenerator implements StateGenerator {

	private static final int DEBUG_CODE = 932891293;
	public static int DEFAULT_RNG_INDEX = 0;
	private int numBlocks = 2;
	private int width = 13;
	private  int height = 13;
	
	public static void setDebugMode(boolean mode) {
		DPrint.toggleCode(DEBUG_CODE, mode);
	}
	
	public int getNumBlocks() {
		return numBlocks;
	}

	public void setNumBlocks(int numBlocks) {
		this.numBlocks = numBlocks;
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


	public State generateTaxiInCleanup(int numBlocks) {

		Random rng = RandomFactory.getMapped(DEFAULT_RNG_INDEX);
		
		int numRooms = 1;
		int numDoors = 4;
		
		int mx = width/2;
		int my = height/2;
		
		int ax = mx;
		int ay = my;
		String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

		CleanupState s = new CleanupState(width, height, ax, ay, agentDirection, numBlocks, numRooms, numDoors);
		
		List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS);
		colors.add("green");
		colors.add("blue");
		colors.add("yellow");
		colors.add("red");
		
		int mainW = 3;
		int mainH = 3;
		
		int index = 0;
		do {
			int bx = ax + (rng.nextBoolean() ? -1 : 1);
			int by = ay + (rng.nextBoolean() ? -1 : 1);
			if (!s.blockAt(bx, by)) {
				s.addObject(new CleanupBlock("block"+index, bx, by, "backpack", colors.get(rng.nextInt(numDoors - 1))));
				numBlocks -= 1;
				index += 1;
			}
		} while (numBlocks > 0);
		
		
		s.addObject(new CleanupRoom("room0", mx-mainW, mx+mainW, my-mainH, my+mainH, "cyan", Cleanup.SHAPE_ROOM));
		s.addObject(new CleanupDoor("door0", mx-mainW+1, mx-mainW+1, my, my, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(0)));
		s.addObject(new CleanupDoor("door1", mx+mainW-1, mx+mainW-1, my, my, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(1)));
		s.addObject(new CleanupDoor("door2", mx, mx, my-mainH+1, my-mainH+1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(2)));
		s.addObject(new CleanupDoor("door3", mx, mx, my+mainH-1, my+mainH-1, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, colors.get(3)));
		mx -= mainW+1;
//		s.addObject(new CleanupRoom("room1", mx-1, mx+1, my-1, my+1, colors.get(0), Cleanup.SHAPE_ROOM));
		mx += (mainW+1)*2;
//		s.addObject(new CleanupRoom("room2", mx-1, mx+1, my-1, my+1, colors.get(1), Cleanup.SHAPE_ROOM));
		mx -= mainW+1;
		my -= mainH+1;
//		s.addObject(new CleanupRoom("room3", mx-1, mx+1, my-1, my+1, colors.get(2), Cleanup.SHAPE_ROOM));
		my += (mainH+1)*2;
//		s.addObject(new CleanupRoom("room4", mx-1, mx+1, my-1, my+1, colors.get(3), Cleanup.SHAPE_ROOM));
		
//		int i = 0;
//		while (i < numBlocks) {
//			String id = "block"+i;
//			CleanupRoom room = (CleanupRoom) s.objectsOfClass(Cleanup.CLASS_ROOM).get(rng.nextInt(numRooms));
//			int rLeft = ((Integer) room.get(Cleanup.ATT_LEFT))+1;
//			int rRight = ((Integer) room.get(Cleanup.ATT_RIGHT))-1;
//			int rTop = ((Integer) room.get(Cleanup.ATT_TOP))-1;
//			int rBottom = ((Integer) room.get(Cleanup.ATT_BOTTOM))+1;
//			int bX = rng.nextInt(rRight - rLeft) + rLeft;
//			int bY = rng.nextInt(rTop - rBottom) + rBottom;
//			if (s.isOpen(room, bX, bY)) {
//				String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
//				String color = Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)];
//				DPrint.cl(DEBUG_CODE,"block"+i+": "+ shape + " " + color + " (" + bX + ", " + bY + ") in the " + room.get(Cleanup.ATT_COLOR) + " " + ((CleanupRoom)room).name);
//				s.addObject(new CleanupBlock(id, bX, bY, shape, color));
//				i = i + 1;
//			}
//		}
		
		return s;
	}
	
	public State generateCentralRoomWithClosetsAndBeacon(int numBlocks) {

		Random rng = RandomFactory.getMapped(DEFAULT_RNG_INDEX);

		int numRooms = 5;
		int numDoors = 5;
		
		int mx = width/2;
		int my = height/2;
		
		int ax = mx;
		int ay = my;
		String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

		CleanupState s = new CleanupState(width, height, ax, ay, agentDirection, numBlocks, numRooms, numDoors);
		
		List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS);
		colors.add("green");
		colors.add("blue");
		colors.add("yellow");
		colors.add("red");
		
		int mainW = 2;
		int mainH = 2;
		
		int index = 0;
		String lastBlockColor = null;
		do {
			int bx = ax + (rng.nextBoolean() ? -1 : 1);
			int by = ay + (rng.nextBoolean() ? -1 : 1);
			if (!s.blockAt(bx, by)) {
				lastBlockColor = colors.get(rng.nextInt(numRooms - 1));
				s.addObject(new CleanupBlock("block"+index, bx, by, "backpack", lastBlockColor));
				numBlocks -= 1;
				index += 1;
			}
		} while (numBlocks > 0);
		
		s.addObject(new CleanupRoom("room0", mx-mainW, mx+mainW, my-mainH, my+mainH, "cyan", Cleanup.SHAPE_ROOM));
		s.addObject(new CleanupDoor("door0", mx-mainW, mx-mainW, my, my, Cleanup.LOCKABLE_STATES[0], colors.get(0)));
		s.addObject(new CleanupDoor("door1", mx+mainW, mx+mainW, my, my, Cleanup.LOCKABLE_STATES[0], colors.get(1)));
		s.addObject(new CleanupDoor("door2", mx, mx, my-mainH, my-mainH, Cleanup.LOCKABLE_STATES[0], colors.get(2)));
		s.addObject(new CleanupDoor("door3", mx, mx, my+mainH, my+mainH, Cleanup.LOCKABLE_STATES[0], colors.get(3)));
		s.addObject(new CleanupDoor("beacon", mx, mx, my, my, Cleanup.LOCKABLE_STATES[0], "beacon", lastBlockColor));
		mx -= mainW+1;
		s.addObject(new CleanupRoom("room1", mx-1, mx+1, my-1, my+1, colors.get(0), Cleanup.SHAPE_ROOM));
		mx += (mainW+1)*2;
		s.addObject(new CleanupRoom("room2", mx-1, mx+1, my-1, my+1, colors.get(1), Cleanup.SHAPE_ROOM));
		mx -= mainW+1;
		my -= mainH+1;
		s.addObject(new CleanupRoom("room3", mx-1, mx+1, my-1, my+1, colors.get(2), Cleanup.SHAPE_ROOM));
		my += (mainH+1)*2;
		s.addObject(new CleanupRoom("room4", mx-1, mx+1, my-1, my+1, colors.get(3), Cleanup.SHAPE_ROOM));
		
		return s;
	}
	
	public State generateCentralRoomWithClosets(int numBlocks) {

		Random rng = RandomFactory.getMapped(DEFAULT_RNG_INDEX);

		int numRooms = 5;
		int numDoors = 4;
		
		int mx = width/2;
		int my = height/2;
		
		int ax = mx;
		int ay = my;
		String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];

		CleanupState s = new CleanupState(width, height, ax, ay, agentDirection, numBlocks, numRooms, numDoors);
		
		List<String> colors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS);
		colors.add("green");
		colors.add("blue");
		colors.add("yellow");
		colors.add("red");
		
		int mainW = 2;
		int mainH = 2;
		
		int index = 0;
		do {
			int bx = ax + (rng.nextBoolean() ? -1 : 1);
			int by = ay + (rng.nextBoolean() ? -1 : 1);
			if (!s.blockAt(bx, by)) {
				String color = "";//colors.get(rng.nextInt(numDoors - 1));
				boolean fresh = false;
				while (!fresh) {
					color = colors.get(rng.nextInt(numDoors - 1));
					fresh = true;
					for (CleanupBlock block : s.getBlocks().values()) {
						if (color.equals(block.get(Cleanup.ATT_COLOR))) {
							fresh = false;
							break;
						}
					}
				}
				s.addObject(new CleanupBlock("block"+index, bx, by, "backpack", color));
				numBlocks -= 1;
				index += 1;
			}
		} while (numBlocks > 0);
		
		
		s.addObject(new CleanupRoom("room0", mx-mainW, mx+mainW, my-mainH, my+mainH, "cyan", Cleanup.SHAPE_ROOM));
		s.addObject(new CleanupDoor("door0", mx-mainW, mx-mainW, my, my, Cleanup.LOCKABLE_STATES[0]));
		s.addObject(new CleanupDoor("door1", mx+mainW, mx+mainW, my, my, Cleanup.LOCKABLE_STATES[0]));
		s.addObject(new CleanupDoor("door2", mx, mx, my-mainH, my-mainH, Cleanup.LOCKABLE_STATES[0]));
		s.addObject(new CleanupDoor("door3", mx, mx, my+mainH, my+mainH, Cleanup.LOCKABLE_STATES[0]));
		mx -= mainW+1;
		s.addObject(new CleanupRoom("room1", mx-1, mx+1, my-1, my+1, colors.get(0), Cleanup.SHAPE_ROOM));
		mx += (mainW+1)*2;
		s.addObject(new CleanupRoom("room2", mx-1, mx+1, my-1, my+1, colors.get(1), Cleanup.SHAPE_ROOM));
		mx -= mainW+1;
		my -= mainH+1;
		s.addObject(new CleanupRoom("room3", mx-1, mx+1, my-1, my+1, colors.get(2), Cleanup.SHAPE_ROOM));
		my += (mainH+1)*2;
		s.addObject(new CleanupRoom("room4", mx-1, mx+1, my-1, my+1, colors.get(3), Cleanup.SHAPE_ROOM));
		
//		int i = 0;
//		while (i < numBlocks) {
//			String id = "block"+i;
//			CleanupRoom room = (CleanupRoom) s.objectsOfClass(Cleanup.CLASS_ROOM).get(rng.nextInt(numRooms));
//			int rLeft = ((Integer) room.get(Cleanup.ATT_LEFT))+1;
//			int rRight = ((Integer) room.get(Cleanup.ATT_RIGHT))-1;
//			int rTop = ((Integer) room.get(Cleanup.ATT_TOP))-1;
//			int rBottom = ((Integer) room.get(Cleanup.ATT_BOTTOM))+1;
//			int bX = rng.nextInt(rRight - rLeft) + rLeft;
//			int bY = rng.nextInt(rTop - rBottom) + rBottom;
//			if (s.isOpen(room, bX, bY)) {
//				String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
//				String color = Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)];
//				DPrint.cl(DEBUG_CODE,"block"+i+": "+ shape + " " + color + " (" + bX + ", " + bY + ") in the " + room.get(Cleanup.ATT_COLOR) + " " + ((CleanupRoom)room).name);
//				s.addObject(new CleanupBlock(id, bX, bY, shape, color));
//				i = i + 1;
//			}
//		}
		
		return s;
	}
	
	
	@Override
	public State generateState() {
//		return generateFourRooms();
		return generateCentralRoomWithClosets(1);
	}
	
	public State generateFourRooms() {

		Random rng = RandomFactory.getMapped(DEFAULT_RNG_INDEX);

		int numRooms = 4;
		int numDoors = 4;
				
		int y1 = 3;
		int y2 = 7;
		int y3 = 12;

		int x1 = 4;
		int x2 = 8;
		int x3 = 12;

		int ax = 7;
		int ay = 1;
		String agentDirection = Cleanup.ACTION_NORTH;

		CleanupState s = new CleanupState(width, height, ax, ay, agentDirection, numBlocks, numRooms, numDoors);
		
//		s.addObject(new CleanupBlock("block0", bx, by, bShape, bColor));
		
		s.addObject(new CleanupRoom("room0", x1, x2, 0, y2, Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)], Cleanup.SHAPE_ROOM));
		s.addObject(new CleanupRoom("room1", 0, x1, y1, y2, Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)], Cleanup.SHAPE_ROOM));
		s.addObject(new CleanupRoom("room2", 0, x3, y2, y3, Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)], Cleanup.SHAPE_ROOM));
		s.addObject(new CleanupRoom("room3", x2, x3, 0, y2, Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)], Cleanup.SHAPE_ROOM));
		
		s.addObject(new CleanupDoor("door0", x2, x2, 1, 1, Cleanup.LOCKABLE_STATES[0]));
		s.addObject(new CleanupDoor("door1", x1, x1, 5, 5, Cleanup.LOCKABLE_STATES[0]));
		s.addObject(new CleanupDoor("door2", 2, 2, y2, y2, Cleanup.LOCKABLE_STATES[0]));
		s.addObject(new CleanupDoor("door3", 10, 10, y2, y2, Cleanup.LOCKABLE_STATES[0]));
		

		int i = 0;
		while (i < numBlocks) {
			String id = "block"+i;
			CleanupRoom room = (CleanupRoom) s.objectsOfClass(Cleanup.CLASS_ROOM).get(rng.nextInt(numRooms));
			int rLeft = ((Integer) room.get(Cleanup.ATT_LEFT))+1;
			int rRight = ((Integer) room.get(Cleanup.ATT_RIGHT))-1;
			int rTop = ((Integer) room.get(Cleanup.ATT_TOP))-1;
			int rBottom = ((Integer) room.get(Cleanup.ATT_BOTTOM))+1;
			int bX = rng.nextInt(rRight - rLeft) + rLeft;
			int bY = rng.nextInt(rTop - rBottom) + rBottom;
			if (s.isOpen(room, bX, bY)) {
				String shape = Cleanup.SHAPES[rng.nextInt(Cleanup.SHAPES.length)];
				String color = Cleanup.COLORS[rng.nextInt(Cleanup.COLORS.length)];
				DPrint.cl(DEBUG_CODE,"block"+i+": "+ shape + " " + color + " (" + bX + ", " + bY + ") in the " + room.get(Cleanup.ATT_COLOR) + " room");
				s.addObject(new CleanupBlock(id, bX, bY, shape, color));
				i = i + 1;
			}
		}
//		Cleanup.setBlock(s, 0, 5, 4, "chair", "blue");
//		Cleanup.setBlock(s, 1, 6, 10, "basket", "red");
//		Cleanup.setBlock(s, 2, 2, 10, "bag", "magenta");
		
		return s;
		

	}
	
	public static boolean regionContainsPoint(ObjectInstance o, int x, int y, boolean countBoundary){
		int top = (Integer) o.get(Cleanup.ATT_TOP);
		int left = (Integer) o.get(Cleanup.ATT_LEFT);
		int bottom = (Integer) o.get(Cleanup.ATT_BOTTOM);
		int right = (Integer) o.get(Cleanup.ATT_RIGHT);

		if(countBoundary){
			if(y >= bottom && y <= top && x >= left && x <= right){
				return true;
			}
		}
		else{
			if(y > bottom && y < top && x > left && x < right){
				return true;
			}
		}

		return false;
	}

	public static CleanupGoalDescription[] getRandomGoalDescription(CleanupState s, int numGoals, PropositionalFunction pf) {
		return getRandomGoalDescription(s, numGoals, pf, RandomFactory.getMapped(DEFAULT_RNG_INDEX));
	}
	
	public static CleanupGoalDescription[] getRandomGoalDescription(CleanupState s, int numGoals, PropositionalFunction pf, Random rng) {
		CleanupGoalDescription[] goals = new CleanupGoalDescription[numGoals];
		if (pf.getName().equals(Cleanup.PF_BLOCK_IN_ROOM)) {
			List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
			List<ObjectInstance> rooms = s.objectsOfClass(Cleanup.CLASS_ROOM);
			List<Integer> blockIdxs = new ArrayList<Integer>();
			for (int i = 0; i < blocks.size(); i++) { blockIdxs.add(i); }
			for (int i = 0; i < numGoals; i++) {
				ObjectInstance block = blocks.get(blockIdxs.get(i));
				ObjectInstance room = rooms.get(rng.nextInt(rooms.size()));
				while (regionContainsPoint(room, (Integer) block.get(Cleanup.ATT_X), (Integer) block.get(Cleanup.ATT_Y), true)){
					// disallow the room the block is already in
					room = rooms.get(rng.nextInt(rooms.size()));
				}
				goals[i] = new CleanupGoalDescription(new String[]{block.name(), room.name()}, pf);
				DPrint.cl(DEBUG_CODE,goals[i] + ": "
						+ block.get(Cleanup.ATT_COLOR) + " "
						+ block.get(Cleanup.ATT_SHAPE) + " to "
						+ room.get(Cleanup.ATT_COLOR) + " room");
			}
		} else if (pf.getName().equals(Cleanup.PF_AGENT_IN_DOOR)) {
			List<ObjectInstance> agents = s.objectsOfClass(Cleanup.CLASS_AGENT);
			List<ObjectInstance> doors = s.objectsOfClass(Cleanup.CLASS_DOOR);
			for (int i = 0; i < numGoals; i++) {
				ObjectInstance door = doors.get(rng.nextInt(doors.size()));
				ObjectInstance agent = agents.get(0);
				goals[i] = new CleanupGoalDescription(new String[]{agent.name(), door.name()}, pf);
				DPrint.cl(DEBUG_CODE,goals[i] + ": agent (x:"
						+ agent.get(Cleanup.ATT_X) + ", y:"
						+ agent.get(Cleanup.ATT_Y) + ") to door (x:"
						+ door.get(Cleanup.ATT_X) + ", y:"
						+ door.get(Cleanup.ATT_Y) + ")");
			}
		} else if (pf.getName().equals(Cleanup.PF_BLOCK_IN_DOOR)) {
			List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
			List<ObjectInstance> doors = s.objectsOfClass(Cleanup.CLASS_DOOR);
			for (int i = 0; i < numGoals; i++) {
				ObjectInstance door = doors.get(rng.nextInt(doors.size()));
				ObjectInstance block = blocks.get(0);
				goals[i] = new CleanupGoalDescription(new String[]{block.name(), door.name()}, pf);
				DPrint.cl(DEBUG_CODE,goals[i] + ": block (x:"
						+ block.get(Cleanup.ATT_X) + ", y:"
						+ block.get(Cleanup.ATT_Y) + ") to door (x:"
						+ door.get(Cleanup.ATT_X) + ", y:"
						+ door.get(Cleanup.ATT_Y) + ")");
			}
		} else {
			throw new RuntimeException("Randomization of goal not implemented for given propositional function.");
		}
		return goals;
	}


	public static CleanupGoalDescription[] getGoalDescriptionBlockToRoomSameColor(CleanupState s, int numGoals, PropositionalFunction pf) {
		CleanupGoalDescription[] goals = new CleanupGoalDescription[numGoals];
		List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
		List<ObjectInstance> rooms = s.objectsOfClass(Cleanup.CLASS_ROOM);
		List<Integer> blockIdxs = new ArrayList<Integer>();
		for (int i = 0; i < blocks.size(); i++) { blockIdxs.add(i); }

		// temp debug, reverse block order to get last block added to domain as the initial goal
		Collections.reverse(blockIdxs);
		
		for (int i = 0; i < numGoals; i++) {
			CleanupBlock block = (CleanupBlock) blocks.get(blockIdxs.get(i));
			String blockColor = (String) block.get(Cleanup.ATT_COLOR);
			for (int j = 0; j < rooms.size(); j++) {
				CleanupRoom room = (CleanupRoom) rooms.get(j);
				String roomColor = (String) room.get(Cleanup.ATT_COLOR);
				if (blockColor.equals(roomColor)) {
					goals[i] = new CleanupGoalDescription(new String[]{block.name(), room.name()}, pf);
					DPrint.cl(DEBUG_CODE,goals[i] + ": "
							+ block.get(Cleanup.ATT_COLOR) + " "
							+ block.get(Cleanup.ATT_SHAPE) + " to "
							+ room.get(Cleanup.ATT_COLOR) + " " + ((CleanupRoom)room).getName());
					break;
				}
			}
			if (goals[i] == null) {
				throw new RuntimeException("Error: unable to find a room of block's color, " + blockColor);
			}
		}
		return goals;
	}

	public static CleanupGoalDescription[] getGoalDescriptionBlockToDoorSameColor(CleanupState s, int numGoals, PropositionalFunction pf) {
		CleanupGoalDescription[] goals = new CleanupGoalDescription[numGoals];
		List<ObjectInstance> blocks = s.objectsOfClass(Cleanup.CLASS_BLOCK);
		List<ObjectInstance> doors = s.objectsOfClass(Cleanup.CLASS_DOOR);
		List<Integer> blockIdxs = new ArrayList<Integer>();
		for (int i = 0; i < blocks.size(); i++) { blockIdxs.add(i); }		
		for (int i = 0; i < numGoals; i++) {
			CleanupBlock block = (CleanupBlock) blocks.get(blockIdxs.get(i));
			String blockColor = (String) block.get(Cleanup.ATT_COLOR);
			for (int j = 0; j < doors.size(); j++) {
				CleanupDoor door = (CleanupDoor) doors.get(j);
				String doorColor = (String) door.get(Cleanup.ATT_COLOR);
				if (blockColor.equals(doorColor)) {
					goals[i] = new CleanupGoalDescription(new String[]{block.name(), door.name()}, pf);
					DPrint.cl(DEBUG_CODE,goals[i] + ": "
							+ block.get(Cleanup.ATT_COLOR) + " "
							+ block.get(Cleanup.ATT_SHAPE) + " to "
							+ door.get(Cleanup.ATT_COLOR) + " " + ((CleanupDoor)door).getName());
					break;
				}
			}
			if (goals[i] == null) {
				throw new RuntimeException("Error: unable to find a room of block's color, " + blockColor);
			}
		}
		return goals;
	}

	public OOState generateOneRoomOneDoor() {
		
		Random rng = RandomFactory.getMapped(DEFAULT_RNG_INDEX);
		
		int numBlocks = 0;
		int numRooms = 1;
		int numDoors = 1;
		
		int mx = width/2;
		int my = height/2;
		int ax = mx;
		int ay = my;
		String agentDirection = Cleanup.directions[rng.nextInt(Cleanup.directions.length)];
		CleanupState s = new CleanupState(width, height, ax, ay, agentDirection, numBlocks, numRooms, numDoors);
		
		List<String> blockColors = new ArrayList<String>(); // Arrays.asList(Cleanup.COLORS);
		blockColors.add("green");
		blockColors.add("blue");
		blockColors.add("yellow");
		blockColors.add("red");
		blockColors.add("magenta");
		List<String> roomColors = new ArrayList<String>();
		roomColors.addAll(blockColors);
		roomColors.add("cyan");
		roomColors.add("orange");
		roomColors.add("white");
		
		int mainW = 2;
		int mainH = 2;
		
		int index = 0;
		while (numBlocks > 0) {
			int bx = ax + (rng.nextBoolean() ? -1 : 1);
			int by = ay + (rng.nextBoolean() ? -1 : 1);
			if (!s.blockAt(bx, by)) {
				s.addObject(new CleanupBlock("block"+index, bx, by, "backpack", blockColors.get(rng.nextInt(numDoors - 1))));
				numBlocks -= 1;
				index += 1;
			}
		};
		
		
		String roomColor = roomColors.get(rng.nextInt(roomColors.size()));
		CleanupRoom room = new CleanupRoom("room0", mx-mainW, mx+mainW, my-mainH, my+mainH, roomColor, Cleanup.SHAPE_ROOM);
		int rx = ((Integer)room.get(Cleanup.ATT_LEFT));
		int ry = ((Integer)room.get(Cleanup.ATT_BOTTOM));
		int rWidth  = ((Integer)room.get(Cleanup.ATT_RIGHT)) - ((Integer)room.get(Cleanup.ATT_LEFT));
		int rHeight = ((Integer)room.get(Cleanup.ATT_TOP)) - ((Integer)room.get(Cleanup.ATT_BOTTOM));
		boolean leftOrBottom = rng.nextBoolean();
		int dx = 0;
		int dy = 0;
		boolean onVerticalWall = rng.nextBoolean();
		if (onVerticalWall) {
			dx = leftOrBottom ? rx : rx + rWidth;
			dy = 1 + ry + rng.nextInt(rHeight-1);
		} else {
			dx = 1 + rx + rng.nextInt(rWidth-1);
			dy = leftOrBottom ? ry : ry + rHeight;
		}
		CleanupDoor door = new CleanupDoor("door0", dx, dx, dy, dy, Cleanup.LOCKABLE_STATES[0], Cleanup.SHAPE_DOOR, blockColors.get(0));
		
		s.addObject(room);
		s.addObject(door);
		
		return s;
	}
	
	
}
