package com.miware.clout.SourceCode.activities;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.miware.clout.R;
import com.miware.clout.SourceCode.adapters.FeedPostRecyclerAdapter;
import com.miware.clout.SourceCode.datamodels.postFeedDataModle;

public class SocialFeed extends AppCompatActivity {
    private RecyclerView recyclerView;
    FeedPostRecyclerAdapter adapter; // Create Object of the Adapter class
    DatabaseReference mbase; // Create object of the Firebase Realtime Database
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_feed);

        getSupportActionBar().hide();

        // Create a instance of the database and get
        // its reference
        mbase = FirebaseDatabase.getInstance().getReference();

        // To display the Recycler view linearly
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        // It is a class provide by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<postFeedDataModle> options
                = new FirebaseRecyclerOptions.Builder<postFeedDataModle>()
                .setQuery(mbase, postFeedDataModle.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new FeedPostRecyclerAdapter(options);
        // Connecting Adapter class with the Recycler view*/
        recyclerView.setAdapter(adapter);
    }
}