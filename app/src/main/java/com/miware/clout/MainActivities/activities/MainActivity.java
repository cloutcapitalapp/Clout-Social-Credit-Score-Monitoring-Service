package com.miware.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.miware.clout.MainActivities.Classes.AccountKeyManager;
import com.miware.clout.MainActivities.Classes.AddFriendHandler;
import com.miware.clout.MainActivities.datamodels.MainDataModel;
import com.miware.clout.MainActivities.Classes.ScoreHandler;
import com.miware.clout.R;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.AccountApi;
import sibModel.GetAccount;

/*** This activity will house the main-hub. From here you'll have access to the other app activities
 * and you'll be able to see the full transaction history of the entire app.
 * @param // TODO: 10/12/20 There is a bug that causes the app to return to CreateNewSession2 after
 * @param // TODO: 10/12/20 CreateNewSession2 has been passed, and seems to be recreated when the
 * @param // TODO: 10/12/20 the user tries to return to the main method after passing CreateNewSession2
 * @param // TODO: 10/12/20 Strangely, it affects all devices when any device returns to main
 * @see // FIXME: 10/12/20 Above!***/

public class MainActivity extends AppCompatActivity {


    int clickedamount = 0;
    boolean ranBefore;

    RelativeLayout topLevel;

    TextView cloutintro1;
    TextView cloutintro2;
    TextView cloutintro3;
    TextView cloutintro4;
    TextView profileintro1;
    ImageView profileimage2;

    //TabLayout
    TabLayout tabLayout;

    //Classes
    AddFriendHandler addFriend = new AddFriendHandler(MainActivity.this);
    AccountKeyManager accKey = new AccountKeyManager();
    ScoreHandler scrHandle;

    //TextView
    TextView money, usernameTextView;

    //CircleImageView and ImageView
    CircleImageView profileImage;

    //RecyclerView
    RecyclerView recyclerView;

    //Firebase init
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    FirebaseDatabase mDatabaseRef, db;
    DatabaseReference mCrawlUsers,
            mRefUsers,
            mVal,
            mCardInfo,
            mGetCash,
            getAccountKeyRef,
            mEventTransactionsList,
            mEventReceivedTransactionList,
            pendingRef;

    //MaterialButton
    MaterialButton cloutScore;

    //ListView
    ListView listView, receivedListView;

    //ArrayAdapter
    ArrayAdapter<String> adapter, adapterReceived;
    //ArrayList
    ArrayList<String> arrayList, arrayListReceived;

    SharedPreferences sharedpreferences;
    SharedPreferences preferences;

    /** onCreate should house initialized variables as well as methods, but no direct code*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();

        ScoreHandler scoreHandler = new ScoreHandler();
        scoreHandler.sessionStartScoreIncrease(.01);

        //Call ref to animations
        openAnimations();
        //When image view is tapped go to user profile
        imageViewButtonToProfile();
        //
        cashButtonHandle();
        firstTimeCloutScoreOnClick();
        eventTransactionsReceivedListView();
        eventTrasnactionsListView();
        tabLayoutManager();
        receivedListViewOnClick();

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
                //Log.d("value", "" + value);
                if(!value.equals(null)){
                    if(value.equals("yes")){
                        cloutScore.setEnabled(false);
                        topLevel.setVisibility(View.VISIBLE);
                        introLoop();
                    }else{
                        // do nothing
                        //Toast.makeText(MainActivity.this, "No Go", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    /***/

    private void initVars(){
        topLevel = findViewById(R.id.top_layout);

        preferences = getPreferences(MODE_PRIVATE);
        ranBefore = preferences.getBoolean("RanBefore", false);

        tabLayout = findViewById(R.id.transactionTabs);

        cloutintro1 = findViewById(R.id.cloutintrotest1);
        cloutintro2 = findViewById(R.id.cloutintrotest2);
        cloutintro3 = findViewById(R.id.cloutintrotest3);
        cloutintro4 = findViewById(R.id.cloutintrotest4);
        profileintro1 = findViewById(R.id.profileintro1);
        profileimage2 = findViewById(R.id.profileintro2);

        //ListView
        arrayList = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.eventTransactionsListView);
        adapter = new ArrayAdapter<String>(this,R.layout.listview, R.id.listviewitem, arrayList);
        listView.setAdapter(adapter);

        //receivedListView
        arrayListReceived = new ArrayList<String>();
        receivedListView = (ListView) findViewById(R.id.receivedListView);
        adapterReceived = new ArrayAdapter<String>(this,R.layout.listview, R.id.listviewitem, arrayListReceived);
        receivedListView.setAdapter(adapterReceived);

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
        mEventReceivedTransactionList = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser.getEmail()) + "_" + "event_received");
    }

    private void transactionalEmailAPI(){
        /**Start Transactional Email API*/
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure API key authorization: api-key
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //apiKey.setApiKeyPrefix("Token");

        // Configure API key authorization: partnerKey
        ApiKeyAuth partnerKey = (ApiKeyAuth) defaultClient.getAuthentication("partner-key");
        partnerKey.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //partnerKey.setApiKeyPrefix("Token");

        AccountApi apiInstance = new AccountApi();
        try {
            GetAccount result = apiInstance.getAccount();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AccountApi#getAccount");
            e.printStackTrace();
        }
        /**END - Transactional Email API*/
    }

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
                    receivedListView.setVisibility(View.VISIBLE);
                }else /* if else, then index 0 is active | index 0 is the sign up tab, and all elements will be available when index 0 is active */{
                    listView.setVisibility(View.VISIBLE);
                    receivedListView.setVisibility(View.INVISIBLE);
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

    /**The receivedListViewOnClick method should hold an onClick listener for the pendingAlertsListView
     * the  listener should listen for click that will display an alert to the user that asks if the
     * transaction was complete.*/
    public void receivedListViewOnClick(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userName = (String) listView.getItemAtPosition(position);
                String userNamePull = userName.substring(userName.indexOf("\n&")+1);
                receivedListViewOnClickAlert(userNamePull);
                Toast.makeText(MainActivity.this, "" + userNamePull, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void receivedListViewOnClickAlert(String userName){
        final AlertDialog endTransactionAlert= new MaterialAlertDialogBuilder(MainActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        endTransactionAlert.setView(layout);

        endTransactionAlert.setCancelable(false);
        endTransactionAlert.setCanceledOnTouchOutside(false);

        endTransactionAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = endTransactionAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        endTransactionAlert.setTitle("DONE?");
        endTransactionAlert.setMessage("Let us know how " + userName.trim() + " did." + " was " +
                userName.trim() +
                " on-time, positive, and over-all pleasant? If so, let them know!");

        MaterialButton greatCompletion = new MaterialButton(MainActivity.this);
        MaterialButton okCompletion = new MaterialButton(MainActivity.this);
        MaterialButton badCompletion = new MaterialButton(MainActivity.this);
        MaterialButton terribleCompletion = new MaterialButton(MainActivity.this);
        MaterialButton cancelButton = new MaterialButton(MainActivity.this);

        greatCompletion.setText(String.format("%s", getString(R.string.great_score_increase)));
        okCompletion.setText(R.string.okay);
        badCompletion.setText(R.string.bad);
        terribleCompletion.setText(R.string.terrible);
        cancelButton.setText(R.string.cancel);

        greatCompletion.setBackgroundResource(R.color.colorPrimary);
        okCompletion.setBackgroundResource(R.color.colorPrimary);
        badCompletion.setBackgroundResource(R.color.colorPrimary);
        terribleCompletion.setBackgroundResource(R.color.colorPrimary);
        terribleCompletion.setBackgroundResource(R.color.colorPrimary);

        layout.addView(greatCompletion);
        layout.addView(okCompletion);
        layout.addView(badCompletion);
        layout.addView(terribleCompletion);
        layout.addView(cancelButton);

        greatCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrHandle.increaseEndUserScore(userName.trim(), 1);
                pendingRef = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser
                        .getEmail())
                        + "_" + "event_transactions");

                pendingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                            adapter.notifyDataSetChanged();
                            listView.invalidateViews();

                            completedTransactionAlert();
                        }
                        scrHandle.increaseEndUserScore(userName.trim(), 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Log.e("failed", "onCancelled", databaseError.toException());
                    }
                });

                endTransactionAlert.dismiss();
                positiveAlert();
            }
        });
        okCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrHandle.increaseEndUserScore(userName.trim(), .50);
                pendingRef = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser
                        .getEmail())
                        + "_" + "event_transactions");

                Query applesQuery = pendingRef.orderByChild("sent_to").equalTo(userName);

                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                            adapter.notifyDataSetChanged();
                            listView.invalidateViews();

                            completedTransactionAlert();
                        }
                        scrHandle.increaseEndUserScore(userName.trim(), .50);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }

                });

                endTransactionAlert.dismiss();
                positiveAlert();
            }
        });
        badCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrHandle.increaseEndUserScore(userName.trim(), -.50);
                pendingRef = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser
                        .getEmail())
                        + "_" + "event_transactions");

                Query applesQuery = pendingRef.orderByChild("sent_to").equalTo(userName);

                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                            adapter.notifyDataSetChanged();
                            listView.invalidateViews();

                            completedTransactionAlert();
                        }
                        scrHandle.increaseEndUserScore(userName.trim(), -.50);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                endTransactionAlert.dismiss();
                negativeAlert();
            }
        });
        terribleCompletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrHandle.increaseEndUserScore(userName.trim(), -1);
                pendingRef = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser.getEmail()) + "_" + "event_transactions");

                Query applesQuery = pendingRef.orderByChild("sent_to").equalTo(userName);

                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                            adapter.notifyDataSetChanged();
                            listView.invalidateViews();

                            completedTransactionAlert();
                        }
                        scrHandle.increaseEndUserScore(userName.trim(), -1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Log.e("failed", "onCancelled", databaseError.toException());
                    }
                });

                endTransactionAlert.dismiss();
                negativeAlert();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTransactionAlert.dismiss();
            }
        });
        endTransactionAlert.show();
    }

    /**This method displays a thank you to the user for their support in using the app*/
    public void positiveAlert(){
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

        confirm.setTitle("THANKS!");
        confirm.setMessage("Clout is still young. Your support really helps us grow.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                finish();
                startActivity(getIntent());
            }
        });
        confirm.show();
    }

    /**This method displays a thank you to the user for their support in using the app*/
    public void negativeAlert(){
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

        confirm.setTitle("THANKS!");
        confirm.setMessage("Sorry it went so poorly for you. We'll be sure to decrease the users score. " +
                "So others will know to watch out.");

        MaterialButton button = new MaterialButton(MainActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                finish();
                startActivity(getIntent());
            }
        });
        confirm.show();
    }

    public void eventTrasnactionsListView(){
        Collections.reverse(arrayList);

        mEventTransactionsList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("forLoopStart", "Start");
                //Log.d("forLoopStart", "start" + " : " + snapshot);

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    String desc = snapshot1.child("description").getValue(String.class);
                    String email = snapshot1.child("sent_to").getValue(String.class);
                    String allData = desc + "\n\n" + email;

                    //Log.d("logValue", "" + allData);
                    arrayList.add(allData);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void eventTransactionsReceivedListView(){
        mEventReceivedTransactionList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("forLoopStart", "Start");
                //Log.d("forLoopStart", "start" + " : " + snapshot);

                Collections.reverse(arrayListReceived);

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    //Log.d("logValue", "" + snapshot1.child("accepted_event").getValue(String.class));
                    arrayListReceived.add(snapshot1.child("accepted_event").getValue(String.class));
                    adapterReceived.notifyDataSetChanged();
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
                //Log.d("value", "" + value);
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

    /**When the user taps on a RecyclerView item they will be prompted with that users information
     * And a follow option will be granted apon accaptance. This may be removed as this may be
     * more information than general users should have access to.*/
    private void confirmationAlert(/*Selected users account name*/String usersName,
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
                //Log.d("ripUNCheck", ripUsersName);
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
    private static class tasksViewHolder extends RecyclerView.ViewHolder{

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
    private void initScoreHandling(){
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
    private void scoreGrab(){
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
                //Log.w("db_ref_failed", "Failed to read value.", error.toException());
            }
        });

    }
    /***/

    /**When the scorehandler/cloutScore button is tapped go CreateNewSessionStart activity*/
    private void onClickGoToCreateNewSessionActivity(){
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
    private void cashButtonHandle(){
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
                        //Log.d("CardSuccess", "Value is: " + value);

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
                        //Log.w("Fail", "Failed to read value.", error.toException());
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
                    //Log.d("Success", "Value is: " + value);
                    money.setText(value);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w("Fail", "Failed to read value.", error.toException());
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
                String key = dataSnapshot.getKey();
                usernameTextView.setText(key);
                //Log.d("getUNameOnStartTest: ", "Value is: " + value + " : " + getAccountKeyRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).getKey());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w("getUNOnStartTest : ", "Failed to read value.", error.toException());
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
    private void firstReturnAlertBlock1(){
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
    private void firstReturnAlertBlock2(){
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
    private void firstReturnAlertBlock3(){
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
                        //Log.d("value", "" + value);
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
        confirm.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("My Dialog");

        builder.setMessage("Check out the transition!");

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("Create A Transaction?");
        confirm.setMessage("What kind of transaction would you like to create?");

        MaterialButton eventTransButton = new MaterialButton(MainActivity.this);
        MaterialButton fundedMoneyTransaction = new MaterialButton(MainActivity.this);
        MaterialButton PreFundedMoneyTransaction = new MaterialButton(MainActivity.this);

        MaterialButton cancelButton = new MaterialButton(MainActivity.this);

        eventTransButton.setText(R.string.eventTransaction);
        PreFundedMoneyTransaction.setText(R.string.nonFunded);
        fundedMoneyTransaction.setText(R.string.funded);
        cancelButton.setText(R.string.cancel);

        eventTransButton.setBackgroundResource(R.color.colorPrimary);
        PreFundedMoneyTransaction.setBackgroundResource(R.color.colorPrimary);
        fundedMoneyTransaction.setBackgroundResource(R.color.colorPrimary);
        cancelButton.setBackgroundResource(R.color.colorPrimary);

        layout.addView(eventTransButton);
        layout.addView(PreFundedMoneyTransaction);
        layout.addView(fundedMoneyTransaction);
        layout.addView(cancelButton);

        eventTransButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO - Create EventTransactionActivity
                confirm.dismiss();
                Intent passToEventTransaction = new Intent(MainActivity.this, EventTransactionActivity.class);
                startActivity(passToEventTransaction);
            }
        });

        PreFundedMoneyTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
                Intent passToNonFundedMoneyTransactionActivity = new Intent(MainActivity.this, PreFundedMoneyTransaction.class);
                startActivity(passToNonFundedMoneyTransactionActivity);
            }
        });

        fundedMoneyTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //T.O.D.O
                // An alert that informs the user that the payments system is not yet ready.
                // DONE!
                confirm.dismiss();
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
    private void openAnimations(){
        ObjectAnimator animProfileImage = ObjectAnimator.ofFloat(profileImage, "translationX", 30f);
        animProfileImage.setDuration(500);
        animProfileImage.start();

        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(money, "translationY", -90f);
        animCashButton.setDuration(500);
        animCashButton.start();

        /**This below block will be kept because the recycler view will be returned to the app when funds
         * are built out*/
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
            Glide.with(MainActivity.this).load(profileImage).into(profileImage);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
    }
    /***/

    /**This alert will report to the user what the details of an recylerView list item are when tapped*/
    private void transactionDetialsAlert(/* Get the date from the selected viewHolder */ String date,
            /* Get the rate from the selected viewHolder */ String rate,
            /* Get the description from the selected viewHolder */ String description,
            /*get username from transacting users*/ String TransactingUsers){

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
                //Log.d("reversedLog", transUserString);
                mVal.child(transUserString).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = snapshot.child("Email").getValue(String.class);
                        String Score = snapshot.child("Score").getValue(String.class);
                        //Log.d("checkForEmail", "" + email);
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
    private void firstTimeCloutScoreOnClick(){
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
                        //Log.d("value", "" + value);
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
    private void firstimeCloutScoreOnClickAlert1(){
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
    private void firstimeCloutScoreOnClickAlert2(){
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
    private void firstimeCloutScoreOnClickAlert3(){
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
    private void firstimeCloutScoreOnClickAlert4(){
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
                        //Log.d("value", "" + value);
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

    /**This alert will let the user know the transaction was completed*/
    private void completedTransactionAlert(){
        final AlertDialog whoAlert = new MaterialAlertDialogBuilder(MainActivity.this).create();

        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        whoAlert.setView(layout);

        whoAlert.setCancelable(false);

        whoAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = whoAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        whoAlert.setTitle("Completed!");
        whoAlert.setMessage("This transaction was marked as completed. Thank you!");

        MaterialButton confirmButton = new MaterialButton(MainActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whoAlert.dismiss();
            }
        });
        whoAlert.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.apply();
            topLevel.setVisibility(View.VISIBLE);
            topLevel.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    clickedamount = clickedamount+1;
                    if(clickedamount == 1){
                        cloutintro1.setVisibility(View.GONE);
                        cloutintro2.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                    }
                    if(clickedamount == 2){
                        cloutintro2.setVisibility(View.GONE);
                        cloutintro3.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                    }
                    if(clickedamount == 3){
                        cloutintro3.setVisibility(View.GONE);
                        cloutintro4.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                    }
                    if(clickedamount == 4){
                        cloutintro4.setVisibility(View.GONE);
                        profileintro1.setVisibility(View.VISIBLE);
                        profileimage2.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                    }
                    if(clickedamount == 5){
                        topLevel.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                }

            });
        }
        return ranBefore;
    }
    public void introLoop(){
        topLevel.setVisibility(View.VISIBLE);
        topLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedamount = clickedamount + 1;

                if (clickedamount == 1) {
                    cloutintro1.setVisibility(View.GONE);
                    cloutintro2.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                }
                if (clickedamount == 2) {
                    cloutintro2.setVisibility(View.GONE);
                    cloutintro3.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                }
                if (clickedamount == 3) {
                    cloutintro3.setVisibility(View.GONE);
                    cloutintro4.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                }
                if (clickedamount == 4) {
                    cloutintro4.setVisibility(View.GONE);
                    profileintro1.setVisibility(View.VISIBLE);
                    profileimage2.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                }
                if (clickedamount == 5) {
                    topLevel.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Clicked Amount : " + clickedamount, Toast.LENGTH_SHORT).show();
                    cloutScore.setEnabled(true);
                }
            }
        });
    }
}