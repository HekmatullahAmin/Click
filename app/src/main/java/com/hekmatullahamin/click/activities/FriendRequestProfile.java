package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Methods;
import com.hekmatullahamin.click.utils.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestProfile extends AppCompatActivity {

    private CircleImageView circleImageView;
    private TextView name;
    private TextView about;
    private Button sendRequestButton;

    private Toolbar toolbar;
    private User selectedUser;

    private DatabaseReference requestsReference, contactsReference, usersReference;

    private String senderUserId;
    private String receiverUserId;
    private String current_state = "new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_profile);

        fieldsInitialization();
        selectedUser = (User) getIntent().getSerializableExtra(Constants.USER_FIND_FRIEND_BUNDLE);

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId = selectedUser.getUserId();

        name.setText(selectedUser.getName());
        about.setText(selectedUser.getUserAbout());
        Picasso.get().load(selectedUser.getUserProfile()).placeholder(getResources().getDrawable(R.drawable.default_profile_picture))
                .into(circleImageView);

        if (senderUserId.equals(receiverUserId)) {
            sendRequestButton.setVisibility(View.INVISIBLE);
        }

        manageChatRequest();
    }

    private void fieldsInitialization() {
        circleImageView = (CircleImageView) findViewById(R.id.friendRequestActivityProfilePictureCircleImageViewId);
        name = (TextView) findViewById(R.id.friendRequestActivityNameTextViewId);
        about = (TextView) findViewById(R.id.friendRequestActivityAboutTextViewId);
        toolbar = (Toolbar) findViewById(R.id.friendRequestActivityProfileActionBarId);
        sendRequestButton = (Button) findViewById(R.id.friendRequestActivityUpdateProfileButtonId);
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        requestsReference = FirebaseDatabase.getInstance().getReference().child(Constants.FRIEND_REQUESTS);
        contactsReference = FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friend request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void manageChatRequest() {
        //TODO remove listener
        requestsReference.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(receiverUserId)) {
                   String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                   if (request_type.equals("sent")) {
                       current_state = "request_sent";
                       sendRequestButton.setText("Cancel friend request");
                   }else if (request_type.equals("received")){
                       current_state = "request_received";
                       sendRequestButton.setText("Accept request");
                   }
                }else {
                    contactsReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receiverUserId)) {
                                current_state = "friends";
                                sendRequestButton.setText("Remove contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!senderUserId.equals(receiverUserId)) {
            sendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestButton.setEnabled(false);
                    switch (current_state) {
                        case "request_sent":
                            cancelFriendRequest();
                            break;
                        case "request_received":
                            acceptFriendRequest();
                            break;
                        case "friends":
                            removeContact();
                            break;
                        case "new":
                            sendFriendRequest();
                            break;
                    }
                }
            });
        }else {
            sendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void sendFriendRequest() {
       requestsReference.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful()) {
                    requestsReference.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendRequestButton.setEnabled(true);
                                current_state = "request_sent";
                                sendRequestButton.setText("Cancel friend request");
                            }
                        }
                    });
               }
           }
       });
    }

    private void removeContact() {
        contactsReference.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsReference.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendRequestButton.setEnabled(true);
                                current_state = "new";
                                sendRequestButton.setText("Send Request");
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendRequest() {
        contactsReference.child(senderUserId).child(receiverUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsReference.child(receiverUserId).child(senderUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                requestsReference.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            requestsReference.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        sendRequestButton.setEnabled(true);
                                                        current_state = "friends";
                                                        sendRequestButton.setText("Remove contact");
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    private void cancelFriendRequest() {
        requestsReference.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    requestsReference.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sendRequestButton.setEnabled(true);
                            current_state = "new";
                            sendRequestButton.setText("Send request");
                        }
                    });
                }else {
                    Toast.makeText(FriendRequestProfile.this, "Request cancellation failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (senderUserId != null) {
            Methods.updateUserStatus(FriendRequestProfile.this, usersReference, "online", senderUserId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senderUserId != null) {
            Methods.updateUserStatus(FriendRequestProfile.this, usersReference, "offline", senderUserId);
        }
    }

}