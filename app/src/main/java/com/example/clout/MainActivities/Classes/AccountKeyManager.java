package com.example.clout.MainActivities.Classes;

import android.util.Log;
import com.example.clout.MainActivities.objects.UserObject;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountKeyManager {

    // This class will create an &AccountKey for each user which enters the app
    // It will be used in-tandum with the user object which has not been created yet
    // T.O.D.O: Create user object - done
    UserObject user = new UserObject();
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    private Object AccountKeyManager;

    public String createAccountKey(String emailToPass){
        StringBuilder emailBreak = new StringBuilder(emailToPass);
        String emailAdd = String.valueOf(emailBreak.insert(0, "&")).split("@")[0].toLowerCase();
        //Log.d("accountkeytest", emailAdd);
        user.setAccountKey(emailAdd);
        myRef.child(emailAdd);
        if(user.getAccountKey() == null){
            //Log.d("nullKey", "Nothing was entered");
        }else{
            user.getAccountKey();
        }
        return emailAdd;
    }

    public String reversAccountKeyFromRecyclerViewAdapter(String accountKeyToPass){
        String reversedAccountKey = accountKeyToPass.split(" : ")[0];
        return reversedAccountKey;
    }
}
