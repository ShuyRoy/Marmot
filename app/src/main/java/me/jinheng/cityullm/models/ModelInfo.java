package me.jinheng.cityullm.models;

public class ModelInfo {

    private String modelName;

    private String modelUrl;

    private String modelLocalPath;

    private long modelSize;

    private long kvSize;

    private long prefetchSize;

    public String getModelName() { return modelName; }

    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelUrl() { return modelUrl; }

    public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }

    public String getModelLocalPath() { return modelLocalPath; }

    public void setModelLocalPath(String modelLocalPath) { this.modelLocalPath = modelLocalPath; }

    public long getModelSize() { return modelSize; }

    public void setModelSize(long modelSize) { this.modelSize = modelSize; }

    public long getKvSize() { return kvSize; }

    public void setKvSize(long kvSize) { this.kvSize = kvSize; }

    public long getPrefetchSize() { return prefetchSize; }

    public void setPrefetchSize(long prefetchSize) { this.prefetchSize = prefetchSize; }

    public ModelInfo() { }

    public ModelInfo(String modelName, String modelUrl, String modelLocalPath, long modelSize, long kvSize, long prefetchSize) {
        this.modelName = modelName;
        this.modelUrl = modelUrl;
        this.modelLocalPath = modelLocalPath;
        this.modelSize = modelSize;
        this.kvSize = kvSize;
        this.prefetchSize = prefetchSize;
    }

    public ModelInfo(String modelName, String modelUrl, String modelLocalPath, long modelSize) {
        this.modelName = modelName;
        this.modelUrl = modelUrl;
        this.modelLocalPath = modelLocalPath;
        this.modelSize = modelSize;
    }
}
