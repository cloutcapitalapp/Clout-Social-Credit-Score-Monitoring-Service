package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class userprfileactivity extends AppCompatActivity {

    Button notificationsButton;
    ListView listView;
    ArrayAdapter adapter;
    ArrayList<String> arrayList;
    AddFriendHandler addFriend;
    private StorageReference mStorageRefPfofPic;
    ProgressBar progress;
    TextView accountKey;
    CircleImageView profilePic;
    Button addedButton, update;
    AccountKeyManager accKey;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersFrieldKListRef = database.getReference("Users_Friends_List");
    DatabaseReference usersRef = database.getReference("Users");
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    Uri mImageUri; /* Image URI is for Firebase Image Storage */
    private StorageReference mStorageRef;
    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprfileactivity);

        notificationsButton = findViewById(R.id.notifButton);
        addFriend = new AddFriendHandler(userprfileactivity.this);
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

        progress.setVisibility(View.INVISIBLE);
        //updateUserName();
        addFriendsHandler();
        usernameRec();
        toFetchImage();
    }
    @Override
    protected void onStart(){
        super.onStart();
        onStartGrabImage();

        populateListView();
    }

    //onClick set on button XML
    public void notificationsButtonOnClick(View v){
        Intent goToNotifActivity = new Intent(userprfileactivity.this, NotificationActivity.class);
        startActivity(goToNotifActivity);
    }

    public void populateListView(){
        usersFrieldKListRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("forLoopStart", "Start");
                Log.d("forLoopStart", "start" + " : " + snapshot);

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    String value = snapshot1.getKey();
                    Log.d("logValue", "" + value);
                    arrayList.add(value);
                    adapter.notifyDataSetChanged();
                }
                Log.d("forLoopEnd", "End");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*** The below method will add friends to the users account
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
        final AlertDialog confirm = new MaterialAlertDialogBuilder(userprfileactivity.this).create();
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

        EditText friendsName = new EditText(userprfileactivity.this);
        friendsName.setGravity(Gravity.CENTER);
        friendsName.setHint("&AccountName");
        friendsName.setBackground(getDrawable(R.drawable.roundededittext));
        MaterialButton submit = new MaterialButton(userprfileactivity.this);
        MaterialButton cancel = new MaterialButton(userprfileactivity.this);
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
                    Toast.makeText(userprfileactivity.this, "Please make sure " +
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

    /*** We will now create an confrimation dialoge that asks the currentUser if the user the've found
     * is the user they were looking for and a prompt will be displayed for them to add the user to their
     * friends list ... ***/
    public void confirmationAlert(/*Searched users account name*/String usersName,
            /*Searched users account name*/String usersCloutScore, String usersEmail){

        final AlertDialog confirm = new MaterialAlertDialogBuilder(userprfileactivity.this).create();
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

        MaterialButton submit = new MaterialButton(userprfileactivity.this);
        MaterialButton cancel = new MaterialButton(userprfileactivity.this);
        TextView usersNameTV = new MaterialButton(userprfileactivity.this);
        CircleImageView circleImageView = new CircleImageView(userprfileactivity.this);
        circleImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_24));
        /*** Image Grab START ***/
        StorageReference mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + usersEmail + "profilepicture" + "." + "jpg");
        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(userprfileactivity.this).load(uriString).into(circleImageView);
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

            Glide.with(userprfileactivity.this).load(circleImageView).into(circleImageView);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
        /*** Image Grab END ***/
        TextView usersCloutScoreTV = new MaterialButton(userprfileactivity.this);
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

                /** A check will need to be built out to make sure the users hasn't alrady
                 * been added */
                addFriend.AddFriend(usersName);
                confirm.dismiss();
                Toast.makeText(userprfileactivity.this, usersName + " has been added to your friends list", Toast.LENGTH_SHORT).show();
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

                            Toast.makeText(userprfileactivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            Intent reload = new Intent(userprfileactivity.this, userprfileactivity.class);
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

    // ported to errands Check Done*
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
        mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + mCurrentUser.getEmail() + "profilepicture" + "." + "jpg");

        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(userprfileactivity.this).load(uriString).into(profilePic);
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

            Glide.with(userprfileactivity.this).load(profilePic).into(profilePic);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
    }
}