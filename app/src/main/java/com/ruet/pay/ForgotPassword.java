package com.ruet.pay;

import static java.security.AccessController.getContext;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ForgotPassword extends AppCompatActivity {
    TextInputLayout femail_layout;
    TextInputEditText forgot_email;
    LottieAnimationView fsubmit;
    TextView femailView;
    String fEmail;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_password);

        //dark mood disable
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.actionbaricon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("  Password Recovery");
        }

        //Background logo
        //FrameLayout frameLayout = findViewById(R.id.frameLayout);
        AspectRatioImageView imageViewBackground = findViewById(R.id.imageViewBackground);
        imageViewBackground.setImageResource(R.drawable.bglogo);
        imageViewBackground.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();

        femail_layout = findViewById(R.id.femail_layout);
        forgot_email = findViewById(R.id.forgot_email);
        femailView = findViewById(R.id.femailView);
        fsubmit = findViewById(R.id.fsubmit);
        forgot_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                femail_layout.setError(null);
            }
        });
        fsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fEmail = forgot_email.getText().toString().trim() + femail_layout.getSuffixText();
                femailView.setText(fEmail);
                if (Patterns.EMAIL_ADDRESS.matcher(fEmail).matches()){
                    //Network check
                    if(((ConnectivityManager) ForgotPassword.this
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getActiveNetworkInfo() != null){
                        findViewById(R.id.pb).setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(fEmail, "NisarqkZ9u!Fg$3pLx@7rYs#2vTd&5mXgB8oNhA6cV1jW4eI0fRdU9yKwQzC4hJ3lMpO2iEnisaR").addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                mAuth.getInstance().getCurrentUser().delete();

                                femail_layout.setError("Not Registered! Please Register your Account first.");
                                findViewById(R.id.pb).setVisibility(View.GONE);
                            }
                            else{
                                if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                    sendRecovery(fEmail);
                                }
                                else if (task.getException() instanceof FirebaseNetworkException || task.getException() instanceof NetworkErrorException){
                                    Intent nointernet = new Intent(ForgotPassword.this, NoInternet.class);
                                    findViewById(R.id.pb).setVisibility(View.GONE);
                                    startActivity(nointernet);
                                }
                                else {
                                    findViewById(R.id.pb).setVisibility(View.GONE);
                                    Toast.makeText(ForgotPassword.this,"Account Searching Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        Intent nointernet = new Intent(ForgotPassword.this, NoInternet.class);
                        startActivity(nointernet);
                    }
                }
                else {
                    femail_layout.setError("Please Enter your Email without suffix(ruet.ac.bd) and put a '.' or '@' before the Suffix!");
                }
            }
        });
    }

    private void sendRecovery (String fEmail){
        mAuth.sendPasswordResetEmail(fEmail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Password reset email sent successfully
                        // You may add a Toast or handle success as required
                        findViewById(R.id.pb).setVisibility(View.GONE);
                        findViewById(R.id.flayout1).setVisibility(View.GONE);
                        findViewById(R.id.flayout2).setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        findViewById(R.id.pb).setVisibility(View.GONE);
                        // Handle errors
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            // Email address not registered
                            femail_layout.setError("Email not registered! Please check and try again.");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid email address
                            femail_layout.setError("Invalid email address! Please check and try again.");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // Too many requests sent to the server
                            // You may consider showing a message to the user indicating temporary unavailability
                            Toast.makeText(ForgotPassword.this, "Too many requests. Please try again later.", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseNetworkException || e instanceof NetworkErrorException) {
                            // Network-related errors
                            Intent nointernet = new Intent(ForgotPassword.this, NoInternet.class);
                            startActivity(nointernet);
                        } else {
                            // Other exceptions
                            Toast.makeText(ForgotPassword.this, "Recovery Email sending Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}