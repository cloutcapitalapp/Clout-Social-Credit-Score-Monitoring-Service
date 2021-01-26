package com.miware.clout.SourceCode.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.miware.clout.SourceCode.Classes.AccountKeyManager;
import com.miware.clout.SourceCode.Classes.AddFriendHandler;
import com.miware.clout.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.miware.clout.SourceCode.Classes.ScoreHandler;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle mToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navView, navigationView;
    AdView adview;
    TextView emptyTextView;

    //Declare general vars START
    private StorageReference mStorageRefProfilePic;
    Button update;
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
    DatabaseReference userFriendsList, usersRef, friendRef;
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

        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'> CLOUT </font>"));

        navigationView = findViewById(R.id.nav_view);
        navView = findViewById(R.id.nav_view);
        adview = findViewById(R.id.adView);
        emptyTextView = findViewById(R.id.empty);
        userFriendsList = database.getReference("User_Friends_List");
        usersRef = database.getReference("Users");
        friendRef = database.getReference("User_Friends_List");
        addedTV = findViewById(R.id.addedTextView);
        addFriend = new AddFriendHandler(UserProfileActivity.this);
        arrayList = new ArrayList<String>();
        progress = findViewById(R.id.progressBar);
        mStorageRef = FirebaseStorage.getInstance().getReference("user/");
        accountKey = findViewById(R.id.accountKeyTitle);
        profilePic = findViewById(R.id.profile_image);
        accKey = new AccountKeyManager();
        update = findViewById(R.id.submitButton);
        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this,R.layout.listview, R.id.listviewitem, arrayList);
        listView.setAdapter(adapter);

        //progressbar setVisibility to invisible.
        //this is of course changed when the user uploads an image.
        progress.setVisibility(View.INVISIBLE);

        listView.setEmptyView(emptyTextView);
        //updateUserName();
        listViewItemSelect();
        usernameRec();
        toFetchImage();
        populateListView();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);

        setNavigationViewListener();
    }
    private void setNavigationViewListener() {
        drawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(true);
        drawerLayout.closeDrawers();
        if (id == R.id.watch) {
            addFriendsHandlerAlert();
        }else if(id == R.id.invite){
            String caughtAccountKet = accountKey.getText().toString();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBody = "Hey! I use this app called clout. It helps users let people know " +
                    "when the people around them are trust worthy.\nDownload here to get started - " +
                    "https://cutt.ly/bh5aVEt";
            String shareSub = "Your Clout Score!";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Share to : "));
        }else if(id == R.id.settings){
            Intent passToSettings = new Intent(UserProfileActivity.this, SettingsActivity.class);
            startActivity(passToSettings);
        }else if(id == R.id.notifications){
            Intent passToNotifications = new Intent(UserProfileActivity.this, NotificationActivity.class);
            startActivity(passToNotifications);
        }else if(id == R.id.loutout){
            signoutAlert();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        Intent returnToMain = new Intent(UserProfileActivity.this, MainActivity.class);
        startActivity(returnToMain);
    }

    /**onStart will populate the listView withe the 'Users_Friend_List' data from frirebase
     * */
    @Override
    protected void onStart(){
        super.onStart();
        onStartGrabImage();
    }
    /**signoutAlert will ask the user if they want to signout. Assuming 'yes' the users firebase instance
     * will be signed out and the user will be returned to the AuthActivity*/
    public void signoutAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_warning_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("SignOut?");
        confirm.setMessage("Are you ready to signout?");

        MaterialButton button = new MaterialButton(UserProfileActivity.this);
        MaterialButton cancel = new MaterialButton(UserProfileActivity.this);

        cancel.setText(R.string.cancel);
        button.setText(R.string.confirm);

        cancel.setBackgroundResource(R.color.colorPrimary);
        button.setBackgroundResource(R.color.colorPrimary);

        layout.addView(button);
        layout.addView(cancel);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();

                FirebaseAuth.getInstance().signOut();
                Intent toAuth = new Intent(UserProfileActivity.this, AuthActivity.class);
                startActivity(toAuth);
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

    /**This method will populate the listView with data from the firebase 'User_Friends_List object'
     * */
    public void populateListView(){
        //Log.d("forLoopEnd", "End");
        if(!arrayList.contains(null)){
            myHandler = new Handler();
            addedTV.setText("Friends List");

            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addedTV.setVisibility(View.INVISIBLE);
                    ObjectAnimator tTextViewAnimation = ObjectAnimator.ofFloat(listView, "translationY", -40f);
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
            /**If the user has a follow target that is equal to mCurrentUser, then the user should
             * be blocked from following - as it's pointless for the users to add themselves to their
             * follow list.*/
            if(userName.trim().equals(accKey.createAccountKey(mCurrentUser.getEmail()))){
                //Alert the user that they can't send transactions to themselves.
                //Toast.makeText(NonFundedMoneyTransaction.this, "Test 1", Toast.LENGTH_SHORT).show();
                checkForSelfAlert();
            }else{
                //
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String usersCloutScore = snapshot.child(userName).child("Score").getValue(String.class);
                        String usersEmail = snapshot.child(userName).child("Email").getValue(String.class);
                        //listViewOnClickItemAlert(userName);
                        checkUserInfoAlert(userName, usersCloutScore, usersEmail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
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
                                    //confirmationAlert(usersName, usersCloutScore, usersEmail);
                                    addFriendsHandlerAlert();
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
    public void checkUserInfoAlert(/*Searched users account name*/String usersName,
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

        /*** We'll need to add in a function that grabs the searched usersprofile image, but
         * if there isn't one then we'll just use the profile image drawable. ***/

        MaterialButton submit = new MaterialButton(UserProfileActivity.this);
        MaterialButton unfollow = new MaterialButton(UserProfileActivity.this);
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
        unfollow.setText(R.string.unfollow);
        submit.setText(R.string.yes);
        submit.setBackgroundResource(R.color.colorPrimary);
        cancel.setText(R.string.cancel);
        cancel.setBackgroundResource(R.color.colorPrimary);

        layout.addView(circleImageView, editTextParams);
        layout.addView(usersNameTV);
        layout.addView(usersCloutScoreTV);
        layout.addView(unfollow, editTextParams);
        layout.addView(cancel);

        unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query unfollowQuerry = friendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child(usersName);
                //Log.d("unfollow_Query_Check", "" + unfollowQuerry);

                /**When the unfollow button is tapped the follow target shoudld be removed from the users
                 * friends list.*/
                friendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child(usersName).removeValue();
                Toast.makeText(UserProfileActivity.this, friendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child(usersName).getKey() + " has been removed from your follow list", Toast.LENGTH_SHORT).show();
                confirm.dismiss();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
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
    /**Users should not be able to add themselves as friehds.*/
    public void checkForSelfAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();

        LinearLayout layout = new LinearLayout(UserProfileActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_warning_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("Hmmm?");
        noMatchAlert.setMessage("You can not make a deed transaction with yourself.");

        MaterialButton confirmButton = new MaterialButton(UserProfileActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noMatchAlert.dismiss();
            }
        });
        noMatchAlert.show();
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

        confirm.setTitle("Follow A User!");
        confirm.setMessage("Please type a users account name to add that user to your follow list. " +
                "Also please make sure to add the \"&\" symbol before the account name.");

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
                if(friendsName.getText().toString().trim().contains("&")){
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
                                if(friendsName.getText().toString().trim().equals(usersName)){
                                    /* Log.d("userNameCompareCheck", "Got a match from the database" + " : " + usersName);
                                    *
                                    * This above log will let us know we found a match*/

                                    /* Now that we know the username can be indexed
                                    * we can report to the currentUser and confirm that they have
                                    * found the user they were looking for
                                    *  */

                                    // TODO BELOW
                                    // The confirmation alert will go here ...
                                    confirmationFriendAlert(usersName.trim(), usersCloutScore, usersEmail);
                                    confirm.dismiss();
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
                if(!usersName.equals(accKey.createAccountKey(mCurrentUser.getEmail()))){
                    addFriend.AddFriend(usersName);
                    confirm.dismiss();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }else{
                    Toast.makeText(UserProfileActivity.this, "You can not follow yourself", Toast.LENGTH_SHORT).show();
                }
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
                confirm.dismiss();
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
                            ScoreHandler handleScore = new ScoreHandler();
                            handleScore.sessionStartScoreIncrease(.02);
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Your score increased by .02", Snackbar.LENGTH_LONG);
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
                imageSelectAlert();
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
            Glide.with(UserProfileActivity.this).load(profilePic).into(profilePic);
        }
    }
    /**Alerts the user that proceeding will take them to select a profile image*/
    public void imageSelectAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(UserProfileActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Profile Photo");
        confirm.setMessage("Would you like to choose a profile image?");

        MaterialButton button = new MaterialButton(UserProfileActivity.this);
        MaterialButton cancelButton = new MaterialButton(UserProfileActivity.this);

        button.setText(R.string.confirm);
        cancelButton.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        layout.addView(cancelButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });

        confirm.show();
    }
    /**END*/
}