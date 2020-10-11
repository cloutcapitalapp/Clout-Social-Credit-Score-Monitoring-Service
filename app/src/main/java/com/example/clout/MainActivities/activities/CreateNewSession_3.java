package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.LocaleData;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.MainActivities.Classes.ScoreHandler;
import com.example.clout.MainActivities.Classes.SessionActivityID;
import com.example.clout.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

import static com.example.clout.MainActivities.Classes.notificationHandler.CHANNEL_1_ID;
import static com.example.clout.MainActivities.Classes.notificationHandler.CHANNEL_2_ID;

public class CreateNewSession_3 extends AppCompatActivity {

    private NotificationManagerCompat notificationManagerCompat;
    String receiver;
    TextView date;
    CalendarView calendarView;
    SessionActivityID sessionActivityID;
    Button fundsReturnedNoneRequired;
    ScoreHandler scrHandle;
    ImageButton continueButton;
    ImageButton backButton;
    AccountKeyManager accKey = new AccountKeyManager();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    DatabaseReference usersRef = mDataBase.getReference("Users");
    DatabaseReference transactionRef = mDataBase.getReference("User_Transactions");
    DatabaseReference notificationsRef = mDataBase.getReference("Users_Notifications");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_3);

        notificationManagerCompat = NotificationManagerCompat.from(this);
        sessionActivityID = new SessionActivityID();
        calendarView = findViewById(R.id.calendarView);
        date = findViewById(R.id.dateTextView);
        scrHandle = new ScoreHandler();
        fundsReturnedNoneRequired = findViewById(R.id.noReturnButton);
        continueButton = findViewById(R.id.continueArrow);
        backButton = findViewById(R.id.backArrow);

        calViewDateSelected();
        FundsNotRequiredForReturnMethod();
        openAnimations();
        continueButton();
    }
    protected void onStart(){
        super.onStart();
        getExtras();
    }
    private void getExtras(){
        Bundle intentExtras = getIntent().getExtras();
        assert intentExtras != null;
        receiver = intentExtras.getString("receiver");
        Log.d("recieverTest", "extras : " + receiver);
    }

    private void FundsNotRequiredForReturnMethod(){

        fundsReturnedNoneRequired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FundsNotRequiredForReturnAlert();
            }
        });

    }
    private void FundsNotRequiredForReturnAlert(){
        AlertDialog.Builder NotRequired = new AlertDialog.Builder(this);
        NotRequired.setTitle("Are You Sure?");
        NotRequired.setMessage("Accepting these terms means that you are not expecting a return " +
                "on the funds you are allowing the receiver to borrow. You will see a score increase " +
                "immediately, but please understand that you will NOT be repaid!");
        NotRequired.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If clicked, send user back to Main Activity
                // Then user should see an increase in their CS
                //

                scrHandle.sessionStartScoreIncrease(.50);
                LocalDate currentDate = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentDate = LocalDate.now(ZoneId.systemDefault());
                }


                //send notification of score increase to firebasde
                LocalDate finalCurrentDate = currentDate;
                notificationsRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        notificationsRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).push().child("Notify").setValue(finalCurrentDate + " : \n" + "Score Increased by .50! Great Job!");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Notification notification = new NotificationCompat.Builder(CreateNewSession_3.this, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_baseline_emoji_emotions_24)
                        .setContentTitle("HEY!")
                        .setContentText("Your Score INCREASED by .50 points! Great Job!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                notificationManagerCompat.notify(1, notification);

                Intent returnToMainActivity1 = new Intent(CreateNewSession_3.this, MainActivity.class);
                startActivity(returnToMainActivity1);
            }
        });

        NotRequired.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        NotRequired.create();
        NotRequired.show();
    }

    public void stage3Transaction(){
        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());

                transactionRef.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityID.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), /*This value needs to be the received extra*/ receiver, String.valueOf(currentDate)));
                transactionRef.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityID.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), /*This value needs to be the received extra*/ receiver, String.valueOf(currentDate)))
                        .child("date").setValue(date.getText().toString());
                transactionRef.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityID.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), /*This value needs to be the received extra*/ receiver, String.valueOf(currentDate)))
                        .child("current_date").setValue(String.valueOf(currentDate));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void openAnimations(){
        ObjectAnimator animForwardArrow = ObjectAnimator.ofFloat(continueButton, "translationX", 60f);
        animForwardArrow.setDuration(1700);
        animForwardArrow.start();

        ObjectAnimator animBackArrow = ObjectAnimator.ofFloat(backButton, "translationX", -60f);
        animBackArrow.setDuration(1700);
        animBackArrow.start();
    }
    public void continueButton(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                stage3Transaction();
                if(!date.getText().toString().equals("Date Selected")){

                    scrHandle.sessionStartScoreIncrease(.25);
                    LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());

                    //send notification of score increase to firebasde
                    notificationsRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            notificationsRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).push().child("Notify").setValue(currentDate+ " : \n" + "Score Increased by .25! Great Job!");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Notification notification = new NotificationCompat.Builder(CreateNewSession_3.this, CHANNEL_1_ID)
                            .setSmallIcon(R.drawable.ic_baseline_emoji_emotions_24)
                            .setContentTitle("HEY!")
                            .setContentText("Your Score INCREASED by .25 points! Great Job!")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .build();
                    notificationManagerCompat.notify(1, notification);
                    Intent returnToMain = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(returnToMain);
                }else{
                    //if no date is selected display the following Toast!
                    Toast.makeText(CreateNewSession_3.this, "Please selected a date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*** calViewDateSelected() - This method is will be used to create functionality for the calendar view
     * First we will create an setOnDateChangeListener which listens for the user tapping
     * Second - once the user selects different dates inside the calendar view the TextView
     *  above the View needs to change to the date selected
     * Third - The Current Date and the Date Selected must be compared to check whether or not the dates
     *  are equal to each other or if the date selected is less than the current date ***/
    public void calViewDateSelected(){
        /* The notable issue which came with this method was creating an algorithm which was
        * 1 - needed to create variables which housed both the dateSelected as well as the currentDate
        * 2 - Both values needed to be converted to Integers after being converted to Strings for the purpose of testing
        * 3 - Once the values were integers they needed to be logged for testing, also the currentDate
        *  is not a string or int in its' original form and needed to be compared with the dateSelected var
        *  the solution was to cast it to a string array with a String Builder
        *  and use the deleteCharAt() method to remove the 0 at the 5th index
        *  once that char is removed it can be rebuilt as a string so that the dashes (" - ")
        *  could be removed
        *  once the dashes were removed it could then be casted to an Integer to be compared to the
        *  dateSelected var
        * 4 - Once the testing is done an if-Statement is used to check if the removed char is 0
        *  if the character at index 4 is 0 the currentDate var which was converted to a string and
        *  became currentDatePureString to remove the dashes will be looped through by its length()
        *  This might be pointless, but i'll analyze that later
        * 5 - because the 0 was removed both dates will have equal .size() and can be simply compared
        *  for equality*/
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String dateSelected = year + "/" + (month+1) + "/" + dayOfMonth;
                LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
                String currentDatePureString = currentDate.toString().replaceAll("-", "");

                StringBuilder remove0 = new StringBuilder(String.valueOf(currentDate));
                remove0 = remove0.deleteCharAt(5);
                String remove0BackToString = remove0.toString().replaceAll("-", "");

                int dateSelectedConvert = Integer.parseInt(dateSelected.replaceAll("/", ""));
                int currentDateConvert = Integer.parseInt(String.valueOf(currentDate).replaceAll("-", ""));

                char currentDateCharacters = String.valueOf(currentDateConvert).charAt(4);
                int currentDateCharToInt = Integer.parseInt(String.valueOf(currentDateCharacters));

                if(currentDateCharToInt == 0){
                    Log.d("charTest",  "character was 0");
                    for(int i = 0; i < currentDatePureString.length(); i++){
                        Log.d("Check Current Date", "" + remove0BackToString);
                        Log.d("Check Date Selected", "" + dateSelectedConvert);

                        if(Integer.parseInt(remove0BackToString) == dateSelectedConvert){
                            Log.d("dateChecker", "the Dates are equal");
                        }else{
                            Log.d("dateChecker", "the Dates are not equal");
                        }
                    }
                }else{
                    Log.d("charTest",  "character was not 0" + " : " + currentDateCharacters);
                }
                Log.d("currentDate", "" + currentDate + " : " + dateSelected + " | " + dateSelectedConvert + " : " + currentDateConvert);
                // compare the current date and the date selected to make sure a previous date is not selected
                date.setText(dateSelected);
            }
        });
    }
}