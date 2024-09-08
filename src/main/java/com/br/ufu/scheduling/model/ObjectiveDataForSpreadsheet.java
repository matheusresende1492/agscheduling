package com.br.ufu.scheduling.model;

import com.br.ufu.scheduling.enums.SortFunctionType;
import com.br.ufu.scheduling.utils.Utils;

public class ObjectiveDataForSpreadsheet {
    private String algorithm;
    private String bestValueFounded;
    private String valuePercentageFounded;
    private String sortFunction;
    private int totalChromosomesNonDominated;
    private String runtime;
    private String bestSolutionSimpleAverage;
    private String bestSolutionHarmonicAverage;

    public ObjectiveDataForSpreadsheet() {
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getBestValueFounded() {
        return bestValueFounded;
    }

    public String getValuePercentageFounded() {
        return valuePercentageFounded;
    }

    public String getSortFunction() {
        return sortFunction;
    }

    public int getTotalChromosomesNonDominated() {
        return totalChromosomesNonDominated;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getBestSolutionSimpleAverage() {
        return bestSolutionSimpleAverage;
    }

    public String getBestSolutionHarmonicAverage() {
        return bestSolutionHarmonicAverage;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setBestValueFounded(String bestValueFounded) {
        this.bestValueFounded = bestValueFounded;
    }

    public void setValuePercentageFounded(String valuePercentageFounded) {
        this.valuePercentageFounded = valuePercentageFounded;
    }

    public void setSortFunction(String sortFunction) {
        this.sortFunction = sortFunction;
    }

    public void setTotalChromosomesNonDominated(int totalChromosomesNonDominated) {
        this.totalChromosomesNonDominated = totalChromosomesNonDominated;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public void setBestSolutionSimpleAverage(String bestSolutionSimpleAverage) {
        this.bestSolutionSimpleAverage = bestSolutionSimpleAverage;
    }

    public void setBestSolutionHarmonicAverage(String bestSolutionHarmonicAverage) {
        this.bestSolutionHarmonicAverage = bestSolutionHarmonicAverage;
    }

    @Override
    public String toString() {
        String dataBase = null;
        dataBase = algorithm + "\t" +
                bestValueFounded + "\t" +
                valuePercentageFounded + "\t" +
                (sortFunction != null ? sortFunction + "\t" : "") + 
                totalChromosomesNonDominated + "\t" + 
                runtime + "\t";

        SortFunctionType sortFunctionType = Utils.getTypeOfSortFunction(sortFunction);

        if (sortFunctionType == null) {
            dataBase += bestSolutionSimpleAverage + "\t" +
                    bestSolutionHarmonicAverage + "\t";

        } else if (sortFunctionType == SortFunctionType.SIMPLE_AVERAGE) {
            dataBase += bestSolutionSimpleAverage + "\t";

        } else if (sortFunctionType == SortFunctionType.HARMONIC_AVERAGE) {
            dataBase += bestSolutionHarmonicAverage + "\t";
        }

        return dataBase;
    }
}