package com.islabs.ramafoto.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.Helper.ItemTouchHelperAdapter;
import com.islabs.ramafoto.Helper.ItemTouchHelperViewHolder;
import com.islabs.ramafoto.Helper.OnStartDragListener;
import com.islabs.ramafoto.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by shanky on 31/5/17.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> implements
        ItemTouchHelperAdapter {

    private Context context;
    private Cursor cursor;
    private DatabaseHelper helper;
    private OnStartDragListener mDragStartListener;

    public AlbumAdapter(Context context, Cursor cursor, OnStartDragListener mDragStartListener) {
        this.context = context;
        this.cursor = cursor;
        this.mDragStartListener = mDragStartListener;
        helper = new DatabaseHelper(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.album_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String pin = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID));
        Uri uri = Uri.parse(context.getFilesDir().getPath().concat(File.separator).concat(pin)
                .concat(File.separator).concat("cover.jpg"));
        holder.albumCover.setImageURI(uri);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, AlbumViewActivity.class);
//                intent.putExtra(StaticData.ALBUM_PIN, cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID)));
//                context.startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        helper.open();
        cursor.moveToPosition(fromPosition);
        helper.setIndex(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID)), toPosition, fromPosition);
        cursor = helper.getAllAlbums();
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    class ViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        @BindView(R.id.album_cover)
        ImageView albumCover;

        @BindView(R.id.select)
        View foreground;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onItemSelected() {
            foreground.setVisibility(View.VISIBLE);
        }

        @Override
        public void onItemClear() {
            foreground.setVisibility(View.INVISIBLE);
        }
    }
}
