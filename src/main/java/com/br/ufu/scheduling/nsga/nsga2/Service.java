package com.br.ufu.scheduling.nsga.nsga2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.br.ufu.scheduling.model.Chromosome;

public final class Service {
    private Service() {
    }

    public static boolean populaceHasUnsetRank(List<Chromosome> populace) {
        for (Chromosome chromosome : populace) {
            if (chromosome.getRank() == -1) {
                return true;
            }
        }

        return false;
    }

    public static void normalizeSortedObjectiveValues(List<Chromosome> chromosomeList, int objectiveIndex) {
        double actualMin = chromosomeList.get(0).getObjectiveValue(objectiveIndex);
        double actualMax = chromosomeList.get(chromosomeList.size() - 1).getObjectiveValue(objectiveIndex);

        for (Chromosome chromosome : chromosomeList) {
            chromosome.setNormalizedObjectiveValue(
                    objectiveIndex,
                    Service.minMaxNormalization(
                            chromosome.getObjectiveValue(objectiveIndex),
                            actualMin,
                            actualMax));
        }
    }

    public static double minMaxNormalization(double value,
            double actualMin,
            double actualMax,
            double normalizedMin,
            double normalizedMax) {
        return (((value - actualMin) / (actualMax - actualMin)) * (normalizedMax - normalizedMin)) + normalizedMin;
    }

    public static double minMaxNormalization(double value, double actualMin, double actualMax) {
        return Service.minMaxNormalization(value, actualMin, actualMax, 0, 1);
    }

    public static double roundOff(double value, double decimalPlace) {
        if (value == Double.MAX_VALUE || value == Double.MIN_VALUE) {
            return value;
        }

        decimalPlace = Math.pow(10, decimalPlace);

        return (Math.round(value * decimalPlace) / decimalPlace);
    }

    public static void sortFrontWithCrowdingDistance(List<Chromosome> chomosomeList, int front) {
        int frontStartIndex = -1;
        int frontEndIndex = -1;
        List<Chromosome> frontToSort = new ArrayList<>();

        for (int i = 0; i < chomosomeList.size(); i++) {
            if (chomosomeList.get(i).getRank() == front) {
                frontStartIndex = i;
                break;
            }
        }

        if ((frontStartIndex == -1) || (frontStartIndex == (chomosomeList.size() - 1))
                || (chomosomeList.get(frontStartIndex + 1).getRank() != front)) {
            return;
        }

        for (int i = frontStartIndex + 1; i < chomosomeList.size(); i++) {
            if (chomosomeList.get(i).getRank() != front) {
                frontEndIndex = i - 1;
                break;

            } else if (i == (chomosomeList.size() - 1)) {
                frontEndIndex = i;
            }
        }

        for (int i = frontStartIndex; i <= frontEndIndex; i++) {
            frontToSort.add(chomosomeList.get(i));
        }

        frontToSort.sort(Collections.reverseOrder(Comparator.comparingDouble(Chromosome::getCrowdingDistance)));

        for (int i = frontStartIndex; i <= frontEndIndex; i++) {
            chomosomeList.set(i, frontToSort.get(i - frontStartIndex));
        }
    }
}