package com.br.ufu.scheduling.model;

import java.io.BufferedWriter;
import java.util.*;

import com.br.ufu.scheduling.enums.AlgorithmType;
import com.br.ufu.scheduling.exceptions.BetterChromosomeFoundException;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Mutation;
import com.br.ufu.scheduling.utils.Printer;
import com.br.ufu.scheduling.utils.Utils;

public class Chromosome implements Cloneable {

	// Variables
	private int[] mapping; 		//Processors execution order - starts on processor 1
	// Variables
	private int[] scheduling;	//Task execution order - starts at task 1
	// Objectives
	private Metrics metrics = new Metrics();

	public Map<Object, Object> attributes = new HashMap<>();

	public Chromosome(Graph graph) {
		createVectors(graph);
	}

	public Chromosome(Random generator, Graph graph, Configuration config) throws Exception {
		createVectors(graph);
		generateChromosome(generator, graph, config);
	}

	public Chromosome(int[] mapping, int[] scheduling, Graph graph, Configuration config) throws Exception {
		this.mapping = mapping;
		this.scheduling = scheduling;

		calculateMetrics(graph, config);
	}

	private Chromosome() {
	}

	public int[] getMapping() {
		return mapping;
	}

	public int[] getScheduling() {
		return scheduling;
	}

	public double getSLength() {
		return metrics.getSLength();
	}

	public double getLoadBalance() {
		return metrics.getLoadBalance();
	}

	public double getFlowTime() {
		return metrics.getFlowTime();
	}

	public double getCommunicationCost() {
		return metrics.getCommunicationCost();
	}

	public double getWaitingTime() {
		return metrics.getWaitingTime();
	}

	public double getFitness() {
		return metrics.getFitness();
	}

	public int getFitnessAdjusted() {
		return metrics.getFitnessAjusted();
	}

	public double getFitnessForSLength() {
		return metrics.getFitnessForSLength();
	}

	public double getFitnessForLoadBalance() {
		return metrics.getFitnessForLoadBalance();
	}

	public double getFitnessForFlowTime() {
		return metrics.getFitnessForFlowTime();
	}

	public double getFitnessForCommunicationCost() {
		return metrics.getFitnessForCommunicationCost();
	}

	public double getFitnessForWaitingTime() {
		return metrics.getFitnessForWaitingTime();
	}

	//NSGA2 and NSGAIII
    public int getRank() {
        return metrics.getRank();
    }

    public void setRank(int rank) {
        metrics.setRank(rank);
    }

    public double getCrowdingDistance() {
        return metrics.getCrowdingDistance();
    }

    public void setCrowdingDistance(double crowdingDistance) {
        metrics.setCrowdingDistance(crowdingDistance);
    }

    public List<Chromosome> getDominatedChromosomes() {
        return metrics.getDominatedChromosomes();
    }

    public void setDominatedChromosomes(List<Chromosome> dominatedChromosomes) {
        metrics.setDominatedChromosomes(dominatedChromosomes);
    }

    public int getDominatedCount() {
        return metrics.getDominatedCount();
    }

    public void setDominatedCount(int dominationCount) {
        metrics.setDominatedCount(dominationCount);
    }

    public void addDominatedChromosome(Chromosome chromosome) {
        metrics.addDominatedChromosome(chromosome);
    }

    public void incrementDominatedCount(int incrementValue) {
        metrics.incrementDominatedCount(incrementValue);
    }

    public void reset() {
        metrics.reset();
    }

    public List<Double> getNormalizedObjectiveValues() {
        return metrics.getNormalizedObjectiveValues();
    }

    public void setNormalizedObjectiveValue(int index, double value) {
        metrics.setNormalizedObjectiveValue(index, value);
    }
    //FIM NSGA2

    public double getSimpleAverage() {
        return metrics.getSimpleAverage();
    }

    public double getHarmonicAverage() {
        return metrics.getHarmonicAverage();
    }

	public double getValueForSort() {
		return metrics.getValueForSort();
	}

	public void setValueForSort(double valueForSort) {
		metrics.setValueForSort(valueForSort);
	}

	private void createVectors(Graph graph) {
		mapping = new int[graph.getNumberOfVertices()];
		scheduling = new int[graph.getNumberOfVertices()];
	}

	private void generateChromosome(Random generator, Graph graph, Configuration config) throws Exception {
		populateMapping(generator, config);
		populateScheduling(generator, graph);
		calculateMetrics(graph, config);
	}

	private void populateMapping(Random generator, Configuration config) {
		for (int i = 0; i < mapping.length; i++) {
			//Plus 1, because the index of the first processor will be 1 e not 0 
			mapping[i] = generator.nextInt(config.getTotalProcessors()) + 1;
		}
	}

	private void populateScheduling(Random generator, Graph graph) {
		List<String> listOfRaffledEdges = new ArrayList<String>();
		List<Integer> listOfRaffledDestinationNodes = new ArrayList<Integer>();

		List<Integer> listOfSourceNodesForRaffle = new ArrayList<>();
		listOfSourceNodesForRaffle.add(graph.getFirstTask()); //We always begin for the first task

		for (int i = 0; i < scheduling.length; i++) {
			if (i == 0) { //First Task?
				int rootNode = listOfSourceNodesForRaffle.get(i);
				scheduling[i] = rootNode;

				addRaffledDestinationNode(listOfRaffledDestinationNodes, rootNode);

				continue;
			} 

			int raffledDestinationNode, raffledSourceNode;

			do {
				raffledSourceNode = listOfSourceNodesForRaffle.get(generator.nextInt(listOfSourceNodesForRaffle.size()));
				List<Edge> adjacency = graph.getVertex(raffledSourceNode).getAdjacency();
				raffledDestinationNode = adjacency.get(generator.nextInt(adjacency.size())).getDestination().getTask();

			} while(!isFinishedEntry(graph, listOfRaffledDestinationNodes, raffledDestinationNode) 
					|| isEdgeUsed(listOfRaffledEdges, raffledSourceNode, raffledDestinationNode) 
					|| isAlreadyRaffledDestinationNode(listOfRaffledDestinationNodes, raffledDestinationNode));

			scheduling[i] = raffledDestinationNode;

			addRaffledNode(listOfSourceNodesForRaffle, raffledDestinationNode); 
			addRaffledDestinationNode(listOfRaffledDestinationNodes, raffledDestinationNode);

			addRaffledEdges(listOfRaffledEdges, raffledSourceNode, raffledDestinationNode);
			handleInvalidData(graph, listOfRaffledEdges, listOfRaffledDestinationNodes, listOfSourceNodesForRaffle, raffledSourceNode, raffledDestinationNode);
		}
	}

	private boolean isFinishedEntry(Graph graph, List<Integer> listOfRaffledDestinationNodes, int raffledDestinationNode) {
		for (Integer entradaNecessariaParaExecucao : graph.getVertex(raffledDestinationNode).getEntries()) {
			if (!listOfRaffledDestinationNodes.contains(entradaNecessariaParaExecucao)) {
				return false;
			}
		}

		return true;
	}

	private boolean isEdgeUsed(List<String> listOfRaffledEdges, int raffledSourceNode, int raffledDestinationNode) {
		return listOfRaffledEdges.contains(getNameOfEdge(raffledSourceNode, raffledDestinationNode)); 
	}

	//A node can be the destination of only one origin, so if it is already in the tree, it cannot be selected again
	private boolean isAlreadyRaffledDestinationNode(List<Integer> listOfRaffledDestinationNodes, int raffledDestinationNode) {
		return listOfRaffledDestinationNodes.contains(raffledDestinationNode);
	}

	private void addRaffledNode(List<Integer> listOfSourceNodesForRaffle, int raffledDestinationNode) {
		listOfSourceNodesForRaffle.add(raffledDestinationNode);
	}

	private void addRaffledDestinationNode(List<Integer> listOfRaffledDestinationNodes, int raffledDestinationNode) {
		if (!listOfRaffledDestinationNodes.contains(raffledDestinationNode)) {
			listOfRaffledDestinationNodes.add(raffledDestinationNode);
		}
	}

	private void addRaffledEdges(List<String> listOfEdges, int sourceNode, int destinationNode) {
		listOfEdges.add(getNameOfEdge(sourceNode, destinationNode));
	}

	private String getNameOfEdge(int sourceNode, int destinationNode) {
		return sourceNode + "-" + destinationNode;
	}

	private void handleInvalidData(Graph graph, List<String> listOfRaffledEdges, List<Integer> listOfRaffledDestinationNodes, List<Integer> listOfSourceNodesForRaffle, int raffledSourceNode, int raffledDestinationNode) {
		List<Edge> adjagency = graph.getVertex(raffledDestinationNode).getAdjacency();

		for (Edge edje : adjagency) {
			int destinationAdjagencyNode = edje.getDestination().getTask();

			if (destinationAdjagencyNode == raffledSourceNode || (!listOfRaffledDestinationNodes.contains(destinationAdjagencyNode)) && destinationAdjagencyNode != graph.getFirstTask()) {
				continue;
			}

			addRaffledEdges(listOfRaffledEdges, raffledDestinationNode, destinationAdjagencyNode);

			//It could be that destinationAdjagencyNode becomes invalid when we add this new edge, so I should remove it from the raffle list
			handleInvalidNodeForRaffle(graph, listOfRaffledEdges, listOfSourceNodesForRaffle, destinationAdjagencyNode);
		}

		handleInvalidNodeForRaffle(graph, listOfRaffledEdges, listOfSourceNodesForRaffle, raffledSourceNode);
		handleInvalidNodeForRaffle(graph, listOfRaffledEdges, listOfSourceNodesForRaffle, raffledDestinationNode);
	}

	//If the node has already reached all its destinations, it cannot be raffle again, so we remove it from the list of source nodes suitable for raffle
	private void handleInvalidNodeForRaffle(Graph graph, List<String> listOfRaffledEdges, List<Integer> listOfSourceNodesForRaffle, int node) {
		if (isSourceNodeReachedAllDestinations(graph, listOfRaffledEdges, node)) {
			listOfSourceNodesForRaffle.remove(Integer.valueOf(node));
		}
	}

	private boolean isSourceNodeReachedAllDestinations(Graph graph, List<String> listOfRaffledEdges, int sourceNode) {
		return listOfRaffledEdges.stream().filter(nameOfEdge -> nameOfEdge.startsWith(sourceNode + "-")).count() == graph.getVertex(sourceNode).getAdjacency().size();
	}

	public void calculateMetrics(Graph graph, Configuration config) throws Exception {
		try {
			metrics.calculateMetrics(graph, this, config);
		} catch (BetterChromosomeFoundException e) {
			System.out.println("Better Chromosome Found:");
			Printer.printChromosomeVectors(mapping, scheduling);

			BetterChromosomeFoundException e2 = new BetterChromosomeFoundException(e.getMessage());
			e2.initCause(e);
			throw e2;
		}
	}

	public void applyMutation(Random generator, Graph graph, Configuration config) throws Exception {
		mapping = Mutation.applyMutation(generator, mapping, config);
		calculateMetrics(graph, config);
	}

	public void printChromosome(Configuration config, AlgorithmType algorithmType) throws Exception {
		Printer.printChromosome(config, this, algorithmType);
	}

    public void printChromosome(Configuration config, AlgorithmType algorithmType, boolean showFitness) throws Exception {
        Printer.printChromosome(config, this, showFitness, algorithmType);
    }

    public void printChromosome(Configuration config, AlgorithmType algorithmType, boolean showFitness, BufferedWriter finalResultWriter) throws Exception {
        Printer.printChromosome(config, this, showFitness, algorithmType, finalResultWriter);
    }

	public Object clone() throws CloneNotSupportedException {
		Chromosome clone = new Chromosome();
		clone.mapping = this.mapping.clone();
		clone.scheduling = this.scheduling.clone();
		clone.metrics = (Metrics) this.metrics.clone();
		return  clone;
	}

	public double getObjectiveValue(Integer objectiveIndex) {
		double objectiveValue = 0.0;

		switch (objectiveIndex) {
		case Constants.MAKESPAN:
			objectiveValue = getFitnessForSLength();
			break;

		case Constants.LOAD_BALANCE:
			objectiveValue = getFitnessForLoadBalance();
			break;

		case Constants.FLOW_TIME:
			objectiveValue = getFitnessForFlowTime();
			break;

		case Constants.COMMUNICATION_COST:
			objectiveValue = getFitnessForCommunicationCost();
			break;

		case Constants.WAITING_TIME:
			objectiveValue = getFitnessForWaitingTime();
			break;

		default:
			throw new IllegalArgumentException("Type of objective invalid. Value: " + objectiveIndex + ".");
		}

		return objectiveValue;
	}

	public double[] objectives(int numberOfObjectives) {
		switch (numberOfObjectives) {
			case 1:
				return new double[]{
						getSLength()
				};
			case 2:
				return new double[]{
						getSLength(),
						getLoadBalance()
				};
			case 3:
				return new double[]{
						getSLength(),
						getLoadBalance(),
						getFlowTime()
				};
			case 4:
				return new double[]{
						getSLength(),
						getLoadBalance(),
						getFlowTime(),
						getCommunicationCost()
				};
			case 5:
				return new double[]{
						getSLength(),
						getLoadBalance(),
						getFlowTime(),
						getCommunicationCost(),
						getWaitingTime()
				};
			default:
				return new double[]{};
		}
	}

	public Map<Object, Object> attributes() {
		return attributes;
	}

    public double getRealObjectiveValue(Integer objectiveIndex) {
        double objectiveValue = 0.0;

        switch (objectiveIndex) {
        case Constants.MAKESPAN:
            objectiveValue = getSLength();
            break;

        case Constants.LOAD_BALANCE:
            objectiveValue = getLoadBalance();
            break;

        case Constants.FLOW_TIME:
            objectiveValue = getFlowTime();
            break;

        case Constants.COMMUNICATION_COST:
            objectiveValue = getCommunicationCost();
            break;

        case Constants.WAITING_TIME:
            objectiveValue = getWaitingTime();
            break;

        default:
            throw new IllegalArgumentException("Type of objective invalid. Value: " + objectiveIndex + ".");
        }

        return objectiveValue;
    }

	/**
	 * Rule:
	 *   B dominates A or A is dominated by B if:
	 *     1: B >= A in all objectives;
	 *     2: B > A in at least one objective
	 * 
	 * */
	public boolean isChromosomeDominated(Configuration config, Chromosome chromosomeB) {
		boolean isChromosomeDominated = false;

		for (int objectiveIndex = 1; objectiveIndex <= config.getTotalObjectives(); objectiveIndex++) {
			int result = Double.compare(chromosomeB.getObjectiveValue(Utils.getActualObjectiveIndex(config, objectiveIndex)), this.getObjectiveValue(Utils.getActualObjectiveIndex(config, objectiveIndex)));

			if (result > 0) {
				isChromosomeDominated = true;
			} else {
				return false;
			}
		}

		return isChromosomeDominated;
	}

	   /**
     * Rule:
     *   B dominates A or A is dominated by B if:
     *     1: B >= A in all objectives;
     *     2: B > A in at least one objective
     * 
     * */
    public boolean isChromosomeDominated(Configuration config, List<Integer> objectives, Chromosome chromosomeB) {
        boolean isChromosomeDominated = false;

        for (int objective : objectives) {
            int realObjective = Utils.getActualObjectiveIndex(config, objective);
            int result = Double.compare(chromosomeB.getObjectiveValue(realObjective), this.getObjectiveValue(realObjective));

            if (result > 0) {
                isChromosomeDominated = true;
            } else {
                return false;
            }
        }

        return isChromosomeDominated;
    }

	@Override
	public boolean equals(Object obj) {
		Chromosome other = (Chromosome) obj;
		boolean equals = true;

		for (int i = 0; i < other.getMapping().length; i++) {
			equals = mapping[i] == other.mapping[i] && scheduling[i] == other.scheduling[i];

			if (!equals) {
				return equals;
			}
		}

		return equals;
	}

    @Override
    public String toString() {

        StringBuilder response = new StringBuilder("Objective values: [ ");

        response.append(getSLength()).append(" ");
        response.append(getLoadBalance()).append(" ");
        response.append(getFlowTime()).append(" ");
        response.append(getCommunicationCost()).append(" ");
        response.append(getWaitingTime()).append(" ");

        response
            .append("] | Rank: ")
            .append(getRank())
            .append(" | Crowding Distance: ")
            .append(getCrowdingDistance());

        return response.toString();
    }

	public static void main(String args[]) throws Exception {
		Configuration config = new Configuration();

		Graph graph = Graph.initializeGraph();
		Chromosome chromosome = new Chromosome(graph);

		chromosome.mapping[0] = 3; 
		chromosome.mapping[1] = 3;
		chromosome.mapping[2] = 2;
		chromosome.mapping[3] = 1;
		chromosome.mapping[4] = 2;
		chromosome.mapping[5] = 3;
		chromosome.mapping[6] = 1;
		chromosome.mapping[7] = 1;
		chromosome.mapping[8] = 3;

		chromosome.scheduling[0] = 1; 
		chromosome.scheduling[1] = 2;
		chromosome.scheduling[2] = 3;
		chromosome.scheduling[3] = 4;
		chromosome.scheduling[4] = 6;
		chromosome.scheduling[5] = 5;
		chromosome.scheduling[6] = 7;
		chromosome.scheduling[7] = 8;
		chromosome.scheduling[8] = 9;

		chromosome.calculateMetrics(graph, config);
		chromosome.printChromosome(config, config.getAlgorithmType());
	    
	    Random r = new Random();
	    for (int i = 0; i < 10; i ++) {
	        int value = r.nextInt(Integer.MAX_VALUE);
	        System.out.println(i % 2 == 0 ? value : value * (-1));
	    }
	}
}