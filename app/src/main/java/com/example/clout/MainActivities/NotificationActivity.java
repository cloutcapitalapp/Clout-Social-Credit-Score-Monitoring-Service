package com.example.clout.MainActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.clout.R;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    /** This activity will be the notification activity
     * It will be a light weight activity in charge of
     * displaying the users notifications.
     * The notifications that will be displayed are the ...
     * Returned Payments
     * Score Increases or Decreases*/

    ListView notifListView;
    ArrayList<String> fbArrayList;
    ArrayAdapter<String> fbArrayAdapter;


    //Standard ListView will be used
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // We'll need to create the ListView and create entries in a ListView node in firebase
        notifListView = findViewById(R.id.notificationsListView);

        //Set up listView
        fbArrayList = new ArrayList<>();
        fbArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.listview,
                R.id.listviewitem,
                fbArrayList);
    }
}