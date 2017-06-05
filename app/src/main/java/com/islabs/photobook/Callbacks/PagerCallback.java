package com.islabs.photobook.Callbacks;

import android.graphics.Bitmap;

/**
 * Created by shanky on 1/6/17.
 */

public interface PagerCallback {
    void onAlbumDelete(String albumPin);

    void onShare(String albumPin, Bitmap bitmap);

    void callPhotoStudio(String contactNumber);

    void viewAlbum(String albumPin);
    void getAlbum(String pin);

}