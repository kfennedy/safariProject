package com.ahlab.safaristudent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ahlab.safaristudent.camera.CameraSource;
import com.ahlab.safaristudent.camera.CameraSourcePreview;
import com.ahlab.safaristudent.camera.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static xdroid.toaster.Toaster.toast;


/**
 * Created by Katherine on 6/8/17.
 */

public final class CameraLauncher extends Activity implements BarcodeNotifier, TextToSpeech.OnInitListener {

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    private static final String TAG = "Barcode-reader";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private boolean valueBeingProccessed = false;

    DatabaseReference dbMappings;
    DatabaseReference dbLogs;
    DatabaseReference dbSettings;
    DatabaseReference dbMessages;
    DatabaseReference dbAlerts;
    ArrayList<Message> messagesList;
    Settings settings;
    TextToSpeech tts;
    String dateTime;
    String studentName = "unknown";
    String qrName = "";
    String qrContent = "";
    String message0;
    String message1;
    Integer maxCount = 0;
    CountDownTimer timer;
    Integer delayTime = 4000;
    Integer durationTemp;
    Integer durationUrgent;
    Integer durationInterval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);

        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        dbSettings = FirebaseDatabase.getInstance().getReference("settings");
        dbLogs = FirebaseDatabase.getInstance().getReference("logs");
        dbSettings = FirebaseDatabase.getInstance().getReference("settings");
        dbAlerts = FirebaseDatabase.getInstance().getReference("alerts");
        tts = new TextToSpeech(getApplicationContext(), this);
        messagesList =new ArrayList<>();

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
                processMessage0();
            }
        });
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                android.util.Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void notifyBarcode(Barcode barcode) {

        timer.cancel();
//        System.out.println("============ notifyBarcode cancels timer =============");

        if( valueBeingProccessed == false )
        {
            valueBeingProccessed = true;
            maxCount += 1;
            qrName = barcode.displayValue;

            toast(qrName);
            processInteraction();
        }
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
//                            System.out.println(dataSnapshot.child(qrName));
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
                    MyLog log = new MyLog(dateTime, studentName, qrName, qrContent);
                    dbLogs.child(dateTime).setValue(log);

                    tts.speak(replaceSubString(qrContent), TextToSpeech.QUEUE_ADD, null);

                    // true = the scanner will not read
                    valueBeingProccessed = true;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            valueBeingProccessed = false;
                        }
                    }, delayTime);
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processMessageNext();
                    }
                }, delayTime);
            }
        });
        timer.cancel();
//        System.out.println("============ processInteraction cancels timer =============");
    }

    public void processMessage0(){
        message0 = messagesList.get(0).getMessageContent();
        tts.speak(replaceSubString(message0), TextToSpeech.QUEUE_FLUSH, null);

        valueBeingProccessed = true;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSM1(durationTemp, durationInterval);
                valueBeingProccessed = false;
            }
        }, delayTime); //to ensure that the above 2 lines of code only got activated after the first message has completed
    }

    // if studentName QR codes are scanned
    public void processMessage1(){
        maxCount = 1;
        qrContent = studentName;
        dateTime = getDateTimeNow();
//        System.out.println("========processMessage1========= " + qrContent);
        MyLog log = new MyLog(getDateTimeNow(), studentName, qrName, qrContent);
        dbLogs.child(dateTime).setValue(log);

        message1 = messagesList.get(1).getMessageContent();
        tts.speak(replaceSubString(message1), TextToSpeech.QUEUE_ADD, null);

        valueBeingProccessed = true;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startSM1(durationTemp, durationInterval);
                valueBeingProccessed = false;
            }
        }, delayTime);
    }

    public void processMessageNext(){
        if (maxCount <= messagesList.size()-4){
            String aha = messagesList.get(maxCount+1).getMessageContent();
            tts.speak(replaceSubString(aha), TextToSpeech.QUEUE_ADD, null);
        }
        startSM1(durationTemp, durationInterval);
    }

    // trigger temp alert
    public void startSM1(int duration, final int interval){

//        System.out.println("============ SM 1 started =============");
        if(timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(duration, interval) {
            public void onTick(long millisUntilFinished) {
//                System.out.println("================= "+String.valueOf(interval/1000)+" second(s) has passed ==============");
            }

            public void onFinish() {
//                System.out.println("========== SM 1 completed ============");
                String tempMessage = messagesList.get(messagesList.size()-2).getMessageContent();
                tts.speak(replaceSubString(tempMessage), TextToSpeech.QUEUE_ADD, null);

                dateTime = getDateTimeNow();
                int duration = (durationTemp/1000);
                Alert alert = new Alert(dateTime, studentName, "temporary", String.valueOf(duration));
                dbAlerts.child(dateTime).setValue(alert);
                toast("Temporary alert has been sent to teacher");

                startSM2(durationUrgent, durationInterval);
            }
        }.start();
    }

    // trigger urgent alert
    public void startSM2(int duration, final int interval){

//        System.out.println("============ SM 2 started =============");
        if(timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(duration, interval) {
            public void onTick(long millisUntilFinished) {
//                System.out.println("================= "+String.valueOf(interval/1000)+" second(s) has passed ==============");
            }

            public void onFinish() {
//                System.out.println("========== SM 2 completed ============");
                String alertMessage = messagesList.get(messagesList.size()-1).getMessageContent();
                tts.speak(replaceSubString(alertMessage), TextToSpeech.QUEUE_ADD, null);

                dateTime = getDateTimeNow();
                int duration = (durationUrgent/1000);
                Alert alert = new Alert(dateTime, studentName, "urgent", String.valueOf(duration));
                dbAlerts.child(dateTime).setValue(alert);
                toast("Urgent alert has been sent to teacher");

                startSM2(durationUrgent,durationInterval);
            }
        }.start();
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

    public String replaceSubString(String string){
        if (string.contains("@studentName")){
            String newString = string.replace("@studentName", studentName);
            return newString;
        } else {
            return string;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
            timer.cancel();
//            System.out.println("============ SM cancelled =============");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
            timer.cancel();
//            System.out.println("============ SM cancelled =============");

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
//        System.out.println("============ SM cancelled =============");

    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        barcodeFactory.setBarcodeNotifier(this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            android.util.Log.w(TAG, "Detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                android.util.Log.w(TAG, getString(R.string.low_storage_error));
            }
        }
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
//                .setRequestedPreviewSize(1600, 1024)
                .setRequestedPreviewSize(1700, 1088) // not sure how to get it full screen, this one has a side bar
                .setRequestedFps(15.0f);

        builder.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCameraSource = builder.build();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        android.util.Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{android.Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            android.util.Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d(TAG, "Camera permission granted - initialize the camera source");
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton("OK", listener)
                .show();
    }
}
