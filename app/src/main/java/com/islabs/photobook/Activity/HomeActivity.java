package com.islabs.photobook.Activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.intrusoft.milano.Milano;
import com.intrusoft.milano.OnRequestComplete;
import com.islabs.photobook.Fragments.AddNewAlbum;
import com.islabs.photobook.Fragments.AllAlbumDetails;
import com.islabs.photobook.Fragments.ContactUs;
import com.islabs.photobook.Fragments.RearrangeAlbums;
import com.islabs.photobook.Helper.DatabaseHelper;
import com.islabs.photobook.R;
import com.islabs.photobook.Services.DownloadService;
import com.islabs.photobook.Utils.StaticData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AddNewAlbum.NewAlbumCallback,
        RearrangeAlbums.AllAlbumsCallback,
        AllAlbumDetails.AllAlbumsDetailsCallback,
        ContactUs.ContactUsCallback {

    private CoordinatorLayout rootLayout;
    private Milano.Builder milanoBuilder;
    private DatabaseHelper helper;
    private ProgressDialog progressDialog;
    private AddNewAlbum addNewAlbum;
    private RearrangeAlbums rearrangeAlbums;
    private AllAlbumDetails allAlbumDetails;
    private ContactUs contactUs;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private static final String HOME_STACK = "home_stack";
    private static final String REARRANGE_STACK = "rearrange_stack";
    private static final String ADD_NEW_STACK = "add_new_stack";
    private static final String CONTACT_US_STACK = "contact_us";
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        rootLayout = (CoordinatorLayout) findViewById(R.id.root_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new DatabaseHelper(this);
        preferences = getSharedPreferences(StaticData.PREF, MODE_PRIVATE);
        helper.open();
        progressDialog = new ProgressDialog(this);
        rearrangeAlbums = new RearrangeAlbums();
        allAlbumDetails = new AllAlbumDetails();
        addNewAlbum = new AddNewAlbum();
        contactUs = new ContactUs();
        fab = (FloatingActionButton) findViewById(R.id.add_new_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inStack(ADD_NEW_STACK))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.conatiner, addNewAlbum)
                            .addToBackStack(ADD_NEW_STACK)
                            .commit();
            }
        });

        milanoBuilder = new Milano.Builder(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView userName = (TextView) header.findViewById(R.id.user_name);
        TextView mobile = (TextView) header.findViewById(R.id.mobile);
        userName.setText(preferences.getString("name", "Guest"));
        mobile.setText(preferences.getString("mobile", ""));
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.conatiner, allAlbumDetails, HOME_STACK).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            helper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.rearrange_albums:
                if (inStack(REARRANGE_STACK))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.conatiner, rearrangeAlbums)
                            .addToBackStack(REARRANGE_STACK)
                            .commit();
                break;
            case R.id.contact_us:
                if (inStack(CONTACT_US_STACK))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.conatiner, contactUs)
                            .addToBackStack(CONTACT_US_STACK)
                            .commit();
                break;
            case R.id.home:
                getSupportFragmentManager().popBackStack();
                break;
            case R.id.add_new:
                fab.performClick();
                break;
//            case R.id.log_out:
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("Are you sure?\nYour albums will be deleted.")
//                        .setTitle("Logout")
//                        .setNegativeButton("No", null)
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                preferences.edit().clear().apply();
//                                helper.resetAlbums();
//                                startActivity(new Intent(HomeActivity.this, MainActivity.class));
//                                HomeActivity.this.finish();
//                            }
//                        });
//                builder.show();
//
//                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean inStack(String stackEntry) {
        boolean inStack = true;
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
//            System.out.println(getSupportFragmentManager().getBackStackEntryAt(i).getName());
//            if (getSupportFragmentManager().getBackStackEntryAt(i).getName().equals(stackEntry)) {
                getSupportFragmentManager().popBackStack();
//                inStack = false;
//            }
        }
        return inStack;
    }

    @Override
    public void getAlbum(final String pin) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = helper.getAlbumDetailsById(pin);
                if (cursor.moveToFirst()) {
                    showMessage("Album Already Exists!");
                    return;
                }
                Uri uri = Uri.parse(StaticData.GET_ALBUM);
                uri = uri.buildUpon().appendQueryParameter(StaticData.ALBUM_PIN, pin)
                        .appendQueryParameter("user_id", preferences.getString("uid", "")).build();
                milanoBuilder.setNetworkErrorMessage("You must be connected to internet")
                        .setDialogMessage("Fetching album info..").shouldDisplayDialog(true);
                milanoBuilder.build().fromURL(uri.toString()).doGet().execute(new OnRequestComplete() {
                    @Override
                    public void onSuccess(String response, int responseCode) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (object.getString("status").toLowerCase().equals("success")) {
                                if (object.getJSONArray("album_photos").length() < 1) {
                                    showMessage("No photos found in this album.");
                                    return;
                                }
                                progressDialog.setTitle("Downloading Album");
                                progressDialog.setMax(100);
                                progressDialog.setCancelable(false);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progressDialog.show();
                                Intent downloadIntent = new Intent(HomeActivity.this, DownloadService.class);
                                downloadIntent.putExtra("json", response);
                                downloadIntent.putExtra("pin", pin);
                                HomeActivity.this.startService(downloadIntent);
                                IntentFilter statusIntentFilter = new IntentFilter(
                                        StaticData.BROADCAST_ACTION);
                                DownloadReceiver statusReceiver = new DownloadReceiver();
                                LocalBroadcastManager.getInstance(HomeActivity.this).registerReceiver(statusReceiver, statusIntentFilter);
                            } else {
                                showMessage(object.getString("message"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String error, int errorCode) {

                    }
                });
            }
        });
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    public void showActionMessage(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_INDEFINITE).setAction("Okay", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
    }

    @Override
    public void contactUs(String message, String subject) {
        Cursor cursor = helper.getAllAlbums();
        String album = "";
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            album = album.concat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ALBUM_ID)));
            if (i != cursor.getCount() - 1)
                album = album.concat(",");
        }
        System.out.println(album);
        Milano.Builder builder = new Milano.Builder(this);
        builder.shouldDisplayDialog(true);
        builder.setDialogMessage("Submitting..");
        builder.setNetworkErrorMessage("Internet not connected");
        builder.addRequestParams("Ticket[user_id]", preferences.getString("uid", ""));
        builder.addRequestParams("Ticket[subject]", subject);
        builder.addRequestParams("Ticket[message]", message);
        builder.addRequestParams(" Ticket[albums]", album);
        Milano milano = builder.build();
        milano.fromURL(StaticData.CONTACT_US)
                .doPost("")
                .execute(new OnRequestComplete() {
                    @Override
                    public void onSuccess(String response, int responseCode) {
                        try{
                            JSONObject object = new JSONObject(response);
                            showActionMessage(object.getString("message"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        onBackPressed();
                    }

                    @Override
                    public void onError(String error, int errorCode) {
                        //Do whatever you want to do
                    }
                });
    }

    @Override
    public void setToolbarTitle(String title, String subTitle) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(subTitle);
    }

    @Override
    public void viewAlbum(String albumPin) {
        Intent intent = new Intent(HomeActivity.this, AlbumViewActivity.class);
        intent.putExtra(StaticData.ALBUM_PIN, albumPin);
        startActivity(intent);
    }

    @Override
    public void addNewAlbum() {
        if (inStack(ADD_NEW_STACK))
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.conatiner, addNewAlbum)
                    .commit();
    }

    @Override
    public void onAttachToHome(boolean attach) {
        if (attach) fab.hide();
        else fab.show();
    }

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(StaticData.BROADCAST_ACTION)) {
                int progress = intent.getIntExtra("progress", 0);
                System.out.println("Progress in receiver " + progress);
                progressDialog.setProgress(progress);
                if (intent.getBooleanExtra("completed", false)) {
                    progressDialog.dismiss();
                    showMessage("Album Downloaded");
                    try {
                        inStack(HOME_STACK);
                        getSupportFragmentManager().beginTransaction().replace(R.id.conatiner, allAlbumDetails, HOME_STACK).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
