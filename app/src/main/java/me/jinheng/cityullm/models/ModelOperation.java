package me.jinheng.cityullm.models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;

import org.apache.commons.io.FileUtils;

public class ModelOperation {

    public static HashMap<String, ModelInfo> modelName2modelInfo = new HashMap<>();

    public static String modelInfoUrl = "https://conference.cs.cityu.edu.hk/saccps/app/models/models.json";

    public enum DownloadStatus {
        RUNNING,
        PAUSED,
        CANCELED
    }

    private static volatile DownloadStatus downloadStatus = DownloadStatus.RUNNING;

    public interface ProgressListener {
        void onProgressUpdate(int progress);
    }

    public static boolean downloadFile(String fileUrl, String filePath, ProgressListener listener) throws Exception {
        Log.d("debug", "Download model file from " + fileUrl);
        Log.d("debug", "Save model in " + filePath);
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        int totalSize = conn.getContentLength();
        int downloadedSize = 0;

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            return false;
        }

        InputStream inputStream = conn.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(filePath);

        int bytesRead = 0;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedSize += bytesRead;
            int progress = (int) ((downloadedSize * 100L) / totalSize);
            if (listener != null) {
                listener.onProgressUpdate(progress);
            }

            // Check the download status
            synchronized (downloadStatus) {
                while (downloadStatus == DownloadStatus.PAUSED) {
                    try {
                        downloadStatus.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                if (downloadStatus == DownloadStatus.CANCELED) {
                    outputStream.close();
                    inputStream.close();
                    conn.disconnect();
                    new File(filePath).delete(); // Optionally remove the partial file
                    return false;
                }
            }
        }
        outputStream.close();
        inputStream.close();
        conn.disconnect();
        Log.d("debug", fileUrl + " is downloaded");
        return true;
    }

    public static void updateModels() {
        String modelInfoPath = Config.modelPath + "models.json";
        File localFile = new File(modelInfoPath);
        if (localFile.exists()) {
            try {
                String content = FileUtils.readFileToString(localFile, "utf-8");
                List<ModelInfo> models = JSON.parseArray(content, ModelInfo.class);

                modelName2modelInfo.clear();
                for (ModelInfo info : models) {
                    info.setModelLocalPath(Config.modelPath + info.getModelLocalPath());
                    modelName2modelInfo.put(info.getModelName(), info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> {
            try {
                // Download metadata of models from server
                boolean result = downloadFile(modelInfoUrl, modelInfoPath, null);
                if (result) {
                    File remoteFile = new File(modelInfoPath);
                    String content = FileUtils.readFileToString(remoteFile, "utf-8");
                    List<ModelInfo> models = JSON.parseArray(content, ModelInfo.class);

                    for (ModelInfo info : models) {
                        info.setModelLocalPath(Config.modelPath + info.getModelLocalPath());
                        modelName2modelInfo.put(info.getModelName(), info);
                    }
                } else {
                    Log.d("debug", modelInfoUrl + " cannot be downloaded");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static List<ModelInfo> getAllSupportModels() {
        List<ModelInfo> models = new ArrayList<>(modelName2modelInfo.values());
        return models;
    }

    public static void downloadModelAsync(String modelName, TextView textViewProgress) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        ModelInfo model = getModelInfo(modelName);
        executorService.execute(() -> {
            try {
                boolean result = downloadFile(model.getModelUrl(), model.getModelLocalPath(), progress -> handler.post(() ->
                        textViewProgress.setText("" + progress + "%")));

                handler.post(() -> {
                    if (result) {
                        textViewProgress.setText("Download completed");
                    } else {
                        textViewProgress.setText("Download failed");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> textViewProgress.setText("Download failed with exception"));
            }
        });
    }

    public static void downloadModelAsync(String modelName, TextView textViewProgress, Runnable backComplete) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        ModelInfo model = getModelInfo(modelName);
        executorService.execute(() -> {
            try {
                boolean result = downloadFile(model.getModelUrl(), model.getModelLocalPath(), progress -> handler.post(() ->
                        textViewProgress.setText("" + progress + "%")));

                handler.post(() -> {
                    if (result) {
                        textViewProgress.setText("Download completed");
                        backComplete.run();
                    } else {
                        textViewProgress.setText("Download failed");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> textViewProgress.setText("Download failed with exception"));
            }
        });
    }

    public static void pauseDownload() {
        synchronized (downloadStatus) {
            downloadStatus = DownloadStatus.PAUSED;
        }
    }

    public static void resumeDownload() {
        synchronized (downloadStatus) {
            downloadStatus = DownloadStatus.RUNNING;
            downloadStatus.notifyAll();
        }
    }

    public static void cancelDownload() {
        synchronized (downloadStatus) {
            downloadStatus = DownloadStatus.CANCELED;
            downloadStatus.notifyAll();
        }
    }

    public static ModelInfo getModelInfo(String modelName) {
        if (modelName2modelInfo.isEmpty()) {
            updateModels();
        }
        if (modelName2modelInfo.get(modelName) != null) {
            return modelName2modelInfo.get(modelName);
        } else {
            Log.d("debug", "Cannot find the model " + modelName);
            return null;
        }
    }

    public static boolean deleteModel(String modelName) {
        ModelInfo modelInfo = modelName2modelInfo.get(modelName);
        assert modelInfo != null;
        File f = new File(modelInfo.getModelLocalPath());
        return f.delete();
    }

}
