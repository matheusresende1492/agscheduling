package com.br.ufu.scheduling.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.br.ufu.scheduling.enums.MetricType;
import com.br.ufu.scheduling.exceptions.BetterChromosomeFoundException;
import com.br.ufu.scheduling.utils.CalculateValueForSort;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Printer;

public class Metrics implements Cloneable {
	private double sLength; /* makespan */
	private double loadBalance;
	private double flowTime; /* sum of processor times */
	private double communicationCost;
	private double waitingTime;

	private double fitness;
	private double fitnessForSlength;
	private double fitnessForLoadBalance;
	private double fitnessForFlowTime;
	private double fitnessForCommunicationCost;
	private double fitnessForWaitingTime;

	//NSGA2
	private int rank = -1;
	private double crowdingDistance = 0;
	private List<Chromosome> dominatedChromosomes = new ArrayList<>();
    private int dominatedCount = 0;
    private final List<Double> normalizedObjectiveValues = new ArrayList<>();

	private double simpleAvegare;
	private double harmonicAverage;
	private double valueForSort;

	public Metrics() {
	}

	public double getSLength() {
		return sLength;
	}

	public double getLoadBalance() {
		return loadBalance;
	}

	public double getFlowTime() {
		return flowTime;
	}

	public double getCommunicationCost() {
		return communicationCost;
	}

	public double getWaitingTime() {
		return waitingTime;
	}

	public double getFitness() {
		return fitness;
	}

	public int getFitnessAjusted() {
		return Long.valueOf(Math.round(Math.abs(fitness) * Constants.ADJUST_VALUE_FOR_FITNESS_IN_ROULLETE)).intValue();
	}

	public double getFitnessForSLength() {
		return fitnessForSlength;
	}

	public double getFitnessForLoadBalance() {
		return fitnessForLoadBalance;
	}

	public double getFitnessForFlowTime() {
		return fitnessForFlowTime;
	}

	public double getFitnessForCommunicationCost() {
		return fitnessForCommunicationCost;
	}

	public double getFitnessForWaitingTime() {
		return fitnessForWaitingTime;
	}

	//NSGA2
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public List<Chromosome> getDominatedChromosomes() {
        return dominatedChromosomes;
    }

    public void setDominatedChromosomes(List<Chromosome> dominatedChromosomes) {
        this.dominatedChromosomes = dominatedChromosomes;
    }

    public int getDominatedCount() {
        return dominatedCount;
    }

    public void setDominatedCount(int dominationCount) {
        this.dominatedCount = dominationCount;
    }

    public void addDominatedChromosome(Chromosome chromosome) {
        dominatedChromosomes.add(chromosome);
    }

    public void incrementDominatedCount(int incrementValue) {
        dominatedCount += incrementValue;
    }

    public void reset() {
        dominatedCount = 0;
        rank = -1;
        dominatedChromosomes = new ArrayList<>();
    }

    public List<Double> getNormalizedObjectiveValues() {
        return normalizedObjectiveValues;
    }

    public void setNormalizedObjectiveValue(int index, double value) {
        if (getNormalizedObjectiveValues().size() <= index) {
            this.normalizedObjectiveValues.add(index, value);
        } else {
            this.normalizedObjectiveValues.set(index, value);
        }
    }
    //FIM NSGA2

    public double getSimpleAverage() {
        return simpleAvegare;
    }

    public double getHarmonicAverage() {
        return harmonicAverage;
    }

	public double getValueForSort() {
		return valueForSort;
	}

	public void setValueForSort(double valueForSort) {
		this.valueForSort = valueForSort;
	}

	public void calculateMetrics(Graph graph, Chromosome chromosome, Configuration config) throws Exception {
		//To facilitate the calculation, we will not work with zero index for the auxiliary vectors created
		int [] startTimeTask = new int[graph.getNumberOfVertices() + 1]; 
		int [] finalTimeTask = new int[graph.getNumberOfVertices() + 1];
		int [] readinessTime = new int[config.getTotalProcessors() + 1]; 

		for (int taskIndex = 1; taskIndex <= graph.getNumberOfVertices(); taskIndex++) {
			//Need to subtract one because the scheduling/mapping vector starts from index 0
			int task = chromosome.getScheduling()[taskIndex - 1];
			int processor = chromosome.getMapping()[task - 1];

			startTimeTask[task] = Integer.max(readinessTime[processor], dat(graph, finalTimeTask, task, processor, chromosome.getMapping()));
			finalTimeTask[task] = startTimeTask[task] + graph.getVertex(task).getComputationalCost();
			readinessTime[processor] = finalTimeTask[task];

			if (config.isTestMode()) {
				Printer.printExecutionOrder(startTimeTask, finalTimeTask, readinessTime, task, config.getTotalProcessors());
			}
		}

		calculateSLenght(finalTimeTask, config);
		calculateLoadBalance(readinessTime, config);
		calculateFlowTime(finalTimeTask, config);
		validateCommunicationCost(config);
		calculateWaitingTime(startTimeTask, finalTimeTask, graph, chromosome, config);
		calculateFitnessForMetrics(config);
		calculateFitness(config);
		calculateAverages(chromosome, config);
	}

    private int dat(Graph graph, int [] finalTimeTask, int task, int processor, int[] mapping) {
		int max = 0;
		List<Integer> entries = graph.getVertex(task).getEntries();

		if (entries != null) {
			for (Integer entry : entries) {
				int resultCost = finalTimeTask[entry];
				int communicationCost = getCommunicationCost(graph, task, processor, mapping, entry);

				if (communicationCost > 0) {
					accumulateCommunicationCost(communicationCost);
					resultCost += communicationCost;
				}

				if (max < resultCost) {
					max = resultCost;
				}
			}
		}

		return max;
	}

	private int getCommunicationCost(Graph graph, int task, int processor, int[] mapping, Integer entry) {
		//Need to subtract one because the mapping vector starts from index 0
		int parentTaskProcessor = mapping[entry - 1];
		int communicationCost = 0;

		if (processor != parentTaskProcessor) {
			communicationCost = graph.getVertex(entry)
									.getAdjacency()
									.stream()
									.filter(edge -> edge.getSource().getTask() == entry && edge.getDestination().getTask() == task)
									.iterator()
									.next()
									.getCommunicationCost(); 
		}

		return communicationCost;
	}

	private void accumulateCommunicationCost(int communicationCost) {
		this.communicationCost += communicationCost; 
	}

	private void calculateSLenght(int[] finalTimeTask, Configuration config) {
		sLength = maxValueFromVector(finalTimeTask);
		
		if (config.isConvergenceForTheBestSolution() && sLength < Constants.BEST_SLENGTH) {
			throw new BetterChromosomeFoundException("We found a better chromosome than the last one found. SLength: " + sLength + ".");
		}
	}

	private int maxValueFromVector(int[] finalTimeTask) {
		return Arrays.stream(finalTimeTask)
				.max()
				.getAsInt();
	}

	private void calculateLoadBalance(int[] readinessTime, Configuration config) {
		double avg = (double) Arrays.stream(readinessTime)
								.boxed()
								.collect(Collectors.toList())
								.stream()
								.mapToInt(Integer::intValue)
								.sum() / config.getTotalProcessors(); 
		loadBalance = sLength / avg;
		
		// Rounding off above double number to 9 precision
		loadBalance = Math.round(loadBalance * 1000000000) / 1000000000.0;

		if (config.isConvergenceForTheBestSolution() && loadBalance < Constants.BEST_LOAD_BALANCE) {
			throw new BetterChromosomeFoundException("We found a better chromosome than the last one found. LoadBalance: " + loadBalance + ".");
		}
	}

	private void calculateFlowTime(int[] finalTimeTask, Configuration config) {
		flowTime = Arrays.stream(finalTimeTask)
					.boxed()
					.collect(Collectors.toList())
					.stream()
					.mapToInt(Integer::intValue)
					.sum();

		if (config.isConvergenceForTheBestSolution() && flowTime < Constants.BEST_FLOW_TIME) {
			throw new BetterChromosomeFoundException("We found a better chromosome than the last one found. FlowTime: " + flowTime + ".");
		}		
	}

	private void validateCommunicationCost(Configuration config) {
		if (config.isConvergenceForTheBestSolution() && communicationCost < Constants.BEST_COMMUNICATION_COST) {
			throw new BetterChromosomeFoundException("We found a better chromosome than the last one found. CommunicationCost: " + communicationCost + ".");
		}
	}

	private void calculateWaitingTime(int [] startTimeTask, int [] finalTimeTask, Graph graph, Chromosome chromosome, Configuration config) {
		waitingTime = 0.0;

		for (int taskIndex = 1; taskIndex <= graph.getNumberOfVertices(); taskIndex++) {
			//Need to subtract one because the scheduling/mapping vector starts from index 0
			int task = chromosome.getScheduling()[taskIndex - 1];
			List<Integer> entries = graph.getVertex(task).getEntries();

			if (entries != null) {
				int maxRuntimePredecessors = 0;

				for (Integer entry : entries) {
					if (maxRuntimePredecessors < finalTimeTask[entry]) {
						maxRuntimePredecessors = finalTimeTask[entry];
					}
				}

				if (startTimeTask[task] - maxRuntimePredecessors > 0) {
					waitingTime += startTimeTask[task] - maxRuntimePredecessors;
				}
			}
		}

		if (config.isConvergenceForTheBestSolution() && waitingTime < Constants.BEST_WAITING_TIME) {
			throw new BetterChromosomeFoundException("We found a better chromosome than the last one found. WaitingTime: " + waitingTime + ".");
		}
	}

	private void calculateFitnessForMetrics(Configuration config) {
		fitnessForSlength = calculateFitnessForMetric(config, MetricType.MAKESPAN);
		fitnessForLoadBalance = calculateFitnessForMetric(config, MetricType.LOAD_BALANCE);
		fitnessForFlowTime = calculateFitnessForMetric(config, MetricType.FLOW_TIME);
		fitnessForCommunicationCost = calculateFitnessForMetric(config, MetricType.COMMUNICATION_COST);
		fitnessForWaitingTime = calculateFitnessForMetric(config, MetricType.WAITING_TIME);
	}

	private Double calculateFitnessForMetric(Configuration config, MetricType metricType) {
	    return config.getTransformedObjectiveValue(getMetricValue(metricType));
	}

	private void calculateFitness(Configuration config) {
		switch (config.getMetricType()) {
		case MAKESPAN:
			fitness = fitnessForSlength;
			break;

		case LOAD_BALANCE:
			fitness = fitnessForLoadBalance;
			break;

		case FLOW_TIME:
			fitness = fitnessForFlowTime;
			break;

		case COMMUNICATION_COST:
			fitness = fitnessForCommunicationCost;
			break;

		case WAITING_TIME:
			fitness = fitnessForWaitingTime;
			break;

		default:
			throw new IllegalArgumentException("Metric type not implemented.");
		}
	}

	private Double getMetricValue(MetricType metricType) {
		switch (metricType) {
		case MAKESPAN:
			return sLength;

		case LOAD_BALANCE:
			return loadBalance;

		case FLOW_TIME:
			return flowTime;

		case COMMUNICATION_COST:
			return communicationCost;

		case WAITING_TIME:
			return waitingTime;
		default:
			throw new IllegalArgumentException("Metric type not implemented.");
		}
	}

    private void calculateAverages(Chromosome chromosome, Configuration config) {
        simpleAvegare = CalculateValueForSort.calculateAverageBySimpleAverage(chromosome, config);
        harmonicAverage = CalculateValueForSort.calculateAverageByHarmonicAverage(chromosome, config);
    }

	public Object clone() throws CloneNotSupportedException {
		Metrics clone = new Metrics();
		clone.sLength = this.sLength;
		clone.loadBalance = this.loadBalance;
		clone.flowTime = this.flowTime;
		clone.communicationCost = this.communicationCost;
		clone.waitingTime = this.waitingTime;
		clone.fitness = this.fitness;
		clone.fitnessForSlength = this.fitnessForSlength;
		clone.fitnessForLoadBalance = this.fitnessForLoadBalance;
		clone.fitnessForFlowTime = this.fitnessForFlowTime;
		clone.fitnessForCommunicationCost = this.fitnessForCommunicationCost;
		clone.fitnessForWaitingTime = this.fitnessForWaitingTime;
		clone.simpleAvegare = this.simpleAvegare;
		clone.harmonicAverage = this.harmonicAverage;
		clone.valueForSort = this.valueForSort;
		return clone;
	}

	//Not Finished!!!
	//idle = ocioso
	/*private void testSlots(Graph graph, int[] mapping, int[] scheduling, Integer totalProcessors, boolean testMode) {
		//To facilitate the calculation, we will not work with zero index for the auxiliary vectors created
		int [] startTimeTask = new int[graph.getNumberOfVertices() + 1]; 
		int [] finalTimeTask = new int[graph.getNumberOfVertices() + 1];
		int [] readinessTime = new int[totalProcessors + 1]; 

		int s_idle_time = 0;
		int e_idle_time = 0;

		int st_time = 0;
		List<Integer> idle_time = new ArrayList<>();

		for (int taskIndex = 1; taskIndex <= graph.getNumberOfVertices(); taskIndex++) {
			int min_ST = Integer.MAX_VALUE;
			int this_ST = 0;

			for (int processor = 1; processor <= totalProcessors; processor++) {
				//Need to subtract one because the mapping vector starts from index 0
				if (mapping[taskIndex - 1] == processor) {
					startTimeTask[taskIndex] = Integer.max(readinessTime[processor], dat(graph, finalTimeTask, taskIndex, processor, mapping));
					finalTimeTask[taskIndex] = startTimeTask[taskIndex] + graph.getVertex(taskIndex).getComputationalCost();
					readinessTime[processor] = finalTimeTask[taskIndex];

					this_ST = startTimeTask[taskIndex]; 

					if (this_ST < min_ST) {
						min_ST = this_ST; 
					}
				}

				//I cannot understand when this list is fulfilled. The article is not clear about this.
				if (!idle_time.isEmpty()) {
					for (int time_slot : idle_time) {
						if ((e_idle_time - s_idle_time) >= graph.getVertex(taskIndex).getComputationalCost()
								&& dat(graph, finalTimeTask, taskIndex, processor, mapping) < s_idle_time) {
							
						}
					}
				}
			}

			if (st_time < min_ST) {
				min_ST = st_time;
			}
		}
	}*/
}