package com.example.clout.MainActivities;

import com.example.clout.R;
import android.annotation.SuppressLint;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clout.MainActivities.Classes.AccountKeyGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.PaymentConfiguration;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentSource;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentSourceCollection;
import com.stripe.model.Source;
import com.stripe.model.Token;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class AddPaymentMethodActivity extends AppCompatActivity {

    TextInputLayout cardNumber;
    TextInputLayout ExpMonth;
    TextInputLayout ExpYear;
    TextInputLayout cvc;
    MaterialButton submitButton;
    FirebaseUser mCurrentUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    AccountKeyGenerator accKey;
    private Customer customerA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment_method);

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_51GuYBuLsgkZ2wTkEgd35DE6mgq0iJxMlShxbwG86L1npkctNyQw3Ht81z9KelC15PiYH7yk8I9OxyhTbpFLqZhHQ00IM0NKGlw"
        );

        accKey = new AccountKeyGenerator();
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
    }

    // The below method will be used to house an Material Alert which will
    // require the user to confirm there information
    public void confirmationAlertAndAsyncInit(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(AddPaymentMethodActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);
        confirm.setTitle("CONFIRM");
        confirm.setMessage("Are you sure the information you've entered is correct?");

        MaterialButton button = new MaterialButton(AddPaymentMethodActivity.this);
        MaterialButton cancel = new MaterialButton(AddPaymentMethodActivity.this);
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
                    Log.d("val_test", " " + cNumString + " " + eMString + " " + eYString + " " + cvcString);


                    //new AsyncStripeTask().execute(AsyncStripeTask);
                    confirm.dismiss();
                }else{
                    Toast.makeText(AddPaymentMethodActivity.this, "Please make sure all fields are populated", Toast.LENGTH_SHORT).show();
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

    // Async Task MUST BE USED to create a payment processing.
    //@SuppressLint("NewApi")
    /* private class AsyncStripeTask extends AsyncTask {
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
                    Log.d("success", "Value is: " + value);

                    assert value != null;

                    class AsyncStripeTaskDepth extends AsyncTask {
                        private Object AsyncAddCustomerTaskDepth;

                        @SuppressLint("WrongThread")
                        @Override
                        protected Object doInBackground(Object[] objects) {

                            try {
                                Customer customerA;
                                customerA = Customer.retrieve(value);

                                Map<String, Object> cardParam = new HashMap<String, Object>();
                                cardParam.put("number", CardNumberString);
                                cardParam.put("exp_month", expMonthString);
                                cardParam.put("exp_year", expYearString);
                                cardParam.put("cvc", cvcString);

                                Map<String, Object> tokenParam = new HashMap<String, Object>();
                                tokenParam.put("card", cardParam);

                                try {
                                    Token token = Token.create(tokenParam);

                                    Map<String, Object> source = new HashMap<String, Object>();
                                    source.put("source", token.getId());

                                    try {
                                        Log.d("customerA", "" + customerA);
                                        customerA.getSources().create(source);

                                        myRef.child("isCardOnFile").setValue("YES");
                                    } catch (StripeException e) {
                                        e.printStackTrace();
                                    }
                                } catch (StripeException e) {
                                    e.printStackTrace();
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
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.d("failed", "Failed to read value.");
                }
            });
            return null;
        }
    } */
}
