package kultalaaki.vpkapuri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class HalytysFragment extends Fragment {

    static DBHelper db;
    EditText halytyksentunnus, halytyksenviesti;
    TextToSpeech t1;
    int palautaMediaVol, tekstiPuheeksiVol;
    boolean palautaMediaVolBoolean = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getActivity();
        if(ctx != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

    }

    /*public static HalytysFragment newInstance(String newAlarmComing) {
        HalytysFragment halytys = new HalytysFragment();
        Bundle args = new Bundle();
        args.putString("newAlarm", newAlarmComing);
        halytys.setArguments(args);
        return halytys;
    }*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.halytys_fragment, parent, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getArguments() != null) {
            Toast.makeText(getActivity(), "Load basic.", Toast.LENGTH_LONG).show();
        } else {
            getNewestDatabaseEntry();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        halytyksentunnus = view.findViewById(R.id.halytyksenTunnus);
        halytyksenviesti = view.findViewById(R.id.halytyksenViesti);
    }

    public void getNewestDatabaseEntry(){
        try {
            db = new DBHelper(getActivity());
            Cursor c = db.haeViimeisinLisays();
            if(c != null) {
                halytyksentunnus.setText(c.getString(c.getColumnIndex(DBHelper.TUNNUS)));
                halytyksenviesti.setText(c.getString(c.getColumnIndex(DBHelper.VIESTI)));
            }
        } catch (Exception e) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Huomautus!")
                    .setMessage("Arkisto on tyhjä. Ei näytettävää hälytystä.")
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
        }
    }

    public void txtToSpeechVolume() {
        Context ctx = getActivity();
        if(ctx != null) {
            AudioManager ad = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            if(ad != null) {
                palautaMediaVol = ad.getStreamVolume(AudioManager.STREAM_MUSIC);
                palautaMediaVolBoolean = true;
                ad.setStreamVolume(AudioManager.STREAM_MUSIC, 4, 0);
                // teksti puheeksi äänenvoimakkuus
                try {
                    SharedPreferences prefe_general = PreferenceManager.getDefaultSharedPreferences(ctx);
                    tekstiPuheeksiVol = prefe_general.getInt("tekstiPuheeksiVol", -1);
                    tekstiPuheeksiVol = saadaAani(tekstiPuheeksiVol);
                    ad.setStreamVolume(AudioManager.STREAM_MUSIC, tekstiPuheeksiVol, 0);
                    puhu();
                } catch (Exception e) {
                    Log.i("VPK Apuri", "Teksti puheeksi äänenvoimakkuuden lukeminen asetuksista epäonnistui.");
                }
            }
        }
    }

    public int saadaAani(int voima) {
        Context ctx = getActivity();
        if(ctx != null) {
            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            if(audioManager != null) {
                tekstiPuheeksiVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                double aani = (double)tekstiPuheeksiVol/100*voima;
                tekstiPuheeksiVol = (int) aani;
            }

            if(tekstiPuheeksiVol == 0) { return 1;
            } else if(tekstiPuheeksiVol == 1) {
                return 1;
            } else if(tekstiPuheeksiVol == 2) {
                return 2;
            } else if(tekstiPuheeksiVol == 3) {
                return 3;
            } else if(tekstiPuheeksiVol == 4) {
                return 4;
            } else if(tekstiPuheeksiVol == 5) {
                return 5;
            } else if(tekstiPuheeksiVol == 6) {
                return 6;
            } else if(tekstiPuheeksiVol == 7) {
                return 7;
            }

            return tekstiPuheeksiVol;
        }
        return 0;
    }

    public void txtToSpeech(){
        t1 = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = t1.setLanguage(Locale.getDefault());
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getActivity(), "Kieli ei ole tuettu.", Toast.LENGTH_LONG).show();
                    }
                    txtToSpeechVolume();
                } else {
                    Toast.makeText(getActivity(), "Virhe", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void lopetaPuhe() {
        Context ctx = getActivity();
        if(ctx != null) {
            AudioManager ad = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            if(ad != null) {
                ad.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                if(t1 != null) {
                    t1.stop();
                    t1.shutdown();
                }
            }
        }
    }

    public void puhu() {
        String puheeksi = halytyksentunnus.getText().toString() + " " + halytyksenviesti.getText().toString();
        if(Build.VERSION.SDK_INT >= 21) {
            t1.playSilentUtterance(1000, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            t1.playSilence(1000, TextToSpeech.QUEUE_FLUSH, null);
        }
        t1.speak(puheeksi, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        if(palautaMediaVolBoolean) {
            Context ctx = getActivity();
            if(ctx != null) {
                AudioManager ad = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                if(ad != null) {
                    ad.setStreamVolume(AudioManager.STREAM_MUSIC, palautaMediaVol, 0);
                }
            }
        }
    }
}