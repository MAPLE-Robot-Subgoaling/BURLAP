package cleanup;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import cleanup.state.CleanupAgent;
import cleanup.state.CleanupBlock;
import cleanup.state.CleanupState;

import java.util.List;

public class CleanupModel implements FullStateModel {

    protected double[][] transitionProbs;
//		private double lockProb;

    public CleanupModel() {

    }

    public CleanupModel(int numActions) {
        this.transitionProbs = new double[numActions][numActions];
        for (int i = 0; i < numActions; i++) {
            for (int j = 0; j < numActions; j++) {
                double p = i != j ? 0 : 1;
                transitionProbs[i][j] = p;
            }
        }
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        return FullStateModel.Helper.deterministicTransition(this, s, a);
    }

    @Override
    public State sample(State s, Action a) {
        s = s.copy();
        String actionName = a.actionName();
        if (actionName.equals(Cleanup.ACTION_NORTH)
                || actionName.equals(Cleanup.ACTION_SOUTH)
                || actionName.equals(Cleanup.ACTION_EAST)
                || actionName.equals(Cleanup.ACTION_WEST)) {
            return move(s, actionName);
        } else if (actionName.equals(Cleanup.ACTION_PULL)) {
//				return pull(s, (SAObjectParameterizedAction)a);
            return pull(s, a);
        }
        throw new RuntimeException("Unknown action " + actionName);
    }

    //		public State pull(State s, SAObjectParameterizedAction action) {
    public State pull(State s, Action action) {

        CleanupState cws = (CleanupState) s;

        CleanupAgent agent = cws.getAgent();
        int direction = actionDir(agent.get(Cleanup.ATT_DIR).toString());
        int ax = (Integer) agent.get(Cleanup.ATT_X);
        int ay = (Integer) agent.get(Cleanup.ATT_Y);
        //first get change in x and y from direction using 0: north; 1: south; 2:east; 3: west
        int xdelta = 0;
        int ydelta = 0;
        if (direction == 0) {
            ydelta = 1;
        } else if (direction == 1) {
            ydelta = -1;
        } else if (direction == 2) {
            xdelta = 1;
        } else {
            xdelta = -1;
        }
        int nx = ax + xdelta;
        int ny = ay + ydelta;
        CleanupBlock block = cws.getBlockAtPoint(nx, ny);
        if (block != null) {
            int bx = (Integer) block.get(Cleanup.ATT_X);
            int by = (Integer) block.get(Cleanup.ATT_Y);
            int nbx = ax;
            int nby = ay;
            CleanupBlock nBlock = cws.touchBlock(block.name());
            nBlock.set(Cleanup.ATT_X, nbx);
            nBlock.set(Cleanup.ATT_LEFT, nbx);
            nBlock.set(Cleanup.ATT_RIGHT, nbx);
            nBlock.set(Cleanup.ATT_Y, nby);
            nBlock.set(Cleanup.ATT_TOP, nby);
            nBlock.set(Cleanup.ATT_BOTTOM, nby);
            //face in direction of the block movement
            String newDirection = "";
            if (by - ay > 0) {
                newDirection = Cleanup.ACTION_SOUTH;
            } else if (by - ay < 0) {
                newDirection = Cleanup.ACTION_NORTH;
            } else if (bx - ax > 0) {
                newDirection = Cleanup.ACTION_WEST;
            } else if (bx - ax < 0) {
                newDirection = Cleanup.ACTION_EAST;
            }
            CleanupAgent nAgent = cws.touchAgent();
            nAgent.set(Cleanup.ATT_X, nx);
            nAgent.set(Cleanup.ATT_LEFT, nx);
            nAgent.set(Cleanup.ATT_RIGHT, nx);
            nAgent.set(Cleanup.ATT_Y, ny);
            nAgent.set(Cleanup.ATT_TOP, ny);
            nAgent.set(Cleanup.ATT_BOTTOM, ny);
            nAgent.set(Cleanup.ATT_DIR, newDirection);
        }
        return s;
    }

    public State move(State s, String actionName) {

        CleanupState cws = (CleanupState) s;
        CleanupAgent agent = cws.getAgent();
        int direction = actionDir(actionName);
        int curX = (Integer) agent.get(Cleanup.ATT_X);
        int curY = (Integer) agent.get(Cleanup.ATT_Y);
        //first get change in x and y from direction using 0: north; 1: south; 2:east; 3: west
        int xdelta = 0;
        int ydelta = 0;
        if (direction == 0) {
            ydelta = 1;
        } else if (direction == 1) {
            ydelta = -1;
        } else if (direction == 2) {
            xdelta = 1;
        } else {
            xdelta = -1;
        }
        int nx = curX + xdelta;
        int ny = curY + ydelta;
        int nbx = nx;
        int nby = ny;

        boolean agentCanMove = false;
        boolean blockCanMove = false;
        CleanupBlock pushedBlock = cws.getBlockAtPoint(nx, ny);
        if (pushedBlock == null) {
            if (!cws.wallAt(nx, ny)) {
                agentCanMove = true;
            }
        } else {
            int bx = (Integer) pushedBlock.get(Cleanup.ATT_X);
            int by = (Integer) pushedBlock.get(Cleanup.ATT_Y);
            nbx = bx + xdelta;
            nby = by + ydelta;
            if (cws.isOpen(nbx, nby)) {
                blockCanMove = true;
                agentCanMove = true;
//					}
            }
        }
        CleanupAgent nAgent = cws.touchAgent();
        if (agentCanMove) {
            if (blockCanMove) {
                CleanupBlock nBlock = cws.touchBlock(pushedBlock.name());
                nBlock.set(Cleanup.ATT_X, nbx);
                nBlock.set(Cleanup.ATT_LEFT, nbx);
                nBlock.set(Cleanup.ATT_RIGHT, nbx);
                nBlock.set(Cleanup.ATT_Y, nby);
                nBlock.set(Cleanup.ATT_TOP, nby);
                nBlock.set(Cleanup.ATT_BOTTOM, nby);
            }
            nAgent.set(Cleanup.ATT_X, nx);
            nAgent.set(Cleanup.ATT_LEFT, nx);
            nAgent.set(Cleanup.ATT_RIGHT, nx);
            nAgent.set(Cleanup.ATT_Y, ny);
            nAgent.set(Cleanup.ATT_TOP, ny);
            nAgent.set(Cleanup.ATT_BOTTOM, ny);
        }
        nAgent.set(Cleanup.ATT_DIR, actionName);
        return s;
    }

    protected boolean checkDoorLockStatus(CleanupState cws, int nbx, int nby) {
        boolean isUnlocked = true;
//			CleanupDoor doorAtNewPoint = cws.doorContainingPoint(nbx, nby);
//			if(doorAtNewPoint != null){
//				String val = doorAtNewPoint.get(Cleanup.ATT_LOCKED).toString();
//				if(val.equals("locked")) { //locked door
//					updatePosition = false;
//				} else if(val.equals("unknown")){ //unknown door
//					CleanupDoor copy = (CleanupDoor) doorAtNewPoint.copy();
//					double roll = RandomFactory.getMapped(0).nextDouble();
//					if(roll < this.lockProb){
//						updatePosition = false;
//						copy.set(Cleanup.ATT_LOCKED, "locked");
//					} else{
//						//unlock the door
//						copy.set(Cleanup.ATT_LOCKED, "unlocked");
//					}
//					s.doors.put(copy.name(),copy);
//				}
//			}
        return isUnlocked;
    }

    protected static int actionDir(String actionName) {
        int direction = -1;
        if (actionName.equals(Cleanup.ACTION_NORTH)) {
            direction = 0;
        } else if (actionName.equals(Cleanup.ACTION_SOUTH)) {
            direction = 1;
        } else if (actionName.equals(Cleanup.ACTION_EAST)) {
            direction = 2;
        } else if (actionName.equals(Cleanup.ACTION_WEST)) {
            direction = 3;
        } else {
            throw new RuntimeException("ERROR: not a valid direction for " + actionName);
        }
        return direction;
    }

    protected int actionDir(Action a) {
        return actionDir(a.actionName());
    }

}