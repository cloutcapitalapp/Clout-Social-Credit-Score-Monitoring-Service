package com.miware.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.miware.clout.MainActivities.Classes.AccountKeyManager;
import com.miware.clout.MainActivities.objects.UserObject;
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

//TODO Firebase Database paths must not contain '.', '#', '$', '[', or ']

/**The login activity must be attractive as it will be the introduction of the user to the app
 * @param // FIXME: 10/18/20 refactor notes on this activity*/
public class AuthActivity extends AppCompatActivity {
    /* Init Vars */
    VideoView videoView;
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
    private AccountKeyManager keyGenerator;
    private UserObject userObject;

    // onCreate method should be confound to Vars and method calls
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Assign Vars */
        videoView = (VideoView) findViewById(R.id.videoView);
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
        mRefUsers = db.getReference("Users");

        // App functionality
        loginButton.setVisibility(View.INVISIBLE);
        openingAnimation();
        tabLayoutManager();
        loginButtonOnClick();
        submitButtonOnClick();
    }

    /**When the back button was being pressed, the user would be taken back to the launch screen
     * with no animation and the intent was killed so the user wasn't being returned to the */
    @Override
    public void onBackPressed() {
        //...do nothing
    }

    /**onStart the ad video should play
     * when the add video plays, because its so loud the system volume should be changed to 6*/
    private void onStartPlayIntroVideo(){
        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.cloutlogoad);

        AudioManager audioManager =
                (AudioManager)getSystemService(AuthActivity.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 6, 1);

        videoView.setVideoURI(uri);
        videoView.start();
    }

    /**These are animations
     * openingAnimation will run onCreate
     * reverseAnimation will run on return to sign up tab*/
    private void openingAnimation(){
        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(submit, "translationY", -90f);
        animCashButton.setDuration(500);
        animCashButton.start();
    }
    private void reverseAnimation(){
        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(submit, "translationY", 90f);
        animCashButton.setDuration(500);
        animCashButton.start();
    }

    /**Check if user is signed in (non-null) and update UI accordingly.
     * If there is a user cached then go to MainActivity,
     * if not then stay on AuthActivity to sign up or log in*/
    @Override
    public void onStart() {
        super.onStart();
        if (mCurrentUser != null){
            Intent toProfileActivity = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(toProfileActivity);
        }else{
            Toast.makeText(AuthActivity.this, "Please Create Account!", Toast.LENGTH_SHORT).show();
            onStartPlayIntroVideo();
        }
    } /* If there is no user, user will be prompted to create an account. */

    /**Firebase method: if email is properly formatted and passwords match firebase user will be created and cached.
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

                                /**userObject = new UserObject does not appear to be used
                                 *@// FIXME: 10/17/20 will fix soon */
                                userObject = new UserObject();


                                String accKey = keyGenerator.createAccountKey(email);

                                //Log.d("New Pass", "" + accKey);

                                // Send a userObject to firebase RTDB
                                //
                                //

                                mRefUsers.child(accKey);
                                assert fireUser != null;
                                mRefUsers.child(accKey).child("Email").setValue(email);

                                mRefUsers.child(accKey).child("FirstName").setValue("CS200.00");
                                mRefUsers.child(accKey).child("LastName").setValue("CS200.00");
                                mRefUsers.child(accKey).child("Score").setValue("CS200.00");
                                mRefUsers.child(accKey).child("Cash").setValue("$0.00");
                                mRefUsers.child(accKey).child("isCardOnFile").setValue("NO");
                                mRefUsers.child(accKey).child("isFirstTimeUserIntro").setValue("yes");
                                mRefUsers.child(accKey).child("isFirstTimeUserScore").setValue("yes");
                                mRefUsers.child(accKey).child("isFirstTimeUserCash").setValue("yes");
                                //For now creating a TransactionList inside the user element won't be used
                                //mRefUsers.child(accKey).child("TransactionList").setValue("Null");
                                new AsyncStripeTask().execute(AsyncStripeTask);

                                //
                                //
                                // DONE

                                Intent toMain = new Intent(AuthActivity.this, IntroHubActivity.class);
                                startActivity(toMain);
                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.w("Failed", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(AuthActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                                Toast.makeText(AuthActivity.this, email + "", Toast.LENGTH_SHORT).show();

                                checkEmailAlert();
                            }
                            // ...
                        }
                    });
        }else{
            Toast.makeText(this, "We need an email", Toast.LENGTH_SHORT).show();
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
                            //Log.d("isLoggedIn", "signInWithEmail:success");
                            Intent passToMain = new Intent(AuthActivity.this, MainActivity.class);
                            startActivity(passToMain);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w("isLoggedIn", "signInWithEmail:failure", task.getException());
                            //Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            loginAlert();
                        }
                        // ...
                    }
                });
    }
    /**End block*/

    /* BUTTON FUNCTIONS */
    /*  */
    /*  */

    /**When submit button is clicked, check if passwords match and check email*/
    private void submitButtonOnClick(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Email to store for creation of users account */ String email          = Objects.requireNonNull(username.getEditText()).getText().toString();

                /* Passwords will be used by if conditional | if passwords match account will be created (assuming email is formated correctly). */
                /* initial password for comparision */             String passwordMain   = Objects.requireNonNull(password.getEditText()).getText().toString();
                /* secondary password for comparison */            String passwordRetype = Objects.requireNonNull(retype.getEditText()).getText().toString();

                /**Password requirements should be set
                 * rule 1 - should match - satisfied
                 * rule 2 - should be at least 10 characters @todo check*/
                if(/*Passwords match*/ passwordMain.equals(passwordRetype)
                        && (/*can NOT be less than 10 characters*/(passwordMain.length() >= 10))){
                    createAccount(email.replace(" ", ""), passwordMain);
                }else{
                    //Log.d("SOMETHING: ", "Password check does not match:" + " " + passwordMain + " " + passwordRetype);
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
        confirm.setMessage("Your password isn't long enough or doesn't match. Please check it and try again. " +
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
                /* Email to store for creation of users account */ String email          = username.getEditText().getText().toString();

                /* Passwords will be used by if conditional | if passwords match account will be created (assuming email is formated correctly). */
                /* initial password for comparision */             String passwordMain   = password.getEditText().getText().toString();

                signInUser(email.trim(), passwordMain);
                //Log.d("test login cred", email + " " + passwordMain);
            }
        });
    }
    private void loginAlert(){
        /**User needs to be warned to check email and password and try again*/
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

    /* APP FUNCTIONALITY */
    /*  */
    /*  */

    // This section will house any functionality that does not need to be placed inside any * onclicklisteners *
    // Alternatively, the functionality listed below can be used inside of functions which are housed inside of
    // onclicklisteners
    //
    //
    //

    private void tabLayoutManager(){
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
                    openingAnimation();
                    reverseAnimation();
                }else /* if else, then index 0 is active | index 0 is the sign up tab, and all elements will be available when index 0 is active */{
                    //Log.d("isindex0", "onTabSelected: " + false); {test is complete | index1 is false and shows}

                    confirmAssistText.setVisibility(View.VISIBLE);
                    retype.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.INVISIBLE);
                    submit.setVisibility(View.VISIBLE);
                    openingAnimation();
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

    class AsyncStripeTask extends AsyncTask {
        @SuppressLint("WrongThread")
        @Override
        protected Object doInBackground(Object[] objects) {
            com.stripe.Stripe.apiKey = "sk_test_51GuYBuLsgkZ2wTkEPW1f3aeAOcqJTWWgDTq2frFZSpsn2dtM1zLtiGQd3E90OGFNo7VPmL9Y2w62zpvwiwf5nwW5007TNe558H";
            FirebaseUser fireUser = mAuth.getCurrentUser();
            assert fireUser != null;
            /**Incase of crash - you added - assert fireUser != null;
             * and you added Objects.requerNonNull to mapUsernameString
             * of auth bug reports - then reverse these implementations*/
            String accKey = keyGenerator.createAccountKey(fireUser.getEmail());
            String mapUsernameString = Objects.requireNonNull(username.getEditText()).getText().toString().replace(" ", "");
            Map<String, Object> mapCustomer = new HashMap<String, Object>();
            mapCustomer.put("email", mapUsernameString);
            try {
                Customer newCustomer = Customer.create(mapCustomer);
                String custID = newCustomer.getId();
                //Log.d("CustomerID", custID);
                mRefUsers.child(accKey);
                mRefUsers.child(accKey).child("stripeCustomerID").setValue(custID);
            } catch (StripeException e) {
                e.printStackTrace();
            }            return null;
        }
    }

    /**@// TODO: 10/17/20 this method is fixed by the above method which is an async
     * connection to stripe that creates a customer. They may be both taken out, but
     * for now the below method @fixme createStripeCustomer()
     * will be taken out before ship*/
    /* This method doesn't need to be used as it is not delivered Asynchronously */
    /* Create stripe customer */
    private void createStripeCustomer(){
        com.stripe.Stripe.apiKey = "sk_live_PRhe9eFUANmDRa7KlIqQF2mj00LBHktQVS";
        String mapUsernameString = username.getEditText().getText().toString().replace(" ", "");
        Map <String, Object> mapCustomer = new HashMap<String, Object>();
        mapCustomer.put("email", mapUsernameString);
        try {
            Customer newCustomer = Customer.create(mapCustomer);
            String custID = newCustomer.getId();
            //Log.d("CustomerID", custID);
            mRefUsers.child(mapUsernameString.replace(".", ""));
            mRefUsers.child(mapUsernameString.replace(".", "")).child("customerID").setValue(custID);
        } catch (StripeException e) {
            e.printStackTrace();
        }
    }
}