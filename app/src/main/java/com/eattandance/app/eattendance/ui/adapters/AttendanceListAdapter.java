package com.eattandance.app.eattendance.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.eattandance.app.eattendance.R;
import com.eattandance.app.eattendance.models.Attendance;
import com.eattandance.app.eattendance.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class AttendanceListAdapter extends
        RecyclerView.Adapter<AttendanceListAdapter.AttendanceViewHolder> implements Filterable {

    private List<Attendance> mAttendanceList = new ArrayList<>();
    private List<Attendance> copy = new ArrayList<>(); //Copy of the original list

    public AttendanceListAdapter(List<Attendance> attendances) {
        mAttendanceList = attendances;
        copy = attendances;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AttendanceViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_attendance, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {

        Attendance attendance = mAttendanceList.get(position);
        final String title = "#Attendance " + position;
        holder.titleTextView.setText(title);
        holder.dateTextView.setText(attendance.date);
    }

    @Override
    public int getItemCount() {
        return mAttendanceList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults;
                if (constraint == null || constraint.toString().trim().length() <= 0) {
                    filterResults = new FilterResults();
                    filterResults.count = 0;
                    filterResults.values = null;
                    return filterResults;
                } else {
                    List<Attendance> filter = filterList(constraint.toString().trim());
                    FilterResults results = new FilterResults();
                    results.count = filter.size();
                    results.values = filter;
                    return results;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (results == null || results.count <= 0) {
                    mAttendanceList.clear();
                    mAttendanceList = copy;
                    notifyDataSetChanged();
                    return;
                }
                mAttendanceList = (List) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<Attendance> filterList(String keyword) {

        List<Attendance> result = new ArrayList<>();
        for (Attendance next : mAttendanceList) {

            if (next == null) continue;

            if (next.date.contains(keyword)
                    || next.date.startsWith(keyword) || next.date.endsWith(keyword)) {
                result.add(next);
            }
        }

        return result;
    }

    class AttendanceViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_attendance_title)
        TextView titleTextView;
        @BindView(R.id.tv_date_layout_attendance)
        TextView dateTextView;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
        }
    }
}
