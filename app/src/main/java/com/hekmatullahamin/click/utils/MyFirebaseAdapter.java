package com.hekmatullahamin.click.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.activities.FriendRequestProfile;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyFirebaseAdapter extends FirebaseRecyclerAdapter<User, MyFirebaseAdapter.ViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private Context context;
    private String currentUserId;

    public MyFirebaseAdapter(@NonNull FirebaseRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void onBindViewHolder(@NonNull MyFirebaseAdapter.ViewHolder holder, int position, @NonNull User user) {
        holder.rowName.setText(user.getName());
        holder.rowAbout.setText(user.getUserAbout());
        Picasso.get().load(user.getUserProfile()).placeholder(R.drawable.default_profile_picture).into(holder.rowProfileImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send us to friend request activity
                Intent friendRequestProfileIntent = new Intent(context, FriendRequestProfile.class);
                Bundle userBundle = new Bundle();
                userBundle.putSerializable(Constants.USER_FIND_FRIEND_BUNDLE, user);
                friendRequestProfileIntent.putExtras(userBundle);
                context.startActivity(friendRequestProfileIntent);
            }
        });
    }

    @NonNull
    @Override
    public MyFirebaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_row, null, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView rowProfileImage;
        private TextView rowName;
        private TextView rowAbout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rowProfileImage = (CircleImageView) itemView.findViewById(R.id.friendsRowProfileImageViewId);
            rowName = (TextView) itemView.findViewById(R.id.friendsRowNameTextViewId);
            rowAbout = (TextView) itemView.findViewById(R.id.friendsRowAboutTextViewId);
        }
    }

}
