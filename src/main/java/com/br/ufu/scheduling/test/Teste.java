package com.br.ufu.scheduling.test;

import com.br.ufu.scheduling.ag.AGScheduling;

public class Teste {

    public static void main(String[] args) {
        long initialTime = System.currentTimeMillis();

        try {
            AGScheduling scheduling = new AGScheduling();
               scheduling.execute(initialTime);
        } catch (Exception e) {
            e.printStackTrace();

            System.out.println("Erro: " + e);
            System.out.println("Tempo de execução com erro: " + ((double) (System.currentTimeMillis() - initialTime) / 1000) + " segundos.");
        } finally {
            System.out.println("## FINISHED ##");
        }
    }
}
