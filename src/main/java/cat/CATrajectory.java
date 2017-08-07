package cat;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.TransitionProb;

import java.util.*;

public class CATrajectory {

	private List<String> actions;
	private List<CausalEdge> edges;
	private Set<String>[] checkedVariables, changedVariables;
	private Episode baseTrajectory;
	
	public CATrajectory() {
		this.actions = new ArrayList<String>();
		this.edges = new ArrayList<CausalEdge>();
	}
	
	//parent structure  action -> variable/ R(reward) -> relevant var
	public void annotateTrajectory(Episode e, Map<String, Map<String, VariableTree>> decisions, FullModel model){
		baseTrajectory = e;
		actions.add("START");
		for(Action a : e.actionSequence){
			actions.add(a.actionName());
		}
		actions.add("END");
		
		checkedVariables = new Set[actions.size()];
		changedVariables = new Set[actions.size()];

		for(int i = 0; i < actions.size(); i++){
			String action = actions.get(i);
			checkedVariables[i] = new HashSet<String>();
			changedVariables[i] = new HashSet<String>();

			if(action.equals("START") || action.equals("END")){
				State s = e.stateSequence.get(0);
				for(Object var : s.variableKeys()){
					checkedVariables[i].add(var.toString());
					changedVariables[i].add(var.toString());
				}
				continue;

			}

			State s = e.stateSequence.get(i - 1);
			Action a = e.actionSequence.get(i - 1);

			//add the vars which were used to get reward
			VariableTree rewardTree = decisions.get(action).get("R");
			List<String> rewardChecked = rewardTree.getCheckedVariables(s);
			checkedVariables[i].addAll(rewardChecked);

			for(Object var : s.variableKeys()){
				Object sVal = s.get(var);
				boolean changed = false;
				List<TransitionProb> transitions = model.transitions(s, a);
				for(TransitionProb tp : transitions){
					if(tp.p > 0) {
						Object spVal = tp.eo.op.get(var);
						if(!sVal.equals(spVal))
							changed = true;
					}
				}

				if(changed){
					changedVariables[i].add(var.toString());
					VariableTree varTree = decisions.get(action).get(var.toString());
					List<String> chexked = varTree.getCheckedVariables(s);
					checkedVariables[i].addAll(chexked);
				}
			}
		}

		//created edges - a changes x, b checks x, and x is not changed by action in between
		for(int i = 0; i < actions.size() - 1; i++){
			for (String var : changedVariables[i]){
				int end = i + 1;
				boolean createEdge = true;
				while (!checkedVariables[end].contains(var)){
					if(changedVariables[end].contains(var)){
						createEdge = false;
					}
					end++;
				}
				if(createEdge){
					edges.add(new CausalEdge(i, end, var));
				}
			}
		}
	}
	
	public int findEdge(int start, String variable){
		for(CausalEdge edge : edges){
			if(edge.getStart() == start && edge.getRelavantVariable().equals(variable)){
				return edge.getEnd();
			}
		}
		return -1;
	}
	
	public int actionCount(){
		if(baseTrajectory == null){
			return 0;
		}else{
			return baseTrajectory.actionSequence.size();
		}
	}
	@Override
	public String toString(){
		String out = "";
		if(actions.size() == 0){
			out = "No actiond";
		}else{
			out = "Actions: ";
			for(String a : actions){
				out += a + " ";
			}
			out += "\n";
			
			for(CausalEdge edge : edges){
				out += actions.get(edge.getStart()) + " " +
						actions.get(edge.getEnd()) + " " +
						edge.getRelavantVariable() + "\n";
			}
		}
		return out;
	}
	
	public State getState(int index){
		return baseTrajectory.stateSequence.get(index);
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<CausalEdge> getEdges() {
		return edges;
	}

	public void setEdges(List<CausalEdge> edges) {
		this.edges = edges;
	}

	public Set<String>[] getCheckedVariables() {
		return checkedVariables;
	}

	public void setCheckedVariables(Set<String>[] checkedVariables) {
		this.checkedVariables = checkedVariables;
	}

	public Set<String>[] getChangedVariables() {
		return changedVariables;
	}

	public void setChangedVariables(Set<String>[] changedVariables) {
		this.changedVariables = changedVariables;
	}

	public Episode getBaseTrajectory() {
		return baseTrajectory;
	}

	public void setBaseTrajectory(Episode baseTrajectory) {
		this.baseTrajectory = baseTrajectory;
	}
	
	
	
}
