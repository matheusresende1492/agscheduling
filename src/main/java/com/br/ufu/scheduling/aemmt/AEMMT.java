package com.br.ufu.scheduling.aemmt;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.br.ufu.scheduling.agmo.Table;
import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.DataForSpreadsheet;
import com.br.ufu.scheduling.model.Graph;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Crossover;
import com.br.ufu.scheduling.utils.Printer;

public class AEMMT {
	private Random generator;
	private Configuration config;
	private Graph graph;
    private Map<String, DataForSpreadsheet> mapDataForSpreadsheet = new LinkedHashMap<>();
    private BufferedWriter finalResultWriterForSpreadsheet = null;

	private List<Table> tables = new ArrayList<>();
	private List<Chromosome> chromosomeInitialList = new ArrayList<>();

	private Table table1ForDoubleTournament;
	private Table table2ForDoubleTournament;

	private int initialPopulation;
	private int nonDominatedTableIndex;
	private int generationAccumulated;
	private int generationAccumulatedForResetTableScore;
	private int generationAccumulatedForApplyMutation;

	public AEMMT(Configuration config, Graph graph, Random generator) throws Exception {
		this.config = config;
		this.graph = graph;
		this.generator = generator;

		createTables();

		initialPopulation = tables.size() * config.getSizeOfTables();
		nonDominatedTableIndex = tables.size() - 1;
	}

	private void createTables() {
		if (config.getTotalObjectives() < 2 || config.getTotalObjectives() > 5) {
			throw new IllegalArgumentException("Invalid value to work with multi-objective algorithm. Valid values between 2 and 5.");
		}

		switch (config.getTotalObjectives()) {
		case 2:
			createTable(1);
			createTable(2);

			createTable(Arrays.asList(1, 2));
			createNonDominatedTable(Arrays.asList(1, 2));
			break;

		case 3:
			createTable(1);
			createTable(2);
			createTable(3);

			createTable(Arrays.asList(1, 2));
			createTable(Arrays.asList(1, 3));
			createTable(Arrays.asList(2, 3));

			createTable(Arrays.asList(1, 2, 3));
			createNonDominatedTable(Arrays.asList(1, 2, 3));
			break;

		case 4:
			createTable(1);
			createTable(2);
			createTable(3);
			createTable(4);

			createTable(Arrays.asList(1, 2));
			createTable(Arrays.asList(1, 3));
			createTable(Arrays.asList(1, 4));
			createTable(Arrays.asList(2, 3));
			createTable(Arrays.asList(2, 4));
			createTable(Arrays.asList(3, 4));

			createTable(Arrays.asList(1, 2, 3));
			createTable(Arrays.asList(1, 2, 4));
			createTable(Arrays.asList(1, 3, 4));
			createTable(Arrays.asList(2, 3, 4));

			createTable(Arrays.asList(1, 2, 3, 4));
			createNonDominatedTable(Arrays.asList(1, 2, 3, 4));
			break;

		case 5:
			createTable(1);
			createTable(2);
			createTable(3);
			createTable(4);
			createTable(5);

			createTable(Arrays.asList(1, 2));
			createTable(Arrays.asList(1, 3));
			createTable(Arrays.asList(1, 4));
			createTable(Arrays.asList(1, 5));
			createTable(Arrays.asList(2, 3));
			createTable(Arrays.asList(2, 4));
			createTable(Arrays.asList(2, 5));
			createTable(Arrays.asList(3, 4));
			createTable(Arrays.asList(3, 5));
			createTable(Arrays.asList(4, 5));

			createTable(Arrays.asList(1, 2, 3));
			createTable(Arrays.asList(1, 2, 4));
			createTable(Arrays.asList(1, 2, 5));
			createTable(Arrays.asList(1, 3, 4));
			createTable(Arrays.asList(1, 3, 5));
			createTable(Arrays.asList(1, 4, 5));
			createTable(Arrays.asList(2, 3, 4));
			createTable(Arrays.asList(2, 3, 5));
			createTable(Arrays.asList(2, 4, 5));
			createTable(Arrays.asList(3, 4, 5));

			createTable(Arrays.asList(1, 2, 3, 4));
			createTable(Arrays.asList(1, 2, 3, 5));
			createTable(Arrays.asList(1, 2, 4, 5));
			createTable(Arrays.asList(1, 3, 4, 5));
			createTable(Arrays.asList(2, 3, 4, 5));

			createTable(Arrays.asList(1, 2, 3, 4, 5));
			createNonDominatedTable(Arrays.asList(1, 2, 3, 4, 5));
			break;

		default:
			throw new IllegalArgumentException("Invalid value to work with multi-objective algorithm. Valid values between 2 and 5.");
		}
	}

	private void createNonDominatedTable(List<Integer> objectives) {
		Table table = new TableAEMMT(config.getSizeOfNonDominatedTable(), true);
		table.addObjectives(objectives);
		tables.add(table);
	}

	private void createTable(Integer objective) {
		Table table = new TableAEMMT(config.getSizeOfTables());
		table.addObjective(objective);
		tables.add(table);
	}

	private void createTable(List<Integer> objectives) {
		Table table = new TableAEMMT(config.getSizeOfTables());
		table.addObjectives(objectives);
		tables.add(table);
	}

    public Map<String, DataForSpreadsheet> executeForSpreadsheet(long initialTime, BufferedWriter finalResultWriter) throws Exception {
        this.finalResultWriterForSpreadsheet = finalResultWriter;

        execute(initialTime);

        return mapDataForSpreadsheet;
    }

	public void execute(long initialTime) throws Exception {
		initialize();

		while (generationAccumulated < config.getTotalGenerations()) {
			resetTableScore();

			if (config.isPrintIterations()) {
				System.out.println("############################\n");
				System.out.println("####### GENERATION: " + (generationAccumulated + 1) + " #######\n");
				System.out.println("############################\n");
			}

			executeAG();

			finalizeGeneration();
		}

		showResult(initialTime);
	}

	private void initialize() throws Exception {
		generationAccumulated = 0;
		generationAccumulatedForResetTableScore = 0;
		generationAccumulatedForApplyMutation = 0;

		if (config.isCalculateMaximusAndMinimusForNormalization()) {
			calculateMaxMinObjectivesValues();
		}

		generateInitialPopulation();
	}

	   /**
     * Used to find the highs and lows of the objectives. 
     * */
    private void calculateMaxMinObjectivesValues() throws Exception {
        double maxObjectiveValue1 = 0.0; 
        double minObjectiveValue1 = 0.0;

        double maxObjectiveValue2 = 0.0;
        double minObjectiveValue2 = 0.0;

        double maxObjectiveValue3 = 0.0;
        double minObjectiveValue3 = 0.0;

        double maxObjectiveValue4 = 0.0;
        double minObjectiveValue4 = 0.0;

        double maxObjectiveValue5 = 0.0;
        double minObjectiveValue5 = 0.0;

        int totalExecutions = 10;
        System.out.println("Total runs for calculation: " + totalExecutions);
        System.out.println();
        
        for (int i = 0; i < totalExecutions; i++) {
            System.out.println("Run: " + (i + 1));

            initialPopulation = config.getTotalGenerations();
            chromosomeInitialList.clear();
            generateInitialPopulation();

            for (int j = 0; j < chromosomeInitialList.size(); j++) {
                Chromosome chromosome = chromosomeInitialList.get(j);

                if (i == 0 && j == 0) {
                    maxObjectiveValue1 = minObjectiveValue1 = chromosome.getSLength();
                    maxObjectiveValue2 = minObjectiveValue2 = chromosome.getLoadBalance();
                    maxObjectiveValue3 = minObjectiveValue3 = chromosome.getFlowTime();
                    maxObjectiveValue4 = minObjectiveValue4 = chromosome.getCommunicationCost();
                    maxObjectiveValue5 = minObjectiveValue5 = chromosome.getWaitingTime();
                } else {
                    if (chromosome.getSLength() > maxObjectiveValue1) maxObjectiveValue1 = chromosome.getSLength();
                    if (chromosome.getSLength() < minObjectiveValue1) minObjectiveValue1 = chromosome.getSLength();
                    
                    if (chromosome.getLoadBalance() > maxObjectiveValue2) maxObjectiveValue2 = chromosome.getLoadBalance();
                    if (chromosome.getLoadBalance() < minObjectiveValue2) minObjectiveValue2 = chromosome.getLoadBalance();
                    
                    if (chromosome.getFlowTime() > maxObjectiveValue3) maxObjectiveValue3 = chromosome.getFlowTime();
                    if (chromosome.getFlowTime() < minObjectiveValue3) minObjectiveValue3 = chromosome.getFlowTime();
                    
                    if (chromosome.getCommunicationCost() > maxObjectiveValue4) maxObjectiveValue4 = chromosome.getCommunicationCost();
                    if (chromosome.getCommunicationCost() < minObjectiveValue4) minObjectiveValue4 = chromosome.getCommunicationCost();
                    
                    if (chromosome.getWaitingTime() > maxObjectiveValue5) maxObjectiveValue5 = chromosome.getWaitingTime();
                    if (chromosome.getWaitingTime() < minObjectiveValue5) minObjectiveValue5 = chromosome.getWaitingTime();
                }
            }
        }

        System.out.println();
        System.out.println("Slenght -> Max : " + maxObjectiveValue1 + " | Min: " + minObjectiveValue1);
        System.out.println("LoadBalance -> Max : " + maxObjectiveValue2 + " | Min: " + minObjectiveValue2);
        System.out.println("FlowTime -> Max : " + maxObjectiveValue3 + " | Min: " + minObjectiveValue3);
        System.out.println("CommunicationCost -> Max : " + maxObjectiveValue4 + " | Min: " + minObjectiveValue4);
        System.out.println("WaitingTime -> Max : " + maxObjectiveValue5 + " | Min: " + minObjectiveValue5);

        System.exit(0);
    }

	private void generateInitialPopulation() throws Exception {
		for (int i = 1; i <= initialPopulation; i++) {
			Chromosome chromosome = new Chromosome(generator, graph, config);

			if (!chromosomeInitialList.contains(chromosome)) {
				addChromosomeToTables(chromosome);
				chromosomeInitialList.add(chromosome);
			}
		}

		addChromosomesFromInitialPopulationToNonDominatedTable(chromosomeInitialList);
	}

	private boolean addChromosomeToTables(Chromosome chromosome) throws Exception {
		boolean addedInSometable = false;

		//the non-dominated table will be processed separately
		for (int tableIndex = 0; tableIndex < tables.size() - 1; tableIndex++) {
			boolean added = tables.get(tableIndex).add(chromosome, config);

			if (!addedInSometable) {
				addedInSometable = added; 
			}
		}

		return addedInSometable;
	}

	private void addChromosomesFromInitialPopulationToNonDominatedTable(List<Chromosome> chromosomeInitialList) throws Exception {
		List<Chromosome> dominatedChromosomeList = new ArrayList<>();

		externalLoop:
		for (int chromosomeAIndex = 0; chromosomeAIndex < chromosomeInitialList.size(); chromosomeAIndex++) {
			Chromosome chromosomeA = chromosomeInitialList.get(chromosomeAIndex);

			if (dominatedChromosomeList.contains(chromosomeA)) {
				continue;
			}

			for (int chromosomeBIndex = 0; chromosomeBIndex < chromosomeInitialList.size(); chromosomeBIndex++) {
				if (chromosomeAIndex == chromosomeBIndex) {
					continue;
				}

				Chromosome chromosomeB = chromosomeInitialList.get(chromosomeBIndex);

				if (chromosomeB.isChromosomeDominated(config, chromosomeA)) {
					dominatedChromosomeList.add(chromosomeB);
				}

				if (chromosomeA.isChromosomeDominated(config, chromosomeB)) {
					continue externalLoop;
				}
			}

			addChromosomeToNonDominatedTable(chromosomeA);
		}
	}

	private void addChromosomeToNonDominatedTable(Chromosome chromosome) throws Exception {
		Table nonDominatedTable = tables.get(nonDominatedTableIndex);

		//Repeated individual is not allowed in the solution table
		if (nonDominatedTable.contains(chromosome)) {
		    return;
		}

		for (int chromosomeIndex = 0; chromosomeIndex < nonDominatedTable.getTotalChromosomes(); chromosomeIndex++) {
			Chromosome chromosomeB = nonDominatedTable.getChromosomeFromIndex(chromosomeIndex);

			if (chromosome.isChromosomeDominated(config, chromosomeB)) {
			    return;
			}
		}

		nonDominatedTable.removeChromosomeFromTable(chromosome, config);
		nonDominatedTable.add(chromosome, config);
	}

    private void resetTableScore() {
		if (config.getTotalGenerationsToResetTableScore() > 0 && generationAccumulatedForResetTableScore > config.getTotalGenerationsToResetTableScore()) {
			tables.forEach(table -> table.resetScore());
			generationAccumulatedForResetTableScore = 1;
		}
	}

	private void executeAG() throws Exception {
		processTablesForDoubleTournament();

		Chromosome child = processPairSelection();
		applyMutation(child);

		if (addChromosomeToTables(child)) {
			processTableScore();
			addChromosomeChildToNonDominatedTable(child);
		}
	}

	private void processTablesForDoubleTournament() {
		table1ForDoubleTournament = raffleTableByDoubleTournament();
		table2ForDoubleTournament = raffleTableByDoubleTournament();
	}

	private Table raffleTableByDoubleTournament() {
		Table table = null;

		for (int tour = 0; tour < Constants.DOUBLE_TOURNAMENT; tour++) {
			int raffleIndex = raffleIndex(tables.size());

			if (table == null || table.getScore() > tables.get(raffleIndex).getScore()) { 
				table = tables.get(raffleIndex);
			}
		}

		return table;
	}

	private int raffleIndex(int limit) {
		return generator.nextInt(limit);
	}

	private Chromosome processPairSelection() throws Exception {
		Chromosome parent1 = raflleChromosomeFromTable(table1ForDoubleTournament);
		Chromosome parent2 = raflleChromosomeFromTable(table2ForDoubleTournament);

		return getCrossoverChildren(parent1, parent2);
	}

	private Chromosome raflleChromosomeFromTable(Table table) {
		return table.getChromosomeFromIndex(raffleIndex(table.getTotalChromosomes()));
	}

	private Chromosome getCrossoverChildren(Chromosome parent1, Chromosome parent2) throws Exception {
		List<Chromosome> generatedChildren = Crossover.getCrossover(parent1, parent2, graph, generator, config);

		if (generatedChildren.size() == 0) {
			return generatedChildren.get(0);
		}

		return generatedChildren.get(raffleIndex(generatedChildren.size()));
	}

	private void applyMutation(Chromosome chromosome) throws Exception {
		if (config.getTotalGenerationsToApplyMutation() > 0 && generationAccumulatedForApplyMutation > config.getTotalGenerationsToApplyMutation()) {
			chromosome.applyMutation(generator, graph, config);
			generationAccumulatedForApplyMutation = 1;
		}		
	}

	private void processTableScore() {
		table1ForDoubleTournament.addScore(1);
		table2ForDoubleTournament.addScore(1);
	}

	private void addChromosomeChildToNonDominatedTable(Chromosome child) throws Exception {
		boolean chromosomeChildIsDominated = false;

		//the non-dominated table will be processed only at the end
		externalLoop:
		for (int tableIndex = 0; tableIndex < tables.size() - 1; tableIndex++) {
			Table table = tables.get(tableIndex);

			for (int chromosomeIndex = 0; chromosomeIndex < table.getTotalChromosomes(); chromosomeIndex++) {
				Chromosome chromosomeB = table.getChromosomeFromIndex(chromosomeIndex);

				if (child.isChromosomeDominated(config, chromosomeB)) {
					chromosomeChildIsDominated = true;
					break externalLoop;
				}
			}
		}

		if (!chromosomeChildIsDominated) {
			addChromosomeToNonDominatedTable(child);
		}
	}

	private void finalizeGeneration() {
		generationAccumulatedForResetTableScore++;
		generationAccumulatedForApplyMutation++;
		generationAccumulated++;
	}

	private void showResult(long initialTime) throws Exception {
        if (finalResultWriterForSpreadsheet != null) {
            Printer.printFinalResultForAEMMT(config, tables.get(nonDominatedTableIndex), initialTime, mapDataForSpreadsheet, finalResultWriterForSpreadsheet);
            return;
        }

		if (config.isPrintComparisonNonDominatedChromosomes()) {
			Printer.printFinalResultForAEMMTWithComparedToNonDominated(config, tables, initialTime);
		} else {
			Printer.printFinalResultForAEMMT(config, tables.get(nonDominatedTableIndex), initialTime, mapDataForSpreadsheet);
		}
	}
}