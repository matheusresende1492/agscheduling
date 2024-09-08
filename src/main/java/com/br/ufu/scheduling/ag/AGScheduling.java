package com.br.ufu.scheduling.ag;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.br.ufu.scheduling.aemmd.AEMMD;
import com.br.ufu.scheduling.aemmt.AEMMT;
import com.br.ufu.scheduling.enums.SelectionType;
import com.br.ufu.scheduling.file.csv.GeneratorDifferentChromosome;
import com.br.ufu.scheduling.model.BestResultByObjective;
import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.DataForSpreadsheet;
import com.br.ufu.scheduling.model.FinalResultModel;
import com.br.ufu.scheduling.model.Graph;
import com.br.ufu.scheduling.nsga.nsga2.NSGAII;
import com.br.ufu.scheduling.nsga.nsga3.NSGAIII;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Crossover;
import com.br.ufu.scheduling.utils.Constants;

public class AGScheduling {
    private List<Chromosome> chromosomeList = new ArrayList<>();
    private List<Chromosome> parentList = new ArrayList<>();
    private List<Chromosome> childrenList = new ArrayList<>();

    private Random generator;
    private Configuration config;
    private Graph graph;
    private int[] roulette;
    private int totalChromosomeScoreForSorting;
    private Chromosome bestChromosomeFound;
    private BestResultByObjective bestResult;
    private BufferedWriter finalResultWriterForSpreadsheet = null;

    private boolean firstGeneration = true;
    private boolean findBestChromosomeInGeneration = false;

    // Variables for the average calculation
    private int totalSuccess;
    private double totalSLengthOfBestChromosomes;
    private double totalLoadBalanceOfBestChromosomes;
    private double totalFlowTimeOfBestChromosomes;
    private double totalCommunicationCostOfBestChromosomes;
    private double totalWaitingTimeOfBestChromosomes;
    private double totalFitnessOfBestChromosomes;
    private int totalNumberOfChromosomes;

    public AGScheduling() throws Exception {
        this(new Configuration());
    }

    public AGScheduling(Configuration config) throws Exception {
        this.config = config;
        bestResult = new BestResultByObjective(this.config);

        if (config.getSeed() == Constants.RANDOM_SEED) {
            generator = new Random();
        } else {
            generator = new Random(this.config.getSeed());
        }

        if (Constants.USE_DEFAULT_GRAPH.equals(this.config.getTaskGraphFileName())) {
            graph = Graph.initializeGraph();
        } else {
            graph = Graph.initializeGraph(this.config);
        }
    }

    public AGScheduling(Configuration config, BufferedWriter finalResultWriter) throws Exception {
        this(config);
        this.finalResultWriterForSpreadsheet = finalResultWriter;
    }

    public void execute(long initialTime) throws Exception {
        if (config.isGenerateCsvFile()) {
            executeAGAndGenerateCsvFile();
            return;
        }

        switch (config.getAlgorithmType()) {
            case SINGLE_OBJECTIVE:
                executeStandarGeneticAlgorithm(initialTime);
                break;

            case NSGAII:
                executeMultiObjectiveGeneticAlgorithmNSGA2(initialTime);
                break;

            case SPEAII:
                throw new IllegalArgumentException("Algoritm type not implemented. SPEA2");

            case AEMMT:
                executeMultiObjectiveGeneticAlgorithmAEMMT(initialTime);
                break;

            case AEMMD:
                executeMultiObjectiveGeneticAlgorithmAEMMD(initialTime);
                break;

            default:
                throw new IllegalArgumentException("Algoritm type not implemented.");
        }
    }

    private void executeAGAndGenerateCsvFile() throws Exception {
        GeneratorDifferentChromosome generator = new GeneratorDifferentChromosome(config, graph, this.generator);
        generator.execute();
    }

    private void executeMultiObjectiveGeneticAlgorithmNSGA2(long initialTime) throws Exception {
        NSGAII nsga2 = new NSGAII(config, graph, generator);
        nsga2.execute(initialTime);
    }

    private void executeMultiObjectiveGeneticAlgorithmAEMMT(long initialTime) throws Exception {
        AEMMT aemmt = new AEMMT(config, graph, generator);
        aemmt.execute(initialTime);
    }

    private void executeMultiObjectiveGeneticAlgorithmAEMMD(long initialTime) throws Exception {
        AEMMD aemmd = new AEMMD(config, graph, generator);
        aemmd.execute(initialTime);
    }

    private void executeStandarGeneticAlgorithm(long initialTime) throws Exception {
        int iteration = 0;

        while (iteration < config.getIterations()) {
            if (config.isPrintIterations()) {
                System.out.println("############################\n");
                System.out.println("####### ITERATION: " + (iteration + 1) + " #######\n");
                System.out.println("############################\n");
            }

            resetIteration();

            int generation = 0;

            while (generation < config.getGenerations()
                    && !(config.isStopGenerationIfFindBestSolution() && findBestChromosomeInGeneration)) {
                if (config.isPrintGenerations()) {
                    System.out.println("##### GENERATION: " + (generation + 1) + " #####\n");
                }

                resetGeneration();
                executeAG();
                processGenerationResult();

                generation++;
            }

            processIterationResult();
            populateAverageDataOfBestChromosomes(chromosomeList.get(Constants.INDEX_BEST_CHROMOSOME));

            iteration++;
        }

        showResult(initialTime);
    }

    private void resetIteration() {
        firstGeneration = true;
        chromosomeList.clear();
    }

    private void resetGeneration() {
        roulette = null;
        parentList.clear();
        childrenList.clear();
        findBestChromosomeInGeneration = false;
    }

    private void executeAG() throws Exception {
        if (firstGeneration) {
            for (int i = 0; i < config.getInitialPopulation(); i++) {
                Chromosome chromosome = new Chromosome(generator, graph, config);
                addChromosomeInGeneralList(chromosome);
            }

            firstGeneration = false;
        }

        executeSelection();
        selectBestChromosomesForReinsertion();
    }

    private void addChromosomeInGeneralList(Chromosome chromosome) {
        chromosomeList.add(chromosome);
        bestResult.verifyBestAndWorstSolutions(chromosome);
    }

    private void addChromosomeInGeneralList(List<Chromosome> chromosomeList) {
        this.chromosomeList.addAll(chromosomeList);
        bestResult.verifyBestAndWorstSolutions(chromosomeList);
    }

    private void executeSelection() throws Exception {
        for (int pair = 0; pair < getNumberOfChromosomesForSelection(); pair++) {
            processPairSelection();
        }

        applyMutationOnChildren();
    }

    private int getNumberOfChromosomesForSelection() {
        return (int) (config.getInitialPopulation() * config.getCrossoverRate() / 100 / 2);
    }

    private void processPairSelection() throws Exception {
        Chromosome parent1 = null;
        Chromosome parent2 = null;

        switch (config.getSelectionType()) {
            case ROULETTE:
            case LINEAR_RANKING:
            case NON_LINEAR_RANKING:
                parent1 = raffleChromosomeByRoulette(null);
                parent2 = raffleChromosomeByRoulette(parent1);
                break;

            case SIMPLE_TOURNAMENT:
            case STOCHASTIC_TOURNAMENT:
                parent1 = raffleChromosomeByTournament(new ArrayList<>(chromosomeList), null);
                parent2 = raffleChromosomeByTournament(new ArrayList<>(chromosomeList), parent1);
                break;

            default:
                throw new IllegalArgumentException("Selection type not implemented.");
        }

        parentList.add(parent1);
        parentList.add(parent2);

        selectChildren(parent1, parent2);
    }

    private Chromosome raffleChromosomeByRoulette(Chromosome chromosomeAlreadyChosen) {
        Chromosome chromosome = null;

        if (roulette == null) {
            switch (config.getSelectionType()) {
                case LINEAR_RANKING:
                    totalChromosomeScoreForSorting = getTotalPositionsForLinearRanking();
                    roulette = populateRouletteForRanking(false);
                    break;

                case NON_LINEAR_RANKING:
                    totalChromosomeScoreForSorting = getTotalPositionsForNonLinearRanking();
                    roulette = populateRouletteForRanking(true);
                    break;

                default:
                    totalChromosomeScoreForSorting = getTotalChromosomeScoreForSorting();
                    roulette = populateRoulette();
                    break;
            }
        }

        // If it's the first individual of the pair to be chosen, I'll raffle anyone
        // For the second individual of the pair, we will try x times until an
        // individual different from the first is drawn, or we will use a repeated one
        // even
        if (chromosomeAlreadyChosen == null) {
            chromosome = raffleChromosomeByRoulette(totalChromosomeScoreForSorting);
        } else {
            int currentAttemptSelectParentNotRepeated = 0;

            while (currentAttemptSelectParentNotRepeated < config.getAttemptSelectParentNotRepeated()
                    && (chromosome == null || chromosomeAlreadyChosen.equals(chromosome))) {
                chromosome = raffleChromosomeByRoulette(totalChromosomeScoreForSorting);

                currentAttemptSelectParentNotRepeated++;
            }
        }

        return chromosome;
    }

    private int getTotalPositionsForLinearRanking() {
        int totalPositions = 0;
        int totalChromosomes = chromosomeList.size();

        // Sum of Gauss
        if (totalChromosomes % 2 == 0) {
            totalPositions = (totalChromosomes + 1) * (totalChromosomes / 2);
        } else {
            totalPositions = (totalChromosomes * (totalChromosomes / 2)) + totalChromosomes;
        }

        return totalPositions;
    }

    private int getTotalPositionsForNonLinearRanking() {
        int totalPositions = 0;

        for (int i = 1; i <= chromosomeList.size(); i++) {
            totalPositions += (int) Math.pow(i, 2);
        }

        return totalPositions;
    }

    private int getTotalChromosomeScoreForSorting() {
        return chromosomeList
                .stream()
                .mapToInt(Chromosome::getFitnessAdjusted)
                .sum();
    }

    private int[] populateRouletteForRanking(boolean isNonLinearRanking) {
        int[] roulette = new int[chromosomeList.size()];
        int amountPerChromosome = config.getInitialPopulation();
        int accumulatedValue = 0;

        chromosomeList.sort(new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome o1, Chromosome o2) {
                double o1Fitness = o1.getFitness();
                double o2Fitness = o2.getFitness();

                return o1Fitness > o2Fitness ? 1 : o1Fitness == o2Fitness ? 0 : -1;
            }
        });

        for (int chromsomeIndex = 0; chromsomeIndex < chromosomeList.size(); chromsomeIndex++) {
            accumulatedValue += (isNonLinearRanking ? (int) Math.pow(amountPerChromosome, 2) : amountPerChromosome);
            roulette[chromsomeIndex] = accumulatedValue;
            amountPerChromosome--;
        }

        return roulette;
    }

    private int[] populateRoulette() {
        int[] roulette = new int[chromosomeList.size()];
        int accumulatedValue = 0;

        for (int chromsomeIndex = 0; chromsomeIndex < chromosomeList.size(); chromsomeIndex++) {
            Chromosome chromosome = chromosomeList.get(chromsomeIndex);
            accumulatedValue += chromosome.getFitnessAdjusted();
            roulette[chromsomeIndex] = accumulatedValue;
        }

        return roulette;
    }

    private Chromosome raffleChromosomeByRoulette(int totalChromosomeScoreForSorting) {
        int indexForSearch = raffleRouletteIndex(totalChromosomeScoreForSorting);
        return chromosomeList.get(getRealRouletteIndex(indexForSearch));
    }

    private int getRealRouletteIndex(int indexForSearch) {
        int begin = 0;
        int end = roulette.length - 1;
        int firstIndice = 0;

        while (begin <= end) {
            int middle = (begin + end) / 2;

            if (roulette[middle] == indexForSearch
                    || (middle == firstIndice && roulette[middle] > indexForSearch)
                    || (roulette[middle - 1] < indexForSearch && roulette[middle] > indexForSearch)) {
                return middle;
            }

            if (roulette[middle] < indexForSearch) { /* Item is in the sub-vector on the right */
                begin = middle + 1;
            } else { /* Item is in the sub-vector on the right */
                end = middle;
            }
        }

        throw new IllegalStateException(
                "It was not possible to fetch the value found in Roulette: " + indexForSearch + ".");
    }

    private int raffleRouletteIndex(int rouletteLength) {
        return raffleIndex(rouletteLength);
    }

    private Chromosome raffleChromosomeByTournament(List<Chromosome> copyOfChromosomeList,
            Chromosome chromosomeAlreadyChosen) {
        Chromosome chromosome = null;

        if (config.getSelectionType() == SelectionType.STOCHASTIC_TOURNAMENT && roulette == null) {
            totalChromosomeScoreForSorting = getTotalChromosomeScoreForSorting();
            roulette = populateRoulette();
        }

        // If it's the first individual of the pair to be chosen, I'll raffle anyone
        // For the second individual of the pair, we will try x times until an
        // individual different from the first is drawn, or we will use a repeated one
        // even
        if (chromosomeAlreadyChosen == null) {
            chromosome = raffleChromosomeByTournament(new ArrayList<Chromosome>(copyOfChromosomeList));

        } else {
            int currentAttemptSelectParentNotRepeated = 0;

            while (currentAttemptSelectParentNotRepeated < config.getAttemptSelectParentNotRepeated()
                    && (chromosome == null || chromosomeAlreadyChosen.equals(chromosome))) {
                chromosome = raffleChromosomeByTournament(new ArrayList<Chromosome>(copyOfChromosomeList));

                currentAttemptSelectParentNotRepeated++;
            }
        }

        return chromosome;
    }

    private Chromosome raffleChromosomeByTournament(List<Chromosome> copyOfChromosomeList) {
        Chromosome chromosome = null;

        for (int tour = 0; tour < config.getTourForTournament(); tour++) {
            int chromosomeRaffledIndex = 0;

            if (config.getSelectionType() == SelectionType.STOCHASTIC_TOURNAMENT) {
                int indexForSearch = raffleRouletteIndex(totalChromosomeScoreForSorting);
                chromosomeRaffledIndex = getRealRouletteIndex(indexForSearch);
            } else {
                chromosomeRaffledIndex = raffleChromosomeIndexByTournament();
            }

            if (chromosome == null
                    || chromosome.getFitness() > copyOfChromosomeList.get(chromosomeRaffledIndex).getFitness()) {
                chromosome = copyOfChromosomeList.get(chromosomeRaffledIndex);
            }
        }

        return chromosome;
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
        addChromosomeInGeneralList(generatedChildren);

        return generatedChildren;
    }

    private void applyMutationOnChildren() throws Exception {
        List<Integer> raffledIndexList = new ArrayList<>();

        for (int mutatedChromosomeIndex = 0; mutatedChromosomeIndex < getNumberOfChromosomesMutated(); mutatedChromosomeIndex++) {
            int raffledIndex = -1;

            if (!config.isAllowApplyingMutationOnRepeatedChild()) {
                do {
                    raffledIndex = raffleIndex(childrenList.size());
                } while (raffledIndexList.contains(raffledIndex));

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
        sort(parentList);
        sort(childrenList);

        // ELITISM -> It will depend on how many children were generated, because
        // depending on the crossover used, one or two children can be generated
        int totalChildrenGenerated = childrenList.size();
        int elitismParents = config.getInitialPopulation() - totalChildrenGenerated;

        List<Chromosome> chromosomeListForReinsertion = new ArrayList<>(parentList.subList(0, elitismParents));
        chromosomeListForReinsertion.addAll(new ArrayList<>(childrenList));

        chromosomeList = new ArrayList<>(chromosomeListForReinsertion);
        sort(chromosomeList);

        if (chromosomeList.size() != config.getInitialPopulation()) {
            throw new Exception("Invalid population size.");
        }
    }

    private void sort(List<Chromosome> chromosomeList) {
        // Descending ordering (higher fitness = better chromosome)
        chromosomeList.sort(new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome chromosome1, Chromosome chromosome2) {
                double fitnessChromosome1 = chromosome1.getFitness();
                double fitnessChromosome2 = chromosome2.getFitness();

                return fitnessChromosome1 > fitnessChromosome2 ? -1
                        : fitnessChromosome1 == fitnessChromosome2 ? 0
                                : 1;
            }
        });
    }

    private void processGenerationResult() throws Exception {
        Chromosome bestChromosomeOfGeneration = chromosomeList.get(Constants.INDEX_BEST_CHROMOSOME);
        findBestChromosomeInGeneration = findBestChromosome(bestChromosomeOfGeneration);

        if (config.isPrintBestChromosomeOfGeneration()) {
            System.out.println("Best Chromosome of Generation: ");
            bestChromosomeOfGeneration.printChromosome(config, config.getAlgorithmType());
        }

        updateBestChromosome(bestChromosomeOfGeneration);
    }

    private boolean findBestChromosome(Chromosome bestChromosome) {
        // If this flag is true, it has to reach the optimal solution
        if (config.isConvergenceForTheBestSolution()) {
            switch (config.getMetricType()) {
                case MAKESPAN:
                    if (Constants.BEST_SLENGTH < bestChromosome.getSLength()) {
                        return false;
                    }
                    break;

                case LOAD_BALANCE:
                    if (Constants.BEST_LOAD_BALANCE < bestChromosome.getLoadBalance()) {
                        return false;
                    }
                    break;

                case FLOW_TIME:
                    if (Constants.BEST_FLOW_TIME < bestChromosome.getFlowTime()) {
                        return false;
                    }
                    break;

                case COMMUNICATION_COST:
                    if (Constants.BEST_COMMUNICATION_COST < bestChromosome.getCommunicationCost()) {
                        return false;
                    }
                    break;

                case WAITING_TIME:
                    if (Constants.BEST_WAITING_TIME < bestChromosome.getWaitingTime()) {
                        return false;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Metric type not implemented.");
            }

            return true;
        }

        return false;
    }

    private void updateBestChromosome(Chromosome bestChromosome) {
        if (bestChromosomeFound == null || bestChromosome.getFitness() > bestChromosomeFound.getFitness()) {
            bestChromosomeFound = bestChromosome;
        }
    }

    private void populateAverageDataOfBestChromosomes(Chromosome chromosome) {
        totalSLengthOfBestChromosomes += chromosome.getSLength();
        totalLoadBalanceOfBestChromosomes += chromosome.getLoadBalance();
        totalFlowTimeOfBestChromosomes += chromosome.getFlowTime();
        totalCommunicationCostOfBestChromosomes += chromosome.getCommunicationCost();
        totalWaitingTimeOfBestChromosomes += chromosome.getWaitingTime();
        totalFitnessOfBestChromosomes += chromosome.getFitness();
        totalNumberOfChromosomes += 1;
    }

    private void processIterationResult() throws Exception {
        Chromosome bestChromosomeOfIteration = chromosomeList.get(Constants.INDEX_BEST_CHROMOSOME);

        if (findBestChromosome(bestChromosomeOfIteration)) {
            totalSuccess++;
        }

        updateBestChromosome(bestChromosomeOfIteration);
    }

    private void showResult(long initialTime) throws Exception {
        FinalResultModel result = new FinalResultModel();
        result.setInitialTime(initialTime);
        result.setTotalSuccess(totalSuccess);
        result.setTotalSLength(totalSLengthOfBestChromosomes);
        result.setTotalLoadBalance(totalLoadBalanceOfBestChromosomes);
        result.setTotalFlowTime(totalFlowTimeOfBestChromosomes);
        result.setTotalCommunicationCost(totalCommunicationCostOfBestChromosomes);
        result.setTotalWaitingTime(totalWaitingTimeOfBestChromosomes);
        result.setTotalFitness(totalFitnessOfBestChromosomes);
        result.setTotalNumberOfChromosomes(totalNumberOfChromosomes);

        result.showResult(bestChromosomeFound, config);
        bestResult.showResult();
    }

    // DataGeneratorForSpreadsheet
    public Map<String, DataForSpreadsheet> executeForSpreadsheet(long initialTime) throws Exception {
        switch (config.getAlgorithmType()) {
            case SINGLE_OBJECTIVE:
                throw new IllegalArgumentException("Algoritm type not implemented for DataGeneratorForSpreadsheet. SINGLE_OBJECTIVE");

            case NSGAII:
                return executeMultiObjectiveGeneticAlgorithmNSGA2ForSpreadsheet(initialTime);

            case SPEAII:
                throw new IllegalArgumentException("Algoritm type not implemented for DataGeneratorForSpreadsheet. SPEA2");

            case AEMMT:
                return executeMultiObjectiveGeneticAlgorithmAEMMTForSpreadsheet(initialTime);

            case AEMMD:
                return executeMultiObjectiveGeneticAlgorithmAEMMDForSpreadsheet(initialTime);

            case NSGAIII:
                return executeMultiObjectiveGeneticAlgorithmNSGA3ForSpreadsheet(initialTime);

            default:
                throw new IllegalArgumentException("Algoritm type not implemented.");
        }
    }

    private Map<String, DataForSpreadsheet> executeMultiObjectiveGeneticAlgorithmNSGA2ForSpreadsheet(long initialTime) throws Exception {
        NSGAII nsga2 = new NSGAII(config, graph, generator);
        return nsga2.executeForSpreadsheet(initialTime, finalResultWriterForSpreadsheet);
    }

    private Map<String, DataForSpreadsheet> executeMultiObjectiveGeneticAlgorithmNSGA3ForSpreadsheet(long initialTime) throws Exception {
        NSGAIII nsga3 = new NSGAIII(config, graph, generator);
        return nsga3.executeForSpreadsheet(initialTime, finalResultWriterForSpreadsheet);
    }

    private Map<String, DataForSpreadsheet> executeMultiObjectiveGeneticAlgorithmAEMMTForSpreadsheet(long initialTime) throws Exception {
        AEMMT aemmt = new AEMMT(config, graph, generator);
        return aemmt.executeForSpreadsheet(initialTime, finalResultWriterForSpreadsheet);
    }

    private Map<String, DataForSpreadsheet> executeMultiObjectiveGeneticAlgorithmAEMMDForSpreadsheet(long initialTime) throws Exception {
        AEMMD aemmd = new AEMMD(config, graph, generator);
        return aemmd.executeForSpreadsheet(initialTime, finalResultWriterForSpreadsheet);
    }

    public static void main(String[] args) {
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            System.out.println(r.nextInt(Integer.MAX_VALUE));
        }
    }
}