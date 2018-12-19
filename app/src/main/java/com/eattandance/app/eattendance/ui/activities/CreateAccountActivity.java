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
import com.eattandance.app.eattendance.models.User;
import com.eattandance.app.eattendance.ui.base.BaseActivity;
import com.eattandance.app.eattendance.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.OnClick;

public class CreateAccountActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, CreateAccountActivity.class);
        context.startActivity(starter);
    }

    @BindView(R.id.edt_email_sign_up)
    EditText emailEditText;
    @BindView(R.id.edt_name_sign_up)
    EditText nameEditText;
    @BindView(R.id.edt_password_sign_up)
    EditText passwordEditText;

    //Firebase auth
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sign_up);

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_create_account) public void onCreateAccountClick() {

        final String email = Util.textOf(emailEditText);
        final String name = Util.textOf(nameEditText); // name is optional. No validation check
        final String password = Util.textOf(passwordEditText);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Enter a valid password");
            return;
        }


        final ProgressDialog dialog = Util.dialog(this, "Creating account...");
        dialog.show();
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        dialog.cancel();
                        Exception ex = task.getException();
                        if (ex != null) {

                            if (ex instanceof FirebaseAuthUserCollisionException) {

                                toast("Someone is already using that email");
                                return;
                            }

                            toast(ex.getMessage());
                            return;
                        }

                        User user = new User();
                        user.email = email;
                        user.name = name;
                        createUserData(user);

                        HomeActivity.start(CreateAccountActivity.this);
                    }
                });
    }

    private void createUserData(User user) {

        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser != null) {

            DatabaseReference reference =
                    FirebaseDatabase.getInstance().getReference(Util.NODES.USERS);
            reference.child(authUser.getUid()).setValue(user);
        }
    }

    @OnClick(R.id.btn_sign_in_2) public void onSignInClick() {
        SignInActivity.start(this);
    }
}
