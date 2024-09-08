package com.br.ufu.scheduling.model;

import java.util.ArrayList;
import java.util.List;

public class DataForSpreadsheet {
    private String tasks;
    private int processors;
    private int seed;
    private String bestValue;
    private String worstValue;
    private String objective;
    private String interval;
    private String bottomLimit;
    private String topLimit;
    private List<ObjectiveDataForSpreadsheet> listObjectivesDataForSpreadsheet = new ArrayList<>();

    public DataForSpreadsheet() {
    }

    public String getTasks() {
        return tasks;
    }

    public int getProcessors() {
        return processors;
    }

    public int getSeed() {
        return seed;
    }

    public String getBestValue() {
        return bestValue;
    }

    public String getWorstValue() {
        return worstValue;
    }

    public String getObjective() {
        return objective;
    }

    public String getInterval() {
        return interval;
    }

    public String getBottomLimit() {
        return bottomLimit;
    }

    public String getTopLimit() {
        return topLimit;
    }

    public List<ObjectiveDataForSpreadsheet> getListObjectivesDataForSpreadsheet() {
        return listObjectivesDataForSpreadsheet;
    }

    public void addObjective(ObjectiveDataForSpreadsheet objectiveDataForSpreadsheet) {
        listObjectivesDataForSpreadsheet.add(objectiveDataForSpreadsheet);
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public void setBestValue(String bestValue) {
        this.bestValue = bestValue;
    }

    public void setWorstValue(String worstValue) {
        this.worstValue = worstValue;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public void setBottomLimit(String bottomLimit) {
        this.bottomLimit = bottomLimit;
    }

    public void setTopLimit(String topLimit) {
        this.topLimit = topLimit;
    }

    public String getKey() {
        return tasks + "-" + processors + "-" + seed + "-" + bestValue + "-" + worstValue + "-" + objective + "-"
                + interval + "-" + bottomLimit + "-" + topLimit;
    }

    @Override
    public String toString() {
        String dataBase = null;
        dataBase = tasks + "\t" +
                processors + "\t" +
                seed + "\t" +
                bestValue + "\t" +
                worstValue + "\t" +
                objective + "\t" +
                interval + "\t" +
                bottomLimit + "\t" +
                topLimit + "\t";

        for (ObjectiveDataForSpreadsheet objectiveDataForSpreadsheet : listObjectivesDataForSpreadsheet) {
            dataBase += objectiveDataForSpreadsheet.toString();
        }

        return dataBase;
    }
}