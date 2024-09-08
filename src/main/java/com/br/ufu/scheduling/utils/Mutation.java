package com.br.ufu.scheduling.utils;

import java.util.Arrays;
import java.util.Random;

public class Mutation {
	public static int[] applyMutation(Random generator, int[] orginalMapping, Configuration config) {
		switch (config.getMutationType()) {
		case ONE_POINT:
			return applyOnePointMutation(generator, orginalMapping, config.getTotalProcessors());

		case TWO_POINTS:
			return applyTwoPointsMutation(generator, orginalMapping, config.getTotalProcessors());

		default:
			throw new IllegalArgumentException("Crossover type not implemented.");
		}
	}

	private static int[] applyOnePointMutation(Random generator, int[] orginalMapping, int totalProcessors) {
		int[] newMapping = new int[orginalMapping.length];
		newMapping = Arrays.copyOf(orginalMapping, orginalMapping.length);

		int indexRaffled = generator.nextInt(orginalMapping.length);
		int processor = orginalMapping[indexRaffled];

		int processorForChange = 0;

		do {
			//Add 1, because the index of the first processor will be 1 and not 0
			processorForChange = generator.nextInt(totalProcessors) + 1;
		} while (processor == processorForChange);

		newMapping[indexRaffled] = processorForChange;

		return newMapping;
	}
	
	private static int[] applyTwoPointsMutation(Random generator, int[] orginalMapping, int totalProcessors) {
		int[] newMapping = new int[orginalMapping.length];
		newMapping = Arrays.copyOf(orginalMapping, orginalMapping.length);

		int indexRaffled1 = generator.nextInt(orginalMapping.length);
		int indexRaffled2 = 0;
		
		do {
			indexRaffled2 = generator.nextInt(orginalMapping.length);
		} while (indexRaffled1 == indexRaffled2); //It's not permited raffle the same index twice
		
		//If the processors of the two raffled positions are different, I switch one for the other. 
		//If they are the same, I choose one of them at random and rafflew a new processor for the selected position.
		if (newMapping[indexRaffled1] != newMapping[indexRaffled2]) {
			int auxiliaryVariable = newMapping[indexRaffled1];

			newMapping[indexRaffled1] = newMapping[indexRaffled2];
			newMapping[indexRaffled2] = auxiliaryVariable;
		} else {
			int indexMaintained = getIndexMaintained(generator, indexRaffled1, indexRaffled2);
			int newRaffledIndex = 0;

			do {
				newRaffledIndex = generator.nextInt(orginalMapping.length);
			} while (newMapping[indexMaintained] == newMapping[newRaffledIndex]);

			int auxiliaryVariable = newMapping[indexMaintained];

			newMapping[indexMaintained] = newMapping[newRaffledIndex];
			newMapping[newRaffledIndex] = auxiliaryVariable;
		}

		return newMapping;
	}

	private static int getIndexMaintained(Random generator, int indexRaffled1, int indexRaffled2) {
		return generator.nextInt(2) == 0 ? indexRaffled1 : indexRaffled2;
	}
}
