package com.example.clout.MainActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clout.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialTextInputPicker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


// The login activity must be attractive as it will be the introduction of the user to the app
public class LoginActivity extends AppCompatActivity {
    /* Init Vars */
    private TextInputLayout username;
    private TextInputLayout password;
    private TextView confirmAssistText;
    private TextInputLayout retype;
    private Button submit;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference mRefUsers;
    private FirebaseUser mCurrentUser;
    private Button loginButton;
    private TabLayout logAndSignTab;

    // onCreate method should be confound to Vars and method calls
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Assign Vars */
        confirmAssistText = findViewById(R.id.confirmAssistText);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        retype   = findViewById(R.id.retype);
        submit   = findViewById(R.id.submit);
        loginButton = findViewById(R.id.login);
        logAndSignTab = findViewById(R.id.LogAndSignTab);

        // Initialize Firebase Auth and firebase paths
        db = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mRefUsers = db.getReference("users");

        // App functionality
        loginButton.setVisibility(View.INVISIBLE);
        tabLayoutManager();
        loginButtonOnClick();
        submitButtonOnClick();
    }

    // Check if user is signed in (non-null) and update UI accordingly.
    @Override
    public void onStart() {
        super.onStart();

        if (mCurrentUser != null){
            Intent toProfileActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(toProfileActivity);
        }else{
            Toast.makeText(LoginActivity.this, "Please Create Account!", Toast.LENGTH_SHORT).show();
        }
    } /* If there is no user, user will be prompted to create an account. */

    // Firebase method: if email is properly formated and passwords match firebase user will be created and cashed.
    public void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("success", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent toMain = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(toMain);

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w("Failed", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    // This method will be used to handle the user sign in.
    // TODO: The login button will not be shown unless the login tab is active.
    public void signInUser(final String email, final String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("firetest", email + " " + password);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("isLoggedIn", "signInWithEmail:success");
                            Intent passToMain = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(passToMain);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("isLoggedIn", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    /* BUTTON FUNCTIONS */
    /*  */
    /*  */

    // When submit button is clicked, check if passwords match
    public void submitButtonOnClick(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Email to store for creation of users account */ String email          = username.getEditText().getText().toString();

                /* Passwords will be used by if conditional | if passwords match account will be created (assuming email is formated correctly). */
                /* initial password for comparision */             String passwordMain   = password.getEditText().getText().toString();
                /* secondary password for comparison */            String passwordRetype = retype.getEditText().getText().toString();

                if(passwordMain.equals(passwordRetype)){
                    createAccount(email.replace(" ", ""), passwordMain);
                }else{
                    Log.d("SOMETHING: ", "Password check does not match:" + " " + passwordMain + " " + passwordRetype);
                }
            }
        });
    }

    // Below will be the method used to log the user into his/her account
    public void loginButtonOnClick(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Email to store for creation of users account */ String email          = username.getEditText().getText().toString();

                /* Passwords will be used by if conditional | if passwords match account will be created (assuming email is formated correctly). */
                /* initial password for comparision */             String passwordMain   = password.getEditText().getText().toString();

                signInUser(email, passwordMain);
                Log.d("test login cred", email + " " + passwordMain);
            }
        });
    }

    /* APP FUNCTIONALITY */
    /*  */
    /*  */

    // This section will house any functionality that does not need to be placed inside any * onclicklisteners *
    // Alternatively, the functionality listed below can be used inside of functions which are housed inside of
    // onclicklisteners
    //
    //
    //

    public void tabLayoutManager(){
        logAndSignTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // called when tab selected
                int position = tab.getPosition();
                // Log.d("tabPosition: ", "" + position); /* Log.d test is shown to work index is shown correctly */

                //TODO: if signup tab { index 0 } is selected, all elements should be visible
                //TODO: if login tab { index 1 } is selected, only username and password should be visible

                if(position == /* check for index 1 which is the login tab */ 1){
                    //Log.d("isindex1", "onTabSelected: " + true); {test is complete | index0 is true and shows}

                    retype.setVisibility(View.INVISIBLE);
                    confirmAssistText.setVisibility(View.INVISIBLE);
                    submit.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                }else /* if else, then index 0 is active | index 0 is the sign up tab, and all elements will be available when index 0 is active */{
                    //Log.d("isindex0", "onTabSelected: " + false); {test is complete | index1 is false and shows}

                    confirmAssistText.setVisibility(View.VISIBLE);
                    retype.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.INVISIBLE);
                    submit.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // called when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // called when a tab is reselected
            }
        });

    }

}