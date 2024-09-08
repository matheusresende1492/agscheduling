package com.br.ufu.scheduling.utils;

import java.util.List;

import com.br.ufu.scheduling.model.Chromosome;

public class CalculateValueForSort {
    public static double calculate(Chromosome chromosome, Configuration config, List<Integer> objectives) {
        switch (config.getSortFunctionType()) {
        case WEIGHT:
            return calculateAverageByWeight(chromosome, config, objectives);

        case SIMPLE_AVERAGE:
            return calculateAverageBySimpleAverage(chromosome, config, objectives);

        case HARMONIC_AVERAGE:
            return calculateAverageByHarmonicAverage(chromosome, config, objectives);

        default:
            throw new IllegalArgumentException("Sort Function type not implemented.");
        }
    }

    public static double calculateAverageBySimpleAverage(Chromosome chromosome, Configuration config) {
        double accumulatedValue = 0.0;

        for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
            accumulatedValue += getValueForObjectiveBySimpleAverage(chromosome, config, objective); 
        }

        return accumulatedValue / config.getTotalObjectives();
    }

    public static double calculateAverageByHarmonicAverage(Chromosome chromosome, Configuration config) {
        double accumulatedValue = 0.0;

        for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
            accumulatedValue += getValueForObjectiveByHarmonicAverage(chromosome, config, objective); 
        }

        return config.getTotalObjectives() / accumulatedValue;
    }

    private static double calculateAverageByWeight(Chromosome chromosome, Configuration config, List<Integer> objectives) {
        double accumulatedValue = 0.0;

        for (Integer objective : objectives) {
            switch (objective) {
            case 1:
                accumulatedValue += chromosome.getObjectiveValue(config.getObjective1()) * config.getWeight1();
                break;

            case 2:
                accumulatedValue += chromosome.getObjectiveValue(config.getObjective2()) * config.getWeight2();
                break;

            case 3:
                accumulatedValue += chromosome.getObjectiveValue(config.getObjective3()) * config.getWeight3();
                break;

            case 4:
                accumulatedValue += chromosome.getObjectiveValue(config.getObjective4()) * config.getWeight4();
                break;

            case 5:
                accumulatedValue += chromosome.getObjectiveValue(config.getObjective5()) * config.getWeight5();
                break;

            default:
                throw new IllegalArgumentException("Objective invalid. Value: " + objective + ".");
            }
        }

        return accumulatedValue /= objectives.size();
    }

    private static double calculateAverageBySimpleAverage(Chromosome chromosome, Configuration config, List<Integer> objectives) {
        double accumulatedValue = 0.0;

        for (Integer objective : objectives) {
            accumulatedValue += getValueForObjectiveBySimpleAverage(chromosome, config, objective);
        }

        return accumulatedValue / objectives.size();
    }

    private static double getValueForObjectiveBySimpleAverage(Chromosome chromosome, Configuration config, int objective) {
        switch (objective) {
        case 1:
            return getNormalizedObjectiveValue(config, config.getObjective1(), config.getMaxObjectiveValue1(), config.getMinObjectiveValue1(), chromosome);

        case 2:
            return getNormalizedObjectiveValue(config, config.getObjective2(), config.getMaxObjectiveValue2(), config.getMinObjectiveValue2(), chromosome);

        case 3:
            return getNormalizedObjectiveValue(config, config.getObjective3(), config.getMaxObjectiveValue3(), config.getMinObjectiveValue3(), chromosome);

        case 4:
            return getNormalizedObjectiveValue(config, config.getObjective4(), config.getMaxObjectiveValue4(), config.getMinObjectiveValue4(), chromosome);

        case 5:
            return getNormalizedObjectiveValue(config, config.getObjective5(), config.getMaxObjectiveValue5(), config.getMinObjectiveValue5(), chromosome);

        default:
            throw new IllegalArgumentException("Objective invalid. Value: " + objective + ".");
        }
    }

    private static double calculateAverageByHarmonicAverage(Chromosome chromosome, Configuration config, List<Integer> objectives) {
        double accumulatedValue = 0.0;

        for (Integer objective : objectives) {
            accumulatedValue += getValueForObjectiveByHarmonicAverage(chromosome, config, objective);
        }

        return objectives.size() / accumulatedValue;
    }

    private static double getValueForObjectiveByHarmonicAverage(Chromosome chromosome, Configuration config, int objective) {
        switch (objective) {
        case 1:
            return (1 / getNormalizedObjectiveValue(config, config.getObjective1(), config.getMaxObjectiveValue1(), config.getMinObjectiveValue1(), chromosome));

        case 2:
            return (1 / getNormalizedObjectiveValue(config, config.getObjective2(), config.getMaxObjectiveValue2(), config.getMinObjectiveValue2(), chromosome));

        case 3:
            return (1 / getNormalizedObjectiveValue(config, config.getObjective3(), config.getMaxObjectiveValue3(), config.getMinObjectiveValue3(), chromosome));

        case 4:
            return (1 / getNormalizedObjectiveValue(config, config.getObjective4(), config.getMaxObjectiveValue4(), config.getMinObjectiveValue4(), chromosome));

        case 5:
            return (1 / getNormalizedObjectiveValue(config, config.getObjective5(), config.getMaxObjectiveValue5(), config.getMinObjectiveValue5(), chromosome));

        default:
            throw new IllegalArgumentException("Objective invalid. Value: " + objective + ".");
        }
    }

    private static double getNormalizedObjectiveValue(Configuration config, Integer objectiveIndex, double maxObjectiveValue, double minObjectiveValue, Chromosome chromosome) {
        double objectiveValue = 0.0;

        switch (objectiveIndex) {
            case Constants.MAKESPAN:
                objectiveValue = calculateNormalizedObjectiveValue(config, chromosome.getFitnessForSLength(), maxObjectiveValue, minObjectiveValue);
                break;

            case Constants.LOAD_BALANCE:
                objectiveValue = calculateNormalizedObjectiveValue(config, chromosome.getFitnessForLoadBalance(), maxObjectiveValue, minObjectiveValue);
                break;

            case Constants.FLOW_TIME:
                objectiveValue = calculateNormalizedObjectiveValue(config, chromosome.getFitnessForFlowTime(), maxObjectiveValue, minObjectiveValue);
                break;

            case Constants.COMMUNICATION_COST:
                objectiveValue = calculateNormalizedObjectiveValue(config, chromosome.getFitnessForCommunicationCost(), maxObjectiveValue, minObjectiveValue);
                break;

            case Constants.WAITING_TIME:
                objectiveValue = calculateNormalizedObjectiveValue(config, chromosome.getFitnessForWaitingTime(), maxObjectiveValue, minObjectiveValue);
                break;

            default:
                throw new IllegalArgumentException("Type of objective invalid. Value: " + objectiveIndex + ".");
        }

        return objectiveValue;
    }

    private static double calculateNormalizedObjectiveValue(Configuration config, double objectiveValue, double maxObjectiveValue, double minObjectiveValue) {
        double differenceToMinimum = objectiveValue - minObjectiveValue;
        double differenceBetweenExtremes = Math.abs(maxObjectiveValue - minObjectiveValue);

        return differenceBetweenExtremes > 0.0 ? differenceToMinimum / differenceBetweenExtremes : 0.0;
    }

    private static double calculateNormalizedObjectiveValue(double objectiveValue, double maxObjectiveValue, double minObjectiveValue) {
        if (objectiveValue > maxObjectiveValue || objectiveValue < minObjectiveValue) {
            throw new IllegalArgumentException("Invalid objectiveValue: " + objectiveValue + " -> minObjectiveValue: " + minObjectiveValue + " | maxObjectiveValue: " + maxObjectiveValue + ".");
        }

        double differenceToMinimum = objectiveValue - minObjectiveValue;
        double differenceBetweenExtremes = Math.abs(maxObjectiveValue - minObjectiveValue);

        return differenceBetweenExtremes > 0.0 ? differenceToMinimum / differenceBetweenExtremes : 0.0;
    }

    public static void main (String args[]) {
        int maximizationCost = 1;
        
        double SLength = maximizationCost / 100.0;
        double LoadBalance = maximizationCost / 1.114206128;
        //double LoadBalance2 = maximizationCost / 1.184206128;
        //double LoadBalance3 = maximizationCost / 1.094206128;
        double FlowTime = maximizationCost / 2285.0;
        double CommunicationCost = maximizationCost / 848.0;
        double WaitingTime = maximizationCost / 334.0;
        
        System.out.println("Real Values");
        System.out.println(SLength);
        System.out.println(LoadBalance);
        System.out.println(FlowTime);
        System.out.println(CommunicationCost);
        System.out.println(WaitingTime);
        
        double maxSLength = maximizationCost / 342.0;
        double minSLength = maximizationCost / 73.0;
        
        double maxLoadBalance = maximizationCost / 1.804066543;
        double minLoadBalance = maximizationCost / 1.0;
        
        double maxFlowTime = maximizationCost / 8148.0;
        double minFlowTime = maximizationCost / 1894.0;

        double maxCommunicationCost = maximizationCost / 641.0;
        double minCommunicationCost = maximizationCost / 279.0;
        
        double maxWaitingTime = maximizationCost / 2546.0;
        double minWaitingTime = maximizationCost / 251.0;

        System.out.println("\nMax and min: ");
        System.out.println(maxSLength);
        System.out.println(minSLength);
        System.out.println(maxLoadBalance);
        System.out.println(minLoadBalance);
        System.out.println(maxFlowTime);
        System.out.println(minFlowTime);
        System.out.println(maxCommunicationCost);
        System.out.println(minCommunicationCost);
        System.out.println(maxWaitingTime);
        System.out.println(minWaitingTime);
        
        
        double accumulatedSLength = 1 / calculateNormalizedObjectiveValue(SLength, minSLength, maxSLength);
        double accumulatedLoadBalance = 1 / calculateNormalizedObjectiveValue(LoadBalance, minLoadBalance, maxLoadBalance);
        double accumulatedFlowTime = 1 / calculateNormalizedObjectiveValue(FlowTime, minFlowTime, maxFlowTime);
        double accumulatedCommunicationCost = 1 / calculateNormalizedObjectiveValue(CommunicationCost, minCommunicationCost, maxCommunicationCost);
        double accumulatedWaitingTime = 1 / calculateNormalizedObjectiveValue(WaitingTime, minWaitingTime, maxWaitingTime);
        
        System.out.println("\nNormalized");
        System.out.println(accumulatedSLength);
        System.out.println(accumulatedLoadBalance);
        System.out.println(accumulatedFlowTime);
        System.out.println(accumulatedCommunicationCost);
        System.out.println(accumulatedWaitingTime);
        
        double sum = accumulatedSLength + accumulatedLoadBalance + accumulatedFlowTime + accumulatedCommunicationCost + accumulatedWaitingTime;
        
        System.out.println("\nSum");
        System.out.println(sum);
        
        double result = 5 / sum;
        System.out.println("\nFinal Value");
        System.out.println(result);
    }
}