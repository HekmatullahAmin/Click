package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Methods;
import com.hekmatullahamin.click.utils.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userName;
    private EditText userAbout;
    private TextView userMobileNumber;
    private CircleImageView userProfilePicture;
    private ImageButton changeProfileImageButton;
    private Button updateButton;
    private Toolbar settingsToolbar;

    private StorageReference storageReference, profilePictureReference;
    private DatabaseReference databaseReference, usersReference;

    private String currentUserId;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        profilePictureReference = storageReference.child("Profile picture");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        usersReference = databaseReference.child(Constants.USERS);

        fieldsInitialization();
        retrieveUserProfile();

        userProfilePicture.setOnClickListener(this::onClick);
        changeProfileImageButton.setOnClickListener(this::onClick);
        updateButton.setOnClickListener(this::onClick);

    }

    private void fieldsInitialization() {
        userName = (EditText) findViewById(R.id.settingsActivityNameEditTextId);
        userAbout = (EditText) findViewById(R.id.settingsActivityAboutEditTextId);
        userMobileNumber = (TextView) findViewById(R.id.settingsActivityMobileNumberTextViewId);
        userProfilePicture = (CircleImageView) findViewById(R.id.settingsActivityProfilePictureImageViewId);
        changeProfileImageButton = (ImageButton) findViewById(R.id.settingsActivityChangeProfilePictureImageButton);
        updateButton = (Button) findViewById(R.id.settingsActivityUpdateProfileButtonId);
        settingsToolbar = (Toolbar) findViewById(R.id.settingsActivityActionBarId);

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void retrieveUserProfile() {
        // TODO remover listener
        usersReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild(Constants.USER_PROFILE) && snapshot.hasChild(Constants.USER_ABOUT)) {
                    String profileUrl = snapshot.child(Constants.USER_PROFILE).getValue().toString();
                    Picasso.get().load(profileUrl).into(userProfilePicture);

                    userAbout.setText(snapshot.child(Constants.USER_ABOUT).getValue().toString());
                }
                userName.setText(snapshot.child(Constants.USER_NAME).getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingsActivityProfilePictureImageViewId:
                //TODO show full profile picture
//                displayFullProfilePicture();
                break;
            case R.id.settingsActivityChangeProfilePictureImageButton:
                changeProfilePicture();
                break;
            case R.id.settingsActivityUpdateProfileButtonId:
                updateUserProfile();
                break;
        }
    }

    private void displayFullProfilePicture() {
        //TODO display full image of profile picture
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.full_profile_image, null);
        builder.setView(view);
        ImageView imageView = view.findViewById(R.id.fullProfileImageViewId);
        imageView.setImageURI(imageUri);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void changeProfilePicture() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Choose your image"), Constants.GALLERY_REQUEST_CODE);
    }

    private void updateUserProfile() {
        String name = userName.getText().toString().trim();
        String about = userAbout.getText().toString().trim();
        //TODO add mobile to user
        String mobileNumber = userMobileNumber.getText().toString().trim();

        if (!TextUtils.isEmpty(name)) {

            User user = new User();

            if (TextUtils.isEmpty(about)) {
                user.setUserAbout("Hey! I am using Click");
            }

            user.setName(name);
            user.setUserAbout(about);
            Map<String, Object> userDetailsHashMap = new HashMap<>();
            userDetailsHashMap.put(Constants.USER_NAME, user.getName());
            userDetailsHashMap.put(Constants.USER_ABOUT, user.getUserAbout());

            usersReference.child(currentUserId).updateChildren(userDetailsHashMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            Toast.makeText(SettingsActivity.this, "Name us mandatory.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // full image uri
            imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                // cropped image uri
                Uri resultUri = result.getUri();
                StorageReference filePath = profilePictureReference.child(currentUserId + ".jpg");

                //for putting our profile image to firebase
                filePath.putFile(resultUri).addOnCompleteListener(SettingsActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            // for getting an url for our profile picture
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri profileUri) {

                                    String downloadUrl = profileUri.toString();
                                    User user = new User();
                                    user.setUserProfile(profileUri.toString());
                                    Map<String, Object> userDetailsHashMap = new HashMap<>();
                                    userDetailsHashMap.put(Constants.USER_PROFILE, downloadUrl);

                                    usersReference.child(currentUserId).updateChildren(userDetailsHashMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            Toast.makeText(SettingsActivity.this, "Profile picture added successfully", Toast.LENGTH_SHORT).show();
                                            Picasso.get().load(downloadUrl).placeholder(R.drawable.default_profile_picture).into(userProfilePicture);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SettingsActivity.this, "Your profile image could't get downloaded", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId != null) {
            Methods.updateUserStatus(SettingsActivity.this, usersReference, "online", currentUserId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUserId != null) {
            Methods.updateUserStatus(SettingsActivity.this, usersReference, "offline", currentUserId);
        }
    }
}