package com.hekmatullahamin.click.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Methods;
import com.hekmatullahamin.click.utils.MyFirebaseAdapter;
import com.hekmatullahamin.click.utils.User;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private FirebaseRecyclerOptions<User> options;
    private DatabaseReference usersReference;
    private MyFirebaseAdapter adapter;

    private String senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        fieldsInitialization();

        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        recyclerView.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));
        recyclerView.setHasFixedSize(true);
    }

    private void fieldsInitialization() {
        recyclerView = (RecyclerView) findViewById(R.id.findFriendsActivityRecyclerViewId);
        toolbar = (Toolbar) findViewById(R.id.findFriendsActivityActionBarId);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (senderUserId != null) {
            Methods.updateUserStatus(FindFriendsActivity.this, usersReference, "online", senderUserId);
        }

        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersReference, User.class)
                .build();
        adapter = new MyFirebaseAdapter(options, FindFriendsActivity.this);
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(FindFriendsActivity.this,
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recycler_view_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senderUserId != null) {
            Methods.updateUserStatus(FindFriendsActivity.this, usersReference, "offline", senderUserId);
        }
    }
}