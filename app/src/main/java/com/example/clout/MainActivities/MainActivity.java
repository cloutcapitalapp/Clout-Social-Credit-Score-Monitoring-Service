package com.example.clout.MainActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clout.MainActivities.Classes.AccountKeyGenerator;
import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.MainActivities.objects.TransactionObject;
import com.example.clout.R;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

//1 ... What this activity needs TODO: create a system that will allow each user to report activity between themselves and other users.

public class MainActivity extends AppCompatActivity {

    private StorageReference mStorageRefPfofPic;
    FirebaseDatabase db;
    DatabaseReference mRefUsers;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    MaterialButton cloutScore;
    TransactionObject transReportObj;
    AccountKeyGenerator accKey = new AccountKeyGenerator();
    FirebaseDatabase mDatabaseRef = FirebaseDatabase.getInstance();
    DatabaseReference getAccountKeyRef = mDatabaseRef.getReference();
    DatabaseReference mVal = mDatabaseRef.getReference("Users");
    DatabaseReference mCardInfo = mDatabaseRef.getReference("Users");
    ArrayAdapter adapter;
    TextView textView3;
    ScoreHandler scrHandle;
    ListView listView;
    TextView usernameTextView;
    CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] stringArray = {"test", "test this", "this this again", "Its fine if you test this",
        "we can test this all day "};
        transReportObj = new TransactionObject();
        transReportObj.setId(1323);
        transReportObj.getId();
        transReportObj.setAmount(32.80);
        transReportObj.getAmount();

        // TODO - Change database code to match new user specific divisions.

        /////////////////////////////////////////
        //                                     //
        //Init Vars and Data Base Connection   //
        //                                     //
        /////////////////////////////////////////
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        mRefUsers = db.getReference("Users").child(accKey.createAccountKey(mCurrentUser.getEmail()));
        adapter = new ArrayAdapter<String>(this, R.layout.listview, R.id.listviewitem, stringArray);
        cloutScore = findViewById(R.id.CloutScore);
        textView3 = findViewById(R.id.money);
        scrHandle = new ScoreHandler();
        listView = findViewById(R.id.activity);
        listView.setAdapter(adapter);
        usernameTextView = findViewById(R.id.usernameTextView);
        profileImage = findViewById(R.id.profile_image);
        openAnimations();
        imageViewButtonToProfile();
        cashButtonHandle();
        listViewListener();
    }
    // OnStart Check what the users clout score is.
    @Override
    protected void onStart(){
        super.onStart();
        initScoreHandling();/* because there won't be a score
        when the user is new, create one { of 200 } send to DB and set button text to value */
        onClickGoToCreateNewSessionActivity(); /*  */
        adjustCashVal();
        getUsernameOnStart(); /* Gets username from database - and sets text to AccountKey */

        onStartGrabImage(); /* When the app loads MainActivity crawl the database and find profile image */

        // This algo needs to check to see if the user is returning or if the user opening the app for the first time
        // The reason this needs to be done is because the firstReturnAlertBlock methods need to not show up anymore after the
        // alerts have been fully dismissed.
        DatabaseReference firstTimeCheckIntro = mDatabaseRef.getReference("Users")
                .child(accKey.createAccountKey(mCurrentUser.getEmail()))
                .child("isFirstTimeUserIntro");

        firstTimeCheckIntro.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                Log.d("value", "" + value);
                if(value.equals("yes")){
                    firstReturnAlertBlock1();
                }else{
                    // do nothing
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void initScoreHandling(){
        FirebaseUser user = mAuth.getCurrentUser();
        AccountKeyGenerator accKey = new AccountKeyGenerator();
        if(mRefUsers.child(accKey.createAccountKey(user.getEmail())) != null){
            scoreGrab();
            // Nothing should happen if the value of the score is 200 already
        }else{
            // should ONLY EVER happen on first load.
            mRefUsers.setValue("200");
            scoreGrab();
        }
    }
    //init user score
    public void scoreGrab(){
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        final MaterialButton cloutScore1 = findViewById(R.id.CloutScore);
        FirebaseUser user = mAuth.getCurrentUser();
        AccountKeyGenerator accKey = new AccountKeyGenerator();

        // Read from the database to get the users score
        mRefUsers.child("Score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("db_ref_success", "Value is: " + value + " " + mRefUsers);
                cloutScore1.setText(String.format(String.valueOf(value)));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("db_ref_failed", "Failed to read value.", error.toException());
            }
        });

    }
    //When the ScoreHolder button is clicked go to the CreateNewSession Activity
    public void onClickGoToCreateNewSessionActivity(){
        cloutScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //transAlert();
                Intent toCreateNewSession = new Intent(MainActivity.this, CreateNewSessionStart.class);
                startActivity(toCreateNewSession);
            }
        });
    }
    public void createScoreInit(){
        mRefUsers.child(mCurrentUser.getEmail().replace(".", ""));
        mRefUsers.child(mCurrentUser.getEmail().replace(".", "")).child("score").setValue("200.00");
    }
    public void cashButtonHandle(){
        // Read from the database
        textView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: add conditional statement that verifies user has not yet added banking info
                // Read from the database
                mCardInfo.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("isCardOnFile").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        Log.d("CardSuccess", "Value is: " + value);

                        if(value.equals("NO")){

                            AlertDialog.Builder trackNo = new AlertDialog.Builder(MainActivity.this);
                            trackNo.setTitle("Add Payment Method?");
                            trackNo.setMessage("There is no card information on file, " +
                                    "would you like to add a payment method?");
                            trackNo.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent passToCardHandler = new Intent(MainActivity.this, AddPaymentMethodActivity.class);
                                    startActivity(passToCardHandler);
                                }
                            });
                            trackNo.setNegativeButton("Not Right Now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            trackNo.create();
                            trackNo.show();

                        }else{

                            //go to add money activity
                            if(value.equals("YES")) {

                                AlertDialog.Builder trackYES = new AlertDialog.Builder(MainActivity.this);
                                trackYES.setTitle("Add Cash?");
                                trackYES.setMessage("Would you like to add funds to your account?");
                                trackYES.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent passToCardHandler = new Intent(MainActivity.this, addMoneyActivity.class);
                                        startActivity(passToCardHandler);
                                    }
                                });
                                trackYES.setNegativeButton("Not Right Now", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                                trackYES.create();
                                trackYES.show();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w("Fail", "Failed to read value.", error.toException());
                    }
                });

            }
        });
    }
    private void adjustCashVal(){
        mVal.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Cash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                    String value = snapshot1.getValue(String.class);
                    Log.d("Success", "Value is: " + value);
                    textView3 = findViewById(R.id.money);
                    textView3.setText(value);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail", "Failed to read value.", error.toException());
            }
        });
    }
    private void listViewListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("test_log", "This will be the test item click");
                final TransactionObject transReport = new TransactionObject();
                AlertDialog.Builder testAlert = new AlertDialog.Builder(MainActivity.this);
                testAlert.setMessage("Test Message");
                testAlert.setTitle("test title");
                LinearLayout layout = new LinearLayout(MainActivity.this);
                testAlert.setView(layout);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                final EditText editTest = new EditText(MainActivity.this);
                editTest.setHint("Amount");
                layout.addView(editTest);
                testAlert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("transReport Debug", "Pass : ");
                        Double editText = Double.valueOf(editTest.getText().toString());
                        Log.d("editTextCapture", " " + editText);
                        transReportObj.setAmount(editText);
                        transReportObj.getAmount();
                    }
                });
                testAlert.show();
            }
        });

    }
    private void getUsernameOnStart(){
        // Read from the database
        getAccountKeyRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                usernameTextView.setText(key);
                Log.d("getUNameOnStartTest: ", "Value is: " + value + " : " + getAccountKeyRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).getKey());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("getUNOnStartTest : ", "Failed to read value.", error.toException());
            }
        });
    }
    private void imageViewButtonToProfile(){
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(MainActivity.this, userprfileactivity.class);
                startActivity(goToProfile);
            }
        });
    }
    public void firstReturnAlertBlock1(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("Intro Block 1");
        confirm.setMessage("Welcome to clout! Clout is a social credit score monitoring software.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        MaterialButton cancel = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        cancel.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(button);
        layout.addView(cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                firstReturnAlertBlock2();
            }
        });
        confirm.show();
    }
    public void firstReturnAlertBlock2(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("Intro Block 2");
        confirm.setMessage("Clout is general purpose community driven software for helping you decide " +
                "who is trust worthy and who is not.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        MaterialButton cancel = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        cancel.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(button);
        layout.addView(cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstReturnAlertBlock3();
                confirm.dismiss();
            }
        });
        confirm.show();
    }
    public void firstReturnAlertBlock3(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("Intro Block 3");
        confirm.setMessage("We're excited you decided to join us! Please explore the app to discover its' features!");

        MaterialButton button = new MaterialButton(MainActivity.this);
        MaterialButton cancel = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        cancel.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(button);
        layout.addView(cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference firstTimeCheckIntro = mDatabaseRef.getReference("Users")
                        .child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("isFirstTimeUserIntro");

                firstTimeCheckIntro.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        Log.d("value", "" + value);
                        if(value.equals("yes")){
                            firstTimeCheckIntro.setValue("no");
                        }else{
                            // do nothing
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                confirm.dismiss();
            }
        });
        confirm.show();
    }
    public void openAnimations(){

        ObjectAnimator animProfileImage = ObjectAnimator.ofFloat(profileImage, "translationX", 30f);
        animProfileImage.setDuration(500);
        animProfileImage.start();

        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(textView3, "translationY", -90f);
        animCashButton.setDuration(500);
        animCashButton.start();

        ObjectAnimator animListView = ObjectAnimator.ofFloat(listView, "translationY", -90f);
        animListView.setDuration(500);
        animListView.start();

    }
    /* When onStart 'LifeCycle' begins check if there is a user profile image, if so place in C.ImgView */
    private void onStartGrabImage(){
        mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + mCurrentUser.getEmail() + "profilepicture" + "." + "jpg");

        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(MainActivity.this).load(uriString).into(profileImage);
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

            Glide.with(MainActivity.this).load(profileImage).into(profileImage);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
    }
}