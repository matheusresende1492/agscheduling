package com.br.ufu.scheduling.model;

public class Edge {
	private Vertex source;
	private Vertex destination;

	private int communicationCost = 0;

	public Edge(Vertex source, Vertex destination, int communicationCost) {
		this.source = source;
		this.destination = destination;
		this.communicationCost = communicationCost;
	}

	public Vertex getSource() {
		return source;
	}

	public Vertex getDestination() {
		return destination;
	}

	public int getCommunicationCost() {
		return communicationCost;
	}

	public void setSource(Vertex source) {
		this.source = source;
	}

	public void setDestination(Vertex destination) {
		this.destination = destination;
	}

	public void setCommunicationCost(int communicationCost) {
		this.communicationCost= communicationCost;
	}
}