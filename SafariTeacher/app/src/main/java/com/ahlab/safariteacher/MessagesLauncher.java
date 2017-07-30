package com.ahlab.safariteacher;

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

public class MessagesLauncher extends ActivityLauncher {

    DatabaseReference dbMessages;
    String message0;
    String message1;
    String message2;
    String message3;
    String message4;
    String message5;
    TextView messageView0;
    TextView messageView1;
    TextView messageView2;
    TextView messageView3;
    TextView messageView4;
    TextView messageView5;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_message);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pullMessagesFromCloud();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageView0 = findViewById(R.id.messageView0);
        messageView0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("message0");
            }
        });

        messageView1 = findViewById(R.id.messageView1);
        messageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("message1");
            }
        });

        messageView2 = findViewById(R.id.messageView2);
        messageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("message2");
            }
        });

        messageView3 = findViewById(R.id.messageView3);
        messageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("message3");
            }
        });

        messageView4 = findViewById(R.id.messageView4);
        messageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("message4");
            }
        });
        messageView5 = findViewById(R.id.messageView5);
        messageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptDialog("message5");
            }
        });

        dbMessages = FirebaseDatabase.getInstance().getReference("messages");
    }

    @Override
    protected void onStart() {
        super.onStart();
        pullMessagesFromCloud();
    }

    public void pullMessagesFromCloud(){
        dbMessages.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbMessages.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            message0 = dataSnapshot.child("message0").getValue().toString();
                            message1 = dataSnapshot.child("message1").getValue().toString();
                            message2 = dataSnapshot.child("message2").getValue().toString();
                            message3 = dataSnapshot.child("message3").getValue().toString();
                            message4 = dataSnapshot.child("message4").getValue().toString();
                            message5 = dataSnapshot.child("message5").getValue().toString();
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
        messageView0.setText(message0);
        messageView1.setText(message1);
        messageView2.setText(message2);
        messageView3.setText(message3);
        messageView4.setText(message4);
        messageView5.setText(message5);
    }

    public void promptDialog(final String messageIndex){
        LayoutInflater inflater = this.getLayoutInflater();
        View promptsView = inflater.inflate(R.layout.dialog_edit_content, null);

        final EditText userInput = promptsView.findViewById(R.id.userInput);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setTitle("Insert new message");

        // set dialog message
        alertDialogBuilder
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String newContent = userInput.getText().toString();
                                updateContentOnline(messageIndex, newContent);
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

    public void updateContentOnline(String messageIndex, String newContent){
        dbMessages.child(messageIndex).setValue(newContent);
        Toast.makeText(getApplicationContext(), "updated", Toast.LENGTH_SHORT).show();
    }
}
