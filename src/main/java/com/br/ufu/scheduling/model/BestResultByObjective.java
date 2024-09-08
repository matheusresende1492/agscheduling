package com.br.ufu.scheduling.model;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import com.br.ufu.scheduling.enums.SortFunctionType;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Utils;

public class BestResultByObjective {
    private double bestSlength = Double.MAX_VALUE;
    private double bestLoadBalance = Double.MAX_VALUE;
    private double bestFlowTime = Double.MAX_VALUE;
    private double bestCommunicationCost = Double.MAX_VALUE;
    private double bestWaitingTime = Double.MAX_VALUE;

    private double worstSlength = Double.MIN_VALUE;
    private double worstLoadBalance = Double.MIN_VALUE;
    private double worstFlowTime = Double.MIN_VALUE;
    private double worstCommunicationCost = Double.MIN_VALUE;
    private double worstWaitingTime = Double.MIN_VALUE;

    private double hiperVolume = 0.0;
    private int finalChromosomeCount = 0;
    private double bestSolutionHiperVolume = 0.0;

    private Chromosome bestSolutionBySimpleAverage;
    private Chromosome bestSolutionByHarmonicAverage;

    private Configuration config;

    public BestResultByObjective(Configuration config) {
        this.config = config;
    }

    public double getBestSlength() {
        return bestSlength;
    }

    public double getBestLoadBalance() {
        return bestLoadBalance;
    }

    public double getBestFlowTime() {
        return bestFlowTime;
    }

    public double getBestCommunicationCost() {
        return bestCommunicationCost;
    }

    public double getBestWaitingTime() {
        return bestWaitingTime;
    }

    public double getWorstSlength() {
        return worstSlength;
    }

    public double getWorstLoadBalance() {
        return worstLoadBalance;
    }

    public double getWorstFlowTime() {
        return worstFlowTime;
    }

    public double getWorstCommunicationCost() {
        return worstCommunicationCost;
    }

    public double getWorstWaitingTime() {
        return worstWaitingTime;
    }

    public void setHiperVolume(double hiperVolume) {
        this.hiperVolume = hiperVolume;
    }

    public void setFinalChromosomeCount(int finalChromosomeCount) {
        this.finalChromosomeCount = finalChromosomeCount;
    }

    public void setBestSolutionHiperVolume(double bestSolutionHiperVolume) {
        this.bestSolutionHiperVolume = bestSolutionHiperVolume;
    }

    public Chromosome getBestSolutionBySimpleAverage() {
        return bestSolutionBySimpleAverage;
    }

    public Chromosome getBestSolutionByHarmonicAverage() {
        return bestSolutionByHarmonicAverage;
    }

    public void verifyBestAndWorstSolutions(Chromosome chromosome) {
        if (config.isPrintBestResultsByObjectives()) {
            List<Chromosome> chromosomeList = new ArrayList<>();
            chromosomeList.add(chromosome);

            verifyBestAndWorstSolutions(chromosomeList);
        }
    }

    public void verifyBestAndWorstSolutions(List<Chromosome> chromosomeList) {
        if (config.isPrintBestResultsByObjectives()) {
            for (Chromosome chromosome : chromosomeList) {
                //Best Solutions
                if (chromosome.getSLength() < bestSlength) {
                    bestSlength = chromosome.getSLength();
                }

                if (chromosome.getLoadBalance() < bestLoadBalance) {
                    bestLoadBalance = chromosome.getLoadBalance();
                }

                if (chromosome.getFlowTime() < bestFlowTime) {
                    bestFlowTime = chromosome.getFlowTime();
                }

                if (chromosome.getCommunicationCost() < bestCommunicationCost) {
                    bestCommunicationCost = chromosome.getCommunicationCost();
                }

                if (chromosome.getWaitingTime() < bestWaitingTime) {
                    bestWaitingTime = chromosome.getWaitingTime();
                }

                //Worst Solutions
                if (chromosome.getSLength() > worstSlength) {
                    worstSlength = chromosome.getSLength();
                }

                if (chromosome.getLoadBalance() > worstLoadBalance) {
                    worstLoadBalance = chromosome.getLoadBalance();
                }

                if (chromosome.getFlowTime() > worstFlowTime) {
                    worstFlowTime = chromosome.getFlowTime();
                }

                if (chromosome.getCommunicationCost() > worstCommunicationCost) {
                    worstCommunicationCost = chromosome.getCommunicationCost();
                }

                if (chromosome.getWaitingTime() > worstWaitingTime) {
                    worstWaitingTime = chromosome.getWaitingTime();
                }
                
                //Best Non-Dominated Chromosome
                if (bestSolutionBySimpleAverage == null || bestSolutionBySimpleAverage.getSimpleAverage() < chromosome.getSimpleAverage()) {
                    bestSolutionBySimpleAverage = chromosome;
                }

                if (bestSolutionByHarmonicAverage == null || bestSolutionByHarmonicAverage.getHarmonicAverage() < chromosome.getHarmonicAverage()) {
                    bestSolutionByHarmonicAverage = chromosome;
                }
            }
        }
    }

    public void showResult() throws Exception {
        showResult(null);
    }

    public void showResult(BufferedWriter finalResultWriter) throws Exception {
        if (config.isPrintBestResultsByObjectives()) {
            Utils.print("", finalResultWriter);
            Utils.print("##################################", finalResultWriter);
            Utils.print("## Best And Worst Results By Objectives ##", finalResultWriter);
            Utils.print("", finalResultWriter);

            for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
                int realObjective = Utils.getActualObjectiveIndex(config, objective);

                switch (realObjective) {
                    case Constants.MAKESPAN:
                        Utils.print("Best SLength: " + bestSlength, finalResultWriter);
                        break;

                    case Constants.LOAD_BALANCE:
                        Utils.print("Best LoadBalance: " + bestLoadBalance, finalResultWriter);
                        break;

                    case Constants.FLOW_TIME:
                        Utils.print("Best FlowTime: " + bestFlowTime, finalResultWriter);
                        break;

                    case Constants.COMMUNICATION_COST:
                        Utils.print("Best CommunicationCost: " + bestCommunicationCost, finalResultWriter);
                        break;

                    case Constants.WAITING_TIME:
                        Utils.print("Best WaitingTime: " + bestWaitingTime, finalResultWriter);
                        break;

                    default:
                        throw new IllegalArgumentException("Objective invalid. Value: " + objective + ".");
                }
            }
            Utils.print("", finalResultWriter);

            for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
                int realObjective = Utils.getActualObjectiveIndex(config, objective);

                switch (realObjective) {
                    case Constants.MAKESPAN:
                        Utils.print("Worst SLength: " + worstSlength, finalResultWriter);
                        break;

                    case Constants.LOAD_BALANCE:
                        Utils.print("Worst LoadBalance: " + worstLoadBalance, finalResultWriter);
                        break;

                    case Constants.FLOW_TIME:
                        Utils.print("Worst FlowTime: " + worstFlowTime, finalResultWriter);
                        break;

                    case Constants.COMMUNICATION_COST:
                        Utils.print("Worst CommunicationCost: " + worstCommunicationCost, finalResultWriter);
                        break;

                    case Constants.WAITING_TIME:
                        Utils.print("Worst WaitingTime: " + worstWaitingTime, finalResultWriter);
                        break;

                    default:
                        throw new IllegalArgumentException("Objective invalid. Value: " + objective + ".");
                }
            }

            Utils.print("", finalResultWriter);
            Utils.print("Hiper Volume: " + hiperVolume, finalResultWriter);
            Utils.print("", finalResultWriter);
            
            if (config.isPrintHiperVolumeInConsole()) {
                System.out.print(config.getTotalObjectives() + ",");
                System.out.print(Constants.USE_DEFAULT_GRAPH.equals(config.getTaskGraphFileName()) ? "Graph_Omara_Arafa" : config.getTaskGraphFileName() + ",");
                System.out.print(config.getTotalProcessors() + ",");
                System.out.print(config.getSeed() + ",");
                System.out.print(Utils.getAlgorithmName(config) + (Utils.getAlgorithmName(config) == Constants.ALGORITHM_AEMMT ? " - " + Utils.getSortFunction(config) : "") + ",");
                System.out.print(hiperVolume + ",");
                System.out.print(finalChromosomeCount + ",");
                System.out.print(bestSolutionHiperVolume + "\n");
            }
            
            Utils.print("Graph: " + (Constants.USE_DEFAULT_GRAPH.equals(config.getTaskGraphFileName()) ? "Graph_Omara_Arafa" : config.getTaskGraphFileName()), finalResultWriter);
            Utils.print("Processors: " + config.getTotalProcessors(), finalResultWriter);
            Utils.print("With CommunicationCost: " + (Constants.USE_DEFAULT_GRAPH.equals(config.getTaskGraphFileName()) ? true : config.isGraphWithCommunicationCost()), finalResultWriter);
            Utils.print("Seed: " + config.getSeed(), finalResultWriter);
            Utils.print("Algorithm: " + Utils.getAlgorithmName(config) + (Utils.getAlgorithmName(config) == Constants.ALGORITHM_AEMMT ? " - " + Utils.getSortFunction(config) : ""), finalResultWriter);

            Utils.print("", finalResultWriter);
            Utils.print("##################################", finalResultWriter);
            Utils.print("## Best Non-Dominated Chromosome ##", finalResultWriter);
            Utils.print("", finalResultWriter);

            if (Utils.getAlgorithmName(config) == Constants.ALGORITHM_AEMMT) {
                if (config.getSortFunctionType() == SortFunctionType.SIMPLE_AVERAGE) {
                    Utils.print("## Simple Average ##", finalResultWriter);
                    bestSolutionBySimpleAverage.printChromosome(config, config.getAlgorithmType(), false, finalResultWriter);

                } else if (config.getSortFunctionType() == SortFunctionType.HARMONIC_AVERAGE) {
                    Utils.print("## Harmonic Average ##", finalResultWriter);
                    bestSolutionByHarmonicAverage.printChromosome(config, config.getAlgorithmType(), false, finalResultWriter);
                }
            } else {
                Utils.print("## Simple Average ##", finalResultWriter);
                bestSolutionBySimpleAverage.printChromosome(config, config.getAlgorithmType(), false, finalResultWriter);

                Utils.print("", finalResultWriter);

                Utils.print("## Harmonic Average ##", finalResultWriter);
                bestSolutionByHarmonicAverage.printChromosome(config, config.getAlgorithmType(), false, finalResultWriter);
            }
        }        
    }    
}
