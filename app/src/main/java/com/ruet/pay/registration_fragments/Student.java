package com.ruet.pay.registration_fragments;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.ruet.pay.NoInternet;
import com.ruet.pay.OTPDialog;
import com.ruet.pay.R;

public class Student extends Fragment {
    TextInputLayout email_layout, pass_layout1, pass_layout2;
    TextInputEditText register_email, register_pass1, register_pass2;
    LottieAnimationView Continue;
    String reg_email, reg_pass;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.student, container, false);

        mAuth = FirebaseAuth.getInstance();

        email_layout = rootView.findViewById(R.id.email_layout);
        pass_layout1 = rootView.findViewById(R.id.pass_layout1);
        pass_layout2 = rootView.findViewById(R.id.pass_layout2);

        register_email = rootView.findViewById(R.id.register_email);
        register_pass1 = rootView.findViewById(R.id.register_pass1);
        register_pass2 = rootView.findViewById(R.id.register_pass2);

        Continue = rootView.findViewById(R.id.Continue);

        register_email.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // User has finished editing the text field
                String tempemail = register_email.getText().toString().trim();
                if (tempemail.length() != 7) {
                    email_layout.setError("Enter your 7 digit Roll number ");
                } else {
                    email_layout.setError(null); // Clear any previous error message
                }
            }
        });

        register_pass1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                pass_layout1.setError(null);
                pass_layout2.setError(null);
            }
        });
        register_pass2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                pass_layout1.setError(null);
                pass_layout2.setError(null);
            }
        });

        Continue.setOnClickListener(view -> {

            //Network check
            if(((ConnectivityManager) getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo() != null){}
            else{
                Intent nointernet = new Intent(getContext(), NoInternet.class);
                startActivity(nointernet);
            }
            String tempemail = register_email.getText().toString().trim();
            if (tempemail.length() != 7) {
                email_layout.setError("Enter your 7 digit Roll number ");
            } else {
                email_layout.setError(null); // Clear any previous error message
            }

            reg_email = register_email.getText().toString().trim() + email_layout.getSuffixText();
            reg_pass = register_pass1.getText().toString().trim();
            if (reg_pass.equals(register_pass2.getText().toString().trim()) ){
                if (reg_pass.length() >= 6) {

                    mAuth.createUserWithEmailAndPassword(reg_email,reg_pass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            mAuth.getInstance().getCurrentUser().delete();

                            OTPDialog otpverify = new OTPDialog(getContext(), reg_email, reg_pass,getContext());
                            otpverify.setCancelable(false);
                            otpverify.show();
                        }
                        else{
                            if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                email_layout.setError("Account Exists! Please go back and Sign in with your Password.");
                            }
                            else if (task.getException() instanceof FirebaseNetworkException || task.getException() instanceof NetworkErrorException){
                                Intent nointernet = new Intent(getContext(), NoInternet.class);
                                startActivity(nointernet);
                            }
                            else {
                                Toast.makeText(getContext(),"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
                else {
                    pass_layout1.setError("Password need to be at least 6 character! ");
                    pass_layout2.setError("Password need to be at least 6 character! ");
                }
            }
            else {
                pass_layout1.setError("Passwords not Matched! ");
                pass_layout2.setError("Passwords not Matched! ");
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }
}