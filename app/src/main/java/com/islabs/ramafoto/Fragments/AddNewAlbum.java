package com.islabs.ramafoto.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.islabs.ramafoto.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddNewAlbum extends Fragment {

    private NewAlbumCallback callback;

    @BindView(R.id.album_pin)
    public EditText albumPin;

    @OnClick(R.id.get_album)
    public void getAlbum(View view) {
        if (albumPin.getText().toString().length() < 5)
            callback.showMessage("Enter valid pin");
        else callback.getAlbum(albumPin.getText().toString(), true);
    }

    private Unbinder unbinder;

    public AddNewAlbum() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new_album, container, false);
        unbinder = ButterKnife.bind(this, view);
        albumPin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    getAlbum(null);
                return false;
            }
        });
        albumPin.setText("");
        callback.setToolbarTitle("Add New Album", "");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        albumPin.setText("");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NewAlbumCallback) {
            callback = (NewAlbumCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement NewAlbumCallback");
        }
        callback.onAttachToHome(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback.onAttachToHome(false);
        callback = null;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    public interface NewAlbumCallback {
        void getAlbum(String pin, boolean completed);

        void showMessage(String error);

        void setToolbarTitle(String title, String subTitle);

        void onAttachToHome(boolean attach);
    }
}
