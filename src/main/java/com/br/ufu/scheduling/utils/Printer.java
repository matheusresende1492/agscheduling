package com.br.ufu.scheduling.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import com.br.ufu.scheduling.agmo.Table;
import com.br.ufu.scheduling.enums.AlgorithmType;
import com.br.ufu.scheduling.model.AGMOResultModel;
import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.DataForSpreadsheet;
import com.br.ufu.scheduling.model.FinalResultModel;
import com.br.ufu.scheduling.model.ObjectiveDataForSpreadsheet;

public class Printer {
	public static void printExecutionOrder(int[] startTimeTask, int[] finalTimeTask, int[] readinessTime, int task, Integer totalProcessors) throws Exception {
		StringBuilder sbExecutionOrder = new StringBuilder();

		for (int processor = 1; processor <= totalProcessors; processor++) {
			sbExecutionOrder.append("RT[" + processor + "] = " + readinessTime[processor]);

			if (processor != totalProcessors) {
				sbExecutionOrder.append(", ");
			}
		}

		sbExecutionOrder.append("\n");

		for (int taskIndex = 1; taskIndex <= totalProcessors; taskIndex++) {
			sbExecutionOrder.append("ST[" + taskIndex + "] = " + startTimeTask[taskIndex] + ", ");
			sbExecutionOrder.append("FT[" + taskIndex + "] = " + finalTimeTask[taskIndex] + (taskIndex == task ? " [Running task now " + task + "]" : ""));
			sbExecutionOrder.append("\n");
		}

		Utils.print(sbExecutionOrder.toString());
	}

	public static void printChromosome(Configuration config, Chromosome chromosome, AlgorithmType algorithmType) throws Exception {
		printChromosome(config, chromosome, true, algorithmType);
	}

	public static void printChromosome(Configuration config, Chromosome chromosome, boolean printFitness, AlgorithmType algorithmType) throws Exception {
	    Utils.print(getObjectivesFromChromosomeFormatted(config, chromosome, printFitness, algorithmType));
	}

    public static void printChromosome(Configuration config, Chromosome chromosome, boolean printFitness, AlgorithmType algorithmType, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getObjectivesFromChromosomeFormatted(config, chromosome, printFitness, algorithmType), finalResultWriter);
    }

	public static void printChromosomeVectors(int[] mapping, int[] scheduling) {
		System.out.println("Mapping (Processors) : " + getFormattedVector(mapping));
		System.out.println("Scheduling (Tasks) : " + getFormattedVector(scheduling));
	}

	private static String getFormattedVector(int[] vector) {
		StringBuilder sbFormattedVector = new StringBuilder();
		sbFormattedVector.append("[ ");

		for (int i = 0; i < vector.length; i++) {
			sbFormattedVector.append(vector[i]);

			if (i != vector.length - 1) {
				sbFormattedVector.append(", ");
			}
		}

		sbFormattedVector.append(" ]");

		return sbFormattedVector.toString();
	}

	public static void printFinalResult(FinalResultModel result, Configuration config) throws Exception {
		StringBuilder builder = new StringBuilder();

		append(builder, "######################################");
		append(builder, "############ Final Result ############");
		append(builder, "######################################");
		append(builder, "");

		if (config.isConvergenceForTheBestSolution()) {
			double sucessPercentage = result.getTotalSuccess() * 100.0 / result.getTotalNumberOfChromosomes();
			append(builder, String.format("Successful run number: " + result.getTotalSuccess() + " -> %.2f", sucessPercentage) + "%");
		}

		append(builder, "Runtime for " + result.getTotalNumberOfChromosomes() + " iterations: " + ((double) (System.currentTimeMillis() - result.getInitialTime()) / 1000) + " segundos.\n");

		append(builder, "## Averages of the best chromosomes of the iterations ##");
		append(builder, "Average S_Length: " + ( ((double) result.getTotalSLenght()) / result.getTotalNumberOfChromosomes()));
		append(builder, "Average Load_Balance: " + (result.getTotalLoadBalance() / result.getTotalNumberOfChromosomes()));
		append(builder, "Average FlowTime: " + (result.getTotalFlowTime() / result.getTotalNumberOfChromosomes()));
		append(builder, "Average CommunicationCost: " + (result.getTotalCommunicationCost() / result.getTotalNumberOfChromosomes()));
		append(builder, "Average WaitingTime: " + (result.getTotalWaitingTime() / result.getTotalNumberOfChromosomes()));
		append(builder, "Average Fitness: " + (result.getTotalFitness() / result.getTotalNumberOfChromosomes()));

		Utils.print(builder.toString());
	}

	private static void append(StringBuilder builder, String message) {
		builder.append(message + Constants.LINE_BREAK);
	}

   public static void printFinalResultForNSGA2(Configuration config, List<Chromosome> chromosomeList, long initialTime, Map<String, DataForSpreadsheet> mapDataForSpreadsheet) throws Exception {
       if (config.isSystemOutPrintInFile()) {
           try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
               printFinalResultForNSGA2(config, chromosomeList, initialTime, mapDataForSpreadsheet, finalResultWriter);

           } catch (Exception e) {
               Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
               e2.initCause(e);

               throw e2;
           } 
       } else {
           printFinalResultForNSGA2(config, chromosomeList, initialTime, mapDataForSpreadsheet, null);
       }
    }

    public static void printFinalResultForNSGA3(Configuration config, List<Chromosome> chromosomeList, long initialTime, Map<String, DataForSpreadsheet> mapDataForSpreadsheet) throws Exception {
        if (config.isSystemOutPrintInFile()) {
            try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
                printFinalResultForNSGA3(config, chromosomeList, initialTime, mapDataForSpreadsheet, finalResultWriter);

            } catch (Exception e) {
                Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                e2.initCause(e);

                throw e2;
            }
        } else {
            printFinalResultForNSGA3(config, chromosomeList, initialTime, mapDataForSpreadsheet, null);
        }
    }

    public static void printFinalResultForNSGA2(Configuration config, List<Chromosome> chromosomeList, long initialTime, Map<String, DataForSpreadsheet> mapDataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getHeadFinalResult(AlgorithmType.NSGAII), finalResultWriter);

        AGMOResultModel resultModel = new AGMOResultModel(config, chromosomeList.size());

        for (int i = 0; i < chromosomeList.size(); i++) {
            Utils.print("## Chromosome " + (i + 1) + " ##", finalResultWriter);
            Utils.print(getObjectivesFromChromosomeFormatted(config, chromosomeList.get(i), false, AlgorithmType.NSGAII), finalResultWriter);

            resultModel.processChromosome(chromosomeList.get(i));
        }

        resultModel.calculateHiperVolume(config, chromosomeList);
        resultModel.showResult(config, mapDataForSpreadsheet, finalResultWriter);

        double runtime = getRuntime(initialTime);
        Utils.print(getGeneralData(config, chromosomeList.size(), runtime), finalResultWriter);

        putFinalData(mapDataForSpreadsheet, chromosomeList.size(), runtime);
    }

    public static void printFinalResultForNSGA3(Configuration config, List<Chromosome> chromosomeList, long initialTime, Map<String, DataForSpreadsheet> mapDataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getHeadFinalResult(AlgorithmType.NSGAIII), finalResultWriter);

        AGMOResultModel resultModel = new AGMOResultModel(config, chromosomeList.size());

        for (int i = 0; i < chromosomeList.size(); i++) {
            Utils.print("## Chromosome " + (i + 1) + " ##", finalResultWriter);
            Utils.print(getObjectivesFromChromosomeFormatted(config, chromosomeList.get(i), false, AlgorithmType.NSGAIII), finalResultWriter);

            resultModel.processChromosome(chromosomeList.get(i));
        }

        resultModel.calculateHiperVolume(config, chromosomeList);
        resultModel.showResult(config, mapDataForSpreadsheet, finalResultWriter);

        double runtime = getRuntime(initialTime);
        Utils.print(getGeneralData(config, chromosomeList.size(), runtime), finalResultWriter);

        putFinalData(mapDataForSpreadsheet, chromosomeList.size(), runtime);
    }
   
	public static void printFinalResultForAEMMT(Configuration config, Table resultTable, long initialTime, Map<String, DataForSpreadsheet> dataForSpreadsheet) throws Exception {
	    printFinalResult(config, resultTable, initialTime, AlgorithmType.AEMMT, dataForSpreadsheet);
	}

    public static void printFinalResultForAEMMD(Configuration config, Table resultTable, long initialTime, Map<String, DataForSpreadsheet> dataForSpreadsheet) throws Exception {
        printFinalResult(config, resultTable, initialTime, AlgorithmType.AEMMD, dataForSpreadsheet);
    }

    public static void printFinalResultForAEMMT(Configuration config, Table resultTable, long initialTime, Map<String, DataForSpreadsheet> dataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        printFinalResult(config, resultTable, initialTime, AlgorithmType.AEMMT, dataForSpreadsheet, finalResultWriter);
    }

    public static void printFinalResultForAEMMD(Configuration config, Table resultTable, long initialTime, Map<String, DataForSpreadsheet> dataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        printFinalResult(config, resultTable, initialTime, AlgorithmType.AEMMD, dataForSpreadsheet, finalResultWriter);
    }

    private static void printFinalResult(Configuration config, Table resultTable, long initialTime, AlgorithmType algorithmType, Map<String, DataForSpreadsheet> mapDataForSpreadsheet) throws Exception {
        if (config.isSystemOutPrintInFile()) {
            try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
                printFinalResult(config, resultTable, initialTime, algorithmType, mapDataForSpreadsheet, finalResultWriter);

            } catch (Exception e) {
                Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                e2.initCause(e);

                throw e2;
            } 
        } else {
            printFinalResult(config, resultTable, initialTime, algorithmType, mapDataForSpreadsheet, null);
        }
    }

    private static void printFinalResult(Configuration config, Table resultTable, long initialTime, AlgorithmType algorithmType, Map<String, DataForSpreadsheet> mapDataForSpreadsheet, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getHeadFinalResult(algorithmType), finalResultWriter);

        AGMOResultModel resultModel = new AGMOResultModel(config, resultTable.getTotalChromosomes());

        for (int i = 0; i < resultTable.getTotalChromosomes(); i++) {
            Utils.print("## Chromosome " + (i + 1) + " ##", finalResultWriter);
            Utils.print(getObjectivesFromChromosomeFormatted(config, resultTable.getChromosomeFromIndex(i), false, algorithmType), finalResultWriter);

            resultModel.processChromosome(resultTable.getChromosomeFromIndex(i));
        }

        resultModel.calculateHiperVolume(config, resultTable);
        resultModel.showResult(config, mapDataForSpreadsheet, finalResultWriter);

        double runtime = getRuntime(initialTime);
        Utils.print(getGeneralData(config, resultTable.getTotalChromosomes(), runtime), finalResultWriter);

        putFinalData(mapDataForSpreadsheet, resultTable.getTotalChromosomes(), runtime);
    }

    private static String getHeadFinalResult(AlgorithmType algorithmType) {
        StringBuilder builder = new StringBuilder();

        append(builder, "######################################");
        append(builder, "############ Final Result ############");
        append(builder, "######################################");
        append(builder, "");

        append(builder, "############ " + Utils.getAlgorithmName(algorithmType) + " ############");
        append(builder, "## Chromosomes Non-Dominated ##");
        append(builder, "");

        return builder.toString();
    }

	private static String getObjectivesFromChromosomeFormatted(Configuration config, Chromosome chromosome, boolean showFitness, AlgorithmType algorithmType) {
		StringBuilder builder = new StringBuilder();

		append(builder, "Mapping (Processors) : " + getFormattedVector(chromosome.getMapping()));
		append(builder, "Scheduling (Tasks) : " + getFormattedVector(chromosome.getScheduling()));

		for (int objective = 1; objective <= config.getTotalObjectives(); objective++) {
		    appendObjective(config, chromosome, builder, objective);
		}

		append(builder, "SimpleAverage: " + chromosome.getSimpleAverage());
		append(builder, "HarmonicAverage: " + chromosome.getHarmonicAverage());

        if (AlgorithmType.AEMMT == algorithmType) {
            append(builder, "Value For Sort (normalization): " + chromosome.getValueForSort());
        }

        if (AlgorithmType.NSGAII == algorithmType) {
            append(builder, "Rank: " + chromosome.getRank());
            append(builder, "CrowndingDistance: " + chromosome.getCrowdingDistance());
        }

        if (AlgorithmType.NSGAIII == algorithmType) {
            append(builder, "Rank: " + chromosome.getRank());
        }

		if (showFitness) {
			append(builder, "Fitness : " + chromosome.getFitness());
		}

		return builder.toString();
	}

    private static void appendObjective(Configuration config, Chromosome chromosome, StringBuilder builder, int objective) {
        append(builder, Utils.getObjectiveName(Utils.getActualObjectiveIndex(config, objective)) + ": " + chromosome.getRealObjectiveValue(Utils.getActualObjectiveIndex(config, objective)));
    }

    private static double getRuntime(long initialTime) {
        return (double) (System.currentTimeMillis() - initialTime) / 1000.0;
    }

	private static String getGeneralData(Configuration config, int totalChromosomes, double runtime) {
		StringBuilder builder = new StringBuilder();

        append(builder, "##################################");
        append(builder, "");
		append(builder, "Total Chromosomes Non-Dominated: " + totalChromosomes + ".");
		append(builder, "Runtime for " + config.getTotalGenerations() + " generations: " + runtime + " segundos.\n");

		return builder.toString();
	}

    private static void putFinalData(Map<String, DataForSpreadsheet> mapDataForSpreadsheet, int totalChromosomes, double runtime) {
        for (Map.Entry<String, DataForSpreadsheet> mapData : mapDataForSpreadsheet.entrySet()) {
            DataForSpreadsheet dataForSpreadsheet = mapData.getValue();

            for (ObjectiveDataForSpreadsheet objectiveDataForSpreadsheet : dataForSpreadsheet.getListObjectivesDataForSpreadsheet()) {
                objectiveDataForSpreadsheet.setTotalChromosomesNonDominated(totalChromosomes);
                objectiveDataForSpreadsheet.setRuntime(getStringValue(runtime));
            }
        }
    }

    private static String getStringValue(double source) {
        return Double.toString(source).replace(".", ",");
    }

    public static void printFinalResultForNSGA2WithComparedToNonDominated(Configuration config, List<Chromosome> chromosomeList, long initialTime) throws Exception {
        if (config.isSystemOutPrintInFile()) {
            try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
                printFinalResultForNSGA2WithComparedToNonDominated(config, chromosomeList, initialTime, finalResultWriter);

            } catch (Exception e) {
                Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                e2.initCause(e);

                throw e2;
            } 
        } else {
            printFinalResultForNSGA2WithComparedToNonDominated(config, chromosomeList, initialTime, null);
        }
    }

    public static void printFinalResultForNSGA2WithComparedToNonDominated(Configuration config, List<Chromosome> chromosomeList, long initialTime, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getHeadFinalResult(AlgorithmType.NSGAII), finalResultWriter);

        for (int chromosomeAIndex = 0; chromosomeAIndex < chromosomeList.size(); chromosomeAIndex++) {
            Chromosome chromosomeA = chromosomeList.get(chromosomeAIndex);
            Utils.print(getDataFromChromosomeA(config, chromosomeAIndex, chromosomeA, AlgorithmType.NSGAII), finalResultWriter);

            for (int chromosomeBIndex = 0; chromosomeBIndex < chromosomeList.size(); chromosomeBIndex++) {
                Chromosome chromosomeB = chromosomeList.get(chromosomeBIndex);
                Utils.print(getDataFromChromosomeB(config, chromosomeA, chromosomeBIndex, chromosomeB, AlgorithmType.NSGAII), finalResultWriter);
            }
        }

        double runtime = getRuntime(initialTime);
        Utils.print(getGeneralData(config, chromosomeList.size(), runtime), finalResultWriter);
    }

    public static void printFinalResultForNSGA3WithComparedToNonDominated(Configuration config, List<Chromosome> chromosomeList, long initialTime) throws Exception {
        if (config.isSystemOutPrintInFile()) {
            try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
                printFinalResultForNSGA3WithComparedToNonDominated(config, chromosomeList, initialTime, finalResultWriter);

            } catch (Exception e) {
                Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                e2.initCause(e);

                throw e2;
            }
        } else {
            printFinalResultForNSGA3WithComparedToNonDominated(config, chromosomeList, initialTime, null);
        }
    }

    public static void printFinalResultForNSGA3WithComparedToNonDominated(Configuration config, List<Chromosome> chromosomeList, long initialTime, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getHeadFinalResult(AlgorithmType.NSGAIII), finalResultWriter);

        for (int chromosomeAIndex = 0; chromosomeAIndex < chromosomeList.size(); chromosomeAIndex++) {
            Chromosome chromosomeA = chromosomeList.get(chromosomeAIndex);
            Utils.print(getDataFromChromosomeA(config, chromosomeAIndex, chromosomeA, AlgorithmType.NSGAIII), finalResultWriter);

            for (int chromosomeBIndex = 0; chromosomeBIndex < chromosomeList.size(); chromosomeBIndex++) {
                Chromosome chromosomeB = chromosomeList.get(chromosomeBIndex);
                Utils.print(getDataFromChromosomeB(config, chromosomeA, chromosomeBIndex, chromosomeB, AlgorithmType.NSGAIII), finalResultWriter);
            }
        }

        double runtime = getRuntime(initialTime);
        Utils.print(getGeneralData(config, chromosomeList.size(), runtime), finalResultWriter);
    }

	public static void printFinalResultForAEMMTWithComparedToNonDominated(Configuration config, List<Table> tables, long initialTime) throws Exception {
        if (config.isSystemOutPrintInFile()) {
            try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
                printFinalResultForAEMMTWithComparedToNonDominated(config, tables, initialTime, finalResultWriter);

            } catch (Exception e) {
                Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                e2.initCause(e);

                throw e2;
            } 
        } else {
            printFinalResultForAEMMTWithComparedToNonDominated(config, tables, initialTime, null);
        }
	}

	public static void printFinalResultForAEMMTWithComparedToNonDominated(Configuration config, List<Table> tables, long initialTime, BufferedWriter finalResultWriter) throws Exception {
	       Utils.print(getHeadFinalResult(AlgorithmType.AEMMT), finalResultWriter);

	        Table nonDominatedTable = tables.get(tables.size() - 1);

	        for (int chromosomeNonDominatedIndex = 0; chromosomeNonDominatedIndex < nonDominatedTable.getTotalChromosomes(); chromosomeNonDominatedIndex++) {
	            Chromosome chromosomeA = nonDominatedTable.getChromosomeFromIndex(chromosomeNonDominatedIndex);
	            Utils.print(getDataFromChromosomeA(config, chromosomeNonDominatedIndex, chromosomeA, AlgorithmType.AEMMT), finalResultWriter);

	            int totalCromossomesAnalised = 0;
	            for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
	                Table table = tables.get(tableIndex);

	                for (int chromosomeIndex = 0; chromosomeIndex < table.getTotalChromosomes(); chromosomeIndex++) {
	                    Chromosome chromosomeB = table.getChromosomeFromIndex(chromosomeIndex);
	                    Utils.print(getDataFromChromosomeB(config, chromosomeA, totalCromossomesAnalised, chromosomeB, AlgorithmType.AEMMT), finalResultWriter);

	                    totalCromossomesAnalised++;
	                }
	            }
	        }

	        double runtime = getRuntime(initialTime);
	        Utils.print(getGeneralData(config, nonDominatedTable.getTotalChromosomes(), runtime), finalResultWriter);
	}

    public static void printFinalResultForAEMMDWithComparedToNonDominated(Configuration config, Table resultTable, long initialTime) throws Exception {
        if (config.isSystemOutPrintInFile()) {
            try (BufferedWriter finalResultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("finalResult.txt")))) {
                printFinalResultForAEMMDWithComparedToNonDominated(config, resultTable, initialTime, finalResultWriter);

            } catch (Exception e) {
                Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                e2.initCause(e);

                throw e2;
            } 
        } else {
            printFinalResultForAEMMDWithComparedToNonDominated(config, resultTable, initialTime, null);
        }
    }

    public static void printFinalResultForAEMMDWithComparedToNonDominated(Configuration config, Table resultTable, long initialTime, BufferedWriter finalResultWriter) throws Exception {
        Utils.print(getHeadFinalResult(AlgorithmType.AEMMD), finalResultWriter);

        for (int chromosomeNonDominatedIndex = 0; chromosomeNonDominatedIndex < resultTable.getTotalChromosomes(); chromosomeNonDominatedIndex++) {
            Chromosome chromosomeA = resultTable.getChromosomeFromIndex(chromosomeNonDominatedIndex);
            Utils.print(getDataFromChromosomeA(config, chromosomeNonDominatedIndex, chromosomeA, AlgorithmType.AEMMD), finalResultWriter);

            for (int chromosomeIndex = 0; chromosomeIndex < resultTable.getTotalChromosomes(); chromosomeIndex++) {
                Chromosome chromosomeB = resultTable.getChromosomeFromIndex(chromosomeIndex);
                Utils.print(getDataFromChromosomeB(config, chromosomeA, chromosomeIndex, chromosomeB, AlgorithmType.AEMMD), finalResultWriter);
            }
        }

        double runtime = getRuntime(initialTime);
        Utils.print(getGeneralData(config, resultTable.getTotalChromosomes(), runtime), finalResultWriter);
    }

    private static String getDataFromChromosomeA(Configuration config, int chromosomeNonDominatedIndex, Chromosome chromosomeA, AlgorithmType algorithmType) {
        StringBuilder builder = new StringBuilder();

        append(builder, "##################################");
        append(builder, "Chromosome A: - " + (chromosomeNonDominatedIndex + 1));
        append(builder, "");

        append(builder, getObjectivesFromChromosomeFormatted(config, chromosomeA, false, algorithmType));
        append(builder, "");

        return builder.toString();
    }

    private static String getDataFromChromosomeB(Configuration config, Chromosome chromosomeA, int totalCromossomesAnalised, Chromosome chromosomeB, AlgorithmType algorithmType) {
        StringBuilder builder = new StringBuilder();

        append(builder, "----------------------------------");
        append(builder, "Chromosome B: - " + (totalCromossomesAnalised + 1));
        append(builder, "");

        append(builder, getObjectivesFromChromosomeFormatted(config, chromosomeB, false, algorithmType));
        append(builder, "");

        append(builder, "SLength : ");
        append(builder, "   B: " + chromosomeB.getFitnessForSLength() + " | A: " + chromosomeA.getFitnessForSLength());
        append(builder, "   Result: " + getResolvedComparison(chromosomeB.getFitnessForSLength(), chromosomeA.getFitnessForSLength()));

        append(builder, "LoadBalance : ");
        append(builder, "   B: " + chromosomeB.getFitnessForLoadBalance() + " | A: " + chromosomeA.getFitnessForLoadBalance());
        append(builder, "   Result: " + getResolvedComparison(chromosomeB.getFitnessForLoadBalance(), chromosomeA.getFitnessForLoadBalance()));

        append(builder, "FlowTime : ");
        append(builder, "   B: " + chromosomeB.getFitnessForFlowTime() + " | A: " + chromosomeA.getFitnessForFlowTime());
        append(builder, "   Result: " + getResolvedComparison(chromosomeB.getFitnessForFlowTime(), chromosomeA.getFitnessForFlowTime()));

        append(builder, "CommunicationCost : ");
        append(builder, "   B: " + chromosomeB.getFitnessForCommunicationCost() + " | A: " + chromosomeA.getFitnessForCommunicationCost());
        append(builder, "   Result: " + getResolvedComparison(chromosomeB.getFitnessForCommunicationCost(), chromosomeA.getFitnessForCommunicationCost()));

        append(builder, "WaitingTime : ");
        append(builder, "   B: " + chromosomeB.getFitnessForWaitingTime() + " | A: " + chromosomeA.getFitnessForWaitingTime());
        append(builder, "   Result: " + getResolvedComparison(chromosomeB.getFitnessForWaitingTime(), chromosomeA.getFitnessForWaitingTime()));

        append(builder, "");

        append(builder, "B domina A: " + chromosomeA.isChromosomeDominated(config, chromosomeB));
        append(builder, "");

        return builder.toString();
    }

	private static String getResolvedComparison(double valueA, double valueB) {
		if (valueA > valueB) {
			return "Greater or Equal";

		} else if (valueA == valueB) {
			return "Equal";

		} else {
			return "Less";
		}
	}
}
