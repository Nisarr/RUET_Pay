package com.ruet.pay;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.NetworkErrorException;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Dialog welcome;
    LinearLayout popup, welcomelayout, register, forgot_pass;
    Animation zoom_in, fade_out;
    private boolean popupShown = false;
    TextInputLayout lemail_layout, lpass_layout;
    TextInputEditText login_email, login_pass;
    LottieAnimationView login;
    String Email, Pass;
    FirebaseAuth mAuth;
    FirebaseUser user;

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

        popupShown = getIntent().getBooleanExtra("popupShown", false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user != null){
            popupShown = true;
            finish();
            Intent mainmenu = new Intent(MainActivity.this, MainMenu.class);
            startActivity(mainmenu);
        }

        //Welcome Pop up Dialog
        if(!popupShown) {
            welcome = new Dialog(this);
            welcome.setContentView(R.layout.welcome_popup);
            zoom_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoom_in);
            fade_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
            welcomelayout = welcome.findViewById(R.id.welcomelayout);
            welcomelayout.startAnimation(zoom_in);
            welcome.show();
            popupShown = true;
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


        lemail_layout = findViewById(R.id.lemail_layout);
        lpass_layout = findViewById(R.id.lpass_layout);
        login_email = findViewById(R.id.login_email);
        login_pass = findViewById(R.id.login_pass);
        login = findViewById(R.id.login);
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                lemail_layout.setError(null);
                lpass_layout.setError(null);
            }
        };
        login_email.addTextChangedListener(textWatcher);
        login_pass.addTextChangedListener(textWatcher);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Network check
                if(((ConnectivityManager) MainActivity.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo() != null){

                    Email = login_email.getText().toString().trim() + lemail_layout.getSuffixText();
                    Pass = login_pass.getText().toString();
                    if (Patterns.EMAIL_ADDRESS.matcher(Email).matches() && Pass.length() >= 6) {
                        findViewById(R.id.pb).setVisibility(View.VISIBLE);

                        mAuth.signInWithEmailAndPassword(Email, Pass)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        findViewById(R.id.pb).setVisibility(View.GONE);
                                        finish();
                                        Intent mainmenu = new Intent(MainActivity.this, MainMenu.class);
                                        mainmenu.putExtra("popupShown", true);
                                        startActivity(mainmenu);
                                    } else {
                                        findViewById(R.id.pb).setVisibility(View.GONE);
                                        Exception exception = task.getException();
                                        if (exception instanceof FirebaseAuthInvalidUserException) {
                                            lemail_layout.setError("Not Registered! Please Register your Account first.");
                                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                            checkbySignup(Email, Pass);
                                        } else if (exception instanceof FirebaseNetworkException || exception instanceof NetworkErrorException) {
                                            Intent nointernet = new Intent(MainActivity.this, NoInternet.class);
                                            startActivity(nointernet);
                                        } else {
                                            Toast.makeText(MainActivity.this, "Sign in Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                    else{
                        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
                            lemail_layout.setError("Please Enter your Email without suffix(ruet.ac.bd) and put a '.' or '@' before the Suffix!");
                        if (Pass.length() < 6)
                            lpass_layout.setError("Enter a Valid Password!");
                    }
                }
                else{
                    Intent nointernet = new Intent(MainActivity.this, NoInternet.class);
                    startActivity(nointernet);
                }
            }
        });


        forgot_pass = findViewById(R.id.forgot_pass);
        forgot_pass.setOnClickListener(view -> {
            Intent forgotpass = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(forgotpass);
        });

        register = findViewById(R.id.register);
        register.setOnClickListener(view -> {
            Intent registration = new Intent(MainActivity.this, Registration.class);
            startActivity(registration);
        });
    }

    private void checkbySignup(String Email, String Pass){

        mAuth.createUserWithEmailAndPassword(Email, Pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                mAuth.getInstance().getCurrentUser().delete();

                lemail_layout.setError("Not Registered! Please Register your Account first.");
            }
            else{
                if (task.getException() instanceof FirebaseAuthUserCollisionException){
                    lpass_layout.setError("Incorrect password! Please try putting your correct Password.");
                }
                else if (task.getException() instanceof FirebaseNetworkException || task.getException() instanceof NetworkErrorException){
                    Intent nointernet = new Intent(MainActivity.this, NoInternet.class);
                    startActivity(nointernet);
                }
                else {
                    Toast.makeText(MainActivity.this,"Account Searching Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // When user click bakpress button this method is called
    long mBackPressed;
    Toast toastBack;
    @Override
    public void onBackPressed() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            toastBack.cancel();
            super.onBackPressed();
        } else {
            toastBack = Toast.makeText(getBaseContext(), "Press the Back key again to Exit",
                    Toast.LENGTH_SHORT);
            toastBack.show();
        }
        mBackPressed = System.currentTimeMillis();
    } // end of onBackpressed method

}