package com.miware.clout.SourceCode.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.miware.clout.SourceCode.Classes.AccountKeyManager;
import com.miware.clout.SourceCode.Classes.AnimationClass;
import com.miware.clout.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*

Firebase Database paths must not contain '.', '#', '$', '[', or ']

*/

/**The login activity must be attractive as it will be the
 * introduction of the user to the app
 * @param // FIXME: 10/18/20 refactor notes on this activity*/
public class AuthActivity extends AppCompatActivity {
    /* Declare Vars */
    private View top_layout;
    private AnimationClass animationClass;
    private TabLayout logAndSignTab;
    private TextInputLayout username, password, retype;
    private Button submit, loginButton;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mRefUsers;
    private FirebaseDatabase db;
    private AccountKeyManager keyGenerator;

    // onCreate method should be confound to Vars and method calls
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        //Init vars
        initVars();

        loginButton.setVisibility(View.INVISIBLE);
        openingAnimation(submit);
        tabLayoutManager_onClickHandler();
        loginButtonOnClick();
        submitButtonOnClick();
    }

    /*Check if user is signed in (non-null) and update UI accordingly.
     * If there is a user cached then go to MainActivity,
     * if not then stay on AuthActivity to sign up or log in*/
    @Override
    public void onStart() {
        super.onStart();
        //onStart set login button to Invisible
        loginButton.setVisibility(View.INVISIBLE);

        if (mCurrentUser != null){
            CountDown();
        }else{
            Snackbar.make(getWindow().getDecorView().getRootView(),
                    "Create Account or log in",
                    BaseTransientBottomBar.LENGTH_LONG).show();
        }
    } /* If there is no user, user will be prompted to create an account. */

    // Init application vars
    private void initVars(){
        /* Assign Vars */
        top_layout = findViewById(R.id.top_layout);
        animationClass = new AnimationClass();
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
        mRefUsers = db.getReference("Users");
    }

    /**When the back button was being pressed, the user would be taken back to the launch screen
     * with no animation and the intent was killed so the user wasn't being returned to the */
    @Override
    public void onBackPressed() {
        /*Do nothing when the back button is pressed to avoid returning to auth activity*/
    }

    private void openingAnimation(Button buttonToAnimate){
        animationClass.openingAnimation(buttonToAnimate);
        animationClass.reverseAnimation(buttonToAnimate);
    }

    /**Firebase method: if email is properly formatted and passwords match firebase user
     * will be created and cached.
     * The following methods will be for creating an account or signing into one.
     * */
    private void createAccount(final String email, String password){
        if(!email.isEmpty()){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        private Object AsyncStripeTask;

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //If the sign in is successful
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d("success", "createUserWithEmail:success");
                                FirebaseUser fireUser = mAuth.getCurrentUser();
                                keyGenerator = new AccountKeyManager();

                                /*userObject = new UserObject does not appear to be used
                                 *@// FIXME: 10/17/20 will fix soon */

                                String accKey = keyGenerator.createAccountKey(email);
                                // Send init user data to firebase real time database
                                mRefUsers.child(accKey);
                                assert fireUser != null;
                                mRefUsers.child(accKey).child("Email").setValue(email);

                                mRefUsers.child(accKey).child("FirstName").setValue("Not entered yet");
                                mRefUsers.child(accKey).child("LastName").setValue("Not entered yet");
                                mRefUsers.child(accKey).child("Address").setValue("Not entered yet");
                                mRefUsers.child(accKey).child("Score").setValue("CS200.00");
                                mRefUsers.child(accKey).child("Cash").setValue("$0.00");
                                mRefUsers.child(accKey).child("isCardOnFile").setValue("NO");
                                mRefUsers.child(accKey).child("isFirstTimeUserIntro").setValue("yes");
                                mRefUsers.child(accKey).child("isFirstTimeUserScore").setValue("yes");
                                mRefUsers.child(accKey).child("isFirstTimeUserCash").setValue("yes");
                                //For now creating a TransactionList inside the user element won't be used
                                //mRefUsers.child(accKey).child("TransactionList").setValue("Null");
                                new AsyncStripeTask().execute(AsyncStripeTask);

                                CountDown();
                            } else {
                                // If sign in fails, display a message to the user.
                                Snackbar.make(getWindow().getDecorView().getRootView(), "Authentication Failed", Snackbar.LENGTH_LONG);
                                checkEmailAlert();
                            }
                            // ...
                        }
                    });
        }else{
            Snackbar snackbar = Snackbar
                    .make(getWindow().getDecorView().getRootView(),
                            "We need an email", Snackbar.LENGTH_LONG);
            snackbar.setAction("Action", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Refresh layout
                    finish();
                    startActivity(getIntent());
                }
            }).show();
        }
    }
    private void signInUser(final String email, final String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d("firebase_test", email + " " + password);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            CountDown();
                        } else {
                            // If sign in fails, display a message to the user.
                            loginAlert();
                        }
                        // ...
                    }
                });
    }

    /**When submit button is clicked, check if passwords match and check email*/
    private void submitButtonOnClick(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Email to store for creation of users account */ String email = Objects
                        .requireNonNull(username.getEditText()).getText().toString();

                /* Passwords will be used by if conditional | if passwords match account will
                be created (assuming email is formatted correctly). */
                /* initial password for comparision */ String passwordMain = Objects
                        .requireNonNull(password.getEditText()).getText().toString();
                /* secondary password for comparison */String passwordRetype = Objects
                        .requireNonNull(retype.getEditText()).getText().toString();

                /*Password requirements should be set
                 * rule 1 - should match - satisfied
                 * rule 2 - should be at least 10 characters @todo check*/
                if(/*Passwords match*/ passwordMain.equals(passwordRetype)
                        && (/*can NOT be less than 10 characters*/(passwordMain.length() >= 10))){
                    createAccount(email.replace(" ", ""), passwordMain);
                }else{
                    passwordLengthAlert();
                }
            }
        });
    }
    private void passwordLengthAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(AuthActivity.this).create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("WAIT!");
        confirm.setMessage("Your password isn't long enough or doesn't match." +
                "Please check it and try again. " +
                "It just helps keep your account secure.");

        MaterialButton button = new MaterialButton(AuthActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    }
    private void checkEmailAlert(){
        final AlertDialog confirm = new MaterialAlertDialogBuilder(AuthActivity.this)
                .create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("WAIT!");
        confirm.setMessage("Please make sure your email is correct and try again!");

        MaterialButton button = new MaterialButton(AuthActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    }

    /**Below will be the method used to log the user into his/her account*/
    private void loginButtonOnClick(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Email to store for creation
                of users account */ String email = username
                        .getEditText().getText().toString();

                /* Passwords will be used by
                if conditional | if passwords match account
                will be created (assuming email is formatted correctly). */
                /* initial password for comparision */ String passwordMain = password
                        .getEditText().getText().toString();

                signInUser(email.trim(), passwordMain);
            }
        });
    }
    private void loginAlert(){
        /*User needs to be warned to check email and password and try again*/
        final AlertDialog confirm = new MaterialAlertDialogBuilder(AuthActivity.this)
                .create();
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        confirm.setView(layout);

        confirm.setCancelable(false);
        confirm.setCanceledOnTouchOutside(false);

        confirm.setIcon(R.drawable.ic_baseline_emoji_emotions_24);

        Window window = confirm.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        confirm.setTitle("WAIT!");
        confirm.setMessage("Looks like your user name or password was incorrect. Please check, " +
                "and try again.");

        MaterialButton button = new MaterialButton(AuthActivity.this);
        button.setText(R.string.confirm);
        button.setBackgroundResource(R.color.colorPrimary);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm.dismiss();
            }
        });
        confirm.show();
    }

    //
    private void tabLayoutManager_onClickHandler(){
        logAndSignTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == /* check for index 1 which is the login tab */ 1){
                    retype.setVisibility(View.INVISIBLE);
                    submit.setVisibility(View.GONE);
                    loginButton.setVisibility(View.VISIBLE);
                    animationClass.openingAnimation(submit);
                    animationClass.reverseAnimation(submit);
                    animationClass.openingAnimation(loginButton);
                }else /* if else, then index 0 is active | index 0 is the sign up tab, and all
                elements will be available when index 0 is active */{
                    retype.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.INVISIBLE);
                    submit.setVisibility(View.VISIBLE);
                    animationClass.openingAnimation(submit);
                    animationClass.reverseAnimation(loginButton);
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

    /**Creates stripe customer*/
    class AsyncStripeTask extends AsyncTask {
        @SuppressLint("WrongThread")
        @Override
        protected Object doInBackground(Object[] objects) {
            com.stripe.Stripe.apiKey =
                    getString(R.string.stripekey);
            FirebaseUser fireUser = mAuth.getCurrentUser();
            assert fireUser != null;
            /*In case of crash - you added - assert fireUser != null;
             * and you added Objects.requireNonNull to mapUsernameString
             * of auth bug reports - then reverse these implementations*/
            String accKey = keyGenerator.createAccountKey(fireUser.getEmail());
            String mapUsernameString = Objects.requireNonNull(username.getEditText())
                    .getText()
                    .toString()
                    .replace(" ", "");
            Map<String, Object> mapCustomer = new HashMap<String, Object>();
            mapCustomer.put("email", mapUsernameString);
            try {
                Customer newCustomer = Customer.create(mapCustomer);
                String customerID = newCustomer.getId();
                //Log.d("CustomerID", customerID);
                mRefUsers.child(accKey);
                mRefUsers.child(accKey).child("stripeCustomerID").setValue(customerID);
            } catch (StripeException e) {
                e.printStackTrace();
            }            return null;
        }
    }

    private void accountCreatedLoad_Intent(){
        top_layout.setVisibility(View.VISIBLE);
    }

    /*count down method starts count down
     * might want to change this to separate class*/
    private void CountDown() {
        new CountDownTimer(1500, 3000) {
            private java.util.Locale Locale;

            public void onTick(long millisUntilFinished) {
                //clockTextView.setText("" + millisUntilFinished / 1000);
                //here you can have your logic to set text to EditText
                accountCreatedLoad_Intent();
            }
            public void onFinish() {
                Intent passToMessengerActivity = new Intent(AuthActivity.this,
                        MainActivity.class);
                startActivity(passToMessengerActivity);
            }
        }.start();
    }
}