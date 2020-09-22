package com.example.clout.MainActivities;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.example.clout.MainActivities.Classes.AccountKeyGenerator;
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
import com.stripe.model.PaymentMethod;

import org.jetbrains.annotations.NotNull;

public class CreateNewSession_2 extends AppCompatActivity {

    ImageButton continueButton;
    ImageButton backArrow;
    EditText amountEditText;
    TextView amountTextView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    AccountKeyGenerator accKey;
    FirebaseDatabase mDataBase = FirebaseDatabase.getInstance();
    DatabaseReference userReference = mDataBase.getReference("Users");
    DatabaseReference transactionRef = mDataBase.getReference("User_Transactions");
    String defaultSource;
    private Object AsyncStripeTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_2);
        amountEditText = findViewById(R.id.AmountEditText);
        amountTextView = findViewById(R.id.amountTextView);
        continueButton = findViewById(R.id.continueArrow);
        backArrow = findViewById(R.id.backArrow);
        accKey = new AccountKeyGenerator();
        new AsyncStripeTask().execute(AsyncStripeTask);
        openAnimations();
        continueButtonMethod();
        amountUpdater();
    }

    private void continueButtonMethod(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stage2Transaction();
                Intent toStage2 = new Intent(CreateNewSession_2.this, CreateNewSession_3.class);
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
                amountTextView.setText(getString(R.string.money_sign) + String.valueOf(Double.valueOf(amountEditText.getText().toString())/100), TextView.BufferType.EDITABLE);
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
    @SuppressLint("NewApi")
    private class AsyncStripeTask extends AsyncTask {
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
                        Toast.makeText(CreateNewSession_2.this, value, Toast.LENGTH_SHORT).show();
                        Log.d("dbcheck", "" + value);

                        @SuppressLint("NewApi")
                        class AsyncStripeTaskDepth extends AsyncTask {
                            @SuppressLint("WrongThread")
                            @Override
                            protected Void doInBackground(Object... objects) {
                                assert value != null;
                                Looper.prepare();
                                try {
                                    if(Customer.retrieve(value) != null){
                                        customoer[0] = Customer.retrieve(value);
                                        Toast.makeText(CreateNewSession_2.this, value + " " + customoer[0], Toast.LENGTH_SHORT).show();
                                        Log.d("check", value);
                                        customerSource[0] = Customer.retrieve(value);
                                        if(customerSource[0].getSources() == null){
                                            //Log.d("sourcecheck", "there is no source available");
                                        }else{
                                            //Log.d("sourcechck", "There is a payment source");
                                        }

                                        //Log.d("getCustID", "Value is: " + value + " : " + customoer[0]);
                                    }else{
                                        Toast.makeText(CreateNewSession_2.this, "no customer id", Toast.LENGTH_SHORT).show();
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
    }
    public void isPaymentSourceAlert(){
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
    public void stage2Transaction(){
        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionRef.child(accKey.createAccountKey(mCurrentUser.getEmail()));
                transactionRef.child(accKey.createAccountKey(mCurrentUser.getEmail()))
                        .child("Amount").setValue(amountEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void openAnimations(){

        ObjectAnimator animForwardArrow = ObjectAnimator.ofFloat(continueButton, "translationY", -150f);
        animForwardArrow.setDuration(1700);
        animForwardArrow.start();

        ObjectAnimator animBackArrow = ObjectAnimator.ofFloat(backArrow, "translationY", 150f);
        animBackArrow.setDuration(1700);
        animBackArrow.start();

    }
}