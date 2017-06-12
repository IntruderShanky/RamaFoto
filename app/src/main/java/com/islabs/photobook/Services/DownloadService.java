package com.islabs.photobook.Services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.islabs.photobook.Helper.DatabaseHelper;
import com.islabs.photobook.Utils.StaticData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class DownloadService extends IntentService {

    DatabaseHelper helper;
    String pin;

    public DownloadService() {
        super("download_service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        System.out.println(intent.getStringExtra("json"));
        pin = intent.getStringExtra("pin");
        helper.open();
        try {
            JSONObject object = new JSONObject(intent.getStringExtra("json"));
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
            albumDetails.put(DatabaseHelper.INDEX, helper.getAllAlbums().getCount());
            addAlbumToDatabase(object, pin, albumDetails, object.getInt("photo_count") + 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void addAlbumToDatabase(final JSONArray photos, final ContentValues albumDetails, int position, final int total) {
//        try {
//            final JSONObject photo = photos.getJSONObject(position);
//            Bitmap resource = Glide.with(this).load(photo.getString("src"))
//                    .asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .get();
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//            System.out.println("Progress From Service : " + ((total * (position + 3)) / 100));
//            Intent localIntent = new Intent(StaticData.BROADCAST_ACTION);
//            localIntent.putExtra("progress", ((total * (position + 3)) / 100));
//            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
//            position++;
//            if (position == photos.length()) {
//                localIntent.putExtra("progress", ((total * (position + 3)) / 100));
//                localIntent.putExtra("completed", true);
//                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
//            } else
//                addAlbumToDatabase(photos, null, position, total);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void addAlbumToDatabase(Object data, String pin, ContentValues albumDetails, int total) {
        try {
            if (data instanceof JSONObject && albumDetails != null) {
                JSONObject object = (JSONObject) data;
                String key = DatabaseHelper.COVER;
                if (albumDetails.containsKey(key)) {
                    key = DatabaseHelper.BACK;
                }
                Bitmap resource = Glide.with(this).load(object.getString(key)).asBitmap()
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                albumDetails.put(key, byteArray);
                if (key.equals(DatabaseHelper.COVER)) {
                    sendProgress(100 / total, false);
                    addAlbumToDatabase(object, pin, albumDetails, total);
                } else {
                    sendProgress(200 / total, false);
                    helper.insertAlbum(DatabaseHelper.ALBUM_DETAILS, albumDetails);
                    addAlbumToDatabase(object.getJSONArray("album_photos"), pin, null, total);
                }
            } else {
                JSONArray photos = (JSONArray) data;
                for (final int[] i = {0}; i[0] < photos.length(); i[0]++) {
                    JSONObject photo = photos.getJSONObject(i[0]);
                    Bitmap resource = Glide.with(this).load(photo.getString("src")).asBitmap()
                            .listener(new RequestListener<String, Bitmap>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                    sendProgress(-1, false);
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    ContentValues image = new ContentValues();
                    image.put(DatabaseHelper.ALBUM_ID, pin);
                    image.put(DatabaseHelper.IMAGE_NUM, photo.getInt("page_number"));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    image.put(DatabaseHelper.IMAGE_DATA, byteArray);
                    helper.insertAlbum(DatabaseHelper.ALBUMS_TABLE, image);
                    sendProgress((100 * (i[0] + 3) / total), false);
                }
                sendProgress(100, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendProgress(-1, false);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        helper = new DatabaseHelper(this);
        helper.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        helper.close();
    }

    private void sendProgress(int progress, boolean completed) {
        Intent intent = new Intent(StaticData.BROADCAST_ACTION);
        intent.putExtra("progress", progress);
        intent.putExtra("completed", completed);
        intent.putExtra("pin", pin);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        if (progress == -1)
            stopSelf();
    }
}
