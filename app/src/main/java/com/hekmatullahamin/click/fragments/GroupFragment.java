package com.hekmatullahamin.click.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.activities.ChooseFriendsForGroupActivity;
import com.hekmatullahamin.click.activities.GroupChatActivity;
import com.hekmatullahamin.click.activities.MainActivity;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class GroupFragment extends Fragment {

    private View groupFragment;

    private DatabaseReference groupReference, userGroupsReference;

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private FirebaseUser currentUser;
    private String currentUserId;

    private AlertDialog.Builder dialogBuilder;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        groupFragment = inflater.inflate(R.layout.fragment_group, container, false);
        groupReference = FirebaseDatabase.getInstance().getReference().child(Constants.GROUPS);
        if (currentUserId != null) {
            userGroupsReference = groupReference.child(currentUserId);
        }

        recyclerView = (RecyclerView) groupFragment.findViewById(R.id.groupFragmentRecyclerViewId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycler_view_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
        floatingActionButton = (FloatingActionButton) groupFragment.findViewById(R.id.groupFragmentFloatingActionButtonId);
        return groupFragment;

    }

    @Override
    public void onStart() {
        super.onStart();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToChooseFriendsForGroupActivity();
            }
        });

        if (userGroupsReference != null) {
            FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
                    .setQuery(userGroupsReference, Group.class)
                    .build();

            FirebaseRecyclerAdapter<Group, ViewHolder> adapter = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Group model) {
                    String groupId = getRef(position).getKey();
                    if (groupId != null) {
                        groupReference.child(currentUserId).child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String groupName = snapshot.child(Constants.GROUP_NAME).getValue(String.class);
                                    if (groupName != null) {
                                        holder.groupName.setText(groupName);
                                    } else {
                                        Log.e("GroupFragment", "Group name is null");
                                    }
                                } else {
                                    Log.e("GroupFragment", "Snapshot does not exist");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("GroupFragment", "Database error: " + error.getMessage());
                            }
                        });
                    } else {
                        Log.e("GroupFragment", "Group ID is null");
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent groupChatActivityIntent = new Intent(getContext(), GroupChatActivity.class);
                            if (groupId != null) {
                                groupChatActivityIntent.putExtra(Constants.GROUP_ID, groupId);
                                startActivity(groupChatActivityIntent);
                            } else {
                                Log.e("GroupFragment", "Group ID is null");
                            }
                        }
                    });
                }

                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_friend_to_group, null);
                    return new ViewHolder(view);
                }
            };
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        } else {
            Log.e("GroupFragment", "userGroupsReference is null");
        }
    }
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendUserToChooseFriendsForGroupActivity();
//            }
//        });
//
//        FirebaseRecyclerOptions<Group> options = new FirebaseRecyclerOptions.Builder<Group>()
//                .setQuery(userGroupsReference, Group.class)
//                .build();
//
//        FirebaseRecyclerAdapter<Group, ViewHolder> adapter = new FirebaseRecyclerAdapter<Group, ViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Group model) {
//                String groupId = getRef(position).getKey();
//                groupReference.child(currentUserId).child(groupId).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String groupName = snapshot.child(Constants.GROUP_NAME).getValue().toString();
//                        holder.groupName.setText(groupName);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent groupChatActivityIntent = new Intent(getContext(), GroupChatActivity.class);
//                        groupChatActivityIntent.putExtra(Constants.GROUP_ID, groupId);
//                        startActivity(groupChatActivityIntent);
//                    }
//                });
//            }
//
//            @NonNull
//            @Override
//            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_friend_to_group, null);
//                return new ViewHolder(view);
//            }
//        };
//        recyclerView.setAdapter(adapter);
//        adapter.startListening();
//
//    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;
        private CheckBox checkBox;
        private CircleImageView groupImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.selectFriendToGroupRowNameTextViewId);
            groupImage = itemView.findViewById(R.id.selectFriendToGroupRowCircleImageViewId);
            checkBox = itemView.findViewById(R.id.selectFriendToGroupCheckBoxId);
            checkBox.setVisibility(View.GONE);
        }
    }

    private void sendUserToChooseFriendsForGroupActivity() {
        Intent chooseFriendsForGroupActivity = new Intent(getContext(), ChooseFriendsForGroupActivity.class);
        chooseFriendsForGroupActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(chooseFriendsForGroupActivity);
    }
}