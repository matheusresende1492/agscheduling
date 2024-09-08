package com.br.ufu.scheduling.file.csv;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.model.Graph;
import com.br.ufu.scheduling.utils.Configuration;

public class GeneratorDifferentChromosome {
	private static final int CHROMOSOME_FORMATTED = 0;
	private static final int OBJECTIVES_FORMATTED = 1;

	private Random generator;
	private Configuration config;
	private Graph graph;
	private List<Chromosome> chromosomeList = new ArrayList<>();
	private List<Chromosome> repeatedChromosomesList = new ArrayList<>();

	public GeneratorDifferentChromosome(Configuration config, Graph graph, Random generator) {
		this.config = config;
		this.graph = graph;
		this.generator = generator;
	}

	public void execute() throws Exception {
		int differentsChromosomes = 0;
		int iteration = 0;

		while (differentsChromosomes < config.getTotalDifferentChromosomes() && iteration < config.getMaximumAttemptsGenerateDifferentChromosomes()) {
			if (config.isPrintIterations()) {
				System.out.println("############################\n");
				System.out.println("####### ATTEMPT TO GENERATE DIFFERENT CHROMOSOME: " + (iteration + 1) + " #######\n");
				System.out.println("############################\n");
			}

			if (generateDiferenteChromosome()) {
				System.out.println("##### GENERATION CHROMOSOME: " + (differentsChromosomes + 1) + " #####\n");
				differentsChromosomes++;
			}

			iteration++;
		}

		generateCSVFile();		
	}

	private boolean generateDiferenteChromosome() throws Exception {
		Chromosome chromosome = new Chromosome(generator, graph, config);
		if (chromosomeList.contains(chromosome)) {
			repeatedChromosomesList.add(chromosome);
			return false;
		}

		chromosomeList.add(chromosome);
		return true;
	}

	private void generateCSVFile() throws Exception {
        try (BufferedWriter writerObjective = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file-differentChromosomesObjective.csv")));
                BufferedWriter writerChromosome = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file-differentChromosomes.csv")));
                BufferedWriter writerRepeteadChromosome = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("file-repeatedChromosomes.csv")))) {

			generateCSVFile(writerObjective, "makespan,loadBalance,flowtime,communicationCost,waitingTime", chromosomeList, OBJECTIVES_FORMATTED);
			generateCSVFile(writerChromosome, "mapping,scheduling", chromosomeList, CHROMOSOME_FORMATTED);
			generateCSVFile(writerRepeteadChromosome, "mapping,scheduling", repeatedChromosomesList, CHROMOSOME_FORMATTED);

		} catch (Exception e) {
			Exception e2 = new Exception("Error generating .csv file: " + e);
			e2.initCause(e);

			throw e2;
		}
	}

	private void generateCSVFile(BufferedWriter writer, String title, List<Chromosome> chromosomeList, int key) throws Exception {
		write(writer, title);

		for (Chromosome chromosome: chromosomeList) {
			switch (key) {
			case CHROMOSOME_FORMATTED:
				write(writer, getChromosomeFormatted(chromosome));
				break;

			case OBJECTIVES_FORMATTED:
				write(writer, getObjectivesFormatted(chromosome));
				break;

			default:
				throw new IllegalArgumentException("Invalid data type for .csv file.");
			}
		}
	}

	private void write(BufferedWriter write, String message) throws Exception {
		write.write(message);
		write.newLine();
	}

	private String getChromosomeFormatted(Chromosome chromosome) {
		return Arrays.toString(chromosome.getMapping()) + Arrays.toString(chromosome.getScheduling());
	}

	private String getObjectivesFormatted(Chromosome chromosome) {
		return chromosome.getSLength() + "," + chromosome.getLoadBalance() + "," + chromosome.getFlowTime() + "," + chromosome.getCommunicationCost() + "," + chromosome.getWaitingTime();
	}
}