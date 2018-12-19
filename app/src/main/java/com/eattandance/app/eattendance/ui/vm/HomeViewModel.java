package com.eattandance.app.eattendance.ui.vm;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.eattandance.app.eattendance.models.Attendance;
import com.eattandance.app.eattendance.utils.L;
import com.eattandance.app.eattendance.utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private MutableLiveData<List<Attendance>> mAttendanceLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mCreateAttendanceProgressTracker = new MutableLiveData<>();
    private MutableLiveData<List<Attendance>> mFilterLiveData = new MutableLiveData<>();

    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance()
            .getReference(Util.NODES.ATTENDANCE).child(mFirebaseUser.getUid());

    private List<Attendance> mCached = new ArrayList<>();

    /*
    * List currently logged in user's attendance, it first check if the data is already cached.
    * useful for restoring data when device orientation changed and activity is teared down and recreated
    * */
    public void listAttendance() {

        if (mCached.size() > 0) {
            mAttendanceLiveData.postValue(mCached);
            return;
        }

        listFresh();
    }

    /*
    * listFresh also lists attendance for current user, but it doesn't consult cache
    * */
    public void listFresh() {

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mCached.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Attendance attendance = snapshot.getValue(Attendance.class);
                    mCached.add(attendance);
                }

                mAttendanceLiveData.postValue(mCached);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //post null in case of Error
                mAttendanceLiveData.postValue(null);
            }
        });
    }

    /*
    * submit a new attendance.
    * */
    public void submitAttendance(Attendance attendance) {

        mDatabaseReference.push().setValue(attendance)
                .addOnCompleteListener(task -> {
                    mCreateAttendanceProgressTracker.postValue(task.isSuccessful());
                });
    }

    public MutableLiveData<Boolean> getCreateAttendanceProgressTracker() {
        return mCreateAttendanceProgressTracker;
    }

    public MutableLiveData<List<Attendance>> getAttendanceLiveData() {
        return mAttendanceLiveData;
    }

    /*
    * returns true if an attendance with @param data already exists
    * */
    public boolean attendanceExists(String date) {

        for (Attendance next : mCached) {

            if (next != null && next.date.equalsIgnoreCase(date.trim())) {
                return true;
            }
        }

        return false;
    }

    /*
    * returns true if date passed is a weekend - Saturday || Sunday
    * */
    public boolean isAWeekend(int day, int month, int year) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek == Calendar.SATURDAY
                || dayOfWeek == Calendar.SUNDAY;
    }

    public void filter(String keyword) {

        List<Attendance> result = new ArrayList<>();
        for (Attendance next : mCached) {

            if (next == null) continue;

            if (next.date.contains(keyword)
                    || next.date.startsWith(keyword) || next.date.endsWith(keyword)) {
                result.add(next);
            }
        }

        if (result.size() > 0) {
            mFilterLiveData.postValue(result);
        }
    }
}
