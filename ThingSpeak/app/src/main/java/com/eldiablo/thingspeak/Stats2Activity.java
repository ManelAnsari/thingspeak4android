package com.eldiablo.thingspeak;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
public class Stats2Activity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public static final List<String> sensori=new ArrayList<>();
    public static List<LinearLayout> charts=new ArrayList<>();
    public LineChartView lineGraph;
    private ViewPager mViewPager;
    public LinearLayout buttonGraph;
    public ScrollView v;

    public RelativeLayout rel;
    public  Integer checkFirst=0;

    public LineChartView creaGrafico(JSONObject oggetto ,JSONArray feeds){
        System.out.println("creo grafico");
        List<PointValue> valuesx = new ArrayList<PointValue>();
        List<PointValue> valuesy = new ArrayList<PointValue>();
        List<PointValue> valuesz = new ArrayList<PointValue>();

        for(int i=0;i<feeds.length();i++){
            JSONObject el= null;
            try {
                el = feeds.getJSONObject(i);
            } catch (JSONException e) {
             //   e.printStackTrace();
            }
            String data=null;
            try {
                data=el.getString("created_at");
            } catch (JSONException e) {
              //  e.printStackTrace();
                data="undefined";
            }
            String val1= null;
            try {
                val1 = el.getString("field1");
            } catch (JSONException e) {
             //   e.printStackTrace();
                val1="0.0";
            }
            String val2= null;
            try {
                val2 = el.getString("field2");
            } catch (JSONException e) {
              //  e.printStackTrace();
                val2="0.0";
            }
            String val3= null;
            try {
                val3 = el.getString("field3");
            } catch (JSONException e) {
              //  e.printStackTrace();
                val3="0.0";
            }


               /* valuesx.add(new PointValue(Float.parseFloat(String.valueOf(i)),Float.parseFloat(val1)));
                valuesy.add(new PointValue(Float.parseFloat(String.valueOf(i)),Float.parseFloat(val2)));
                valuesz.add(new PointValue(Float.parseFloat(String.valueOf(i)),Float.parseFloat(val3)));*/
            valuesx.add(new PointValue(Integer.parseInt(String.valueOf(i)),Float.parseFloat(val1)).setLabel(data));
            valuesy.add(new PointValue(Integer.parseInt(String.valueOf(i)),Float.parseFloat(val2)).setLabel(data));
            valuesz.add(new PointValue(Integer.parseInt(String.valueOf(i)),Float.parseFloat(val3)).setLabel(data));

        }

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line linex = new Line(valuesx).setColor(Color.BLUE).setCubic(false);
        Line liney = new Line(valuesy).setColor(Color.RED).setCubic(false);
        Line linez = new Line(valuesz).setColor(Color.GREEN).setCubic(false);
        linex.setHasLabels(true);
        linex.setHasLabelsOnlyForSelected(true);
        liney.setHasLabels(true);
        liney.setHasLabelsOnlyForSelected(true);
        linez.setHasLabels(true);
        linez.setHasLabelsOnlyForSelected(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(linex);
        lines.add(liney);
        lines.add(linez);

        System.out.println("valuesx " + valuesx);
        System.out.println("valuesy " + valuesy);
        System.out.println("valuesz " + valuesz);

        LineChartData data = new LineChartData();
        data.setLines(lines);


        LineChartView chart = new LineChartView(getApplicationContext());


        Axis axisX = new Axis().setHasLines(true);
        Axis axisY = new Axis().setHasLines(true);

        axisY.setMaxLabelChars(5);
        JSONObject a=new JSONObject();
        String chan="";
        try {
            a=oggetto.getJSONObject("channel");
            chan=a.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        axisY.setName(chan);

        axisY.setTextSize(12);
        axisX.setValues(null);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);


        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
        chart.setValueSelectionEnabled(true);

    //    charts.add(chart);
        return chart;

    }
    class GetThingspeakTask extends AsyncTask<String, Void, String> {
        //   https://api.thingspeak.com/channels/149617/feeds.json?api_key=YDHLYNMWD4T2Q0OG
        String THINGSPEAK_UPDATE_URL = "https://api.thingspeak.com/channels/";
        String THINGSPEAK_API_KEY_STRING = "api_key";
        String THINGSPEAK_START_TIME="";
        String THINGSPEAK_CHANNEL="";
        public int TYPE_EVENT;


        protected void onPreExecute() {


        }


        protected String doInBackground(String... strings) {
            System.out.println("get");

            try {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences stat=getSharedPreferences("STATS"+strings[0],Context.MODE_PRIVATE);

                String THINGSPEAK_API_KEY = getSharedPreferences(stat.getString("name",""),Context.MODE_PRIVATE).getString("read", "");
                THINGSPEAK_START_TIME=stat.getString("date","");
                THINGSPEAK_CHANNEL=stat.getString("id","");
                URL url = new URL(THINGSPEAK_UPDATE_URL + THINGSPEAK_CHANNEL+"/feed.json?"+THINGSPEAK_API_KEY_STRING + "=" +
                        THINGSPEAK_API_KEY +"&start="+THINGSPEAK_START_TIME);
                Log.i("url ", String.valueOf(url));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();

                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            // We completely ignore the response
            // Ideally we should confirm that our update was successful
            checkFirst++;
            Log.i("res ", response);
            JSONObject res=null;
            JSONArray array=null;

            try {
                res=new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                array=res.getJSONArray("feeds");
                if(array.length()==0){
                    System.out.println("feed vuoti");
                }

                    System.out.println("feeddddd "+array);
                   lineGraph=creaGrafico(res,array);




            } catch (JSONException e) {
                e.printStackTrace();
            }
            TYPE_EVENT = 0;

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SharedPreferences prefs=getSharedPreferences("SENSORI", Context.MODE_PRIVATE);
        System.out.println(prefs.getAll());
        int i=1;
        if(sensori.size()==0){
        for (String el: prefs.getAll().keySet()){
            if(el.contains("read")||el.contains("write")){

            } else if (el.contains("_type")){

            }
            else{
                sensori.add(el);
               // new GetThingspeakTask().execute(String.valueOf(i));
                i++;
            }

        }}


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

       /* if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_stats2, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Plot number "+getArguments().getInt(ARG_SECTION_NUMBER)+"not returned yet!\n Wait and scroll...");


            SharedPreferences pref=this.getActivity().getSharedPreferences("STATS"+getArguments().getInt(ARG_SECTION_NUMBER),Context.MODE_PRIVATE);
            System.out.println("STATS" + getArguments().getInt(ARG_SECTION_NUMBER));
            System.out.println(pref.getAll());
          //  View graph=rootView;

          //  if(charts.size()==sensori.size()){
            View graph;
                System.out.println("forse stampiam");
                if(charts.size()>=(getArguments().getInt(ARG_SECTION_NUMBER)))
                       graph=charts.get(getArguments().getInt(ARG_SECTION_NUMBER)-1);
                else  graph=rootView;
               // graph=charts.get(0);

               // System.out.println(getArguments().getInt(ARG_SECTION_NUMBER) + " eccolo  " + ll.getChildCount());




            return graph;
           // return rootView;

        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            position=position%sensori.size();
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.

            return sensori.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return sensori.get(position);
        }
    }
}
