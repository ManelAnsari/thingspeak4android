package com.eldiablo.thingspeak;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eldiablo.thingspeak.Sensor.SensorContent;

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a {@link SensorListActivity}
 * in two-pane mode (on tablets) or a {@link SensorDetailActivity}
 * on handsets.
 */
public class SensorDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The Sensor content this fragment is presenting.
     */
    private SensorContent.SensorItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the Sensor content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            System.out.printf("map %s%n", SensorContent.ITEM_MAP);
            System.out.println(getArguments().containsKey(ARG_ITEM_ID));
            System.out.println(getArguments());
            mItem = SensorContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            System.out.println(mItem);
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                StringBuilder builder = new StringBuilder();

                builder.append(mItem.Sensorname);
                appBarLayout.setTitle(builder.toString());
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sensor_detail, container, false);

        // Show the Sensor content as text in a TextView.
        if (mItem != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("\nType: ").append(mItem.type);
            builder.append("\nVendor: ").append(mItem.vendor);
            builder.append("\nVersion: ").append(mItem.version);
            builder.append("\nMax. range: ").append(mItem.maxRange);
            builder.append("\nMin. delay: ").append(mItem.minDelay);
            builder.append("\nPower: ").append(mItem.power);
            builder.append("\nResolution: ").append(mItem.resolution);



            ((TextView) rootView.findViewById(R.id.sensor_detail)).setText(builder.toString());
        }

        return rootView;
    }
}
