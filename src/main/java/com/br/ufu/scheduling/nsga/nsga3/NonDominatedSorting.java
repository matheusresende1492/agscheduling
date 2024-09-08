package com.br.ufu.scheduling.nsga.nsga3;

import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.utils.Configuration;

import java.util.*;

public class NonDominatedSorting {
    private final Configuration config;
    public NonDominatedSorting(Configuration config) {
        this.config = config;
    }

    private final String attributeId = getClass().getName();

    public List<List<Chromosome>> compute(List<Chromosome> solutionList) {
        List<Chromosome> population = solutionList;

        // dominateMe[i] contains the number of population dominating i
        int[] dominateMe = new int[population.size()];

        // iDominate[k] contains the list of population dominated by k
        List<List<Integer>> iDominate = new ArrayList<>(population.size());

        // front[i] contains the list of individuals belonging to the front i
        ArrayList<List<Integer>> front = new ArrayList<>(population.size() + 1);

        // Initialize the fronts
        for (int i = 0; i < population.size() + 1; i++) {
            front.add(new LinkedList<Integer>());
        }

        // Fast non dominated sorting algorithm
        // Contribution of Guillaume Jacquenot
        for (int p = 0; p < population.size(); p++) {
            // Initialize the list of individuals that i dominate and the number
            // of individuals that dominate me
            iDominate.add(new LinkedList<Integer>());
            dominateMe[p] = 0;
        }

        int flagDominate;
        for (int p = 0; p < (population.size() - 1); p++) {
            // For all q individuals , calculate if p dominates q or vice versa
            for (int q = p + 1; q < population.size(); q++) {
                flagDominate = compare(solutionList.get(p), solutionList.get(q));

                if (flagDominate == -1) {
                    iDominate.get(p).add(q);
                    dominateMe[q]++;
                } else if (flagDominate == 1) {
                    iDominate.get(q).add(p);
                    dominateMe[p]++;
                }
            }
        }

        for (int i = 0; i < population.size(); i++) {
            if (dominateMe[i] == 0) {
                front.get(0).add(i);
                solutionList.get(i).attributes().put(attributeId, 0);
            }
        }

        // Obtain the rest of fronts
        int i = 0;
        Iterator<Integer> it1, it2; // Iterators
        while (!front.get(i).isEmpty()) {
            i++;
            it1 = front.get(i - 1).iterator();
            while (it1.hasNext()) {
                it2 = iDominate.get(it1.next()).iterator();
                while (it2.hasNext()) {
                    int index = it2.next();
                    dominateMe[index]--;
                    if (dominateMe[index] == 0) {
                        front.get(i).add(index);
                        solutionList.get(index).attributes().put(attributeId, i);
                    }
                }
            }
        }

        List<List<Chromosome>> rankedSubPopulations;

        rankedSubPopulations = new ArrayList<>();
        // 0,1,2,....,i-1 are fronts, then i fronts
        for (int j = 0; j < i; j++) {
            rankedSubPopulations.add(j, new ArrayList<>(front.get(j).size()));
            it1 = front.get(j).iterator();
            while (it1.hasNext()) {
                rankedSubPopulations.get(j).add(solutionList.get(it1.next()));
            }
        }

        for (int r = 0; r < rankedSubPopulations.size(); r++) {
            for (int c = 0; c < rankedSubPopulations.get(r).size(); c++) {
                rankedSubPopulations.get(r).get(c).setRank(r + 1);
            }
        }

        return rankedSubPopulations;
    }

    public int compare(Chromosome solution1, Chromosome solution2) {
        return dominanceTest(solution1.objectives(config.getTotalObjectives()), solution2.objectives(config.getTotalObjectives())) ;
    }

    public static int dominanceTest(double[] vector1, double[] vector2) {
        int bestIsOne = 0;
        int bestIsTwo = 0;
        int result;
        for (int i = 0; i < vector1.length; i++) {
            double value1 = vector1[i];
            double value2 = vector2[i];
            if (value1 != value2) {
                if (value1 < value2) {
                    bestIsOne = 1;
                }
                if (value2 < value1) {
                    bestIsTwo = 1;
                }
            }
        }
        result = Integer.compare(bestIsTwo, bestIsOne);
        return result;
    }
}
