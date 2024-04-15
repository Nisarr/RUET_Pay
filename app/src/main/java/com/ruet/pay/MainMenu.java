package com.ruet.pay;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MainMenu extends AppCompatActivity {
    Dialog welcome;
    LinearLayout popup, welcomelayout;
    CardView paymentbtn, profilebtn, historybtn, signOutbtn;
    Animation zoom_in, fade_out;
    boolean popupShown = false;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String profileName, Email, userID;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_menu);

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
        userID = user.getUid();
        Email = user.getEmail();
        profileName = Email.substring(0, Email.indexOf('@'));
        if(user == null){
            popupShown = true;
            finish();
            Intent mainactivity = new Intent(MainMenu.this, MainActivity.class);
            startActivity(mainactivity);
        }

        //Welcome Pop up Dialog
        if(!popupShown) {
            welcome = new Dialog(this);
            welcome.setContentView(R.layout.welcome_popup);
            zoom_in = AnimationUtils.loadAnimation(MainMenu.this, R.anim.zoom_in);
            fade_out = AnimationUtils.loadAnimation(MainMenu.this, R.anim.fade_out);
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



        paymentbtn = findViewById(R.id.paymentbtn);
        paymentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent payment = new Intent(MainMenu.this, Payment.class);
                startActivity(payment);
            }
        });

        profilebtn = findViewById(R.id.profilebtn);
        profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile = new Intent(MainMenu.this, Profile.class);
                startActivity(profile);
            }
        });

        historybtn = findViewById(R.id.historybtn);
        drawerLayout = findViewById(R.id.drawer_layout);
        historybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Network check
                if(((ConnectivityManager) MainMenu.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo() != null){
                    findViewById(R.id.pb).setVisibility(View.VISIBLE);
                    PaymentHistory();
                }
                else{
                    Intent nointernet = new Intent(MainMenu.this, NoInternet.class);
                    startActivity(nointernet);
                }
            }
        });

        signOutbtn = findViewById(R.id.signOutbtn);
        signOutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder signoutD = new AlertDialog.Builder(MainMenu.this);
                signoutD.setTitle("Sign Out");
                signoutD.setMessage("Are you sure you want to Sign Out?");
                signoutD.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        finish();
                        Intent mainactivity = new Intent(MainMenu.this, MainActivity.class);
                        mainactivity.putExtra("popupShown", true);
                        startActivity(mainactivity);
                    }
                });
                signoutD.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                signoutD.show();
            }
        });
    }


    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<PaymentSlip> paymentSlipsList;
    private FirebaseFirestore fstore;
    private void PaymentHistory(){
        drawerLayout.openDrawer(GravityCompat.END);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        paymentSlipsList = new ArrayList<>();
        adapter = new MyAdapter(paymentSlipsList);
        recyclerView.setAdapter(adapter);

        fstore = FirebaseFirestore.getInstance();
        CollectionReference paymentSlipsCollectionRef = fstore.collection("Student").document(userID).collection("PaymentSlips");
        paymentSlipsCollectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String transId = document.getId();
                    String title = document.getString("Title");
                    String dateTime = document.getString("Date_Time");
                    String downloadUrl = document.getString("downloadUrl");

                    PaymentSlip paymentSlip = new PaymentSlip(transId, title, dateTime, downloadUrl);
                    paymentSlipsList.add(paymentSlip);
                }
                adapter.notifyDataSetChanged();
            } else {
                // Handle failure
                Exception e = task.getException();
                if (e instanceof FirebaseFirestoreException) {
                    FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                    if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Toast.makeText(MainMenu.this, "Permission Error! Please Comtact with admin team." + e.getMessage(), Toast.LENGTH_LONG).show();
                    } else if (firestoreException.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                        Toast.makeText(MainMenu.this, "Error: User's Payment History not available! " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainMenu.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (e instanceof FirebaseNetworkException || e instanceof NetworkErrorException) {
                    Intent nointernet = new Intent(MainMenu.this, NoInternet.class);
                    startActivity(nointernet);
                } else {
                    Toast.makeText(MainMenu.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    //Menu Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        Email = user.getEmail();
        profileName = Email.substring(0, Email.indexOf('@'));
        MenuItem profile = menu.findItem(R.id.profile);
        profile.setTitle(profileName);
        profile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                Intent profile = new Intent(MainMenu.this, Profile.class);
                startActivity(profile);
                return true;
            }
        });

        MenuItem signout = menu.findItem(R.id.signout);
        signout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                AlertDialog.Builder signoutD = new AlertDialog.Builder(MainMenu.this);
                signoutD.setTitle("Sign Out");
                signoutD.setMessage("Are you sure you want to Sign Out?");
                signoutD.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        finish();
                        Intent mainactivity = new Intent(MainMenu.this, MainActivity.class);
                        mainactivity.putExtra("popupShown", true);
                        startActivity(mainactivity);
                    }
                });
                signoutD.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                signoutD.show();
                return false;
            }
        });
        return true;
    }


    // When user click bakpress button this method is called
    long mBackPressed;
    Toast toastBack;
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) { // Check if the drawer is open from the end (right) side
            drawerLayout.closeDrawer(GravityCompat.END); // Close the drawer
        } else {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                toastBack.cancel();
                super.onBackPressed();
            } else {
                toastBack = Toast.makeText(getBaseContext(), "Press the Back key again to Exit",
                        Toast.LENGTH_SHORT);
                toastBack.show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    } // end of onBackpressed method











    public class PaymentSlip {
        private String transId;
        private String title;
        private String dateTime;
        private String downloadUrl;

        public PaymentSlip(String transId, String title, String dateTime, String downloadUrl) {
            this.transId = transId;
            this.title = title;
            this.dateTime = dateTime;
            this.downloadUrl = downloadUrl;
        }

        public String getTransId() {
            return transId;
        }

        public String getTitle() {
            return title;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewDateTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.title);
            textViewDateTime = itemView.findViewById(R.id.date);
        }
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<PaymentSlip> paymentSlipsList;

        public MyAdapter(List<PaymentSlip> paymentSlipsList) {
            this.paymentSlipsList = paymentSlipsList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            PaymentSlip paymentSlip = paymentSlipsList.get(position);
            holder.textViewTitle.setText(paymentSlip.getTitle());
            holder.textViewDateTime.setText(paymentSlip.getDateTime());

            findViewById(R.id.pb).setVisibility(View.GONE);
            // Set click listener to fetch downloadUrl
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    //Network check
                    if(((ConnectivityManager) MainMenu.this
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getActiveNetworkInfo() != null){

                        String downloadUrl = paymentSlip.getDownloadUrl();
                        String Title = paymentSlip.getTitle();
                        try {
                            downloadPDF (downloadUrl, Title);
                        } catch (IOException e) {
                            Toast.makeText(MainMenu.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        Intent nointernet = new Intent(MainMenu.this, NoInternet.class);
                        startActivity(nointernet);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return paymentSlipsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewTitle;
            public TextView textViewDateTime;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.title);
                textViewDateTime = itemView.findViewById(R.id.date);
            }
        }
    }


    FirebaseStorage storage;
    StorageReference pdfRef;
    File localFile;
    private void downloadPDF (String downloadUrl, String Title) throws IOException {
        storage = FirebaseStorage.getInstance();
        pdfRef = storage.getReferenceFromUrl(downloadUrl);

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Downloading PDF...");
        pd.show();

        if (checkPermission()) {
            localFile = File.createTempFile("RUETPay", ".pdf");
            pdfRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Intent savepdf = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                            savepdf.addCategory(Intent.CATEGORY_OPENABLE);
                            savepdf.setType("application/pdf");
                            savepdf.putExtra(Intent.EXTRA_TITLE, Title + ".pdf");
                            pd.dismiss();
                            startActivityForResult(savepdf, 1);
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                            float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            pd.setMessage("Downloaded: " + (int) percent + "%");
                        }
                    })
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        pd.dismiss();
                        Toast.makeText(this, "Error Downloading pdf: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(MainMenu.this, "Please Provide Storage permission to save pdf!", Toast.LENGTH_SHORT).show();
            requestPermission();
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                try {
                    InputStream in = new FileInputStream(localFile);
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();

                    localFile.delete();
                    Toast.makeText(this, "Pdf Saved Successfully!", Toast.LENGTH_SHORT).show();

                    Intent opnpdf = new Intent(Intent.ACTION_VIEW);
                    opnpdf.setDataAndType(uri, "application/pdf");
                    opnpdf.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        startActivity(opnpdf);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "No PDF viewer available!", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to Decrypt pdf: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else Toast.makeText(this, "Storage Error!", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this, "PDF Download cancelled or failed!", Toast.LENGTH_SHORT).show();
    }
}