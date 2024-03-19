package com.ruet.pay;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import androidx.appcompat.app.ActionBar;

public class MainActivity extends AppCompatActivity {
    Dialog welcome;
    LinearLayout popup, welcomelayout;
    Animation zoom_in, fade_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dark mood disable
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.actionbaricon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("  RUET Pay");
        }

        //Background logo
        //FrameLayout frameLayout = findViewById(R.id.frameLayout);
        AspectRatioImageView imageViewBackground = findViewById(R.id.imageViewBackground);
        imageViewBackground.setImageResource(R.drawable.bglogo);
        imageViewBackground.setVisibility(View.VISIBLE);

        //Welcome Pop up Dialog
        welcome = new Dialog(this);
        welcome.setContentView(R.layout.welcome_popup);
        zoom_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoom_in);
        fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
        welcomelayout = welcome.findViewById(R.id.welcomelayout);
        welcomelayout.startAnimation(zoom_in);
        welcome.show();
        popup = welcome.findViewById(R.id.popup);
        popup.setOnClickListener(view -> {
            welcomelayout.startAnimation(fade_out);
            new Handler().postDelayed(() -> welcome.dismiss(), 1500);
        });
        new Handler().postDelayed(() -> {
            welcomelayout.startAnimation(fade_out);
            new Handler().postDelayed(() -> welcome.dismiss(), 1500);
        }, 5000);


    }
}