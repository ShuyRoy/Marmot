package me.jinheng.cityullm.models;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

public class Utils {

    public static void copyFileFromAssets(Context context, String fileName, String destinationPath) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // 打开 assets 中的文件输入流
            inputStream = context.getAssets().open(fileName);
            // 创建输出文件的输出流
            File outFile = new File(destinationPath);
            outputStream = new FileOutputStream(outFile);

            // 用于存储临时数据的缓冲区
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            // 尝试设置执行权限
            if (!outFile.setExecutable(true, false)) {
                throw new IOException("Failed to set execute permission for the file.");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    public static String fetchJSONData(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                System.out.println("GET request not worked");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // TODO: 获取可用内存大小
    public static long getTotalMemory() {

        return 0;
    }

}
