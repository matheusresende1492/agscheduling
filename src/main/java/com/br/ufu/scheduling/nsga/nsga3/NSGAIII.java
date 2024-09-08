package com.br.ufu.scheduling.nsga.nsga3;

import java.io.BufferedWriter;
import java.util.*;

import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.DataForSpreadsheet;
import com.br.ufu.scheduling.model.Graph;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Crossover;
import com.br.ufu.scheduling.utils.Printer;

public class NSGAIII {
    private Random generator;
    private Configuration config;
    private Graph graph;
    private Map<String, DataForSpreadsheet> mapDataForSpreadsheet = new LinkedHashMap<>();
    private BufferedWriter finalResultWriterForSpreadsheet = null;

    private final ServiceNSGAIII serviceNSGAIII;

    private final NonDominatedSorting sortingUtil;

    private List<Chromosome> chromosomeList = new ArrayList<>();
    private List<Chromosome> childrenList   = new ArrayList<>();

    private int generationAccumulated;

    public NSGAIII(Configuration config, Graph graph, Random generator) throws Exception {
        this.config = config;
        this.graph = graph;
        this.generator = generator;
        this.serviceNSGAIII = new ServiceNSGAIII(config);
        this.sortingUtil = new NonDominatedSorting(config);
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
        //calculate/set/sort rank
        sortingUtil.compute(chromosomeList);
        chromosomeList.sort(Comparator.comparingInt(Chromosome::getRank));
    }

    private void executeAG() throws Exception {
        executeSelection();


        preparePopulation(childrenList);

        //front f - 1
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

        List<List<Chromosome>> combinedFronts = sortingUtil.compute(combinedPopulation);

        List<Chromosome> last = new ArrayList<>();
        List<Chromosome> pop = new ArrayList<>();
        List<List<Chromosome>> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < config.getInitialPopulation()) {
            last = combinedFronts.get(rankingIndex);
            fronts.add(last);
            candidateSolutions += last.size();
            if ((pop.size() + last.size()) <= config.getInitialPopulation())
                pop.addAll(last);
            rankingIndex++;
        }

        if (pop.size() == config.getInitialPopulation()){
            chromosomeList = new ArrayList<>(pop);
        }

      List<Chromosome> chosen = serviceNSGAIII.environmentSelection(
                fronts,
                last,
                config.getInitialPopulation() - pop.size(),
                config.getTotalObjectives());

        pop.addAll(chosen);
        chromosomeList = new ArrayList<>(pop);

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
    }

    private void showResult(long initialTime) throws Exception {
        if (finalResultWriterForSpreadsheet != null) {
            Printer.printFinalResultForNSGA3(config, chromosomeList, initialTime, mapDataForSpreadsheet, finalResultWriterForSpreadsheet);
            return;
        }

        if (config.isPrintComparisonNonDominatedChromosomes()) {
            Printer.printFinalResultForNSGA3WithComparedToNonDominated(config, chromosomeList, initialTime);
        } else {
            Printer.printFinalResultForNSGA3(config, chromosomeList, initialTime, mapDataForSpreadsheet);
        }
    }
}