package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

    //firebase database referance
    DatabaseReference pendingNotifications, mEventReceivedTransactionList;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    //TabLayout
    TabLayout tabLayout;

    //ListView
    ListView notifListView, pendingDeedListView;
    ArrayAdapter<String> fbArrayAdapter, pendingAdapter;
    ArrayList<String> pendingArrayList = new ArrayList<String>();
    ArrayList<String> fbArrayList = new ArrayList<String>();

    //set up Firebase
    //init firebase vars
    FirebaseDatabase database;
    DatabaseReference myRef, pendingRef;
    FirebaseUser currentUser;
    AccountKeyManager accKey;

    /**This activity will show the users notifications
     * over a listView*/
    //Standard ListView will be used
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //tabLayout
        tabLayout = findViewById(R.id.tabLayout);

        //Call AccountKeyManager
        accKey = new AccountKeyManager();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //END...

        //Init listView
        notifListView = findViewById(R.id.notificationsListView);
        pendingDeedListView = findViewById(R.id.pendingDeedListView);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        assert mCurrentUser != null;

        //setup firebase START
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users_Notifications");
        pendingRef = database.getReference(accKey.createAccountKey(mCurrentUser.getEmail()) + "_" + "Pending_Notifications");

        listViewManager();
        callNotificiationsData();
        callPendingData();
        tabLayoutManager();
        callPendingDataItemListener();
    }

    /**TabLayout - tab layout will show notifications and pending transaction listviews*/
    public void tabLayoutManager(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // called when tab selected
                int position = tab.getPosition();
                // Log.d("tabPosition: ", "" + position); /* Log.d test is shown to work index is shown correctly */

                //TODO: if signup tab { index 0 } is selected, all elements should be visible
                //TODO: if login tab { index 1 } is selected, only username and password should be visible

                if(position == /* check for index 1 which is the login tab */ 1){
                    notifListView.setVisibility(View.INVISIBLE);
                    pendingDeedListView.setVisibility(View.VISIBLE);
                }else /* if else, then index 0 is active | index 0 is the sign up tab, and all elements will be available when index 0 is active */{
                    notifListView.setVisibility(View.VISIBLE);
                    pendingDeedListView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**Finds Pending_Notifications object and locates mCurrentUser
     * Once the current users object is found in firebase iterate over that users children which will be the
     * users notifications and will be passed into the listView*/
    public void callPendingData(){
        pendingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                    String desc = snapshot1.child("description").getValue(String.class);
                    String end = snapshot1.child("enddate").getValue(String.class);
                    String from = snapshot1.child("sentFrom").getValue(String.class);

                    String allData = desc + "\n" + end + "\n" + from;

                    //Log.d("succeed", "Value is: " + allData);
                    pendingArrayList.add(allData);
                    pendingAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("failed", "Failed to read value.", error.toException());
            }
        });
    }

    /**CallPendingData listView item listener*/
    public void callPendingDataItemListener(){

        pendingDeedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = pendingAdapter.getItem(position);
                String findUsername = item.substring(item.indexOf("\n&")+1);
                Toast.makeText(NotificationActivity.this, "" + findUsername, Toast.LENGTH_SHORT).show();
                pendingAcceptAlert(findUsername, item);
            }
        });
    }
    /**Alert the user and ask if they'd like to approve the deed-transaction*/
    public void pendingAcceptAlert(String fromUser, String itemText){
        final AlertDialog pendingAlert = new MaterialAlertDialogBuilder(NotificationActivity.this).create();

        LinearLayout layout = new LinearLayout(NotificationActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        pendingAlert.setView(layout);

        pendingAlert.setCancelable(false);

        pendingAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = pendingAlert.getWindow();
        window.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        pendingAlert.setTitle("Will you accept?");
        pendingAlert.setMessage("Will you accept this deed transactions from " + fromUser + "?");

        MaterialButton confirmButton = new MaterialButton(NotificationActivity.this);
        MaterialButton cancelButton = new MaterialButton(NotificationActivity.this);

        cancelButton.setText(R.string.cancel);
        cancelButton.setBackgroundResource(R.color.colorPrimary);

        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);

        layout.addView(confirmButton);
        layout.addView(cancelButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventReceivedTransactionList = database.getReference(accKey.createAccountKey(mCurrentUser.getEmail()) + "_" + "event_received").push();
                mEventReceivedTransactionList.child("accepted_event").setValue(itemText);

                Query applesQuery = pendingRef.orderByChild("sentFrom").equalTo(fromUser);
                Log.d("querycheck", "" + applesQuery);

                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("failed", "onCancelled", databaseError.toException());
                    }
                });

                pendingAdapter.notifyDataSetChanged();
                pendingAlert.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingAlert.dismiss();
            }
        });
        pendingAlert.show();
    }

    /**Finds Users_Notificiations object and locates mCurrentUser
     * Once the current users object is found in firebase iterate over that users children which will be the
     * users notifications and will be passed into the listView*/
    public void callNotificiationsData(){
        myRef.child(accKey.createAccountKey(currentUser.getEmail())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                    String value = snapshot1.child("notify").getValue(String.class);
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

    /**callNotificationsData item Listener*/
    public void callNotificationsDataItemListener(){
        notifListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    /**houses init code for the both listViews*/
    public void listViewManager(){
        /**ListView adapter for general notifications*/
        fbArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.notiflistview,
                R.id.listviewitem,
                fbArrayList);

        notifListView.setAdapter(fbArrayAdapter);

        /**ListView adapter for pendingTransactions notifications*/
        pendingAdapter = new ArrayAdapter<String>(this,
                R.layout.notiflistview,
                R.id.listviewitem,
                pendingArrayList);

        pendingDeedListView.setAdapter(pendingAdapter);
    }
}