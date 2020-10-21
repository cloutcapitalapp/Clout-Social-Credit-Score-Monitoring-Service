package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**This method is ready for MVP*/
public class EventTransactionActivity extends AppCompatActivity {

    //boolean
    boolean descPass = false;

    //View
    View barSeperator;

    //TextView
    TextView whenTextView;

    //String
    String datePicked, newString;

    //Firebase declare
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabaseRef;
    DatabaseReference mVal, mEventTransactionFirebaseReceived, notifications, mEventTransactionFirebaseSubmitted, usersNotify;

    //Classes
    AccountKeyManager accKey = new AccountKeyManager();
    ScoreHandler scoreHandler = new ScoreHandler();

    //spinner
    Spinner eventSpinner;

    //Buttons
    Button submitTransactionsButton;

    //EditText
    EditText editTextTextPersonName;
    TextInputLayout description;

    //CalannderView
    CalendarView calView;
    private Date date;

    /**This method will house the Events Transaction code
     * Creating this event transaction should create an entry into firebase that passes
     * data to the MainActivity recycler view*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datePicked = null;
        setContentView(R.layout.activity_event_transaction);

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
        eventSpinner = (Spinner) findViewById(R.id.eventTypeSpinner9);

        //firebase
        mDatabaseRef = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        assert mCurrentUser != null;
        mEventTransactionFirebaseSubmitted = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser.getEmail()).trim() + "_" + "event_transactions").push();

        //TODO this line may no longer be needed.
        //mEventSubmitted = mDatabaseRef.getReference(editTextTextPersonName.getText().toString() + "_" + "event_submitted").push();

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

        //text watcher
        checkUsername();

        //opening animations
        openingAnimation();

        //Methods
        submitTransactionButtonOnClick();
    }

    /**This onClick handler should take the
     * @param // TODO: 10/15/20 onClickCheckTransactionsInfo() method*/
    /**START*/
    private void submitTransactionButtonOnClick(){
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
        Intent toMain = new Intent(EventTransactionActivity.this, MainActivity.class);
        startActivity(toMain);
    }
    /**What needs to be checked?
     * &Accountname needs to be checked against firebase to make sure the user is present
     * Spinner needs to be checked for the item which is selected
     * Description must be checked for entry
     * and CalView needs to be checked for an entered date that isn't before the current date
     * */
    private void onClickCheckTransactionsInfo(){
        if(descriptionCheck(Objects.requireNonNull(description.getEditText()).getText().toString())
        && (!whenTextView.getText().toString().equals("When should this be done?"))
        && (eventSpinner.getSelectedItemPosition() != 0)){
            goodDeedAlert();
        }else{
            Toast.makeText(this, "Did not pass", Toast.LENGTH_SHORT).show();
        }
    }

    /**When the back button was being pressed, the user would be taken back to the launch screen
     * with no animation and the intent was killed so the user wasn't being returned to the */
    @Override
    public void onBackPressed() {
        //...do nothing
    }

    /**This alert will report to the user that they've committed a deed transaction and then creates
     * a notification that alerts the user the score increase amount they've recieved.*/
    public void goodDeedAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("AWESOME!");
        noMatchAlert.setMessage("Creating deed transactions in clout helps everyone build their score. " +
                "Keeping helping people around you and your score will skyrocket!");

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

                notifications = mDatabaseRef.getReference(editTextTextPersonName.getText().toString().trim() + "_" + "Pending_Notifications").push();
                usersNotify = mDatabaseRef.getReference("Users_Notifications").child(accKey.createAccountKey(mCurrentUser.getEmail())).push();

                mEventTransactionFirebaseReceived = mDatabaseRef.getReference(String.valueOf(editTextTextPersonName.getText()).trim() + "_" + "event_received").push();

                /**This sequence will generate a firebase realtime database node that will house
                 * the 'Submitted' children in the database
                 * @// TODO: 10/19/20 This sequence needs to be changed to push a submitted node
                 * But should leave the request node to be sent to the receiver.*/
                mEventTransactionFirebaseSubmitted.child("sent_to").setValue(String.valueOf(editTextTextPersonName.getText()));
                mEventTransactionFirebaseSubmitted.child("description").setValue(Objects.requireNonNull(description.getEditText()).getText().toString());
                mEventTransactionFirebaseSubmitted.child("end_date").setValue(whenTextView.getText().toString());
                scoreHandler.sessionStartScoreIncrease(.25);

                /**Send user notification of score increase*/
                usersNotify.child("notify").setValue("Great job! You're score increased by .25 points!");

                /**This sequence will generate a firebase realtime database node that will house
                 * the 'Received' children in the database, it will be sent to the pending notifications
                 * view for approval from the requested user*/
                notifications.child("description").setValue(description.getEditText().getText().toString());
                notifications.child("sentFrom").setValue(accKey.createAccountKey(mCurrentUser.getEmail()));
                notifications.child("enddate").setValue(whenTextView.getText().toString());
                notifications.child("read_status").setValue("Unread");

                /**The score will be increase after both submitted and received nodes are sent to firebase*/
                scoreHandler.sessionStartScoreIncrease(.25);

                /**Next we need to dismiss the Alert and return the user to the MainActivity
                 * returning the user also has the added benefit not allowing the user to be able
                 * to create multiple transactions from the same instance.  */
                noMatchAlert.dismiss();
                Intent toMain = new Intent(EventTransactionActivity.this, MainActivity.class);
                startService(new Intent(EventTransactionActivity.this, dateReached.class));

                /**Add the notification to the notifications node
                 * @// FIXME: 10/19/20 @todo !!*/

                startActivity(toMain);
            }
        });
        noMatchAlert.show();
    }

    /**This method is a TextWatcher that reports to the user when they've typed the &Accountname of
     * a located user.*/
    public void checkUsername(){
        editTextTextPersonName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            private Handler myHandler;

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(editTextTextPersonName.getText().toString().contains("&")){
                    myHandler = new Handler();
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Check &Accountname
                            checkFirebaseUsername(editTextTextPersonName);
                            //Toast.makeText(EventTransactionActivity.this, "Checking2", Toast.LENGTH_SHORT).show();
                        }
                    }, 100);
                }
            }
        });
    }

    /**This method will run a check against all users in the firebase Users database
     * returns true if User is found
     * @return*/
    public void
    checkFirebaseUsername(EditText editTextVal){
        //Connect to firebase
        mDatabaseRef = FirebaseDatabase.getInstance();
        mVal = mDatabaseRef.getReference("Users");

        //Change editTextVal to string
        String editTextToString = editTextVal.getText().toString().toLowerCase().trim();

        if(editTextToString.contains("&")){
            // Read from the database
            mVal.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //This for-loop needs to check the entered value against the users in the database for accountkeys
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String snappedEmail = (String) snapshot.child("Email").getValue();
                        String accKeySanpped = accKey.createAccountKey(snappedEmail);

                        //Check to make sure the user isn't making a transaction with themselves
                        if(editTextToString.trim().equals(accKey.createAccountKey(mCurrentUser.getEmail()))){
                            //Alert the user that they can't send transactions to themselves.
                            checkForSelfAlert();
                        }else if(editTextToString.trim().equals(accKeySanpped)){
                            //Log.d("Match", "Match Found");
                            //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + editTextVal.getText().toString());
                            matchingUserAlert();
                            submitTransactionsButton.setEnabled(true);
                            break;
                        }else{
                            //Log.d("Match", "Match Not Found");
                            //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + editTextVal.getText().toString());
                            //alert user there is no matching user
                            //noMatchingUserAlert();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    //Log.w("UserCheck", "Failed to read value.", error.toException());
                }
            });
        }else{
            //Log.d("containsFail", "Fail");
        }
    }

    /**Searched for self alert
     *
     */
    public void checkForSelfAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_warning_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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
    public void noMatchingUserAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

        LinearLayout layout = new LinearLayout(EventTransactionActivity.this);
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
    public boolean matchingUserAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(EventTransactionActivity.this).create();

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
                noMatchAlert.dismiss();
            }
        });
        noMatchAlert.show();
        return true;
    }

    /**This method will check for the selected spinner value*/
    public void spinnerItemSelected(){
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spinnerItemSelectedConditions(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }
    public void spinnerItemSelectedConditions(int retrievedID){
        // your code here
        int getPosition = retrievedID;
        if(getPosition == 0){
            //do nothing
            Toast.makeText(this, "Please choose an Event Type", Toast.LENGTH_SHORT).show();
        }else if(getPosition == 1){
            Toast.makeText(EventTransactionActivity.this, "Meeting", Toast.LENGTH_SHORT).show();
        }else if(getPosition == 2){
            Toast.makeText(EventTransactionActivity.this, "Date", Toast.LENGTH_SHORT).show();
        }else if(getPosition == 3){
            Toast.makeText(EventTransactionActivity.this, "Party", Toast.LENGTH_SHORT).show();
        }else if(getPosition == 4){
            Toast.makeText(EventTransactionActivity.this, "Other", Toast.LENGTH_SHORT).show();
        }
        Log.d("getIdCheck", "" + (long) getPosition);
    }

    /**Description needs to be checked to make sure it is valid
     * rule 1 - Must be over 50 characters in length
     * @return*/
    public boolean descriptionCheck(String descriptionString){
        if(descriptionString.length() <= 50){
            Toast.makeText(this, "Description must be at least 50 characters", Toast.LENGTH_SHORT).show();
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
    public void dateSelected(){
        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String dateSelected = year + "/" + (month+1) + "/" + dayOfMonth;
                whenTextView.setText(dateSelected);
            }
        });
    }
    /**END*/

    /**This method houses the onCreate animations for this activity*/
    public void openingAnimation(){
        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(barSeperator, "translationY", -80f);
        animCashButton.setDuration(500);
        animCashButton.start();
    }
}