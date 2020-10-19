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
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.android.material.tabs.TabLayout;
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
import java.util.ArrayList;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;

/*** This activity will house the main-hub. From here you'll have access to the other app activities
 * and you'll be able to see the full transaction history of the entire app.
 * @param // TODO: 10/12/20 There is a bug that causes the app to return to CreateNewSession2 after
 * @param // TODO: 10/12/20 CreateNewSession2 has been passed, and seems to be recreated when the
 * @param // TODO: 10/12/20 the user tries to return to the main method after passing CreateNewSession2
 * @param // TODO: 10/12/20 Strangely, it affects all devices when any device returns to main
 * @see // FIXME: 10/12/20 Above!***/

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
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
    DatabaseReference mVal, mCardInfo, mGetCash, getAccountKeyRef, mEventTransactionsList;
    //ListView
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> arrayList;


    /** onCreate should house initialized variables as well as methods, but no direct code*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.transactionTabs);

        //ListView
        arrayList = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.eventTransactionsListView);
        adapter = new ArrayAdapter<String>(this,R.layout.listview, R.id.listviewitem, arrayList);
        listView.setAdapter(adapter);

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
        mEventTransactionsList = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser.getEmail()) + "_" + "event_transactions");

        /**Method | Functionality*/
        openAnimations();
        imageViewButtonToProfile();
        cashButtonHandle();
        firstTimeCloutScoreOnClick();
        //listViewListener();
        eventTrasnactionsListView();
        tabLayoutManager();
    }
    /***/

    /**When the back button was being pressed, the user would be taken back to the launch screen
     * with no animation and the intent was killed so the user wasn't being returned to the */
    @Override
    public void onBackPressed() {
        //...do nothing
    }

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
                    listView.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else /* if else, then index 0 is active | index 0 is the sign up tab, and all elements will be available when index 0 is active */{
                    listView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // called when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // called when a tab is reselected
            }
        });

    }

    public void eventTrasnactionsListView(){
        mEventTransactionsList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("forLoopStart", "Start");
                Log.d("forLoopStart", "start" + " : " + snapshot);

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    String desc = snapshot1.child("description").getValue(String.class);
                    String enddate = snapshot1.child("enddate").getValue(String.class);
                    String email = snapshot1.child("username").getValue(String.class);
                    String allData = desc + "\n" + enddate + "\n" + email;

                    Log.d("logValue", "" + allData);
                    arrayList.add(allData);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**OnStart should pull the users clout score and init RecyclerView
     * The current code should be refactored such that it is more organized and should only house
     * methods that perform the code needed --- */
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
    /***/

    /** onPause should start the dateReached service
     * dateReach service should be refactored to handle notifications,
     * but should also be changed to launch onDestroy()*/
    @Override
    protected void onPause(){
        super.onPause();
        //startService(new Intent(this, dateReached.class));
    }
    /***/

    /**Currently onDestroy() does not create service
     * this must be changed*/
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //startService(new Intent(this, dateReached.class));
    }
    /***/

    /**When the user taps on a RecyclerView item they will be prompted with that users information
     * And a follow option will be granted apon accaptance. This may be removed as this may be
     * more information than general users should have access to.*/
    public void confirmationAlert(/*Selected users account name*/String usersName,
            /*Selected users clout score*/String usersCloutScore, /*Selected users account email*/String usersEmail){

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
                String ripUsersName = usersName.substring(usersName.indexOf(" : ") + 1);
                Log.d("ripUNCheck", ripUsersName);
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
    /***/

    /** When the app starts, firebase should be called and the users cash amount should be retrieved*/
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
    }
    /***/

    /**This view holder is for the Main RecyclerView*/
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
    /***/

    /**onStart the usersScore should be checked
     * if there is a score score grab will be called
     * if there is no score a score will be created*/
    public void initScoreHandling(){
        if(mRefUsers.child("Score") != null){
            scoreGrab();
            // Nothing should happen if the value of the score is 200 already
        }else{
            // should ONLY EVER happen on first load.
            mRefUsers.setValue("200");
            scoreGrab();
        }
    }

    /**onStart the users score should be grabed*/
    //init user score
    public void scoreGrab(){
        final MaterialButton cloutScore1 = findViewById(R.id.CloutScore);
        // Read from the database to get the users score
        mRefUsers.child("Score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);

                //Log.d("db_ref_success", "Value is: " + value + " " + mRefUsers);

                cloutScore1.setText(String.valueOf(value));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("db_ref_failed", "Failed to read value.", error.toException());
            }
        });

    }
    /***/

    /**When the scorehandler/cloutScore button is tapped go CreateNewSessionStart activity*/
    public void onClickGoToCreateNewSessionActivity(){
        cloutScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //transAlert();
                createTransactionAlert();
            }
        });
    }
    /***/

    /**When the cash/money button is tapped the user should be taken to the LoadCash activity
     * The users system should check to see if the user has added banking information,
     * if they have not, the user should be able to use a card*/
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
    /***/

    /**This method listens for updates to the user objects cash amount item in firebase*/
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
    /***/

    /**This method was implements a listView
     * it has since been changed to a recyclerView
     * so the below method has no use
     * it will be removed before release*/
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
    /***/

    /**When the app starts the firebase User Objects' username item should be retrieved and displayed
     * on the Title TextView*/
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
                //Log.d("getUNameOnStartTest: ", "Value is: " + value + " : " + getAccountKeyRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).getKey());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("getUNOnStartTest : ", "Failed to read value.", error.toException());
            }
        });
    }
    /***/

    /**When the imageView at the top right corner is tapped, the user should be taken to their personal
     * profile*/
    private void imageViewButtonToProfile(){
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToProfile = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(goToProfile);
            }
        });
    }
    /***/

    /**The following three alerts work together to introduce the user to the app
     * Each user object in firebase has an 'isFirstTimeUserIntro' item with a boolean val(TRUE or FALSE)
     * which simply keeps track of weather or not the user has been introduced to the app with the alerts yet*/
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
    /***/

    /**The following alert will ask the user if the user would like to create a transaction.
     * If the user would like to create a transaction they will have 2 options.
     * O 1 - Event|Trust Transaction
     * O 2 - Money Transaction*/
    private void createTransactionAlert(){
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

        confirm.setTitle("Create A Transaction?");
        confirm.setMessage("What kind of transaction would you like to create?");

        MaterialButton eventTransButton = new MaterialButton(MainActivity.this);
        MaterialButton moneyTransButton = new MaterialButton(MainActivity.this);
        MaterialButton cancelButton = new MaterialButton(MainActivity.this);

        eventTransButton.setText(R.string.eventTransaction);
        moneyTransButton.setText(R.string.money_transaction);
        cancelButton.setText(R.string.cancel);

        eventTransButton.setBackgroundResource(R.color.colorPrimary);
        moneyTransButton.setBackgroundResource(R.color.colorPrimary);
        cancelButton.setBackgroundResource(R.color.colorPrimary);

        layout.addView(eventTransButton);
        layout.addView(moneyTransButton);
        layout.addView(cancelButton);

        eventTransButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO - Create EventTransactionActivity
                Intent passToEventTransaction = new Intent(MainActivity.this, EventTransactionActivity.class);
                startActivity(passToEventTransaction);
            }
        });

        moneyTransButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //T.O.D.O
                // An alert that informs the user that the payments system is not yet ready.
                // DONE!
                paymentsNotReadyAlert();
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
    private void paymentsNotReadyAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
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

        MaterialButton confirmButton = new MaterialButton(MainActivity.this);
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

    /**This method houses 3 animations one for the profileImage, money button and recyclerView
     * These are standard animations meant to attract the user*/
    public void openAnimations(){
        ObjectAnimator animProfileImage = ObjectAnimator.ofFloat(profileImage, "translationX", 30f);
        animProfileImage.setDuration(500);
        animProfileImage.start();

        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(money, "translationY", -90f);
        animCashButton.setDuration(500);
        animCashButton.start();

        /*ObjectAnimator animListView = ObjectAnimator.ofFloat(recyclerView, "translationY", -90f);
        animListView.setDuration(500);
        animListView.start();*/
    }
    /***/

    /** When onStart 'LifeCycle' begins check if there is a user profile image, if so place in C.ImgView */
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
    /***/

    /**This alert will report to the user what the details of an recylerView list item are when tapped*/
    public void transactionDetialsAlert(/* Get the date from the selected viewHolder */ String date,
            /* Get the rate from the selected viewHolder */ String rate,
            /* Get the description from the selected viewHolder */ String description,
            /*get usersnames from transacting users*/ String TransactingUsers){

        // The below string is a test that grabs the users and isolates the to-Users
        // String FinalRipTUsers = TransactingUsers.substring(TransactingUsers.indexOf(" : ") + 1)
           //     .replace(": ", "");
        //Log.d("CheckUsers", FinalRipTUsers);

        String transUserString = accKey.reversAccountKeyFromRecyclerViewAdapter(TransactingUsers);
        MaterialAlertDialogBuilder transactionDetialsAlert = new MaterialAlertDialogBuilder(this);
        SpannableString ss = new SpannableString(rate);
        BackgroundColorSpan fcsGreen = new BackgroundColorSpan(Color.RED);
        ss.setSpan(fcsGreen, 0, rate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        transactionDetialsAlert.setIcon(getResources().getDrawable(R.drawable.ic_baseline_emoji_emotions_24));
        transactionDetialsAlert.setTitle("Transaction Details");
        transactionDetialsAlert.setMessage("Users : " + TransactingUsers.replace(" : ", " and ") +
                "\n\nThis loan is in the amount of : " + ss +
                "\n\nTo be repaid on " + ": " + date
                + "\n\nWould you like to add : " + transUserString + " as a friend?" + "\n");
        transactionDetialsAlert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("reversedLog", transUserString);
                mVal.child(transUserString).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child("Email").getValue(String.class);
                        String Score = snapshot.child("Score").getValue(String.class);
                        Log.d("checkForEmail", "" + email);
                        confirmationAlert(transUserString, Score, email);
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
    /***/

    /**This method will provide a first time alert for the CloutScoreButton
     * What needs to happen?
     * Alert will introducse the user to what transactions are*/

    /**First time user cloutscore Alert blocks*/
    public void firstTimeCloutScoreOnClick(){
        cloutScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference firstTimeCheckIntro = mDatabaseRef.getReference("Users")
                        .child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("isFirstTimeUserScore");

                firstTimeCheckIntro.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        Log.d("value", "" + value);
                        if(value.equals("yes")){
                            firstimeCloutScoreOnClickAlert1();
                        }else{
                            // do nothing
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
    public void firstimeCloutScoreOnClickAlert1(){
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

        confirm.setTitle("HELLO!");
        confirm.setMessage("I see you've found the Clout Score button!");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                firstimeCloutScoreOnClickAlert2();
            }
        });
        confirm.show();
    }
    public void firstimeCloutScoreOnClickAlert2(){
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

        confirm.setTitle("HELLO!");
        confirm.setMessage("We will keep track of your Clout Score here.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                firstimeCloutScoreOnClickAlert3();
            }
        });
        confirm.show();
    }
    public void firstimeCloutScoreOnClickAlert3(){
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

        confirm.setTitle("HELLO!");
        confirm.setMessage("You'll be able to create transactions here also. Transactions are submissions " +
                "to other users that increase your score and gives the other user the ability to increase theirs. " +
                "So have fun building your score.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                firstimeCloutScoreOnClickAlert4();
            }
        });
        confirm.show();
    }
    public void firstimeCloutScoreOnClickAlert4(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(MainActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_mood_bad_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Warning!");
        confirm.setMessage("Just remember, return debts and keep your word and your score will increase, " +
                "but if you don't, it will decrease.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference firstTimeScoreCheck = mDatabaseRef.getReference("Users")
                        .child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("isFirstTimeUserScore");

                firstTimeScoreCheck.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        Log.d("value", "" + value);
                        if(value.equals("yes")){
                            firstTimeScoreCheck.setValue("no");
                        }else{
                            // do nothing
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        confirm.show();
    }
    /**END*/
}