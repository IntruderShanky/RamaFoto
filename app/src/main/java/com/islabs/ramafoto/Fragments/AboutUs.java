package com.islabs.ramafoto.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islabs.ramafoto.R;

public class AboutUs extends Fragment {

    private AboutUsCallback callback;

    public AboutUs() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);
        callback.setToolbarTitle("About Us", "");
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AboutUsCallback) {
            callback = (AboutUsCallback) context;
            callback.onAttachToHome(true);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FAQsCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback.onAttachToHome(false);
        callback = null;
    }


    public interface AboutUsCallback {
        void setToolbarTitle(String s, String s1);

        void onAttachToHome(boolean attach);
    }
}
