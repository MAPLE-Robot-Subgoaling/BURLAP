package cleanup;

import burlap.behavior.valuefunction.ValueFunction;
import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.HashableState;
import burlap.visualizer.Visualizer;
import cleanup.state.*;
import burlap.statehashing.selected.SelectedHashableStateFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class Cleanup implements DomainGenerator {


    public static final String ATT_X = "x";
    public static final String ATT_Y = "y";
    public static final String ATT_DIR = "direction";
    public static final String ATT_MAP = "map";
    public static final String ATT_TOP = "top";
    public static final String ATT_LEFT = "left";
    public static final String ATT_BOTTOM = "bottom";
    public static final String ATT_RIGHT = "right";
    public static final String ATT_COLOR = "color";
    public static final String ATT_SHAPE = "shape";
    public static final String ATT_LOCKED = "locked";

    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_BLOCK = "block";
    public static final String CLASS_ROOM = "room";
    public static final String CLASS_DOOR = "door";

    public static final String ACTION_NORTH = "north";
    public static final String ACTION_SOUTH = "south";
    public static final String ACTION_EAST = "east";
    public static final String ACTION_WEST = "west";
    public static final String ACTION_PULL = "pull";
    public static final String[] directions = {ACTION_NORTH, ACTION_SOUTH, ACTION_EAST, ACTION_WEST};

    public static final String PF_AGENT_IN_ROOM = "agentInRoom";
    public static final String PF_BLOCK_IN_ROOM = "blockInRoom";
    public static final String PF_AGENT_IN_DOOR = "agentInDoor";
    public static final String PF_BLOCK_IN_DOOR = "blockInDoor";

    public static final String WALL_NORTH = "wallNorth";
    public static final String WALL_SOUTH = "wallSouth";
    public static final String WALL_EAST = "wallEast";
    public static final String WALL_WEST = "wallWest";

    public static final String[] COLORS_BLOCKS = new String[]{"blue",
            "green", "magenta",
            "red", "yellow"};
    public static final String[] COLORS_ROOMS = new String[]{"blue",
            "green", "magenta",
            "red", "yellow",
            "orange", "cyan", "white"};
    public static final String COLOR_GRAY = "gray";

    public static final String[] SHAPES = new String[]{"chair", "bag", "backpack", "basket"};

    public static final String SHAPE_ROOM = "shapeRoom";
    public static final String SHAPE_DOOR = "shapeDoor";
    public static final String SHAPE_AGENT = "shapeAgent";


    public static final String[] DIRECTIONS = new String[]{"north", "south", "east", "west"};

    public static final String[] LOCKABLE_STATES = new String[]{"unknown", "unlocked", "locked"};

    protected static final String RCOLORBASE = "roomIs";
    protected static final String BCOLORBASE = "blockIs";
    protected static final String BSHAPEBASE = "shape";

    public static final double REWARD_GOAL = 1000;
    public static final double REWARD_DEFAULT = -1;
    public static final double REWARD_NOOP = -1;
    public static final double REWARD_PULL = -0.01;

    private RewardFunction rf;
    private TerminalFunction tf;


    protected int minX;
    protected int minY;
    protected int maxX;
    protected int maxY;
//	protected boolean includeDirectionAttribute = false;
//	protected boolean includePullAction = false;
//	protected boolean includeWalls = false;
//	protected boolean lockableDoors = false;
//	protected double lockProb = 0.0;

    public Cleanup() {

    }

    public Cleanup(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setRf(RewardFunction rf) {
        this.rf = rf;
    }

    public void setTf(TerminalFunction tf) {
        this.tf = tf;
    }

    public List<PropositionalFunction> generatePfs() {
        List<PropositionalFunction> pfs = new ArrayList<PropositionalFunction>();

        pfs.add(new InRegion(PF_AGENT_IN_ROOM, new String[]{CLASS_AGENT, CLASS_ROOM}, false));
        pfs.add(new InRegion(PF_BLOCK_IN_ROOM, new String[]{CLASS_BLOCK, CLASS_ROOM}, false));

        pfs.add(new InRegion(PF_AGENT_IN_DOOR, new String[]{CLASS_AGENT, CLASS_DOOR}, true));
        pfs.add(new InRegion(PF_BLOCK_IN_DOOR, new String[]{CLASS_BLOCK, CLASS_DOOR}, true));

        for (String col : COLORS_ROOMS) {
            pfs.add(new IsColor(RoomColorName(col), new String[]{CLASS_ROOM}, col));
            pfs.add(new IsColor(BlockColorName(col), new String[]{CLASS_BLOCK}, col));
        }

        for (String shape : SHAPES) {
            pfs.add(new IsShape(BlockShapeName(shape), new String[]{CLASS_BLOCK}, shape));
        }

//		if(this.includeWalls){
        pfs.add(new WallTest(WALL_NORTH, 0, 1));
        pfs.add(new WallTest(WALL_SOUTH, 0, -1));
        pfs.add(new WallTest(WALL_EAST, 1, 0));
        pfs.add(new WallTest(WALL_WEST, -1, 0));
//		}
        return pfs;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

//	public void includeDirectionAttribute(boolean includeDirectionAttribute){
//		this.includeDirectionAttribute = includeDirectionAttribute;
//	}
//
//	public void includeLockableDoors(boolean lockableDoors){
//		this.lockableDoors = lockableDoors;
//	}
//
//	public void setLockProbability(double lockProb){
//		this.lockProb = lockProb;
//	}

    public static String RoomColorName(String color) {
        String capped = firstLetterCapped(color);
        return RCOLORBASE + capped;
    }

    public static String BlockColorName(String color) {
        String capped = firstLetterCapped(color);
        return BCOLORBASE + capped;
    }

    public static String BlockShapeName(String shape) {
        String capped = firstLetterCapped(shape);
        return BSHAPEBASE + capped;
    }


    public static int maxRoomXExtent(OOState s) {

        int max = 0;
        List<ObjectInstance> rooms = s.objectsOfClass(CLASS_ROOM);
        for (ObjectInstance r : rooms) {
            int right = (Integer) r.get(ATT_RIGHT);
            if (right > max) {
                max = right;
            }
        }

        return max;
    }

    public static int maxRoomYExtent(OOState s) {

        int max = 0;
        List<ObjectInstance> rooms = s.objectsOfClass(CLASS_ROOM);
        for (ObjectInstance r : rooms) {
            int top = (Integer) r.get(ATT_TOP);
            if (top > max) {
                max = top;
            }
        }

        return max;
    }


    protected static String firstLetterCapped(String s) {
        String firstLetter = s.substring(0, 1);
        String remainder = s.substring(1);
        return firstLetter.toUpperCase() + remainder;
    }


    public class InRegion extends PropositionalFunction {

        protected boolean countBoundary;

        public InRegion(String name, String[] parameterClasses, boolean countBoundary) {
            super(name, parameterClasses);
            this.countBoundary = countBoundary;
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
            CleanupState cws = (CleanupState) s;
            ObjectInstance o = cws.object(params[0]);
            ObjectInstance region = cws.object(params[1]);
            if (o == null || region == null) {
                return false;
            }
            int x = (Integer) o.get(ATT_X);
            int y = (Integer) o.get(ATT_Y);
            return CleanupState.regionContainsPoint(region, x, y, countBoundary);
        }
    }

    public class IsColor extends PropositionalFunction {

        protected String colorName;

        public IsColor(String name, String[] params, String color) {
            super(name, params);
            this.colorName = color;
        }

        @Override
        public boolean isTrue(OOState s, String... params) {

            ObjectInstance o = s.object(params[0]);
            String col = o.get(ATT_COLOR).toString();

            return this.colorName.equals(col);
        }
    }

    public class IsShape extends PropositionalFunction {

        protected String shapeName;

        public IsShape(String name, String[] params, String shape) {
            super(name, params);
            this.shapeName = shape;
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
            ObjectInstance o = s.object(params[0]);
            String shape = o.get(ATT_SHAPE).toString();

            return this.shapeName.equals(shape);
        }
    }


    public class WallTest extends PropositionalFunction {

        protected int dx;
        protected int dy;

        public WallTest(String name, int dx, int dy) {
            super(name, new String[]{CLASS_AGENT});
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public boolean isTrue(OOState s, String... params) {
            CleanupState cws = (CleanupState) s;
            ObjectInstance agent = cws.objectsOfClass(CLASS_AGENT).get(0);
            int ax = (Integer) agent.get(ATT_X);
            int ay = (Integer) agent.get(ATT_Y);
            return cws.wallAt(ax + this.dx, ay + this.dy);
        }
    }

    @Override
    public Domain generateDomain() {
        OOSADomain domain = new OOSADomain();
        domain.addStateClass(CLASS_AGENT, CleanupAgent.class)
                .addStateClass(CLASS_BLOCK, CleanupBlock.class)
                .addStateClass(CLASS_ROOM, CleanupRoom.class)
                .addStateClass(CLASS_DOOR, CleanupDoor.class);
        domain.addActionTypes(
                new UniversalActionType(ACTION_NORTH),
                new UniversalActionType(ACTION_SOUTH),
                new UniversalActionType(ACTION_EAST),
                new UniversalActionType(ACTION_WEST),
                new UniversalActionType(ACTION_PULL));
//				new PullActionType(ACTION_PULL));
        OODomain.Helper.addPfsToDomain(domain, this.generatePfs());
        CleanupModel smodel = new CleanupModel(minX, minY, maxX, maxY, domain.getActionTypes().size());
        RewardFunction rf = this.rf;
        TerminalFunction tf = this.tf;
        if (rf == null) {
            rf = new UniformCostRF();
        }
        if (tf == null) {
            tf = new NullTermination();
        }
        FactoredModel model = new FactoredModel(smodel, rf, tf);
        domain.setModel(model);
        return domain;
    }

    public RewardFunction getRf() {
        return rf;
    }

	/*
    public class PullActionType extends ObjectParameterizedActionType {

		public PullActionType(String name){
			super(name,new String[]{Cleanup.CLASS_AGENT, Cleanup.CLASS_BLOCK});
		}
		
		public boolean applicableInState(State st, ObjectParameterizedAction groundedAction){
			CleanupState cws = (CleanupState)st;
			String [] params = groundedAction.getObjectParameters();
			CleanupAgent agent = (CleanupAgent)cws.object(params[0]);
			CleanupBlock block = (CleanupBlock)cws.object(params[1]);
			if (agent == null || block == null) {
				return false;
			}
			
			if (agent.get(ATT_DIR) == null) {
				return false;
			}
			
			int direction = CleanupModel.actionDir(agent.get(ATT_DIR).toString());
			int curX = (Integer) agent.get(ATT_X);
			int curY = (Integer) agent.get(ATT_Y);
			//first get change in x and y from direction using 0: north; 1: south; 2:east; 3: west
			int xdelta = 0;
			int ydelta = 0;
			if(direction == 0){
				ydelta = 1;
			} else if(direction == 1){
				ydelta = -1;
			} else if(direction == 2){
				xdelta = 1;
			} else{
				xdelta = -1;
			}
			int nx = curX + xdelta;
			int ny = curY + ydelta;
			if ((Integer)block.get(ATT_X) == nx && (Integer)block.get(ATT_Y) == ny) {
				return true;
			}
			return false;
		}
	}
	*/

    public CleanupState getEmptyState(int ax, int ay) {
        int width = maxX - minX;
        int height = maxY - minY;
        CleanupState s = new CleanupState(width, height, ax, ay, DIRECTIONS[1], 0, 0, 0);
        return s;
    }

    public CleanupState getSimpleState(int ax, int ay) {
        int width = maxX - minX;
        int height = maxY - minY;
        int nBlocks = 1;
        int nRooms = 2;
        int nDoors = 1;

        CleanupState s = new CleanupState(width, height, ax, ay, DIRECTIONS[1], nBlocks, nRooms, nDoors);
        s.addObject(new CleanupBlock("block0", 3, 1, "basket", "blue"));
        s.addObject(new CleanupRoom("room0", 0, width / 2, 0, maxY - 1, "green", SHAPE_ROOM));
        s.addObject(new CleanupRoom("room1", width / 2, maxX - 1, 0, maxY - 1, "blue", SHAPE_ROOM));
        s.addObject(new CleanupDoor("door0", width / 2, width / 2, 1, 1, LOCKABLE_STATES[0]));

        return s;
    }

    public CleanupState getClassicState(int ax, int ay) {
        int width = maxX - minX;
        int height = maxY - minY;
        int nBlocks = 3;
        int nRooms = 3;
        int nDoors = 2;

        CleanupState s = new CleanupState(width, height, ax, ay, DIRECTIONS[1], nBlocks, nRooms, nDoors);
        s.addObject(new CleanupBlock("block0", 2, 2, "basket", "blue"));
        s.addObject(new CleanupBlock("block1", 6, 7, "chair", "blue"));
        s.addObject(new CleanupBlock("block2", 2, 7, "basket", "green"));
        s.addObject(new CleanupRoom("room0", 0, width / 2, 0, (maxY / 2), "green"));
        s.addObject(new CleanupRoom("room1", width / 2, maxX - 1, 0, (maxY / 2), "blue"));
        s.addObject(new CleanupRoom("room2", 0, maxX - 1, (maxY / 2), maxY - 1, "yellow"));
        s.addObject(new CleanupDoor("door0", width / 4, width / 4, (maxY / 2), (maxY / 2), LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door1", 3 * width / 4, 3 * width / 4, maxY / 2, maxY / 2, LOCKABLE_STATES[0]));

        return s;
    }

    public CleanupState getClassicStateAlt(int ax, int ay) {
        int width = maxX - minX;
        int height = maxY - minY;
        int nBlocks = 3;
        int nRooms = 3;
        int nDoors = 2;

        CleanupState s = new CleanupState(width, height, ax, ay, DIRECTIONS[1], nBlocks, nRooms, nDoors);
        s.addObject(new CleanupBlock("block0", 2, 2, "basket", "blue"));
        s.addObject(new CleanupBlock("block1", 6, 7, "chair", "blue"));
        s.addObject(new CleanupBlock("block2", 2, 7, "basket", "yellow"));
        s.addObject(new CleanupRoom("room0", 0, width / 2, 0, (maxY / 2), "yellow"));
        s.addObject(new CleanupRoom("room1", width / 2, maxX - 1, 0, (maxY / 2), "blue"));
        s.addObject(new CleanupRoom("room2", 0, maxX - 1, (maxY / 2), maxY - 1, "green"));
        s.addObject(new CleanupDoor("door0", width / 4, width / 4, (maxY / 2), (maxY / 2), LOCKABLE_STATES[0]));
        s.addObject(new CleanupDoor("door1", 3 * width / 4, 3 * width / 4, maxY / 2, maxY / 2, LOCKABLE_STATES[0]));

        return s;
    }

    public static ValueFunction getGroundHeuristic(State s, RewardFunction rf, double lockProb) {

        double discount = 0.99;
        // prop name if block -> block and room if
        CleanupGoal rfCondition = (CleanupGoal) ((CleanupRF) rf).getGoalCondition();
        String PFName = rfCondition.goals[0].pf.getName();
        String[] params = rfCondition.goals[0].objects;
        if (PFName.equals(Cleanup.PF_AGENT_IN_ROOM)) {
            return new AgentToRegionHeuristic(params[1], discount, lockProb);
        } else if (PFName.equals(Cleanup.PF_AGENT_IN_DOOR)) {
            return new AgentToRegionHeuristic(params[1], discount, lockProb);
        } else if (PFName.equals(Cleanup.PF_BLOCK_IN_ROOM)) {
            return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        } else if (PFName.equals(Cleanup.PF_BLOCK_IN_DOOR)) {
            return new BlockToRegionHeuristic(params[0], params[1], discount, lockProb);
        }
        throw new RuntimeException("Unknown Reward Function with propositional function " + PFName + ". Cannot construct l0 heuristic.");
    }

    public static class AgentToRegionHeuristic implements ValueFunction {

        String goalRegion;
        double discount;
        double lockProb;

        public AgentToRegionHeuristic(String goalRegion, double discount, double lockProb) {
            this.goalRegion = goalRegion;
            this.discount = discount;
            this.lockProb = lockProb;
        }

        //@Override
        //public double qValue(State s, AbstractGroundedAction a) {
        //    return value(s);
        //}

        @Override
        public double value(State s) {

            int delta = 1;
            boolean freeRegion = true;
            ObjectInstance region = ((CleanupState) s).object(this.goalRegion);
            if (region.className().equals(Cleanup.CLASS_DOOR)) {
                delta = 0;
            }


            //get the agent
            CleanupAgent agent = ((CleanupState) s).getAgent();
            int ax = (Integer) agent.get(Cleanup.ATT_X);
            int ay = (Integer) agent.get(Cleanup.ATT_Y);


            int l = (Integer) region.get(Cleanup.ATT_LEFT);
            int r = (Integer) region.get(Cleanup.ATT_RIGHT);
            int b = (Integer) region.get(Cleanup.ATT_BOTTOM);
            int t = (Integer) region.get(Cleanup.ATT_TOP);

            int dist = toRegionManDistance(ax, ay, l, r, b, t, delta);

            double fullChanceV = Math.pow(discount, dist - 1);
            double v = freeRegion ? fullChanceV : lockProb * fullChanceV + (1. - lockProb) * 0;

            return v;
        }


    }


    public static class BlockToRegionHeuristic implements ValueFunction {

        String blockName;
        String goalRegion;
        double discount;
        double lockProb;

        public BlockToRegionHeuristic(String blockName, String goalRegion, double discount, double lockProb) {
            this.blockName = blockName;
            this.goalRegion = goalRegion;
            this.discount = discount;
            this.lockProb = lockProb;
        }
//
//        @Override
//        public double qValue(State s, AbstractGroundedAction a) {
//            return value(s);
//        }

        @Override
        public double value(State s) {

            int delta = 1;
            boolean freeRegion = true;
            ObjectInstance region = ((CleanupState) s).object(this.goalRegion);
            if (region.className().equals(Cleanup.CLASS_DOOR)) {
                delta = 0;
            }


            //get the agent
            CleanupAgent agent = ((CleanupState) s).getAgent();
            int ax = (Integer) agent.get(Cleanup.ATT_X);
            int ay = (Integer) agent.get(Cleanup.ATT_Y);


            int l = (Integer) region.get(Cleanup.ATT_LEFT);
            int r = (Integer) region.get(Cleanup.ATT_RIGHT);
            int b = (Integer) region.get(Cleanup.ATT_BOTTOM);
            int t = (Integer) region.get(Cleanup.ATT_TOP);

            //get the block
            ObjectInstance block = ((CleanupState) s).object(this.blockName);
            int bx = (Integer) block.get(Cleanup.ATT_X);
            int by = (Integer) block.get(Cleanup.ATT_Y);

            int dist = manDistance(ax, ay, bx, by) - 1; //need to be one step away from block to push it

            //and then block needs to be at room
            dist += toRegionManDistance(bx, by, l, r, b, t, delta);

            double fullChanceV = Math.pow(discount, dist - 1);
            double v = freeRegion ? fullChanceV : lockProb * fullChanceV + (1. - lockProb) * 0.;

            return v;
        }
    }


    public static int manDistance(int x0, int y0, int x1, int y1) {
        return Math.abs(x0 - x1) + Math.abs(y0 - y1);
    }


    /**
     * Manhatten distance to a room or door.
     *
     * @param x
     * @param y
     * @param l
     * @param r
     * @param b
     * @param t
     * @param delta set to 1 for rooms because boundaries are walls which are not sufficient to be in room; 0 for doors
     * @return
     */
    public static int toRegionManDistance(int x, int y, int l, int r, int b, int t, int delta) {
        int dist = 0;

        //use +1s because boundaries define wall, which is not sufficient to be in the room
        if (x <= l) {
            dist += l - x + delta;
        } else if (x >= r) {
            dist += x - r + delta;
        }

        if (y <= b) {
            dist += b - y + delta;
        } else if (y >= t) {
            dist += y - t + delta;
        }

        return dist;
    }


    public static void main(String[] args) {

//        RandomFactory.seedMapped(0, 575L);
//        Random rng = RandomFactory.getMapped(0);

        List<String> objectAttributes = new ArrayList<String>();
        Cleanup cleanup = new Cleanup();
        OOSADomain domain = (OOSADomain) cleanup.generateDomain();
        CleanupRandomStateGenerator gen = new CleanupRandomStateGenerator();
        int size = 9 + RandomFactory.getDefault().nextInt(10);
        gen.setWidth(size);
        gen.setHeight(size);
        int numBlocks1 = 1;
        int numBlocks2 = 2;
        State state1 = gen.generateTwoRoomsWithFourDoors(numBlocks1); //gen.generateCentralRoomWithFourDoors(numBlocks1);
        State state2 = gen.getStateFor("noRoomsOneDoor",numBlocks2);//gen.generateTwoRoomsWithFourDoors(numBlocks2); //gen.generateCentralRoomWithFourDoors(numBlocks2);

        System.out.println(state1);
        System.out.println(state2);

        SelectedHashableStateFactory shsf = new SelectedHashableStateFactory();
        HashableState hs1 = shsf.hashState(state1);
        HashableState hs2 = shsf.hashState(state2);

        System.out.println(state1.equals(state1));
        System.out.println(state2.equals(state2));
        System.out.println(state1.equals(state2));

        // select no objectAttributes
        System.out.println(hs1.equals(hs1));
        System.out.println(hs2.equals(hs2));
        System.out.println(hs1.equals(hs2));

        objectAttributes.clear();
        objectAttributes.add("agent0:left");
        shsf.setSelection(objectAttributes);
        System.out.println(hs1.equals(hs1));
        System.out.println(hs2.equals(hs2));
        System.out.println(hs1.equals(hs2));

        objectAttributes.clear();
        objectAttributes.add("agent0:direction");
        shsf.setSelection(objectAttributes);
        System.out.println(hs1.equals(hs1));
        System.out.println(hs2.equals(hs2));
        System.out.println(hs1.equals(hs2));


        Visualizer v = CleanupVisualizer.getVisualizer(size, size);
		VisualExplorer exp = new VisualExplorer(domain, v, state2);
		exp.addKeyAction("w", ACTION_NORTH, "");
		exp.addKeyAction("s", ACTION_SOUTH, "");
		exp.addKeyAction("d", ACTION_EAST, "");
		exp.addKeyAction("a", ACTION_WEST, "");
		exp.addKeyAction("r", ACTION_PULL, "");
		exp.initGUI();
		exp.requestFocus();
		exp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//		OOSADomain domain;
//		RewardFunction rf;
//		TerminalFunction tf;
//		CleanupGoal goalCondition;
//		OOState initialState;
//		HashableStateFactory hashingFactory;
//		Environment env;
//		int minX = 0;
//		int minY = 0;
//		int width = 9;
//		int height = 9;
//		goalCondition = new CleanupGoal();
//		rf = new CleanupRF(goalCondition);
//		tf = new GoalConditionTF(goalCondition);
//		Cleanup gen = new Cleanup(minX, minY, minX + width, minY + height);
//		gen.setRf(rf);
//		gen.setTf(tf);
//		domain = (OOSADomain) gen.generateDomain();
//		CleanupGoalDescription[] goals = new CleanupGoalDescription[]{
//				new CleanupGoalDescription(new String[]{"block0", "room1"}, domain.propFunction(PF_BLOCK_IN_ROOM)),
//				new CleanupGoalDescription(new String[]{"block1", "room1"}, domain.propFunction(PF_BLOCK_IN_ROOM)),
//				new CleanupGoalDescription(new String[]{"block2", "room0"}, domain.propFunction(PF_BLOCK_IN_ROOM))
//		};
//		goalCondition.setGoals(goals);
//		initialState = gen.getClassicState(2, 1);
//		hashingFactory = new SimpleHashableStateFactory();
//		env = new SimulatedEnvironment(domain, initialState);
//
//		Visualizer v = CleanupVisualizer.getVisualizer(width, height);
//		VisualExplorer exp = new VisualExplorer(domain, v, initialState);
//		exp.addKeyAction("w", ACTION_NORTH, "");
//		exp.addKeyAction("s", ACTION_SOUTH, "");
//		exp.addKeyAction("d", ACTION_EAST, "");
//		exp.addKeyAction("a", ACTION_WEST, "");
//		exp.addKeyAction("r", ACTION_PULL, "");
//		exp.initGUI();
//		exp.requestFocus();
//		exp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//		String outputPath = "./output/";
//		double gamma = 0.9;
//		double qInit = 0;
//		double learningRate = 0.01;
//		int nEpisodes = 100;
//		int maxEpisodeSize = 1001;
//		int writeEvery = 1;
//		LearningAgent agent = new QLearning(domain, gamma, hashingFactory, qInit, learningRate, maxEpisodeSize);
//		for(int i = 0; i < nEpisodes; i++){
//			Episode e = agent.runLearningEpisode(env, maxEpisodeSize);
//			if (i % writeEvery == 0) {
//				e.write(outputPath + "ql_" + i);
//			}
//			System.out.println(i + ": " + e.maxTimeStep());
//			env.resetEnvironment();
//		}
//		Visualizer v = CleanupVisualizer.getVisualizer(width, height);
//		EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(v, domain, outputPath);
//		esv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public TerminalFunction getTf() {
        return tf;
    }
}