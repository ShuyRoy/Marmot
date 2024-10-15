package me.jinheng.cityullm.models;

import org.jetbrains.annotations.Nullable;

public class Config {

    public static int threadNum = CONSTANT.DEFAULT_THREAD_NUM;

    public static String CPUGPU = CONSTANT.DEFAULT_IS_CPU;

    public static int maxMemorySize = CONSTANT.DEFAULT_MAX_MEMORY_SIZE;

    public static boolean useMMap = true;

    public static int prefechSize = 1;

    public static float lockSize = 0.8F;

    public static String basePath = null;

    public static String cppPath = null;

    public static String modelPath = null;

    public static String historyPath = null;

    public static String dataPath = null;

    public static String model_name_raw = null;
}
