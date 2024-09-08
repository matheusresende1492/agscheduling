package com.br.ufu.scheduling.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import com.br.ufu.scheduling.ag.AGScheduling;
import com.br.ufu.scheduling.model.DataForSpreadsheet;
import com.br.ufu.scheduling.model.ObjectiveDataForSpreadsheet;
import com.br.ufu.scheduling.utils.Configuration;
import com.br.ufu.scheduling.utils.Constants;
import com.br.ufu.scheduling.utils.Utils;

public class ExecuteAG {

    public static void main(String[] args) throws Exception {
        long initialTime = System.currentTimeMillis();
        boolean generateDataBase = true;

        try {
            if (generateDataBase) {
                generateDataBase();
            } else {
                AGScheduling scheduling = new AGScheduling();
                scheduling.execute(initialTime);
            }
        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Erro: " + e);
            System.out.println("Tempo de execução com erro: " + ((double) (System.currentTimeMillis() - initialTime) / 1000) + " segundos.");
        } finally {
            System.out.println("Tempo de execução total: " + ((double) (System.currentTimeMillis() - initialTime) / 1000) + " segundos.");
            System.out.println("## FINISHED ##");
        }
    }

    public static void generateDataBase() throws Exception {
//        String[] dags = { "rand0105-with-communication-costs-300-tasks.stg"};
        String[] dags = { "rand0000-with-communication-costs-50-tasks.stg"};
//        String[] dags = {"rand0000-with-communication-costs-50-tasks.stg", "rand0055-with-communication-costs-100-tasks.stg", "rand0105-with-communication-costs-300-tasks.stg"};

//        int[] tasks = {300};
        int[] tasks = {50};
//        int[] tasks = {50, 100, 300};

//        int[] processors = { 2, 4 };
        int[] processors = { 4 };
//        int[] processors = {2, 4, 8, 16};

        int[] seeds = { -1995383100, -1989726638, -1949695351};
//        int[] seeds = { 1793184929 };
//        int[] seeds = {-1995383100, -1989726638, -1949695351, -813009771, -609065737, 64083307, 866349162, 918118122, 983153476, 1793184929};

        /* !! IMPORTANT !!
        * Java and Maven necessary
        * run the command "mvn clean compile" after any code change to compile the code
        * */

        int[] algorithms = { 3 };
//        int[] algorithms = { 5 }; //NSGAIII
//        int[] algorithms = { 1 }; //NSGAII
//        int[] algorithms = { 1, 5 }; //NSGAII and NSGAIII
//        int[] algorithms = {3, -3, 4, 1, 5};
//
//        int[] objectives = { 2 };
//        int[] objectives = { 3 };
//        int[] objectives = {5, 4, 3, 2};
        int[] objectives = {5, 3};

        boolean headerPrinted = false;
        boolean isPrintHiperVolumeInConsole = false;

        for (int objective = 0; objective < objectives.length; objective++) {
            for (int dag = 0; dag < dags.length; dag++) {
                Map<String, DataForSpreadsheet> mapDataForSpreadsheet = new LinkedHashMap<>();
                String finalResultName = objectives[objective] + "-Objectives-" + tasks[dag] + "-Tasks.txt";

                File file = new File(finalResultName);

                if (!file.exists()) {
                    file.createNewFile();
                }

                //System.out.println("Inicio: " + finalResultName);

                try (BufferedWriter finalResultWriterForSpreadsheet = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath())))) {
                    for (int processor = 0; processor < processors.length; processor++) {
                        for (int seed = 0; seed < seeds.length; seed++) {
                            for (int algorithm = 0; algorithm < algorithms.length; algorithm++) {
                                String fileNameForDataNormalization = dags[dag].split(".stg")[0]
                                        + Constants.SUFIX_NORMLIZATION_FILE_NAME
                                        + "-"
                                        + (Integer.toString(processors[processor]).length() == 1
                                                ? "0" + processors[processor]
                                                : processors[processor])
                                        + "-processors"
                                        + ".txt";

                                if (!isPrintHiperVolumeInConsole) {
                                    System.out.printf("Executing... OBJECTIVE %s | GRAPH: %s | PROCESSOR: %s | SEED: %s | ALGORITHM: %s%n",
                                            objectives[objective],
                                            dags[dag],
                                            processors[processor],
                                            seeds[seed],
                                            algorithms[algorithm]);
                                }

                                Configuration config = new Configuration(fileNameForDataNormalization);
                                config.setTotalObjectives(objectives[objective]);
                                config.setTaskGraphFileName(dags[dag]);
                                config.setTotalProcessors(processors[processor]);
                                config.setSeed(seeds[seed]);
                                config.setAlgorithm(Math.abs(algorithms[algorithm]));
                                config.setPrintHiperVolumeInConsole(isPrintHiperVolumeInConsole);

                                if (Math.abs(algorithms[algorithm]) == 3 || algorithms[algorithm] == 4) {// AEMMT ou AEMMT
                                    config.setTotalGenerations(15000);

                                    if (algorithms[algorithm] == 3) { // AEMMT - Simple Average
                                        config.setSortFunction(1);

                                    } else if (algorithms[algorithm] == -3) { // AEMMT - Harmonic Average
                                        config.setSortFunction(2);
                                    }
                                } else {
                                    config.setTotalGenerations(500);
                                }

                                long initialTime = System.currentTimeMillis();
                                AGScheduling scheduling = null;

                                if (config.isPrintHiperVolumeInConsole()) {
                                    if (!headerPrinted) {
                                        System.out.println("Objectives,DAG,Processor,Seed,Algorithm,HiperVolume,ChromosomeCont,BestChromosomeHiperVolume");
                                        headerPrinted = true;
                                    }

                                    isPrintHiperVolumeInConsole = true;

                                    scheduling = new AGScheduling(config, null);
                                } else {
                                    scheduling = new AGScheduling(config, finalResultWriterForSpreadsheet);
                                }

                                Map<String, DataForSpreadsheet> newMapDataForSpreadsheet = scheduling.executeForSpreadsheet(initialTime);

                                for (Map.Entry<String, DataForSpreadsheet> mapData : newMapDataForSpreadsheet.entrySet()) {
                                    DataForSpreadsheet newDataForSpreadsheet = mapData.getValue();

                                    if (mapDataForSpreadsheet.containsKey(newDataForSpreadsheet.getKey())) {
                                        DataForSpreadsheet dataForSpreadsheet = mapDataForSpreadsheet
                                                .get(newDataForSpreadsheet.getKey());

                                        for (ObjectiveDataForSpreadsheet objectiveDataForSpreadsheet : newDataForSpreadsheet
                                                .getListObjectivesDataForSpreadsheet()) {
                                            dataForSpreadsheet.addObjective(objectiveDataForSpreadsheet);
                                        }
                                    } else {
                                        mapDataForSpreadsheet.put(newDataForSpreadsheet.getKey(), newDataForSpreadsheet);
                                    }
                                }

                                finalResultWriterForSpreadsheet.flush();
                            }
                        }
                    }

                    if (!isPrintHiperVolumeInConsole) {
                        for (Map.Entry<String, DataForSpreadsheet> mapData : mapDataForSpreadsheet.entrySet()) {
                            DataForSpreadsheet dataForSpreadsheet = mapData.getValue();
                            Utils.print(dataForSpreadsheet.toString(), finalResultWriterForSpreadsheet);
                        }
                    }
                } catch (Exception e) {
                    Exception e2 = new Exception("Error generating .txt file from FinalResult: " + e);
                    e2.initCause(e);

                    throw e2;
                }
                
                //System.out.println("Fim: " + finalResultName);
            }
        }
    }
}