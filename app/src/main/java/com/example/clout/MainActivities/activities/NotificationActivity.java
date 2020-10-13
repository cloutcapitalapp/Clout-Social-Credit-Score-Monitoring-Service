package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class NotificationActivity extends AppCompatActivity {

    /** This activity will be the notification activity
     * It will be a light weight activity in charge of
     * displaying the users notifications.
     * The notifications that will be displayed are the ...
     * Returned Payments
     * Score Increases or Decreases*/

    ListView notifListView;
    ArrayAdapter<String> fbArrayAdapter;
    ArrayList<String> fbArrayList = new ArrayList<String>();

    //set up Firebase
    //init firebase vars
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    AccountKeyManager accKey;

    /**This activity will show the users notifications
     * over a listView*/
    //Standard ListView will be used
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        //Call AccountKeyManager
        accKey = new AccountKeyManager();

        //setup firebase START
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users_Notifications");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //END...

        //Init listView
        notifListView = findViewById(R.id.notificationsListView);

        listViewPopulate();
        callListViewData();
    }

    /**Finds Users_Notificiations object and locates mCurrentUser
     * Once the current users object is found in firebase iterate over that users children which will be the
     * users notifications and will be passed into the listView*/
    public void callListViewData(){
        myRef.child(accKey.createAccountKey(currentUser.getEmail())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                    String value = snapshot1.child("Notify").getValue(String.class);
                    Log.d("succeed", "Value is: " + value);
                    fbArrayList.add(value);
                    fbArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("failed", "Failed to read value.", error.toException());
            }
        });
    }

    public void listViewPopulate(){
        fbArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.notiflistview,
                R.id.listviewitem,
                fbArrayList);

        notifListView.setAdapter(fbArrayAdapter);
    }
}