package com.ahlab.safaristudent;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.speech.tts.TextToSpeech.OnInitListener;


public class ActivityLauncher extends Activity implements OnInitListener, ZXingScannerView.ResultHandler{

    ZXingScannerView zXingScannerView;
    ZXingScannerView.ResultHandler resultHandler;
    DatabaseReference dbMessages;
    DatabaseReference dbMappings;
    DatabaseReference dbLogs;
    DatabaseReference dbSettings;
    DatabaseReference dbAlerts;
    Settings settings;
    ArrayList<Message> messagesList;
    TextToSpeech tts;
    String dateTime;
    String studentName = "unknown";
    String qrName = "";
    String qrContent = "";
    String message0;
    String message1;
    Integer maxCount = 0;
    CountDownTimer timer;
    Integer delayTime = 3000;
    Integer durationTemp;
    Integer durationUrgent;
    Integer durationInterval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_student);

        dbLogs = FirebaseDatabase.getInstance().getReference("logs");
        dbSettings = FirebaseDatabase.getInstance().getReference("settings");
        dbAlerts = FirebaseDatabase.getInstance().getReference("alerts");
        tts = new TextToSpeech(getApplicationContext(), this);
        messagesList = new ArrayList<>();

        pullSettingsFromCloud();
    }


    public void scan(){
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
        resultHandler = this;
    }

    @Override
    public void handleResult(Result result) {

        qrName = result.getText();
        Toast.makeText(getApplicationContext(), qrName, Toast.LENGTH_SHORT).show();
        maxCount += 1;

        // refresh state machine
        timer.cancel();
        startSM1(durationTemp, durationInterval);

        if (maxCount <= messagesList.size()-4){
//            System.out.println("============maxCount=============== " + maxCount);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String aha = messagesList.get(maxCount+1).getMessageContent();
                    tts.speak(aha, TextToSpeech.QUEUE_ADD, null);
//                    System.out.println("============message spoken=============== " + aha);
                }
            }, delayTime);
            processInteraction();
        } else {
//            System.out.println("============maxCount=============== " + maxCount);
            processInteraction();
        }

    }

    public void pullSettingsFromCloud(){
        dbSettings.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbSettings.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String duration1 = dataSnapshot.child("durationTemp").getValue().toString();
                            String duration2 = dataSnapshot.child("durationUrgent").getValue().toString();
                            int dur1 = Integer.valueOf(duration1);
                            int dur2 = Integer.valueOf(duration2);
                            settings = new Settings(dur1,dur2);
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
                durationTemp = settings.getDurationTemp()*1000;
                durationUrgent = settings.getDurationUrgent()*1000;
                pullMessagesFromCloud();
            }
        });

    }

    public void pullMessagesFromCloud(){
        messagesList.clear();
        dbMessages = FirebaseDatabase.getInstance().getReference("messages");
        dbMessages.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbMessages.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data: dataSnapshot.getChildren()){
                                Message message = new Message(data.getKey(), data.getValue().toString());
                                messagesList.add(message);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Toast.makeText(getApplicationContext(), "completes syncing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void processInteraction(){
        dbMappings = FirebaseDatabase.getInstance().getReference("mappings");
        dbMappings.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null){
                    dbMappings.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println(dataSnapshot.child(qrName));
                            qrContent = dataSnapshot.child(qrName).getValue().toString();
//                            System.out.println("========processInteraction========= " + qrContent);
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

                if (qrName.equals("QR009") || qrName.equals("QR010")){
                    studentName = qrContent;
                    processMessage1();
                } else {
                    dateTime = getDateTimeNow();
//                    System.out.println("========onComplete(process interaction)========= " + qrContent);
                    Log log = new Log(dateTime, studentName, qrName, qrContent);
                    dbLogs.child(dateTime).setValue(log);

                    tts.speak(qrContent, TextToSpeech.QUEUE_ADD, null);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            zXingScannerView.resumeCameraPreview(resultHandler);
                        }
                    }, delayTime);
                }
            }
        });
    }

    // this method is invoked from xml file's button
    public void processMessage0(View view1){
        message0 = messagesList.get(0).getMessageContent();
        tts.speak(message0, TextToSpeech.QUEUE_FLUSH, null);
        startSM1(durationTemp, durationInterval);
        scan();
    }

    // if studentName QR codes are scanned
    public void processMessage1(){
        maxCount = 1;
        qrContent = studentName;
        dateTime = getDateTimeNow();
//        System.out.println("========processMessage1========= " + qrContent);
        Log log = new Log(getDateTimeNow(), studentName, qrName, qrContent);
        dbLogs.child(dateTime).setValue(log);

        message1 = messagesList.get(1).getMessageContent();
        tts.speak(message1, TextToSpeech.QUEUE_ADD, null);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                zXingScannerView.resumeCameraPreview(resultHandler);
            }
        }, delayTime);
    }

    // trigger temp alert
    public void startSM1(int duration, final int interval){
//        System.out.println("============ SM 1 started =============");

        timer = new CountDownTimer(duration, interval) {
            public void onTick(long millisUntilFinished) {
//                System.out.println("================= "+String.valueOf(interval/1000)+" second(s) has passed ==============");
            }

            public void onFinish() {
//                System.out.println("========== SM 1 completed ============");
                String tempMessage = messagesList.get(messagesList.size()-2).getMessageContent();
                tts.speak(tempMessage, TextToSpeech.QUEUE_ADD, null);
                
                dateTime = getDateTimeNow();
                int duration = (durationTemp/1000);
                Alert alert = new Alert(dateTime, studentName, "temporary", String.valueOf(duration));
                dbAlerts.child(dateTime).setValue(alert);
                Toast.makeText(getApplicationContext(), "Temporary alert has been sent to teacher", Toast.LENGTH_SHORT).show();

                startSM2(durationUrgent, durationInterval);
            }
        }.start();
    }

    // trigger urgent alert
    public void startSM2(int duration, final int interval){

//        System.out.println("============ SM 2 started =============");

        timer = new CountDownTimer(duration, interval) {
            public void onTick(long millisUntilFinished) {
//                System.out.println("================= "+String.valueOf(interval/1000)+" second(s) has passed ==============");
            }

            public void onFinish() {
//                System.out.println("========== SM 2 completed ============");
                String alertMessage = messagesList.get(messagesList.size()-1).getMessageContent();
                tts.speak(alertMessage, TextToSpeech.QUEUE_ADD, null);

                dateTime = getDateTimeNow();
                int duration = (durationUrgent/1000);
                Alert alert = new Alert(dateTime, studentName, "urgent", String.valueOf(duration));
                dbAlerts.child(dateTime).setValue(alert);
                Toast.makeText(getApplicationContext(), "Urgent alert has been sent to teacher", Toast.LENGTH_SHORT).show();

                startSM2(durationUrgent,durationInterval);
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
        timer.cancel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    public String getDateTimeNow(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        String dateTime = dateFormat.format(cal.getTime()).replace('/','|');
        return dateTime;
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }

    }

}
