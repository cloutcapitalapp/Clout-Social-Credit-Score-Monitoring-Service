package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.MainActivities.Classes.AddFriendHandler;
import com.example.clout.MainActivities.datamodels.MainDataModel;
import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import java.text.MessageFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/*** This activity will house the main-hub. From here you'll have access to the other app activities
 * and you'll be able to see the full transaction history of the entire app. ***/

public class MainActivity extends AppCompatActivity {

    AddFriendHandler addFriend = new AddFriendHandler(MainActivity.this);
    TextView money, usernameTextView;
    ScoreHandler scrHandle;
    CircleImageView profileImage;
    RecyclerView recyclerView;
    FirebaseDatabase db;
    DatabaseReference mCrawlUsers, mRefUsers;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    MaterialButton cloutScore;
    AccountKeyManager accKey = new AccountKeyManager();
    FirebaseDatabase mDatabaseRef;
    DatabaseReference mVal, mCardInfo, mGetCash, getAccountKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScoreHandler scoreHandler = new ScoreHandler();
        scoreHandler.sessionStartScoreIncrease(.01);
        /*transReportObj = new TransactionObject();
        transReportObj.setId(1323);
        transReportObj.getId();
        transReportObj.setAmount(32.80);
        transReportObj.getAmount();*/

        // T.O.D.O - Change database code to match new user specific divisions.
        /*** The above T.O.D.O was done ***/

        /////////////////////////////////////////
        //                                     //
        //Init Vars and Data Base Connection   //
        //                                     //
        /////////////////////////////////////////

        mDatabaseRef = FirebaseDatabase.getInstance();
        getAccountKeyRef = mDatabaseRef.getReference();
        mVal = mDatabaseRef.getReference("Users");
        mCardInfo = mDatabaseRef.getReference("Users");
        mGetCash = mDatabaseRef.getReference("Users");
        money = findViewById(R.id.money);
        recyclerView = findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        mCrawlUsers = db.getReference("User_Transactions");
        mRefUsers = db.getReference("Users").child(accKey.createAccountKey(mCurrentUser.getEmail()));
        cloutScore = findViewById(R.id.CloutScore);
        scrHandle = new ScoreHandler();
        usernameTextView = findViewById(R.id.usernameTextView);
        profileImage = findViewById(R.id.profile_image);
        openAnimations();
        imageViewButtonToProfile();
        cashButtonHandle();
        //listViewListener();
    }
    // OnStart Check what the users clout score is.
    @Override
    protected void onStart(){
        super.onStart();

        onStartGetCash();
        initScoreHandling();/* because there won't be a score
        when the user is new, create one { of 200 } send to DB and set button text to value */
        onClickGoToCreateNewSessionActivity(); /*  */
        adjustCashVal();
        getUsernameOnStart(); /* Gets username from database - and sets text to AccountKey */

        onStartGrabImage(); /* When the app loads MainActivity crawl the database and find profile image */

        FirebaseRecyclerOptions<MainDataModel> options =
                new FirebaseRecyclerOptions.Builder<MainDataModel>()
                        .setQuery(mCrawlUsers, MainDataModel.class)
                        .build();

        // Initialize recycler view...
        FirebaseRecyclerAdapter<MainDataModel, tasksViewHolder> adapter = new FirebaseRecyclerAdapter<MainDataModel, tasksViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull tasksViewHolder holder, int position, @NonNull MainDataModel model) {
                holder.getTransacting_Users.setText(model.getTransacting_Users());
                holder.getDate.setText(model.getDate());
                holder.getLocation.setText(model.getLocation());
                holder.getCurrent_date.setText(model.getCurrent_date());
                holder.getAmount.setText(MessageFormat.format("{0}{1}", getString(R.string.usdCurSign), String.format(Locale.ENGLISH, "%.2f", model.getAmount()/100)));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // This string will call get the item id for the item selected in the recycler view
                        //String itemID = getRef(position).getKey();
                        String TransactingUsers = holder.getTransacting_Users.getText().toString();
                        String currentDateString = holder.getCurrent_date.getText().toString();
                        String dateString = holder.getDate.getText().toString();
                        String rateString = holder.getAmount.getText().toString();
                        String descString = holder.getCurrent_date.getText().toString();

                        // This is a test toast message that will display the item ID for the item selected in the Recycler View
                        //Toast.makeText(MainHubActivity.this, itemID + " " + holder.getRate.getText().toString(), Toast.LENGTH_SHORT).show();

                        // TODO: Launch alert that will, for now, act as a test to take the Errand selected.
                        // This alert will also serve as a quick test for transactions
                        transactionDetialsAlert(dateString, rateString, descString, TransactingUsers);
                    }
                });
            }

            @NonNull
            @Override
            public tasksViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i){
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_list_item, viewGroup,false);
                tasksViewHolder viewHolder = new tasksViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.startListening();

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
                if(!value.equals(null)){
                    if(value.equals("yes")){
                        firstReturnAlertBlock1();
                    }else{
                        // do nothing
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void confirmationAlert(/*Searched users account name*/String usersName,
            /*Searched users account name*/String usersCloutScore, String usersEmail){

        final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
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

        confirm.setTitle("CONFIRM FRIEND REQUEST");

        /*** We'll need to add in a function that grabs the searched usersprofile image, but
         * if there isn't one then we'll just use the profile image drawable. ***/
        MaterialButton submit = new MaterialButton(MainActivity.this);
        MaterialButton cancel = new MaterialButton(MainActivity.this);
        TextView usersNameTV = new MaterialButton(MainActivity.this);
        CircleImageView circleImageView = new CircleImageView(MainActivity.this);
        circleImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_person_24));
        /*** Image Grab START ***/
        StorageReference mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + usersEmail + "profilepicture" + "." + "jpg");
        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String uriString = uri.toString();
                    Glide.with(MainActivity.this).load(uriString).into(circleImageView);
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

            Glide.with(MainActivity.this).load(circleImageView).into(circleImageView);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
        /*** Image Grab END ***/
        TextView usersCloutScoreTV = new MaterialButton(MainActivity.this);
        usersNameTV.setText(usersName);
        usersCloutScoreTV.setText(usersCloutScore);
        submit.setText(R.string.add_friend);
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
                confirm.dismiss();
                Toast.makeText(MainActivity.this, usersName + " has been added to your friends list", Toast.LENGTH_SHORT).show();
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

    private void onStartGetCash(){
        mGetCash.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("Cash").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String cashValue = snapshot.getValue(String.class);
                //@SuppressLint("DefaultLocale") String convertCashValue = String.format("%.02f", Double.parseDouble(cashValue));
                money.setText(cashValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }/* This method needs to call the cash value stored at the user level */

    public static class tasksViewHolder extends RecyclerView.ViewHolder{

        TextView getTransacting_Users, getDate, getCurrent_date, getLocation, getAmount;

        public tasksViewHolder(@NonNull View itemView) {
            super(itemView);

            getTransacting_Users = itemView.findViewById(R.id.Transacting_Users);
            getDate         = itemView.findViewById(R.id.date);
            getCurrent_date  = itemView.findViewById(R.id.current_date);
            getLocation     = itemView.findViewById(R.id.location);
            getAmount       = itemView.findViewById(R.id.amount);
        }
    }
    public void initScoreHandling(){
        FirebaseUser user = mAuth.getCurrentUser();
        AccountKeyManager accKey = new AccountKeyManager();
        if(mRefUsers.child("Score") != null){
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
        AccountKeyManager accKey = new AccountKeyManager();

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
    public void cashButtonHandle(){
        // Read from the database
        money.setOnClickListener(new View.OnClickListener() {
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

                            final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
                            LinearLayout layout = new LinearLayout(getApplicationContext());
                            layout.setOrientation(LinearLayout.VERTICAL);
                            confirm.setView(layout);

                            confirm.setCancelable(false);
                            confirm.setCanceledOnTouchOutside(false);

                            confirm.setIcon(R.drawable.ic_baseline_attach_money_24);

                            Window window = confirm.getWindow();
                            window.setGravity(Gravity.BOTTOM);

                            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            confirm.setTitle("Add Funds To Your Account!");
                            confirm.setMessage("There are no funds available on your account. Would " +
                                    "you like to add funds?");

                            MaterialButton button = new MaterialButton(MainActivity.this);
                            MaterialButton cancel = new MaterialButton(MainActivity.this);
                            button.setText(R.string.confirm);
                            button.setBackgroundResource(R.color.colorPrimary);
                            cancel.setText("CANCEL");
                            cancel.setBackgroundResource(R.color.colorPrimary);
                            layout.addView(button);
                            layout.addView(cancel);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent passToCardHandler = new Intent(MainActivity.this, LoadCashActivity.class);
                                    startActivity(passToCardHandler);
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
                    money.setText(value);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail", "Failed to read value.", error.toException());
            }
        });
    }
    /*private void listViewListener(){
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

    }*/
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

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        confirm.setTitle("WELCOME!");
        confirm.setMessage("Welcome to Clout! Clout is a social credit score monitoring software.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
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

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("WELCOME!");
        confirm.setMessage("Clout is a general purpose, community driven software for helping you decide " +
                "who is trust worthy and who is not.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
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

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("WELCOME!");
        confirm.setMessage("We're excited you decided to join us! Please explore the app to discover its' features!");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
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

        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(money, "translationY", -90f);
        animCashButton.setDuration(500);
        animCashButton.start();

        ObjectAnimator animListView = ObjectAnimator.ofFloat(recyclerView, "translationY", -90f);
        animListView.setDuration(500);
        animListView.start();

    }
    /* When onStart 'LifeCycle' begins check if there is a user profile image, if so place in C.ImgView */
    private void onStartGrabImage(){
        StorageReference mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + mCurrentUser.getEmail() + "profilepicture" + "." + "jpg");

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
    public void transactionDetialsAlert(/* Get the date from the selected viewHolder */ String date,
            /* Get the rate from the selected viewHolder */ String rate,
            /* Get the description from the selected viewHolder */ String description,
            /*get usersnames from transacting users*/ String TransactingUsers){
        String testLog = accKey.reversAccountKeyFromRecyclerViewAdapter(TransactingUsers);
        MaterialAlertDialogBuilder transactionDetialsAlert = new MaterialAlertDialogBuilder(this);
        SpannableString ss = new SpannableString(rate);
        BackgroundColorSpan fcsGreen = new BackgroundColorSpan(Color.RED);
        ss.setSpan(fcsGreen, 0, rate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        transactionDetialsAlert.setIcon(getResources().getDrawable(R.drawable.ic_baseline_emoji_emotions_24));
        transactionDetialsAlert.setTitle("Transaction Details");
        transactionDetialsAlert.setMessage("Users : " + TransactingUsers.replace(" : ", " and ") +
                "\n\nThis loan is in the amount of : " + ss +
                "\n\nTo be repaid on " + ": " + date
                + "\n\nWould you like to add : " + testLog + " as a friend?" + "\n");
        transactionDetialsAlert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("reversedLog", testLog);
                mVal.child(testLog).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child("Email").getValue(String.class);
                        String Score = snapshot.child("Score").getValue(String.class);
                        Log.d("checkForEmail", "" + email);
                        confirmationAlert(testLog, Score, email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        transactionDetialsAlert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        transactionDetialsAlert.show();
    }
}