package com.example.clout.MainActivities.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.clout.MainActivities.Classes.CheckAccountName;
import com.example.clout.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class EventTransactionActivity extends AppCompatActivity {

    //Classes
    CheckAccountName checkName = new CheckAccountName();

    //Buttons
    Button submitTransactionsButton;

    //EditText
    EditText editTextTextPersonName;
    /**This method will house the Events Transaction code
     * Creating this event transaction should create an entry into firebase that passes
     * data to the MainActivity recycler view*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_transaction);
        //Buttons
        submitTransactionsButton = findViewById(R.id.submitEventTransactionButton);
        //EditText
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);

        //Methods
        submitTransactionButtonOnClick();
    }

    /**This onClick handler should take the
     * @param // TODO: 10/15/20 onClickCheckTransactionsInfo() method*/
    /**START*/
    private void submitTransactionButtonOnClick(){
        submitTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckTransactionsInfo();
            }
        });
    }
    /**What needs to be checked?
     * &Accountname needs to be checked against firebase to make sure the user is present
     * Spinner needs to be checked for the item which is selected
     * Description must be checked for entry
     * and CalView needs to be checked for an entered date that isn't before the current date
     * */
    private void onClickCheckTransactionsInfo(){
        //Check &Accountname
        checkName.checkFirebaseUsername(editTextTextPersonName);

        //Check spinner item selected
        //Check description for validity ??
    }

    /**END*/
}