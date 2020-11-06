package com.example.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.clout.MainActivities.Classes.AccountKeyManager;
import com.example.clout.MainActivities.Classes.SessionActivityID;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

public class CreateNewSession_2 extends AppCompatActivity {
    String receiver;
    ImageButton continueButton;
    ImageButton backArrow;
    EditText amountEditText;
    TextView amountTextView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    AccountKeyManager accKey;
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();
    DatabaseReference userReference = mDataBase.getReference("Users");
    DatabaseReference transactionRef = mDataBase.getReference("User_Transactions");
    SessionActivityID sessionActivityID;
    //private Object AsyncStripeTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_2);
        amountEditText = findViewById(R.id.AmountEditText);
        amountTextView = findViewById(R.id.amountTextView);
        continueButton = findViewById(R.id.continueArrow);
        backArrow = findViewById(R.id.backArrow);
        accKey = new AccountKeyManager();
        sessionActivityID = new SessionActivityID();
        //new AsyncStripeTask().execute(AsyncStripeTask);

        openAnimations();
        continueButtonMethod();
        amountUpdater();
    }

    @Override
    protected void onStart(){
        super.onStart();
        getExtras();
    }

    private void getExtras(){
        Bundle intentExtras = getIntent().getExtras();
        assert intentExtras != null;
        receiver = intentExtras.getString("receiver");
        //Log.d("recieverTest", "extras : " + receiver);
    }

    private void continueButtonMethod(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stage2Transaction();
                Bundle toUserBundle = new Bundle();
                Bundle amountBundle = new Bundle();

                Intent toStage2 = new Intent(CreateNewSession_2.this, CreateNewSession_3.class);
                String getToUser = receiver;
                String getAmount = amountEditText.getText().toString();
                toUserBundle.putString("toUser", "" + getToUser);
                amountBundle.putString("amount", "" + getAmount);
                toStage2.putExtras(toUserBundle);
                toStage2.putExtras(amountBundle);
                startActivity(toStage2);
            }
        });
    }
    private void amountUpdater(){
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(amountEditText.getText().toString().equals("")){
                    amountEditText.setText("0");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                amountTextView.setText(MessageFormat.format("{0}{1}", getString(R.string.money_sign), String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(amountEditText.getText().toString()) / 100)), TextView.BufferType.EDITABLE);
                //TODO: cause textView to decrease size when divisible by 1000
                if(Double.parseDouble(amountEditText.getText().toString()) % 10000 != 0 ||
                        Double.parseDouble(amountEditText.getText().toString()) % 10000 == 0){
                    amountTextView.setTextSize(70);
                }else{
                    amountTextView.setTextSize(136);
                }

                if(Double.parseDouble(amountEditText.getText().toString()) >= 200000){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        amountTextView.setTextColor(Color.RED);
                    }
                    amountTextView.setText(R.string.overAmount);
                }else{
                    amountTextView.setTextColor(Color.GRAY);
                }
            }
        });
    }
    
    // Async Task MUST BE USED to create a payment processing.
    //@SuppressLint("NewApi")
    /*private class AsyncStripeTask extends AsyncTask {
        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Object... objects) {
            try {
                // Read from the database
                userReference.child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("stripeCustomerID").addValueEventListener(new ValueEventListener() {
                    private Object AsyncStripeTaskDepth;

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Stripe.apiKey = "sk_test_51GuYBuLsgkZ2wTkEPW1f3aeAOcqJTWWgDTq2frFZSpsn2dtM1zLtiGQd3E90OGFNo7VPmL9Y2w62zpvwiwf5nwW5007TNe558H";
                        final Customer[] customoer = {null};
                        final Customer[] customerSource = {null};
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        Log.d("dbcheck", "" + value);

                        @SuppressLint("NewApi")
                        class AsyncStripeTaskDepth extends AsyncTask {
                            @SuppressLint("WrongThread")
                            @Override
                            protected Void doInBackground(Object... objects) {
                                assert value != null;

                                try {
                                    if(Customer.retrieve(value) != null){
                                        customoer[0] = Customer.retrieve(value);
                                        Log.d("check", value);
                                        customerSource[0] = Customer.retrieve(value);
                                        if(customerSource[0].getSources() == null){
                                            //Log.d("sourcecheck", "there is no source available");
                                        }else{
                                            //Log.d("sourcechck", "There is a payment source");
                                        }

                                        //Log.d("getCustID", "Value is: " + value + " : " + customoer[0]);
                                    }else{
                                        isPaymentSourceAlert();
                                    }
                                } catch (StripeException ex) {
                                    ex.printStackTrace();
                                }
                                return null;
                            }
                        }
                        new AsyncStripeTaskDepth().execute(AsyncStripeTaskDepth);
                    }
                    @Override
                    public void onCancelled(@NotNull DatabaseError error) {
                        // Failed to read value
                        Log.w("GetCustIDFailed", "Failed to read value.", error.toException());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/
    private void isPaymentSourceAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(getApplicationContext()).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("No Payment Source!");
        confirm.setMessage("There is no payment method available yet. " +
                "Please go to your profile before you create a transaction to add a payment method.");

        MaterialButton button = new MaterialButton(getApplicationContext());
        MaterialButton cancel = new MaterialButton(getApplicationContext());
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
            }
        });
        confirm.create();
        confirm.show();
    }
    private void stage2Transaction(){
        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
                transactionRef.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityID.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), /*This value needs to be the received extra*/ receiver, String.valueOf(currentDate)));
                transactionRef.child(/*** This needs to be changed to the accountSessionID ***/ sessionActivityID.generateSessionID(
                        accKey.createAccountKey(mCurrentUser.getEmail()), /*This value needs to be the received extra*/ receiver, String.valueOf(currentDate)))
                        .child("amount").setValue(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(amountEditText.getText().toString())));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void openAnimations(){

        ObjectAnimator animForwardArrow = ObjectAnimator.ofFloat(continueButton, "translationY", -150f);
        animForwardArrow.setDuration(1700);
        animForwardArrow.start();

        ObjectAnimator animBackArrow = ObjectAnimator.ofFloat(backArrow, "translationY", 150f);
        animBackArrow.setDuration(1700);
        animBackArrow.start();

    }
}