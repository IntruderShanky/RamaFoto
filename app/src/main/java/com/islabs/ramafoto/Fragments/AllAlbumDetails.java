package com.islabs.ramafoto.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.islabs.ramafoto.Adapters.AlbumPagerAdapter;
import com.islabs.ramafoto.Callbacks.PagerCallback;
import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Transformers.StackTransformer;
import com.islabs.ramafoto.Utils.NetworkConnection;

import java.io.File;

public class AllAlbumDetails extends Fragment implements PagerCallback {

    private AllAlbumsDetailsCallback callback;
    private DatabaseHelper helper;
    private ViewPager albumsPager;

    public AllAlbumDetails() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_album_details, container, false);
        albumsPager = view.findViewById(R.id.all_albums_pager);
        TextView addNewInfo = view.findViewById(R.id.add_album_info);
        albumsPager.setPageMargin(30);
        albumsPager.setPageTransformer(false, new StackTransformer(getContext()));
        albumsPager.setOffscreenPageLimit(3);
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
        System.out.println("in refress");
        Cursor cursor = helper.getAllAlbums();
        AlbumPagerAdapter adapter = new AlbumPagerAdapter(getContext(), cursor, AllAlbumDetails.this);
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
        if(getContext() == null)
            return;
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Delete Album");
        dialog.setMessage("Do you want to delete this album?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAlbum(albumPin);
                refreshPager();
            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();

    }

    void deleteAlbum(String albumPin) {
        if(getContext()==null)
            return;
        helper.deleteAlbum(albumPin);
        File file = new File(getContext().getFilesDir().getPath()
                .concat(File.separator).concat(albumPin));
        deleteRecursive(file);
    }

    @Override
    public void onShare(String albumPin, Uri image, String eventName) {
        if(getContext()==null)
            return;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9);
            }
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
        Uri imageUri = Uri.parse(MediaStore.Images.Media
                .insertImage(getContext().getContentResolver(), bitmap, albumPin, "Cover Image"));
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "View my Photobook '" + eventName +
                "' at RamaFoto. Download the app from ramaphotoexpress.com/app & use pin : " + albumPin);
        getContext().startActivity(Intent.createChooser(sharingIntent, "Share Album"));
    }

    @Override
    public void callPhotoStudio(String contactNumber) {
        if(getContext() == null)
            return;
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + contactNumber));
        getContext().startActivity(callIntent);
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    @Override
    public void viewAlbum(String albumPin) {
        callback.viewAlbum(albumPin);
    }

    @Override
    public void getAlbum(String pin) {
        if(getContext() == null)
            return;
        if (NetworkConnection.isConnected(getContext())) {
            deleteAlbum(pin);
            callback.getAlbum(pin);
        } else
            callback.showMessage("Internet connection unavailable..");
    }

    @Override
    public void showMessage(String message) {
        callback.showMessage(message);
    }

    public interface AllAlbumsDetailsCallback {
        void setToolbarTitle(String title, String subTitle);

        void viewAlbum(String AlbumPin);

        void addNewAlbum();

        void getAlbum(String pin);

        void showMessage(String s);
    }

}
