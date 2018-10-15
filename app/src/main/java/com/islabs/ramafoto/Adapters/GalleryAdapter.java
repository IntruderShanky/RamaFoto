package com.islabs.ramafoto.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.islabs.ramafoto.Activity.AlbumViewActivity;
import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;
    private AlbumViewActivity.GalleryListener galleryListener;

    public GalleryAdapter(Context context, Cursor cursor, AlbumViewActivity.GalleryListener galleryListener) {
        this.context = context;
        this.cursor = cursor;
        this.galleryListener = galleryListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        cursor.moveToPosition(position);
        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA)));
        holder.albumCover.setImageURI(uri);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryListener.onItemSelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        ImageView albumCover;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}