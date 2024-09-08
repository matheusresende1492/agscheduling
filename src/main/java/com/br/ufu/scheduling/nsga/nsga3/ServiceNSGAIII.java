package com.br.ufu.scheduling.nsga.nsga3;

import com.br.ufu.scheduling.model.Chromosome;
import com.br.ufu.scheduling.utils.Configuration;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;

public final class ServiceNSGAIII {
    private final Configuration config;
    public ServiceNSGAIII(Configuration config) {
        this.config = config;
    }

        public List<Chromosome> environmentSelection(List<List<Chromosome>> fronts, List<Chromosome> source, int selectionSize, int numberOfObjectives) {

        /*
        * NSGA-III code based on https://jmetal.readthedocs.io/en/latest/about.html
        * */

        if (source.size() == selectionSize) {
            return source;
        }
        List<Double> ideal_point = translateObjectives(fronts, numberOfObjectives);
        List<Chromosome> extreme_points = findExtremePoints(fronts, numberOfObjectives);
        List<Double> intercepts = constructHyperplane(extreme_points, numberOfObjectives);

        normalizeObjectives(intercepts, ideal_point, fronts, numberOfObjectives);

        List<ReferencePoint<Chromosome>> referencePoints = new Vector<>() ;
        (new ReferencePoint<Chromosome>()).generateReferencePoints(referencePoints,numberOfObjectives, 6);

        associate(fronts, referencePoints);

        for (ReferencePoint<Chromosome> rp : referencePoints) {
            rp.sort();
            this.addToTree(rp);
        }

        JMetalRandom rand = JMetalRandom.getInstance();
        List<Chromosome> result = new ArrayList<>();

        while (result.size() < selectionSize) {
            final ArrayList<ReferencePoint<Chromosome>> first = this.referencePointsTree.firstEntry().getValue();
            final int min_rp_index = 1 == first.size() ? 0 : rand.nextInt(0, first.size() - 1);
            final ReferencePoint<Chromosome> min_rp = first.remove(min_rp_index);
            if (first.isEmpty()) this.referencePointsTree.pollFirstEntry();
            Chromosome chosen = SelectClusterMember(min_rp);
            if (chosen != null) {
                min_rp.AddMember();
                this.addToTree(min_rp);
                result.add(chosen);
            }
        }

        return result;
    }


    Chromosome SelectClusterMember(ReferencePoint<Chromosome> rp) {
        Chromosome chosen = null;
        if (rp.HasPotentialMember()) {
            if (rp.MemberSize() == 0) // currently has no member
            {
                chosen = rp.FindClosestMember();
            } else {
                chosen = rp.RandomMember();
            }
        }
        return chosen;
    }


    private TreeMap<Integer, ArrayList<ReferencePoint<Chromosome>>> referencePointsTree = new TreeMap<>();
    private void addToTree(ReferencePoint<Chromosome> rp) {
        int key = rp.MemberSize();
        if (!this.referencePointsTree.containsKey(key))
            this.referencePointsTree.put(key, new ArrayList<>());
        this.referencePointsTree.get(key).add(rp);
    }

    public void associate(List<List<Chromosome>> fronts, List<ReferencePoint<Chromosome>> referencePoints) {

        for (int t = 0; t < fronts.size(); t++) {
            for (Chromosome s : fronts.get(t)) {
                int min_rp = -1;
                double min_dist = Double.MAX_VALUE;
                for (int r = 0; r < referencePoints.size(); r++) {
                    double d = perpendicularDistance(referencePoints.get(r).position, (List<Double>) getAttribute(s));
                    if (d < min_dist) {
                        min_dist = d;
                        min_rp = r;
                    }
                }
                if (t + 1 != fronts.size()) {
                    referencePoints.get(min_rp).AddMember();
                } else {
                    referencePoints.get(min_rp).AddPotentialMember(s, min_dist);
                }
            }
        }
    }

    public double perpendicularDistance(List<Double> direction, List<Double> point) {
        double numerator = 0, denominator = 0;
        for (int i = 0; i < direction.size(); i += 1) {
            numerator += direction.get(i) * point.get(i);
            denominator += Math.pow(direction.get(i), 2.0);
        }
        double k = numerator / denominator;

        double d = 0;
        for (int i = 0; i < direction.size(); i += 1) {
            d += Math.pow(k * direction.get(i) - point.get(i), 2.0);
        }
        return Math.sqrt(d);
    }

    public List<Double> translateObjectives(List<List<Chromosome>> fronts, int numberOfObjectives) {
        List<Double> ideal_point;
        ideal_point = new ArrayList<>(numberOfObjectives);

        for (int f = 0; f < numberOfObjectives; f += 1) {
            double minf = Double.MAX_VALUE;
            for (int i = 0; i < fronts.get(0).size(); i += 1) // min values must appear in the first front
            {
                minf = Math.min(minf, fronts.get(0).get(i).objectives(numberOfObjectives)[f]);
            }
            ideal_point.add(minf);

            for (List<Chromosome> list : fronts) {
                for (Chromosome s : list) {
                    if (f == 0) // in the first objective we create the vector of conv_objs
                        setAttribute(s, new ArrayList<Double>());

                    getAttribute(s).add(s.objectives(numberOfObjectives)[f] - minf);
                }
            }
        }

        return ideal_point;
    }

    private List<Chromosome> findExtremePoints(List<List<Chromosome>> fronts, int numberOfObjectives) {
        List<Chromosome> extremePoints = new ArrayList<>();
        Chromosome min_indv = null;
        for (int f = 0; f < numberOfObjectives; f += 1) {
            double min_ASF = Double.MAX_VALUE;
            for (Chromosome s : fronts.get(0)) { // only consider the individuals in the first front
                double asf = ASF(s, f, numberOfObjectives);
                if (asf < min_ASF) {
                    min_ASF = asf;
                    min_indv = s;
                }
            }

            extremePoints.add(min_indv);
        }
        return extremePoints;
    }

    private double ASF(Chromosome s, int index, int numberOfObjectives) {
        double max_ratio = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < s.objectives(numberOfObjectives).length; i++) {
            double weight = (index == i) ? 1.0 : 0.000001;
            max_ratio = Math.max(max_ratio, s.objectives(numberOfObjectives)[i] / weight);
        }
        return max_ratio;
    }

    public List<Double> constructHyperplane(List<Chromosome> extreme_points, int numberOfObjectives) {
        // Check whether there are duplicate extreme points.
        // This might happen but the original paper does not mention how to deal with it.
        boolean duplicate = false;
        for (int i = 0; !duplicate && i < extreme_points.size(); i += 1) {
            for (int j = i + 1; !duplicate && j < extreme_points.size(); j += 1) {
                duplicate = extreme_points.get(i).equals(extreme_points.get(j));
            }
        }

        List<Double> intercepts = new ArrayList<>();

        if (duplicate) // cannot construct the unique hyperplane (this is a casual method to deal with
        // the condition)
        {
            for (int f = 0; f < numberOfObjectives; f += 1) {
                // extreme_points[f] stands for the individual with the largest value of objective f
                intercepts.add(extreme_points.get(f).objectives(numberOfObjectives)[f]);
            }
        } else {
            // Find the equation of the hyperplane
            List<Double> b = new ArrayList<>(); // (pop[0].objs().size(), 1.0);
            for (int i = 0; i < numberOfObjectives; i++) b.add(1.0);

            List<List<Double>> A = new ArrayList<>();
            for (Chromosome s : extreme_points) {
                List<Double> aux = new ArrayList<>();
                for (int i = 0; i < numberOfObjectives; i++) aux.add(s.objectives(numberOfObjectives)[i]);
                A.add(aux);
            }
            List<Double> x = guassianElimination(A, b);

            // Find intercepts
            for (int f = 0; f < numberOfObjectives; f += 1) {
                intercepts.add(1.0 / x.get(f));
            }
        }
        return intercepts;
    }

    public List<Double> guassianElimination(List<List<Double>> A, List<Double> b) {
        List<Double> x = new ArrayList<>();

        int N = A.size();
        for (int i = 0; i < N; i += 1) {
            A.get(i).add(b.get(i));
        }

        for (int base = 0; base < N - 1; base += 1) {
            for (int target = base + 1; target < N; target += 1) {
                double ratio = A.get(target).get(base) / A.get(base).get(base);
                for (int term = 0; term < A.get(base).size(); term += 1) {
                    A.get(target).set(term, A.get(target).get(term) - A.get(base).get(term) * ratio);
                }
            }
        }

        for (int i = 0; i < N; i++) x.add(0.0);

        for (int i = N - 1; i >= 0; i -= 1) {
            for (int known = i + 1; known < N; known += 1) {
                A.get(i).set(N, A.get(i).get(N) - A.get(i).get(known) * x.get(known));
            }
            x.set(i, A.get(i).get(N) / A.get(i).get(i));
        }
        return x;
    }

    public void normalizeObjectives(List<Double> intercepts, List<Double> ideal_point, List<List<Chromosome>> fronts, int numberOfObjectives) {
        for (int t = 0; t < fronts.size(); t += 1) {
            for (Chromosome s : fronts.get(t)) {

                for (int f = 0; f < numberOfObjectives; f++) {
                    List<Double> conv_obj = (List<Double>) getAttribute(s);
                    if (Math.abs(intercepts.get(f) - ideal_point.get(f)) > 10e-10) {
                        conv_obj.set(f, conv_obj.get(f) / (intercepts.get(f) - ideal_point.get(f)));
                    } else {
                        conv_obj.set(f, conv_obj.get(f) / (10e-10));
                    }
                }
            }
        }
    }

    public void setAttribute(Chromosome solution, List<Double> value) {
        solution.attributes().put(getAttributeIdentifier(), value);
    }

    public List<Double> getAttribute(Chromosome solution) {
        return (List<Double>) solution.attributes().get(getAttributeIdentifier());
    }

    public Object getAttributeIdentifier() {
        return this.getClass();
    }

    public static boolean populaceHasUnsetRank(List<Chromosome> populace) {
        for (Chromosome chromosome : populace) {
            if (chromosome.getRank() == -1) {
                return true;
            }
        }

        return false;
    }
}