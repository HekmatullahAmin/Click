package com.hekmatullahamin.click.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hekmatullahamin.click.R;
import com.hekmatullahamin.click.utils.Constants;
import com.hekmatullahamin.click.utils.Methods;
import com.hekmatullahamin.click.utils.TabAccessAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Toolbar toolbar;
    private TabAccessAdapter pagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private DatabaseReference usersReference;
    private String currentUserId;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fieldsInitialization();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Click");
        pagerAdapter = new TabAccessAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

    }

    private void fieldsInitialization() {

        auth = FirebaseAuth.getInstance();

        toolbar = (Toolbar) findViewById(R.id.mainActivityActionBarId);
        viewPager = (ViewPager) findViewById(R.id.mainActivityViewPagerId);
        tabLayout = (TabLayout) findViewById(R.id.mainActivityTabLayoutId);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.mainMenuFindFriendsId:
                sendUserToFindFriendActivity();
                break;
            case R.id.mainMenuSettingsId:
                sendUserToSettingsActivity();
                break;
            case R.id.mainMenuSignOutId:
                Methods.updateUserStatus(MainActivity.this, usersReference, "offline", currentUserId);
                auth.signOut();
                sendUserToLoginActivity();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            sendUserToLoginActivity();
        }else {
            Methods.updateUserStatus(MainActivity.this, usersReference, "online", currentUserId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser != null) {
            Methods.updateUserStatus(MainActivity.this, usersReference, "offline", currentUserId);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser != null) {
            Methods.updateUserStatus(MainActivity.this, usersReference, "offline", currentUserId);
        }
    }

    private void sendUserToFindFriendActivity() {
        Intent findFriendActivityIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendActivityIntent);
    }

    private void sendUserToLoginActivity() {
        Intent loginActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void sendUserToSettingsActivity() {
        Intent settingsActivityIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsActivityIntent);
    }


}