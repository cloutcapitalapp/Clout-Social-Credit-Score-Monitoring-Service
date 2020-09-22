package com.example.clout.MainActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clout.MainActivities.Classes.AccountKeyGenerator;
import com.example.clout.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class userprfileactivity extends AppCompatActivity {

    private StorageReference mStorageRefPfofPic;
    ProgressBar progress;
    TextView accountKey;
    TextView lastFourTV;
    TextView updateUNTV;
    CircleImageView profilePic;
    Button addedButton;
    Button updateCardButton;
    AccountKeyGenerator accKey;
    Button update;
    EditText editTextName;
    // Write a message to the database
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("Users");
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();
    Uri mImageUri; /* Image URI is for Firebase Image Storage */
    private StorageReference mStorageRef;
    public static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprfileactivity);

        progress = findViewById(R.id.progressBar);
        mStorageRef = FirebaseStorage.getInstance().getReference("user/");
        accountKey = findViewById(R.id.accountKeyTitle);
        lastFourTV = findViewById(R.id.lastFourTextView);
        updateUNTV = findViewById(R.id.updateTextView);
        profilePic = findViewById(R.id.profile_image);
        addedButton = findViewById(R.id.addedButton);
        updateCardButton = findViewById(R.id.updateCardButton);
        accKey = new AccountKeyGenerator();
        update = findViewById(R.id.submitButton);
        editTextName = findViewById(R.id.editTextTextPersonName);

        progress.setVisibility(View.INVISIBLE);
        updateUserName();
        usernameRec();
        toFetchImage();
    }
    @Override
    protected void onStart(){
        super.onStart();

        onStartGrabImage();
    }
    public void usernameRec (){

        usersRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                accountKey.setText(key);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void updateUserName(){
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //This for-loop needs to check the entered value against the users in the database for accountkeys
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            String snappedEmail = (String) snapshot1.child("Email").getValue();
                            String accKeySanpped = accKey.createAccountKey(snappedEmail);
                            // Check if the entered username has been used before
                            if(editTextName.getText().toString().equals(accKeySanpped)){
                                Toast.makeText(userprfileactivity.this, "This username is not available", Toast.LENGTH_LONG).show();
                                //Log.d("UserCheck", "Value is: " + accKeySanpped + " : " + borrowerET.getEditText().getText());
                                break;
                            }else{
                                // The username is not taken and can be updated
                                Toast.makeText(userprfileactivity.this, "UPDATING...", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    /* When an image is selected from gallary return result */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            //TODO: action
            mImageUri = data.getData();

            uploadFile();

        }
    }

    /* When image file is selected master the extension of the file for sending to DB */
    private String getFileExtention (Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /* When image is selected and extension is grabbed, send to storage */
    private void uploadFile(){
        if (mImageUri != null) {
            //getFileExtention(mImageUri)
            StorageReference fileReference = mStorageRef.child("user/" + mCurrentUser.getEmail() + "profilepicture" + "." + getFileExtention(mImageUri));
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setProgress(0);
                                }
                            }, 500);

                            progress.setVisibility(View.INVISIBLE);

                            Toast.makeText(userprfileactivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            Intent reload = new Intent(userprfileactivity.this, userprfileactivity.class);
                            startActivity(reload);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    Double progressVal = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progress.setVisibility(View.VISIBLE);
                    progress.setProgress(progressVal.intValue());

                }
            });

        }else{

            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();

        }
    }

    // ported to errands Check Done*
    /* When C.ImgView is tapped, take user to users photo Gallery */
    private void toFetchImage(){
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }

    /* When onStart 'LifeCycle' begins check if there is a user profile image, if so place in C.ImgView */
    private void onStartGrabImage(){
        mStorageRefPfofPic = FirebaseStorage.getInstance().getReference("user/user/" + mCurrentUser.getEmail() + "profilepicture" + "." + "jpg");

        if(mStorageRefPfofPic != null){
            mStorageRefPfofPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String uriString = uri.toString();
                    Glide.with(userprfileactivity.this).load(uriString).into(profilePic);
                    //Log.d("load image", "" + uriString);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else{
            //Log.d("ReportImageTarget", "\n " +
            //"\n " +
            //"\n " +
            //"onStart: NOTHING " +
            //"\n " +
            //"\n");

            Glide.with(userprfileactivity.this).load(profilePic).into(profilePic);
            //Log.d("load image", "" + mStorageRefPfofPic.toString());
        }
    }
}