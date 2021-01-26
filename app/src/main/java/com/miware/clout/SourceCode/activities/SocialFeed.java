package com.miware.clout.SourceCode.activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.miware.clout.R;
import com.miware.clout.SourceCode.Classes.AccountKeyManager;
import com.miware.clout.SourceCode.Classes.ScoreHandler;
import com.miware.clout.SourceCode.datamodels.postFeedDataModle;

import de.hdodenhof.circleimageview.CircleImageView;

public class SocialFeed extends AppCompatActivity {

    private StorageReference mStorageRefProfilePic;
    //declare scoreHandler class
    ScoreHandler handleScore;
    //Declare firebase user for current user
    FirebaseUser currentUser;
    //get firebase database instance
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //declare AccountKeyManager as accKey
    AccountKeyManager accKey;
    //We need to declare string to hold the Current users user key
    String caughtUserKey;
    //We'll need to declare a db ref for the feed node TODO : get rid of one without breaking anything
    DatabaseReference postFeedRef, mCrawlFeed;
    //Declare postButton
    private MaterialButton postButton;
    //Declare EditText
    private EditText post_EditText;
    //Declare recyclerView
    private RecyclerView recyclerView;
    DatabaseReference mBase; // Create object of the Firebase Realtime Database
    //Declare firebase recyclerView options
    FirebaseRecyclerOptions<postFeedDataModle> options;
    //Declare firebaseAuth as mAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_feed);

        getSupportActionBar().hide();
        initVars();

        //postButton OnClick Handler
        postButtonOnClickHandler(caughtUserKey);

        //get userKey onCreate
        //getUserKey();

        recViewHander();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }
    /**This view holder is for the Main RecyclerView*/
    private class postsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profilePic;
        TextView getTransacting_Users;
        TextView getUserKey;

        public postsViewHolder(@NonNull View itemView) {
            super(itemView);
            getTransacting_Users = itemView.findViewById(R.id.Transacting_Users);
            getUserKey = itemView.findViewById(R.id.userKey);
            profilePic = itemView.findViewById(R.id.profileImageView);
        }
    }

    public void getUserKey(){
        // Check if user is signed in (non-null) and update UI accordingly.
        String caughtUserKey = accKey.createAccountKey(currentUser.getEmail());
        Snackbar.make(getWindow().getDecorView().getRootView(), "UserKey Down : " + caughtUserKey, Snackbar.LENGTH_SHORT).show();
    }

    public void initVars(){
        //init ScoreHandle class reference
        handleScore = new ScoreHandler();
        //init mCrawFeed database ref
        mCrawlFeed = database.getReference("feed");
        //init post EditText
        post_EditText = findViewById(R.id.post_EditText);
        //Declare auth
        mAuth = FirebaseAuth.getInstance();
        //Declare ref to feed real time database
        postFeedRef = database.getReference("feed").push();
        //declare ref to AccountKeyManager
        accKey = new AccountKeyManager();
        //ini post button
        postButton = findViewById(R.id.postButton);
        // Create a instance of the database and get
        // its reference
        mBase = FirebaseDatabase.getInstance().getReference();
        recyclerView = findViewById(R.id.post_Feed_RecView);
        currentUser = mAuth.getCurrentUser();
    }

    private void recViewHander(){
        //Bind firebase to recycler options
        FirebaseRecyclerOptions<postFeedDataModle> options =
                new FirebaseRecyclerOptions.Builder<postFeedDataModle>()
                        .setQuery(mCrawlFeed, postFeedDataModle.class)
                        .build();

        // Initialize recycler view...
        FirebaseRecyclerAdapter<postFeedDataModle, SocialFeed.postsViewHolder> adapter =
                new FirebaseRecyclerAdapter<postFeedDataModle, postsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull postsViewHolder postsViewHolder, int i,
                                                    @NonNull postFeedDataModle postFeedDataModle) {

                        postsViewHolder.getTransacting_Users.setText(postFeedDataModle.getPostString());
                        postsViewHolder.getUserKey.setText(postFeedDataModle.getUserKey());

                        // Read from the database
                        database.getReference().child("Users").child((String) postsViewHolder.getUserKey.getText()).child("Email").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                String value = snapshot.getValue(String.class);
                                Log.d("check_value", "Value is: " + value);
                                mStorageRefProfilePic = FirebaseStorage.getInstance()
                                        .getReference(
                                                "user/user/" + value + "profilepicture" + "." + "jpg");

                                //fill circle image view with the users image
                                if(mStorageRefProfilePic != null){
                                    mStorageRefProfilePic.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    String uriString = uri.toString();
                                                    Glide.with(SocialFeed.this).load(uriString)
                                                            .into(postsViewHolder.profilePic);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }else{
                                    Glide.with(SocialFeed.this).load(postsViewHolder.profilePic).into(postsViewHolder.profilePic);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public SocialFeed.postsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
                        View view = LayoutInflater
                                .from(viewGroup.getContext())
                                .inflate(R.layout.card_view_list_item, viewGroup,false);
                        return new postsViewHolder(view);
                    }
                };
        recyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter.startListening();
    }

    public void postButtonOnClickHandler(String caughtUserKey){
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!post_EditText.getText().toString().equals("") && post_EditText.getText().toString() != null){
                    String postString = post_EditText.getText().toString();
                    postFeedRef.child("postString").setValue(postString);
                    postFeedRef.child("userKey").setValue(accKey
                            .createAccountKey(currentUser.getEmail()));

                    handleScore.sessionStartScoreIncrease(.02);

                    Snackbar.make(getWindow().getDecorView().getRootView(),
                            "" + "Your score increased by .02!", Snackbar.LENGTH_SHORT).show();

                    finish();
                    startActivity(getIntent());
                }else{
                    //do nothing
                }

            }
        });
    }
}