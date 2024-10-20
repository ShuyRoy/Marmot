package me.jinheng.cityullm.models;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import me.jinheng.cityullm.CustomChat.CustomChat;
import me.jinheng.cityullm.Message;
import me.jinheng.cityullm.MessageAdapter;

public class LLama {

    public static MessageAdapter messageAdapter;

    public static Activity activity;

    public static RecyclerView recyclerView;

    public static TextView speedTextView;

    public static ExtendedFloatingActionButton fab;

    public static Long id = 0L;

    public static String historyFolder = null /* "/data/local/tmp/llama.cpp/build/" */;

    public static String historyPath;

    public static FileWriter historyWriter;

    public static boolean answering = false;

    public static AnswerState answerState = AnswerState.NO_MESSAGE_NEED_REPLY;

    public static NativeMessageReceiver msg = new NativeMessageReceiver();

    public static String input;

    public static Thread curThread;

    public static HistoryLogger historyLogger;

    public static void walkFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                Log.d("debug", "Directory: " + file.getAbsolutePath());
                walkFolder(file.getAbsolutePath());
            } else if (file.isFile()) {
                Log.d("debug", "File: " + file.getAbsolutePath() + ", size " + file.length());
            }
        }
    }

    public static void initFolder(File externalDir) {
        File llamaFolder = new File(externalDir, "llama");
        Config.basePath = llamaFolder.getAbsolutePath() + "/";

        File cppFolder = new File(llamaFolder, "main");
        Config.cppPath = cppFolder.getAbsolutePath() + "/";
        if (!cppFolder.exists()) {
            cppFolder.mkdirs();
        }

        File modelFolder = new File(llamaFolder, "models");
        Config.modelPath = modelFolder.getAbsolutePath() + "/";
        if (!modelFolder.exists()) {
            modelFolder.mkdirs();
        }

        File historyFolder = new File(llamaFolder, "history");
        Config.historyPath = historyFolder.getAbsolutePath() + "/";
        if (!historyFolder.exists()) {
            historyFolder.mkdirs();
        }

        File dataFolder = new File(llamaFolder, "data");
        Config.dataPath = dataFolder.getAbsolutePath() + "/";
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public static boolean hasInitialModel() {
        File modelFolder = new File(Config.modelPath);
        if (modelFolder.exists()) {
            File[] files = modelFolder.listFiles();
            for (File f : files) {
                if (f.getName().endsWith(".gguf")) {
                    Log.d("debug", "Find initial model " + f.getAbsolutePath());
                    return true;
                }
            }
        } else {
            Log.d("debug", modelFolder.getAbsolutePath() + " does not exist");
        }
        return false;
    }
    public static void init(String modelName, String modelPath, CustomChat chat) throws IOException {
        //String localModelPath = Config.basePath + modelName + ".gguf";
        Log.e("LLAMA INIT", modelName + " ||| " + modelPath);
        String localModelPath = Config.basePath + modelPath;
        float totalMemory = Utils.getTotalMemory() / CONSTANT.GB;
        float canUseMemory = Math.min(totalMemory, Config.maxMemorySize);
        ModelInfo mInfo = ModelOperation.getModelInfo(modelName);
        float modelSize = (float) mInfo.getModelSize() / CONSTANT.GB;

        float prefetchSizeInGB = 0f;
        float kvCacheSizeInGB = 0f;
        float lSize = 0f;

        if (canUseMemory <= modelSize) {
            prefetchSizeInGB = (float) mInfo.getPrefetchSize() / CONSTANT.GB;
            kvCacheSizeInGB = (float) mInfo.getKvSize() / CONSTANT.GB;
        }

        System.out.println("[localModelPath: " + modelPath + " ]");
        // ggml-model-tinyllama-1.1b-chat-v1.0-q4_0.gguf
        // tinyllama-1.1b-chat-v1.0.gguf
        // cpp中根据prefetchSizeInGB、lSize决定是否调用prefetch版本
        startLLamaWOPrefetch(msg, modelPath, Config.threadNum, prefetchSizeInGB, lSize);

        curThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                msg.reset();
                String s = msg.waitForString();

                if (answerState == AnswerState.NO_MESSAGE_NEED_REPLY) {
                    //
                } else if (answerState == AnswerState.MESSAGE_NEED_REPLY) {
                    answerState = AnswerState.ANSWERING;
                    chat.botContinue(s);
                } else {
                    if (msg.isStart()) {
                        // START/END
                        // NO_MESSAGE_NEED_REPLY,
                        // MESSAGE_NEED_
                        // REPLY,
                        // ANSWERING
                        chat.updateInfo(s);
                        answerState = AnswerState.NO_MESSAGE_NEED_REPLY;
                    } else {
                        chat.botContinue(s);
                    }
                }
            }
        });
        curThread.start();
    }

    public static native void startLLama(NativeMessageReceiver msg, String localModelPath, int threadNum);

    public static void init(String modelName, MessageAdapter messageAdapter_,
                            Activity activity_, RecyclerView recyclerView_,
                            TextView speedTextView_, ExtendedFloatingActionButton fab_) throws IOException {
        messageAdapter = messageAdapter_;
        activity = activity_;
        recyclerView = recyclerView_;
        speedTextView = speedTextView_;
        fab = fab_;
//        historyPath = historyFolder + modelName;
//        historyWriter = new FileWriter(historyPath, true);
//        historyLogger = new HistoryLogger(historyFolder + modelName, CONSTANT.MAX_INIT_HISTORY_ITEM);
        String localModelPath = Config.basePath + modelName + ".gguf";
        float totalMemory = Utils.getTotalMemory() / CONSTANT.GB;
        float canUseMemory = Math.min(totalMemory, Config.maxMemorySize);
        ModelInfo mInfo = ModelOperation.getModelInfo(modelName);
        float modelSize = (float) mInfo.getModelSize() / CONSTANT.GB;

        float prefetchSizeInGB = 0f;
        float kvCacheSizeInGB = 0f;
        float lSize = 0f;

        if (canUseMemory <= modelSize) {
            prefetchSizeInGB = (float) mInfo.getPrefetchSize() / CONSTANT.GB;
            kvCacheSizeInGB = (float) mInfo.getKvSize() / CONSTANT.GB;
        }

        // cpp中根据prefetchSizeInGB、lSize决定是否调用prefetch版本
        startLLamaWOPrefetch(msg, localModelPath, Config.threadNum, prefetchSizeInGB, lSize);

        // initRecord(recyclerView);

        curThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String output = "";
                while (!Thread.currentThread().isInterrupted()) {
                    msg.reset();
                    String s = msg.waitForString();
                    System.out.println(s);

                    if (answerState == AnswerState.NO_MESSAGE_NEED_REPLY) {
                        continue;
                    } else if (answerState == AnswerState.MESSAGE_NEED_REPLY) {
                        output = s;
                        addMessageOnUI(s);
                        answerState = AnswerState.ANSWERING;
                        continue;
                    } else {
                        if (msg.isStart()) {
                            // START/END
                            answerState = AnswerState.NO_MESSAGE_NEED_REPLY;
                            historyLogger.append2File(new HistoryItem(id, input, output));
                            updateSpeedOnUI(s);
                            updateClear();
                        } else {
                            output += s;
                            updateMessageOnUI(output);
                            updateStop();
                        }
                    }
                }
            }
        });
        curThread.start();

    }

    private static native void startLLamaWOPrefetch(NativeMessageReceiver msg, String localModelPath, int threadNum, float prefetchSizeInGB, float lSize);

    public static void updateSpeedOnUI(String time) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speedTextView.setText(time);
            }
        });
    }

    public static void updateStop() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setText("STOP");
            }
        });
    }

    public static void updateClear() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setText("CLEAR");
            }
        });
    }

    public static native void startLLamaPrefetch(NativeMessageReceiver msg, String localModelPath, int threadNum, float prefetchSizeInGB, float lSize);

    static {
        System.loadLibrary("main");
    }

    public static native void inputString(String s);
    public static void CustomRun(String input_) throws RuntimeException {
        input = input_;
        inputString(input);
        answerState = AnswerState.MESSAGE_NEED_REPLY;
    }
    public static void run(String input_) throws RuntimeException {
        updateStop();
        input = input_;
        inputString(input);
        answerState = AnswerState.MESSAGE_NEED_REPLY;
    }

    public static native void stop();
    public static native void kill();


    public static void initRecord(RecyclerView recyclerView) throws IOException {
        FixedSizeQueue<HistoryItem> queue = historyLogger.readItems();

        HistoryItem historyItem;
        while ((historyItem = queue.remove()) != null) {
            Message msgSend = new Message(historyItem.getSend(), true);
            Message msgRecv = new Message(historyItem.getReceive(), false);

            messageAdapter.addMessage(msgSend);
            messageAdapter.addMessage(msgRecv);
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

            id = historyItem.getId() + 1;
        }
    }

    public static void addMessageOnUI(String msg) {
        Message newMessage = new Message(msg, false);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.addMessage(newMessage);
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
    }

    public static void updateMessageOnUI(String msg) {
        Message newMessage = new Message(msg, false);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int finalIndex = messageAdapter.getItemCount() - 1;
                messageAdapter.updateMessage(finalIndex, newMessage);
            }
        });
    }

    public static void destroy() {
        kill();
        curThread.interrupt();
    }

    public static void clear() {
        messageAdapter.clear();
    }
}
