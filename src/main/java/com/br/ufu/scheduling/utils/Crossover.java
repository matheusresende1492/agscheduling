package com.br.ufu.scheduling.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.br.ufu.scheduling.enums.CrossoverType;
import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.Graph;

public class Crossover {
	public static List<Chromosome> getCrossover(Chromosome parent1, Chromosome parent2, Graph graph, Random generator, Configuration config) throws Exception {
		switch (solveCrossoverType(generator)) {
		case CROSSOVER_MAP:
			return getCrossoverMap(parent1, parent2, graph, generator, config);

		case ORDER_CROSSOVER:
			return getOrderCrossover(parent1, parent2, graph, generator, config);

		default:
			throw new IllegalArgumentException("Crossover type not implemented.");
		}
	}

	private static CrossoverType solveCrossoverType(Random generator) {
		return generator.nextDouble() < Constants.RANDOM_NUMBER_FIXED_IN_ARTICLE ? CrossoverType.CROSSOVER_MAP : CrossoverType.ORDER_CROSSOVER;
	}

	public static List<Chromosome> getCrossoverMap(Chromosome parent1, Chromosome parent2, Graph graph, Random generator, Configuration config) throws Exception {
		List<Chromosome> childrenList = new ArrayList<>();
		
		//Add 1 to generate values between 1 and the total of tasks (inclusive)
		int cutPoint = generator.nextInt(graph.getNumberOfVertices()) + 1;

		if (config.isTestMode()) {
			System.out.println("CutPoint: " + cutPoint);
		}

		int [] mappingChild1 = new int[graph.getNumberOfVertices()];
		int [] schedulingChild1 = new int[graph.getNumberOfVertices()];

		int [] mappingChild2 = new int[graph.getNumberOfVertices()];
		int [] schedulingChild2 = new int[graph.getNumberOfVertices()];

		for (int index = 0; index < cutPoint; index++) {
			//The first part of the children's cutpoint is copied from the parents
			mappingChild1[index] = parent1.getMapping()[index];
			mappingChild2[index] = parent2.getMapping()[index];

			//In the scheduling vector there is no change, so we just do the copy
			schedulingChild1[index] = parent1.getScheduling()[index];
			schedulingChild2[index] = parent2.getScheduling()[index];
		}

		for (int index = cutPoint; index < graph.getNumberOfVertices(); index++) {
			//The second part of the children's cutpoint is copied from the parents in reverse: child 1 receives parent 2 data and child 2 receives parent 1 data
			mappingChild1[index] = parent2.getMapping()[index];
			mappingChild2[index] = parent1.getMapping()[index];

			//In the scheduling vector there is no change, so we just do the copy
			schedulingChild1[index] = parent1.getScheduling()[index];
			schedulingChild2[index] = parent2.getScheduling()[index];
		}

		Chromosome children1 = new Chromosome(mappingChild1, schedulingChild1, graph, config);
		childrenList.add(children1);

		Chromosome children2 = new Chromosome(mappingChild2, schedulingChild2, graph, config);
		childrenList.add(children2);

		return childrenList;
	}

	public static List<Chromosome> getOrderCrossover(Chromosome parent1, Chromosome parent2, Graph graph, Random generator, Configuration config) throws Exception {
		List<Chromosome> childrenList = new ArrayList<>();
		
		//Add 1 to generate values between 1 and the total of tasks (inclusive)
		int cutPoint = generator.nextInt(graph.getNumberOfVertices()) + 1;

		if (config.isTestMode()) {
			System.out.println("CutPoint: " + cutPoint);
		}

		int [] mappingChild = new int[graph.getNumberOfVertices()];
		int [] schedulingChild = new int[graph.getNumberOfVertices()];

		List<Integer> taskAlreadyInserted = new ArrayList<>();

		for (int index = 0; index < cutPoint; index++) {
			//Child mapping vector is copied all from parent 1
			mappingChild[index] = parent1.getMapping()[index];

			//The first part of the child's cutpoint is copied from the parent 1
			int task = parent1.getScheduling()[index];
			schedulingChild[index] = task;
			taskAlreadyInserted.add(task);
		}

		//This index is an auxiliary variable to insert the scheduling vector in the same loop as we traverse the data of parent 2, optimizing the process
		int indexInsertionVectorScheduling = cutPoint;
		for (int index = 0; index < graph.getNumberOfVertices(); index++) {
			if (index >= cutPoint) {
				//Copying the part of the mapping vector from parent 1 after the cut point
				mappingChild[index] = parent1.getMapping()[index];
			}

			//The second part of the child's cutpoint is copied from parent 2 in the order in which the tasks appear
			int task = parent2.getScheduling()[index];

			if (!taskAlreadyInserted.contains(task)) {
				schedulingChild[indexInsertionVectorScheduling] = task;
				taskAlreadyInserted.add(task);
				indexInsertionVectorScheduling++;
			}
		}

		Chromosome children = new Chromosome(mappingChild, schedulingChild, graph, config);
		childrenList.add(children);

		return childrenList;
	}
}
