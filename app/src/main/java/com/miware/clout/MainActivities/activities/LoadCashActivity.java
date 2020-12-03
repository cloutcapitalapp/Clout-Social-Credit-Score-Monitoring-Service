package com.miware.clout.MainActivities.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.miware.clout.MainActivities.Classes.AccountKeyManager;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.stripe.android.PaymentConfiguration;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Token;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoadCashActivity extends AppCompatActivity {

    TextView amountTextView;
    EditText amoutEditTest;
    TextInputLayout cardNumber;
    TextInputLayout ExpMonth;
    TextInputLayout ExpYear;
    TextInputLayout cvc;
    MaterialButton submitButton;
    FirebaseUser mCurrentUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    AccountKeyManager accKey;
    private Customer customerA;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_cash);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51GuYBuLsgkZ2wTkEgd35DE6mgq0iJxMlShxbwG86L1npkctNyQw3Ht81z9KelC15PiYH7yk8I9OxyhTbpFLqZhHQ00IM0NKGlw"
        );

        amoutEditTest = findViewById(R.id.editTextNumber);
        amountTextView = findViewById(R.id.amountTextView);
        accKey = new AccountKeyManager();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        cardNumber = findViewById(R.id.cardNumber);
        ExpMonth = findViewById(R.id.exp_month);
        ExpYear = findViewById(R.id.exp_year);
        cvc = findViewById(R.id.enter_cvc);

        submitButton = findViewById(R.id.addPaymentMethodButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationAlertAndAsyncInit();
            }
        });

        amountUpdater();
        addCashAlert();
    }

    /*** The below alert will remind the user that they will be adding card details that will not be
     * stored, but instead will only be used to add funds to the users account***/
    private void addCashAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(LoadCashActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);
        confirm.setCanceledOnTouchOutside(false);
        confirm.setCancelable(false);
        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        confirm.setTitle("ADDING FUNDS!");
        confirm.setMessage("You will add funds to your account here. You're credit card details will " +
                "not be stored. If you'd like to withdraw funds, you can use the \"WITHDRAW FUNDS NOW\" button " +
                "below to do that now.");

        MaterialButton button = new MaterialButton(LoadCashActivity.this);
        MaterialButton cancel = new MaterialButton(LoadCashActivity.this);
        MaterialButton withDrawNow = new MaterialButton(LoadCashActivity.this);

        withDrawNow.setText(R.string.withdrawnow);
        button.setText(R.string.confirm);
        cancel.setText(R.string.cancel);
        withDrawNow.setBackgroundResource(R.color.colorPrimary);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(withDrawNow);
        layout.addView(button);
        layout.addView(cancel);

        withDrawNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            private Object AsyncStripeTask;
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(updatePaymentsActivity.this, "This is a test", Toast.LENGTH_SHORT).show(); /* Testing this functionality is complete Toast is no longer needed */
                // {'Cancel'} is selected and alert dialog should be dismissed.
                //confirm.dismiss();
                Intent backToMain = new Intent(LoadCashActivity.this, MainActivity.class);
                startActivity(backToMain);
            }
        });
        confirm.show();
    }

    // The below method will be used to house an Material Alert which will
    // require the user to confirm there information
    public void confirmationAlertAndAsyncInit(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(LoadCashActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("CONFIRM");
        confirm.setMessage("Are you sure the information you've entered is correct?");

        MaterialButton button = new MaterialButton(LoadCashActivity.this);
        MaterialButton cancel = new MaterialButton(LoadCashActivity.this);
        button.setText(R.string.confirm);
        cancel.setText(R.string.cancel);
        button.setBackgroundResource(R.color.colorPrimary);
        cancel.setBackgroundResource(R.color.colorPrimaryDark);
        layout.addView(button);
        layout.addView(cancel);
        button.setOnClickListener(new View.OnClickListener() {
            private Object AsyncStripeTask;
            @Override
            public void onClick(View v) {
                String cNumString = String.valueOf(cardNumber.getEditText().getText());
                String eMString = String.valueOf(ExpMonth.getEditText().getText());
                String eYString = String.valueOf(ExpYear.getEditText().getText());
                String cvcString = String.valueOf(cvc.getEditText().getText());
                // The below Toast {'which is anexed out'} was used to test whether or not the if-statment conditions were being met incorrectly
                // InputLayoutTexts' needed to be converted to strings {'as seen above'} before the conditions would work properly
                /* Toast.makeText(updatePaymentsActivity.this, "Please Maker Sure All Fields are Populated-Fail", Toast.LENGTH_SHORT).show(); */
                if(!cNumString.equals(null)
                        && !eMString.equals("")
                        && !eYString.equals("")
                        && !cvcString.equals("")) {
                    //Log.d("val_test", " " + cNumString + " " + eMString + " " + eYString + " " + cvcString);

                    new AsyncStripeTask().execute(AsyncStripeTask);
                    confirm.dismiss();
                }else{
                    Toast.makeText(LoadCashActivity.this, "Please make sure all fields are populated", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(updatePaymentsActivity.this, "This is a test", Toast.LENGTH_SHORT).show(); /* Testing this functionality is complete Toast is no longer needed */
                // {'Cancel'} is selected and alert dialog should be dismissed.
                confirm.dismiss();
            }
        });
        confirm.show();
    }

    private void amountUpdater(){
        amoutEditTest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(amoutEditTest.getText().toString().equals("")){
                    amoutEditTest.setText("0");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                amountTextView.setText(MessageFormat.format("{0}{1}", getString(R.string.money_sign), String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(amoutEditTest.getText().toString()) / 100)), TextView.BufferType.EDITABLE);
                //TODO: cause textView to decrease size when divisible by 1000
                if(Double.parseDouble(amoutEditTest.getText().toString()) % 10000 != 0 ||
                        Double.parseDouble(amoutEditTest.getText().toString()) % 10000 == 0){
                    amountTextView.setTextSize(70);
                }else{
                    amountTextView.setTextSize(136);
                }

                if(Double.parseDouble(amoutEditTest.getText().toString()) >= 200000){
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
    private class AsyncStripeTask extends AsyncTask {
        @SuppressLint("WrongThread")
        @Override
        protected Object doInBackground(Object[] objects) {
            final String CardNumberString = String.valueOf(cardNumber.getEditText().getText());
            final int expMonthString = Integer.parseInt(String.valueOf(ExpMonth.getEditText().getText()));
            final int expYearString = Integer.parseInt(String.valueOf(ExpYear.getEditText().getText()));
            final int cvcString = Integer.parseInt(String.valueOf(cvc.getEditText().getText()));
            DatabaseReference myRef = database.getReference("Users");

            // Read from the database
            myRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("stripeCustomerID").addValueEventListener(new ValueEventListener() {
                private Object AsyncStripeTaskDepth;

                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    com.stripe.Stripe.apiKey = "sk_test_51GuYBuLsgkZ2wTkEPW1f3aeAOcqJTWWgDTq2frFZSpsn2dtM1zLtiGQd3E90OGFNo7VPmL9Y2w62zpvwiwf5nwW5007TNe558H";

                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    //Log.d("success", "Value is: " + value);

                    assert value != null;

                    class AsyncStripeTaskDepth extends AsyncTask {
                        private Object AsyncAddCustomerTaskDepth;

                        @SuppressLint("WrongThread")
                        @Override
                        protected Object doInBackground(Object[] objects) {

                            Customer customerA = null;
                            try {
                                customerA = Customer.retrieve(value);
                            } catch (StripeException e) {
                                e.printStackTrace();
                            }

                            Map<String, Object> cardParam = new HashMap<String, Object>();
                             cardParam.put("number", CardNumberString);
                             cardParam.put("exp_month", expMonthString);
                             cardParam.put("exp_year", expYearString);
                             cardParam.put("cvc", cvcString);

                             Map<String, Object> tokenParam = new HashMap<String, Object>();
                             tokenParam.put("card", cardParam);

                            Token token = null;
                            try {
                                token = Token.create(tokenParam);
                            } catch (StripeException e) {
                                e.printStackTrace();
                            }

                            assert token != null;
                            Map<String, Object> source = new HashMap<String, Object>();
                            source.put("source", token.getId());

                             //Log.d("customerA", "" + customerA);
                            try {
                                assert customerA != null;
                                customerA.getSources().create(source);

                            } catch (StripeException e) {
                                e.printStackTrace();
                            }
                            myRef.child("isCardOnFile").setValue("YES");
                            return null;
                        }
                    }
                    new AsyncStripeTaskDepth().execute(AsyncStripeTaskDepth);
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    //Log.d("failed", "Failed to read value.");
                }
            });
            return null;
        }
    }
}
