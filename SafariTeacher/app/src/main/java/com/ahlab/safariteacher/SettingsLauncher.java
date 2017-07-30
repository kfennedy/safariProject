package com.ahlab.safariteacher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Katherine on 28/7/17.
 */

public class SettingsLauncher extends Activity {

    DatabaseReference dbSettings;
    DatabaseReference dbMessages;
    Integer durationTemp;
    Integer durationUrgent;
    String messageTemp;
    String messageUrgent;
    TextView tempMessageEdit;
    TextView tempDurationEdit;
    TextView urgentMessageEdit;
    TextView urgentDurationEdit;

    String messageTempID = "message6";
    String messageUrgentID = "message7";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_settings);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pullSettingsFromCloud();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        tempMessageEdit = findViewById(R.id.tempMessageEdit);
        tempMessageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("temp", "message");
            }
        });

        tempDurationEdit = findViewById(R.id.tempDurationEdit);
        tempDurationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("temp", "duration");
            }
        });

        urgentMessageEdit = findViewById(R.id.urgentMessageEdit);
        urgentMessageEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("urgent", "message");
            }
        });

        urgentDurationEdit = findViewById(R.id.urgentDurationEdit);
        urgentDurationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("urgent", "duration");
            }
        });

        dbSettings = FirebaseDatabase.getInstance().getReference("settings");
    }

    @Override
    protected void onStart() {
        super.onStart();

        pullSettingsFromCloud();
    }

    public void pullSettingsFromCloud(){
        dbSettings.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbSettings.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            durationTemp = Integer.parseInt(dataSnapshot.child("durationTemp").getValue().toString());
                            durationUrgent = Integer.parseInt(dataSnapshot.child("durationUrgent").getValue().toString());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                pullMessagesFromCloud();
            }
        });

    }

    public void pullMessagesFromCloud(){
        dbMessages = FirebaseDatabase.getInstance().getReference("messages");
        dbMessages.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbMessages.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            messageTemp = dataSnapshot.child(messageTempID).getValue().toString();
                            messageUrgent = dataSnapshot.child(messageUrgentID).getValue().toString();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                updateView();
            }
        });
    }

    public void updateView(){
        String m1 = String.valueOf(durationTemp) + " seconds" ;
        tempMessageEdit.setText(messageTemp);
        tempDurationEdit.setText(m1);

        String m2 = String.valueOf(durationUrgent) + " seconds" ;
        urgentMessageEdit.setText(messageUrgent);
        urgentDurationEdit.setText(m2);
    }

    public void promptDialog(final String alertType, final String contentType){

        LayoutInflater inflater = this.getLayoutInflater();
        View promptsView = inflater.inflate(R.layout.dialog_edit_content, null);

        final EditText userInput = promptsView.findViewById(R.id.userInput);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Insert new " + contentType );

        if (contentType == "duration"){
            userInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            userInput.setHint("in seconds");
            userInput.setHintTextColor(getResources().getColor(R.color.light_gray));
        }

        // set dialog message
        alertDialogBuilder
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String newContent = userInput.getText().toString();
                                updateContentOnline(alertType, contentType, newContent);
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

    public void updateContentOnline(String alertType, String contentType, String newContent){
        if (contentType == "message"){
            if (alertType == "temp"){
                dbMessages.child(messageTempID).setValue(newContent);
            } else if (alertType == "urgent"){
                dbMessages.child(messageUrgentID).setValue(newContent);
            }
        } else if (contentType == "duration"){
            if (alertType == "temp"){
                dbSettings.child("durationTemp").setValue(newContent);
            } else if (alertType == "urgent"){
                dbSettings.child("durationUrgent").setValue(newContent);
            }
        }
        Toast.makeText(getApplicationContext(), "updated", Toast.LENGTH_SHORT).show();
    }
}
