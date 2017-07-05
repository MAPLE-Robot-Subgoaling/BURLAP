package burlap.behavior.singleagent.opoptions;


import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.propositional.PropositionalFunction;

public class OPOGoalPF extends PropositionalFunction {

	public OPOGoalPF(String name, String[] parameterClasses, String[] parameterOrderGroup) {
		super(name, parameterClasses, parameterOrderGroup);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isTrue(OOState s, String... params) {
		throw new RuntimeException("not implemented");
	}

}
