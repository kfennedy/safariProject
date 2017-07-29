package com.ahlab.safariteacher;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Katherine on 24/7/17.
 */

public class LogsListAdapter extends ArrayAdapter<Log> {

    private Activity context;
    private ArrayList<Log> logsList;

    public LogsListAdapter(Activity context, ArrayList<Log> objects) {
        super(context, R.layout.each_log, objects);
        this.context = context;
        this.logsList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewLogs = inflater.inflate(R.layout.each_log, null, true);

        final Log log = logsList.get(position);

        TextView dateTimeView = listViewLogs.findViewById(R.id.dateTime);
        dateTimeView.setText(log.getDateTime());

        TextView studentNameView = listViewLogs.findViewById(R.id.studentName);
        studentNameView.setText(log.getStudentName());

        TextView qrNameView = listViewLogs.findViewById(R.id.qrName);
        qrNameView.setText(log.getqrName());

        TextView qrContentView = listViewLogs.findViewById(R.id.qrContent);
        qrContentView.setText(log.getqrContent());

        return listViewLogs;

    }

}
