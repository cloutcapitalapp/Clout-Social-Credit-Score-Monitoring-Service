package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.MainActivities.Classes.AddFriendHandler;
import com.example.clout.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    //Declare general vars START
    private StorageReference mStorageRefProfilePic;
    Button addedButton, update, notificationsButton, withdrawButton;
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> arrayList;
    AddFriendHandler addFriend;
    ProgressBar progress;
    TextView accountKey, addedTV;
    CircleImageView profilePic;
    AccountKeyManager accKey;
    //END...

    //Declare firebase vars START
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userFriendsList, usersRef;
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    //END...

    //prepare image for selection and placement vars START
    Uri mImageUri; /* Image URI is for Firebase Image Storage */
    private StorageReference mStorageRef;
    public static final int PICK_IMAGE = 1;
    private Handler myHandler;
    //END...

    /** onCreate should house initialized variables as well as methods, but no direct code
     * However - There is a single line of code as follows
     *         progress.setVisibility(View.INVISIBLE);
     * I don't see value in placing this one line of code into its' own method*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprfileactivity);

        withdrawButton = findViewById(R.id.withDrawButton);
        userFriendsList = database.getReference("User_Friends_List");
        usersRef = database.getReference("Users");
        addedTV = findViewById(R.id.addedTextView);
        notificationsButton = findViewById(R.id.notifButton);
        addFriend = new AddFriendHandler(UserProfileActivity.this);
        arrayList = new ArrayList<String>();
        progress = findViewById(R.id.progressBar);
        mStorageRef = FirebaseStorage.getInstance().getReference("user/");
        accountKey = findViewById(R.id.accountKeyTitle);
        profilePic = findViewById(R.id.profile_image);
        addedButton = findViewById(R.id.addedButton);
        accKey = new AccountKeyManager();
        update = findViewById(R.id.submitButton);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this,R.layout.listview, R.id.listviewitem, arrayList);
        listView.setAdapter(adapter);

        //progressbar setVisibility to invisible.
        //this is of course changed when the user uploads an image.
        progress.setVisibility(View.INVISIBLE);

        //updateUserName();
        listViewItemSelect();
        addFriendsHandler();
        usernameRec();
        toFetchImage();
        populateListView();
        withdrawOnClick();
    }

    /**onStart will populate the listView withe the 'Users_Friend_List' data from frirebase
     * */
    @Override
    protected void onStart(){
        super.onStart();
        onStartGrabImage();
    }

    /**When the notifications button is tapped
     * An Intent will be created and the user will be sent to the NotificationActivity*/
    public void notificationsButtonOnClick(View v){
        Intent goToNotifActivity = new Intent(UserProfileActivity.this, NotificationActivity.class);
        startActivity(goToNotifActivity);
    }

    /**This method will populate the listView with data from the firebase 'User_Friends_List object'
     * */
    public void populateListView(){
        Log.d("forLoopEnd", "End");
        if(!arrayList.contains(null)){
            myHandler = new Handler();
            addedTV.setText("Friends List");

            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addedTV.setVisibility(View.INVISIBLE);
                    ObjectAnimator tTextViewAnimation = ObjectAnimator.ofFloat(listView, "translationY", -70f);
                    tTextViewAnimation.setDuration(1000);
                    tTextViewAnimation.start();
                }
            }, 3000);
        }
        userFriendsList.child(accKey.createAccountKey(mCurrentUser.getEmail())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("forLoopStart", "Start");
                //Log.d("forLoopStart", "start" + " : " + snapshot);

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    String value = snapshot1.getKey();
                    //Log.d("logValue", "" + value);
                    arrayList.add(value);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    
    /**This mthod will allow the listView onClick functionality
     * When an item is clicked it will deliver some information about the user.
     * profile picture, username and score
     * @param // TODO: 10/13/20 is up next for completion */
    public void listViewItemSelect(){
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String userName = String.valueOf(listView.getItemAtPosition(position));
            listViewOnClickItemAlert(userName);
        });
    }
    /**This alert should confirm the user selected will be sent a transaction request
     * @// TODO: 10/18/20 This method takes code from addFriend, it must be changed to suit its new purpose
     * @// TODO: 10/18/20 Its' new purpose will be to submit a transaction request */
    public void listViewOnClickItemAlert(String userName){
        Toast.makeText(this, "" + userName, Toast.LENGTH_SHORT).show();
        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(130, 0, 130, 30);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Start Transaction Request?");
        confirm.setMessage("Make sure this is the correct username.");

        EditText friendsName = new EditText(UserProfileActivity.this);
        friendsName.setGravity(Gravity.CENTER);
        friendsName.setText(userName);
        friendsName.setHint("&AccountName");
        friendsName.setBackground(getDrawable(R.drawable.roundededittext));
        MaterialButton submit = new MaterialButton(UserProfileActivity.this);
        MaterialButton cancel = new MaterialButton(UserProfileActivity.this);
        submit.setText(R.string.submit);
        submit.setBackgroundResource(R.color.colorPrimary);
        cancel.setText(R.string.cancel);
        cancel.setBackgroundResource(R.color.colorPrimary);
        layout.addView(friendsName, editTextParams);
        layout.addView(submit, editTextParams);
        layout.addView(cancel);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* When the users touches the submit button the firebase needs to be checked for
                 * said user in the users list, then a dialog needs to pop up showing the user in question
                 * and confirming the user is the correct user that needs to be added. A small amount of the
                 * users information will be displayed - Profile image, clout score, -- bio when they are added in --, and user name
                 *
                 * The data will display as follows
                 * 1 - Account Key/Name
                 * 2 - Profile image
                 * 3 - Clout Score
                 * 4 - Bio -- When added*/

                /*** the friendsName var will be checked against all users to ensure the user is present in the
                 * system. if the user is present they will be added. In the second version of the app, this will change
                 * to a notification system - where the users will verify that they wish to be added. Instead, the user
                 * will be notified when they are added.
                 *
                 * I find this acceptable for now because the users will have to give out there user names in order to
                 * be added because there is no search bar for names.
                 *
                 * I believe this is secure enough for the moment to avoid hacks or manipulations of any kind
                 *
                 * We will start with an if conditional which makes sure the username is entered
                 * correctly, then, if-so the system will procede with the check.
                 * ***/
                if(friendsName.getText().toString().contains("&")){
                    /*if the entered friendsName contains the "&" symbol it is passed to be searched*/
                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                String usersName = snapshot1.getKey();
                                String usersCloutScore = snapshot1.child("Score").getValue(String.class);
                                String usersEmail = snapshot1.child("Email").getValue(String.class);
                                // this log checks which usernames are being snapshot -- Log.d("userNameCheck", "" + usersName);

                                /* This if conditional will now check the username entered against the usernames in the firebase DB
                                 *
                                 *  */
                                if(friendsName.getText().toString().equals(usersName)){
                                    /* Log.d("userNameCompareCheck", "Got a match from the database" + " : " + usersName);
                                     *
                                     * This above log will let us know we found a match*/

                                    /* Now that we know the username can be indexed
                                     * we can report to the currentUser and confirm that they have
                                     * found the user they were looking for
                                     *  */

                                    // TODO BELOW
                                    // The confirmation alert will go here ...
                                    confirmationAlert(usersName, usersCloutScore, usersEmail);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    /*if the entered friendsName does NOT contain the "&" symbol it is not passed to be
                     * searched*/
                    Toast.makeText(UserProfileActivity.this, "Please make sure " +
                            "you've entered the \"&\" symbol before the users account name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    }

    /*** The below methods will add friends to the users account
     * 1 - Select a user by typing the account key/name
     * 2 - Once the user has been found, create db table for users and their friends
     * 3 - funnel the friends list users to the current users friend list Recycler View ***/
    private void addFriendsHandler(){
        addedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriendsHandlerAlert();
            }
        });
    }
    public void addFriendsHandlerAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(130, 0, 130, 30);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Add A Friend!");
        confirm.setMessage("Please type a users account name to add that user to your friends list. " +
                "Also Please make sure to add the \"&\" symbol before the account name.");

        EditText friendsName = new EditText(UserProfileActivity.this);
        friendsName.setGravity(Gravity.CENTER);
        friendsName.setHint("&AccountName");
        friendsName.setBackground(getDrawable(R.drawable.roundededittext));
        MaterialButton submit = new MaterialButton(UserProfileActivity.this);
        MaterialButton cancel = new MaterialButton(UserProfileActivity.this);
        submit.setText(R.string.submit);
        submit.setBackgroundResource(R.color.colorPrimary);
        cancel.setText(R.string.cancel);
        cancel.setBackgroundResource(R.color.colorPrimary);
        layout.addView(friendsName, editTextParams);
        layout.addView(submit, editTextParams);
        layout.addView(cancel);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* When the users touches the submit button the firebase needs to be checked for
                * said user in the users list, then a dialog needs to pop up showing the user in question
                * and confirming the user is the correct user that needs to be added. A small amount of the
                * users information will be displayed - Profile image, clout score, -- bio when they are added in --, and user name
                *
                * The data will display as follows
                * 1 - Account Key/Name
                * 2 - Profile image
                * 3 - Clout Score
                * 4 - Bio -- When added*/

                /*** the friendsName var will be checked against all users to ensure the user is present in the
                 * system. if the user is present they will be added. In the second version of the app, this will change
                 * to a notification system - where the users will verify that they wish to be added. Instead, the user
                 * will be notified when they are added.
                 *
                 * I find this acceptable for now because the users will have to give out there user names in order to
                 * be added because there is no search bar for names.
                 *
                 * I believe this is secure enough for the moment to avoid hacks or manipulations of any kind
                 *
                 * We will start with an if conditional which makes sure the username is entered
                 * correctly, then, if-so the system will procede with the check.
                 * ***/
                if(friendsName.getText().toString().contains("&")){
                    /*if the entered friendsName contains the "&" symbol it is passed to be searched*/
                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                String usersName = snapshot1.getKey();
                                String usersCloutScore = snapshot1.child("Score").getValue(String.class);
                                String usersEmail = snapshot1.child("Email").getValue(String.class);
                                // this log checks which usernames are being snapshot -- Log.d("userNameCheck", "" + usersName);

                                /* This if conditional will now check the username entered against the usernames in the firebase DB
                                *
                                *  */
                                if(friendsName.getText().toString().equals(usersName)){
                                    /* Log.d("userNameCompareCheck", "Got a match from the database" + " : " + usersName);
                                    *
                                    * This above log will let us know we found a match*/

                                    /* Now that we know the username can be indexed
                                    * we can report to the currentUser and confirm that they have
                                    * found the user they were looking for
                                    *  */

                                    // TODO BELOW
                                    // The confirmation alert will go here ...
                                    confirmationFriendAlert(usersName, usersCloutScore, usersEmail);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else{
                    /*if the entered friendsName does NOT contain the "&" symbol it is not passed to be
                    * searched*/
                    Toast.makeText(UserProfileActivity.this, "Please make sure " +
                            "you've entered the \"&\" symbol before the users account name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    } /*TODO*/
    /**END*/

    /**Widthdraw Button is tapped*/
    public void withdrawOnClick(){
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentsNotReadyAlert();
            }
        });
    }

    /**This method will alert the user that payments are not yet ready*/
    private void paymentsNotReadyAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_mood_bad_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("NOT YET!");
        confirm.setMessage("Payments are not ready yet. Stick around, they'll be ready soon.");

        MaterialButton confirmButton = new MaterialButton(UserProfileActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    }

    /*** We will now create an confirmation dialog that asks the currentUser if the user the've found
     * is the user they were looking for and a prompt will be displayed for them to add the user to their
     * friends list ... ***/
    public void confirmationFriendAlert(/*Searched users account name*/String usersName,
            /*Searched users account name*/String usersCloutScore, String usersEmail){

        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(130, 0, 130, 30);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Is this who you're looking for?");

        /*** We'll need to add in a function that grabs the searched usersprofile image, but
         * if there isn't one then we'll just use the profile image drawable. ***/

        MaterialButton submit = new MaterialButton(UserProfileActivity.this);
        MaterialButton cancel = new MaterialButton(UserProfileActivity.this);
        TextView usersNameTV = new MaterialButton(UserProfileActivity.this);
        CircleImageView circleImageView = new CircleImageView(UserProfileActivity.this);
        circleImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_24));
        /*** Image Grab START ***/
        StorageReference mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + usersEmail + "profilepicture" + "." + "jpg");
        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(UserProfileActivity.this).load(uriString).into(circleImageView);
                    //Log.d("load image", "" + uriString);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else{
            //Log.d("ReportImageTarget", "\n " +
            //"\n " +
            //"\n " +
            //"onStart: NOTHING " +
            //"\n " +
            //"\n");

            Glide.with(UserProfileActivity.this).load(circleImageView).into(circleImageView);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
        /*** Image Grab END ***/
        TextView usersCloutScoreTV = new MaterialButton(UserProfileActivity.this);
        usersNameTV.setText(usersName);
        usersCloutScoreTV.setText(usersCloutScore);
        submit.setText(R.string.yes);
        submit.setBackgroundResource(R.color.colorPrimary);
        cancel.setText(R.string.cancel);
        cancel.setBackgroundResource(R.color.colorPrimary);

        layout.addView(circleImageView, editTextParams);
        layout.addView(usersNameTV);
        layout.addView(usersCloutScoreTV);
        layout.addView(submit, editTextParams);
        layout.addView(cancel);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend.AddFriend(usersName);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.create();
        confirm.show();
    }
    public void confirmationAlert(/*Searched users account name*/String usersName,
            /*Searched users account name*/String usersCloutScore, String usersEmail){

        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextParams.setMargins(130, 0, 130, 30);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Is this who you're looking for?");

        /*** We'll need to add in a function that grabs the searched usersprofile image, but
         * if there isn't one then we'll just use the profile image drawable. ***/

        MaterialButton submit = new MaterialButton(UserProfileActivity.this);
        MaterialButton cancel = new MaterialButton(UserProfileActivity.this);
        TextView usersNameTV = new MaterialButton(UserProfileActivity.this);
        CircleImageView circleImageView = new CircleImageView(UserProfileActivity.this);
        circleImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_24));
        /*** Image Grab START ***/
        StorageReference mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + usersEmail + "profilepicture" + "." + "jpg");
        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(UserProfileActivity.this).load(uriString).into(circleImageView);
                    //Log.d("load image", "" + uriString);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else{
            //Log.d("ReportImageTarget", "\n " +
            //"\n " +
            //"\n " +
            //"onStart: NOTHING " +
            //"\n " +
            //"\n");

            Glide.with(UserProfileActivity.this).load(circleImageView).into(circleImageView);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
        /*** Image Grab END ***/
        TextView usersCloutScoreTV = new MaterialButton(UserProfileActivity.this);
        usersNameTV.setText(usersName);
        usersCloutScoreTV.setText(usersCloutScore);
        submit.setText(R.string.yes);
        submit.setBackgroundResource(R.color.colorPrimary);
        cancel.setText(R.string.cancel);
        cancel.setBackgroundResource(R.color.colorPrimary);

        layout.addView(circleImageView, editTextParams);
        layout.addView(usersNameTV);
        layout.addView(usersCloutScoreTV);
        layout.addView(submit, editTextParams);
        layout.addView(cancel);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The current user must be sent to the transaction activity with the username set in the
                // username field to the to-users' username.
                Intent toTransactionActiviy = new Intent(UserProfileActivity.this, EventTransactionActivity.class);
                toTransactionActiviy.putExtra("username", usersNameTV.getText().toString());
                startActivity(toTransactionActiviy);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.create();
        confirm.show();
    }

    /**This method will retrieve the users accountKey/Username [which is a Key node from 'Users']
     * from firebase('Users') and will
     * replace the top-most textView with the retrieved string*/
    public void usernameRec (){
        usersRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                accountKey.setText(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**This method changes the current users username
     * Changing the username may cause unwanted side effects at the moment so for-now it will
     * be removed until further notice*/
    /*public void updateUserName(){
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //This for-loop needs to check the entered value against the users in the database for accountkeys
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            String snappedEmail = (String) snapshot1.child("Email").getValue();
                            String accKeySanpped = accKey.createAccountKey(snappedEmail);
                            // Check if the entered username has been used before
                            if(editTextName.getText().toString().equals(accKeySanpped)){
                                Toast.makeText(userprfileactivity.this, "This username is not available", Toast.LENGTH_LONG).show();
                                //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + borrowerET.getEditText().getText());
                                break;
                            }else{
                                // The username is not taken and can be updated
                                Toast.makeText(userprfileactivity.this, "UPDATING...", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }*/
    /**END*/

    /**The following blocks of code will allow the user to select an image from their gallery
     * and will place the image inside a circle imageView with the Glide library START*/
    /* When an image is selected from the gallery return result */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            //TODO: action
            mImageUri = data.getData();

            uploadFile();

        }
    }
    /* When image file is selected master the extension of the file for sending to DB */
    private String getFileExtention (Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    /* When image is selected and extension is grabbed, send to storage */
    private void uploadFile(){
        if (mImageUri != null) {
            //getFileExtention(mImageUri)
            StorageReference fileReference = mStorageRef.child("user/" + mCurrentUser.getEmail() + "profilepicture" + "." + getFileExtention(mImageUri));
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setProgress(0);
                                }
                            }, 500);

                            progress.setVisibility(View.INVISIBLE);

                            Toast.makeText(UserProfileActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            Intent reload = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                            startActivity(reload);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    Double progressVal = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progress.setVisibility(View.VISIBLE);
                    progress.setProgress(progressVal.intValue());

                }
            });

        }else{

            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();

        }
    }
    // Ported to Clouts Check Done*
    /* When C.ImgView is tapped, take user to users photo Gallery */
    private void toFetchImage(){
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }
    /* When onStart 'LifeCycle' begins check if there is a user profile image, if so place in C.ImgView */
    private void onStartGrabImage(){
        mStorageRefProfilePic = FirebaseStorage.getInstance().getReference("user/user/" + mCurrentUser.getEmail() + "profilepicture" + "." + "jpg");

        if(mStorageRefProfilePic != null){
            mStorageRefProfilePic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(UserProfileActivity.this).load(uriString).into(profilePic);
                    //Log.d("load image", "" + uriString);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else{
            //Log.d("ReportImageTarget", "\n " +
            //"\n " +
            //"\n " +
            //"onStart: NOTHING " +
            //"\n " +
            //"\n");

            Glide.with(UserProfileActivity.this).load(profilePic).into(profilePic);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
    }
    /**END*/
}