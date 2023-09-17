package com.hekmatullahamin.click.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private View fragmentView;
    private View rowView;
    private RecyclerView recyclerView;

    private FirebaseRecyclerOptions<User> options;
    private FirebaseRecyclerAdapter<User, ViewHolder> adapter;

    private DatabaseReference usersReference, requestReference, contactsReference, optionsSenderRequestReference;
    private FirebaseUser currentUser;

    private String senderUserId;
    private String receiverUserId;
    private List<String> receiverIds;


    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        receiverIds = new ArrayList<>();
        fragmentView =  inflater.inflate(R.layout.fragment_request, container, false);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.requestFragmentRecyclerViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycler_view_line));
        recyclerView.addItemDecoration(dividerItemDecoration);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        requestReference = FirebaseDatabase.getInstance().getReference().child(Constants.FRIEND_REQUESTS);
        optionsSenderRequestReference = requestReference.child(senderUserId);
        contactsReference = FirebaseDatabase.getInstance().getReference().child(Constants.CONTACTS);
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(optionsSenderRequestReference, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull User model) {
                receiverUserId = getRef(position).getKey();
                receiverIds.add(receiverUserId);

                // for populating view
//                holder.getAdapterPosition();

                usersReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists() && snapshot.hasChild(Constants.USER_PROFILE)) {
                            String imageUrl = snapshot.child(Constants.USER_PROFILE).getValue().toString();
                            Picasso.get().load(imageUrl).into(holder.circleImageView);
                        }
                        holder.name.setText(snapshot.child(Constants.USER_NAME).getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // for requests
                optionsSenderRequestReference.child(receiverUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // for visibility of buttons
                        if (snapshot.hasChild("request_type")) {
                            String request_type = snapshot.child("request_type").getValue().toString();
                            if (request_type.equals("sent")) {
                                holder.acceptRequest.setVisibility(View.INVISIBLE);
                                holder.rejectRequest.setVisibility(View.INVISIBLE);
                                holder.cancelRequest.setVisibility(View.VISIBLE);

                            }else {
                                holder.acceptRequest.setVisibility(View.VISIBLE);
                                holder.rejectRequest.setVisibility(View.VISIBLE);
                                holder.cancelRequest.setVisibility(View.INVISIBLE);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.acceptRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int friendListPosition = holder.getAdapterPosition();
                        String friendListReceiverID = receiverIds.get(friendListPosition);
                        receiverIds.remove(friendListPosition);
                        acceptRequest(friendListReceiverID);
                    }
                });

                holder.rejectRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int friendListPosition = holder.getAdapterPosition();
                        String friendListReceiverID = receiverIds.get(friendListPosition);
                        receiverIds.remove(friendListPosition);
                        cancelRequest(friendListReceiverID);
                    }
                });

                holder.cancelRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int friendListPosition = holder.getAdapterPosition();
                        String friendListReceiverID = receiverIds.get(friendListPosition);
                        receiverIds.remove(friendListPosition);
                        cancelRequest(friendListReceiverID);
                    }
                });
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_accept_cancel_row, parent, false);
                return new ViewHolder(rowView);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void acceptRequest(String receiverId) {
        contactsReference.child(senderUserId).child(receiverId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsReference.child(receiverId).child(senderUserId).child("contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                requestReference.child(senderUserId).child(receiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            requestReference.child(receiverId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
//                                                        recyclerView.getAdapter().notifyItemRemoved(position);
//                                                        recyclerView.getAdapter().notifyDataSetChanged();
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


    private void cancelRequest(String receiverId) {
        requestReference.child(senderUserId).child(receiverId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    requestReference.child(receiverId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Friend request rejected", Toast.LENGTH_SHORT).show();
//                                recyclerView.getAdapter().notifyItemRemoved(position);
                            }
                        }
                    });
                }
            }
        });
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView circleImageView;
        private TextView name;
        private Button acceptRequest, rejectRequest, cancelRequest;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.friendAcceptCancelProfileImageViewId);
            name = (TextView) itemView.findViewById(R.id.friendAcceptCancelNameTextViewId);
            acceptRequest = (Button) itemView.findViewById(R.id.friendAcceptCancelCheckButtonId);
            rejectRequest = (Button) itemView.findViewById(R.id.friendAcceptCancelRejectButtonId);
            cancelRequest = (Button) itemView.findViewById(R.id.friendAcceptCancelCButtonId);

        }
    }
}