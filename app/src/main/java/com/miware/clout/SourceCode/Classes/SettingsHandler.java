package com.miware.clout.SourceCode.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.miware.clout.SourceCode.activities.SettingsActivity;

import org.bouncycastle.util.Store;

import java.util.Random;

public class SettingsHandler extends SettingsActivity {

    /*This class will handle all logic for settings activity*/

    private static final String MyPREFERENCES = "SettingsPreferences" ;
    public SharedPreferences sharedPreferences;
    private String storedKey = "UserKey";

    //Declare Random
    private Random random, randomLetter;
    //Declare english alphabet for randomization
    //private final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //Here we'll create a random alphanumeric user key
    /*The purpose of this key will serve to be
    * a random ID for users who want to stay anonymous*/

    public String generateRandomKey(){
        //create empty string that will become the random key
        String generatedKey = "";

        //Random 9 digit upper bound
        int upperBound = 999999995;
        //Random 9 digit lower bound
        int lowerBound = 000000000;

        random = new Random();
        int random_integer = random.nextInt(upperBound-lowerBound) + lowerBound;
        generatedKey = String.valueOf(random_integer);
        return generatedKey;
    }

    //Get the generated key and hash it with letters
    /*public String hashKey(View view, Context context){
        StringBuilder hashKeyString = new StringBuilder();
        String fullKey = "";
        //Cast string to char array
        char[] turnCaughtKeyTo_CharArray = generateRandomKey().toCharArray();
        //generate random letters
        randomLetter = new Random();

        //loop through and add random letters into the sequence.
        for(char catchChar : turnCaughtKeyTo_CharArray){
            char randomChar = alphabet.charAt(randomLetter.nextInt(alphabet.length()));
            fullKey += turnCaughtKeyTo_CharArray;
            String turnToString = String.valueOf(turnCaughtKeyTo_CharArray);
            if(catchChar % 2 == 0){
                turnToString += randomChar;
            }
            fullKey += turnToString;
        }
        serializeGeneratedKey(view, context);
        return fullKey;
    }*/

    /*Serialize key -- This is the method you will use to both generate
    and serialize random key when button onClick*/
    public void serializeGeneratedKey(Context context){
        sharedPreferences = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.clear();
        edit.putString(storedKey, generateRandomKey());
        edit.apply();
    }

    //Get serialized key
    public String getSerializedKey(Context context, Button button){
        SharedPreferences getSharedPrefs = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String randUserKey = getSharedPrefs.getString(storedKey, "nothing yet");
        button.setText(randUserKey);
        return getSharedPrefs.getString(storedKey, "nothing yet");
    }
}
