package com.android.gis.huapp;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class Start extends Activity {
    private static int SPLASH_TIME_OUT = 5000;
    private ProgressBar pbr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.android.gis.huapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("name not found","package name not found");

        } catch (NoSuchAlgorithmException e) {
            Log.d("fail","algorithm not currect");

        }
        setContentView(R.layout.activity_start);
        startService(new Intent(this, RegistrationService.class));
        pbr= (ProgressBar) findViewById(R.id.pbar);

       com.android.gis.huapp.ProgressBarAnimation anim = new com.android.gis.huapp.ProgressBarAnimation(pbr, 100, 0);
        anim.setDuration(5000);
        pbr.startAnimation(anim);
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();
        String toke = FirebaseInstanceId.getInstance().getToken();
        Log.d("tokee", "" + toke);

        //pbr.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(Start.this, SearchMap.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}