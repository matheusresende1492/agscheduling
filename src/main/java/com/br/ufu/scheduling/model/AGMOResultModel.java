package com.br.ufu.scheduling.model;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.br.ufu.scheduling.agmo.Table;
import com.br.ufu.scheduling.enums.AlgorithmType;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Utils;

public class AGMOResultModel {
    private int totalChromossomes;

    private double bestSLength;
    private double bestLoadBalance;
    private double bestFlowTime;
    private double bestCommunicationCost;
    private double bestWaitingTime;

    private double worstSLength;
    private double worstLoadBalance;
    private double worstFlowTime;
    private double worstCommunicationCost;
    private double worstWaitingTime;

    private double diffBetweenMaxAndMinSLength;
    private double diffBetweenMaxAndMinLoadBalance;
    private double diffBetweenMaxAndMinFlowTime;
    private double diffBetweenMaxAndMinCommunicationCost;
    private double diffBetweenMaxAndMinWaitingTime;

    private TotalCalculation totalCalculationSLength = new TotalCalculation();
    private TotalCalculation totalCalculationLoadBalance = new TotalCalculation();
    private TotalCalculation totalCalculationFlowTime = new TotalCalculation();
    private TotalCalculation totalCalculationCommunicationCost = new TotalCalculation();
    private TotalCalculation totalCalculationWaitingTime = new TotalCalculation();

    private BestResultByObjective bestResult;

	public AGMOResultModel(Configuration config, int totalChromossomes) {
	    this.totalChromossomes = totalChromossomes;
	    this.bestResult = new BestResultByObjective(config);

	    handleValueFromObjective(config, Constants.MAKESPAN);
	    handleValueFromObjective(config, Constants.LOAD_BALANCE);
	    handleValueFromObjective(config, Constants.FLOW_TIME);
	    handleValueFromObjective(config, Constants.COMMUNICATION_COST);
	    handleValueFromObjective(config, Constants.WAITING_TIME);
	}

    private void handleValueFromObjective(Configuration config, int objective) {
	    double bestObjectiveValue = 0.0;
	    double worstObjectiveValue = 0.0;

	    //The lower the values, the better the chromosome, so best has the best value.
	    if (config.getObjective1() == objective) {
	        bestObjectiveValue = config.getRealMinObjectiveValue1();
	        worstObjectiveValue = config.getRealMaxObjectiveValue1();

	    } else if (config.getObjective2() == objective) {
            bestObjectiveValue = config.getRealMinObjectiveValue2();
            worstObjectiveValue = config.getRealMaxObjectiveValue2();

	    } else if (config.getObjective3() == objective) {
            bestObjectiveValue = config.getRealMinObjectiveValue3();
            worstObjectiveValue = config.getRealMaxObjectiveValue3();

	    } else if (config.getObjective4() == objective) {
            bestObjectiveValue = config.getRealMinObjectiveValue4();
            worstObjectiveValue = config.getRealMaxObjectiveValue4();

	    } else if (config.getObjective5() == objective) {
            bestObjectiveValue = config.getRealMinObjectiveValue5();
            worstObjectiveValue = config.getRealMaxObjectiveValue5();	        
	    }

	    switch (objective) {
            case Constants.MAKESPAN:
                bestSLength = bestObjectiveValue;
                worstSLength = worstObjectiveValue;
                diffBetweenMaxAndMinSLength = Math.abs(bestSLength - worstSLength) / Constants.SOLUTION_RANGE;
                break;

            case Constants.LOAD_BALANCE:
                bestLoadBalance = bestObjectiveValue;
                worstLoadBalance = worstObjectiveValue;
                diffBetweenMaxAndMinLoadBalance = Math.abs(bestLoadBalance - worstLoadBalance) / Constants.SOLUTION_RANGE;
                break;

            case Constants.FLOW_TIME:
                bestFlowTime = bestObjectiveValue;
                worstFlowTime = worstObjectiveValue;
                diffBetweenMaxAndMinFlowTime = Math.abs(bestFlowTime - worstFlowTime) / Constants.SOLUTION_RANGE;
                break;

            case Constants.COMMUNICATION_COST:
                bestCommunicationCost = bestObjectiveValue;
                worstCommunicationCost = worstObjectiveValue;
                diffBetweenMaxAndMinCommunicationCost = Math.abs(bestCommunicationCost - worstCommunicationCost) / Constants.SOLUTION_RANGE;
                break;

            case Constants.WAITING_TIME:
                bestWaitingTime = bestObjectiveValue;
                worstWaitingTime = worstObjectiveValue;
                diffBetweenMaxAndMinWaitingTime = Math.abs(bestWaitingTime - worstWaitingTime) / Constants.SOLUTION_RANGE;
                break;

            default:
                throw new IllegalArgumentException("Objective invalid.");
        } 
	}

    public void processChromosome(Chromosome chromosome) {
        processSLength(chromosome.getSLength());
        processLoadBalance(chromosome.getLoadBalance());
        processFlowTime(chromosome.getFlowTime());
        processCommunicationCost(chromosome.getCommunicationCost());
        processWaitingTime(chromosome.getWaitingTime());

        bestResult.verifyBestAndWorstSolutions(chromosome);
    }

    private void processSLength(double SLength) {
        putValueOnRangeMap(totalCalculationSLength, processTotalCalculation(SLength, bestSLength, diffBetweenMaxAndMinSLength));
    }

    private void processLoadBalance(double loadBalance) {
        putValueOnRangeMap(totalCalculationLoadBalance, processTotalCalculation(loadBalance, bestLoadBalance, diffBetweenMaxAndMinLoadBalance));
    }

    private void processFlowTime(double flowTime) {
        putValueOnRangeMap(totalCalculationFlowTime, processTotalCalculation(flowTime, bestFlowTime, diffBetweenMaxAndMinFlowTime));
    }

    private void processCommunicationCost(double communicationCost) {
        putValueOnRangeMap(totalCalculationCommunicationCost, processTotalCalculation(communicationCost, bestCommunicationCost, diffBetweenMaxAndMinCommunicationCost));
    }

    private void processWaitingTime(double waitingTime) {
        putValueOnRangeMap(totalCalculationWaitingTime, processTotalCalculation(waitingTime, bestWaitingTime, diffBetweenMaxAndMinWaitingTime));
    }

    private void putValueOnRangeMap(TotalCalculation totalCalculationAccumulated, TotalCalculation totalCalculation) {
        totalCalculationAccumulated.totalBest += totalCalculation.totalBest;
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_90_100, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_90_100) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_90_100));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_80_90, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_80_90) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_80_90));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_70_80, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_70_80) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_70_80));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_60_70, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_60_70) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_60_70));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_50_60, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_50_60) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_50_60));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_40_50, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_40_50) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_40_50));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_30_40, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_30_40) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_30_40));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_20_30, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_20_30) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_20_30));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_10_20, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_10_20) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_10_20));
        totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_0_10, totalCalculationAccumulated.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_0_10) + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_0_10));
    }

    private TotalCalculation processTotalCalculation(double objectiveValue, double best, double diffBetweenMaxAndMin) {
        TotalCalculation calculation = new TotalCalculation();

        if (objectiveValue <= best) {
            calculation.totalBest++;
        }

        if (objectiveValue < (best + diffBetweenMaxAndMin)) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_90_100, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (2 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_80_90, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (3 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_70_80, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (4 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_60_70, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (5 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_50_60, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (6 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_40_50, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (7 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_30_40, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (8 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_20_30, Constants.INCREMENT_RANGE_VALUE);

        } else if (objectiveValue < (best + (9 * diffBetweenMaxAndMin))) {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_10_20, Constants.INCREMENT_RANGE_VALUE);

        } else {
            calculation.mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_0_10, Constants.INCREMENT_RANGE_VALUE);
        }

        return calculation;
    }

    public void showResult(Configuration config, Map<String, DataForSpreadsheet> mapDataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        Utils.print("##################################", finalResultWriter);
        Utils.print("######## General Analysis ########", finalResultWriter);

        for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
            int realObjective = Utils.getActualObjectiveIndex(config, objective);
            
            switch (realObjective) {
                case Constants.MAKESPAN:
                    printObjectiveFormatted(config, "SLength", totalCalculationSLength, bestSLength, worstSLength, diffBetweenMaxAndMinSLength, mapDataForSpreadsheet, finalResultWriter);
                    break;

                case Constants.LOAD_BALANCE:
                    printObjectiveFormatted(config, "LoadBalance", totalCalculationLoadBalance, bestLoadBalance, worstLoadBalance, diffBetweenMaxAndMinLoadBalance, Constants.NINE_DECIMAL_PLACES, mapDataForSpreadsheet, finalResultWriter);
                    break;

                case Constants.FLOW_TIME:
                    printObjectiveFormatted(config, "FlowTime", totalCalculationFlowTime, bestFlowTime, worstFlowTime, diffBetweenMaxAndMinFlowTime, mapDataForSpreadsheet, finalResultWriter);
                    break;

                case Constants.COMMUNICATION_COST:
                    printObjectiveFormatted(config, "CommunicationCost", totalCalculationCommunicationCost, bestCommunicationCost, worstCommunicationCost, diffBetweenMaxAndMinCommunicationCost, mapDataForSpreadsheet, finalResultWriter);
                    break;

                case Constants.WAITING_TIME:
                    printObjectiveFormatted(config, "WaitingTime", totalCalculationWaitingTime, bestWaitingTime, worstWaitingTime, diffBetweenMaxAndMinWaitingTime, mapDataForSpreadsheet, finalResultWriter);
                    break;

                default:
                    throw new IllegalArgumentException("Objective invalid. Value: " + objective + ".");
            }
        }

        bestResult.showResult(finalResultWriter);
    }

    private void printObjectiveFormatted(Configuration config, String objectiveName, TotalCalculation totalCalculation, double bestValue, double worstValue, double diffBetweenMaxAndMin, Map<String, DataForSpreadsheet> mapDataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        printObjectiveFormatted(config, objectiveName, totalCalculation, bestValue, worstValue, diffBetweenMaxAndMin, Constants.TWO_DECIMAL_PLACES, mapDataForSpreadsheet, finalResultWriter);
    }

    private void printObjectiveFormatted(Configuration config, String objectiveName, TotalCalculation totalCalculation, double bestValue, double worstValue, double diffBetweenMaxAndMin, int decimalPlaces, Map<String, DataForSpreadsheet> mapDataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        Utils.print("", finalResultWriter);
        Utils.print("## " + objectiveName + "##", finalResultWriter);

        double percTotalBestObjective = round((totalCalculation.totalBest * 100.0 / totalChromossomes), 2);
        Utils.print("Total Best " + objectiveName + " Founded: " + totalCalculation.totalBest + " -> " + percTotalBestObjective + "%", finalResultWriter);
        Utils.print("", finalResultWriter);

        double percTotalValueBetween90_100 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_90_100) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween80_90 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_80_90) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween70_80 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_70_80) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween60_70 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_60_70) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween50_60 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_50_60) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween40_50 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_40_50) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween30_40 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_30_40) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween20_30 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_20_30) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween10_20 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_10_20) * 100.0 / totalChromossomes), 2);
        double percTotalValueBetween0_10 = round((totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_0_10) * 100.0 / totalChromossomes), 2);

        double bottomLimit90_100 = round(bestValue, decimalPlaces);
        double topLimit90_100 = round(bestValue + diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit80_90 = topLimit90_100;
        double topLimit80_90 = round(bestValue + 2 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit70_80 = topLimit80_90;
        double topLimit70_80 = round(bestValue + 3 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit60_70 = topLimit70_80;
        double topLimit60_70 = round(bestValue + 4 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit50_60 = topLimit60_70;
        double topLimit50_60 = round(bestValue + 5 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit40_50 = topLimit50_60;
        double topLimit40_50 = round(bestValue + 6 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit30_40 = topLimit40_50;
        double topLimit30_40 = round(bestValue + 7 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit20_30 = topLimit30_40;
        double topLimit20_30 = round(bestValue + 8 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit10_20 = topLimit20_30;
        double topLimit10_20 = round(bestValue + 9 * diffBetweenMaxAndMin, decimalPlaces);

        double bottomLimit0_10 = topLimit10_20;
        double topLimit0_10 = round(bestValue + 10 * diffBetweenMaxAndMin, decimalPlaces);

        Utils.print(objectiveName + " Between Min and Max: [" + bestValue + ", " + worstValue + "]", finalResultWriter);
        Utils.print("90-100 [" + bottomLimit90_100 + ", " + topLimit90_100 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_90_100) + " -> " + percTotalValueBetween90_100 + "%", finalResultWriter);
        Utils.print("80-90 [" + bottomLimit80_90 + ", " + topLimit80_90 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_80_90) + " -> " + percTotalValueBetween80_90 + "%", finalResultWriter);
        Utils.print("70-80 [" + bottomLimit70_80 + ", " + topLimit70_80 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_70_80) + " -> " + percTotalValueBetween70_80 + "%", finalResultWriter);
        Utils.print("60-70 [" + bottomLimit60_70 + ", " + topLimit60_70 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_60_70) + " -> " + percTotalValueBetween60_70 + "%", finalResultWriter);
        Utils.print("50-60 [" + bottomLimit50_60 + ", " + topLimit50_60 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_50_60) + " -> " + percTotalValueBetween50_60 + "%", finalResultWriter);
        Utils.print("40-50 [" + bottomLimit40_50 + ", " + topLimit40_50 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_40_50) + " -> " + percTotalValueBetween40_50 + "%", finalResultWriter);
        Utils.print("30-40 [" + bottomLimit30_40 + ", " + topLimit30_40 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_30_40) + " -> " + percTotalValueBetween30_40 + "%", finalResultWriter);
        Utils.print("20-30 [" + bottomLimit20_30 + ", " + topLimit20_30 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_20_30) + " -> " + percTotalValueBetween20_30 + "%", finalResultWriter);
        Utils.print("10-20 [" + bottomLimit10_20 + ", " + topLimit10_20 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_10_20) + " -> " + percTotalValueBetween10_20 + "%", finalResultWriter);
        Utils.print("0-10 [" + bottomLimit0_10 + ", " + topLimit0_10 + "): " + totalCalculation.mapTotalValueBetweenBottonAndTopLimit.get(Constants.RANGE_0_10) + " -> " + percTotalValueBetween0_10 + "%", finalResultWriter);

        Utils.print("", finalResultWriter);

        fillDataForSpreadsheet(config, bestValue, worstValue, "90-100", bottomLimit90_100, topLimit90_100, objectiveName, percTotalValueBetween90_100, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "80-90", bottomLimit80_90, topLimit80_90, objectiveName, percTotalValueBetween80_90, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "70-80", bottomLimit70_80, topLimit70_80, objectiveName, percTotalValueBetween70_80, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "60-70", bottomLimit60_70, topLimit60_70, objectiveName, percTotalValueBetween60_70, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "50-60", bottomLimit50_60, topLimit50_60, objectiveName, percTotalValueBetween50_60, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "40-50", bottomLimit40_50, topLimit40_50, objectiveName, percTotalValueBetween40_50, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "30-40", bottomLimit30_40, topLimit30_40, objectiveName, percTotalValueBetween30_40, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "20-30", bottomLimit20_30, topLimit20_30, objectiveName, percTotalValueBetween20_30, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "10-20", bottomLimit10_20, topLimit10_20, objectiveName, percTotalValueBetween10_20, getBestValue(objectiveName), mapDataForSpreadsheet);
        fillDataForSpreadsheet(config, bestValue, worstValue, "0-10", bottomLimit0_10, topLimit0_10, objectiveName, percTotalValueBetween0_10, getBestValue(objectiveName), mapDataForSpreadsheet);
    }

    private double getBestValue(String objectiveName) {
        switch (objectiveName) {
            case "SLength":
                return bestResult.getBestSlength();

            case "LoadBalance":
                return bestResult.getBestLoadBalance();

            case "FlowTime":
                return bestResult.getBestFlowTime();

            case "CommunicationCost":
                return bestResult.getBestCommunicationCost();

            case "WaitingTime":
                return bestResult.getBestWaitingTime();

            default:
                throw new IllegalArgumentException("Objective invalid!");
        }
    }

    private double getWorstValue(String objectiveName, boolean useWorstValueFromGeneration) {
        if (useWorstValueFromGeneration) {
            switch (objectiveName) {
                case "SLength":
                    return bestResult.getWorstSlength();
    
                case "LoadBalance":
                    return bestResult.getWorstLoadBalance();
    
                case "FlowTime":
                    return bestResult.getWorstFlowTime();
    
                case "CommunicationCost":
                    return bestResult.getWorstCommunicationCost();
    
                case "WaitingTime":
                    return bestResult.getWorstWaitingTime();
    
                default:
                    throw new IllegalArgumentException("Objective invalid!");
            }
        } else {
            switch (objectiveName) {
                case "SLength":
                    return worstSLength;
    
                case "LoadBalance":
                    return worstLoadBalance;
    
                case "FlowTime":
                    return worstFlowTime;
    
                case "CommunicationCost":
                    return worstCommunicationCost;
    
                case "WaitingTime":
                    return worstWaitingTime;
    
                default:
                    throw new IllegalArgumentException("Objective invalid!");
            }
        }
    }

    private String getStringValue(double source) {
        return Double.toString(source).replace(".", ",");
    }

    private void fillDataForSpreadsheet(Configuration config, double bestValue, double worstValue, String interval, double bottomLimit, double topLimit, String objectiveName, double perc, double bestResult, Map<String, DataForSpreadsheet> mapDataForSpreadsheet) {
        DataForSpreadsheet dataForSpreadsheet = new DataForSpreadsheet();
        dataForSpreadsheet.setTasks(config.getTaskGraphFileName());
        dataForSpreadsheet.setProcessors(config.getTotalProcessors());
        dataForSpreadsheet.setSeed(config.getSeed());
        dataForSpreadsheet.setBestValue(getStringValue(bestValue));
        dataForSpreadsheet.setWorstValue(getStringValue(worstValue));
        dataForSpreadsheet.setObjective("SLength".equals(objectiveName) ? "Makespan" : objectiveName);
        dataForSpreadsheet.setInterval(interval);
        dataForSpreadsheet.setBottomLimit(getStringValue(bottomLimit));
        dataForSpreadsheet.setTopLimit(getStringValue(topLimit));

        ObjectiveDataForSpreadsheet objectiveDataForSpreadsheet = new ObjectiveDataForSpreadsheet();
        objectiveDataForSpreadsheet.setAlgorithm(Utils.getAlgorithmName(config));
        objectiveDataForSpreadsheet.setBestValueFounded(getStringValue(bestResult));
        objectiveDataForSpreadsheet.setValuePercentageFounded(getStringValue(perc));
        objectiveDataForSpreadsheet.setSortFunction(config.getAlgorithmType() == AlgorithmType.AEMMT ? Utils.getSortFunctionInPortuguese(config) : null);
        objectiveDataForSpreadsheet.setBestSolutionSimpleAverage(getStringValue(this.bestResult.getBestSolutionBySimpleAverage().getSimpleAverage()));
        objectiveDataForSpreadsheet.setBestSolutionHarmonicAverage(getStringValue(this.bestResult.getBestSolutionByHarmonicAverage().getHarmonicAverage()));

        dataForSpreadsheet.addObjective(objectiveDataForSpreadsheet);

        mapDataForSpreadsheet.put(dataForSpreadsheet.getKey(), dataForSpreadsheet);
    }

    private double round(double value, int decimalPlaces) {
        return Math.round(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
    }

    class TotalCalculation {
        int totalBest;
        Map<Integer, Integer> mapTotalValueBetweenBottonAndTopLimit = new HashMap<>();

        int totalValueBetween75_100;
        int totalValueBetween50_75;
        int totalValueBetween25_50;
        int totalValueBetween0_25;

        TotalCalculation() {
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_90_100, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_80_90, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_70_80, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_60_70, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_50_60, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_40_50, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_30_40, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_20_30, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_10_20, Constants.DEFAULT_RANGE_VALUE);
            mapTotalValueBetweenBottonAndTopLimit.put(Constants.RANGE_0_10, Constants.DEFAULT_RANGE_VALUE);
        }
    }

    public static void main (String args[]) {
        double t = 1.424528302;
        System.out.println(t);
        System.out.println(Double.toString(t).replace(".", ","));
    }

    public void calculateHiperVolume(Configuration config, Table resultTable) {
        double hiperVolume;
        double sumHiperVolume = 0.0;
        double maxHiperVolume = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < resultTable.getTotalChromosomes(); i++) {
            hiperVolume = calculateHiperVolumeForChromossome(config, resultTable.getChromosomeFromIndex(i));
            sumHiperVolume +=  hiperVolume;
            if (maxHiperVolume < hiperVolume) {
                maxHiperVolume = hiperVolume;
            }
        }

        bestResult.setFinalChromosomeCount(resultTable.getTotalChromosomes());
        bestResult.setHiperVolume(sumHiperVolume);
        bestResult.setBestSolutionHiperVolume(maxHiperVolume);
    }

    public void calculateHiperVolume(Configuration config, List<Chromosome> chromosomeList) {
        double hiperVolume;
        double sumHiperVolume = 0.0;
        double maxHiperVolume = Double.NEGATIVE_INFINITY;

        for (Chromosome chromosome : chromosomeList) {
            hiperVolume = calculateHiperVolumeForChromossome(config, chromosome);
            sumHiperVolume += hiperVolume;
            if (maxHiperVolume < hiperVolume) {
                maxHiperVolume = hiperVolume;
            }
        }

        bestResult.setFinalChromosomeCount(chromosomeList.size());
        bestResult.setHiperVolume(sumHiperVolume);
        bestResult.setBestSolutionHiperVolume(maxHiperVolume);
    }
    
    private double calculateHiperVolumeForChromossome(Configuration config, Chromosome chromosome) {
        double hiperVolume = 0.0;

        for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
            double objectiveValue = chromosome.getRealObjectiveValue(Utils.getActualObjectiveIndex(config, objective));

            //If you want to use the worst metric value found in the generation, fill the variable with true, 
            //now if you want to use the worst metric value found in general, fill the variable with false
            boolean useWorstValueFromGeneration = false;
            hiperVolume += Math.abs(objectiveValue - getWorstValue(Utils.getObjectiveName(Utils.getActualObjectiveIndex(config, objective)), useWorstValueFromGeneration));
        }

        return hiperVolume;
    }
}
