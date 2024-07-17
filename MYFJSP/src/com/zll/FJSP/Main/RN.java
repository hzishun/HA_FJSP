package com.zll.FJSP.Main;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RN {

    public static void main(String[] args) {
        Random r = new Random(114514);
        int jobNumber = 5;
        int[] operationNumber = new int[jobNumber];
        int machineNumber = 6;
        int[][] machineRecipe = new int[machineNumber][];
        int[][] operationRecipe = new int[jobNumber][];
        int recipeNumber = 12;

        for (int i = 0; i < jobNumber; i++) {
            operationNumber[i] = r.nextInt(2) + 5;
        }

        for (int i = 0; i < jobNumber; i++) {
            operationRecipe[i] = new int[operationNumber[i]];
            for (int j = 0; j < operationNumber[i]; j++) {
                operationRecipe[i][j] = r.nextInt(recipeNumber) + 1;
            }
        }

        Set<Integer> set = new HashSet<>();

        while(set.size() < recipeNumber) {
            for (int i = 0; i < machineNumber; i++) {
                int random = r.nextInt(3) + 4;
                machineRecipe[i] = new int[random];
                for (int j = 0; j < random; j++) {
                    int reNumber = r.nextInt(12) + 1;
                    set.add(reNumber);
                    machineRecipe[i][j] = reNumber;
                }
            }
        }
    }
}
