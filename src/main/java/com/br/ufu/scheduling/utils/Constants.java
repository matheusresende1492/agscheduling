package com.br.ufu.scheduling.utils;

import java.io.File;

public class Constants {
    public final static String USE_DEFAULT_GRAPH = "-1";
    public final static String READ_ME_FILE_NAME = "README.conf";
    public final static String NORMALIZATION_BASE_FILE_NAME = "DAGBase-normalization.txt";
    public final static String SUFIX_NORMLIZATION_FILE_NAME = "-normalization";

    public final static String PACKAGE_BASE = "file";
    public final static String PACKAGE_NORMALIZATION_WITH_COST =  PACKAGE_BASE + File.separator + "normalization" + File.separator + "with" + File.separator + "cost" + File.separator;
    public final static String PACKAGE_NORMALIZATION_WITH_COST_BACKUP =  PACKAGE_NORMALIZATION_WITH_COST + File.separator + "backup" + File.separator;
    public final static String PACKAGE_NORMALIZATION_WITHOUT_COST = PACKAGE_BASE + File.separator + "normalization" + File.separator + "without" + File.separator + "cost" + File.separator;
    public final static String PACKAGE_DAG_WITH_COST =  PACKAGE_BASE + File.separator + "dag" + File.separator + "with" + File.separator + "cost" + File.separator;
    public final static String PACKAGE_DAG_WITHOUT_COST =  PACKAGE_BASE + File.separator + "dag" + File.separator + "without" + File.separator + "cost" + File.separator;
    public final static String PACKAGE_CSV = PACKAGE_BASE + File.separator + "csv" + File.separator;
    public static final String LINE_BREAK = "\n";
    public static final String ALGORITHM_NSGA2 = "NSGA2";
    public static final String ALGORITHM_AEMMT = "AEMMT";
    public static final String ALGORITHM_AEMMD = "AEMMD";

    public static final int RANDOM_SEED = -1;
    public static final int TASK_NUMBER = 0;
    public static final int COMPUTATIONAL_COST = 1;
    public static final int TOTAL_PREDECESSORS = 2;
    public static final int INDEX_BEST_CHROMOSOME = 0;
    public static final int MAXIMIZATION_PROBLEM = 0;
    public static final int DOUBLE_TOURNAMENT = 2;

    public static final double RANDOM_NUMBER_FIXED_IN_ARTICLE = 0.5;
    public static final double ADJUST_VALUE_FOR_FITNESS_IN_ROULLETE = 1000.0;

    //Best result of metrics from the graph used
    public static final double BEST_SLENGTH = 16.0;
    public static final double BEST_LOAD_BALANCE = 1.085106383;
    public static final double BEST_FLOW_TIME = 80.0;
    public static final double BEST_COMMUNICATION_COST = 0.0;
    public static final double BEST_WAITING_TIME = 9.0;

    public static final int MAKESPAN = 0;
    public static final int LOAD_BALANCE = 1;
    public static final int FLOW_TIME = 2;
    public static final int COMMUNICATION_COST = 3;
    public static final int WAITING_TIME = 4;

    public static final int SOLUTION_RANGE = 10;
    public static final int RANGE_90_100 = 0;
    public static final int RANGE_80_90 = 1;
    public static final int RANGE_70_80 = 2;
    public static final int RANGE_60_70 = 3;
    public static final int RANGE_50_60 = 4;
    public static final int RANGE_40_50 = 5;
    public static final int RANGE_30_40 = 6;
    public static final int RANGE_20_30 = 7;
    public static final int RANGE_10_20 = 8;
    public static final int RANGE_0_10 = 9;
    public static final int DEFAULT_RANGE_VALUE = 0;
    public static final int INCREMENT_RANGE_VALUE = 1;

    public static final int TWO_DECIMAL_PLACES = 2;
    public static final int NINE_DECIMAL_PLACES = 9;

    public static final int NSGA2_DOMINANT = 1;
    public static final int NSGA2_INFERIOR = 2;
    public static final int NSGA2_NON_DOMINATED = 3;
    public static final int RANK_PARETO_BORDER = 1;
}