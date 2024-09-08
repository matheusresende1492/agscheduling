package com.br.ufu.scheduling.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GenerateExtremesValuesByObjective {
    public static void main(String args[]) throws Exception {
        double maxSLength = Double.MIN_VALUE;
        double maxLoadBalance = Double.MIN_VALUE;
        double maxFlowTime = Double.MIN_VALUE;
        double maxCommunicationCost = Double.MIN_VALUE;
        double maxWaitingTime = Double.MIN_VALUE;

        double minSLength = Double.MAX_VALUE;
        double minLoadBalance = Double.MAX_VALUE;
        double minFlowTime = Double.MAX_VALUE;
        double minCommunicationCost = Double.MAX_VALUE;
        double minWaitingTime = Double.MAX_VALUE;

        try (BufferedReader buffer = new BufferedReader(new FileReader(new File("initialResult.txt")))) {
            String line = null;

            while ((line = buffer.readLine()) != null) {
                if (line.contains("Worst SLength: ")) {
                    maxSLength = getMaxValue(line.split("Worst SLength: "), maxSLength);
                    continue;
                }

                if (line.contains("Best SLength: ")) {
                    minSLength = getMinValue(line.split("Best SLength: "), minSLength);
                    continue;
                }

                if (line.contains("Worst LoadBalance: ")) {
                    maxLoadBalance = getMaxValue(line.split("Worst LoadBalance: "), maxLoadBalance);
                    continue;
                }

                if (line.contains("Best LoadBalance: ")) {
                    minLoadBalance = getMinValue(line.split("Best LoadBalance: "), minLoadBalance);
                    continue;
                }
                
                if (line.contains("Worst FlowTime: ")) {
                    maxFlowTime = getMaxValue(line.split("Worst FlowTime: "), maxFlowTime);
                    continue;
                }

                if (line.contains("Best FlowTime: ")) {
                    minFlowTime = getMinValue(line.split("Best FlowTime: "), minFlowTime);
                    continue;
                }
                
                if (line.contains("Worst CommunicationCost: ")) {
                    maxCommunicationCost = getMaxValue(line.split("Worst CommunicationCost: "), maxCommunicationCost);
                    continue;
                }

                if (line.contains("Best CommunicationCost: ")) {
                    minCommunicationCost = getMinValue(line.split("Best CommunicationCost: "), minCommunicationCost);
                    continue;
                }
                
                if (line.contains("Worst WaitingTime: ")) {
                    maxWaitingTime = getMaxValue(line.split("Worst WaitingTime: "), maxWaitingTime);
                    continue;
                }

                if (line.contains("Best WaitingTime: ")) {
                    minWaitingTime = getMinValue(line.split("Best WaitingTime: "), minWaitingTime);
                    continue;
                }
            }

            System.out.println("Max SLenght: " + maxSLength);
            System.out.println("Min SLenght: " + minSLength);
            
            System.out.println("\nMax LoadBalance: " + maxLoadBalance);
            System.out.println("Min LoadBalance: " + minLoadBalance);
            
            System.out.println("\nMax FlowTime: " + maxFlowTime);
            System.out.println("Min FlowTime: " + minFlowTime);
            
            System.out.println("\nMax CommunicationCost: " + maxCommunicationCost);
            System.out.println("Min CommunicationCost: " + minCommunicationCost);
            
            System.out.println("\nMax WaitingTime: " + maxWaitingTime);
            System.out.println("Min WaitingTime: " + minWaitingTime);

        } catch (Exception e) {
            Exception e2 = new Exception("Error loading initialResult.txt file: " + e.getMessage());
            e2.initCause(e);
            throw e2;
        }
    }
    
    private static double getMaxValue(String[] vet, double maxValue) {
        String value = vet[1].trim();
        double newValue = Double.parseDouble(value);

        if (newValue > maxValue) {
            maxValue = newValue;
        }

        return maxValue;
    }

    private static double getMinValue(String[] vet, double minValue) {
        String value = vet[1].trim();
        double newValue = Double.parseDouble(value);

        if (newValue < minValue) {
            minValue = newValue;
        }

        return minValue;
    }
}
