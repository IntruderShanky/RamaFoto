package com.islabs.ramafoto.Activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.islabs.ramafoto.Adapters.GalleryAdapter;
import com.islabs.ramafoto.Animations.AlbumPage;
import com.islabs.ramafoto.Animations.AlbumView;
import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Utils.StaticData;

import java.io.File;

public class AlbumViewActivity extends AppCompatActivity {

    private AlbumView albumView;
    private DatabaseHelper helper;
    private RecyclerView galleryView;
    private ObjectAnimator fadeOut, fadeIn;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fadeOut.start();
        }
    };
    private Handler handler = new Handler();
    private float topMargin = 0, sideMargin = 0;
    private ImageView pauseVolume;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        int index = 0;
        if (getLastNonConfigurationInstance() != null) {
            index = (Integer) getLastNonConfigurationInstance();
        }

        helper = new DatabaseHelper(this);
        Intent intent = getIntent();
        String albumId = intent.getStringExtra(StaticData.ALBUM_PIN);

        helper.open();
        Cursor albumDetails = helper.getAlbumDetailsById(albumId);
        Cursor cursor = helper.getImagesOfAlbum(albumId);
        if (!cursor.moveToFirst()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Invalid Album");
            dialog.setMessage("Try adding again this album.");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlbumViewActivity.this.finish();
                }
            });
            dialog.show();
            return;
        }

        galleryView = findViewById(R.id.gallery);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        galleryView.setLayoutManager(linearLayoutManager);
        galleryView.setHasFixedSize(true);
        galleryView.setAdapter(new GalleryAdapter(this, cursor, new GalleryListener() {
            @Override
            public void onItemSelected(int position) {
                if (position == 0)
                    albumView.setCurrentIndex(0);
                else
                    albumView.setCurrentIndex((position / 2) + 1);
            }
        }));
        albumView = findViewById(R.id.album_view);
        albumDetails.moveToFirst();
        cursor.moveToFirst();
        String coverFile = getFilesDir().getPath().concat(File.separator).concat(albumId)
                .concat(File.separator).concat("cover.jpg");
        String backFile = getFilesDir().getPath().concat(File.separator).concat(albumId)
                .concat(File.separator).concat("back.jpg");
        calculateScaleRatio(cursor.getString(cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA)));
        albumView.setPageProvider(new PageProvider(cursor, coverFile, backFile));
        albumView.setSizeChangedObserver(new SizeChangedObserver());
        albumView.setCurrentIndex(index);
        albumView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        fadeOut = ObjectAnimator.ofFloat(galleryView, "alpha", 1f, 0f);
        fadeOut.setDuration(400);
        fadeIn = ObjectAnimator.ofFloat(galleryView, "alpha", 0f, 1f);
        fadeIn.setDuration(400);
        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                galleryView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                galleryView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        handler.postDelayed(runnable, 2000);
        findViewById(R.id.bottom_view).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    fadeIn.start();
                if (event.getAction() == MotionEvent.ACTION_UP)
                    handler.postDelayed(runnable, 2000);
                return true;
            }
        });

        galleryView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 2000);
            }
        });

        mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        pauseVolume = findViewById(R.id.pause_volume);
//        pauseVolume.setVisibility(View.GONE);
        pauseVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    pauseVolume.setImageResource(R.drawable.ic_volume_off);
                } else {
                    mediaPlayer.start();
                    pauseVolume.setImageResource(R.drawable.ic_volume);
                }
            }
        });
    }

    private void calculateScaleRatio(String image) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(image, options);

        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        float ratio = ((float) bitmapHeight) / ((float) bitmapWidth);

        if (bitmapHeight < bitmapWidth) {
            sideMargin = 0;
            int ratioHeight = (int) (displayMetrics.widthPixels * ratio) / 2;
            float scaleFactor = (displayMetrics.heightPixels - ratioHeight) / 2;
            topMargin = ((100 * scaleFactor) / displayMetrics.heightPixels) * .01f;
        } else {
            topMargin = 0;
            int ratioWidth = (int) (displayMetrics.heightPixels * ratio);
            float scaleFactor = (displayMetrics.widthPixels - ratioWidth) / 2;
            sideMargin = ((100 * scaleFactor) / displayMetrics.widthPixels) * .01f;
        }
    }

    /**
     * Bitmap provider.
     */
    private class PageProvider implements AlbumView.PageProvider {

        Cursor cursor;
        String cover, back;
        int pageCount;

        PageProvider(Cursor cursor, String cover, String back) {
            this.cursor = cursor;
            this.cover = cover;
            this.back = back;
            if (cursor.getCount() % 2 != 0)
                pageCount = ((cursor.getCount() + 2) / 2) + 1;
            pageCount = (cursor.getCount() + 2) / 2;

        }

        @Override
        public int getPageCount() {
            return pageCount;
        }

        @Override
        public void updatePage(final AlbumPage page, final int width, final int height, final int index) {
            System.out.println(galleryView.getAdapter().getItemCount());
            Bitmap front, back;
            boolean atLast = false;
            if (cursor.getCount() > index * 2)
                cursor.moveToPosition(index * 2);
            else {
                atLast = true;
            }
            if (index == 0) {
                //linearLayoutManager.scrollToPosition(0);
                front = getBitmapFromFile(this.cover, false);
                back = getBitmapFromFile(cursor.getString(
                        cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA)), true);
            } else if (atLast) {
                // linearLayoutManager.scrollToPosition(cursor.getCount() - 1);
                cursor.moveToLast();
                front = getBitmapFromFile(cursor.getString(
                        cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA)), false);
                back = getBitmapFromFile(this.back, true);

            } else {
                //  linearLayoutManager.scrollToPosition(index * 2);
                back = getBitmapFromFile(cursor.getString(
                        cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA)), true);
                cursor.moveToPrevious();
                front = getBitmapFromFile(cursor.getString(
                        cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA)), false);
            }


            if (front != null)
                page.setTexture(front, AlbumPage.SIDE_FRONT);
            if (back != null)
                page.setTexture(back, AlbumPage.SIDE_BACK);
        }

    }

    public Bitmap getBitmapFromFile(String image, boolean reverse) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(image, options);
        if (reverse) {
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }


    /**
     * CurlView size changed observer.
     */
    private class SizeChangedObserver implements AlbumView.SizeChangedObserver {
        @Override
        public void onSizeChanged(int w, int h) {
            albumView.setViewMode(AlbumView.SHOW_TWO_PAGES);
            albumView.setMargins(sideMargin, topMargin, sideMargin, topMargin);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.close();
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface GalleryListener {
        void onItemSelected(int position);
    }
}