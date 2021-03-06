package com.islabs.ramafoto.Activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.intrusoft.milano.Milano;
import com.intrusoft.milano.OnRequestComplete;
import com.islabs.ramafoto.Fragments.AboutUs;
import com.islabs.ramafoto.Fragments.AddNewAlbum;
import com.islabs.ramafoto.Fragments.AllAlbumDetails;
import com.islabs.ramafoto.Fragments.ContactUs;
import com.islabs.ramafoto.Fragments.FAQs;
import com.islabs.ramafoto.Fragments.RearrangeAlbums;
import com.islabs.ramafoto.Helper.DatabaseHelper;
import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Services.DownloadService;
import com.islabs.ramafoto.Utils.StaticData;

import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AddNewAlbum.NewAlbumCallback,
        RearrangeAlbums.AllAlbumsCallback,
        AllAlbumDetails.AllAlbumsDetailsCallback,
        ContactUs.ContactUsCallback,
        AboutUs.AboutUsCallback,
        FAQs.FAQsCallback {

    private CoordinatorLayout rootLayout;
    private Milano.Builder milanoBuilder;
    private DatabaseHelper helper;
    private ProgressDialog progressDialog;
    private AddNewAlbum addNewAlbum;
    private RearrangeAlbums rearrangeAlbums;
    private ContactUs contactUs;
    private FAQs faQs;
    private AboutUs aboutUs;
    private FloatingActionButton fab;
    private static final String HOME_STACK = "home_stack";
    private static final String REARRANGE_STACK = "rearrange_stack";
    private static final String ADD_NEW_STACK = "add_new_stack";
    private static final String CONTACT_US_STACK = "contact_us";
    private SharedPreferences preferences;
    private DownloadService downloadService;
    private boolean gotoHome = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        rootLayout = findViewById(R.id.root_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        helper = new DatabaseHelper(this);
        preferences = getSharedPreferences(StaticData.PREF, MODE_PRIVATE);
        helper.open();
        progressDialog = new ProgressDialog(this, R.style.ProgressDialogTheme);
        rearrangeAlbums = new RearrangeAlbums();
        AllAlbumDetails allAlbumDetails = new AllAlbumDetails();
        addNewAlbum = new AddNewAlbum();
        contactUs = new ContactUs();
        faQs = new FAQs();
        aboutUs = new AboutUs();
        fab = findViewById(R.id.add_new_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewAlbum();
            }
        });

        milanoBuilder = new Milano.Builder(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView userName = header.findViewById(R.id.user_name);
        TextView mobile = header.findViewById(R.id.mobile);
        userName.setText(preferences.getString("name", "Guest"));
        mobile.setText(preferences.getString("mobile", ""));
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.container, allAlbumDetails, HOME_STACK).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (gotoHome) {
            fab.show();
            if (helper.getAllAlbums().moveToFirst()) {
                try {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.container, new AllAlbumDetails(), HOME_STACK)
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        gotoHome = false;
        fab.hide();
        int id = item.getItemId();
        switch (id) {
            case R.id.rearrange_albums:
                if (inStack(REARRANGE_STACK))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, rearrangeAlbums)
                            .addToBackStack(REARRANGE_STACK)
                            .commit();
                break;
            case R.id.contact_us:
                if (inStack(CONTACT_US_STACK))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, contactUs)
                            .addToBackStack(CONTACT_US_STACK)
                            .commit();
                break;
            case R.id.home:
                if (helper.getAllAlbums().moveToFirst()) {
                    try {
                        fab.show();
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.container, new AllAlbumDetails(), HOME_STACK)
                                .commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.add_new:
                fab.performClick();
                break;
            case R.id.f_a_q:
                if (inStack("FAQ"))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, faQs)
                            .addToBackStack("FAQs")
                            .commit();
                break;
            case R.id.about_us:
                if (inStack("ABOUT US"))
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, aboutUs)
                            .addToBackStack("ABOUT_US")
                            .commit();
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean inStack(String stackEntry) {
        boolean inStack = true;
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            if (getSupportFragmentManager().getBackStackEntryAt(i).getName().equals(stackEntry)) {
                inStack = false;
                break;
            }
            getSupportFragmentManager().popBackStack();
        }
        return inStack;
    }

    @Override
    public void getAlbum(final String pin) {
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
                    downloadService = new DownloadService();
                    JSONObject object = new JSONObject(response);
                    if (object.getString("status").toLowerCase().equals("success")) {
                        if (object.getJSONArray("album_photos").length() < 1) {
                            showMessage("No photos found in this album.");
                            return;
                        }
                        progressDialog.setTitle("Downloading Album");
                        progressDialog.setMax(100);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgress(0);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadService.stopDownload();
                            }
                        });
                        progressDialog.show();
                        Intent downloadIntent = new Intent(HomeActivity.this, DownloadService.class);
                        downloadIntent.putExtra("json", response);
                        downloadIntent.putExtra("pin", pin);
                        downloadService.startDownload(downloadIntent, HomeActivity.this);

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
                showMessage("Network error");
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
                        try {
                            JSONObject object = new JSONObject(response);
                            showActionMessage(object.getString("message"));
                        } catch (Exception e) {
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
        if (getSupportActionBar() == null)
            return;
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
        fab.hide();
        gotoHome = false;
        if (inStack(ADD_NEW_STACK))
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, addNewAlbum)
                    .commit();
    }

    @Override
    public void onAttachToHome(boolean attach) {
        if (fab == null)
            return;
        if (attach) fab.hide();
        else fab.show();
    }

    class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(StaticData.BROADCAST_ACTION)) {
                int progress = intent.getIntExtra("progress", 0);
                boolean completed = intent.getBooleanExtra("completed", false);
                progressDialog.setProgress(progress);
                if (completed) {
                    System.out.println("Progress in receiver " + progress);
                    if (progress < 100) {
                        showMessage("Error in downloading some images..");
                    } else if (progress == 100) {
                        gotoHome = true;
                        showMessage("Album Downloaded");
                    }
                    progressDialog.dismiss();
                    try {
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.container, new AllAlbumDetails(), HOME_STACK)
                                .commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
