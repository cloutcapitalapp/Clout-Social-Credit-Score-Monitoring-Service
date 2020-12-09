package com.miware.clout.MainActivities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miware.clout.MainActivities.IntroFragments.IntroSlide1;
import com.miware.clout.MainActivities.IntroFragments.IntroSlide2;
import com.miware.clout.MainActivities.IntroFragments.IntroSlide3;
import com.miware.clout.R;

public class IntroHubActivity extends AppCompatActivity {

    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_hub);

        introBottomNavigation();
    }

    /* Bottom Navigation for Intro Hub will be handled here */
    public void introBottomNavigation() {
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.welcome_nav) {

                    Menu menuMenu = navView.getMenu();
                    MenuItem menuItemNew = menuMenu.getItem(0);
                    menuItemNew.setChecked(true);

                    IntroSlide1 profileFragment = new IntroSlide1();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.slide_out).replace(R.id.hubLayout, profileFragment).commit();

                    /* Check if 'Slide1' fragment is loading properly */
                    Log.d("Fragment_Side1_Awake: ", "Success");

                } else if (id == R.id.description_nav) {

                    Menu menuMenu = navView.getMenu();
                    MenuItem menuItemNew = menuMenu.getItem(1);
                    menuItemNew.setChecked(true);

                    IntroSlide2 timeLineFragment = new IntroSlide2();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.slide_out).replace(R.id.hubLayout, timeLineFragment).commit();

                    /* Check if 'Slide2' fragment is loading properly */
                    Log.d("Fragment_Side2_Awake", "Success");

                } else if (id == R.id.compliment_nav) {

                    Menu menuMenu = navView.getMenu();
                    MenuItem menuItemNew = menuMenu.getItem(2);
                    menuItemNew.setChecked(true);

                    IntroSlide3 settingsFragment = new IntroSlide3();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.slide_out).replace(R.id.hubLayout, settingsFragment).commit();

                    /* Check if 'Slide3' fragment is loading properly */
                    Log.d("Fragment_Side3_Awake", "Sucess");

                } else if (id == R.id.exit_nav) {

                    Intent toMainHub = new Intent(IntroHubActivity.this, MainActivity.class);
                    startActivity(toMainHub, ActivityOptions.makeSceneTransitionAnimation(IntroHubActivity.this).toBundle());
                    finish();

                    /* Check if 'Exit' fragment is loading properly */
                    Log.d("Fragment_End_Awake: ", "success");

                }
                return false;
            }
        });
    }
}