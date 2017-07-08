package com.islabs.ramafoto.Callbacks;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by shanky on 1/6/17.
 */

public interface PagerCallback {
    void onAlbumDelete(String albumPin);

    void onShare(String albumPin, Uri imageUri);

    void callPhotoStudio(String contactNumber);

    void viewAlbum(String albumPin);
    void getAlbum(String pin);

    void showMessage(String message);

}