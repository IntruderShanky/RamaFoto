package com.islabs.ramafoto.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.islabs.ramafoto.R;
import com.islabs.ramafoto.Utils.StaticData;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences(StaticData.PREF, MODE_PRIVATE);
                Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
                if (preferences.getString("uid", "").isEmpty())
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                SplashScreen.this.finish();
            }
        }, 3000);

    }
}
