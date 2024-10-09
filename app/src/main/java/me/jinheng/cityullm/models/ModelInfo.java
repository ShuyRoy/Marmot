package me.jinheng.cityullm.models;

public class ModelInfo {

    private String modelName;

    private String modelUrl;

    private String modelLocalPath;

    private long modelSize;

    private ModelType modelType;

    public long getKvSize() {
        return kvSize;
    }

    public void setKvSize(long kvSize) {
        this.kvSize = kvSize;
    }

    public long getPrefetchSize() {
        return prefetchSize;
    }

    public void setPrefetchSize(long prefetchSize) {
        this.prefetchSize = prefetchSize;
    }

    private long kvSize;

    private long prefetchSize;

    public ModelInfo() {
    }

    public ModelInfo(String modelName, String modelUrl, long modelSize, ModelType modelType, long kvSize, long prefetchSize) {
        this.modelName = modelName;
        this.modelUrl = modelUrl;
        this.modelSize = modelSize;
        this.modelType = modelType;
        this.kvSize = kvSize;
        this.prefetchSize = prefetchSize;
    }

    public ModelInfo(String modelName, String modelUrl, long modelSize, ModelType modelType) {
        this.modelName = modelName;
        this.modelUrl = modelUrl;
        this.modelSize = modelSize;
        this.modelType = modelType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    public long getModelSize() {
        return modelSize;
    }

    public void setModelSize(long modelSize) {
        this.modelSize = modelSize;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public String getModelLocalPath() { return modelLocalPath; }
}
