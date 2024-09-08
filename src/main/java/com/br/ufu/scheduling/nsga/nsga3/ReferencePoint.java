package com.br.ufu.scheduling.nsga.nsga3;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReferencePoint<Chromosome> {
  public List<Double> position ;
  private int memberSize ;
  private List<Pair<Chromosome, Double>> potentialMembers ;

  public ReferencePoint() {
  }

  /** Constructor */
  public ReferencePoint(int size) {
    position = new ArrayList<>();
    for (int i =0; i < size; i++)
      position.add(0.0);
    memberSize = 0 ;
    potentialMembers = new ArrayList<>();
  }

  public ReferencePoint(ReferencePoint<Chromosome> point) {
    position = new ArrayList<>(point.position.size());
    for (Double d : point.position) {
      position.add(d);
    }
    memberSize = 0 ;
    potentialMembers = new ArrayList<>();
  }

  public void generateReferencePoints(
          List<ReferencePoint<Chromosome>> referencePoints,
          int numberOfObjectives,
          int numberOfDivisions) {

    ReferencePoint<Chromosome> refPoint = new ReferencePoint<>(numberOfObjectives) ;
    generateRecursive(referencePoints, refPoint, numberOfObjectives, numberOfDivisions, numberOfDivisions, 0);
  }

  private void generateRecursive(
          List<ReferencePoint<Chromosome>> referencePoints,
          ReferencePoint<Chromosome> refPoint,
          int numberOfObjectives,
          int left,
          int total,
          int element) {
    if (element == (numberOfObjectives - 1)) {
      refPoint.position.set(element, (double) left / total) ;
      referencePoints.add(new ReferencePoint<>(refPoint)) ;
    } else {
      for (int i = 0 ; i <= left; i +=1) {
        refPoint.position.set(element, (double)i/total) ;

        generateRecursive(referencePoints, refPoint, numberOfObjectives, left-i, total, element+1);
      }
    }
  }
  
  public List<Double> pos()  { return this.position; }
  public int  MemberSize(){ return memberSize; }
  public boolean HasPotentialMember() { return potentialMembers.size()>0; }
  public void clear(){ memberSize=0; this.potentialMembers.clear();}
  public void AddMember(){this.memberSize++;}
  public void AddPotentialMember(Chromosome member_ind, double distance){
    this.potentialMembers.add(new ImmutablePair<Chromosome,Double>(member_ind,distance) );
  }

  public void sort() {
    this.potentialMembers.sort(Comparator.comparing(Pair<Chromosome, Double>::getRight).reversed());
  }

  public Chromosome FindClosestMember() {
    return this.potentialMembers.remove(this.potentialMembers.size() - 1)
            .getLeft();
  }

  public Chromosome RandomMember() {
    int index = this.potentialMembers.size()>1 ? JMetalRandom.getInstance().nextInt(0, this.potentialMembers.size()-1):0;
    return this.potentialMembers.remove(index).getLeft();
  }
}
