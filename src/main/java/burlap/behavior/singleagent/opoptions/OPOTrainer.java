package burlap.behavior.singleagent.opoptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import burlap.debugtools.RandomFactory;
import utils.SimulationConfig;

public abstract class OPOTrainer extends SimulationConfig {
	
	// by default, use 0 (BURLAP default)
	// to use different index, subclasses should override the getIndexForRandomFactory method
	public static final int DEFAULT_RNG_INDEX = 0;

	public abstract String getTrainerName();

	public abstract String getDomainName();

	public String setupSerializationFile(String path, boolean moveFile) {
		String prepend = path;
		String outYaml = prepend + ".yaml";
		System.out.println("looking for serialization file " + prepend + " and out " + outYaml);
		if (moveFile) {
			String moveYaml = prepend + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()).toString() + ".yaml";
			Path filePathSource = Paths.get(outYaml);
			Path filePathDestination = Paths.get(moveYaml);
			try {
				Files.move(filePathSource, filePathDestination);
			} catch (IOException e1) {
				// do nothing
			}
		}
		String serializationFile = outYaml;
		return serializationFile;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
		RandomFactory.seedMapped(getIndexForRandomFactory(), this.seed);
	}

	private int getIndexForRandomFactory() {
		return DEFAULT_RNG_INDEX;
	}

	public abstract void runTraining(String serializationFile);

	public abstract void runEvaluation(String serializationFile);

}
