package com.br.ufu.scheduling.nsga.nsga2;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.DataForSpreadsheet;
import com.br.ufu.scheduling.model.Graph;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Crossover;
import com.br.ufu.scheduling.utils.Printer;
import com.br.ufu.scheduling.utils.Utils;

public class NSGAII {
    private Random generator;
    private Configuration config;
    private Graph graph;
    private Map<String, DataForSpreadsheet> mapDataForSpreadsheet = new LinkedHashMap<>();
    private BufferedWriter finalResultWriterForSpreadsheet = null;

    private List<Chromosome> chromosomeList = new ArrayList<>();
    private List<Chromosome> childrenList   = new ArrayList<>();

    private int generationAccumulated;

    public NSGAII(Configuration config, Graph graph, Random generator) throws Exception {
        this.config = config;
        this.graph = graph;
        this.generator = generator;
    }

    public Map<String, DataForSpreadsheet> executeForSpreadsheet(long initialTime, BufferedWriter finalResultWriter) throws Exception {
        this.finalResultWriterForSpreadsheet = finalResultWriter; 

        execute(initialTime);

        return mapDataForSpreadsheet;
    }

    public void execute(long initialTime) throws Exception {
        initialize();

        while (generationAccumulated < config.getTotalGenerations()) {
            if (config.isPrintIterations()) {
                System.out.println("############################\n");
                System.out.println("####### GENERATION: " + (generationAccumulated + 1) + " #######\n");
                System.out.println("############################\n");
            }

            executeAG();

            finalizeGeneration();
        }

        selectParetoBorder();
        showResult(initialTime);
    }

    private void initialize() throws Exception {
        generationAccumulated = 0;

        generateInitialPopulation();
        preparePopulation(chromosomeList);
    }

    private void generateInitialPopulation() throws Exception {
        int count = 0;
        while (count < config.getInitialPopulation()) {
            Chromosome chromosome = new Chromosome(generator, graph, config);

            if (!chromosomeList.contains(chromosome)) {
                addChromosomeInGeneralList(chromosome);
                count++;
            }
        }
    }

    private void addChromosomeInGeneralList(Chromosome chromosome) {
        chromosomeList.add(chromosome);
    }

    private void preparePopulation(List<Chromosome> chromosomeList) {
        calculateRank(chromosomeList);
        calculateCrowdingDistance(chromosomeList);
        chromosomeList.sort(Comparator.comparingInt(Chromosome::getRank));        
    }

    private void calculateRank(List<Chromosome> chromosomeList) {
        for (Chromosome chromosome : chromosomeList) {
            chromosome.reset();
        }

        for (int i = 0; i < chromosomeList.size() - 1; i++) {
            for (int j = i + 1; j < chromosomeList.size(); j++) {
                switch (dominates(chromosomeList.get(i), chromosomeList.get(j))) {
                    case Constants.NSGA2_DOMINANT:
                        chromosomeList.get(i).addDominatedChromosome(chromosomeList.get(j));
                        chromosomeList.get(j).incrementDominatedCount(1);
                        break;

                    case Constants.NSGA2_INFERIOR:
                        chromosomeList.get(i).incrementDominatedCount(1);
                        chromosomeList.get(j).addDominatedChromosome(chromosomeList.get(i));
                        break;

                    case Constants.NSGA2_NON_DOMINATED:
                        break;
                }
            }

            if (chromosomeList.get(i).getDominatedCount() == 0) {
                chromosomeList.get(i).setRank(1);
            }
        }

        Chromosome lastChromosome = chromosomeList.get(this.chromosomeList.size() - 1);
        if (lastChromosome.getDominatedCount() == 0) {
            lastChromosome.setRank(1);
        }

        while (Service.populaceHasUnsetRank(chromosomeList)) {
            chromosomeList.forEach(chromosome -> {
                if (chromosome.getRank() != -1) {
                    chromosome.getDominatedChromosomes().forEach(dominatedChromosome -> {
                        if (dominatedChromosome.getDominatedCount() > 0) {
                            dominatedChromosome.incrementDominatedCount(-1);

                            if (dominatedChromosome.getDominatedCount() == 0) {
                                dominatedChromosome.setRank(chromosome.getRank() + 1);
                            }
                        }
                    });
                }
            });
        }
    }

    public int dominates(Chromosome chromosome1, Chromosome chromosome2) {
        if (chromosome2.isChromosomeDominated(config, chromosome1)) {
            return Constants.NSGA2_DOMINANT;
        } 
        
        if (chromosome1.isChromosomeDominated(config, chromosome2)) {
            return Constants.NSGA2_INFERIOR;
        }

        return Constants.NSGA2_NON_DOMINATED;
    }

    private void calculateCrowdingDistance(List<Chromosome> chromosomeList) {
        int size = chromosomeList.size();

        for (int objectiveIndex = 1; objectiveIndex <= config.getTotalObjectives(); objectiveIndex++) {
            int realIndex = Utils.getActualObjectiveIndex(config, objectiveIndex); 
            int iFinal = realIndex;

            chromosomeList.sort(Collections.reverseOrder(Comparator.comparingDouble(c -> c.getObjectiveValue(iFinal))));

            Service.normalizeSortedObjectiveValues(chromosomeList, realIndex);

            chromosomeList.get(0).setCrowdingDistance(Double.MAX_VALUE);
            chromosomeList.get(chromosomeList.size() - 1).setCrowdingDistance(Double.MAX_VALUE);

            double maxNormalizedObjectiveValue = selectMaximumNormalizedObjectiveValue(realIndex, chromosomeList);
            double minNormalizedObjectiveValue = selectMinimumNormalizedObjectiveValue(realIndex, chromosomeList);

            for (int j = 1; j < size; j++)
                if (chromosomeList.get(j).getCrowdingDistance() < Double.MAX_VALUE) {
                    double previousChromosomeObjectiveValue = chromosomeList.get(j - 1).getNormalizedObjectiveValues().get(realIndex);
                    double nextChromosomeObjectiveValue = chromosomeList.get(j + 1).getNormalizedObjectiveValues().get(realIndex);
                    double objectiveDifference = nextChromosomeObjectiveValue - previousChromosomeObjectiveValue;
                    double minMaxDifference = maxNormalizedObjectiveValue - minNormalizedObjectiveValue;

                    chromosomeList.get(j).setCrowdingDistance(
                            Service.roundOff(
                                    chromosomeList.get(j).getCrowdingDistance() +
                                            (objectiveDifference / minMaxDifference),
                                    4));
                }
        }
    }

    private double selectMaximumNormalizedObjectiveValue(int objectiveIndex, List<Chromosome> chromosomeList) {
        double result = chromosomeList.get(0).getNormalizedObjectiveValues().get(objectiveIndex);

        for (Chromosome chromosome : chromosomeList) {
            if (chromosome.getNormalizedObjectiveValues().get(objectiveIndex) > result) {
                result = chromosome.getNormalizedObjectiveValues().get(objectiveIndex);
            }
        }

        return result;
    }

    public double selectMinimumNormalizedObjectiveValue(int objectiveIndex, List<Chromosome> chromosomeList) {
        double result = chromosomeList.get(0).getNormalizedObjectiveValues().get(objectiveIndex);

        for (Chromosome chromosome : chromosomeList) {
            if (chromosome.getNormalizedObjectiveValues().get(objectiveIndex) < result) {
                result = chromosome.getNormalizedObjectiveValues().get(objectiveIndex);
            }
        }

        return result;
    }

    private void executeAG() throws Exception {
        executeSelection();
        preparePopulation(childrenList);

        selectBestChromosomesForReinsertion();
    }

    private void executeSelection() throws Exception {
        for (int pair = 0; pair < config.getInitialPopulation() / 2; pair++) {
            processPairSelection();
        }

        applyMutationOnChildren();
    }

    private void processPairSelection() throws Exception {
        Chromosome parent1 = raffleChromosomeByTournament(new ArrayList<>(chromosomeList), null);
        Chromosome parent2 = raffleChromosomeByTournament(new ArrayList<>(chromosomeList), parent1);

        selectChildren(parent1, parent2);
    }

    private Chromosome raffleChromosomeByTournament(List<Chromosome> copyOfChromosomeList, Chromosome chromosomeAlreadyChosen) {
        Chromosome chromosome = null;

        //If it's the first individual of the pair to be chosen, I'll raffle anyone
        //For the second individual of the pair, we will try x times until an individual different from the first is drawn, or we will use a repeated one even
        if (chromosomeAlreadyChosen == null) {
            chromosome = raffleChromosomeByTournament(new ArrayList<Chromosome>(copyOfChromosomeList));

        } else {
            int currentAttemptSelectParentNotRepeated = 0;

            while (currentAttemptSelectParentNotRepeated < config.getAttemptSelectParentNotRepeated() && (chromosome == null || chromosomeAlreadyChosen.equals(chromosome))) {
                chromosome = raffleChromosomeByTournament(new ArrayList<Chromosome>(copyOfChromosomeList));

                currentAttemptSelectParentNotRepeated++;
            }
        }

        return chromosome;
    }

    private Chromosome raffleChromosomeByTournament(List<Chromosome> copyOfChromosomeList) {
        Chromosome chromosome = null;

        for (int tour = 0; tour < Constants.DOUBLE_TOURNAMENT; tour++) {
            int chromosomeRaffledIndex = raffleChromosomeIndexByTournament();

            if (chromosome == null) { 
                chromosome = copyOfChromosomeList.get(chromosomeRaffledIndex);
            } else {
                chromosome = getBestChromosome(chromosome, copyOfChromosomeList.get(chromosomeRaffledIndex));
            }
        }

        return chromosome;
    }

    private Chromosome getBestChromosome(Chromosome parent1, Chromosome parent2) {
        if (parent1.getRank() < parent2.getRank()) {
            return parent1;

        } else if (parent1.getRank() == parent2.getRank()) {
            if (parent1.getCrowdingDistance() > parent2.getCrowdingDistance()) {
                return parent1;

            } else if (parent1.getCrowdingDistance() < parent2.getCrowdingDistance()) {
                return parent2;

            } else {
                return generator.nextBoolean() ? parent1 : parent2;
            }
        } else {
            return parent2;
        }
    }

    private int raffleChromosomeIndexByTournament() {
        return raffleIndex(config.getInitialPopulation());
    }

    private int raffleIndex(int limit) {
        return generator.nextInt(limit);
    }

    private void selectChildren(Chromosome parent1, Chromosome parent2) throws Exception {
        childrenList.addAll(getCrossoverChildren(parent1, parent2));
    }

    private List<Chromosome> getCrossoverChildren(Chromosome parent1, Chromosome parent2) throws Exception {
        List<Chromosome> generatedChildren = Crossover.getCrossover(parent1, parent2, graph, generator, config);

        //If the crossover was executed that generates only one child, I must execute it again, 
        //because we need to produce two children for each pair of parents
        if (generatedChildren.size() == 1) {
            generatedChildren.addAll(Crossover.getOrderCrossover(parent1, parent2, graph, generator, config));
        }

        return generatedChildren;
    }

    private void applyMutationOnChildren() throws Exception {
        List<Integer> raffledIndexList = new ArrayList<>();

        for (int mutatedChromosomeIndex = 0; mutatedChromosomeIndex < getNumberOfChromosomesMutated(); mutatedChromosomeIndex++) {
            int raffledIndex = -1;

            if (!config.isAllowApplyingMutationOnRepeatedChild()) {
                do {
                    raffledIndex = raffleIndex(childrenList.size());
                } while(raffledIndexList.contains(raffledIndex));

                raffledIndexList.add(raffledIndex);
            } else {
                raffledIndex = raffleIndex(childrenList.size());
            }

            applyMutation(childrenList.get(raffledIndex));
        }
    }

    private int getNumberOfChromosomesMutated() {
        return (int) Math.ceil(config.getInitialPopulation() * config.getMutationRate() / 100);
    }

    private void applyMutation(Chromosome chromosome) throws Exception {
        chromosome.applyMutation(generator, graph, config);
    }

    private void selectBestChromosomesForReinsertion() throws Exception {
        List<Chromosome> combinedPopulation = new ArrayList<>();
        combinedPopulation.addAll(chromosomeList);
        combinedPopulation.addAll(childrenList);

        int lastFrontToConsider = combinedPopulation.get(config.getInitialPopulation() - 1).getRank();
        List<Chromosome> newPopulation = new ArrayList<>();

        if (combinedPopulation.get(config.getInitialPopulation()).getRank() == lastFrontToConsider) {
            Service.sortFrontWithCrowdingDistance(combinedPopulation, lastFrontToConsider);
        }

        for (int i = 0; i < config.getInitialPopulation(); i++) {
            newPopulation.add(combinedPopulation.get(i));
        }

        chromosomeList = new ArrayList<>(newPopulation);

        if (chromosomeList.size() != config.getInitialPopulation()) {
            throw new Exception("Invalid population size.");
        }
    }

    private void finalizeGeneration() {
        generationAccumulated++;
        childrenList.clear();
    }

    private void selectParetoBorder() {
        int totalChromosomes = chromosomeList.size();
        int currentIndex = 0;

        while (currentIndex < totalChromosomes) {
            if (chromosomeList.get(currentIndex).getRank() != Constants.RANK_PARETO_BORDER) {
                chromosomeList.remove(currentIndex);
                totalChromosomes--;

                continue;
            }

            currentIndex++;
        }

        chromosomeList.sort(Comparator.comparingDouble(Chromosome::getCrowdingDistance));
    }

    private void showResult(long initialTime) throws Exception {
        if (finalResultWriterForSpreadsheet != null) {
            Printer.printFinalResultForNSGA2(config, chromosomeList, initialTime, mapDataForSpreadsheet, finalResultWriterForSpreadsheet);
            return;
        }

        if (config.isPrintComparisonNonDominatedChromosomes()) {
            Printer.printFinalResultForNSGA2WithComparedToNonDominated(config, chromosomeList, initialTime);
        } else {
            Printer.printFinalResultForNSGA2(config, chromosomeList, initialTime, mapDataForSpreadsheet);
        }
    }
}