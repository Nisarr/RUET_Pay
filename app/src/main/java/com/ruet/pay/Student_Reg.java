package com.ruet.pay;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Student_Reg extends AppCompatActivity {
    TextInputLayout name_layout, reg_layout;
    TextInputEditText email, name, roll, reg, department, session;
    LottieAnimationView submit;
    String Email, Pass, Name, Roll, Reg, Dept, Session, userID;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    ScrollView scrollView;
    LottieAnimationView success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.student_reg);

        //dark mood disable
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.actionbaricon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("  Student Registration");
        }

        //Background logo
        //FrameLayout frameLayout = findViewById(R.id.frameLayout);
        AspectRatioImageView imageViewBackground = findViewById(R.id.imageViewBackground);
        imageViewBackground.setImageResource(R.drawable.bglogo);
        imageViewBackground.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        Email = getIntent().getStringExtra("Email");
        Pass = getIntent().getStringExtra("Pass");

        name_layout = findViewById(R.id.name_layout);
        reg_layout = findViewById(R.id.reg_layout);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        roll = findViewById(R.id.roll);
        reg = findViewById(R.id.reg);
        department = findViewById(R.id.department);
        session = findViewById(R.id.session);
        submit = findViewById(R.id.submit);

        Roll = Email.substring(0,7);
        Dept = FindDept(Integer.parseInt(Email.substring(2,4)));
        Session = "20"+Email.substring(0,2)+"-"+(Integer.parseInt(Email.substring(0,2))+1);

        email.setText(Email);
        roll.setText(Roll);
        department.setText(Dept);
        session.setText(Session);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((ConnectivityManager) Student_Reg.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo() != null){
                    Name = name.getText().toString();
                    Reg = reg.getText().toString().trim();
                    if (Name.isEmpty() || Reg.length() < 4){
                        name_layout.setError("Enter your Full name carefully!");
                        reg_layout.setError("Enter your 4 digit Registration No.");
                    }
                    else {
                        mAuth.createUserWithEmailAndPassword(Email, Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    userID = mAuth.getCurrentUser().getUid();
                                    storeData (Email, Name, Roll, Reg, Dept, Session, userID);
                                }
                                else{
                                    if (task.getException() instanceof FirebaseNetworkException || task.getException() instanceof NetworkErrorException){
                                        Intent nointernet = new Intent(Student_Reg.this, NoInternet.class);
                                        startActivity(nointernet);
                                    }
                                    else {
                                        Toast.makeText(Student_Reg.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                }
                else{
                    Intent nointernet = new Intent(Student_Reg.this, NoInternet.class);
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

    private String FindDept (int DeptCode){
        switch (DeptCode){
            case 0:
                return "Civil Engineering (CE)";
            case 1:
                return "Electrical & Electronics Engineering (EEE)";
            case 2:
                return "Mechanical Engineering (ME)";
            case 3:
                return "Computer Science & Engineering (CSE)";
            case 4:
                return "Electrical & Telecommunication Engineering (ETE)";
            case 5:
                return "Industrial & Production Engineering (IPE)";
            case 6:
                return "Glass & Ceramic Engineering (GCE)";
            case 7:
                return "Urban & Regional Planning (URP)";
            case 8:
                return "Mechatronics Engineering (MTE)";
            case 9:
                return "Architecture (ARCH)";
            case 10:
                return "Electrical & Computer Engineering (ECE)";
            case 11:
                return "Chemical Engineering (ChE)";
            case 12:
                return "Building Engineering & Construction Management (BECM)";
            case 13:
                return "Materials Science & Engineering (MSE)";
            default:
                return "Error fetching Department from Email or Roll!";
        }
    }

    private void storeData (String Email, String Name, String Roll, String Reg, String Dept, String Session, String userID){
        DocumentReference documentReference = fstore.collection("Student").document(userID);
        Map<String, Object> student = new HashMap<>();
        student.put("Email", Email);
        student.put("Name", Name);
        student.put("Roll", Roll);
        student.put("Reg", Reg);
        student.put("Dept", Dept);
        student.put("Session", Session);
        documentReference.set(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    scrollView = findViewById(R.id.scrollView);
                    success = findViewById(R.id.success);
                    scrollView.setVisibility(View.GONE);
                    success.setVisibility(View.VISIBLE);
                    new CountDownTimer(7000,1000){
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            Intent done = new Intent(Student_Reg.this, MainActivity.class);
                            startActivity(done);
                        }
                    }.start();
                }
                else{
                    if (task.getException() instanceof FirebaseNetworkException || task.getException() instanceof NetworkErrorException){
                        Intent nointernet = new Intent(Student_Reg.this, NoInternet.class);
                        startActivity(nointernet);
                    }
                    else {
                        Toast.makeText(Student_Reg.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}