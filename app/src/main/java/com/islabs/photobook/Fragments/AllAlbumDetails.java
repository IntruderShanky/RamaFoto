package com.islabs.photobook.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.islabs.photobook.Adapters.AlbumPagerAdapter;
import com.islabs.photobook.Callbacks.PagerCallback;
import com.islabs.photobook.Helper.DatabaseHelper;
import com.islabs.photobook.R;
import com.islabs.photobook.Transformers.StackTransformer;
import com.islabs.photobook.Utils.StaticData;

public class AllAlbumDetails extends Fragment implements PagerCallback {

    private AllAlbumsDetailsCallback callback;
    private DatabaseHelper helper;
    private AlbumPagerAdapter adapter;
    private ViewPager albumsPager;
    private TextView addNewInfo;

    public AllAlbumDetails() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_album_details, container, false);
        albumsPager = (ViewPager) view.findViewById(R.id.all_albums_pager);
        addNewInfo = (TextView) view.findViewById(R.id.add_album_info);
        Cursor cursor = helper.getAllAlbums();
        callback.setToolbarTitle("My Albums", cursor.getCount() + " Albums");
        adapter = new AlbumPagerAdapter(getContext(), cursor, this);
        albumsPager.setAdapter(adapter);
        albumsPager.setPageMargin(30);
        albumsPager.setPageTransformer(false, new StackTransformer(getContext()));
        albumsPager.setOffscreenPageLimit(3);
        if (cursor.getCount() == 0)
            callback.addNewAlbum();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (helper == null)
            helper = new DatabaseHelper(context);
        helper.open();
        if (context instanceof AllAlbumsDetailsCallback) {
            callback = (AllAlbumsDetailsCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AllAlbumsDetailsCallback");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPager();
    }

    private void refreshPager() {
        Cursor cursor = helper.getAllAlbums();
        adapter = new AlbumPagerAdapter(getContext(), cursor, AllAlbumDetails.this);
        albumsPager.setAdapter(adapter);
        callback.setToolbarTitle("My Albums", cursor.getCount() + " Albums");
        if (cursor.getCount() == 0)
            callback.addNewAlbum();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
        helper.close();
    }

    @Override
    public void onAlbumDelete(final String albumPin) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Delete Album");
        dialog.setMessage("Do you want to delete this album?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                helper.deleteAlbum(albumPin);
                refreshPager();
            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();

    }

    @Override
    public void onShare(String albumPin, Bitmap bitmap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9);
            }
            return;
        }
        Uri uri = Uri.parse("http://weddjunction.com/site/album");
        Uri imageUri = Uri.parse(MediaStore.Images.Media
                .insertImage(getContext().getContentResolver(), bitmap, albumPin, "Cover Image"));
        uri = uri.buildUpon().appendQueryParameter(StaticData.ALBUM_PIN, albumPin).build();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "View my album here " + uri.toString());
        getContext().startActivity(Intent.createChooser(sharingIntent, "Share Album"));
    }

    @Override
    public void callPhotoStudio(String contactNumber) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + contactNumber));
        getContext().startActivity(callIntent);
    }

    @Override
    public void viewAlbum(String albumPin) {
        callback.viewAlbum(albumPin);
    }

    @Override
    public void getAlbum(String pin) {
        helper.deleteAlbum(pin);
        callback.getAlbum(pin);
    }

    public interface AllAlbumsDetailsCallback {
        void setToolbarTitle(String title, String subTitle);

        void viewAlbum(String AlbumPin);

        void addNewAlbum();

        void getAlbum(String pin);
    }

}
