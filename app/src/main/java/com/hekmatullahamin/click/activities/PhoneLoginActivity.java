package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hekmatullahamin.click.R;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mobileNumber, verificationCode;
    private Button sendVerificationCodeButton, verifyButton;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String systemCodeSent;
    private String userCodeEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        fieldsInitialization();

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(PhoneLoginActivity.this);

        sendVerificationCodeButton.setOnClickListener(this::onClick);
        verifyButton.setOnClickListener(this::onClick);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, try with country code", Toast.LENGTH_SHORT).show();
                verificationCode.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);

                mobileNumber.setVisibility(View.VISIBLE);
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken); // afjklsjflksdjfaljfl;j
                verificationCode.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);

                mobileNumber.setVisibility(View.INVISIBLE);
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);


                systemCodeSent = s;
                progressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify", Toast.LENGTH_SHORT).show();

            }
        };
    }

    private void fieldsInitialization() {
        mobileNumber = (EditText) findViewById(R.id.phoneLoginActivityMobileNumberEditTextId);
        verificationCode = (EditText) findViewById(R.id.phoneLoginActivitySentCodeEditTextId);
        sendVerificationCodeButton = (Button) findViewById(R.id.phoneLoginActivitySendVerificationCodeButtonId);
        verifyButton = (Button) findViewById(R.id.phoneLoginActivityVerifyButtonId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.phoneLoginActivitySendVerificationCodeButtonId:
                sentCodeToPhone();
                break;
            case R.id.phoneLoginActivityVerifyButtonId:
                verifyPhoneNumber();
                break;
        }
    }


    private void sentCodeToPhone() {
        String phoneNumber = mobileNumber.getText().toString();

        if (!TextUtils.isEmpty(phoneNumber)) {

            progressDialog.setTitle("Phone verification");
            progressDialog.setMessage("Pleas wait, while we are authentication your phone");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }else {
            Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        }

    }

    private void verifyPhoneNumber() {
        verificationCode.setVisibility(View.INVISIBLE);
        verifyButton.setVisibility(View.INVISIBLE);

        mobileNumber.setVisibility(View.VISIBLE);
        sendVerificationCodeButton.setVisibility(View.VISIBLE);

        userCodeEntered = verificationCode.getText().toString().trim();
        if (!TextUtils.isEmpty(userCodeEntered)) {
            progressDialog.setTitle("Code verification");
            progressDialog.setMessage("Pleas wait, while we are verifying verification code");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(systemCodeSent, userCodeEntered);
            signInWithPhoneAuthCredential(credential);
        }else {
            Toast.makeText(PhoneLoginActivity.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Log in successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else {
                            // Sign in failed, display a message and update the UI
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }


}