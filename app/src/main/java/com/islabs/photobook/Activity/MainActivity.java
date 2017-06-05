package com.islabs.photobook.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.intrusoft.milano.Milano;
import com.intrusoft.milano.OnRequestComplete;
import com.islabs.photobook.R;
import com.islabs.photobook.Utils.StaticData;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @BindView(R.id.app_name)
    TextView appName;

    @BindView(R.id.email)
    TextInputEditText email;

    @BindView(R.id.name)
    TextInputEditText name;

    @BindView(R.id.mobile)
    TextInputEditText mobile;

    @OnClick(R.id.login)
    public void login(View vIew) {
        boolean validate = true;
        if (name.getText().toString().isEmpty() ||
                mobile.getText().toString().length() != 10)
            validate = false;
        if (validate) {
            String android_id = Settings.System.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            System.out.println(android_id);
            Milano.Builder builder = new Milano.Builder(this);
            builder.shouldDisplayDialog(true);
            builder.setDialogMessage("Registering..");
            builder.setNetworkErrorMessage("Internet not connected");
            builder.addRequestParams("Customer[customer_name]", name.getText().toString());
            builder.addRequestParams("Customer[mobile]", mobile.getText().toString());
            builder.addRequestParams("Customer[email]", email.getText().toString());
            builder.addRequestParams("Customer[device_type]", "android");
            builder.addRequestParams("Customer[device_id]", android_id);
            builder.addRequestParams("Customer[location_lat]", currentLatitude + "");
            builder.addRequestParams("Customer[location_lang]", currentLongitude + "");
            Milano milano = builder.build();
            milano.fromURL(StaticData.SIGN_UP)
                    .doPost("")
                    .execute(new OnRequestComplete() {
                        @Override
                        public void onSuccess(String response, int responseCode) {
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.getString("status").contains("success")) {
                                    SharedPreferences preferences = getSharedPreferences(StaticData.PREF, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("uid", object.getString("user_id"));
                                    editor.putString("name", name.getText().toString());
                                    editor.putString("mobile", mobile.getText().toString());
                                    editor.apply();
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    MainActivity.this.finish();
                                }
                                Toast.makeText(MainActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String error, int errorCode) {
                            //Do whatever you want to do
                        }
                    });
        } else {
            Toast.makeText(this, "Invalid Information", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mobile.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(10)
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 9);
            }
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
    }
}
