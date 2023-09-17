package com.hekmatullahamin.click.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Methods {

    public static void updateUserStatus(Context context, DatabaseReference usersReference, String state, String currentUser) {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
        String messageDate = dateFormatter.format(date);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("E, HH:mm");
        String messageTime = timeFormatter.format(date);

        Map<String, Object> statusHashMap = new HashMap<>();
        statusHashMap.put("state", state);
        statusHashMap.put("date", messageDate);
        statusHashMap.put("time", messageTime);
        usersReference.child(currentUser).child("userStatus").updateChildren(statusHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
//                    Toast.makeText(context, "User is " + state, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
