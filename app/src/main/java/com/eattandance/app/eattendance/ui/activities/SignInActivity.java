package com.eattandance.app.eattendance.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.widget.EditText;

import com.eattandance.app.eattendance.R;
import com.eattandance.app.eattendance.ui.base.BaseActivity;
import com.eattandance.app.eattendance.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import butterknife.BindView;
import butterknife.OnClick;

public class SignInActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SignInActivity.class);
        context.startActivity(starter);
    }

    @BindView(R.id.edt_email_sign_in)
    EditText emailEditText;
    @BindView(R.id.edt_password_sign_in)
    EditText passwordEditText;

    //Firebase auth
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_sign_in) public void onSignInClick() {

        final String email = Util.textOf(emailEditText);
        final String password = Util.textOf(passwordEditText);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Enter a valid password");
            return;
        }

        final ProgressDialog dialog = Util.dialog(this, "Processing...");
        dialog.show();
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        dialog.cancel();
                        Exception ex = task.getException();
                        if (ex != null) {

                            if (ex instanceof FirebaseAuthInvalidCredentialsException) {

                                toast("Invalid login credentials. Please check and try again.");
                                return;
                            }

                            toast(ex.getMessage());
                            return;
                        }

                        //all good
                        HomeActivity.start(SignInActivity.this);
                    }
                });
    }

    @OnClick(R.id.btn_create_new_account) public void onCreateAccountClick() {

        CreateAccountActivity.start(this);
    }
}
