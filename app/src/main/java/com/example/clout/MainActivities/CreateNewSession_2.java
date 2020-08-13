package com.example.clout.MainActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.clout.R;

public class CreateNewSession_2 extends AppCompatActivity {

    ImageButton continueButton;
    EditText amountEditText;
    TextView amountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_session_2);
        amountEditText = findViewById(R.id.AmountEditText);
        amountTextView = findViewById(R.id.amountTextView);
        continueButton = findViewById(R.id.continueArrow);
        continueButtonMethod();

        amountUpdater();
    }

    private void continueButtonMethod(){
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                amountTextView.setText("$" + String.valueOf(Double.valueOf(amountEditText.getText().toString())/100), TextView.BufferType.EDITABLE);
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
}