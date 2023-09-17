package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseFriendsForGroupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView toolbarTitle, subTitle;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionArrowButton;

    private DatabaseReference usersReference, groupReference;

    private FirebaseRecyclerOptions<User> options;

    private List<String> allParticipantsId;
    private AlertDialog.Builder builder;
    private Dialog dialog;
    private EditText groupSubject;
    private Button createGroupButton;

    private FirebaseUser currentUser;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_friends_for_group);

        fieldsInitialization();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(ChooseFriendsForGroupActivity.this,
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycler_view_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChooseFriendsForGroupActivity.this));

        floatingActionArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                openGroupDialogAndCreateGroup();
            }
        });
    }

    private void fieldsInitialization() {
        toolbar = (Toolbar) findViewById(R.id.chooseFriendsForGroupActivityActionBarId);

        toolbarTitle = (TextView) findViewById(R.id.groupToolbarTitleTextViewId);
        subTitle = (TextView) findViewById(R.id.groupToolbarSubTitleTextViewId);
        toolbar.setTitle("");
        toolbarTitle.setText("New group");
        subTitle.setText("Add participants");
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.chooseFriendsForGroupActivityRecyclerViewId);
        floatingActionArrowButton = (FloatingActionButton) findViewById(R.id.chooseFriendsFOrGroupFloatingActionButtonId);
        allParticipantsId = new ArrayList<>();

        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        groupReference = FirebaseDatabase.getInstance().getReference().child(Constants.GROUPS);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
    }

    private void openGroupDialogAndCreateGroup() {
        builder = new AlertDialog.Builder(ChooseFriendsForGroupActivity.this);
        View view = LayoutInflater.from(ChooseFriendsForGroupActivity.this).inflate(R.layout.custom_add_group_subject_dialog, null);
        groupSubject = (EditText) view.findViewById(R.id.customAddGroupSubjectDialogSubjectNameEditTextId);
        createGroupButton = (Button) view.findViewById(R.id.customAddGroupSubjectDialogCreateGroupButtonId);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGroupToCloud();
            }
        });
    }

    private void saveGroupToCloud() {
        if (!TextUtils.isEmpty(groupSubject.getText().toString())) {
            String groupId = groupReference.push().getKey();
            String subject = groupSubject.getText().toString();

            Map<String, Object> groupDetailsHashMap = new HashMap<>();
            groupDetailsHashMap.put(Constants.GROUP_ID, groupId);
            groupDetailsHashMap.put(Constants.GROUP_MEMBERS, allParticipantsId);
            groupDetailsHashMap.put(Constants.GROUP_NAME, subject);
            groupDetailsHashMap.put(Constants.GROUP_ADMIN, currentUserId);

            for (int i = 0; i < allParticipantsId.size(); i++) {
                saveGroupForEveryMember(allParticipantsId.get(i), groupId, groupDetailsHashMap);
                if (i == allParticipantsId.size() - 1) {
                    Toast.makeText(ChooseFriendsForGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    sendUserToMainActivity();
                }
            }

        }else {
            Toast.makeText(ChooseFriendsForGroupActivity.this, "Please write down group subject", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGroupForEveryMember(String userId, String groupId, Map groupDetailsHashMap) {
        groupReference.child(userId).child(groupId).setValue(groupDetailsHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUserId != null) {
            Methods.updateUserStatus(ChooseFriendsForGroupActivity.this, usersReference, "online", currentUserId);
        }
        allParticipantsId.clear();

        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersReference, User.class)
                .build();

        FirebaseRecyclerAdapter<User, ViewHolder> adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull User model) {
                String userId = getRef(position).getKey();

                usersReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.hasChild(Constants.USER_PROFILE)) {
                            String profile = snapshot.child(Constants.USER_PROFILE).getValue().toString();
                            Picasso.get().load(profile).into(holder.circleImageView);
                        }
                        String name = snapshot.child(Constants.USER_NAME).getValue().toString();
                        holder.name.setText(name);

                        if (userId.equals(currentUserId)) {
                            allParticipantsId.add(currentUserId);
                            holder.checkBox.setVisibility(View.INVISIBLE);
                        }
                        holder.checkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (holder.checkBox.isChecked()) {
                                    allParticipantsId.add(model.getUserId());
                                }else {
                                    allParticipantsId.remove(model.getUserId());
                                }
                            }
                        });
                    }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_friend_to_group, parent, false);
                return new ViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView circleImageView;
        private TextView name;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = (CircleImageView) itemView.findViewById(R.id.selectFriendToGroupRowCircleImageViewId);
            name = (TextView) itemView.findViewById(R.id.selectFriendToGroupRowNameTextViewId);
            checkBox = (CheckBox) itemView.findViewById(R.id.selectFriendToGroupCheckBoxId);

        }
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(ChooseFriendsForGroupActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Bundle participantsId = new Bundle();
        participantsId.putStringArrayList(Constants.GROUP_MEMBERS_ID_TO_GROUP_FRAGMENT, (ArrayList<String>) allParticipantsId);
        mainActivityIntent.putExtras(participantsId);
        startActivity(mainActivityIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUserId != null) {
            Methods.updateUserStatus(ChooseFriendsForGroupActivity.this, usersReference, "offline", currentUserId);
        }
    }
}