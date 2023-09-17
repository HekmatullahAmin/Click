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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Methods;
import com.hekmatullahamin.click.utils.User;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText enteredName;
    private EditText enteredSurname;
    private EditText enteredEmail;
    private EditText enteredPassword;
    private EditText enteredPasswordAgain;
    private Button goToLogInButton;
    private Button signUpButton;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fieldsInitialization();
        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        goToLogInButton.setOnClickListener(this::onClick);
        signUpButton.setOnClickListener(this::onClick);
    }

    private void fieldsInitialization() {
        enteredName = (EditText) findViewById(R.id.signUpActivityEnterNameEditTextId);
        enteredSurname = (EditText) findViewById(R.id.signUpActivityEnterSureNameEditTextId);
        enteredEmail = (EditText) findViewById(R.id.signUpActivityEnterEmailEditTextId);
        enteredPassword = (EditText) findViewById(R.id.signUpActivityEnterPasswordEditTextId);
        enteredPasswordAgain = (EditText) findViewById(R.id.signUpActivityEnterPasswordAgainEditTextId);
        goToLogInButton = (Button) findViewById(R.id.signUpActivityGoToLoginActivityButtonId);
        signUpButton = (Button) findViewById(R.id.signUpActivityRegisterAccountButtonId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpActivityGoToLoginActivityButtonId:
                sendUserToLoginActivity();
                break;
            case R.id.signUpActivityRegisterAccountButtonId:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String name = enteredName.getText().toString().trim();
        String surname = enteredSurname.getText().toString().trim();
        String email = enteredEmail.getText().toString().trim();
        String password = enteredPassword.getText().toString().trim();
        String reTypedPassword = enteredPasswordAgain.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(surname) && !TextUtils.isEmpty(email) &&
                !TextUtils.isEmpty(password) && !TextUtils.isEmpty(reTypedPassword)) {

            if (!password.equals(reTypedPassword)) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            }else {
                progressDialog.setTitle("Creating Account");
                progressDialog.setMessage("Please wait while we are creating your account");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            User user = new User();
                            user.setName(name);
                            user.setSurname(surname);
                            user.setEmail(email);
                            user.setPassword(password);

                            Toast.makeText(SignupActivity.this, "Account " + name + " created successfully", Toast.LENGTH_SHORT).show();

                            Intent loginActivityIntent = new Intent(SignupActivity.this, LoginActivity.class);
                            Bundle userBundle = new Bundle();
                            userBundle.putSerializable(Constants.USER_SIGN_UP_BUNDLE, user);
                            loginActivityIntent.putExtras(userBundle);
                            loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(loginActivityIntent);
                            finish();
                        }else {
                            Toast.makeText(SignupActivity.this, "Account didn't created", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }

        }else {
            Toast.makeText(this, "Please fill the all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserToLoginActivity() {
        Intent loginActivityIntent = new Intent(SignupActivity.this, LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivityIntent);
        finish();
    }

}