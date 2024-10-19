package me.jinheng.cityullm.models;

public class ModelInfo {

    private String modelName;

    private String modelUrl;

    private String modelLocalPath = "";

    private long modelSize = 0;

    private long kvSize = 0;

    private long prefetchSize = 0;

    private String systemPrompt = "";

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

    public String getSystemPrompt() { return systemPrompt; }

    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }

    public ModelInfo() { }

    public ModelInfo(String modelName, String modelUrl, String modelLocalPath, long modelSize, long kvSize, long prefetchSize, String systemPrompt) {
        this.modelName = modelName;
        this.modelUrl = modelUrl;
        this.modelLocalPath = modelLocalPath;
        this.modelSize = modelSize;
        this.kvSize = kvSize;
        this.prefetchSize = prefetchSize;
        this.systemPrompt = systemPrompt;
    }
}
