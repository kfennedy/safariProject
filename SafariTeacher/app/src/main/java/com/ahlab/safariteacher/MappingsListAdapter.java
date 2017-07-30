package com.ahlab.safariteacher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Katherine on 24/7/17.
 */

public class MappingsListAdapter extends ArrayAdapter<Mapping> {

    private Activity context;
    private ArrayList<Mapping> mappingsList;
    private DatabaseReference dbMappings;

    public MappingsListAdapter(Activity context, ArrayList<Mapping> objects) {
        super(context, R.layout.each_log, objects);
        this.context = context;
        this.mappingsList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int pos = position;

        LayoutInflater inflater = context.getLayoutInflater();
        View listViewMappings = inflater.inflate(R.layout.each_mapping, null, true);

        final Mapping mapping = mappingsList.get(position);
        dbMappings = FirebaseDatabase.getInstance().getReference("mappings");

        TextView qrNameView = listViewMappings.findViewById(R.id.qrNameView);
        qrNameView.setText(mapping.getQrName());

        TextView qrContentView = listViewMappings.findViewById(R.id.qrContentView);
        qrContentView.setText(mapping.getQrContent());

        qrContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialogContent(pos);
            }
        });

        return listViewMappings;
    }

    public void promptDialogContent(final int position){
        LayoutInflater inflater = context.getLayoutInflater();
        View promptsView = inflater.inflate(R.layout.dialog_edit_content, null);

        final EditText userInput = promptsView.findViewById(R.id.userInput);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Insert new content" );

        // set dialog message
        alertDialogBuilder
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String newContent = userInput.getText().toString();
                                updateContentOnline(position, newContent);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void updateContentOnline(int position, String newContent){
        String pos = String.format("%03d", (position + 1));
        String messageIndex = "QR" + pos;
        dbMappings.child(messageIndex).setValue(newContent);
        Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show();
    }

}
