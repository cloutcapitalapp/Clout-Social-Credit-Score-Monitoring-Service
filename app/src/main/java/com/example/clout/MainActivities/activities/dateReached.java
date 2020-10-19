package com.example.clout.MainActivities.activities;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.clout.MainActivities.Classes.notificationHandler.CHANNEL_1_ID;

public class dateReached extends Service {

    private NotificationManagerCompat notificationManagerCompat;
    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    DatabaseReference mRef = mDatabaseRef.getReference("Users");
    AccountKeyManager accKey;

    public dateReached() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startID){
        notificationManagerCompat = NotificationManagerCompat.from(this);
        accKey = new AccountKeyManager();

        Log.d("startService",  "Service has begun");
        Notification notification = new NotificationCompat.Builder(dateReached.this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_baseline_emoji_emotions_24)
                .setContentTitle("HEY!")
                .setContentText("Your Score INCREASED by .25 points! Great Job!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        mRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Cash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                notificationManagerCompat.notify(1, notification);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }
}
