/*
 * Created by Kultala Aki on 5/17/22, 10:07 PM
 * Copyright (c) 2022. All rights reserved.
 * Last modified 5/17/22, 9:44 PM
 */

package kultalaaki.vpkapuri;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;

import kultalaaki.vpkapuri.alarmdetection.Alarm;
import kultalaaki.vpkapuri.alarmdetection.NumberFormatter;
import kultalaaki.vpkapuri.alarmdetection.NumberLists;
import kultalaaki.vpkapuri.alarmdetection.PhoneNumberDetector;
import kultalaaki.vpkapuri.alarmdetection.SMSMessage;

public class SMSBackgroundService extends Service {

    private static final String TAG = "VPK Apuri käynnissä.";
    private static final int MY_ALARM_NOTIFICATION_ID = 264981;
    private static int previousStartId = 1;

    private NumberLists numberLists = null;

    private SharedPreferences preferences;
    SMSMessage message;

    PowerManager powerManager;
    PowerManager.WakeLock wakelock;

    public SMSBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, final int startId) {

        // Kill process if intent is null
        checkIntent(intent);

        // Acquire wakelock to ensure that android doesn't kill this process
        acquireWakelock();

        // Create notification to make sure this service doesn't get cancelled
        startForegroundNotificationVersion2();

        // Check starting id of this service
        startIDChecker(startId);

        // Create SMSMessage object from intent
        formMessage(intent);


        // Message senderID comes from PhoneNumberDetector.java
        switch (message.getSenderID()) {
            case 0:
                // Not important message to this app,
                // let it go, let it go
                // Can't hold it back anymore
                stopSelf();
                break;
            case 1:
                // Message from alarm provider. Needs reaction
                // Todo: Make alarm go loud
                // Create Alarm object and use formAlarm() method to create it ready.
                //
                Alarm alarm = new Alarm(message.getSender(), message.getMessage(), message.getTimeStamp());
                alarm.formAlarm();

                saveAlarm(alarm);
                break;
            case 2:
                // Message from person attending alarm.
                String positionInList = Integer.toString(numberLists.getIndexPositionOfMember(message.getSender()) + 1);
                String name = preferences.getString("nimi" + positionInList, null);
                boolean driversLicense = preferences.getBoolean("kortti" + positionInList, false);
                boolean smoke = preferences.getBoolean("savusukeltaja" + positionInList, false);
                boolean chemical = preferences.getBoolean("kemikaalisukeltaja" + positionInList, false);
                boolean leader = preferences.getBoolean("yksikonjohtaja" + positionInList, false);
                String vacancyNumber = preferences.getString("vakanssinumero" + positionInList, null);
                String optional1 = preferences.getString("optional1_" + positionInList, null);
                String optional2 = preferences.getString("optional2_" + positionInList, null);
                String optional3 = preferences.getString("optional3_" + positionInList, null);
                String optional4 = preferences.getString("optional4_" + positionInList, null);
                String optional5 = preferences.getString("optional5_" + positionInList, null);
                String driver = "";
                String smok = "";
                String chem = "";
                String lead = "";
                if (driversLicense) {
                    driver = "C";
                }
                if (smoke) {
                    smok = "S";
                }
                if (chemical) {
                    chem = "K";
                }
                if (leader) {
                    lead = "Y";
                }

                Responder responder = new Responder(name, vacancyNumber, message.getMessage(), lead, driver, smok, chem, optional1, optional2, optional3, optional4, optional5);
                ResponderRepository repository = new ResponderRepository(getApplication());
                repository.insert(responder);

                Toast.makeText(this, name + " lähetti ilmoituksen.", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                // It is alarm for Vapepa personnel
                // No need to form alarm before saving
                Alarm alarm1 = new Alarm(message.getSender(), message.getMessage(), message.getTimeStamp());
                saveAlarm(alarm1);
                // Todo: Make alarm go loud
        }

        return Service.START_STICKY;
    }

    private void checkIntent(Intent intent) {
        if (intent == null) {
            stopSelf();
        }
    }

    private void acquireWakelock() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "VPK Apuri::Hälytys taustalla.");
        }
    }

    private void startIDChecker(int startId) {
        if (previousStartId != startId) {
            stopSelf(previousStartId);
        }
        previousStartId = startId;
    }

    /**
     * Creates SMSMessage object
     * Is used for detecting sender
     * SMSMessage object gets ID based on sender number
     * ID defines what app needs to do
     */
    private void formMessage(Intent intent) {
        // Take sms message from broadcastreceiver and make it object
        message = new SMSMessage(intent.getStringExtra("number"),
                intent.getStringExtra("message"),
                intent.getStringExtra("timestamp"));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        numberLists = new NumberLists(preferences);

        PhoneNumberDetector phoneNumberDetector = new PhoneNumberDetector();
        NumberFormatter formatter = new NumberFormatter();

        String senderNumber = formatter.formatNumber(message.getSender());

        message.setSenderID(phoneNumberDetector.whoSent(senderNumber, numberLists));
    }

    public void saveAlarm(Alarm alarm) {
        /* FireAlarmRepository handles saving alarm to database */
        FireAlarmRepository fireAlarmRepository = new FireAlarmRepository(getApplication());
        fireAlarmRepository.insert(new FireAlarm(alarm.getAlarmID(), alarm.getUrgencyClass(),
                alarm.getMessage(), alarm.getAddress(), "", "",
                alarm.getTimeStamp(), alarm.getSender(), "", "", ""));

        Log.i("VPK Apuri", "alarm came through");
        Log.i("Alarm sender: ", alarm.getSender());
        Log.i("Alarm message: ", alarm.getMessage());
        Log.i("Alarm address: ", alarm.getAddress());
        Log.i("Alarm ID: ", alarm.getAlarmID());
        Log.i("Alarm text: ", alarm.getAlarmTextField());
        Log.i("Alarm timestamp: ", alarm.getTimeStamp());
    }

    public void startForegroundNotification() {
        Notification.Builder builder = new Notification.Builder(this, "ACTIVE SERVICE")
                .setContentTitle("VPK Apuri")
                .setContentText("Viestin tarkistus menossa!")
                .setAutoCancel(true);

        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ACTIVE SERVICE", "ACTIVE SERVICE", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("VPK Apuri käynnissä.");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        startForeground(MY_ALARM_NOTIFICATION_ID, notification);
    }

    public void startForegroundNotificationVersion2() {
        Log.i("TAG", "service notification method");
        Intent notificationIntent = new Intent(this, AlarmActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, "ACTIVE SERVICE")
                        .setContentTitle("VPK Apuri")
                        .setContentText("Viestin tarkistus menossa")
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        startForeground(12345, notification);
    }

    // Todo this whole method needs to be redone
    public void notificationAlarmMessage(String message) {
        Intent intentsms = new Intent(getApplicationContext(), AlarmActivity.class);
        intentsms.setAction(Intent.ACTION_SEND);
        intentsms.setType("text/plain");
        intentsms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intentsms);
        PendingIntent pendingIntentWithBackStack = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopAlarm = new Intent(this, StopSMSBackgroundService.class);
        PendingIntent stop = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), stopAlarm, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SMSBackgroundService.this, "HALYTYS")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.alarm))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntentWithBackStack)
                // Todo find solution to this error .addAction(R.mipmap.ic_launcher, "HILJENNÄ", stop)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(stop)
                .setAutoCancel(true);

        Notification notification = mBuilder.build();
        startForeground(MY_ALARM_NOTIFICATION_ID, notification);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            try {
                notificationManager.cancel(15);
            } catch (Exception e) {
                Log.i("IsItAlarmService", "There was not notification to cancel.");
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (wakelock != null) {
            try {
                wakelock.release();
            } catch (Throwable th) {
                // No Need to do anything.
            }

        }
    }
}