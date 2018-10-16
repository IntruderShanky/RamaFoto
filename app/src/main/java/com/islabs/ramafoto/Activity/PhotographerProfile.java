package com.islabs.ramafoto.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.intrusoft.indicator.Flare;
import com.intrusoft.milano.Milano;
import com.intrusoft.milano.NetworkConnection;
import com.intrusoft.milano.OnRequestComplete;
import com.islabs.ramafoto.Adapters.PortfolioPagerAdapter;
import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Utils.StaticData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhotographerProfile extends AppCompatActivity implements OnRequestComplete, View.OnClickListener {

    private Toolbar toolbar;
    private ViewPager pager;
    private Flare pagerIndicator;
    private LinearLayout youtube;
    private TextView photographerName, photographerDescription, contact, email, location, whatsApp;
    private ImageView facebook, instagram;

    private List<String> imageUrls;

    String contactNumber, whatsAppNumber, emailAddress, facebookLink, instagramLink, youtubeLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photographer_profile);

        toolbar = findViewById(R.id.toolbar);
        pager = findViewById(R.id.profile_pager);
        pagerIndicator = findViewById(R.id.pager_indicator);
        youtube = findViewById(R.id.open_youtube);
        photographerName = findViewById(R.id.photographer_name);
        photographerDescription = findViewById(R.id.about_photographer);
        contact = findViewById(R.id.photographer_contact_number);
        whatsApp = findViewById(R.id.photographer_mobile);
        email = findViewById(R.id.photographer_email);
        location = findViewById(R.id.photographer_address);
        facebook = findViewById(R.id.facebook);
        instagram = findViewById(R.id.instagram);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fetchProfile();
    }

    private void fetchProfile() {
        if (!NetworkConnection.isConnected(this)) {
            Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        String photographerId = getIntent().getStringExtra("id");

        Milano.Builder builder = new Milano.Builder(this);
        builder.shouldDisplayDialog(true);

        Uri url = Uri.parse(StaticData.PORTFOLIO).buildUpon().appendQueryParameter("photographer_id", photographerId).build();
        builder.build().fromURL(url.toString()).doGet().execute(this);
    }

    @Override
    public void onSuccess(String response, int responseCode) {
        try {
            JSONObject result = new JSONObject(response);

            contactNumber = result.getString("photographer_contact");
            emailAddress = result.getString("email");
            whatsAppNumber = result.getString("mobile");
            facebookLink = result.getString("facebook_link");
            youtubeLink = result.getString("youtube_video_link");
            instagramLink = result.getString("instagram_link");
            String name = result.getString("photographer_name");
            photographerName.setText(name);
            contact.setText(contactNumber);
            location.setText(result.getString("photographer_address"));
            email.setText(emailAddress);
            whatsApp.setText(whatsAppNumber);
            photographerDescription.setText(result.getString("profile_description"));

            imageUrls = new ArrayList<>();
            String url = result.getString("portfolio_1");
            if (checkString(url)) imageUrls.add(url);
            url = result.getString("portfolio_2");
            if (checkString(url)) imageUrls.add(url);
            url = result.getString("portfolio_3");
            if (checkString(url)) imageUrls.add(url);
            url = result.getString("portfolio_4");
            if (checkString(url)) imageUrls.add(url);

            pagerIndicator.setUpWithViewPager(pager);

            if (imageUrls.size() > 0) {
                PortfolioPagerAdapter portfolioPagerAdapter = new PortfolioPagerAdapter(this, imageUrls);
                pager.setAdapter(portfolioPagerAdapter);
            } else {
                findViewById(R.id.portfolio_pager_parent).setVisibility(View.GONE);
            }
            contact.setOnClickListener(this);
            email.setOnClickListener(this);
            whatsApp.setOnClickListener(this);
            youtube.setOnClickListener(this);
            facebook.setOnClickListener(this);
            instagram.setOnClickListener(this);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(name);
            findViewById(R.id.root_layout).setVisibility(View.VISIBLE);
            setUpVisibility();

            if(result.has("profile_photograph")){
                String imageUrl = result.getString("profile_photograph");
                if(checkString(imageUrl)){
                    CircleImageView imageView = findViewById(R.id.photographer_image);
                    Glide.with(this).load(result.getString("profile_photograph")).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpVisibility() {
        findViewById(R.id.portfolio_pager_parent).setVisibility(!imageUrls.isEmpty() ? View.VISIBLE : View.GONE);
        youtube.setVisibility(checkString(youtubeLink) ? View.VISIBLE : View.GONE);
        facebook.setVisibility(checkString(facebookLink) ? View.VISIBLE : View.GONE);
        whatsApp.setVisibility(checkString(whatsAppNumber) ? View.VISIBLE : View.GONE);
        instagram.setVisibility(checkString(instagramLink) ? View.VISIBLE : View.GONE);
        email.setVisibility(checkString(emailAddress) ? View.VISIBLE : View.GONE);
        contact.setVisibility(checkString(contactNumber) ? View.VISIBLE : View.GONE);
        location.setVisibility(checkString(location.getText().toString()) ? View.VISIBLE : View.GONE);
        photographerDescription.setVisibility(checkString(photographerDescription.getText().toString()) ? View.VISIBLE : View.GONE);
        photographerName.setVisibility(checkString(photographerName.getText().toString()) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onError(String error, int errorCode) {
        System.out.println(error);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.photographer_contact_number:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contactNumber));
                startActivity(intent);
                break;
            case R.id.facebook:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(facebookLink));
                startActivity(intent);
                break;
            case R.id.instagram:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(instagramLink));
                startActivity(intent);
                break;
            case R.id.open_youtube:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(youtubeLink));
                startActivity(intent);
                break;
            case R.id.photographer_mobile:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + whatsAppNumber));
                startActivity(intent);
                break;
            case R.id.photographer_email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", emailAddress, null));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
        }
    }

    private boolean checkString(String str) {
        return str != null && str.length() > 0 && !str.equals("null");
    }
}
