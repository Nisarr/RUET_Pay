package com.ruet.pay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCCustomerInfoInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz;
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import android.Manifest;

public class Payment extends AppCompatActivity implements SSLCTransactionResponseListener {
    String[] selectYear = {"1st", "2nd", "3rd", "4th"};
    String[] selectSem = {"Odd", "Even"};
    String[] selectPayto = {"Vice Chancelor","Registrar", "Head of DSW", "Head Librarian", "Dean of Faculty (ECE)", "Head of Department (ECE)"};
    AutoCompleteTextView year_input, sem_input, payto_input;
    ArrayAdapter<String> yearAdapter, semAdapter, paytoAdapter;
    String Name, Email, Roll, Reg = "", Dept, Session = "", Year = "", Semester = "", Title, Payto = "";
    double Amount;
    String category, userID;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    FirebaseUser user;
    FrameLayout paynow;
    TextInputEditText pay_title, amount;
    TextInputLayout amount_layout;
    String StoreID, StorePass, TransID, payDate;
    TextView pdfTitle, pdfPayto, pdfTrans, pdfDate, pdfName, pdfEmail, pdfRoll, pdfDept, pdfYearnSem, pdfRegnSession, pdfAmount, pdfAmountinword;
    PdfDocument document;
    LottieAnimationView pdf_download;
    Dialog payslip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.payment);

        //dark mood disable
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //Action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.drawable.actionbaricon);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("  Payment");
        }

        //Background logo
        //FrameLayout frameLayout = findViewById(R.id.frameLayout);
        AspectRatioImageView imageViewBackground = findViewById(R.id.imageViewBackground);
        imageViewBackground.setImageResource(R.drawable.bglogo);
        imageViewBackground.setVisibility(View.VISIBLE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        user = mAuth.getCurrentUser();
        userID = user.getUid();
        Email = user.getEmail();
        category = Email.substring(Email.indexOf('@')+1, Email.indexOf('.'));
        if (category.equals("student")) {
            findViewById(R.id.frStdnt).setVisibility(View.VISIBLE);

            year_input = findViewById(R.id.year_input);
            yearAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, selectYear);
            year_input.setAdapter(yearAdapter);
            year_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Year = adapterView.getItemAtPosition(i).toString();
                }
            });

            sem_input = findViewById(R.id.sem_input);
            semAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, selectSem);
            sem_input.setAdapter(semAdapter);
            sem_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Semester = adapterView.getItemAtPosition(i).toString();
                }
            });
        }

        payto_input = findViewById(R.id.payto_input);
        paytoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, selectPayto);
        payto_input.setAdapter(paytoAdapter);
        payto_input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Payto = adapterView.getItemAtPosition(i).toString();
            }
        });

        pay_title = findViewById(R.id.pay_title);
        amount = findViewById(R.id.amount);
        amount_layout = findViewById(R.id.amount_layout);
        paynow = findViewById(R.id.paynow);
        paynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Network check
                if(((ConnectivityManager) Payment.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo() != null){}
                else{
                    Intent nointernet = new Intent(Payment.this, NoInternet.class);
                    startActivity(nointernet);
                }
                Title = pay_title.getText().toString();
                if (Title.isEmpty() || amount.getText().toString().trim().isEmpty() || Payto.isEmpty()) {
                    if (amount.getText().toString().trim().isEmpty())
                        amount_layout.setError("Enter a Valid Amount!");
                    else Toast.makeText(Payment.this, "Please fill all the field to make sure a Successful Payment!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Amount = Double.parseDouble(amount.getText().toString().trim());
                    if (Amount <= 0)
                        amount_layout.setError("Amount must be more than 0 tk!");
                    else {
                        findViewById(R.id.main_layout).setVisibility(View.GONE);
                        findViewById(R.id.payment_loading).setVisibility(View.VISIBLE);

                        if (category.equals("student")){
                            DocumentReference studentData = fstore.collection("Student").document(userID);
                            studentData.get().addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()){
                                    Name = documentSnapshot.getString("Name");
                                    Roll = documentSnapshot.getString("Roll");
                                    Dept = documentSnapshot.getString("Dept");
                                    Reg = documentSnapshot.getString("Reg");
                                    Session = documentSnapshot.getString("Session");

                                    PaytoSelect();
                                    SSLsetup();
                                }
                                else {
                                    finish();
                                    Toast.makeText(Payment.this, "User Data not available in the Database!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> {
                                if (e instanceof FirebaseFirestoreException) {
                                    FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                                    if (firestoreException.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE ||
                                            firestoreException.getCode() == FirebaseFirestoreException.Code.CANCELLED) {
                                        // Network-related errors
                                        Intent nointernet = new Intent(Payment.this, NoInternet.class);
                                        startActivity(nointernet);
                                    } else {
                                        // Other exceptions
                                        finish();
                                        Toast.makeText(Payment.this, "Error Fetching User Data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    // Other exceptions
                                    finish();
                                    Toast.makeText(Payment.this, "Error Fetching User Data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                amount_layout.setError(null);
            }
        });
    }


    private void SSLsetup (){
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        String username = Email.substring(0, Email.indexOf('@'));
        TransID = username + "_" + timestamp + "_" + random;
        final SSLCommerzInitialization sslCommerzInitialization = new SSLCommerzInitialization (StoreID,StorePass, Amount, SSLCCurrencyType.BDT,TransID, Title, SSLCSdkType.TESTBOX);
        final SSLCCustomerInfoInitializer customerInfoInitializer = new SSLCCustomerInfoInitializer(Name, Email,
                Roll, Reg, Session, Dept, Year + " Year - " + Semester + " Semsester");

        IntegrateSSLCommerz
                .getInstance(Payment.this)
                .addSSLCommerzInitialization(sslCommerzInitialization)
                .addCustomerInfoInitializer(customerInfoInitializer)
                .buildApiCall(this);

    }


    @Override
    public void transactionSuccess(SSLCTransactionInfoModel sslcTransactionInfoModel) {
        payDate = sslcTransactionInfoModel.getTranDate();
        findViewById(R.id.payment_loading).setVisibility(View.GONE);
        findViewById(R.id.payment_success).setVisibility(View.VISIBLE);
        Toast.makeText(this, "Payment Successful! Please Download your Payment Slip.", Toast.LENGTH_SHORT).show();

        generatePaySlip();
    }

    @Override
    public void transactionFail(String s) {
        finish();
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closed(String s) {
        finish();
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    private void generatePaySlip(){
        payslip = new Dialog(this);
        payslip.requestWindowFeature(Window.FEATURE_NO_TITLE);
        payslip.setContentView(R.layout.pdf_layout);
        payslip.setCancelable(false);

        pdfTitle = payslip.findViewById(R.id.pdfTitle);
        pdfPayto = payslip.findViewById(R.id.pdfPayto);
        pdfTrans = payslip.findViewById(R.id.pdfTrans);
        pdfDate = payslip.findViewById(R.id.pdfDate);
        pdfName = payslip.findViewById(R.id.pdfName);
        pdfEmail = payslip.findViewById(R.id.pdfEmail);
        pdfRoll = payslip.findViewById(R.id.pdfRoll);
        pdfDept = payslip.findViewById(R.id.pdfDept);
        pdfYearnSem = payslip.findViewById(R.id.pdfYearnSem);
        pdfRegnSession = payslip.findViewById(R.id.pdfRegnSession);
        pdfAmount = payslip.findViewById(R.id.pdfAmount);

        pdfTitle.setText(Title);
        pdfPayto.setText(Payto);
        pdfTrans.setText(TransID);
        pdfDate.setText(payDate);
        pdfName.setText(Name);
        pdfEmail.setText(Email);
        pdfRoll.setText(Roll);
        pdfDept.setText(Dept);
        pdfYearnSem.setText(Year + " Year - " + Semester + " Semester");
        pdfRegnSession.setText(Reg + "/" + Session);
        pdfAmount.setText(Amount + " tk");

        payslip.show();
        if (payslip.getWindow() != null) {
            payslip.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(payslip.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            payslip.getWindow().setAttributes(lp);
        }

        View view = payslip.findViewById(R.id.pdf_save);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                Drawable bgDrawable = view.getBackground();
                if (bgDrawable != null)
                    bgDrawable.draw(canvas);
                else canvas.drawColor(Color.WHITE);
                view.draw(canvas);

                document = new PdfDocument();
                PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                PdfDocument.Page myPage = document.startPage(mypageInfo);
                canvas = myPage.getCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                document.finishPage(myPage);

                uploadPDF(document);
            }
        });


        pdf_download = payslip.findViewById(R.id.pdf_download);
        pdf_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    Intent savepdf = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    savepdf.addCategory(Intent.CATEGORY_OPENABLE);
                    savepdf.setType("application/pdf");
                    savepdf.putExtra(Intent.EXTRA_TITLE, Title + ".pdf");
                    startActivityForResult(savepdf, 1);
                }
                else {
                    Toast.makeText(Payment.this, "Please Provide Storage permission to save pdf!", Toast.LENGTH_SHORT).show();
                    requestPermission();
                }
            }
        });
    }


    private void uploadPDF(PdfDocument document){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            document.writeTo(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            StorageReference pdfRef = FirebaseStorage.getInstance().getReference().child("payment_slips").child(Title + TransID + ".pdf");
            pdfRef.putBytes(pdfBytes)
                    .addOnSuccessListener(taskSnapshot -> {
                        pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            CollectionReference paymentSlipsCollectionRef = fstore.collection("Student").document(userID).collection("PaymentSlips");
                            DocumentReference newPaymentSlipRef = paymentSlipsCollectionRef.document(TransID);
                            Map<String, Object> paymentSlipData = new HashMap<>();
                            paymentSlipData.put("Title", Title);
                            paymentSlipData.put("Date_Time", payDate);
                            paymentSlipData.put("downloadUrl", downloadUrl);
                            newPaymentSlipRef.set(paymentSlipData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(Payment.this, "Payment Slip Successfully updated to Payment History!", Toast.LENGTH_SHORT).show();
                                            payslip.setCancelable(true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            findViewById(R.id.pdfupError).setVisibility(View.VISIBLE);
                                            Toast.makeText(Payment.this, "Error updating Payment History: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        findViewById(R.id.pdfupError).setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Error updating Payment History: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });

        } catch (IOException e) {
            findViewById(R.id.pdfupError).setVisibility(View.VISIBLE);
            Toast.makeText(this, "Error updating Payment History: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return  permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null){
                uri = data.getData();
                if (document != null){
                    ParcelFileDescriptor pfd = null;
                    try {
                        pfd = getContentResolver().openFileDescriptor(uri, "w");
                        FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                        document.writeTo(fileOutputStream);
                        document.close();
                        Toast.makeText(this, "Payment Slip Saved Successfully!", Toast.LENGTH_SHORT).show();
                        payslip.setCancelable(true);
                    } catch (IOException e) {
                        try {
                            DocumentsContract.deleteDocument(getContentResolver(), uri);
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                            Toast.makeText(this, "Failed to save Payment slip! Please take a screenshot and print it.", Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save Payment slip! Please take a screenshot and print it.", Toast.LENGTH_SHORT).show();
                    }
                } else Toast.makeText(this, "Failed to save Payment slip! Please take a screenshot and print it.", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, "Failed to save Payment slip! Please take a screenshot and print it.", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this, "Failed to save Payment slip! Please take a screenshot and print it.", Toast.LENGTH_SHORT).show();
    }

    private void PaytoSelect (){
        switch(Payto){
            case "Vice Chancelor" :
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
            case "Registrar" :
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
            case "Head of DSW" :
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
            case "Head Librarian" :
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
            case "Dean of Faculty (ECE)" :
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
            case "Head of Department (ECE)" :
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
            default:
                StoreID = "ruetp660f6014e384a" ;
                StorePass = "ruetp660f6014e384a@ssl" ;
                break;
        }
    }
}