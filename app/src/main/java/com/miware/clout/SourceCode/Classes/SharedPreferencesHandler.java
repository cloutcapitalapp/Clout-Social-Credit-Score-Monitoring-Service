package com.miware.clout.SourceCode.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.miware.clout.SourceCode.activities.MainActivity;

public class SharedPreferencesHandler extends MainActivity {

    private boolean isTicked = false;
    private static final String MyBoolPrefs = "BoolPrefs" ;
    private SharedPreferences BooleanPrefs;
    private String boolPrefKey;

    public boolean useRandomKeyAtTick(Context context, CheckBox checkBoxComponent){
        checkBoxComponent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isTicked = isChecked;
                if(isTicked = true){
                    setTickedToSharedPrefs(context, checkBoxComponent);
                }
            }
        });
        return isTicked;
    }
    public void setTickedToSharedPrefs(Context context, CheckBox checkBoxComponent){
        BooleanPrefs = context.getSharedPreferences(MyBoolPrefs, MODE_PRIVATE);
        SharedPreferences.Editor edit = BooleanPrefs.edit();
        edit.clear();
        edit.putBoolean(boolPrefKey, useRandomKeyAtTick(context, checkBoxComponent));
        edit.apply();
    }
}
