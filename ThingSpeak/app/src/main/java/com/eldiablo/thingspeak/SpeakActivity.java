package com.eldiablo.thingspeak;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.app.AlarmManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.eldiablo.thingspeak.Sensor.SensorContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Provider;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import static com.eldiablo.thingspeak.Sensor.SensorContent.ITEMS;
import static java.lang.Thread.sleep;
public class SpeakActivity extends SensorListActivity implements LocationListener{

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public List<Sensor> list_sen = new ArrayList<>();
    public CheckBox cb[];
    public int getSen;
    public List<SensorContent.SensorItem> type = new ArrayList<>();
    public List<SensorContent.SensorItem> buf = new ArrayList<>();
    public  int counter=0;
    public  int returned=0;
    public SensorManager mSensorManager;

    public TelephonyManager telephonyManager;


    public ConnectivityManager connMgr;
    public WifiManager wifiManager;
    int TYPE_EVENT = 0;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;

    Double lat,lng;
    String provider;
    Location locationCurrent;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    public ListView list;

    public ArrayAdapter<String> adapter;
    public ArrayList<String> arrayList;
    @Override
    public void onLocationChanged(Location location) {
        locationCurrent=location;

     //   System.out.println(String.valueOf("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
        Log.d("provider", provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
        Log.d("provider", provider);
    }





    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */




    class CreateChannel extends AsyncTask<List<String>, Void, String> {
        String THINGSPEAK_CREATE_URL = "https://api.thingspeak.com/channels.json?";
        String THINGSPEAK_API_KEY_STRING = "api_key";

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String THINGSPEAK_API_KEY = prefs.getString("default_channel", getResources().getString(R.string.pref_default_channel));
        String nameChannel="";
        private Exception exception;

        protected void onPreExecute() {
        }

        protected String doInBackground(List<String>... params) {
            nameChannel = params[0].get(0);
            counter++;
            String fields = "";
            double lat, lon;

            try{
                lat=locationCurrent.getLatitude();
                lon=locationCurrent.getLongitude();}
            catch(Exception e){
              //  System.out.println("error on coord");
                lat=0.0;
                lon=0.0;
            }
            for (int i = 1; i < params[0].size(); i++) {
                fields = fields + "&field"+i;
                fields = fields + "=" + params[0].get(i);
            }
            try {
                URL url = new URL(THINGSPEAK_CREATE_URL + THINGSPEAK_API_KEY_STRING + "=" +
                        THINGSPEAK_API_KEY + "&name=" + nameChannel + fields+"&location=true&latitude="+lat+"&longitude="+lon);
                Log.i("url ", String.valueOf(url));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestMethod("POST");
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

        protected void onPostExecute(String response) {
            // We completely ignore the response
            // Ideally we should confirm that our update was successful
           // System.out.println(response);
            JSONObject reader = null;
            String key = null;
            String id=null;


            try {
                reader = new JSONObject(response);
                try {
                    key = reader.getString("api_keys");
                    id = reader.getString("id");

                    for( SensorContent.SensorItem el :SensorContent.ITEMS ){
                        if(el.Sensorname.equals(nameChannel.replace(" ",""))){
                            SharedPreferences keys = getSharedPreferences(el.Sensorname, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = keys.edit();
                            JSONArray reader2 = new JSONArray(key);
                            JSONObject write=reader2.getJSONObject(0);
                            JSONObject read=reader2.getJSONObject(1);
                           // System.out.println("write " + write.getString("api_key"));
                           // System.out.println("read " + read.getString("api_key"));
                            editor.putString("write", write.getString("api_key"));
                            editor.putString("read", read.getString("api_key"));
                            editor.putString("id", id);
                            editor.commit();
                           // System.out.println(key);

                            arrayList.add(el.Sensorname+" returned!");
                            returned=returned+1;
                            System.out.println("ret "+returned+" count "+counter);
                            if(returned==counter){
                                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                               fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorMenuDark)));
                                fab.setClickable(true);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("res ", response);
            Log.i("key", key);

        }
    }


    public void createChannel(View v) {
        list = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<String>();
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
               arrayList);
        list.setAdapter(adapter);

            //  System.out.println(name.getText());
            //   params.add(String.valueOf(name.getText()));


            for (SensorContent.SensorItem s : buf) {
                SharedPreferences prefs = getSharedPreferences("SENSORI", Context.MODE_PRIVATE);
                String textData = prefs.getString(s.Sensorname, "");
               // System.out.println(textData);
                if (textData==String.valueOf(true)){
                  //  System.out.println(s.Sensorname+"weeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                }
                else{
                   // System.out.println(s.Sensorname+"not selecteeeeeeeeeeeeed");
                }


                List<String> params = new ArrayList<>();
                params.add(s.Sensorname);
                if(s.type==5)//lightsensor
                    params.add("Single-Axis");
                else{
                    params.add("X-Axis");
                    params.add("Y-Axis");
                    params.add("Z-Axis");
                }

                new CreateChannel().execute(params);
            }



    }

    public void updateChannel() {


       /* for (int i = 0; i < buf.size(); i++) {
            SharedPreferences prefs=getSharedPreferences("SENSORI",Context.MODE_PRIVATE);
            String res=prefs.getString(buf.get(i).Sensorname,"");
            if(Objects.equals(res, String.valueOf(true)))
            {            System.out.println("shared "+res);

                mSensorManager.registerListener(sensorThread, mSensorManager.getDefaultSensor(buf.get(i).type), SensorManager.SENSOR_DELAY_FASTEST);

            }
        }*/

        final SharedPreferences prefSet=PreferenceManager.getDefaultSharedPreferences(SpeakActivity.this);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager= (WifiManager) getSystemService(WIFI_SERVICE);
        System.out.println("prefset" + prefSet.getString("connection_list", "1"));
        switch(prefSet.getString("connection_list","1")){
            case "1":
                System.out.println("Selected Always Connection");
                startService(new Intent(SpeakActivity.this, MainService.class));

                break;
            case "2":
                System.out.println("Selected Only Wifi");
                if(!wifiManager.isWifiEnabled()){
                    //||!connMgr.getActiveNetworkInfo().isConnected()||connMgr.getActiveNetworkInfo().getType()!=1
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SpeakActivity.this);
                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(SpeakActivity.this);
                    builder1.setMessage("Before this you must enable Wifi!");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    wifiManager.setWifiEnabled(true);
                                    builder2.setMessage("Wifi enabled!");
                                    AlertDialog alert2=builder2.create();
                                    alert2.show();
                                    startService(new Intent(SpeakActivity.this, MainService.class));

                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor=prefSet.edit();
                                    editor.putString("connection_list", "1");
                                    editor.commit();

                                    dialog.cancel();
                                    builder2.setMessage("Settings changed!");
                                    AlertDialog alert2=builder2.create();
                                    alert2.show();
                                    startService(new Intent(SpeakActivity.this, MainService.class));

                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }

                break;
            case "3":
                System.out.println("Selected Only Data");
                if(!connMgr.getActiveNetworkInfo().isConnected()||connMgr.getActiveNetworkInfo().getType()!=0){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SpeakActivity.this);
                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(SpeakActivity.this);
                    builder1.setMessage("Before this you must enable Data!");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    wifiManager.setWifiEnabled(false);

                                    builder2.setMessage("Wifi disabled, check your data!");

                                    AlertDialog alert2=builder2.create();
                                    alert2.show();
                                    startService(new Intent(SpeakActivity.this, MainService.class));

                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences.Editor editor=prefSet.edit();
                                    editor.putString("connection_list","1");
                                    editor.commit();

                                    startService(new Intent(SpeakActivity.this, MainService.class));

                                    dialog.cancel();
                                    builder2.setMessage("Settings changed!");
                                    AlertDialog alert2=builder2.create();
                                    alert2.show();
                                    startService(new Intent(SpeakActivity.this, MainService.class));

                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                break;
        }


    }

    public void getSensors(View v) {


        SimpleItemRecyclerViewAdapter a = new SimpleItemRecyclerViewAdapter(ITEMS);

        type = a.mValues;

        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        if (getSen == 0) {
            cb = new CheckBox[type.size()];
            for (int i = 0; i < type.size(); i++) {
                cb[i] = new CheckBox(this);
                ll.addView(cb[i]);
                // cb[i].setBackgroundColor(Color.GREEN);
                cb[i].setTextColor(Color.BLACK);
                cb[i].setText("Sensor " + i + " : " + type.get(i));
                cb[i].setId(i);


            }
        }
        getSen = 1;


    }

    public void checkSensors(final View view) {
      //buf.clear();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SpeakActivity.this);
        if (getSen != 1) {
            builder1.setMessage("Before this you must get sensors!");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Yes---Get&Check",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getSensors(view);
                            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);

                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            SharedPreferences prefs = getSharedPreferences("SENSORI", Context.MODE_PRIVATE);


            SharedPreferences settings = getSharedPreferences("FIRST", 0);

            if (settings.getBoolean("my_first_time", true)) {
                //the app is being launched for first time, do something
                Log.d("Comments", "First time");

                // first time task
                for (int i = 0; i < type.size(); i++) {

                    if (cb[i].isChecked()) {
                        buf.add(type.get(i));
                        type.get(i).selected = true;
                        SharedPreferences.Editor editor = prefs.edit();



                        editor.putString(type.get(i).Sensorname, String.valueOf(true));
                        editor.putString(String.valueOf(type.get(i).Sensorname+"_type"), String.valueOf(type.get(i).type));
                        editor.commit();


                    } else {
                        buf.add(type.get(i));
                        type.get(i).selected =false;
                        SharedPreferences.Editor editor = prefs.edit();



                        editor.putString(type.get(i).Sensorname, String.valueOf(false));
                        editor.putString(String.valueOf(type.get(i).Sensorname+"_type"), String.valueOf(type.get(i).type));

                        editor.commit();
                    }
                }
                // record the fact that the app has been started at least once
                settings.edit().putBoolean("my_first_time", false).commit();
            }
            else{
            for (int i = 0; i < type.size(); i++) {

                if (cb[i].isChecked()) {
                    buf.add(type.get(i));
                    type.get(i).selected = true;
                    SharedPreferences.Editor editor = prefs.edit();


                    editor.putString(type.get(i).Sensorname, String.valueOf(true));
                    editor.putString(String.valueOf(type.get(i).Sensorname + "_type"), String.valueOf(type.get(i).type));
                    editor.commit();

                    //buf[i]=String.valueOf(cb[i].getText());
                    //  System.out.println(buf + " enabled");
                    //  System.out.println(getSharedPreferences("SENSORI",Context.MODE_PRIVATE).getAll());

                } else {
                    // buf[i]=null;
                    // System.out.println(buf[i]+ " not enabled");
                    SharedPreferences.Editor editor = prefs.edit();


                    editor.putString(type.get(i).Sensorname, String.valueOf(false));
                    editor.commit();
                }

            }
        }
            if (buf.size() == 0) {

                builder1.setMessage("no sensors checked");
            } else {
                builder1.setMessage("Good!");

            }
            builder1.setCancelable(true);
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak);
        getSen = 0;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        list= (ListView) findViewById(R.id.listView);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        locationCurrent = locationManager.getLastKnownLocation(Context.LOCATION_SERVICE);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.beginFakeDrag();

       // sensorThread = new SensorEventThread("SensorThread");
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                switch (mViewPager.getCurrentItem()) {
                    case 0:
                        System.out.println("mail1section");
                        checkSensors(view);
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        break;
                    case 1:
                        System.out.println("mail2section");
                        if(returned<counter){
                            Toast.makeText(getApplicationContext(), "Wait for returning all keys.", Toast.LENGTH_LONG).show();


                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Starting monitoring...", Toast.LENGTH_LONG).show();
                            updateChannel();
                          //  mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                        }


                        break;

                    case 2:
                        System.out.println("mail3section");


                        break;
                }
            }
        });


    }
    public boolean allReturned(SensorEvent[] vec ){
        for (int i=0;i<vec.length;i++){
            if(vec[i]==null)
                return false;
        }
        return true;
    }



    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speak, menu);
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

        public int getTab() {
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    return R.layout.sezione1;
                case 2:
                    return R.layout.sezione2;
                case 3:
                    return R.layout.sezione3;
            }
            return -1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            int Tab = getTab();
            rootView = inflater.inflate(Tab, container, false);
            switch (Tab) {
                case 1:

                case 2:
                    FloatingActionButton fab= (FloatingActionButton) getActivity().findViewById(R.id.fab);
                    fab.setClickable(false);
                case 3:

            }
            // TextView textView = (TextView) rootView.findViewById(Tab);
            // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            // int arg=getArguments().getInt(ARG_SECTION_NUMBER);

            return rootView;
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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SELECT";
                case 1:
                    return "CREATE";
                case 2:
                    return "VIEW";
            }
            return null;
        }
    }


}