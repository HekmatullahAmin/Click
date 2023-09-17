package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.icu.text.Edits;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.hekmatullahamin.click.utils.Message;
import com.hekmatullahamin.click.utils.Methods;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private ImageButton attachFileImageButton;
    private EditText typeMessageEditText;
    private Button sendMessageButton;

    private DatabaseReference messagesReference, senderToReceiverMessageReference, usersReference;

    private String friendToChatId;
    private String senderUserId;

    private FirebaseRecyclerOptions<Message> options;
    private FirebaseRecyclerAdapter<Message, ViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fieldsInitialization();
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendToChatId = getIntent().getExtras().getString(Constants.CHAT_FRAGMENT_USER_ID);
        messagesReference = FirebaseDatabase.getInstance().getReference().child(Constants.MESSAGES);
        senderToReceiverMessageReference = messagesReference.child(senderUserId).child(friendToChatId);
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToFriend();
            }
        });

        attachFileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private void fieldsInitialization() {

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.chatActivityActionBarId);
        recyclerView = (RecyclerView) findViewById(R.id.chatActivityRecyclerViewId);
        attachFileImageButton = (ImageButton) findViewById(R.id.chatActivitySendFileImageButtonId);
        typeMessageEditText = (EditText) findViewById(R.id.chatActivityTypeAMessageEditTextId);
        sendMessageButton = (Button) findViewById(R.id.chatActivitySendMessageButtonId);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void sendMessageToFriend() {
        String messageToSend = typeMessageEditText.getText().toString();
        if (!TextUtils.isEmpty(messageToSend)) {

            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
            String messageDate = dateFormatter.format(date);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("E, HH:mm");
            String messageTime = timeFormatter.format(date);

            String messageId = messagesReference.child(senderUserId).child(friendToChatId).push().getKey();


            Map<String, Object> messageHashMap = new HashMap<>();
            messageHashMap.put(Constants.MESSAGE, messageToSend);
            messageHashMap.put(Constants.DATE, messageDate);
            messageHashMap.put(Constants.TIME, messageTime);
            messageHashMap.put(Constants.ID, messageId);
            messageHashMap.put(Constants.TYPE, "sent");

            messagesReference.child(senderUserId).child(friendToChatId).child(messageId).setValue(messageHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        messageHashMap.replace(Constants.TYPE, "received");
                        messagesReference.child(friendToChatId).child(senderUserId).child(messageId).setValue(messageHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChatActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                                    typeMessageEditText.setText("");
                                }else {
                                    Toast.makeText(ChatActivity.this, "Couldn't receive message", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else {
                        Toast.makeText(ChatActivity.this, "Couldn't send message", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }else {
            Toast.makeText(ChatActivity.this, "Please write something first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (senderUserId != null) {
            Methods.updateUserStatus(ChatActivity.this, usersReference, "online", senderUserId);
        }

        options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(senderToReceiverMessageReference, Message.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Message, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Message model) {
                String messageId = getRef(position).getKey();
                // this has the reference of sender to specific person
                senderToReceiverMessageReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot allMessages) {
                        // iterator has all of our messages id
                        Iterator iterator = allMessages.getChildren().iterator();
                        while (iterator.hasNext()) {
                            if (allMessages.child(messageId).hasChild(Constants.TYPE)) {
                                String messageType = allMessages.child(messageId).child(Constants.TYPE).getValue().toString();
                                if (messageType.equals("sent")) {

                                    usersReference.child(senderUserId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.hasChild(Constants.USER_PROFILE)) {

                                                String senderProfileUrl = snapshot.child(Constants.USER_PROFILE).getValue().toString();
                                                String date = allMessages.child(messageId).child(Constants.DATE).getValue().toString();
                                                String time = allMessages.child(messageId).child(Constants.TIME).getValue().toString();

                                                holder.senderName.setText(snapshot.child(Constants.USER_NAME).getValue().toString());
                                                Picasso.get().load(senderProfileUrl).placeholder(R.drawable.default_profile_picture)
                                                        .into(holder.senderImageView);
                                                holder.senderMessage.setText(allMessages.child(messageId).child(Constants.MESSAGE).getValue().toString());
                                                holder.senderTime.setText(date + " " + time);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    holder.receiverView.setVisibility(View.INVISIBLE);
                                    holder.senderView.setVisibility(View.VISIBLE);

                                }else {

                                    usersReference.child(friendToChatId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists() && snapshot.hasChild(Constants.USER_PROFILE)) {

                                                String receiverProfileUrl = snapshot.child(Constants.USER_PROFILE).getValue().toString();
                                                String date = allMessages.child(messageId).child(Constants.DATE).getValue().toString();
                                                String time = allMessages.child(messageId).child(Constants.TIME).getValue().toString();

                                                holder.receiverName.setText(snapshot.child(Constants.USER_NAME).getValue().toString());
                                                Picasso.get().load(receiverProfileUrl).placeholder(R.drawable.default_profile_picture)
                                                        .into(holder.receiverImageView);
                                                holder.receiverMessage.setText(allMessages.child(messageId).child(Constants.MESSAGE).getValue().toString());
                                                holder.receiverTime.setText(date + " " + time);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    holder.receiverView.setVisibility(View.VISIBLE);
                                    holder.senderView.setVisibility(View.INVISIBLE);

                                }
                            }
                            iterator.next();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row, parent, false);
                return new ViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
        adapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senderUserId != null) {
            Methods.updateUserStatus(ChatActivity.this, usersReference, "offline", senderUserId);
        }

    }


    private class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView senderImageView;
        private TextView senderName;
        private TextView senderMessage;
        private TextView senderTime;

        private CircleImageView receiverImageView;
        private TextView receiverName;
        private TextView receiverMessage;
        private TextView receiverTime;

        private View receiverView;
        private View senderView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);



            senderImageView = (CircleImageView) itemView.findViewById(R.id.messageRowSenderProfilePictureCircleImageViewId);
            senderName = (TextView) itemView.findViewById(R.id.messageRowSenderNameTextViewId);
            senderMessage = (TextView) itemView.findViewById(R.id.messageRowSenderMessageTextViewId);
            senderTime = (TextView) itemView.findViewById(R.id.messageRowSenderTimeAndDateTextViewId);

            receiverImageView = (CircleImageView) itemView.findViewById(R.id.messageRowReceiverProfilePictureCircleImageViewId);
            receiverName = (TextView) itemView.findViewById(R.id.messageRowReceiverNameTextViewId);
            receiverMessage = (TextView) itemView.findViewById(R.id.messageRowReceiverMessageTextViewId);
            receiverTime = (TextView) itemView.findViewById(R.id.messageRowReceiverTimeAndDateTextViewId);

            senderView = (View) itemView.findViewById(R.id.messageRowSenderRelativeLayoutId);
            receiverView = (View) itemView.findViewById(R.id.messageRowReceiverRelativeLayoutId);
        }
    }
}