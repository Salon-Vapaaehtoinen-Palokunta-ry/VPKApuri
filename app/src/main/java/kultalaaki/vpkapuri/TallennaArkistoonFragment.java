package kultalaaki.vpkapuri;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class TallennaArkistoonFragment extends Fragment {

    TextView tunnus, luokka, viesti;
    EditText tunnusteksti, luokkateksti, viestiteksti;
    CardView tallenna;
    static DBHelper db;

    private OnFragmentInteractionListener mListener;

    public TallennaArkistoonFragment() {
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
        return inflater.inflate(R.layout.fragment_tallenna_arkistoon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        tunnus = view.findViewById(R.id.tunnus);
        luokka = view.findViewById(R.id.luokka);
        viesti = view.findViewById(R.id.viesti);
        tunnusteksti = view.findViewById(R.id.tunnusteksti);
        luokkateksti = view.findViewById(R.id.luokkateksti);
        viestiteksti = view.findViewById(R.id.viestiteksti);
        tallenna = view.findViewById(R.id.cardTallenna);
    }

    public void onStart() {
        super.onStart();
        tallennaHalytys();
    }

    public void tallennaHalytys() {
        tallenna.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db = new DBHelper(getActivity());
                        String tunnus = tunnusteksti.getText().toString();
                        String luokka = luokkateksti.getText().toString();
                        String viesti = viestiteksti.getText().toString();

                        boolean tallennettu = db.insertData( tunnus, luokka, viesti,"");
                        if(tallennettu) {
                            Toast.makeText(getActivity(), "Tallennettu.", Toast.LENGTH_LONG).show();
                            mListener.loadEtusivuClearingBackstack();
                            if(getActivity() != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(EtusivuActivity.INPUT_METHOD_SERVICE);
                                if(imm != null) {
                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                }
                            }
                        }
                    }
                }
        );
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void loadEtusivuClearingBackstack();
    }
}
