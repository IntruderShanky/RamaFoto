package com.islabs.ramafoto.Services;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.Utils.StaticData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadService {

    private String pin;
    private static int downloaded;
    private int total;
    private ImageData[] imageDatas;
    private ThreadGroup threadGroup;
    private int imageDownloadPointer = -1;
    private Context context;
    private ImageData backPhoto;
    private JSONObject object;
    private DatabaseHelper helper;
    private int errorCount;
    private boolean downloadCancled;


    public void startDownload(Intent intent, Context context) {
        helper = new DatabaseHelper(context);
        downloadCancled = false;
        helper.open();
        this.context = context;
        assert intent != null;
        pin = intent.getStringExtra("pin");
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("json"));
            addAlbumToDatabase(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAlbumToDatabase(Object main) {
        downloaded = 0;
        errorCount = 0;
        File app = new File(context.getFilesDir().toString().concat(File.separator).concat(pin));
        if (!app.exists()) {
            app.mkdir();
        }
        try {
            JSONArray photos;
            if (main instanceof JSONObject) {
                object = (JSONObject) main;
                photos = object.getJSONArray("album_photos");
                total = photos.length() + 2;
                imageDatas = new ImageData[photos.length()];
                ImageData coverPhoto = new ImageData(object.getString("cover_photo"), app.getPath().concat(File.separator).concat("cover.jpg"), "cover", -1);
                backPhoto = new ImageData(object.getString("back_photo"), app.getPath().concat(File.separator).concat("back.jpg"), "back", -1);
                downloadImage(coverPhoto);
            } else {
                photos = (JSONArray) main;
                total = photos.length();
                imageDatas = new ImageData[total];
            }
            sendProgress();
            threadGroup = new ThreadGroup("download_images");
            for (int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                String fileName = "image" + photo.getInt("page_number") + ".jpg";
                String path = app.getPath().concat(File.separator).concat(fileName);
                imageDatas[i] = new ImageData(photo.getString("src"), path, "image", photo.getInt("page_number"));
            }
            if (main instanceof JSONArray) {
                int tempDownloadLimit = (total > 5 ? 5 : total);
                for (int i = 0; i < tempDownloadLimit; i++) {
                    downloadImage(imageDatas[++imageDownloadPointer]);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void downloadImage(final ImageData imageData) {
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                int count = 0;
                URL url = null;
                HttpURLConnection conection = null;
                try {
                    url = new URL(imageData.getUrl());
                    conection = (HttpURLConnection) url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();
                    InputStream input = new BufferedInputStream(conection.getInputStream(), 8192);

                    OutputStream output = new FileOutputStream(imageData.getFilePath());

                    byte data[] = new byte[1024];

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                    downloaded++;
                    output.flush();
                    output.close();
                    input.close();

                    if (downloadCancled)
                        return;

                    switch (imageData.getType()) {
                        case "cover":
                            downloadImage(backPhoto);
                            break;
                        case "back":
                            storeAlbumToDatabase();
                            int tempDownloadLimit = (total > 5 ? 5 : total);
                            for (int i = 0; i < tempDownloadLimit; i++) {
                                downloadImage(imageDatas[++imageDownloadPointer]);
                            }
                            break;
                        default:
                            saveImageToDatabase(imageData);
                            setNextImageUrl();
                            break;
                    }

                } catch (Exception e) {
                    errorCount++;
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    private void saveImageToDatabase(ImageData imageData) {
        try {
            ContentValues image = new ContentValues();
            image.put(DatabaseHelper.ALBUM_ID, pin);
            image.put(DatabaseHelper.IMAGE_DATA, imageData.getFilePath());
            image.put(DatabaseHelper.IMAGE_NUM, imageData.getNum());
            helper.insertAlbum(DatabaseHelper.ALBUMS_TABLE, image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeAlbumToDatabase() {
        try {
            ContentValues albumDetails = new ContentValues();
            albumDetails.put(DatabaseHelper.NUM_IMAGES, object.getInt("photo_count"));
            albumDetails.put(DatabaseHelper.ALBUM_ID, pin);
            albumDetails.put(DatabaseHelper.VIEW_COUNT, 0);
            albumDetails.put(DatabaseHelper.ALBUM_VERSION, object.getString("version"));
            albumDetails.put(DatabaseHelper.ALBUM_NAME, object.getString("event_heading"));
            albumDetails.put(DatabaseHelper.EVENT_DETAILS, object.getString("event_detail"));
            albumDetails.put(DatabaseHelper.LAB_NAME, object.getString("lab_name"));
            albumDetails.put(DatabaseHelper.LAB_ADDRESS, object.getString("lab_address"));
            albumDetails.put(DatabaseHelper.LAB_CONTACT, object.getString("lab_contact"));
            albumDetails.put(DatabaseHelper.LAB_LOGO, object.getString("lab_logo"));
            albumDetails.put(DatabaseHelper.LAB_BANNER, object.getString("lab_banner"));
            albumDetails.put(DatabaseHelper.PHOTOGRAPHER_NAME, object.getString("photographer_name"));
            albumDetails.put(DatabaseHelper.LAB_LINK, object.getString("lab_link"));
            albumDetails.put(DatabaseHelper.PHOTOGRAPHER_LINK, object.getString("photographer_link"));
            albumDetails.put(DatabaseHelper.PHOTOGRAPHER_ADDRESS, object.getString("photographer_address"));
            albumDetails.put(DatabaseHelper.PHOTOGRAPHER_CONTACT, object.getString("photographer_contact"));
            albumDetails.put(DatabaseHelper.PHOTOGRAPHER_ID, object.getString("photographer_id"));
            albumDetails.put(DatabaseHelper.INDEX, helper.getAllAlbums().getCount());
            helper.insertAlbum(DatabaseHelper.ALBUM_DETAILS, albumDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setNextImageUrl() {
        sendProgress();
        if (++imageDownloadPointer < imageDatas.length)
            downloadImage(imageDatas[imageDownloadPointer]);
    }

    private class ImageData {
        private String url;
        private String filePath;
        private String type;
        private int num;

        ImageData(String url, String filePath, String type, int num) {
            this.url = url;
            this.filePath = filePath;
            this.type = type;
            this.num = num;
        }

        String getUrl() {
            return url;
        }

        String getType() {
            return type;
        }

        String getFilePath() {
            return filePath;
        }

        int getNum() {
            return num;
        }
    }

    private void sendProgress() {
        int progress = downloaded * 100 / total;
        Intent intent = new Intent(StaticData.BROADCAST_ACTION);
        intent.putExtra("progress", progress);
        intent.putExtra("pin", pin);
        intent.putExtra("completed", total == errorCount + downloaded);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void stopDownload() {
        downloadCancled = true;
        threadGroup.interrupt();
    }

}
