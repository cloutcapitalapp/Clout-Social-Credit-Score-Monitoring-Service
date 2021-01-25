package com.miware.clout.SourceCode.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.miware.clout.SourceCode.Classes.AccountKeyManager;
import com.miware.clout.SourceCode.Classes.ScoreHandler;
import com.miware.clout.SourceCode.Classes.dateReached;
import com.miware.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class EventTransactionActivity extends AppCompatActivity {
    String snapshotString;

    //boolean
    private boolean descPass = false;
    private boolean didPass;

    //View
    private View barSeperator;

    //TextView
    private TextView whenTextView;

    //String
    private String datePicked, newString;

    //Firebase declare
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabaseRef;
    private DatabaseReference mVal, mEventTransactionFirebaseReceived,
            notifications, mEventTransactionFirebaseSubmitted, usersNotify;

    //Classes
    private final AccountKeyManager accKey = new AccountKeyManager();
    private final ScoreHandler scoreHandler = new ScoreHandler();

    //spinner
    private Spinner eventSpinner;

    //Buttons
    private Button submitTransactionsButton;

    //Image Button
    private ImageView infoImageView;

    //EditText
    private EditText editTextTextPersonName;
    private TextInputLayout description;

    //CalannderView
    private CalendarView calView;
    private Date date;
    private ImageButton imageLessonTag;

    /**This method will house the Events Transaction code
     * Creating this event transaction should create an entry into firebase that passes
     * data to the MainActivity recycler view*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datePicked = null;
        setContentView(R.layout.activity_event_transaction);

        //init applications vars
        initVars();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        eventSpinner.setAdapter(adapter);

        //Disable submit button until checks are met
        submitTransactionsButton.setEnabled(false);

        //Check spinner item selected
        spinnerItemSelected();

        //grab extras from UserProfile
        newString = null;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
            } else {
                newString= extras.getString("username");
                Toast.makeText(this, "" + newString, Toast.LENGTH_SHORT).show();
                editTextTextPersonName.setText(newString);
            }
        } else {
            newString= (String) savedInstanceState.getSerializable("username");
        }

        /*ImageView button handler handles info image view*/
        ImageTagOnClick();

        //text watcher
        checkUsername();

        //opening animations
        openingAnimation();

        //Methods
        submitTransactionButtonOnClick();
    }
    @Override
    public void onStart(){
        super.onStart();
        onStartAlert();
    }

    private void initVars(){
        //Image View
        infoImageView = findViewById(R.id.infoImageView);
        //View
        barSeperator = findViewById(R.id.barSeperator);
        //TextView
        whenTextView = findViewById(R.id.textView4);
        //CalanderView
        calView = findViewById(R.id.dateCalView);
        //Buttons
        submitTransactionsButton = findViewById(R.id.submitEventTransactionButton);
        //EditText
        description = findViewById(R.id.descEditText);
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
        //Spinner
        eventSpinner = findViewById(R.id.eventTypeSpinner9);

        //firebase
        mDatabaseRef = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        assert mCurrentUser != null;
        mEventTransactionFirebaseSubmitted = mDatabaseRef
                .getReference(accKey
                        .createAccountKey(mCurrentUser.getEmail()).trim() + "_"
                        + "event_transactions").push();
    }

    /**At start, the user should be shown an Alert that displays the purpose of this Deed Transaction.*/
    private void onStartAlert(){
        final AlertDialog whoAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout =
                new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        whoAlert.setView(layout);

        whoAlert.setCancelable(false);

        whoAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = whoAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        whoAlert.setTitle("Event Transactions");
        whoAlert.setMessage("This Deed Transaction type is for Events. " +
                "If you'd like to report a Date, Meeting, Party etc. You'll report that in this " +
                "transaction. Don't forget, use the users &AccountName to report on them. For example, " +
                "your &AccountName is" + "\n" + accKey.createAccountKey(mCurrentUser.getEmail()));

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
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

    /**This onClick handler should take the
     * @param // TODO: 10/15/20 onClickCheckTransactionsInfo() method*/
    /**START*/
    private void submitTransactionButtonOnClick()
    {
        //Check date
        dateSelected();
        submitTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckTransactionsInfo();
            }
        });
    }

    public void backToMain (View view){
        Intent toMain = new Intent(EventTransactionActivity.this,
                MainActivity.class);
        startActivity(toMain);
    }

    /**What needs to be checked?
     * &Accountname needs to be checked against firebase to make sure the user is present
     * Spinner needs to be checked for the item which is selected
     * Description must be checked for entry
     * and CalView needs to be checked for an entered date that isn't before the current date
     * */
    private void onClickCheckTransactionsInfo(){
        //matchingUserAlert();
        DatabaseReference mSubmitted = mDatabaseRef.getReference(
                accKey.createAccountKey(mCurrentUser.getEmail())+"_event_transactions");
        ValueEventListener mSubmitedCheck = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotMain) {
                if(descriptionCheck(Objects.requireNonNull(description
                        .getEditText()).getText().toString())
                        && (!whenTextView.getText().toString().equals("When should this be done?"))
                        && (eventSpinner.getSelectedItemPosition() != 0)){
                    if(!snapshotMain.exists()){
                        goodDeedAlert();
                    }else{
                        Query mSubmitterQuery = mSubmitted.orderByChild("sent_to");
                        mSubmitterQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot query : snapshot.getChildren()) {
                                    String value = (String) query.child("sent_to").getValue();
                                    //System.out.println("Val query check " + value);
                                    assert value != null;
                                    if(value.equals(editTextTextPersonName
                                            .getText().toString().trim())){
                                        /**If snapshot is not null we will check
                                         * to make sure the entered value is not a current
                                         * sent_to value*/
                                        noRepeatAlert();
                                    }else if(!value.equals(editTextTextPersonName
                                            .getText().toString().trim())){
                                        noRepeatAlert();
                                        //goodDeedAlert();
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }else{
                    Toast
                            .makeText(EventTransactionActivity.this,
                                    "Please make sure all fields are entered",
                                    Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mSubmitted.addListenerForSingleValueEvent(mSubmitedCheck);
    }

    private void noRepeatAlert(){
        final AlertDialog noMatchAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_warning_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("Hmmm?");
        noMatchAlert.setMessage("For now, you can only submit one transaction at a time." +
                "Complete the one you have, and you can create a new one.");

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
                noMatchAlert.dismiss();
            }
        });
        noMatchAlert.show();
    }

    /**When the back button was being pressed, the user would be taken back to the launch screen
     * with no animation and the intent was killed so the user wasn't being returned to the */
    @Override
    public void onBackPressed() {
        //...do nothing
    }

    /**This alert will report to the user that they've committed a deed transaction and then creates
     * a notification that alerts the user the score increase amount they've received.*/
    private void goodDeedAlert(){
        final AlertDialog noMatchAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_airplanemode_active_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("AWESOME!");
        noMatchAlert.setMessage("Creating deed transactions in clout helps everyone build their score. " +
                "Keep helping people around you and your score will skyrocket!");

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(this, "Pass", Toast.LENGTH_SHORT).show();
                date = Calendar.getInstance().getTime();
                assert mCurrentUser != null;

                notifications = mDatabaseRef.getReference(editTextTextPersonName
                        .getText().toString().trim() + "_" + "Pending_Notifications").push();
                usersNotify = mDatabaseRef.getReference("Users_Notifications")
                        .child(accKey.createAccountKey(mCurrentUser.getEmail())).push();

                mEventTransactionFirebaseReceived =
                        mDatabaseRef.getReference(String.valueOf(editTextTextPersonName
                                .getText()).trim() + "_" + "event_received").push();

                /*Send user notification of score increase*/
                usersNotify.child("notify").setValue("Great job! You're score increased by .25 points!");

                /*This sequence will generate a firebase realtime database node that will house
                 * the 'Received' children in the database, it will be sent to the pending notifications
                 * view for approval from the requested user*/
                notifications.child("description").setValue(description
                        .getEditText().getText().toString());
                notifications.child("sentFrom")
                        .setValue(accKey.createAccountKey(mCurrentUser.getEmail()));
                notifications
                        .child("enddate").setValue(whenTextView.getText().toString());
                notifications
                        .child("read_status").setValue("Unread");

                /*The score will be increase after both submitted and received nodes are sent to firebase*/
                scoreHandler.sessionStartScoreIncrease(.25);

                /*create alert that tells user we will manage the transaction and notify them*/
                willHandleAlert();

                /*Next we need to dismiss the Alert and return the user to the MainActivity
                 * returning the user also has the added benefit not allowing the user to be able
                 * to create multiple transactions from the same instance.  */
                noMatchAlert.dismiss();
            }
        });
        noMatchAlert.show();
    }
    private void willHandleAlert(){
        final AlertDialog noMatchAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("We got it");
        noMatchAlert.setMessage("We will alert the user and let you know if they accept " +
                "or reject your request");

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noMatchAlert.dismiss();
                Intent toMain = new Intent(EventTransactionActivity.this,
                        MainActivity.class);
                startService(new Intent(EventTransactionActivity.this,
                        dateReached.class));

                /*Add the notification to the notifications node
                 * @// FIXME: 10/19/20 @todo !!*/

                startActivity(toMain);
            }
        });
        noMatchAlert.show();
    }

    /**This method is a TextWatcher that reports to the user when they've typed the &AccountName of
     * a located user.*/
    private void checkUsername(){
        editTextTextPersonName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            private Handler myHandler;

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(editTextTextPersonName.getText().toString().contains("&")){
                    myHandler = new Handler();
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            /*Check for user in Us*/
                            DatabaseReference mTransactionReference = mDatabaseRef
                                    .getReference("Users").child(editTextTextPersonName
                                            .getText().toString().trim());
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()) {
                                        //... Do nothing
                                    }else{
                                        /*Once we find a user, we have to check to make sure
                                        the user isn't searching for themselves*/

                                        /*We will compare the entered value to the
                                        snapshot to know if this is the case*/
                                        if(String.valueOf(dataSnapshot)
                                                .equals(editTextTextPersonName
                                                        .getText().toString().trim())){
                                            checkForSelfAlert();
                                        }else{
                                            /*We've found a user and that user isn't self
                                             * We must now check to make sure the user don't
                                             * already have a transaction submitted*/
                                            matchingUserAlert();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            };
                            mTransactionReference.addListenerForSingleValueEvent(eventListener);
                        }
                    }, 100);
                }else{
                    Toast.makeText(EventTransactionActivity.this,
                            "Please make sure to use the & symbol before the username",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**Searched for self alert
     *
     */
    private void checkForSelfAlert(){
        final AlertDialog noMatchAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout =
                new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_warning_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("Hmmm?");
        noMatchAlert.setMessage("You can not make a deed transaction with yourself.");

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
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

    /**Alert for check user check*/
    private void noMatchingUserAlert(){
        final AlertDialog noMatchAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout =
                new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_mood_bad_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("One Sec!");
        noMatchAlert.setMessage("There were no matching users with that &Accountname");

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
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
    private boolean matchingUserAlert(){
        final AlertDialog noMatchAlert =
                new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("Match");
        noMatchAlert.setMessage("There was a matching user with that &Accountname");

        MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);
        layout.addView(confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTransactionsButton.setEnabled(true);
                noMatchAlert.dismiss();
            }
        });
        noMatchAlert.show();
        return true;
    }

    /**This method will check for the selected spinner value*/
    private void spinnerItemSelected(){
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                spinnerItemSelectedConditions(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }
    private void spinnerItemSelectedConditions(int retrievedID){
        if(retrievedID == 0){
            Toast.makeText(this, "Please choose an Event Type",
                    Toast.LENGTH_SHORT).show();
        }else if(retrievedID == 1){
            Toast.makeText(EventTransactionActivity.this, "Meeting",
                    Toast.LENGTH_SHORT).show();
        }else if(retrievedID == 2){
            Toast.makeText(EventTransactionActivity.this, "Date",
                    Toast.LENGTH_SHORT).show();
        }else if(retrievedID == 3){
            Toast.makeText(EventTransactionActivity.this, "Party",
                    Toast.LENGTH_SHORT).show();
        }else if(retrievedID == 4){
            Toast.makeText(EventTransactionActivity.this, "Other",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**Description needs to be checked to make sure it is valid
     * rule 1 - Must be over 50 characters in length
     * @return*/
    private boolean descriptionCheck(String descriptionString){
        if(descriptionString.length() <= 10
                && (descriptionString.length() >= 20)){
            Toast.makeText(this, "Description must be at least 10 characters, " +
                    "but not more than 20", Toast.LENGTH_SHORT).show();
        }else {
            //Toast.makeText(this, "Description pass", Toast.LENGTH_SHORT).show();
            descPass = true;
        }
        return descPass;
    }

    /**Calander view listener
     * When a date is selected the 'When' textview should set its' text to the chosen date.
     *
     * @param // */
    private void dateSelected(){
        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year,
                                            int month, int dayOfMonth) {
                String dateSelected = year + "/" + (month+1) + "/" + dayOfMonth;
                whenTextView.setText(dateSelected);
            }
        });
    }

    /**This method houses the onCreate animations for this activity*/
    private void openingAnimation(){
        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(barSeperator,
                "translationY", -80f);
        animCashButton.setDuration(500);
        animCashButton.start();
    }
    private void ImageTagOnClick(){
        infoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagAlert();
            }

            private void tagAlert() {
                final AlertDialog tagAlert =
                        new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();
                tagAlert.setTitle("What you should know!");
                tagAlert.setIcon(R.drawable.ic_baseline_warning_24);
                tagAlert.setMessage("This deed transaction will be used to record events with other users. " +
                        "If you'd like to record how successful a date, a meeting a party ... etc... " +
                        "then this is the deed transaction you'll need to use.");

                LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                MaterialButton confirmButton = new MaterialButton(EventTransactionActivity.this);
                MaterialButton cancelButton = new MaterialButton(EventTransactionActivity.this);

                confirmButton.setText(R.string.confirm);
                cancelButton.setText(R.string.cancel);

                layout.addView(confirmButton);
                layout.addView(cancelButton);

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tagAlert.dismiss();
                    }
                });
                tagAlert.show();
            }
        });
    }
}