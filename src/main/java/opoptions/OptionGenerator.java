package opoptions;

import burlap.behavior.singleagent.options.Option;

import java.util.Set;

public interface OptionGenerator {

    public Set<Option> generateOptions(OPOTrainer trainer);

}
