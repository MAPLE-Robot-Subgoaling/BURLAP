package cleanup;

import org.apache.commons.lang3.StringUtils;

import burlap.mdp.core.oo.propositional.PropositionalFunction;

public class CleanupGoalDescription {
	
	String[] objects;
	PropositionalFunction pf;
	
	public CleanupGoalDescription(String[] objects, PropositionalFunction pf) {
		this.objects = objects;
		this.pf = pf;
	}
	
	public String toString() {
		return "" + pf.getName() + "<" + StringUtils.join(objects, "|") + ">";
	}
}
