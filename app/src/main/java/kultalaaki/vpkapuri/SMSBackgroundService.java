/*
 * Created by Kultala Aki on 17/5/2022, 10:07 PM
 * Copyright (c) 2022. All rights reserved.
 * Last modified 9/7/2022
 */

package kultalaaki.vpkapuri;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceManager;

import kultalaaki.vpkapuri.alarmdetection.AlarmMessage;
import kultalaaki.vpkapuri.alarmdetection.AlarmNumberDetector;
import kultalaaki.vpkapuri.alarmdetection.NumberLists;
import kultalaaki.vpkapuri.alarmdetection.RescueAlarm;
import kultalaaki.vpkapuri.alarmdetection.SMSMessage;
import kultalaaki.vpkapuri.alarmdetection.VapepaAlarm;
import kultalaaki.vpkapuri.soundcontrols.AlarmMediaPlayer;
import kultalaaki.vpkapuri.soundcontrols.VibrateController;
import kultalaaki.vpkapuri.util.Constants;
import kultalaaki.vpkapuri.util.FormatNumber;

public class SMSBackgroundService extends Service {

    private static int previousStartId = 1;
    private AlarmMediaPlayer alarmMediaPlayer;
    private NumberLists numberLists = null;
    private SharedPreferences preferences;
    SMSMessage message;
    PowerManager.WakeLock wakelock;

    public SMSBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("VPK Apuri käynnissä", "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.i("VPK Apuri", "Service started");
        // Kill process if intent is null
        checkIntent(intent);

        // Acquire wakelock to ensure that android doesn't kill this process
        acquireWakelock();

        // Create SMSMessage object from intent
        formMessage(intent);

        // Create notification to make sure this service doesn't get cancelled
        notificationAlarmMessage();

        // Check starting id of this service and set timer to stop this service
        startIDChecker(startId);

        // Message senderID comes from PhoneNumberDetector.java
        switch (message.getSenderID()) {
            case 0:
                // Not important message to this app,
                // let it go, let it go
                // Can't hold it back anymore
                stopSelf();
                break;
            case 1:
                // Message from alarm provider. Create notification.
                notificationAlarmMessage();

                // Create Alarm object and use formAlarm() method to create it ready.
                RescueAlarm rescueAlarm = new RescueAlarm(this, message);
                rescueAlarm.formAlarm();
                saveAlarm(rescueAlarm);

                String alarmSound = rescueAlarm.getAlarmSound();
                playAlarmSound(alarmSound);
                break;
            case 2:
                // Message from person attending alarm.
                createPersonComingToAlarm();
                break;
            case 3:
                // It is alarm for Vapepa personnel
                // No need to form alarm before saving
                VapepaAlarm vapepaAlarm = new VapepaAlarm(this, message);
                // If user has set different alarm sound for vapepa alarms, then change that
                if (preferences.getBoolean("boolean_vapepa_sound", false)) {
                    vapepaAlarm.setAlarmSound("ringtone_vapepa");
                }
                saveAlarm(vapepaAlarm);

                String vapepaAlarmSound = vapepaAlarm.getAlarmSound();
                playAlarmSound(vapepaAlarmSound);
                break;
        }

        return Service.START_STICKY;
    }

    // Section: Service start procedures
    private void checkIntent(Intent intent) {
        if (intent == null) {
            stopSelf();
        }
    }

    private void acquireWakelock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "VPK Apuri::Hälytys taustalla.");
        }
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

        AlarmNumberDetector alarmDetector = new AlarmNumberDetector();

        // Format number and assign it for sender
        message.setSender(FormatNumber.formatFinnishNumber(message.getSender()));
        // Set sender ID. ID is based on comparing sender number and user set numbers.
        message.setSenderID(alarmDetector.numberID(message.getSender(), numberLists));
    }

    public void notificationAlarmMessage() {
        // This intent is responsible for opening AlarmActivity
        Intent intentsms = new Intent(getApplicationContext(), AlarmActivity.class);
        intentsms.setAction(Intent.ACTION_SEND);
        intentsms.setType("text/plain");
        intentsms.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intentsms);
        PendingIntent pendingIntentWithBackStack = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);

        // Stopping this service when "POISTA ILMOITUS" button in notification is clicked
        // Stopping service also removes notification
        Intent stopAlarm = new Intent(this, StopSMSBackgroundService.class);
        PendingIntent stop = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), stopAlarm, PendingIntent.FLAG_IMMUTABLE);

        // Foreground notification to show user and keeping service alive
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(SMSBackgroundService.this, "HALYTYS")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.alarm))
                .setContentText(message.getMessage())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntentWithBackStack)
                .addAction(R.mipmap.ic_launcher, "POISTA ILMOITUS", stop)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(stop)
                .setAutoCancel(true);

        Notification notification = mBuilder.build();
        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(Constants.ALARM_NOTIFICATION_ID, notification);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            try {
                notificationManager.cancel(15);
            } catch (Exception e) {
                Log.i("IsItAlarmService", "There was not notification to cancel.");
            }
        }
    }

    private void startIDChecker(int startId) {
        if (previousStartId != startId) {
            try {
                alarmMediaPlayer.stopAlarmMedia();
            } catch (Exception e) {
                // Todo: add this error to firebase
                Log.e("VPK Apuri", "Could not stop alarm media player. Error: " + e);
            }
            stopSelf(previousStartId);
        }
        previousStartId = startId;
    }

    public void saveAlarm(AlarmMessage alarm) {
        /* FireAlarmRepository handles saving alarm to database */
        FireAlarmRepository fireAlarmRepository = new FireAlarmRepository(getApplication());
        fireAlarmRepository.insert(new FireAlarm(alarm.getAlarmID(), alarm.getUrgencyClass(),
                alarm.getMessage(), alarm.getAddress(), "", "",
                alarm.getTimeStamp(), alarm.getSender(), "", "", ""));
    }

    void createPersonComingToAlarm() {
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

    }

    private void playAlarmSound(String alarmSound) {
        // Make alarm go boom
        Uri uri = Uri.parse(alarmSound);
        alarmMediaPlayer = new AlarmMediaPlayer(this, preferences, uri);
        if (alarmMediaPlayer.mediaPlayer != null && alarmMediaPlayer.mediaPlayer.isPlaying()) {
            alarmMediaPlayer.stopAlarmMedia();
        }
        if (alarmMediaPlayer.isDoNotDisturbAllowed()) {
            alarmMediaPlayer.audioFocusRequest();
        } else {
            // Todo: Do Not Disturb not allowed, inform user the reason. Maybe with notification?
            // Use vibration notification
            VibrateController vibrateController = new VibrateController(this, preferences);
            vibrateController.vibrateNotification();
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

        if (alarmMediaPlayer != null) {
            alarmMediaPlayer.stopAlarmMedia();
        }

    }
}