/*
 * Created by Kultala Aki on 4/24/21 9:34 AM
 * Copyright (c) 2021. All rights reserved.
 * Last modified 3/20/21 1:02 PM
 */

package kultalaaki.vpkapuri;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import kultalaaki.vpkapuri.Fragments.AffirmationFragment;
import kultalaaki.vpkapuri.Fragments.ArchiveFragment;
import kultalaaki.vpkapuri.Fragments.ArchivedAlarmFragment;
import kultalaaki.vpkapuri.Fragments.ChangelogFragment;
import kultalaaki.vpkapuri.Fragments.FrontpageFragment;
import kultalaaki.vpkapuri.Fragments.GuidelineFragment;
import kultalaaki.vpkapuri.Fragments.SaveToArchiveFragment;
import kultalaaki.vpkapuri.Fragments.SetTimerFragment;
import kultalaaki.vpkapuri.Fragments.TimerFragment;
import kultalaaki.vpkapuri.databasebackupandrestore.FireAlarmJSONWriter;
import kultalaaki.vpkapuri.databasebackupandrestore.JSONArrayReader;
import kultalaaki.vpkapuri.dbfirealarm.FireAlarm;
import kultalaaki.vpkapuri.dbfirealarm.FireAlarmRepository;
import kultalaaki.vpkapuri.misc.DBTimer;
import kultalaaki.vpkapuri.misc.SoundControls;
import kultalaaki.vpkapuri.services.SMSBackgroundService;
import kultalaaki.vpkapuri.util.Constants;
import kultalaaki.vpkapuri.util.MyNotifications;
import kultalaaki.vpkapuri.versioncheck.VersionDataProcessor;
import kultalaaki.vpkapuri.versioncheck.VersionInfo;


public class FrontpageActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, AffirmationFragment.Listener, FrontpageFragment.OnFragmentInteractionListener, ArchiveFragment.OnFragmentInteractionListener, GuidelineFragment.OnFragmentInteractionListener, SaveToArchiveFragment.OnFragmentInteractionListener, ArchivedAlarmFragment.OnFragmentInteractionListener, TimerFragment.OnFragmentInteractionListener, SetTimerFragment.OnFragmentInteractionListener, TimePickerDialog.OnTimeSetListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_SETTINGS = 3;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DrawerLayout mDrawerLayout;
    String[] emailAddress;
    String emailSubject;
    DBTimer dbTimer;
    SharedPreferences preferences;
    boolean analytics, asemataulu;
    SoundControls soundControls;
    FragmentManager fragmentManager;

    Handler mHandler = new Handler();

    private VersionInfo newestVersion = null;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        asemataulu = preferences.getBoolean("asemataulu", false);
        analytics = preferences.getBoolean("analyticsEnabled", false);
        preferences.edit().putBoolean("showHiljenna", false).apply();
        preferences.edit().putBoolean("HalytysOpen", false).apply();
        fragmentManager = this.getSupportFragmentManager();

        setContentView(R.layout.etusivusidepanel);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_dehaze_white_36dp);
        }

        soundControls = new SoundControls();

        mDrawerLayout = findViewById(R.id.drawer_layout);

        // Setting Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(analytics);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(analytics);

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {

            mDrawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.tallenna_arkistoon_haly:
                    loadTallennaArkistoonFragment();
                    //startTallennaArkistoon();
                    return true;
                case R.id.testaa_haly:
                    showDialog("Hälytyksen testaus! Hälytys tulee 5 sekunnin kuluttua.", "Voit laittaa puhelimen näppäinlukkoon tai poistua sovelluksesta. Älä sammuta sovellusta kokonaan taustalta, silloin sammuu myös ajastin joka lähettää hälytyksen.", "Testaa", "testAlarm");
                    return true;
                case R.id.hiljenna_halyt:
                    if (preferences.getInt("aaneton_profiili", -1) == 1) {
                        showDialog("Hälytysten hiljennys!", "Haluatko varmasti hiljentää hälytykset?", "Kyllä", "setSoundSilent");
                    } else {
                        setSoundSilent();
                    }
                    return true;
                case R.id.timer:
                    startTimerActivity();
                    return true;
                case R.id.changelog:
                    startChangelog();
                    return true;
                case R.id.tallennatietokanta:
                    showDialog("Haluatko tallentaa arkistossa olevat hälytykset?", "Tiedosto on avattavissa MS Excel tai jollain muulla ohjelmalla joka tukee .json tiedostoja.", "Kyllä", "saveDatabase");
                    return true;
                case R.id.palautatietokanta:
                    showDialog("Palauta tietokanta.", "Voit palauttaa arkiston .json tiedostosta mikä on tallennettu tästä sovelluksesta.", "Palauta", "returnDatabase");
                    return true;
                case R.id.tyhjennatietokanta:
                    showDialog("Arkiston tyhjentäminen!", "Arkistossa olevat hälytykset poistetaan.\nPoistamisen jälkeen arkistoa ei voida palauttaa.\nOletko varma että haluat poistaa hälytykset?", "Kyllä", "deleteDatabase");
                    return true;
                case R.id.palautetta:
                    startLahetaPalaute();
                    return true;
                case R.id.check_update:
                    //readVersionData();
                    checkNewestVersion();
                    return true;
            }

            return true;
        });

        emailAddress = new String[1];
        emailAddress[0] = "kultalaaki@gmail.com";
        emailSubject = "VPK Apuri palaute";

        if (preferences.contains("termsShown")) {
            loadEtusivuFragment();
        } else {
            loadLegalFragment();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        createChannels();
        new WhatsNewScreen(this).show();
    }

    /**
     * Show info to user if there is newer version available.
     */
    private void checkNewestVersion() {
        Thread thread = new Thread(() -> {
            try {
                PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
                VersionDataProcessor versionDataProcessor = new VersionDataProcessor(packageInfo.versionCode);
                versionDataProcessor.readObjectsToArray();
                versionDataProcessor.setHighestVersions();
                if (preferences.getBoolean("beta_program", false)) {
                    if (versionDataProcessor.isNewBetaVersionAvailable()) {
                        newestVersion = versionDataProcessor.getHighestBeta();
                        mHandler.post(() -> showDialog(
                                "Sinun versio: " + packageInfo.versionName,
                                "Uusi versio: " + newestVersion.getName() + ".",
                                "Lataa: " + newestVersion.getName(),
                                "newVersion"));
                    } else {
                        runOnUiThread(() -> Toast.makeText(
                                FrontpageActivity.this,
                                "Uusin BETA versio asennettu.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    if (versionDataProcessor.isNewStableVersionAvailable()) {
                        newestVersion = versionDataProcessor.getHighestStable();
                        mHandler.post(() -> showDialog(
                                "Sinun versio: " + packageInfo.versionName,
                                "Uusi versio: " + newestVersion.getName() + ".",
                                "Lataa: " + newestVersion.getName(),
                                "newVersion"));
                    } else {
                        runOnUiThread(() -> Toast.makeText(
                                FrontpageActivity.this,
                                "Uusin versio asennettu", Toast.LENGTH_LONG).show());
                    }
                }

            } catch (Exception e) {
                // Inform user if reading version data fails
                // Log error to firebase crashlytics
                MyNotifications notification = new MyNotifications(this);
                notification.showInformationNotification("Versionumeron tarkistaminen epäonnistui. Yritä myöhemmin uudelleen.");
                FirebaseCrashlytics.getInstance().log("Versionumeron tarkistus: " + e);
            }
        });

        thread.start();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onResume() {
        super.onResume();
        if (!asemataulu) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void loadLegalFragment() {
        AffirmationFragment affirmationFragment = new AffirmationFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.etusivuContainer, affirmationFragment, "etusivuLegal").commit();
    }

    public void loadArkistoFragment() {
        ArchiveFragment archiveFragment = new ArchiveFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.etusivuContainer, archiveFragment, "archiveFragment").commit();
    }

    public void loadSettingsFragment() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(FrontpageActivity.this);
        Intent intent = new Intent(FrontpageActivity.this, SettingsActivity.class);
        startActivity(intent, options.toBundle());

    }

    public void loadOhjeetFragment() {
        GuidelineFragment guidelineFragment = new GuidelineFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        if (findViewById(R.id.etusivuContainerLandScape) != null) {
            fragmentTransaction.replace(R.id.etusivuContainerLandScape, guidelineFragment, "guidelineFragment").commit();
        } else {
            fragmentTransaction.replace(R.id.etusivuContainer, guidelineFragment, "guidelineFragment").commit();
        }
    }

    public void loadEtusivuFragment() {
        FrontpageFragment frontpageFragment = new FrontpageFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.etusivuContainer, frontpageFragment, "etusivuNavigation").commit();
    }

    public void loadEtusivuClearingBackstack() {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FrontpageFragment frontpageFragment = new FrontpageFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.etusivuContainer, frontpageFragment, "etusivuNavigation").commit();
    }

    public void loadEtusivuFromFragment() {
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(analytics);
        FrontpageFragment frontpageFragment = new FrontpageFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.etusivuContainer, frontpageFragment, "etusivuNavigation");
        fragmentTransaction.commit();
    }

    public void loadTallennaArkistoonFragment() {
        SaveToArchiveFragment saveToArchiveFragment = new SaveToArchiveFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.etusivuContainer, saveToArchiveFragment, "saveToArchiveFragment").commit();
    }

    public void startChangelog() {
        ChangelogFragment changelogFragment = new ChangelogFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        if (findViewById(R.id.etusivuContainerLandScape) != null) {
            fragmentTransaction.replace(R.id.etusivuContainerLandScape, changelogFragment, "changelogFragment").commit();
        } else {
            fragmentTransaction.replace(R.id.etusivuContainer, changelogFragment, "changelogFragment").commit();
        }
    }

    public void loadHalytysTietokannastaFragment(FireAlarm fireAlarm) {
        ArchivedAlarmFragment archivedAlarmFragment = ArchivedAlarmFragment.newInstance(fireAlarm);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        if (findViewById(R.id.etusivuContainerLandScape) != null) {
            fragmentTransaction.replace(R.id.etusivuContainerLandScape, archivedAlarmFragment, "archivedAlarmFragment").commit();
        } else {
            fragmentTransaction.replace(R.id.etusivuContainer, archivedAlarmFragment, "archivedAlarmFragment").commit();
        }
    }

    public void openSetTimerNewInstance(String primaryKey) {
        SetTimerFragment setTimerFragment = SetTimerFragment.newInstance(primaryKey);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.etusivuContainer, setTimerFragment, "setTimerFragment").commit();
    }

    public void openSetTimer() {
        SetTimerFragment setTimerFragment = new SetTimerFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.etusivuContainer, setTimerFragment, "setTimerFragment").commit();
    }

    public void startTimerActivity() {
        TimerFragment timerFragment = new TimerFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.etusivuContainer, timerFragment, "timerFragment").commit();
    }

    public long saveTimerToDB(String name, String startTime, String stopTime, String ma, String ti, String ke, String to, String pe, String la, String su, String selector, String isiton) {
        dbTimer = new DBTimer(this);
        long tallennettu = dbTimer.insertData(name, startTime, stopTime, ma, ti, ke, to, pe, la, su, selector, isiton);
        if (tallennettu != -1) {
            showToast("Ajastin", "Tallennettu.");
            //Toast.makeText(getApplicationContext(), "Tallennettu", Toast.LENGTH_LONG).show();
            return tallennettu;
        }
        return -1;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        SetTimerFragment setTimerFragment = (SetTimerFragment) getSupportFragmentManager().findFragmentByTag("setTimerFragment");
        if (setTimerFragment != null) {
            setTimerFragment.setTimerTimes(hourOfDay, minute);
        }
    }

    /**
     * Create notification channels
     */
    public void createChannels() {

        // NotificationChannel alarms
        int importanceHigh = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel alarmChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ALARM, Constants.NOTIFICATION_CHANNEL_ALARM, importanceHigh);
        alarmChannel.setDescription("Tämän kanavan ilmoitukset ovat hälytyksiä varten.");
        alarmChannel.enableVibration(false);
        alarmChannel.setSound(null, null);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(alarmChannel);
        }

        // NotificationChannel alarms silenced
        int importanceDefault = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel silentChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_SILENCE, Constants.NOTIFICATION_CHANNEL_SILENCE, importanceDefault);
        silentChannel.setDescription("Tämä ilmoituskanava ilmoittaa kun sovelluksesta on hälytykset hiljennetty");
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        silentChannel.setSound(null, null);
        silentChannel.enableVibration(false);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(silentChannel);
        }

        // NotificationChannel active service
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel serviceChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_SERVICE, Constants.NOTIFICATION_CHANNEL_SERVICE, importance);
        serviceChannel.setDescription("Tämä ilmoituskanava on käytössä kun sovelluksen taustapalvelu on käynnissä osoitteen hakua ja hälytysäänen soittamista varten.");
        serviceChannel.enableVibration(false);
        serviceChannel.setSound(null, null);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(serviceChannel);
        }

        // NotificationChannel error
        NotificationChannel mChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_INFORMATION, Constants.NOTIFICATION_CHANNEL_INFORMATION, NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.setDescription("Tämä ilmoituskanava on käytössä kun sovelluus ilmoittaa jostain virheestä.");
        mChannel.enableVibration(false);
        mChannel.setSound(null, null);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(mChannel);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Send test alarm
     */
    public void testAlarm() {

        preferences.edit().putString("halyvastaanotto11", "0401234567").apply();
        Handler handler1 = new Handler();
        handler1.postDelayed(() -> {
            long aika = System.currentTimeMillis();
            String Aika = (String) DateFormat.format("EEE, dd.MMM yyyy, H:mm:ss", new Date(aika));
            String timeToMessage = (String) DateFormat.format("H:mm:ss_dd.MM.yyyy", new Date(aika));
            Intent halyaaniService = new Intent(getApplicationContext(), SMSBackgroundService.class);
            String alarmMessage = getString(R.string.testihalytysEricaEtuosa) + " " + timeToMessage + getString(R.string.testihalytysEricaTakaosa);
            halyaaniService.putExtra("message", alarmMessage);
            halyaaniService.putExtra("number", "0401234567");
            halyaaniService.putExtra("halytysaani", "false");
            halyaaniService.putExtra("timestamp", Aika);
            getApplicationContext().startService(halyaaniService);
        }, 5000);

    }

    /**
     * Send feedback through mail app
     */
    public void startLahetaPalaute() {
        Intent intentEmail = new Intent(Intent.ACTION_SENDTO);
        intentEmail.setData(Uri.parse("mailto:"));
        intentEmail.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intentEmail.resolveActivity(getPackageManager()) != null) {
            startActivity(intentEmail);
        }
    }

    @SuppressLint("ApplySharedPref")
    public void setSoundSilent() {

        if (preferences.getInt("aaneton_profiili", -1) == 1) {
            soundControls.setSilent(this);
        } else {
            soundControls.setNormal(this);
        }
    }

    /**
     * Dialog factory
     *
     * @param upperText          Upper of dialog
     * @param lowerText          Lower text of dialog
     * @param positiveButtonText Positive button text
     * @param chooser            Negative button text
     */
    @SuppressLint("SetTextI18n")
    private void showDialog(String upperText, String lowerText, String positiveButtonText, String chooser) {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog, null);
        dialog.setView(dialogLayout);

        TextView dialogUpperText = dialogLayout.findViewById(R.id.dialogUpperText);
        TextView dialogLowerText = dialogLayout.findViewById(R.id.dialogLowerText);
        dialogUpperText.setText(upperText);
        dialogLowerText.setText(lowerText);

        Button buttonPositive = dialogLayout.findViewById(R.id.buttonPositive);
        Button buttonNegative = dialogLayout.findViewById(R.id.buttonNegative);
        buttonPositive.setText(positiveButtonText);
        buttonNegative.setText("Peruuta");

        switch (chooser) {
            case "testAlarm":
                buttonPositive.setOnClickListener(v -> {
                    testAlarm();
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
                break;
            case "setSoundSilent":
                buttonPositive.setOnClickListener(v -> {
                    setSoundSilent();
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
                break;
            case "askPermission":
                buttonPositive.setOnClickListener(v -> {
                    ActivityCompat.requestPermissions(FrontpageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_SETTINGS);
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
                break;
            case "saveDatabase":
                buttonPositive.setOnClickListener(v -> {
                    saveDatabaseBackup();
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
                break;
            case "returnDatabase":
                buttonPositive.setOnClickListener(v -> {
                    openFile();
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
                break;
            case "newVersion":
                buttonPositive.setOnClickListener(v -> {
                    startNewVersionDownload();
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
                break;
            case "newest":
                buttonPositive.setOnClickListener(v -> dialog.dismiss());
                break;
            case "deleteDatabase":
                buttonPositive.setOnClickListener(v -> {
                    deleteDatabase();
                    dialog.dismiss();
                });
                buttonNegative.setOnClickListener(v -> dialog.dismiss());
        }


        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(null);
    }

    /**
     * Launch browser intent
     * User can download new version from github releases page
     */
    private void startNewVersionDownload() {
        Intent startBrowser = new Intent(Intent.ACTION_VIEW);
        // If user is beta tester, send to github page where all versions are
        // else send user to vpkapuri.fi
        if(preferences.getBoolean("beta_program", false)) {
            startBrowser.setData(Uri.parse(Constants.ADDRESS_GITHUB_RELEASES_DOWNLOAD));
        } else {
            startBrowser.setData(Uri.parse(Constants.ADDRESS_STABLE_RELEASE_DOWNLOAD));
        }

        startActivity(startBrowser);
    }

    /**
     * Show toast message
     *
     * @param headText  Head of toast
     * @param toastText text content
     */
    public void showToast(String headText, String toastText) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

        TextView head = layout.findViewById(R.id.head_text);
        head.setText(headText);
        TextView toastMessage = layout.findViewById(R.id.toast_text);
        toastMessage.setText(toastText);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    /**
     * Save database backup as json file
     */
    private void saveDatabaseBackup() {
        if (isExternalStorageWritable()) {
            try {
                File file = getAlbumStorageDir("VPK Apuri", "Halytykset_tietokanta.json");
                FileOutputStream fos = new FileOutputStream(file);
                FireAlarmJSONWriter fireAlarmJsonWriter = new FireAlarmJSONWriter();
                FireAlarmRepository fireAlarmRepository = new FireAlarmRepository(getApplication());
                fireAlarmJsonWriter.writeJsonStream(fos, fireAlarmRepository.getAllFireAlarmsToList());
                fos.close();
                Toast.makeText(this, "Tiedosto on tallennettu puhelimen muistiin. Dokumentit -> VPK Apuri", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                crashlytics.log("IOException: " + e);
                MyNotifications notifications = new MyNotifications(this);
                notifications.showInformationNotification("Virhe tietokannan tallennuksessa.");
            }
        }
    }

    /**
     * Checks if external storage is available for read and write
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Get album storage directory with given album name
     * if album name doesn't exist, create it.
     *
     * @param albumName directory name
     * @param fileName  file name
     * @return new File with given name
     */
    public File getAlbumStorageDir(String albumName, String fileName) {
        // Get the directory for the user's public directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.exists()) {
            // Location not found, creating new directory
            file.mkdirs();
        }

        return new File(file, fileName);
    }

    // Request code for selecting a json file.
    private static final int PICK_JSON_FILE = 2;

    /**
     * Starts activity for user to select file from device
     */
    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, PICK_JSON_FILE);
    }

    /**
     * Acrtivity result after picking file
     * Pass json string forward after selection and successful read operation
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param resultData  result data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == PICK_JSON_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri;
            String actualfilepath = "";

            if (resultData != null) {
                uri = resultData.getData();
                String tempID, id;

                assert uri != null;
                if (Objects.equals(uri.getAuthority(), "com.android.externalstorage.documents")) {
                    tempID = DocumentsContract.getDocumentId(uri);
                    String[] split = tempID.split(":");
                    String type = split[0];
                    id = split[1];
                    if (type.equals("primary")) {
                        actualfilepath = Environment.getExternalStorageDirectory() + "/" + id;
                    }
                }

                // Perform operations on the document using its URI.
                // Read content
                try {
                    //File file = new File(actualfilepath);
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(actualfilepath));
                    String line;
                    StringBuilder result = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    bufferedReader.close();

                    // String result contains all lines from backup json
                    // Read json to java
                    readJsonToJava(result.toString());
                } catch (NullPointerException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads alarms from json and handles inserting alarms back to database
     *
     * @param json string read from file
     */
    private void readJsonToJava(String json) {
        try {
            JSONArrayReader read = new JSONArrayReader(json);
            ArrayList<JSONObject> objects = read.getObjects();

            insertBackupToDatabase(objects);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts alarms back to database
     * handles json exception
     *
     * @param alarms list contains json objects of alarms
     */
    private void insertBackupToDatabase(ArrayList<JSONObject> alarms) {
        FireAlarmRepository repository = new FireAlarmRepository(getApplication());

        try {
            for (JSONObject object : alarms) {
                FireAlarm fireAlarm = new FireAlarm(object.getString("tehtäväluokka"), object.getString("kiireellisyystunnus"),
                        object.getString("viesti"), object.getString("osoite"), object.getString("kommentti"),
                        object.getString("vastaus"), object.getString("timestamp"), object.getString("optional2"),
                        object.getString("optional3"), object.getString("optional4"), object.getString("optional5"));

                repository.insert(fireAlarm);
            }
            Toast.makeText(this, "Tietokanta palautettu.", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Deletes all firealarms from database
     */
    public void deleteDatabase() {
        FireAlarmRepository fireAlarmRepository = new FireAlarmRepository(getApplication());
        fireAlarmRepository.deleteAllFireAlarms();
        //Toast.makeText(this, "Arkisto tyhjennetty.", Toast.LENGTH_LONG).show();
        showToast("Arkisto", "Arkisto tyhjennetty!");
    }

    /**
     * Shows "whats new" screen if new install or app updated
     */
    private class WhatsNewScreen {

        private static final String LAST_VERSION_CODE_KEY = "last_version_code";

        private final Activity mActivity;

        // Constructor memorize the calling Activity ("context")
        private WhatsNewScreen(Activity context) {
            mActivity = context;
        }

        // Show the dialog only if not already shown for this version of the application
        @SuppressLint("ApplySharedPref")
        private void show() {
            try {
                // Get the versionCode of the Package, which must be different (incremented) in each release on the market in the AndroidManifest.xml
                final PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);

                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
                final long lastVersionCode = prefs.getLong(LAST_VERSION_CODE_KEY, 0);

                // Kokeillaan versionCode == getLongVersionCode()
                final int versionCode = packageInfo.versionCode;
                if (versionCode != lastVersionCode) {

                    if (prefs.getBoolean("firstrun", true)) {
                        prefs.edit().putInt("aaneton_profiili", Constants.SOUND_PROFILE_NORMAL).commit();
                        prefs.edit().putBoolean("firstrun", false).commit();
                    }
                    // App updated, add alarmdetection to database
                    FireAlarmRepository fireAlarmRepository = new FireAlarmRepository(getApplication());
                    FireAlarm fireAlarm = new FireAlarm("999", "C", "Uusi asennus tai sovellus on päivitetty.", "Ei osoitetta", "", "", "", "", "", "", "");
                    fireAlarmRepository.insert(fireAlarm);

                    // Delete app cache to prevent unnecessary mistakes.
                    deleteCache(getApplicationContext());

                    final String title = mActivity.getString(R.string.app_name) + " v" + packageInfo.versionName;

                    final String message = mActivity.getString(R.string.onlyNewest);

                    // Show the News since last version
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity).setTitle(title).setMessage(message).setCancelable(false).setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        // Mark this version as read
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putLong(LAST_VERSION_CODE_KEY, versionCode);
                        editor.apply();
                        //showTiewtosuojaAfterWhatsnew();
                        dialogInterface.dismiss();
                    });
                    builder.create().show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        void deleteCache(Context context) {
            try {
                File dir = context.getCacheDir();
                deleteDir(dir);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().log("FrontPageActivity.java. Delete cache failed." + e);
            }
        }

        boolean deleteDir(File dir) {
            if (dir != null && dir.isDirectory()) {
                String[] children = dir.list();
                assert children != null;
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
                return dir.delete();
            } else if (dir != null && dir.isFile()) {
                return dir.delete();
            } else {
                return false;
            }
        }
    }
}