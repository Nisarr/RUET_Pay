package com.ruet.pay;

import android.accounts.NetworkErrorException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {
    TextView pname, pemail, proll, pdept, preg, psession;
    String Name, Email, Roll, Dept, Reg, Session, category, userID;
    String Crntpass, newPass;
    boolean isStudent = false, isFaculty = false, isOthers = false;
    LinearLayout editbtn, chngepassbtn;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);

        //dark mood disable
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Back button in Action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.actionbaricon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("  Profile");
        }

        //Background logo
        //FrameLayout frameLayout = findViewById(R.id.frameLayout);
        AspectRatioImageView imageViewBackground = findViewById(R.id.imageViewBackground);
        imageViewBackground.setImageResource(R.drawable.bglogo);
        imageViewBackground.setVisibility(View.VISIBLE);

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Fetching User Data...");
        pd.show();

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        user = mAuth.getCurrentUser();
        userID = user.getUid();
        Email = user.getEmail();
        category = Email.substring(Email.indexOf('@')+1, Email.indexOf('.'));
        if (category.equals("student"))
            isStudent = true;
        else if (category.equals("ruet"))
            isOthers = true;
        else isFaculty = true;

        if (isStudent){
            findViewById(R.id.stdntcard).setVisibility(View.VISIBLE);
            pname = findViewById(R.id.pname);
            pemail = findViewById(R.id.pemail);
            proll = findViewById(R.id.proll);
            pdept = findViewById(R.id.pdept);
            preg = findViewById(R.id.preg);
            psession = findViewById(R.id.psession);

            DocumentReference studentData = fstore.collection("Student").document(userID);
            studentData.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        Name = documentSnapshot.getString("Name");
                        Roll = documentSnapshot.getString("Roll");
                        Dept = documentSnapshot.getString("Dept");
                        Reg = documentSnapshot.getString("Reg");
                        Session = documentSnapshot.getString("Session");

                        pname.setText(Name);
                        pemail.setText(Email);
                        proll.setText(Roll);
                        preg.setText(Reg);
                        pdept.setText(Dept);
                        psession.setText(Session);

                        pd.dismiss();
                    }
                    else {
                        pname.setText("User Data not available in the Database!");
                        pemail.setText("Please Contact with your Department or Admin team");
                        pname.setTextColor(Profile.this.getResources().getColor(android.R.color.holo_red_dark));
                        pemail.setTextColor(Profile.this.getResources().getColor(android.R.color.holo_red_light));

                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();

                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                        if (firestoreException.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                firestoreException.getCode() == FirebaseFirestoreException.Code.CANCELLED) {
                            // Network-related errors
                            Intent nointernet = new Intent(Profile.this, NoInternet.class);
                            startActivity(nointernet);
                        } else {
                            // Other exceptions
                            pname.setText("Error Fetching User Data!");
                            pname.setTextColor(Profile.this.getResources().getColor(android.R.color.holo_red_dark));
                            Toast.makeText(Profile.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Other exceptions
                        pname.setText("Error Fetching User Data!");
                        pname.setTextColor(Profile.this.getResources().getColor(android.R.color.holo_red_dark));
                        Toast.makeText(Profile.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        chngepassbtn = findViewById(R.id.chngepassbtn);
        chngepassbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordChange(Email, user);
            }
        });

        editbtn = findViewById(R.id.editbtn);
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStudent){
                    stdntEditProfile(Name, Email, Roll, Dept, Reg, Session, userID);
                }
            }
        });

    }


    LottieAnimationView chngesubmit;
    TextInputLayout pass_layout, newpass_layout1, newpass_layout2;
    TextInputEditText pass, new_pass1, new_pass2;
    private void PasswordChange (String Email, FirebaseUser user){
        findViewById(R.id.show_layout).setVisibility(View.GONE);
        findViewById(R.id.passChnge_layout).setVisibility(View.VISIBLE);

        pass_layout = findViewById(R.id.pass_layout);
        newpass_layout1 = findViewById(R.id.newpass_layout1);
        newpass_layout2 = findViewById(R.id.newpass_layout2);

        pass = findViewById(R.id.pass);
        new_pass1 = findViewById(R.id.new_pass1);
        new_pass2 = findViewById(R.id.new_pass2);


        chngesubmit = findViewById(R.id.chngesubmit);
        chngesubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((ConnectivityManager) Profile.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo() != null){

                    Crntpass = pass.getText().toString();
                    newPass = new_pass1.getText().toString();

                    if (newPass.equals(new_pass2.getText().toString()) ){
                        if (newPass.length() >= 6) {
                            findViewById(R.id.pbcng).setVisibility(View.VISIBLE);

                            AuthCredential credential = EmailAuthProvider.getCredential(Email, Crntpass);
                            user.reauthenticate(credential)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            user.updatePassword(newPass)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Password successfully updated
                                                            finish();
                                                            Toast.makeText(Profile.this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
                                                            Intent refresh = new Intent(Profile.this, Profile.class);
                                                            findViewById(R.id.pbcng).setVisibility(View.GONE);
                                                            startActivity(refresh);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            findViewById(R.id.pbcng).setVisibility(View.GONE);
                                                            if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                                                                // User needs to re-authenticate due to recent login requirement
                                                                finish();
                                                                Toast.makeText(Profile.this, "Session Expired! Please Sign in again with your old password.", Toast.LENGTH_SHORT).show();
                                                                Intent MainActivity = new Intent(Profile.this, MainActivity.class);
                                                                MainActivity.putExtra("popupShown", true);
                                                                startActivity(MainActivity);
                                                            } else if (e instanceof FirebaseAuthInvalidUserException) {
                                                                // User is invalid or does not exist
                                                                pass_layout.setError("User invalid! Please Sign out and Sign in again.");
                                                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                                                // Too many requests made to Firebase Authentication servers
                                                                Toast.makeText(Profile.this, "Too many requests! Please try again later.", Toast.LENGTH_SHORT).show();
                                                            } else if (e instanceof FirebaseNetworkException || e instanceof NetworkErrorException) {
                                                                // Network-related errors
                                                                Intent nointernet = new Intent(Profile.this, NoInternet.class);
                                                                startActivity(nointernet);
                                                            } else {
                                                                // Other exceptions
                                                                Toast.makeText(Profile.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            findViewById(R.id.pbcng).setVisibility(View.GONE);
                                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                                // Invalid credentials provided
                                                pass_layout.setError("Wrong Password!");
                                            } else if (e instanceof FirebaseAuthInvalidUserException) {
                                                // User is invalid or does not exist
                                                pass_layout.setError("User invalid! Please Sign out and Sign in again.");
                                            } else if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                                                // User needs to re-authenticate due to recent login requirement
                                                finish();
                                                Toast.makeText(Profile.this, "Session Expired! Please Sign in again with your old password.", Toast.LENGTH_SHORT).show();
                                                Intent MainActivity = new Intent(Profile.this, MainActivity.class);
                                                MainActivity.putExtra("popupShown", true);
                                                startActivity(MainActivity);
                                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                                // Too many requests made to Firebase Authentication servers
                                                Toast.makeText(Profile.this, "Too many requests! Please try again later.", Toast.LENGTH_SHORT).show();
                                            } else if (e instanceof FirebaseNetworkException || e instanceof NetworkErrorException) {
                                                // Network-related errors
                                                Intent nointernet = new Intent(Profile.this, NoInternet.class);
                                                startActivity(nointernet);
                                            } else {
                                                // Other exceptions
                                                Toast.makeText(Profile.this, "Authentication Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                        }
                        else {
                            newpass_layout1.setError("Password need to be at least 6 character! ");
                            newpass_layout2.setError("Password need to be at least 6 character! ");
                        }
                    }
                    else {
                        newpass_layout1.setError("Passwords not Matched! ");
                        newpass_layout2.setError("Passwords not Matched! ");
                    }

                }
                else{
                    Intent nointernet = new Intent(Profile.this, NoInternet.class);
                    startActivity(nointernet);
                }
            }
        });

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                TextInputEditText editText = (TextInputEditText) getCurrentFocus();
                if (editText == null) {
                    return;
                }

                switch (editText.getId()) {
                    case R.id.pass:
                        pass_layout.setError(null);
                        break;
                    case R.id.new_pass1:
                    case R.id.new_pass2:
                        newpass_layout1.setError(null);
                        newpass_layout2.setError(null);
                        break;
                    default:
                        break;
                }
            }
        };

        pass.addTextChangedListener(textWatcher);
        new_pass1.addTextChangedListener(textWatcher);
        new_pass2.addTextChangedListener(textWatcher);
    }


    TextInputLayout name_layout, reg_layout;
    TextInputEditText email, name, roll, reg, department, session;
    LottieAnimationView submit;
    String newName, newReg;
    private void stdntEditProfile(String Name, String Email, String Roll, String Dept, String Reg, String Session, String UserID){
        findViewById(R.id.show_layout).setVisibility(View.GONE);
        findViewById(R.id.stdntedit_layout).setVisibility(View.VISIBLE);

        name_layout = findViewById(R.id.name_layout);
        reg_layout = findViewById(R.id.reg_layout);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        roll = findViewById(R.id.roll);
        reg = findViewById(R.id.reg);
        department = findViewById(R.id.department);
        session = findViewById(R.id.session);
        submit = findViewById(R.id.submit);

        //

        email.setText(Email);
        name.setText(Name);
        roll.setText(Roll);
        reg.setText(Reg);
        department.setText(Dept);
        session.setText(Session);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((ConnectivityManager) Profile.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo() != null){
                    newName = name.getText().toString();
                    newReg = reg.getText().toString().trim();
                    if (newName.isEmpty() || newReg.length() < 4){
                        name_layout.setError("Enter your Full name carefully!");
                        reg_layout.setError("Enter your 4 digit Registration No.");
                    }
                    else {
                        findViewById(R.id.pbstdntedit).setVisibility(View.VISIBLE);

                        DocumentReference documentReference = fstore.collection("Student").document(userID);
                        Map<String, Object> update = new HashMap<>();
                        update.put("Name", newName);
                        update.put("Reg", newReg);
                        documentReference.update(update)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                        Toast.makeText(Profile.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        Intent refresh = new Intent(Profile.this, Profile.class);
                                        findViewById(R.id.pbstdntedit).setVisibility(View.GONE);
                                        startActivity(refresh);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        findViewById(R.id.pbstdntedit).setVisibility(View.GONE);
                                        if (e instanceof FirebaseFirestoreException) {
                                            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                                            if (firestoreException.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                                    firestoreException.getCode() == FirebaseFirestoreException.Code.CANCELLED) {
                                                // Network-related errors
                                                Intent nointernet = new Intent(Profile.this, NoInternet.class);
                                                startActivity(nointernet);
                                            }
                                        }
                                        Toast.makeText(Profile.this, "Error updating Profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
                else{
                    Intent nointernet = new Intent(Profile.this, NoInternet.class);
                    startActivity(nointernet);
                }
            }
        });

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                name_layout.setError(null);
                reg_layout.setError(null);
            }
        };
        name.addTextChangedListener(textWatcher);
        reg.addTextChangedListener(textWatcher);

    }

    //For Back button in Action bar

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            this.finish();

        return super.onOptionsItemSelected(item);
    }
}