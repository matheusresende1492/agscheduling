package com.br.ufu.scheduling.aemmd;

import com.br.ufu.scheduling.agmo.Table;
import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.utils.Configuration;

public class TableAEMMD extends Table {
	public TableAEMMD(int size) {
		super(size);
	}

    public TableAEMMD(int size, boolean isSolutionTable) {
        super(size, isSolutionTable);
    }

	public boolean add(Chromosome chromosome, Configuration config) throws Exception {
	    if (chromosomeList.size() == 0) {
	        Chromosome clone = buildChromosomeClone(chromosome);
	        chromosomeList.add(clone);
	        return true;
	    }

	    if (isChromosomeDominated(chromosome, config)) {
	        return false;
	    }

	    //Repeated individual is not allowed in the solution table
        if (isSolutionTable && chromosomeList.contains(chromosome)) {
            return false;
        }

	    removeChromosomeFromTable(chromosome, config);

        Chromosome clone = buildChromosomeClone(chromosome);
        chromosomeList.add(clone);
        return true;
	}

	private boolean isChromosomeDominated(Chromosome chromosome, Configuration config) {
        for (int chromosomeIndex = 0; chromosomeIndex < chromosomeList.size(); chromosomeIndex++) {
            Chromosome chromosomeB = chromosomeList.get(chromosomeIndex);

            if (chromosome.isChromosomeDominated(config, objectives, chromosomeB)) {
                return true;
            }
        }

        return false;
	}
}