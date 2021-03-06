package com.islabs.ramafoto.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.intrusoft.milano.Milano;
import com.intrusoft.milano.OnRequestComplete;
import com.islabs.ramafoto.Activity.PhotographerProfile;
import com.islabs.ramafoto.Callbacks.PagerCallback;
import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Utils.NetworkConnection;
import com.islabs.ramafoto.Utils.StaticData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

/**
 * Created by shanky on 31/5/17.
 */

public class AlbumPagerAdapter extends PagerAdapter {

    private Context context;
    private Cursor cursor;
    private PagerCallback pagerCallback;
    private DatabaseHelper helper;

    public AlbumPagerAdapter(Context context, Cursor cursor, PagerCallback pagerCallback) {
        this.context = context;
        this.cursor = cursor;
        this.pagerCallback = pagerCallback;
        helper = new DatabaseHelper(context);
        helper.open();
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.full_album_details, container, false);
        ImageView coverPhoto = view.findViewById(R.id.album_cover);
        ImageView delete = view.findViewById(R.id.delete);
        ImageView share = view.findViewById(R.id.share);

        Button openProfile = view.findViewById(R.id.open_profile);

        final TextView viewCount = view.findViewById(R.id.view_count);
        final TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDetails = view.findViewById(R.id.event_date);
        ImageView photographerLogo = view.findViewById(R.id.photographer_logo);
        final Button viewAlbum = view.findViewById(R.id.view_album);
        final Button downloadAgain = view.findViewById(R.id.redownload_album);
        TextView labName = view.findViewById(R.id.studio_name);
        final TextView labContact = view.findViewById(R.id.contact_number);
        final TextView missing_error = view.findViewById(R.id.image_missing_error);
        TextView labAddress = view.findViewById(R.id.studio_address);
        TextView photographerAddress = view.findViewById(R.id.photographer_address);
        final TextView photographerContact = view.findViewById(R.id.photographer_contact_number);
        TextView photographerName = view.findViewById(R.id.photographer_name);
        cursor.moveToPosition(position);
        labName.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAB_NAME)));
        labContact.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAB_CONTACT)));
        labAddress.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAB_ADDRESS)));
        photographerName.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHOTOGRAPHER_NAME)));
        viewCount.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIEW_COUNT)));
        photographerAddress.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHOTOGRAPHER_ADDRESS)));
        photographerContact.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHOTOGRAPHER_CONTACT)));
        Glide.with(context).load(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAB_LOGO))).into(photographerLogo);
        if (labName.getText().toString().isEmpty())
            view.findViewById(R.id.processed_by).setVisibility(View.GONE);
        else view.findViewById(R.id.processed_by).setVisibility(View.VISIBLE);
        if (photographerName.getText().toString().isEmpty())
            view.findViewById(R.id.photographed_by).setVisibility(View.GONE);
        else view.findViewById(R.id.photographed_by).setVisibility(View.VISIBLE);
        final String albumPin = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID));
        final Uri image = Uri.parse(context.getFilesDir().getPath().concat(File.separator).concat(albumPin)
                .concat(File.separator).concat("cover.jpg"));
        coverPhoto.setImageURI(image);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagerCallback.onShare(albumPin, image, eventName.getText().toString());
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagerCallback.onAlbumDelete(albumPin);
            }
        });
        photographerContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagerCallback.callPhotoStudio(photographerContact.getText().toString().split(",")[0]);
            }
        });


        labContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagerCallback.callPhotoStudio(labContact.getText().toString().split(",")[0]);
            }
        });
        eventDetails.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EVENT_DETAILS)));
        eventName.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_NAME)));
        viewAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagerCallback.viewAlbum(albumPin);
            }
        });
        coverPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAlbum.performClick();
            }
        });
        Uri uri = Uri.parse(StaticData.GET_VIEW_COUNT);
        uri = uri.buildUpon().appendQueryParameter(StaticData.ALBUM_PIN, albumPin)
                .appendQueryParameter("user_id", context.getSharedPreferences(StaticData.PREF, Context.MODE_PRIVATE)
                        .getString("uid", "")).build();

        downloadAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagerCallback.getAlbum(albumPin);
            }
        });
        labName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAB_LINK))));
                context.startActivity(intent);
            }
        });
        photographerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHOTOGRAPHER_LINK))));
                context.startActivity(intent);
            }
        });

        openProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PhotographerProfile.class);
                intent.putExtra("id", cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHOTOGRAPHER_ID)));
                context.startActivity(intent);
            }
        });
        if (NetworkConnection.isConnected(context))
            Milano.with(context).fromURL(uri.toString()).doGet().execute(new OnRequestComplete() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(String response, int responseCode) {
                    try {
                        JSONObject object = new JSONObject(response);
                        int count = object.getInt("count");
                        viewCount.setText(String.format(Locale.getDefault(), "%d", count));
                        helper.updateViewCount(albumPin, count);
                        Cursor cursor1 = helper.getAlbumDetailsById(albumPin);
                        cursor1.moveToFirst();
                        int currentVersion = cursor1.getInt(cursor1.getColumnIndex(DatabaseHelper.ALBUM_VERSION));
                        System.out.println(object.getInt("version") + "   Version  " + cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ALBUM_VERSION)) + "   " + currentVersion);
                        if (object.getInt("version") != currentVersion) {
                            missing_error.setVisibility(View.VISIBLE);
                            missing_error.setText("Album update available..");
                            downloadAgain.setVisibility(View.VISIBLE);
                            downloadAgain.setText("Update Album");
                        }
                        JSONArray users = object.getJSONArray("users");
                        final String[] userss = new String[users.length()];
                        for (int j = 0; j < users.length(); j++)
                            userss[j] = users.getString(j);
                        if (users.length() > 0)
                            viewCount.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Views");
                                    builder.setPositiveButton("Cancel", null);
                                    ListView listView = new ListView(context);
                                    listView.setAdapter(new ArrayAdapter<>(context, R.layout.user_view, R.id.user, userss));
                                    builder.setView(listView);
                                    builder.show();
                                }
                            });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error, int errorCode) {

                }
            });
        else
            viewCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pagerCallback.showMessage("Internet connection unavailable.");
                }
            });

        if (helper.getImagesOfAlbum(albumPin).getCount() != cursor.getInt(cursor.getColumnIndex(DatabaseHelper.NUM_IMAGES))) {
            missing_error.setVisibility(View.VISIBLE);
            downloadAgain.setText("Download Again");
            downloadAgain.setVisibility(View.VISIBLE);

        } else {
            missing_error.setVisibility(View.GONE);
            downloadAgain.setVisibility(View.GONE);
        }
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

//    public void setCursor(Cursor cursor) {
//        this.cursor = cursor;
//        this.notifyDataSetChanged();
//    }
}
