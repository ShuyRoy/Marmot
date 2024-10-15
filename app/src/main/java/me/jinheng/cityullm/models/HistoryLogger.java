package me.jinheng.cityullm.models;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HistoryLogger {

    private String historyPath;
    private BufferedWriter writer;

    private int maxItems;

    public HistoryLogger(String historyPath_, int maxItems_) {
        maxItems = maxItems_;
        historyPath = historyPath_;
        try {
            writer = new BufferedWriter(new FileWriter(historyPath, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void append2File(HistoryItem item) {
        String json = JSON.toJSONString(item);
        try {
            writer.write(json);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FixedSizeQueue<HistoryItem> readItems() throws IOException {

        FileReader historyReader = new FileReader(historyPath);

        BufferedReader bufferedReader = new BufferedReader(historyReader);
        FixedSizeQueue<HistoryItem> queue = new FixedSizeQueue<>(maxItems);

        String line;
        while ((line= bufferedReader.readLine()) != null) {
            try {
                HistoryItem item = JSON.parseObject(line, HistoryItem.class);
                queue.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return queue;
    }

}
