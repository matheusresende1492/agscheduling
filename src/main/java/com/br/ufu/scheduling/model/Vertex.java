package com.br.ufu.scheduling.model;

import java.util.ArrayList;
import java.util.List;

public class Vertex implements Comparable<Vertex> {
    private int task;
    private int computationalCost;
    private List<Integer> entries; //Vertex parents
    private List<Edge> adjacency;

	public int shortestDistance = Integer.MAX_VALUE;

    public Vertex(int task, int computationalCost) {
        this.task = task;
        this.computationalCost = computationalCost;
        this.entries = new ArrayList<Integer>();
        this.adjacency = new ArrayList<Edge>();
    }

    public int getTask() {
    	return task;
    }

    public int getComputationalCost() {
    	return computationalCost;
    }

    public void setComputationalCost(int computationalCost) {
        this.computationalCost = computationalCost;
    }

    public List<Integer> getEntries() {
    	return entries;
    }

    public List<Edge> getAdjacency() {
    	return adjacency;
    }

    public void setEntries(List<Integer> entries) {
    	this.entries = entries;
    }

    public void addEntry(Integer entry) {
    	entries.add(entry);
    }

    public void addAdjacency(Edge edge) {
        adjacency.add(edge);
    }

	public String toString() {
		return "" + task;
	}

	public int compareTo(Vertex other) {
		return Double.compare(shortestDistance, other.shortestDistance);
	}
}