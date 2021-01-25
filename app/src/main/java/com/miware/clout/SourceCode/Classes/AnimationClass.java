package com.miware.clout.SourceCode.Classes;

import android.animation.ObjectAnimator;
import android.widget.Button;

import com.miware.clout.SourceCode.activities.MainActivity;

public class AnimationClass extends MainActivity {
    public void animation(){
        /**These are animations
         * openingAnimation will run onCreate
         * reverseAnimation will run on return to sign up tab*/
    }

    public void openingAnimation(Button buttonToAnimate){
        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(buttonToAnimate,
                "translationY", -90f);
        animCashButton.setDuration(500);
        animCashButton.start();
    }
    public void reverseAnimation(Button buttonToAnimate){
        ObjectAnimator animCashButton = ObjectAnimator.ofFloat(buttonToAnimate,
                "translationY", 90f);
        animCashButton.setDuration(500);
        animCashButton.start();
    }
}
