package com.br.ufu.scheduling.utils;

import java.io.BufferedWriter;

import com.br.ufu.scheduling.enums.AlgorithmType;
import com.br.ufu.scheduling.enums.SortFunctionType;

public class Utils {
    public static String getSortFunction(Configuration config) {
        if (config.getAlgorithmType() != AlgorithmType.AEMMT) {
            return "Not applicable";
        }

        switch (config.getSortFunctionType()) {
            case WEIGHT:
                return "Weight";

            case SIMPLE_AVERAGE:
                return "Simple Average";

            case HARMONIC_AVERAGE:
                return "Harmonic Average";

            default:
                throw new IllegalArgumentException("SortFunction invalid!");
        }
    }

    public static String getSortFunctionInPortuguese(Configuration config) {
        if (config.getAlgorithmType() != AlgorithmType.AEMMT) {
            return "Nao se aplica";
        }

        switch (config.getSortFunctionType()) {
            case WEIGHT:
                return "Peso";

            case SIMPLE_AVERAGE:
                return "Media Simples";

            case HARMONIC_AVERAGE:
                return "Media Harmonica";

            default:
                throw new IllegalArgumentException("SortFunction invalid!");
        }
    }

    public static SortFunctionType getTypeOfSortFunction(String sortFunction) {
        if (sortFunction == null) {
            return null;
        }

        switch (sortFunction) {
            case "N�o se aplica":
                return null;

            case "Peso":
                return SortFunctionType.WEIGHT;

            case "Media Simples":
                return SortFunctionType.SIMPLE_AVERAGE;

            case "Media Harmonica":
                return SortFunctionType.HARMONIC_AVERAGE;

            default:
                throw new IllegalArgumentException("SortFunction invalid: " + sortFunction + ".");
        }
    }

    public static String getAlgorithmName(Configuration config) {
        return getAlgorithmName(config.getAlgorithmType());
    }

    public static String getAlgorithmName(AlgorithmType algorithmType) {
        switch (algorithmType) {
            case SINGLE_OBJECTIVE:
                return "SingleObjective";

            case NSGAII:
                return "NSGAII";

            case SPEAII:
                return "SPEAII";

            case AEMMT:
                return "AEMMT";

            case AEMMD:
                return "AEMMD";

            case NSGAIII:
                return "NSGAIII";

            default:
                throw new IllegalArgumentException("Algorithm invalid!");
        }
    }

    public static String getObjectiveName(Integer objectiveIndex) {
        String objectiveName = "";

        switch (objectiveIndex) {
            case Constants.MAKESPAN:
                objectiveName = "SLength";
                break;

            case Constants.LOAD_BALANCE:
                objectiveName = "LoadBalance";
                break;

            case Constants.FLOW_TIME:
                objectiveName = "FlowTime";
                break;

            case Constants.COMMUNICATION_COST:
                objectiveName = "CommunicationCost";
                break;

            case Constants.WAITING_TIME:
                objectiveName = "WaitingTime";
                break;

            default:
                throw new IllegalArgumentException("Type of objective invalid. Value: " + objectiveIndex + ".");
        }

        return objectiveName;
    }

    public static Integer getActualObjectiveIndex(Configuration config, Integer objectiveIndex) {
        Integer actualObjectiveIndex = 0;

        switch (objectiveIndex) {
            case 1:
                actualObjectiveIndex = config.getObjective1();
                break;

            case 2:
                actualObjectiveIndex = config.getObjective2();
                break;

            case 3:
                actualObjectiveIndex = config.getObjective3();
                break;

            case 4:
                actualObjectiveIndex = config.getObjective4();
                break;

            case 5:
                actualObjectiveIndex = config.getObjective5();
                break;

            default:
                throw new IllegalArgumentException("Objective invalid. Value: " + objectiveIndex + ".");
        }

        return actualObjectiveIndex;
    }

    public static void print(String message) throws Exception {
        print(message, null);
    }

    public static void print(String message, BufferedWriter finalResultWriter) throws Exception {
        if (finalResultWriter != null) {
            finalResultWriter.write(message);
            finalResultWriter.newLine();
        } else {
            System.out.println(message);
        }
    }
}
