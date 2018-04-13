package com.gdu.gdusocketdemo.usb;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhangzhilai on 2018/1/25.
 */

public class APTest {

    public static final int CONNECT_TIMEOUT = 5000; // ms
    public static final int READ_TIMEOUT = 5000; // ms

    public static String sendGet(String url) {
        try {
            URL url1 = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
            httpURLConnection.setConnectTimeout(READ_TIMEOUT);
            httpURLConnection.setRequestMethod("GET");
            if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
                InputStream inputStream = httpURLConnection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int len = 0;
                while (-1 != (len = inputStream.read(bytes))) {
                    byteArrayOutputStream.write(bytes, 0, len);
                }
                inputStream.close();
                byteArrayOutputStream.close();
                return byteArrayOutputStream.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
