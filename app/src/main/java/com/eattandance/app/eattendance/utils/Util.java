package com.eattandance.app.eattendance.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class Util {

    /*
    * Handy utils
    * */

    public static final SimpleDateFormat SDF = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    public static String textOf(EditText editText) {

        if(editText == null) return "";

        return editText.getText().toString().trim();
    }

    public static ProgressDialog dialog(Context context, String title) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(title);
        return progressDialog;
    }

    public static void hideKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        try {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }catch (Exception e) {
            //if there is NPE
        }
    }
    public static String message() {
        return "Failed to complete request. Please retry";
    }

    public final static class NODES {

        public static final String USERS = "users";
        public static final String ATTENDANCE = "attendance";
    }

}
