package com.eldiablo.thingspeak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.eldiablo.thingspeak.Sensor.SensorContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public void getSensors(List<SensorContent.SensorItem> list){
        List<Sensor> list_sen;
        SensorManager sMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        list_sen = sMgr.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : list_sen) {


            System.out.println(sensor);
        }
        String sens=list_sen.toString().replace(" ","");
        try {
            JSONArray sen_parsed;
            JSONArray jarray = new JSONArray(sens.replace("=",":"));
            if(list.size()>0){
                list.clear();
            }

            for (int i=0;i<jarray.length();i++){
                SensorContent.addItem(SensorContent.createSensorItem(i + 1, (JSONObject) jarray.get(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getConnection();
        getSensors(SensorContent.ITEMS);
      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }
    public void getConnection(){
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifinfo = wifiManager.getConnectionInfo();
        ScrollView ll = (ScrollView) findViewById(R.id.scrollView3);

        TextView general=new TextView(this.getApplicationContext());

        general.setTextColor(Color.BLACK);
        ConnectivityManager  connMgr  =  (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo  =  connMgr.getActiveNetworkInfo();
        if  (networkInfo  !=  null  )
        {
//  fetch  data
            TextView gsm = new TextView(this.getApplicationContext());


            general.setText("\n\nWifiManager \n\n"+wifinfo.toString().replace(",", "\n")+"\n\nYour connection now: \nType " + networkInfo.getTypeName()+"\nState "+networkInfo.getState()+"\nInfo "+networkInfo.getExtraInfo());

            ll.addView(general);
        }  else  {
//  display  error
            general.setText("WifiManager \n"+wifinfo.toString().replace(",","\n"));
            ll.addView(general);
        }

}

    @Override
    public void onDestroy(){
        System.out.println("ciao");
        super.onDestroy();
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sensors) {
            Intent myIntent = new Intent(this, SensorListActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_thingspeak) {
            Intent myIntent = new Intent(this, SpeakActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_stats) {
            Intent myIntent = new Intent(this, StatsActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
