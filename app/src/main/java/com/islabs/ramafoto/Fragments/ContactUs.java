package com.islabs.ramafoto.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.islabs.ramafoto.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactUs extends Fragment {

    private ContactUsCallback callback;


    private String[] subjects = new String[]{"Enquire a photographer.", "Problem with albums."};
    @BindView(R.id.message)
    public EditText message;

    @BindView(R.id.subject)
    public Spinner subject;

    @OnClick(R.id.submit)
    public void submit(View view) {
        if (message.getText().toString().length() < 5)
            callback.showMessage("Message field required");
        else
            callback.contactUs(message.getText().toString(), subjects[subject.getSelectedItemPosition()]);
    }

    public ContactUs() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        ButterKnife.bind(this, view);
        subject.setAdapter(new ArrayAdapter<>(getContext(), R.layout.user_view, R.id.user, subjects));
        subject.setSelection(0);
        System.out.println("Create view contact us");
        callback.setToolbarTitle("Contact Us", "");
        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ContactUsCallback) {
            callback = (ContactUsCallback) context;
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


    public interface ContactUsCallback {

        void showMessage(String s);

        void contactUs(String message, String subject);

        void setToolbarTitle(String s, String s1);

        void onAttachToHome(boolean attach);
    }
}
