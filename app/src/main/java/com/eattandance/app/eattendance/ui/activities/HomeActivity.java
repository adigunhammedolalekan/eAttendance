package com.eattandance.app.eattendance.ui.activities;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.eattandance.app.eattendance.R;
import com.eattandance.app.eattendance.models.Attendance;
import com.eattandance.app.eattendance.ui.adapters.AttendanceListAdapter;
import com.eattandance.app.eattendance.ui.base.BaseActivity;
import com.eattandance.app.eattendance.ui.vm.HomeViewModel;
import com.eattandance.app.eattendance.utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, HomeActivity.class);
        starter.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @BindView(R.id.layout_no_attendance)
    LinearLayout mNoAttendanceLayout;
    @BindView(R.id.rv_attendance)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout_main)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.edt_search_main)
    EditText searchEditText;

    private ProgressDialog progressDialog;

    private HomeViewModel mViewModel;
    private List<Attendance> mAttendanceList = new ArrayList<>();
    private AttendanceListAdapter mAttendanceListAdapter = new AttendanceListAdapter(mAttendanceList);
    private int mYearPicked, mMonthPicked, mDayPicked, mHourPicked, mMinutesPicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        listAttendance();
        Util.hideKeyboard(this);

        subscribeToLiveDataEvents();
        createRecyclerView();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listAttendance();
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (mAttendanceListAdapter != null) {
                    mAttendanceListAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void listAttendance() {

        mSwipeRefreshLayout.setRefreshing(true);
        mViewModel.listAttendance();
    }

    private void subscribeToLiveDataEvents() {

        mViewModel.getAttendanceLiveData().observe(this, new Observer<List<Attendance>>() {
            @Override
            public void onChanged(@Nullable List<Attendance> attendances) {

                mSwipeRefreshLayout.setRefreshing(false);
                if (attendances == null) {

                    toast("Oops! Network error!");
                    return;
                }

                if (attendances.size() <= 0) {
                    show(mNoAttendanceLayout);
                    return;
                }

                hide(mNoAttendanceLayout);
                mAttendanceList.clear();
                mAttendanceList.addAll(attendances);

                if (mAttendanceListAdapter != null) {
                    mAttendanceListAdapter.notifyDataSetChanged();
                }
            }
        });

        mViewModel.getCreateAttendanceProgressTracker().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {

                if (progressDialog != null) {
                    progressDialog.cancel();
                }

                if (aBoolean != null && !aBoolean.booleanValue()) {
                    //Error has occurred
                    showDialog("Error", "Failed to submit attendance. Please retry");
                    return;
                }

                toast("Attendance submitted");
                mViewModel.listFresh();
            }
        });
    }

    private void createRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAttendanceListAdapter);
    }

    @OnClick(R.id.btn_new_attendance) public void onNewAttendanceClick() {

        showDatePicker();
    }

    @OnClick(R.id.btn_sign_out) public void onSignOutClick() {

        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Do you want to log out?")
                .setPositiveButton("YES", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }).setNegativeButton("NO", (dialog, which) -> {

                    dialog.dismiss();
                }).create().show();
    }

    private void showDatePicker() {

        Calendar now = Calendar.getInstance();
        DatePickerDialog.newInstance((view, year, monthOfYear, dayOfMonth) -> {

            mYearPicked = year;
            mMonthPicked = monthOfYear;
            mDayPicked = dayOfMonth;

            showTimePicker(); //Picker Time
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "DatePicker");

    }

    private void showTimePicker() {

        TimePickerDialog.newInstance(
                (view, hourOfDay, minute, second) -> {

                    mHourPicked = hourOfDay;
                    mMinutesPicked = minute;

                    submitAttendance();
                }, false).show(getFragmentManager(), "TimePicker");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void submitAttendance() {

        //Format date into form DD/MM/YYYY HH:MM
        String date = mDayPicked + "/" + (mMonthPicked + 1) + "/" + mYearPicked + " " + mHourPicked + ":" + mMinutesPicked;
        if (mViewModel.attendanceExists(date)) {
            toast("Attendance has already been submitted");
            return;
        }

        if (mViewModel.isAWeekend(mDayPicked, mMonthPicked, mYearPicked)) {
            toast("Cannot submit an attendance on a weekend");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting attendance...");
        progressDialog.show();

        Attendance attendance = new Attendance();
        attendance.date = date;
        mViewModel.submitAttendance(attendance);
    }
}
