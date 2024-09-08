package com.br.ufu.scheduling.aemmt;

import java.util.Comparator;

import com.br.ufu.scheduling.agmo.Table;
import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.utils.CalculateValueForSort;
import com.br.ufu.scheduling.utils.Configuration;

public class TableAEMMT extends Table {
	public TableAEMMT(int size) {
		super(size);
	}

    public TableAEMMT(int size, boolean isSolutionTable) {
        super(size, isSolutionTable);
    }

	public boolean add(Chromosome chromosome, Configuration config) throws Exception {
		if (chromosomeList.size() < size) {
			Chromosome clone = buildChromosomeClone(chromosome);
			clone.setValueForSort(CalculateValueForSort.calculate(clone, config, objectives));
			chromosomeList.add(clone);

			return true;
		}

		// Ascending sort
		chromosomeList.sort(new Comparator<Chromosome>() {
			@Override
			public int compare(Chromosome o1, Chromosome o2) {
				return o1.getValueForSort() < o2.getValueForSort() ? -1 : o1.getValueForSort() == o2.getValueForSort() ? 0 : 1;
			}
		});

		double valueForSort = CalculateValueForSort.calculate(chromosome, config, objectives);

		if (valueForSort > chromosomeList.get(0).getValueForSort()) {
			chromosomeList.remove(0);

			Chromosome clone = buildChromosomeClone(chromosome);
			clone.setValueForSort(valueForSort);
			chromosomeList.add(clone);

			return true;
		}

		return false;
	}
}