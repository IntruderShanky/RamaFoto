package com.islabs.ramafoto.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islabs.ramafoto.R;

public class FAQs extends Fragment {

    private FAQsCallback callback;

    public FAQs() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faqs, container, false);
        callback.setToolbarTitle("F.A.Qs", "");

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FAQsCallback) {
            callback = (FAQsCallback) context;
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


    public interface FAQsCallback {
        void setToolbarTitle(String s, String s1);

        void onAttachToHome(boolean attach);
    }
}
