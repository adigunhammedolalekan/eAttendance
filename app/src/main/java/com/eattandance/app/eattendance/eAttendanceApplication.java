package com.eattandance.app.eattendance;

import android.app.Application;

public class eAttendanceApplication extends Application {

    private static eAttendanceApplication attendanceApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        attendanceApplication = this;
    }

    public static eAttendanceApplication getApplication() {
        return attendanceApplication;
    }
}
