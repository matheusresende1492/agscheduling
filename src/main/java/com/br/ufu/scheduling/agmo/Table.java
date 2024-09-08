package com.br.ufu.scheduling.agmo;

import java.util.ArrayList;
import java.util.List;

import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.utils.Configuration;

public abstract class Table {
	protected int score = 0;
	protected int size;
	protected List<Integer> objectives = new ArrayList<>();
	protected List<Chromosome> chromosomeList = new ArrayList<>();
	protected boolean isSolutionTable = false;

	public Table(int size) {
		this.size = size;
	}

    public Table(int size, boolean isSolutionTable) {
        this.size = size;
        this.isSolutionTable = isSolutionTable; 
    }

	public int getScore() {
		return score;
	}

	public int getSize() {
		return size;
	}

	public boolean contains(Chromosome chromosome) {
	    return chromosomeList.contains(chromosome);
	}

	public Chromosome getChromosomeFromIndex(int index) {
		if (index < 0 || index >= chromosomeList.size()) {
			throw new IllegalArgumentException("Invalid chromosome index. Valid values between 0 and " + chromosomeList.size() + ". Value Informed: " + index + ".");
		}

		return chromosomeList.get(index);
	}

	public void addScore(int score) {
		this.score += score;
	}

	public void resetScore() {
		this.score = 0;
	}

	public void addObjective(Integer objective) {
		objectives.add(objective);
	}

	public void addObjectives(List<Integer> objectives) {
		this.objectives.addAll(objectives);
	}

	public int getTotalChromosomes() {
		return chromosomeList.size();
	}

	public void remove(int index) {
		if (index < 0 || index >= chromosomeList.size()) {
			throw new IllegalArgumentException("Invalid chromosome index. Valid values between 0 and " + chromosomeList.size() + ".");
		}

		chromosomeList.remove(index);
	}

	public abstract boolean add(Chromosome chromosome, Configuration config) throws Exception;

	protected Chromosome buildChromosomeClone(Chromosome chromosome) throws Exception {
		Chromosome clone = null;

		try {
			clone = (Chromosome) chromosome.clone();
		} catch (CloneNotSupportedException e) {
			Exception e2 = new Exception("Error making base chromosome clone: " + e);
			e2.initCause(e);

			throw e;
		}

		return clone;
	}

    public void removeChromosomeFromTable(Chromosome chromosome, Configuration config) {
        int totalChromosomes = chromosomeList.size();
        int chromosomeIndex = 0;

        while (chromosomeIndex < totalChromosomes) {
            Chromosome chromosomeB = chromosomeList.get(chromosomeIndex);

            if (chromosomeB.isChromosomeDominated(config, objectives, chromosome)) {
                chromosomeList.remove(chromosomeIndex);
                totalChromosomes--;

                continue;
            }

            chromosomeIndex++;
        }
    }
}