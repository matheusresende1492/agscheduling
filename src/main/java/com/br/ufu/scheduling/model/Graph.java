package com.br.ufu.scheduling.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.br.ufu.scheduling.file.dag.with.cost.LoaderDagWithCost;
import com.br.ufu.scheduling.file.dag.without.cost.LoaderDagWithoutCost;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;

public class Graph {
    private Map<Integer, Vertex> vertices;
    private List<Edge> edges;
    private int firstTask;

    private Graph() {
        vertices = new HashMap<Integer, Vertex>();
        edges = new ArrayList<Edge>();
    }

    private Map<Integer, Vertex> getVertices() {
    	return vertices;
    }

    private List<Edge> getEdges() {
        return edges;
    }

    public Vertex getVertex(int task) {
    	if (!containsVertex(task)) {
    		throw new IllegalArgumentException("There is no vertex with the task " + task + ".");
    	}
    	
    	return vertices.get(task);
    }

    private boolean containsVertex(int task) {
        return vertices.containsKey(task);
    }

    public int getFirstTask() {
    	return firstTask;
    }

    public Vertex addVertex(int task, int computationalCost) {
        Vertex vertice = new Vertex(task, computationalCost);
        vertices.put(task, vertice);
        return vertice;
    }

    public Edge addEdge(Vertex source, Vertex destination, int communicationCost) {
        Edge edge = new Edge(source, destination, communicationCost);
        source.addAdjacency(edge);
        edges.add(edge);
        return edge;
    }

    public int getNumberOfVertices() {
    	return vertices.size();
    }

    public static Graph initializeGraph(Configuration config) throws Exception {
    	if (config.isGraphWithCommunicationCost()) {
    		return initializeGraphWithCommunicationCost(config);
    	} else {
    		return initializeGraphWithoutCommunicationCost(config);
    	}
    }

    private static Graph initializeGraphWithCommunicationCost(Configuration config) throws Exception {
    	Graph graph = new Graph();

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(LoaderDagWithCost.class.getClassLoader().getResourceAsStream(Constants.PACKAGE_DAG_WITH_COST + config.getTaskGraphFileName())))) {
			String line = null;

			int totalTasks = -1;
			int counter = 0;
			int firstTask = -1;

			boolean handlePredecessor = false;

			int taskNumber = 0;
			int computationalCost = 0;
			int totalPredecessors = 0;
			int totalPredecessorsAnalyzed = 0;
			Map<Integer, Integer> mapPredecessors = new HashMap<>();

			while ((line = buffer.readLine()) != null && counter != totalTasks) {
				if (totalTasks == -1) { //First Line
					totalTasks = Integer.parseInt(line.trim()) + 1; //We need to add 1 because the stg graph does not consider task 0 in the total tasks
					continue;
				}

				String[] vector = line.split(" ");

				int totalPositionsAnalyzed = 0;

				for (int position = 0; position < vector.length; position++) {
					String value = vector[position];
					if (value == null || Arrays.asList("", " ").contains(value)) {
						continue;	
					}

					int convertedValue = Integer.parseInt(value);
					if (firstTask == -1) {
						firstTask = convertedValue;
					}

					if (handlePredecessor) {
						int taskPredecessor = handleTask(firstTask, convertedValue);
						int communicationCost = -1;

						for (int newPosition = position + 1; newPosition < vector.length; newPosition++, position++) {
							value = vector[newPosition];
							if (value == null || Arrays.asList("", " ").contains(value)) {
								continue;	
							}

							communicationCost = Integer.parseInt(value);
							break;
						}

						mapPredecessors.put(taskPredecessor, communicationCost);
						totalPredecessorsAnalyzed++;
						break;
					} else {

						switch (totalPositionsAnalyzed) {
						case Constants.TASK_NUMBER:
							taskNumber = handleTask(firstTask, convertedValue); 
							break;

						case Constants.COMPUTATIONAL_COST:
							computationalCost = convertedValue;
							break;

						case Constants.TOTAL_PREDECESSORS:
							totalPredecessors = convertedValue;
							handlePredecessor = true;
							break;

						default:
							break;
						}

						totalPositionsAnalyzed++;
					}
				}

				if (totalPredecessors == totalPredecessorsAnalyzed) {
				    Vertex vertex = null; 

				    if (graph.containsVertex(taskNumber)) {
				        //The vertex can be created as the predecessor of another one, so here we just need to update its computational cost
				        vertex = graph.getVertex(taskNumber);
				        vertex.setComputationalCost(computationalCost);
				    } else {
				        vertex = graph.addVertex(taskNumber, computationalCost);
				    }

					if (mapPredecessors.isEmpty()) {
						vertex.setEntries(null);
						graph.firstTask = vertex.getTask();
					} else {
						for (Map.Entry<Integer, Integer> map : mapPredecessors.entrySet()) {
							int taskPredecessor = map.getKey();
							int communicationCost = map.getValue();

							vertex.addEntry(taskPredecessor);

							graph.addEdge(getVertexPredecessor(graph, taskPredecessor), vertex, communicationCost); //With communication cost
						}
					}

					handlePredecessor = false;
					mapPredecessors.clear();
					totalPredecessorsAnalyzed = 0;
					counter++;
				}
			}
		} catch (Exception e) {
			Exception e2 = new Exception("Error loading " + config.getTaskGraphFileName() + "  task graph file: " + e.getMessage());
			e2.initCause(e);
			throw e2;
		}

		printGraph(graph, config);

    	return graph;
    }

    private static Vertex getVertexPredecessor(Graph graph, int taskPredecessor) {
        //A vertex that is a predecessor of another may be being analyzed without having been created in the graph, 
        //so we need to create it and then we will only update the computational cost.
        Vertex vertexPredecessor = null;

        try {
            vertexPredecessor = graph.getVertex(taskPredecessor); 
        } catch (IllegalArgumentException e) {
            vertexPredecessor = graph.addVertex(taskPredecessor, 0); //Will be updated later
        }

        return vertexPredecessor;
    }

    private static void printGraph(Graph graph, Configuration config) {
    	if (config.isPrintGraphAtTheBeginningOfRun()) {
    		System.out.println(graph.toString());
    	}
    }

    private static Graph initializeGraphWithoutCommunicationCost(Configuration config) throws Exception {
    	Graph graph = new Graph();

    	try (BufferedReader buffer = new BufferedReader(new InputStreamReader(LoaderDagWithoutCost.class.getClassLoader().getResourceAsStream(Constants.PACKAGE_DAG_WITHOUT_COST + config.getTaskGraphFileName())))) {
			String line = null;

			int totalTasks = -1;
			int counter = 0;
			int firstTask = -1;

			while ((line = buffer.readLine()) != null && counter != totalTasks) {
				if (totalTasks == -1) { //First Line
					totalTasks = Integer.parseInt(line.trim()) + 1; //We need to add 1 because the stg graph does not consider task 0 in the total tasks
					continue;
				}

				String[] vector = line.split(" ");

				int taskNumber = 0;
				int computationalCost = 0;
				int totalPredecessors = 0;

				int totalPositionsAnalyzed = 0;
				int totalPredecessorsAnalyzed = 0;
				List<Integer> listPredecessors = new ArrayList<>();

				for (int position = 0; position < vector.length; position++) {
					String value = vector[position];
					if (value == null || Arrays.asList("", " ").contains(value)) {
						continue;	
					}

					int convertedValue = Integer.parseInt(value);
					if (firstTask == -1) {
						firstTask = convertedValue;
					}

					switch (totalPositionsAnalyzed) {
					case Constants.TASK_NUMBER:
						taskNumber = handleTask(firstTask, convertedValue); 
						break;

					case Constants.COMPUTATIONAL_COST:
						computationalCost = convertedValue;
						break;

					case Constants.TOTAL_PREDECESSORS:
						totalPredecessors = convertedValue;
						break;

					default:
						listPredecessors.add(handleTask(firstTask, convertedValue));
						totalPredecessorsAnalyzed++;
						break;
					}

					totalPositionsAnalyzed++;

					if (totalPredecessors == totalPredecessorsAnalyzed && totalPredecessors > 0) {
						break;
					}
				}

				Vertex vertex = graph.addVertex(taskNumber, computationalCost);

				if (listPredecessors.isEmpty()) {
					vertex.setEntries(null);
					graph.firstTask = vertex.getTask();
				} else {
					vertex.setEntries(listPredecessors);
					listPredecessors.forEach(predecessor -> graph.addEdge(graph.getVertex(predecessor), vertex, 0)); //Without communication cost
				}


				counter++;
			}
		} catch (Exception e) {
			Exception e2 = new Exception("Error loading " + config.getTaskGraphFileName() + "  task graph file: " + e.getMessage());
			e2.initCause(e);
			throw e2;
		}

		generateRandomCommunicationCostForNoCostDag(graph, config);
		printGraph(graph, config);
        generateDAGWithCommunicationCost(graph, config);

    	return graph;
    }

    private static void generateDAGWithCommunicationCost(Graph graph, Configuration config) {
        if (config.isGenerateCommunicationCostFromDAGWithoutCommunicationCost()) {
            Map<Integer, List<Edge>> mapPredecessors = new HashMap<>();

            for (Vertex vertex : graph.vertices.values()) {
                if (!mapPredecessors.containsKey(vertex.getTask())) {
                    mapPredecessors.put(vertex.getTask(), new ArrayList<>());
                }

                for (Edge edge : vertex.getAdjacency()) {
                    List<Edge> listPredecessors = null;

                    if (!mapPredecessors.containsKey(edge.getDestination().getTask())) {
                        listPredecessors = new ArrayList<>();
                        mapPredecessors.put(edge.getDestination().getTask(), listPredecessors);
                    } else {
                        listPredecessors = mapPredecessors.get(edge.getDestination().getTask()); 
                    }

                    listPredecessors.add(edge);
                }
            }

            //Subtracting one to get the value of the original task according to the Kasahara DAGs that start with task 0
            printValueWith9BlankSpace(graph.getNumberOfVertices() - 1);

            for (Map.Entry<Integer, List<Edge>> map : mapPredecessors.entrySet()) {
                Vertex vertex = graph.getVertex(map.getKey());
                printValueWith9BlankSpace(vertex.getTask() - 1, vertex.getComputationalCost(), map.getValue().size());

                for (Edge edge : map.getValue()) {
                    printValueWith43BlankSpace(edge.getSource().getTask() - 1, edge.getCommunicationCost());
                }
            }
        }
    }

    private static void printValueWith9BlankSpace(int... values) {
        printValueWithBlankSpace(9, values);
    }

    private static void printValueWith43BlankSpace(int... values) {
        printValueWithBlankSpace(43, values);
    }

    private static void printValueWithBlankSpace(int numberOfBlankSpaceForTheFistValue, int... values) {
        printBlankSpace(numberOfBlankSpaceForTheFistValue);

        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i]);

            if (i < values.length - 1) {
                printBlankSpace(9);
            }
        }

        System.out.println();
    }

    private static void printBlankSpace(int numberOfBlankSpace) {
        for (int i = 0; i < numberOfBlankSpace; i++) {
            System.out.print(" ");
        }
    }

    private static int handleTask(int firstTask, int convertedValue) {
    	//The first task cannot be 0, so let's add 1 to each task.
    	return convertedValue + (firstTask == 0 ? 1 : 0);
    }

    private static void generateRandomCommunicationCostForNoCostDag(Graph graph, Configuration config) {
        if (config.isGenerateRandomCommunicationCostForNoCostDag()) {
            int maximumComputationalCost = Integer.MIN_VALUE;
            int minimumComputationalCost = Integer.MAX_VALUE;

            for (Map.Entry<Integer, Vertex> mapVertex : graph.getVertices().entrySet()) {
                Vertex vertex = mapVertex.getValue();

                if (vertex.getComputationalCost() > 0) {
                    maximumComputationalCost = Integer.max(maximumComputationalCost, vertex.getComputationalCost());
                    minimumComputationalCost = Integer.min(minimumComputationalCost, vertex.getComputationalCost());
                }
            }

            Random generator = new Random();
            for (Edge edge : graph.getEdges()) {
                edge.setCommunicationCost(raffleCommunicationCost(generator, minimumComputationalCost, maximumComputationalCost));
            }            
        }
    }

    private static int raffleCommunicationCost(Random generator, int minimumComputationalCost, int maximumComputationalCost) {
        return generator.nextInt(maximumComputationalCost + 1) + minimumComputationalCost;
    }

    public static Graph initializeGraph() {
    	Graph graph = new Graph();

        Vertex vertex1 = graph.addVertex(1, 2);
        vertex1.setEntries(null);
        graph.firstTask = vertex1.getTask();

        Vertex vertex2 = graph.addVertex(2, 3);
        vertex2.setEntries(Arrays.asList(1));

        Vertex vertex3 = graph.addVertex(3, 3);
        vertex3.setEntries(Arrays.asList(1));

        Vertex vertex4 = graph.addVertex(4, 4);
        vertex4.setEntries(Arrays.asList(1));

        Vertex vertex5 = graph.addVertex(5, 5);
        vertex5.setEntries(Arrays.asList(1));

        Vertex vertex6 = graph.addVertex(6, 4);
        vertex6.setEntries(Arrays.asList(2));

        Vertex vertex7 = graph.addVertex(7, 4);
        vertex7.setEntries(Arrays.asList(1, 2));

        Vertex vertex8 = graph.addVertex(8, 4);
        vertex8.setEntries(Arrays.asList(3, 4));

        Vertex vertex9 = graph.addVertex(9, 1);
        //Another way to add entries
        vertex9.addEntry(6);
        vertex9.addEntry(7);
        vertex9.addEntry(8);

        //Building Edges: sources and destinations
        //Vertex 1
        graph.addEdge(vertex1, vertex2, 4);
        graph.addEdge(vertex1, vertex3, 1);
        graph.addEdge(vertex1, vertex4, 1);
        graph.addEdge(vertex1, vertex5, 1);
        graph.addEdge(vertex1, vertex7, 10);

        //Vertex 2
        graph.addEdge(vertex2, vertex6, 1);
        graph.addEdge(vertex2, vertex7, 1);

        //Vertex 3
        graph.addEdge(vertex3, vertex8, 1);

        //Vertex 4
        graph.addEdge(vertex4, vertex8, 1);

        //Vertex 6
        graph.addEdge(vertex6, vertex9, 5);

        //Vertex 7
        graph.addEdge(vertex7, vertex9, 6);

        //Vertex 8
        graph.addEdge(vertex8, vertex9, 5);

        return graph;
    }

    public static Graph initializeGraphFromDissertation() {
        Graph graph = new Graph();

        Vertex vertex1 = graph.addVertex(1, 3);
        vertex1.setEntries(null);
        graph.firstTask = vertex1.getTask();

        Vertex vertex2 = graph.addVertex(2, 2);
        vertex2.setEntries(null);

        Vertex vertex3 = graph.addVertex(3, 1);
        vertex3.setEntries(Arrays.asList(1));

        Vertex vertex4 = graph.addVertex(4, 2);
        vertex4.setEntries(Arrays.asList(1));

        Vertex vertex5 = graph.addVertex(5, 1);
        vertex5.setEntries(Arrays.asList(2));

        Vertex vertex6 = graph.addVertex(6, 1);
        vertex6.setEntries(Arrays.asList(1, 3));

        Vertex vertex7 = graph.addVertex(7, 1);
        vertex7.setEntries(Arrays.asList(4, 5));

        Vertex vertex8 = graph.addVertex(8, 2);
        vertex8.setEntries(Arrays.asList(4, 6, 7));

        //Building Edges: sources and destinations
        //Vertex 1
        graph.addEdge(vertex1, vertex3, 1);
        graph.addEdge(vertex1, vertex4, 2);

        //Vertex 2
        graph.addEdge(vertex2, vertex5, 1);

        //Vertex 3
        graph.addEdge(vertex3, vertex6, 2);

        //Vertex 4
        graph.addEdge(vertex4, vertex7, 2);
        graph.addEdge(vertex4, vertex8, 2);

        //Vertex 5
        graph.addEdge(vertex5, vertex7, 1);

        //Vertex 6
        graph.addEdge(vertex6, vertex8, 1);

        //Vertex 7
        graph.addEdge(vertex7, vertex8, 1);

        return graph;
    }
    
    public String toString() {
        String formattedText = "";

        for (Map.Entry<Integer, Vertex> mapVertice : vertices.entrySet()) {
        	Vertex vertex = mapVertice.getValue();

            formattedText += vertex.getTask() + "[" + vertex.getComputationalCost() + "]" + " -> ";

            for (Edge e : vertex.getAdjacency()) {
                Vertex v = e.getDestination();
                formattedText += v.getTask() + "[" + e.getCommunicationCost() + "], ";
            }

            formattedText += "\n";
        }

        return formattedText;
    }

    public static void main(String[] args) {
        Graph graph = Graph.initializeGraph();
        System.out.println(graph);
    }
}