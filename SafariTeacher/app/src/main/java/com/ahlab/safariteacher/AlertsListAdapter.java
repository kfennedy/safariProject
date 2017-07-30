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

public class AlertsListAdapter extends ArrayAdapter<Alert> {

    private Activity context;
    private ArrayList<Alert> alertsList;

    public AlertsListAdapter(Activity context, ArrayList<Alert> objects) {
        super(context, R.layout.each_log, objects);
        this.context = context;
        this.alertsList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewAlerts = inflater.inflate(R.layout.each_alert, null, true);

        final Alert alert = alertsList.get(position);

        TextView dateTimeView = listViewAlerts.findViewById(R.id.dateTime);
        dateTimeView.setText(alert.getDateTime());

        TextView studentNameView = listViewAlerts.findViewById(R.id.studentName);
        studentNameView.setText(alert.getStudentName());

        TextView alertTypeView = listViewAlerts.findViewById(R.id.alertType);
        alertTypeView.setText(alert.getAlertType());

        TextView durationView = listViewAlerts.findViewById(R.id.durationElapsed);
        durationView.setText(alert.getDurationElapsed() + " sec");

        return listViewAlerts;

    }

}
