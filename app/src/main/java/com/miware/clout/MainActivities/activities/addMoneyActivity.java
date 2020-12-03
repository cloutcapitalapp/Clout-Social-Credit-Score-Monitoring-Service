package com.miware.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.miware.clout.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class addMoneyActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION = 1;
    EditText howMuchEditText;
    TextView howMuchTextView;
    TextView valueTextView;
    TextView withdrawCashTextView;
    Button submitButton;
    FirebaseDatabase database;
    DatabaseReference mAddCashRef;
    DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money);

        df = new DecimalFormat("0.00");
        database = FirebaseDatabase.getInstance();
        mAddCashRef = database.getReference("Users");
        howMuchEditText = findViewById(R.id.howMuchEditText);
        valueTextView = findViewById(R.id.valueTextView);
        submitButton = findViewById(R.id.submitButton);
        withdrawCashTextView = findViewById(R.id.withdrawButton);

        fineLocationPermissionCheck();
        amountUpdater();
        submitButtonFunction();
        withdrawCashButton();
    }
    private void amountUpdater(){
        howMuchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(howMuchEditText.getText().toString().equals("")){
                    howMuchEditText.setText("0");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                valueTextView.setText("$" + String.valueOf(Double.valueOf(howMuchEditText.getText().toString())/100), TextView.BufferType.EDITABLE);
                //TODO: cause textView to decrease size when divisible by 1000
                if(Double.parseDouble(howMuchEditText.getText().toString()) % 10000 != 0 ||
                        Double.parseDouble(howMuchEditText.getText().toString()) % 10000 == 0){
                    valueTextView.setTextSize(70);
                }else{
                    valueTextView.setTextSize(136);
                }

                if(Double.parseDouble(howMuchEditText.getText().toString()) >= 200000){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        valueTextView.setTextColor(Color.RED);
                    }
                    howMuchTextView.setText(R.string.overAmount);
                }else{
                    valueTextView.setTextColor(Color.GRAY);
                }
            }
        });
    }
    private void submitButtonFunction(){
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if there is a value to be sent to firebase,
                // if not return to main regardless and alert user - nothing has changed

                if(/*If there is no value present,
                send to main activity*/ howMuchEditText.getText().toString().equals("")){
                    //Log.d("testValue", "Value is present" + howMuchEditText.getText().toString());
                    addCashAlert();
                }else{
                    // Read from the database
                    mAddCashRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String value = dataSnapshot.getValue(String.class);
                            //Log.d("Add Cash Success", "Value is: " + value);

                            mAddCashRef.setValue(String.valueOf(Double.parseDouble(valueTextView.getText().toString()
                                    .replace("$", "")) + Double.parseDouble(value)));

                            Intent returnToMain = new Intent(addMoneyActivity.this, MainActivity.class);
                            startActivity(returnToMain);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            //Log.w("Add Cash Failed", "Failed to read value.", error.toException());
                        }
                    });
                }
            }
        });
    }
    private void withdrawCashButton(){

        withdrawCashTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Read from the database
                mAddCashRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        //Log.d("Add Cash Success", "Value is: " + value);

                        mAddCashRef.setValue((String.valueOf(df.format(Double.parseDouble(value) -
                                Double.parseDouble(valueTextView.getText().toString()
                                        .replace("$", ""))))));

                        Intent returnToMain = new Intent(addMoneyActivity.this, MainActivity.class);
                        startActivity(returnToMain);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        //Log.w("Add Cash Failed", "Failed to read value.", error.toException());
                    }
                });
            }
        });

    }
    private void addCashAlert(){
        AlertDialog.Builder addCashCheck = new AlertDialog.Builder(this);
        addCashCheck.setTitle("Add Cash?");
        addCashCheck.setMessage("Nothing has been entered. Return to Main Page?");
        addCashCheck.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent returnToMain = new Intent(addMoneyActivity.this, MainActivity.class);
                startActivity(returnToMain);
            }
        });
        addCashCheck.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        addCashCheck.create();
        addCashCheck.show();
    }
    private void fineLocationPermissionCheck(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            // REQUEST_CODE_LOCATION should be defined on your app level
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            throw new RuntimeException("Location services are required in order to " +
                    "connect to a reader.");
        }
    }
}