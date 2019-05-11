package kultalaaki.vpkapuri;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;


public class TimerFragment extends Fragment {

    Button addTimer, deleteTimers;
    ListView listViewTimers;
    DBTimer dbTimer;
    Context ctx;

    private OnFragmentInteractionListener mListener;

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ctx = getActivity();
        dbTimer = new DBTimer(ctx);
        addTimer = view.findViewById(R.id.addTimer);
        deleteTimers = view.findViewById(R.id.deleteTimers);
        listViewTimers = view.findViewById(R.id.listViewTimers);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        populateListView();
        addTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openSetTimer();
            }
        });
        deleteTimers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTimersFromDatabase();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void populateListView() {
        if(ctx != null) {
            Cursor cursor = dbTimer.getAllRows();
            String[] fromFieldNames = new String[] {DBTimer.COL_1, DBTimer.NAME, DBTimer.STARTTIME, DBTimer.STOPTIME, DBTimer.MA, DBTimer.TI, DBTimer.KE, DBTimer.TO, DBTimer.PE, DBTimer.LA, DBTimer.SU,
                    DBTimer.SELECTOR};
            final int[] toViewIDs = new int[] {R.id.sijaID, R.id.timerName, R.id.startTime, R.id.stopTime, R.id.monday, R.id.tuesday, R.id.wednesday, R.id.thursday, R.id.friday, R.id.saturday, R.id.sunday,
                    R.id.selectedState};
            SimpleCursorAdapter myCursorAdapter;
            myCursorAdapter = new SimpleCursorAdapter(ctx, R.layout.item_timer_layout, cursor, fromFieldNames, toViewIDs, 0);
            listViewTimers.setAdapter(myCursorAdapter);
            listViewTimers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("TAG", "tulee");
                    TextView textView = view.findViewById(R.id.sijaID);
                    String primaryKey = textView.getText().toString();
                    mListener.openSetTimerNewInstance(primaryKey);
                }
            });
        }
    }

    void deleteTimersFromDatabase() {
        dbTimer.tyhjennaTietokanta();
        populateListView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void openSetTimerNewInstance(String primaryKey);
        void openSetTimer();
    }
}
