package me.jinheng.cityullm.models;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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

    public static boolean downloadFile(String fileUrl, String filePath, ProgressListener listener, MutableLiveData<Integer> _downloadProgress) throws Exception {
        Log.d("debug", "Download file from " + fileUrl + " and save in " + filePath);
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int totalSize = conn.getContentLength();
        int downloadedSize = 0;

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            return false;
        }

        InputStream inputStream = conn.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(filePath);

        int bytesRead;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedSize += bytesRead;
            int progress = (int) ((downloadedSize * 100L) / totalSize);
            if (listener != null) {
                listener.onProgressUpdate(progress);
            }
            if (_downloadProgress != null) {
                _downloadProgress.postValue(progress);
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
        return true;
    }

    public static void updateModels() {
        String modelInfoPath = Config.localPath + "models.json";
        try {
            // Download metadata of models from server
            boolean result = downloadFile(modelInfoUrl, modelInfoPath, null, null);
            if (result) {
                File file = new File(modelInfoPath);
                String content = FileUtils.readFileToString(file, "utf-8");
                List<ModelInfo> models = JSON.parseArray(content, ModelInfo.class);

                for (ModelInfo info : models) {
                    modelName2modelInfo.put(info.getModelName(), info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ModelInfo> getAllSupportModels() {
        List<ModelInfo> models = new ArrayList<>(modelName2modelInfo.values());
        return models;
    }

    public static void downloadModelAsync(String modelName, TextView textViewProgress) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        ModelInfo model = modelName2modelInfo.get(modelName);
        executorService.execute(() -> {
            try {
                boolean result = downloadFile(model.getModelUrl(), model.getModelLocalPath(), null, progress -> handler.post(() ->
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

        executorService.execute(() -> {
            try {
                boolean result = downloadModel(modelName, progress -> handler.post(() ->
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

    public static List<ModelInfo> getLocalModels() {
        File localDir = new File(Config.localPath);
        File[] modelFiles = localDir.listFiles();
        if (null == modelFiles) {
            return null;
        }

        List<ModelInfo> models = new LinkedList<>();
        for (File file : modelFiles) {
            if (file.isFile() && file.getName().startsWith("ggml-model")) {
                String fileName = file.getName();
                String modelName = fileName.substring(0, fileName.lastIndexOf("."));
                long modelSize = file.length();
                ModelType modelType = ModelType.GGUF;

                ModelInfo modelInfo = new ModelInfo(modelName, "",
                        modelSize, modelType);
                models.add(modelInfo);
                modelName2modelInfo.put(modelName, modelInfo);
            }
        }
        return models;
    }

    public static ModelInfo getModelInfo(String modelName) {
        if (modelName2modelInfo.isEmpty()) {
            getLocalModels();
        }
        return modelName2modelInfo.get(modelName);
    }

    public static boolean deleteModel(String modelName) {
        ModelInfo modelInfo = modelName2modelInfo.get(modelName);
        assert modelInfo != null;
        File f = new File(Config.localPath + modelName + ".gguf");
        return f.delete();
    }

}
