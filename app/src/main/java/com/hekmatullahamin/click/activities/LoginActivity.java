package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Methods;
import com.hekmatullahamin.click.utils.User;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText enteredEmail;
    private EditText enteredPassword;
    private Button logInButton;
    private Button signUpButton;
    private Button phoneButton;
    private ProgressBar progressBar;


    private FirebaseAuth auth;
    private DatabaseReference databaseReference, usersReference;

    private String userId;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fieldsInitialization();
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Sprite wanderingCubes = new WanderingCubes();
        progressBar.setIndeterminateDrawable(wanderingCubes);

        logInButton.setOnClickListener(this::onClick);
        signUpButton.setOnClickListener(this::onClick);
        phoneButton.setOnClickListener(this::onClick);

    }

    private void fieldsInitialization() {
        enteredEmail = (EditText) findViewById(R.id.loginActivityEnterEmailEditTextId);
        enteredPassword = (EditText) findViewById(R.id.loginActivityEnterPasswordEditTextId);
        logInButton = (Button) findViewById(R.id.loginActivityLogInButtonId);
        signUpButton = (Button) findViewById(R.id.loginActivitySignUpButtonId);
        phoneButton = (Button) findViewById(R.id.loginActivityPhoneAuthenticationButtonId);
        progressBar = (ProgressBar) findViewById(R.id.loginActivityProgressBarId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginActivityLogInButtonId:
                loginRegisteredUser();
                break;
            case R.id.loginActivitySignUpButtonId:
                sendUserToSignUpActivity();
                break;
            case R.id.loginActivityPhoneAuthenticationButtonId:
                sendUserToPhoneLoginActivity();
                break;
        }
    }

    private void loginRegisteredUser() {
        String email = enteredEmail.getText().toString().trim();
        String password = enteredPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        bundle = getIntent().getExtras();

                        usersReference = databaseReference.child(Constants.USERS).child(userId);

                        // if condition is for when we come from sign up activity otherwise bundle will be empty
                        // and after sign up we must first sign in before making another account
                        if (bundle != null) {
                            User user = (User) bundle.getSerializable(Constants.USER_SIGN_UP_BUNDLE);
                            user.setUserId(userId);
                            user.setUserAbout("Hey! I am using Click.");

                            Map<String, Object> userDetailsHashMap = new HashMap<>();
                            userDetailsHashMap.put("name", user.getName());
                            userDetailsHashMap.put("surname", user.getSurname());
                            userDetailsHashMap.put("userId", user.getUserId());
                            userDetailsHashMap.put("userAbout", user.getUserAbout());
                            userDetailsHashMap.put("email", user.getEmail());
                            userDetailsHashMap.put("password", user.getPassword());

                            usersReference.setValue(userDetailsHashMap).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // for progress bar to gone after 3 seconds
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                                sendUserToMainActivity();
                                            }
                                        }, 3000);
                                    }
                                }
                            });
                        }else {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    sendUserToMainActivity();
                                }
                            }, 3000);
                        }


                    }else {
                        Toast.makeText(LoginActivity.this, "Couldn't sign in", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }else {
            Toast.makeText(LoginActivity.this, "Please fill the all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }

    private void sendUserToSignUpActivity() {
        Intent signUpActivityIntent = new Intent(LoginActivity.this, SignupActivity.class);
        signUpActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(signUpActivityIntent);
        finish();
    }

    private void sendUserToPhoneLoginActivity() {
        Intent phoneLoginActivityIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        phoneLoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneLoginActivityIntent);
        finish();
    }

}