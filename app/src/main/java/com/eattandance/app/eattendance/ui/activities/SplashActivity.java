package com.eattandance.app.eattendance.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.eattandance.app.eattendance.R;
import com.eattandance.app.eattendance.ui.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);

        //Checks if user has been logged in and redirect to
        //Homepage if he has, or SignIn page if he hasn't
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    HomeActivity.start(SplashActivity.this);
                }else {
                    SignInActivity.start(SplashActivity.this);
                }
            }
        }, 1000);
    }
}
