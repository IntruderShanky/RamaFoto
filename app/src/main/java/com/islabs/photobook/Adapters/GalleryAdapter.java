package com.islabs.photobook.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.islabs.photobook.Activity.AlbumViewActivity;
import com.islabs.photobook.Helper.DatabaseHelper;
import com.islabs.photobook.R;

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



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        cursor.moveToPosition(position);
        byte[] image = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.IMAGE_DATA));
        holder.albumCover.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
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
