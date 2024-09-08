package com.br.ufu.scheduling.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;


import com.br.ufu.scheduling.enums.AlgorithmType;
import com.br.ufu.scheduling.enums.MetricType;
import com.br.ufu.scheduling.enums.MutationType;
import com.br.ufu.scheduling.enums.SelectionType;
import com.br.ufu.scheduling.enums.SortFunctionType;
import com.br.ufu.scheduling.file.normalization.with.cost.LoaderNormalizationWithCost;
import com.br.ufu.scheduling.file.normalization.with.cost.backup.LoaderNormalizationWithCostBackup;
import com.br.ufu.scheduling.file.normalization.without.cost.LoaderNormalizationWithoutCost;

public class Configuration {
	private Integer initialPopulation;
	private Double mutationRate;
	private Double crossoverRate;
	private Integer iterations;
	private Integer generations;
	private MetricType metricType;
	private MutationType mutationType;
	private SelectionType selectionType;
	private AlgorithmType algorithmType;
	private Integer tourForTournament;
	private Integer totalProcessors;
	private Boolean printIterations;
	private Boolean printGenerations;
	private Boolean testMode;
	private Integer maximizationConstant;
	private Integer attemptSelectParentNotRepeated;
	private Boolean stopGenerationIfFindBestSolution;
	private Boolean allowApplyingMutationOnRepeatedChild;
	private Boolean printBestChromosomeOfGeneration;
	private Boolean convergenceForTheBestSolution;
	private String taskGraphFileName;
	private Boolean graphWithCommunicationCost;
	private Boolean generateRandomCommunicationCostForNoCostDag;
	private Boolean generateCommunicationCostFromDAGWithoutCommunicationCost;
	private Boolean printGraphAtTheBeginningOfRun;
	private Boolean printBestResultsByObjectives;
	private Integer seed;
	private Boolean systemOutPrintInFile;
	private Boolean printHiperVolumeInConsole;

	//AGMO
	private Boolean executeMultiObjectiveGA;
	private Integer totalObjectives;
	private Integer sizeOfTables;
	private Integer sizeOfNonDominatedTable;
	private Integer totalGenerations;
	private Integer totalGenerationsToResetTableScore;
	private Boolean printComparisonNonDominatedChromosomes;
	private Integer totalGenerationsToApplyMutation;
	private SortFunctionType sortFunctionType;
	private Boolean calculateMaximusAndMinimusForNormalization;
	private Integer objective1;
	private Integer objective2;
	private Integer objective3;
	private Integer objective4;
	private Integer objective5;
	private Double weight1;
	private Double weight2;
	private Double weight3;
	private Double weight4;
	private Double weight5;

	//AGMO Normalization Data
	private Double maxObjectiveValue1;
	private Double minObjectiveValue1;
	private Double maxObjectiveValue2;
	private Double minObjectiveValue2;
	private Double maxObjectiveValue3;
	private Double minObjectiveValue3;
	private Double maxObjectiveValue4;
	private Double minObjectiveValue4;
	private Double maxObjectiveValue5;
	private Double minObjectiveValue5;
	
    private Double realMaxObjectiveValue1;
    private Double realMinObjectiveValue1;
    private Double realMaxObjectiveValue2;
    private Double realMinObjectiveValue2;
    private Double realMaxObjectiveValue3;
    private Double realMinObjectiveValue3;
    private Double realMaxObjectiveValue4;
    private Double realMinObjectiveValue4;
    private Double realMaxObjectiveValue5;
    private Double realMinObjectiveValue5;

	//CSV File
	private Boolean generateCsvFile;
	private Integer totalDifferentChromosomes;
	private Integer maximumAttemptsGenerateDifferentChromosomes;

	//Auxiliaries
	private int metric;
	private int mutation;
	private int selection;
	private int algorithm;
	private int sortFunction;

	public Configuration() throws Exception {
		readConfiguration(null);
	}

	public Configuration(String fileNameForDataNormalization) throws Exception {
	    readConfiguration(fileNameForDataNormalization);
    }

	public Integer getInitialPopulation() {
		return initialPopulation;
	}

	public Double getMutationRate() {
		return mutationRate;
	}

	public Double getCrossoverRate() {
		return crossoverRate;
	}

	public Integer getIterations() {
		return iterations;
	}

	public Integer getGenerations() {
		return generations;
	}

	public MetricType getMetricType() {
		return metricType;
	}

	public MutationType getMutationType() {
		return mutationType;
	}

	public SelectionType getSelectionType() {
		return selectionType;
	}

	public AlgorithmType getAlgorithmType() {
	    return algorithmType;
	}

	public Integer getTourForTournament() {
		return tourForTournament;
	}

	public Integer getTotalProcessors() {
		return totalProcessors;
	}

	public Boolean isPrintIterations() {
		return printIterations;
	}

    public Boolean isPrintGenerations() {
        return printGenerations;
    }

	public Boolean isTestMode() {
		return testMode;
	}

	public Integer getMaximizationConstant() {
		return maximizationConstant;
	}

	public Integer getAttemptSelectParentNotRepeated() {
		return attemptSelectParentNotRepeated;
	}

	public Boolean isStopGenerationIfFindBestSolution() {
		return stopGenerationIfFindBestSolution;
	}

	public Boolean isAllowApplyingMutationOnRepeatedChild() {
		return allowApplyingMutationOnRepeatedChild;
	}

	public Boolean isPrintBestChromosomeOfGeneration() {
		return printBestChromosomeOfGeneration;
	}

	public Boolean isConvergenceForTheBestSolution() {
		return convergenceForTheBestSolution;
	}

	public String getTaskGraphFileName() {
		return taskGraphFileName;
	}

	public Boolean isGraphWithCommunicationCost() {
		return graphWithCommunicationCost;
	}

	public Boolean isGenerateRandomCommunicationCostForNoCostDag() {
        return generateRandomCommunicationCostForNoCostDag;
    }

    public Boolean isGenerateCommunicationCostFromDAGWithoutCommunicationCost() {
        return generateCommunicationCostFromDAGWithoutCommunicationCost;
    }

    public Boolean isPrintGraphAtTheBeginningOfRun() {
		return printGraphAtTheBeginningOfRun;
	}

	public Boolean isPrintBestResultsByObjectives() {
        return printBestResultsByObjectives;
    }

    public Integer getSeed() {
        return seed;
    }

    public Boolean isSystemOutPrintInFile() {
        return systemOutPrintInFile;
    }

    public Boolean isPrintHiperVolumeInConsole() {
        return printHiperVolumeInConsole;
    }

    public Boolean isExecuteMultiObjectiveGA() {
		return executeMultiObjectiveGA;
	}

	public Integer getTotalObjectives() {
		return totalObjectives;
	}

	public Integer getSizeOfTables() {
		return sizeOfTables;
	}

	public Integer getSizeOfNonDominatedTable() {
		return sizeOfNonDominatedTable;
	}

	public Integer getTotalGenerations() {
		return totalGenerations;
	}

	public Integer getTotalGenerationsToResetTableScore() {
		return totalGenerationsToResetTableScore;
	}

	public Boolean isPrintComparisonNonDominatedChromosomes() {
		return printComparisonNonDominatedChromosomes;
	}

	public Integer getTotalGenerationsToApplyMutation() {
		return totalGenerationsToApplyMutation;
	}

	public SortFunctionType getSortFunctionType() {
		return sortFunctionType;
	}

	public Boolean isCalculateMaximusAndMinimusForNormalization() {
		return calculateMaximusAndMinimusForNormalization;
	}

    public Integer getObjective1() {
		return objective1;
	}

	public Integer getObjective2() {
		return objective2;
	}

	public Integer getObjective3() {
		return objective3;
	}

	public Integer getObjective4() {
		return objective4;
	}

	public Integer getObjective5() {
		return objective5;
	}

	public Double getWeight1() {
		return weight1;
	}

	public Double getWeight2() {
		return weight2;
	}

	public Double getWeight3() {
		return weight3;
	}

	public Double getWeight4() {
		return weight4;
	}

	public Double getWeight5() {
		return weight5;
	}

	public Double getMaxObjectiveValue1() {
		return maxObjectiveValue1;
	}

	public Double getMinObjectiveValue1() {
		return minObjectiveValue1;
	}

	public Double getMaxObjectiveValue2() {
		return maxObjectiveValue2;
	}

	public Double getMinObjectiveValue2() {
		return minObjectiveValue2;
	}

	public Double getMaxObjectiveValue3() {
		return maxObjectiveValue3;
	}

	public Double getMinObjectiveValue3() {
		return minObjectiveValue3;
	}

	public Double getMaxObjectiveValue4() {
		return maxObjectiveValue4;
	}

	public Double getMinObjectiveValue4() {
		return minObjectiveValue4;
	}

	public Double getMaxObjectiveValue5() {
		return maxObjectiveValue5;
	}

	public Double getMinObjectiveValue5() {
		return minObjectiveValue5;
	}

    public Double getRealMaxObjectiveValue1() {
        return realMaxObjectiveValue1;
    }

    public Double getRealMinObjectiveValue1() {
        return realMinObjectiveValue1;
    }

    public Double getRealMaxObjectiveValue2() {
        return realMaxObjectiveValue2;
    }

    public Double getRealMinObjectiveValue2() {
        return realMinObjectiveValue2;
    }

    public Double getRealMaxObjectiveValue3() {
        return realMaxObjectiveValue3;
    }

    public Double getRealMinObjectiveValue3() {
        return realMinObjectiveValue3;
    }

    public Double getRealMaxObjectiveValue4() {
        return realMaxObjectiveValue4;
    }

    public Double getRealMinObjectiveValue4() {
        return realMinObjectiveValue4;
    }

    public Double getRealMaxObjectiveValue5() {
        return realMaxObjectiveValue5;
    }

    public Double getRealMinObjectiveValue5() {
        return realMinObjectiveValue5;
    }

	public Boolean isGenerateCsvFile() {
		return generateCsvFile;
	}

	public Integer getTotalDifferentChromosomes() {
		return totalDifferentChromosomes;
	}

	public Integer getMaximumAttemptsGenerateDifferentChromosomes() {
		return maximumAttemptsGenerateDifferentChromosomes;
	}

	//The sets methods are used by the Java Reflection lib to populate the configuration
	public void setInitialPopulation(Integer initialPopulation) {
		this.initialPopulation = initialPopulation;
	}

	public void setMutationRate(Double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public void setCrossoverRate(Double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}
	
	public void setIterations(Integer iterations) {
		this.iterations = iterations;
	}
	
	public void setGenerations(Integer generations) {
		this.generations = generations;
	}

	public void setMetric(Integer metric) {
		this.metric = metric;
		setMetricType(this.metric);
	}

	public void setMetricType(Integer metric) {
		switch (metric) {
		case 0:
			metricType = MetricType.MAKESPAN;
			break;

		case 1:
			metricType = MetricType.LOAD_BALANCE;
			break;

		case 2:
			metricType = MetricType.FLOW_TIME;
			break;

		case 3:
			metricType = MetricType.COMMUNICATION_COST;
			break;

		case 4:
			metricType = MetricType.WAITING_TIME;
			break;

		default:
			throw new IllegalArgumentException("Invalid value of metric: " + metric + ". Valid values: " + Arrays.asList(0, 1, 2, 3, 4, 5).toString());
		}
	}

	public void setMutation(Integer mutation) {
		this.mutation = mutation;
		setMutationType(this.mutation);
	}

	public void setMutationType(Integer mutation) {
		switch (mutation) {
		case 0:
			mutationType = MutationType.ONE_POINT;
			break;

		case 1:
			mutationType = MutationType.TWO_POINTS;
			break;

		default:
			throw new IllegalArgumentException("Invalid value of mutation: " + mutation + ". Valid values: " + Arrays.asList(0, 1).toString());	
		}		
	}

	public void setSelection(Integer selection) {
		this.selection = selection;
		setSelectionType(this.selection);
	}

	public void setSelectionType(Integer selection) {
		switch (selection) {
		case 0:
			selectionType = SelectionType.ROULETTE;
			break;

		case 1:
			selectionType = SelectionType.SIMPLE_TOURNAMENT;
			break;

		case 2:
			selectionType = SelectionType.STOCHASTIC_TOURNAMENT;
			break;

		case 3:
			selectionType = SelectionType.LINEAR_RANKING;
			break;

		case 4:
			selectionType = SelectionType.NON_LINEAR_RANKING;
			break;

		default:
			throw new IllegalArgumentException("Invalid value of selection: " + selection + ". Valid values: " + Arrays.asList(0, 1, 2, 3, 4).toString());	
		}
	}

    public void setAlgorithm(Integer algorithm) {
        this.algorithm = algorithm;
        setAlgorithmType(this.algorithm);
    }

	private void setAlgorithmType(Integer algorithm) {
        switch (algorithm) {
        case 0:
            algorithmType = AlgorithmType.SINGLE_OBJECTIVE;
            break;

        case 1:
            algorithmType = AlgorithmType.NSGAII;
            break;

        case 2:
            algorithmType = AlgorithmType.SPEAII;
            break;

        case 3:
            algorithmType = AlgorithmType.AEMMT;
            break;

        case 4:
            algorithmType = AlgorithmType.AEMMD;
            break;

		case 5:
			algorithmType = AlgorithmType.NSGAIII;
			break;

        default:
            throw new IllegalArgumentException("Invalid value of selection: " + algorithm + ". Valid values: " + Arrays.asList(0, 1, 2, 3, 4).toString());   
        }        
    }

    public void setTourForTournament(Integer tourForTournament) {
		this.tourForTournament = tourForTournament;
	}

	public void setPrintIterations(Boolean printIterations) {
		this.printIterations = printIterations;
	}

    public void setPrintGenerations(Boolean printGenerations) {
        this.printGenerations = printGenerations;
    }

	public void setTotalProcessors(Integer totalProcessors) {
		this.totalProcessors = totalProcessors;
	}

	public void setTestMode(Boolean testMode) {
		this.testMode = testMode;
	}

	public void setMaximizationConstant(Integer maximizationConstant) {
		this.maximizationConstant = maximizationConstant;
	}

	public void setAttemptSelectParentNotRepeated(Integer attemptSelectParentNotRepeated) {
		this.attemptSelectParentNotRepeated = attemptSelectParentNotRepeated;
	}

	public void setStopGenerationIfFindBestSolution(Boolean stopGenerationIfFindBestSolution) {
		this.stopGenerationIfFindBestSolution = stopGenerationIfFindBestSolution;
	}

	public void setAllowApplyingMutationOnRepeatedChild(Boolean allowApplyingMutationOnRepeatedChild) {
		this.allowApplyingMutationOnRepeatedChild = allowApplyingMutationOnRepeatedChild;
	}

	public void setPrintBestChromosomeOfGeneration(Boolean printBestChromosomeOfGeneration) {
		this.printBestChromosomeOfGeneration = printBestChromosomeOfGeneration;
	}

	public void setConvergenceForTheBestSolution(Boolean convergenceForTheBestSolution) {
		this.convergenceForTheBestSolution = convergenceForTheBestSolution;
	}

	public void setTaskGraphFileName(String taskGraphFileName) {
		this.taskGraphFileName = taskGraphFileName;
	}

	public void setGraphWithCommunicationCost(Boolean graphWithCommunicationCost) {
		this.graphWithCommunicationCost = graphWithCommunicationCost;
	}

    public void setGenerateRandomCommunicationCostForNoCostDag(Boolean generateRandomCommunicationCostForNoCostDag) {
        this.generateRandomCommunicationCostForNoCostDag = generateRandomCommunicationCostForNoCostDag;
    }

    public void setGenerateCommunicationCostFromDAGWithoutCommunicationCost(Boolean generateCommunicationCostFromDAGWithoutCommunicationCost) {
        this.generateCommunicationCostFromDAGWithoutCommunicationCost = generateCommunicationCostFromDAGWithoutCommunicationCost;
    }

	public void setPrintGraphAtTheBeginningOfRun(Boolean printGraphAtTheBeginningOfRun) {
		this.printGraphAtTheBeginningOfRun = printGraphAtTheBeginningOfRun;
	}

    public void setPrintBestResultsByObjectives(Boolean printBestResultsByObjectives) {
        this.printBestResultsByObjectives = printBestResultsByObjectives;
    }

	public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public void setSystemOutPrintInFile(Boolean systemOutPrintInFile) {
        this.systemOutPrintInFile = systemOutPrintInFile;
    }

    public void setPrintHiperVolumeInConsole(Boolean printHiperVolumeInConsole) {
        this.printHiperVolumeInConsole = printHiperVolumeInConsole;
    }

    public void setExecuteMultiObjectiveGA(Boolean executeMultiObjectiveGA) {
		this.executeMultiObjectiveGA = executeMultiObjectiveGA;
	}

	public void setTotalObjectives(Integer totalObjectives) {
		this.totalObjectives = totalObjectives;
	}

	public void setSizeOfTables(Integer sizeOfTables) {
		this.sizeOfTables= sizeOfTables;
	}

	public void setSizeOfNonDominatedTable(Integer sizeOfNonDominatedTable) {
		this.sizeOfNonDominatedTable= sizeOfNonDominatedTable;
	}

	public void setTotalGenerations(Integer totalGenerations) {
		this.totalGenerations = totalGenerations;
	}

	public void setTotalGenerationsToResetTableScore(Integer totalGenerationsToResetTableScore) {
		this.totalGenerationsToResetTableScore = totalGenerationsToResetTableScore;
	}

	public void setPrintComparisonNonDominatedChromosomes(Boolean printComparisonNonDominatedChromosomes) {
		this.printComparisonNonDominatedChromosomes = printComparisonNonDominatedChromosomes;
	}

	public void setTotalGenerationsToApplyMutation(Integer totalGenerationsToApplyMutation) {
		this.totalGenerationsToApplyMutation = totalGenerationsToApplyMutation;
	}

    public void setSortFunction(Integer sortFunction) {
        this.sortFunction = sortFunction;
        setSortFunctionType(this.sortFunction);
    }

    private void setSortFunctionType(Integer sortFunction) {
        switch (sortFunction) {
        case 0:
            sortFunctionType = SortFunctionType.WEIGHT;
            break;

        case 1:
            sortFunctionType = SortFunctionType.SIMPLE_AVERAGE;
            break;

        case 2:
            sortFunctionType = SortFunctionType.HARMONIC_AVERAGE;
            break;

        default:
            throw new IllegalArgumentException("Invalid value of sortFunction: " + sortFunction + ". Valid values: " + Arrays.asList(0, 1, 2).toString());   
        }        
    }

	public void setCalculateMaximusAndMinimusForNormalization(Boolean calculateMaximusAndMinimusForNormalization) {
		this.calculateMaximusAndMinimusForNormalization = calculateMaximusAndMinimusForNormalization;
	}

	public void setObjective1(Integer objective1) {
		this.objective1 = objective1;
	}

	public void setObjective2(Integer objective2) {
		this.objective2 = objective2;
	}

	public void setObjective3(Integer objective3) {
		this.objective3 = objective3;
	}

	public void setObjective4(Integer objective4) {
		this.objective4 = objective4;
	}

	public void setObjective5(Integer objective5) {
		this.objective5 = objective5;
	}

	public void setWeight1(Double weight1) {
		this.weight1 = weight1;
	}

	public void setWeight2(Double weight2) {
		this.weight2 = weight2;
	}

	public void setWeight3(Double weight3) {
		this.weight3 = weight3;
	}

	public void setWeight4(Double weight4) {
		this.weight4 = weight4;
	}

	public void setWeight5(Double weight5) {
		this.weight5 = weight5;
	}

	public void setMaxObjectiveValue1(Double maxObjectiveValue1) {
		this.maxObjectiveValue1 = getTransformedObjectiveValue(maxObjectiveValue1);
		setRealMaxObjectiveValue1(maxObjectiveValue1);
	}

    public void setMinObjectiveValue1(Double minObjectiveValue1) {
		this.minObjectiveValue1 = getTransformedObjectiveValue(minObjectiveValue1);
		setRealMinObjectiveValue1(minObjectiveValue1);
	}

	public void setMaxObjectiveValue2(Double maxObjectiveValue2) {
		this.maxObjectiveValue2 = getTransformedObjectiveValue(maxObjectiveValue2);
		setRealMaxObjectiveValue2(maxObjectiveValue2);
	}

	public void setMinObjectiveValue2(Double minObjectiveValue2) {
		this.minObjectiveValue2 = getTransformedObjectiveValue(minObjectiveValue2);
		setRealMinObjectiveValue2(minObjectiveValue2);
	}

	public void setMaxObjectiveValue3(Double maxObjectiveValue3) {
		this.maxObjectiveValue3 = getTransformedObjectiveValue(maxObjectiveValue3);
		setRealMaxObjectiveValue3(maxObjectiveValue3);
	}

	public void setMinObjectiveValue3(Double minObjectiveValue3) {
		this.minObjectiveValue3 = getTransformedObjectiveValue(minObjectiveValue3);
		setRealMinObjectiveValue3(minObjectiveValue3);
	}

	public void setMaxObjectiveValue4(Double maxObjectiveValue4) {
		this.maxObjectiveValue4 = getTransformedObjectiveValue(maxObjectiveValue4);
		setRealMaxObjectiveValue4(maxObjectiveValue4);
	}

	public void setMinObjectiveValue4(Double minObjectiveValue4) {
		this.minObjectiveValue4 = getTransformedObjectiveValue(minObjectiveValue4);
		setRealMinObjectiveValue4(minObjectiveValue4);
	}

	public void setMaxObjectiveValue5(Double maxObjectiveValue5) {
		this.maxObjectiveValue5 = getTransformedObjectiveValue(maxObjectiveValue5);
		setRealMaxObjectiveValue5(maxObjectiveValue5);
	}

	public void setMinObjectiveValue5(Double minObjectiveValue5) {
		this.minObjectiveValue5 = getTransformedObjectiveValue(minObjectiveValue5);
		setRealMinObjectiveValue5(minObjectiveValue5);
	}

    private void setRealMaxObjectiveValue1(Double realMaxObjectiveValue1) {
        this.realMaxObjectiveValue1 = realMaxObjectiveValue1;
    }

    private void setRealMinObjectiveValue1(Double realMinObjectiveValue1) {
        this.realMinObjectiveValue1 = realMinObjectiveValue1;
    }

    private void setRealMaxObjectiveValue2(Double realMaxObjectiveValue2) {
        this.realMaxObjectiveValue2 = realMaxObjectiveValue2;
    }

    private void setRealMinObjectiveValue2(Double realMinObjectiveValue2) {
        this.realMinObjectiveValue2 = realMinObjectiveValue2;
    }

    private void setRealMaxObjectiveValue3(Double realMaxObjectiveValue3) {
        this.realMaxObjectiveValue3 = realMaxObjectiveValue3;
    }

    private void setRealMinObjectiveValue3(Double realMinObjectiveValue3) {
        this.realMinObjectiveValue3 = realMinObjectiveValue3;
    }

    private void setRealMaxObjectiveValue4(Double realMaxObjectiveValue4) {
        this.realMaxObjectiveValue4 = realMaxObjectiveValue4;
    }

    private void setRealMinObjectiveValue4(Double realMinObjectiveValue4) {
        this.realMinObjectiveValue4 = realMinObjectiveValue4;
    }

    private void setRealMaxObjectiveValue5(Double realMaxObjectiveValue5) {
        this.realMaxObjectiveValue5 = realMaxObjectiveValue5;
    }

    private void setRealMinObjectiveValue5(Double realMinObjectiveValue5) {
        this.realMinObjectiveValue5 = realMinObjectiveValue5;
    }

	public void setGenerateCsvFile(Boolean generateCsvFile) {
		this.generateCsvFile = generateCsvFile;
	}

	public void setTotalDifferentChromosomes(Integer totalDifferentChromosomes) {
		this.totalDifferentChromosomes = totalDifferentChromosomes;
	}

	public void setMaximumAttemptsGenerateDifferentChromosomes(Integer maximumAttemptsGenerateDifferentChromosomes) {
		this.maximumAttemptsGenerateDifferentChromosomes = maximumAttemptsGenerateDifferentChromosomes;
	}

	private void readConfiguration(String fileNameForDataNormalization) throws Exception {
		try (BufferedReader buffer = new BufferedReader(new FileReader(new File(Constants.READ_ME_FILE_NAME)))) {
			String line = null;

			while ((line = buffer.readLine()) != null) {
				if (line.startsWith("#")) continue;

				//0 - FieldName
				//1 - Type (int, double, String)
				//2 - Value
				String[] vector = line.split(":");
				Method method = this.getClass().getDeclaredMethod(getMethodName(vector[0]), getParameterType(vector[1]));
				method.invoke(this, getConvertedValue(vector[2], vector[1]));
			}

			if (getAlgorithmType() != AlgorithmType.SINGLE_OBJECTIVE && getSortFunctionType() != SortFunctionType.WEIGHT) {
			    if (fileNameForDataNormalization == null) {
			        readConfigurationForDataNormalization();
			    } else {
			        readConfigurationForDataNormalizationByFileName(fileNameForDataNormalization);
			    }
				
				if (getMaximizationConstant() != Constants.MAXIMIZATION_PROBLEM) {
					invertMaximumAndMinimumObjectiveValues();
				}
			}
		} catch (Exception e) {
			Exception e2 = new Exception("Error loading README.conf configuration file: " + e.getMessage());
			e2.initCause(e);
			throw e2;
		}
	}

	private void readConfigurationForDataNormalization() throws Exception {
		String fileName;

		if (Constants.USE_DEFAULT_GRAPH.equals(getTaskGraphFileName())) {
			fileName = Constants.NORMALIZATION_BASE_FILE_NAME;
		} else {
			fileName = getTaskGraphFileName().split(".stg")[0] + Constants.SUFIX_NORMLIZATION_FILE_NAME + ".txt";
		} 

		Class cls = null;
		String packagePath = null;

		if (Constants.USE_DEFAULT_GRAPH.equals(getTaskGraphFileName()) || isGraphWithCommunicationCost()) {
		    cls = LoaderNormalizationWithCost.class;
		    packagePath = Constants.PACKAGE_NORMALIZATION_WITH_COST;
		} else {
		    cls = LoaderNormalizationWithoutCost.class;
		    packagePath = Constants.PACKAGE_NORMALIZATION_WITHOUT_COST;
		}

		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(cls.getClassLoader().getResourceAsStream(packagePath + fileName)))) {
			String line = null;

			while ((line = buffer.readLine()) != null) {
				if (line.startsWith("#")) continue;

				//0 - FieldName
				//1 - Type (int, double, String)
				//2 - Value
				String[] vector = line.split(":");
				Method method = this.getClass().getDeclaredMethod(getMethodName(vector[0]), getParameterType(vector[1]));
				method.invoke(this, getConvertedValue(vector[2], vector[1]));
			}
		} catch (Exception e) {
			Exception e2 = new Exception("Error loading " + fileName + " file: " + e.getMessage());
			e2.initCause(e);
			throw e2;
		}
	}

   private void readConfigurationForDataNormalizationByFileName(String fileNameForDataNormalization) throws Exception {

       Class<LoaderNormalizationWithCostBackup> cls = LoaderNormalizationWithCostBackup.class;
	   String packagePath = Constants.PACKAGE_NORMALIZATION_WITH_COST_BACKUP;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(cls.getClassLoader().getResourceAsStream(packagePath + fileNameForDataNormalization)))) {
            String line;

            while ((line = buffer.readLine()) != null) {
                if (line.startsWith("#")) continue;

                //0 - FieldName
                //1 - Type (int, double, String)
                //2 - Value
                String[] vector = line.split(":");
                Method method = this.getClass().getDeclaredMethod(getMethodName(vector[0]), getParameterType(vector[1]));
                method.invoke(this, getConvertedValue(vector[2], vector[1]));
            }
        } catch (Exception e) {
            Exception e2 = new Exception("Error loading " + fileNameForDataNormalization + " file: " + e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }

	private void invertMaximumAndMinimumObjectiveValues() {
		double aux = maxObjectiveValue1;
		this.maxObjectiveValue1 = minObjectiveValue1;
		this.minObjectiveValue1 = aux;

		aux = maxObjectiveValue2;
		this.maxObjectiveValue2 = minObjectiveValue2;
		this.minObjectiveValue2 = aux;

		aux = maxObjectiveValue3;
		this.maxObjectiveValue3 = minObjectiveValue3;
		this.minObjectiveValue3 = aux;

		aux = maxObjectiveValue4;
		this.maxObjectiveValue4 = minObjectiveValue4;
		this.minObjectiveValue4 = aux;

		aux = maxObjectiveValue5;
		this.maxObjectiveValue5 = minObjectiveValue5;
		this.minObjectiveValue5 = aux;
	}

	private String getMethodName(String fieldName) {
		return "set" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1); 
	}

	private Class<?> getParameterType(String type) {
		switch (type) {
		case "int":
			return Integer.class;

		case "double":
			return Double.class;

		case "boolean":
			return Boolean.class;

		case "string":
			return String.class;

		default:
			throw new IllegalArgumentException("Invalid type of field: " + type + "!");
		}
	}

	private Object getConvertedValue(String value, String type) {
		switch (type) {
		case "int":
			return Integer.parseInt(value);

		case "double":
			return Double.parseDouble(value);

		case "boolean":
			return Boolean.parseBoolean(value);

		case "string":
			return value; 

		default:
			throw new IllegalArgumentException("Invalid type of field: " + type + "!");
		}
	}

	public double getTransformedObjectiveValue(double objectiveValue) {
		if (getMaximizationConstant() == Constants.MAXIMIZATION_PROBLEM) {
			return objectiveValue;
		}

		//Transform a minimization problem into a maximization problem 
		return objectiveValue > 0.0 ? getMaximizationConstant() / (double) objectiveValue : 0.0;
	}

	public static void main(String args[]) throws Exception {
		//Configuration config = new Configuration();
		System.out.println("Teste");
		
		System.out.println(Arrays.asList(0, 1, 2).toString());
	}
}