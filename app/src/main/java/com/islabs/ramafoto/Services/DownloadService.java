package com.islabs.ramafoto.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadService extends IntentService {

    String pin;
    private static int downloaded;
    private int total;
    private String fileName;
    private ImageData[] imageDatas;
    ThreadGroup threadGroup;
    int imageDownloadPointer = -1;

    public DownloadService() {
        super(":download_service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        System.out.println(intent.getStringExtra("json"));
        pin = intent.getStringExtra("pin");
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("json"));
            addAlbumToDatabase(object.getJSONArray("album_photos"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAlbumToDatabase(JSONArray photos) {
        total = photos.length();
        threadGroup = new ThreadGroup("download_images");
        try {
            File app = new File(getFilesDir().toString().concat(File.separator).concat(pin));
            System.out.println("FILE PATH: " + app.getPath());
            if (!app.exists()) {
                app.mkdir();
            }
            imageDatas = new ImageData[total];
            for (int i = 0; i < total; i++) {
                JSONObject photo = photos.getJSONObject(i);
                String fileName = "image" + photo.getInt("page_number") + ".jpg";
                String path = app.getPath().concat(File.separator).concat(fileName);
                imageDatas[i] = new ImageData(photo.getString("src"), path);
            }
            int tempDownloadLimit = (total > 5 ? 5 : total);
            for (int i = 0; i < tempDownloadLimit; i++) {
                downloadImage(imageDatas[++imageDownloadPointer]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("On Create Service");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        System.out.println("on Rebind service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("On Destroy Service");
    }

    private void downloadImage(final ImageData imageData) {
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                int count = 0;
                System.out.println(imageData.getFilePath());
                System.out.println(imageData.getUrl());
                URL url = null;
                HttpURLConnection conection = null;
                try {
                    url = new URL(imageData.getUrl());
                    conection = (HttpURLConnection) url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();
                    System.out.println("Content Length: " + lenghtOfFile);
                    InputStream input = new BufferedInputStream(conection.getInputStream(), 8192);

                    OutputStream output = new FileOutputStream(imageData.getFilePath());

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }

                    downloaded++;
                    System.out.println("Downloaded: " + downloaded);

                    output.flush();
                    output.close();
                    input.close();
                    setNextImageUrl();

                } catch (Exception e) {
                    try {
                        int code = conection.getResponseCode();
                        System.out.println(code + " " + conection.getURL().toString());
                        e.printStackTrace();

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        thread.start();

    }

    void setNextImageUrl() {
        if (++imageDownloadPointer < imageDatas.length)
            downloadImage(imageDatas[imageDownloadPointer]);
    }

    private class ImageData {
        String url;
        String filePath;

        ImageData(String url, String filePath) {
            this.url = url;
            this.filePath = filePath;
        }

        String getUrl() {
            return url;
        }

        String getFilePath() {
            return filePath;
        }
    }

}
