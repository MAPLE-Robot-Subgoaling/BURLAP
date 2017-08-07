package opoptions;

import java.util.Set;

import burlap.behavior.singleagent.options.Option;

public interface OptionGenerator {
	
	public Set<Option> generateOptions(OPOTrainer trainer);

}
