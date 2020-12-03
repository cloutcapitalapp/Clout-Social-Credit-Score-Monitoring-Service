package com.miware.clout.MainActivities.Classes;

import android.content.Context;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddFriendHandler {
    AccountKeyManager accKey = new AccountKeyManager();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersFriendRef = database.getReference("User_Friends_List");
    DatabaseReference usersRef = database.getReference("User");
    FirebaseUser mCurrentUser = mAuth.getCurrentUser();

    // variable to hold context
    private Context context;

//save the context recievied via constructor in a local variable

    public AddFriendHandler(Context context){
        this.context=context;
    }
    /** This class must handle all friend adding needs
     * need 1 - add friends to users_friends_list
     * need 2 - delete friends from users_friends_list
     *
     * the AddFriend method will require 2 parameters
     * param 1 - startingUser
     * param 2 - ending user
     *
     * The db entry will have a main node of "Users" that will have children of specific users.
     * Those specific users will have child nodes of each of that users friends.
     * The friends will then be displayed as recyclerList items.
     * The friends list will show each of the users friends with the profile image, the cloutscore
     * and the usersName and as such must store that data in the database to be retrieved by the
     * recyclerView
     *
     * */
    public void AddFriend(String endingUser){
        usersFriendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("chedkAddFriend", "Were good Outer");
                //String username = snapshot1.child(endingUser).getKey();
                String value = snapshot.getKey();
                //Log.d("checkValue", "" + value);
                usersFriendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child(endingUser).setValue("Added");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void deleteFriend(){
        usersFriendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("chedkAddFriend", "Were good Outer");
                //String username = snapshot1.child(endingUser).getKey();
                usersFriendRef.child(accKey.createAccountKey(mCurrentUser.getEmail()));
                usersFriendRef.child(accKey.createAccountKey(mCurrentUser.getEmail())).child("UserName").removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
