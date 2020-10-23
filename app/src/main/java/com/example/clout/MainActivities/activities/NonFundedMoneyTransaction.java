package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Emailv31;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

//TODO Firebase Database paths must not contain '.', '#', '$', '[', or ']

public class NonFundedMoneyTransaction extends AppCompatActivity {

    TextInputLayout whoEditText;
    EditText cashAmountEditText;
    TextView cashAmountTextView;
    MaterialButton cancelButton;
    Button submitButton;
    ScoreHandler scoreHandler = new ScoreHandler();

    //Firebase declare
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabaseRef;
    AccountKeyManager accKey = new AccountKeyManager();

    DatabaseReference mVal, mEventTransactionFirebaseReceived, notifications, mEventTransactionFirebaseSubmitted, usersNotify;
    private Object AsyncEmailTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_funded_money_transaction);

        mDatabaseRef = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        submitButton = findViewById(R.id.submitButton);
        whoEditText = findViewById(R.id.endUserNameEditText);
        cashAmountEditText = findViewById(R.id.cashAmountEditText);
        cashAmountTextView = findViewById(R.id.cashAmountTextView);
        cancelButton = findViewById(R.id.cancelButton);
        amountUpdater();
        cancelButtonOnClick();

        lentToWhoAlert();
        //sendEmail();
        submitButtonOnClick();
        new AsyncEmailTask().execute(AsyncEmailTask);
    }

    /**When the back button was being pressed, the user would be taken back to the launch screen
     * with no animation and the intent was killed so the user wasn't being returned to the */
    @Override
    public void onBackPressed() {
        //...do nothing
    }
    /**cancelbutton onclick listener*/
    public void cancelButtonOnClick(){
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMain = new Intent(NonFundedMoneyTransaction.this, MainActivity.class);
                startActivity(toMain);
            }
        });
    }
    /**Submit Button onClick*/
    public void submitButtonOnClick(){
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NonFundedMoneyTransaction.this, "test: " + whoEditText.getEditText().getText().toString(), Toast.LENGTH_SHORT).show();
                checkFirebaseUsername(whoEditText);
            }
        });
    }
    class AsyncEmailTask extends AsyncTask {
        @SuppressLint("WrongThread")
        @Override
        protected Object doInBackground(Object[] objects) {
            MailjetClient client;
            MailjetRequest request = null;
            MailjetResponse response = null;
            client = new MailjetClient(System.getenv("68f4b667a612106de8c8a74ff2a36f7c"), System.getenv("80e8bcf9f3b3ad2e3a753bf04f020214"), new ClientOptions("v3.1"));
            try {
                request = new MailjetRequest(Emailv31.resource)
                        .property(Emailv31.MESSAGES, new JSONArray()
                                .put(new JSONObject()
                                        .put(Emailv31.Message.FROM, new JSONObject()
                                                .put("Email", "moabcompanies@gmail.com")
                                                .put("Name", "James"))
                                        .put(Emailv31.Message.TO, new JSONArray()
                                                .put(new JSONObject()
                                                        .put("Email", "moabcompanies@gmail.com")
                                                        .put("Name", "James")))
                                        .put(Emailv31.Message.SUBJECT, "Greetings from Mailjet.")
                                        .put(Emailv31.Message.TEXTPART, "My first Mailjet email")
                                        .put(Emailv31.Message.HTMLPART, "<h3>Dear passenger 1, welcome to <a href='https://www.mailjet.com/'>Mailjet</a>!</h3><br />May the delivery force be with you!")
                                        .put(Emailv31.Message.CUSTOMID, "AppGettingStartedTest")));
                Log.d("email", ": " + "sent");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                response = client.post(request);
            } catch (MailjetException e) {
                e.printStackTrace();
            } catch (MailjetSocketTimeoutException e) {
                e.printStackTrace();
            }
            System.out.println(response.getStatus());
            System.out.println(response.getData());
            return null;
        }
    }

    /**Method updates the textview with the amount of cash the user is transacting*/
    private void amountUpdater(){
        cashAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(cashAmountEditText.getText().toString().equals("")){
                    cashAmountEditText.setText("0");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                cashAmountTextView.setText(MessageFormat.format("{0}{1}", getString(R.string.money_sign), String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(cashAmountEditText.getText().toString()) / 100)), TextView.BufferType.EDITABLE);
                //TODO: cause textView to decrease size when divisible by 1000
                if(Double.parseDouble(cashAmountEditText.getText().toString()) % 10000 != 0 ||
                        Double.parseDouble(cashAmountEditText.getText().toString()) % 10000 == 0){
                    cashAmountTextView.setTextSize(30);
                    cashAmountTextView.setTextColor(getResources().getColor(R.color.design_default_color_primary));
                }else{
                    cashAmountTextView.setTextSize(40);
                }

                if(Double.parseDouble(cashAmountEditText.getText().toString()) >= 200000){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        cashAmountEditText.setTextColor(Color.RED);
                    }
                    cashAmountTextView.setText(R.string.overAmount);
                }else{
                    cashAmountTextView.setTextColor(Color.GRAY);
                }
            }
        });
    }

    public void sendEmail(){
        Log.i("Send email", "");

        String[] TO = {"someone@gmail.com"};
        String[] CC = {"xyz@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished email...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(NonFundedMoneyTransaction.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkFirebaseUsername(TextInputLayout editTextVal){
        //Connect to firebase
        mDatabaseRef = FirebaseDatabase.getInstance();
        mVal = mDatabaseRef.getReference("Users");

        //Change editTextVal to string
        String editTextToString = Objects.requireNonNull(editTextVal.getEditText()).getText().toString().toLowerCase().trim();

        if(editTextToString.contains("&")){
            // Read from the database
            mVal.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                    String snappedEmail = String.valueOf(dataSnapshot.child("Email").getValue());
                    //Log.d("Check Email ", "" + snappedEmail);
                    String accKeySanpped = accKey.createAccountKey(snappedEmail);

                    if(editTextToString.trim().equals(accKey.createAccountKey(mCurrentUser.getEmail()))){
                        //Alert the user that they can't send transactions to themselves.
                        //Toast.makeText(NonFundedMoneyTransaction.this, "Test 1", Toast.LENGTH_SHORT).show();
                        checkForSelfAlert();
                    }else if(!dataSnapshot.hasChild(editTextToString)){
                        //Toast.makeText(NonFundedMoneyTransaction.this, "Test 3", Toast.LENGTH_SHORT).show();

                        //Log.d("Match", "Match Not Found");
                        //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + editTextVal.getText().toString());
                        //alert user there is no matching user
                        noMatchingUserAlert();
                    }
                    //This for-loop needs to check the entered value against the users in the database for accountkeys
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String snappedEmailLoop = String.valueOf(snapshot.child("Email").getValue());
                        //Log.d("Check Email ", "" + snappedEmailLoop);
                        String accKeySanppedLoop = accKey.createAccountKey(snappedEmailLoop);

                        //Check to make sure the user isn't making a transaction with themselves
                        if(editTextToString.trim().equals(accKeySanppedLoop)
                                && !editTextToString.trim().equals(accKey.createAccountKey(mCurrentUser.getEmail()))){
                            //Log.d("Match", "Match Found");
                            //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + editTextVal.getText().toString());
                            //Toast.makeText(NonFundedMoneyTransaction.this, "Test 2", Toast.LENGTH_SHORT).show();

                            //We will report the deed transaction to firebase
                            calViewAlert();
                            break;
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
    public void checkForSelfAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(NonFundedMoneyTransaction.this).create();

        LinearLayout layout = new LinearLayout(NonFundedMoneyTransaction.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_warning_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("Hmmm?");
        noMatchAlert.setMessage("You can not make a deed transaction with yourself.");

        MaterialButton confirmButton = new MaterialButton(NonFundedMoneyTransaction.this);
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
    public void noMatchingUserAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(NonFundedMoneyTransaction.this).create();

        LinearLayout layout = new LinearLayout(NonFundedMoneyTransaction.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_mood_bad_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("One Sec!");
        noMatchAlert.setMessage("There were no matching users with that &Accountname");

        MaterialButton confirmButton = new MaterialButton(NonFundedMoneyTransaction.this);
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
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(NonFundedMoneyTransaction.this).create();

        LinearLayout layout = new LinearLayout(NonFundedMoneyTransaction.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("Match");
        noMatchAlert.setMessage("There was a matching user with that &Accountname");

        MaterialButton confirmButton = new MaterialButton(NonFundedMoneyTransaction.this);
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
    public void calViewAlert(){
        final AlertDialog noMatchAlert = new MaterialAlertDialogBuilder(NonFundedMoneyTransaction.this).create();

        LinearLayout layout = new LinearLayout(NonFundedMoneyTransaction.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        noMatchAlert.setView(layout);

        noMatchAlert.setCancelable(false);

        noMatchAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = noMatchAlert.getWindow();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        noMatchAlert.setTitle("WHEN");
        noMatchAlert.setMessage("What date should you be paid back?");

        CalendarView calView = new CalendarView(NonFundedMoneyTransaction.this);
        MaterialButton confirmButton = new MaterialButton(NonFundedMoneyTransaction.this);
        TextView whenTextView = new TextView(NonFundedMoneyTransaction.this);
        whenTextView.setText("...");
        whenTextView.setTextSize(20);
        whenTextView.setGravity(Gravity.CENTER_HORIZONTAL);

        confirmButton.setText(R.string.confirm);
        confirmButton.setBackgroundResource(R.color.colorPrimary);

        layout.addView(whenTextView);
        layout.addView(calView);
        layout.addView(confirmButton);

        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String dateSelected = year + "/" + (month+1) + "/" + dayOfMonth;
                whenTextView.setText(dateSelected);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noMatchAlert.dismiss();
                assert mCurrentUser != null;

                notifications = mDatabaseRef.getReference(Objects.requireNonNull(whoEditText.getEditText()).getText().toString().trim() + "_" + "Pending_Notifications").push();
                usersNotify = mDatabaseRef.getReference("Users_Notifications").child(accKey.createAccountKey(mCurrentUser.getEmail())).push();

                mEventTransactionFirebaseSubmitted = mDatabaseRef.getReference(accKey.createAccountKey(mCurrentUser.getEmail()).trim() + "_" + "event_transactions").push();

                mEventTransactionFirebaseReceived = mDatabaseRef.getReference(String.valueOf(whoEditText.getEditText().getText().toString()).trim() + "_" + "event_received").push();

                /**This sequence will generate a firebase realtime database node that will house
                 * the 'Submitted' children in the database
                 * @// TODO: 10/19/20 This sequence needs to be changed to push a submitted node
                 * But should leave the request node to be sent to the receiver.*/
                mEventTransactionFirebaseSubmitted.child("sent_to").setValue(String.valueOf(whoEditText.getEditText().getText()));
                mEventTransactionFirebaseSubmitted.child("description").setValue(Objects.requireNonNull(cashAmountTextView.getText().toString()));
                mEventTransactionFirebaseSubmitted.child("end_date").setValue(whenTextView.getText().toString());
                scoreHandler.sessionStartScoreIncrease(.25);

                /**Send user notification of score increase*/
                usersNotify.child("notify").setValue("Great job! You're score increased by .25 points!");

                /**This sequence will generate a firebase realtime database node that will house
                 * the 'Received' children in the database, it will be sent to the pending notifications
                 * view for approval from the requested user*/
                notifications.child("description").setValue(cashAmountTextView.getText().toString());
                notifications.child("sentFrom").setValue(accKey.createAccountKey(mCurrentUser.getEmail()));
                notifications.child("enddate").setValue(whenTextView.getText().toString());
                notifications.child("read_status").setValue("Unread");

                /**The score will be increase after both submitted and received nodes are sent to firebase*/
                scoreHandler.sessionStartScoreIncrease(.25);

                /**Next we need to dismiss the Alert and return the user to the MainActivity
                 * returning the user also has the added benefit not allowing the user to be able
                 * to create multiple transactions from the same instance.  */
                noMatchAlert.dismiss();
                Intent toMain = new Intent(NonFundedMoneyTransaction.this, MainActivity.class);
                startService(new Intent(NonFundedMoneyTransaction.this, dateReached.class));

                startActivity(toMain);
            }
        });
        noMatchAlert.show();
    }

    /**Alert the user at every start that this transaction type will allow them to report who they've
     * already lent money to.*/
    public void lentToWhoAlert(){
        final AlertDialog whoAlert = new MaterialAlertDialogBuilder(NonFundedMoneyTransaction.this).create();

        LinearLayout layout = new LinearLayout(NonFundedMoneyTransaction.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        whoAlert.setView(layout);

        whoAlert.setCancelable(false);

        whoAlert.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = whoAlert.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        whoAlert.setTitle("WHO?");
        whoAlert.setMessage("This Deed Transaction type is for reporting what users you've sent physical " +
                "or digital cash to. Don't forget, use the users &AccountName to report on them. For example, " +
                "your &AccountName is" + "\n" + accKey.createAccountKey(mCurrentUser.getEmail()));

        MaterialButton confirmButton = new MaterialButton(NonFundedMoneyTransaction.this);
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

}