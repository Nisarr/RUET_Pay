package com.ruet.pay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

import androidx.annotation.NonNull;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import pl.droidsonroids.gif.GifImageView;


public class OTPDialog extends Dialog {
    EditText otp1, otp2, otp3, otp4;
    EditText[] otpFields = new EditText[4];
    TextView resendbtn, email_view;
    GifImageView verifybtn;
    private boolean resendEnable = false;
    int selectPosition = 0, code;
    private final String Email, Pass;
    Context context;
    public OTPDialog(@NonNull Context context, String Email, String Pass) {
        super(context);
        this.Email = Email;
        this.Pass = Pass;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent)));
        setContentView(R.layout.otpdialog);

        code = sendotp(Email, context);

        email_view = findViewById(R.id.email_view);
        email_view.setText(Email);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otpFields[0] = findViewById(R.id.otp1);
        otpFields[1] = findViewById(R.id.otp2);
        otpFields[2] = findViewById(R.id.otp3);
        otpFields[3] = findViewById(R.id.otp4);

        resendbtn = findViewById(R.id.resendbtn);
        verifybtn = findViewById(R.id.verifybtn);

        for (int i = 0; i<4; i++) {
            otpFields[i].addTextChangedListener(textWatcher);
        }

        showKeyboard(otp1);
        startCountDownTimer();

        resendbtn.setOnClickListener(view -> {
            if(resendEnable){
                code = sendotp(Email, context);
                startCountDownTimer();
            }
            else Toast.makeText(context, "Wait! Until you can send another Code.",Toast.LENGTH_SHORT).show();
        });

        verifybtn.setOnClickListener(view -> {
            String OTP = otp1.getText().toString() + otp2.getText().toString() + otp3.getText().toString() + otp4.getText().toString();
            if (OTP.equals(String.valueOf(code))){
                Intent intent = new Intent(context, Student_Reg.class);
                intent.putExtra("Email", Email);
                intent.putExtra("Pass", Pass);
                context.startActivity(intent);
                dismiss();
            }
            else{
                Toast.makeText(context,"OTP not Matched.\nVerification Error!",Toast.LENGTH_SHORT).show();
            }
        });


    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            for (int i = 0; i < 4; i++) {
                if (editable == otpFields[i].getEditableText()) {
                    selectPosition = i;
                    break;
                }
            }

            // If the current position is not the last position, move focus to the next EditText
            if (selectPosition < 3) {
                showKeyboard(otpFields[selectPosition + 1]);
            }

        }
    };

    private void showKeyboard (EditText otp){
        otp.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(otp, InputMethodManager.SHOW_IMPLICIT);
    }

    private void startCountDownTimer (){
        resendEnable = false;
        resendbtn.setTextColor(Color.parseColor("#99000000"));

        new CountDownTimer(60000,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                resendbtn.setText("Resend Code (" + (millisUntilFinished / 1000) + ")");
            }

            @Override
            public void onFinish() {
                resendEnable = true;
                resendbtn.setText("Resend Code");
                resendbtn.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            }
        }.start();
    }

    private int sendotp(String stringReceiverEmail, Context context){
        try {
            Random random = new Random();
            code = random.nextInt(8999) + 1000;

            String stringSenderEmail = "ruetpay@gmail.com";
            String stringPasswordSenderEmail = "kvxh zrdc flrc apuo";

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(stringSenderEmail,"RUET Pay"));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(stringReceiverEmail));

            mimeMessage.setSubject("Verify your email for RUET Pay");
            mimeMessage.setText("Hello RUETian,\n" +
                    "Here is the one time verification code to verify your Email for RUET Pay App: \n\n" +
                    code + "\n\n" +
                    "If you didn’t ask to verify, you can ignore this email.\n\n" +
                    "Happy Journey with RUET Pay,\n" +
                    "RUET Pay team");

            Thread thread = new Thread(() -> {
                try {
                    Transport.send(mimeMessage);
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                    });
                } catch (MessagingException e) {
                    e.printStackTrace();
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Error sending OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });
            thread.start();

        } catch (MessagingException e) {
            e.printStackTrace();
            ((Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, "Error sending OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return code;
    }
}