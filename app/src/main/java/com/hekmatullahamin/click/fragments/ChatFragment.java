package com.hekmatullahamin.click.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.activities.ChatActivity;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.MyFirebaseAdapter;
import com.hekmatullahamin.click.utils.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private View fragmentView;
    private View rowView;

    private RecyclerView recyclerView;
    private FirebaseRecyclerOptions<User> options;

    private DatabaseReference usersReference, userContactsReference;
    private FirebaseUser currentUser;

    private String senderUserId = "";
    private List<String> receiverIds;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView =  inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.chatFragmentRecyclerViewId);
        receiverIds = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser!=null) {

            senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        userContactsReference = FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS).child(senderUserId);
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycler_view_line));
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(userContactsReference, User.class)
                .build();

        FirebaseRecyclerAdapter<User, ViewHolder> adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull User model) {
                String friendsId = getRef(position).getKey();
                receiverIds.add(friendsId);

                usersReference.child(friendsId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.rowName.setText(snapshot.child(Constants.USER_NAME).getValue().toString());

                        if (snapshot.exists() && snapshot.hasChild(Constants.USER_PROFILE)) {
                            String profileUrl = snapshot.child(Constants.USER_PROFILE).getValue().toString();
                            Picasso.get().load(profileUrl).into(holder.rowProfileImage);
                        }
                        //TODO do it on start and stop
                        if (snapshot.exists() && snapshot.hasChild("userStatus")) {
                            String userState = snapshot.child("userStatus").child("state").getValue().toString();
                            String date = snapshot.child("userStatus").child("date").getValue().toString();
                            String time = snapshot.child("userStatus").child("time").getValue().toString();

                            holder.lastSeen.setVisibility(View.VISIBLE);
                            if (userState.equals("online")) {
                                holder.onlineImage.setVisibility(View.VISIBLE);
                                holder.lastSeen.setText(userState);
                            }else {
                                holder.onlineImage.setVisibility(View.INVISIBLE);
                                holder.lastSeen.setText("Last seen:\n" + date + "\n" + time);
                            }
                        }else {
                            holder.onlineImage.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int friendListPosition = holder.getAdapterPosition();
                        String friendListReceiverID = receiverIds.get(friendListPosition);

                        Intent chatActivityIntent = new Intent(getContext(), ChatActivity.class);
                        chatActivityIntent.putExtra(Constants.CHAT_FRAGMENT_USER_ID, friendListReceiverID);
                        chatActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(chatActivityIntent);
                    }
                });

            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_row, parent, false);
                return new ViewHolder(rowView);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView rowProfileImage;
        private TextView rowName, rowAbout, lastSeen;
        private ImageView onlineImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rowProfileImage = (CircleImageView) itemView.findViewById(R.id.friendsRowProfileImageViewId);
            rowName = (TextView) itemView.findViewById(R.id.friendsRowNameTextViewId);
            rowAbout = (TextView) itemView.findViewById(R.id.friendsRowAboutTextViewId);
            lastSeen = (TextView) itemView.findViewById(R.id.friendsRowLastSeenTextViewId);
            onlineImage = (ImageView) itemView.findViewById(R.id.friendsRowOnlineStatusImageViewId);

            rowAbout.setVisibility(View.INVISIBLE);
        }
    }

}