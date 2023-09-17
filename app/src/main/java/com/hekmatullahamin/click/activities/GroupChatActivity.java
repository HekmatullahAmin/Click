package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Message;
import com.hekmatullahamin.click.utils.Methods;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private ImageButton attachFileImageButton;
    private EditText typeMessageEditText;
    private Button sendMessageButton;

    private DatabaseReference usersReference, groupsReference, groupsMessagesReference, specificGroupMessagesReference;
    private FirebaseUser currentUser;
    private String currentUserId;
    private String groupId = "";

    private FirebaseRecyclerOptions<Message> options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupId = getIntent().getExtras().getString(Constants.GROUP_ID);
        fieldsInitialization();


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToGroup();
            }
        });
    }

    private void fieldsInitialization() {

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.groupChatActivityActionBarId);
        recyclerView = (RecyclerView) findViewById(R.id.groupChatActivityRecyclerViewId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        attachFileImageButton = (ImageButton) findViewById(R.id.groupChatActivitySendFileImageButtonId);
        typeMessageEditText = (EditText) findViewById(R.id.groupChatActivityTypeAMessageEditTextId);
        sendMessageButton = (Button) findViewById(R.id.groupChatActivitySendMessageButtonId);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        groupsReference = FirebaseDatabase.getInstance().getReference().child(Constants.GROUPS);
        groupsMessagesReference = FirebaseDatabase.getInstance().getReference().child(Constants.GROUPS_MESSAGES);
        specificGroupMessagesReference = groupsMessagesReference.child(groupId);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Group Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void sendMessageToGroup() {
        String messageToGroup = typeMessageEditText.getText().toString();
        if (!messageToGroup.equals("")) {
            Date date = new Date();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
            String messageDate = dateFormatter.format(date);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("E, HH:mm");
            String messageTime = timeFormatter.format(date);

            String messageId = groupsMessagesReference.push().getKey();

            Map<String, Object> groupsMessagesDetailsHashMap = new HashMap<>();
            groupsMessagesDetailsHashMap.put(Constants.MESSAGE_ID, messageId);
            groupsMessagesDetailsHashMap.put(Constants.DATE, messageDate);
            groupsMessagesDetailsHashMap.put(Constants.TIME, messageTime);
            groupsMessagesDetailsHashMap.put(Constants.MESSAGE, messageToGroup);
            groupsMessagesDetailsHashMap.put(Constants.SENDER_ID, currentUserId);

            groupsMessagesReference.child(groupId).child(messageId).setValue(groupsMessagesDetailsHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(GroupChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                        typeMessageEditText.setText("");
                    }
                }
            });
        }else {
            Toast.makeText(GroupChatActivity.this, "Please write something first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId != null) {
            Methods.updateUserStatus(GroupChatActivity.this, usersReference, "online", currentUserId);
        }

        options = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(specificGroupMessagesReference, Message.class)
                .build();

        FirebaseRecyclerAdapter<Message, ViewHolder> adapter = new FirebaseRecyclerAdapter<Message, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Message model) {
                String messageId = getRef(position).getKey();

                specificGroupMessagesReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot allGroupMessages) {

                        Iterator iterator = allGroupMessages.getChildren().iterator();
                        while (iterator.hasNext()) {
                            //for checking who has sent the message
                            if (allGroupMessages.child(messageId).hasChild(Constants.SENDER_ID)) {
                                String senderId = allGroupMessages.child(messageId).child(Constants.SENDER_ID).getValue().toString();

                                // if this message sender is current user
                                if (senderId.equals(currentUserId)) {
                                    holder.receiverView.setVisibility(View.INVISIBLE);
                                    holder.senderView.setVisibility(View.VISIBLE);

                                    holder.senderTime.setText(model.getDate() + " " + model.getTime());
                                    holder.senderMessage.setText(model.getMessage());
                                    usersReference.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String name = snapshot.child(Constants.USER_NAME).getValue().toString();
                                            holder.senderName.setText(name);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }else {
                                    holder.receiverView.setVisibility(View.VISIBLE);
                                    holder.senderView.setVisibility(View.INVISIBLE);

                                    holder.receiverTime.setText(model.getDate() + " " + model.getTime());
                                    holder.receiverMessage.setText(model.getMessage());
                                    usersReference.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String name = snapshot.child(Constants.USER_NAME).getValue().toString();
                                            holder.receiverName.setText(name);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
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
        adapter.startListening();
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

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUserId != null) {
            Methods.updateUserStatus(GroupChatActivity.this, usersReference, "offline", currentUserId);
        }

    }
}