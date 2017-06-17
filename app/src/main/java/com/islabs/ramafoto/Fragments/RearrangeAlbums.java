package com.islabs.ramafoto.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islabs.ramafoto.Adapters.AlbumAdapter;
import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.Helper.OnStartDragListener;
import com.islabs.ramafoto.Helper.SimpleItemTouchHelperCallback;
import com.islabs.ramafoto.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RearrangeAlbums extends Fragment implements OnStartDragListener {

    private AllAlbumsCallback callback;
    private AlbumAdapter adapter;
    private DatabaseHelper helper;

    @BindView(R.id.all_albums)
    RecyclerView allAlbums;
    private ItemTouchHelper mItemTouchHelper;

    public RearrangeAlbums() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_albums, container, false);
        ButterKnife.bind(this, view);
        Cursor cursor = helper.getAllAlbums();
        adapter = new AlbumAdapter(getContext(), cursor, this);
        allAlbums.setLayoutManager(new GridLayoutManager(getContext(), 2));
        allAlbums.setHasFixedSize(true);
        allAlbums.setAdapter(adapter);
        ItemTouchHelper.Callback helperCallback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(helperCallback);
        mItemTouchHelper.attachToRecyclerView(allAlbums);
        callback.setToolbarTitle("Rearrange Albums", "");
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (helper == null)
            helper = new DatabaseHelper(context);
        helper.open();
        if (context instanceof AllAlbumsCallback) {
            callback = (AllAlbumsCallback) context;
            callback.onAttachToHome(true);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AllAlbumsCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback.onAttachToHome(false);
        callback = null;
        helper.close();
    }

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public interface AllAlbumsCallback {
        void setToolbarTitle(String title, String subTitle);

        void onAttachToHome(boolean attach);
    }
}
