package me.jinheng.cityullm.models;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;

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

    public static native void startLLama(NativeMessageReceiver msg, String localModelPath, int threadNum);

    public static void init(String modelName, MessageAdapter messageAdapter_, Activity activity_, RecyclerView recyclerView_, TextView speedTextView_, ExtendedFloatingActionButton fab_) throws IOException {
        messageAdapter = messageAdapter_;
        activity = activity_;
        recyclerView = recyclerView_;
        speedTextView = speedTextView_;
        fab = fab_;
        historyPath = historyFolder + modelName;
        historyWriter = new FileWriter(historyPath, true);
        historyLogger = new HistoryLogger(historyFolder + modelName, CONSTANT.MAX_INIT_HISTORY_ITEM);
        String localModelPath = Config.localPath + modelName + ".gguf";
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

        initRecord(recyclerView);

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


    public static void initFolder(File externalDir) {
        File LLamaFolder = new File(externalDir, "llama");

        File cppFolder = new File(LLamaFolder, "main");
        if (!cppFolder.exists()) {
            cppFolder.mkdirs();
        }

        File modelFolder = new File(LLamaFolder, "models");
        if (!modelFolder.exists()) {
            modelFolder.mkdirs();
        }

        File historyFolder_ = new File(LLamaFolder, "history");
        if (!historyFolder_.exists()) {
            historyFolder_.mkdirs();
        }
        historyFolder = historyFolder_.getAbsolutePath() + "/";
    }

    public static void copyCpp(Context context, File externalDir) throws IOException {

    }

    public static boolean findModel(File externalDir) {

        String absolutePath = externalDir.getAbsolutePath();
        Config.localPath = absolutePath + "/llama/models/";
        File modelFolder = new File(Config.localPath);
        if (!modelFolder.exists()) {
            modelFolder.mkdirs();
        }

        File[] files = modelFolder.listFiles();
        for (File f : files) {
            if (f.getName().startsWith("ggml-model")){
                return false;
            }
        }
        return true;
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
