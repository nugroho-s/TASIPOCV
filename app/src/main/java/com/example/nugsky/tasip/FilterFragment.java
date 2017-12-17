package com.example.nugsky.tasip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.nugsky.imageproc.ImageProc;
import com.example.nugsky.imageproc.filter.*;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FilterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    HashMap<String, KernelFilter> mapKernelFilter = new HashMap<String, KernelFilter>(){{
        put("Emboss Filter", new EmbossKernel());
        put("Mean Filter", new MeanKernel());
        put("Sharpen Filter", new SharpenKernel());
        put("Sobel Filter", new SobelKernel());
        put("Laplace Filter", new LaplaceKernel());
        put("Prewitt Filter", new PrewittKernel());
        put("Robinson Filter", new RobinsonKernel());
    }};

    ImageView grayView;
    ImageView filterView;
    Button prosesButton;
    LinearLayout linlaHeaderProgress;
    Spinner listFilter;

    private OnFragmentInteractionListener mListener;
    private String TAG = "FilterFragment";

    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FilterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilterFragment newInstance(String param1, String param2) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);

        View view = rootView;

        linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        prosesButton = (Button) view.findViewById(R.id.prosesButton);
        grayView = (ImageView) view.findViewById(R.id.grayView);
        filterView = (ImageView) view.findViewById(R.id.filterView);
        listFilter = (Spinner) view.findViewById(R.id.listFilter);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.g1small);
        long start = System.nanoTime();
        final Bitmap gray = ImageProc.convertToGrayv2(bm);
        grayView.setImageBitmap(gray);
        linlaHeaderProgress.setVisibility(View.GONE);
        grayView.setVisibility(View.VISIBLE);
        prosesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),listFilter.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
                FilterAsync filterAsync = new FilterAsync(gray);
                String filterName = listFilter.getSelectedItem().toString();
                filterAsync.execute(filterName);
            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        void onFragmentInteraction(Uri uri);
    }

    private class FilterAsync extends AsyncTask<String,Integer,Integer> {
        Bitmap gray;
        Bitmap filtered;

        public FilterAsync(Bitmap gray) {
            this.gray = gray;
        }

        @Override
        protected void onPreExecute() {
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... filters) {
            String filter = filters[0];
            long start = System.nanoTime();
            KernelFilter kernelFilter = mapKernelFilter.get(filter);
            if(kernelFilter != null){
                filtered = ImageProc.convulutionThreaded(gray,kernelFilter);
            } else {
                if (filter.equals("Median Filter")){
                    filtered = ImageProc.medianFilterv2(gray,3,3);
                }
            }
            Log.d(TAG,String.format("process time = %f seconds",((System.nanoTime()-start))/1000000000.0));
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            filterView.setImageBitmap(filtered);
            filterView.setVisibility(View.VISIBLE);

            // HIDE THE SPINNER AFTER LOADING FEEDS
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }
}
