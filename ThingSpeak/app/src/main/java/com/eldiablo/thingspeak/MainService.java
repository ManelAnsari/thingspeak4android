package com.eldiablo.thingspeak;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainService extends Service implements LocationListener{
    public MainService() {

    }

    public Map<String, ?> buf;
    public SensorManager mSensorManager;
    public SensorEventThread sensorThread;
    public Location locationCurrent;



    public TelephonyManager telephonyManager;
    public LocationManager locationManager;
    public ConnectivityManager connMgr;
    public WifiManager wifiManager;

    @Override
    public void onCreate(){


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        System.out.println("creating service");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager= (WifiManager) getSystemService(WIFI_SERVICE);
        SharedPreferences prefs=getSharedPreferences("SENSORI", Context.MODE_PRIVATE);
        buf=prefs.getAll();
        SharedPreferences check=getSharedPreferences("interruttore",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=check.edit();
        edit.putBoolean("value",true);
        edit.commit();
      //  sensorThread = new SensorEventThread("SensorThread");

      //  sensorThread.start();


    }
    @Override
    public void onDestroy() {
        try {
            System.out.println("riparto");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sensorThread.interrupt();


        Toast.makeText(this, "Service ucciso", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Toast.makeText(this, sdf.format(cal.getTime()), Toast.LENGTH_SHORT).show();
        System.out.println("service started");
        SharedPreferences check=getSharedPreferences("interruttore",Context.MODE_PRIVATE);
        Boolean interruttore=check.getBoolean("value",true);
        if(interruttore==false ){
            sensorThread.interrupt();
            System.out.println("stopped old thread");
        }
        else {
            System.out.println(" first run");
            SharedPreferences.Editor edit=check.edit();
            edit.putBoolean("value",false);
            edit.commit();
        }
        sensorThread = new SensorEventThread("SensorThread");


        String prefSet=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("connection_list", "1");


        System.out.println("le settings " + prefSet);


        switch(prefSet){
            case "1":
                System.out.println("Selected Always Connection");
                break;
            case "2":
                System.out.println("Selected Only Wifi");
                if(!wifiManager.isWifiEnabled()||!connMgr.getActiveNetworkInfo().isConnected()||connMgr.getActiveNetworkInfo().getType()!=1) {
                    wifiManager.setWifiEnabled(true);
                }
                break;
            case "3":
                System.out.println("Selected Only Data");
                if(!connMgr.getActiveNetworkInfo().isConnected()||connMgr.getActiveNetworkInfo().getType()!=0){
                    SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor=pref.edit();
                                    editor.putString("connection_list","1");
                                    editor.commit();

                }
                break;
        }
        sensorThread.start();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        System.out.println("finito");
        // If we get killed, after returning from here, restart
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");


    }

    @Override
    public void onLocationChanged(Location location) {
        locationCurrent=location;

        System.out.println(String.valueOf("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude()));
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


    class SensorEventThread extends HandlerThread  implements SensorEventListener{

        Handler handler;
        @Override
        public void run(){
           // System.out.println("cisiamo"+buf.size());

            SharedPreferences prefs=getSharedPreferences("SENSORI", Context.MODE_PRIVATE);

            List<String> sensori=new ArrayList<>();
            for (String el: prefs.getAll().keySet()){
                if(el.contains("read")||el.contains("write")){

                }
                else if (el.contains("_type")){
                    System.out.println("el "+el.replace("_type",""));
                    System.out.println(prefs.getString(el.replace("_type",""),""));
                    if(Objects.equals(prefs.getString(el.replace("_type", ""), ""), String.valueOf(true))){
                        sensori.add(prefs.getString(el,""));
                        System.out.println("el selected" + el.replace("_type", ""));

                    }

                }
                else{

                }

            }
                mSensorManager.unregisterListener(sensorThread);
            for (int i = 0; i < sensori.size(); i++) {
                System.out.println("sensori registrati "+sensori.get(i)+" i "+i);

                    mSensorManager.registerListener(sensorThread, mSensorManager.getDefaultSensor(Integer.parseInt(sensori.get(i))), SensorManager.SENSOR_DELAY_FASTEST);


                }
        }
        @Override
        public void onSensorChanged(final SensorEvent event) {
            Log.v("SensorTest", "onSensorChanged");
            String nameSensor = event.sensor.getName().replace(" ", "");

          //  System.out.println("onsensorrrrr-----------------");
            for (Map.Entry<String, ?> entry : buf.entrySet())
            {
              //  System.out.println(entry.getKey() + "/" + entry.getValue());


                if (entry.getKey().equals(nameSensor)&&entry.getValue().equals(String.valueOf(true))) {

                    String params="";
                    for(int j=0;j<event.values.length;j++){
                        params=params+"&field"+(j+1)+"="+event.values[j];
                    }
                    new UpdateThingspeakTask().execute(entry.getKey(), params);
                    try {


                    } catch(Exception e){
                        Log.d("background","layout not visible");
                    }
                    final int type=event.sensor.getType();
                    mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(event.sensor.getType()));
                    String pref=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("your_time",getResources().getString(R.string.pref_default_time));
                    int seconds=Integer.parseInt(pref);
                    final int intervalTime = seconds*1000 ;//10000*6*2; // 10 sec*6*2-2minuti
                    System.out.println("interval "+ intervalTime);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSensorManager.registerListener(sensorThread, mSensorManager.getDefaultSensor(event.sensor.getType()), SensorManager.SENSOR_DELAY_FASTEST);
                           // System.out.println("registrato"+type);
                        }
                    }, intervalTime);


                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
        public SensorEventThread(String name) {
            super(name);
        }



        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            handler = new Handler(sensorThread.getLooper());
        }

        public Handler getHandler() {
            return handler;
        }

        public void quitLooper() {
            if (sensorThread.isAlive()) {
                sensorThread.getLooper().quit();
            }
        }

    }



    class UpdateThingspeakTask extends AsyncTask<String, Void, String> {
        String THINGSPEAK_UPDATE_URL = "https://api.thingspeak.com/update.json?";
        String THINGSPEAK_API_KEY_STRING = "api_key";
        public int TYPE_EVENT;

        private Exception exception;


        protected void onPreExecute() {


        }


        protected String doInBackground(String... strings) {
            System.out.println("update");

            try {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                double lat, lon;
                try{
                    lat=locationCurrent.getLatitude();
                    lon=locationCurrent.getLongitude();}
                catch(Exception e){
                  //  System.out.println("error on coord");
                    lat=0.0;
                    lon=0.0;
                }
                String THINGSPEAK_API_KEY = getSharedPreferences(strings[0], Context.MODE_PRIVATE).getString("write","");

                TYPE_EVENT = Integer.parseInt(String.valueOf(strings[1].charAt(6)));
                URL url = new URL(THINGSPEAK_UPDATE_URL + THINGSPEAK_API_KEY_STRING + "=" +
                        THINGSPEAK_API_KEY + strings[1]+"&location=true&latitude="+lat+"&longitude="+lon);
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

        protected void onPostExecute(String response) {
            // We completely ignore the response
            // Ideally we should confirm that our update was successful
            Log.i("res ", response);
            TYPE_EVENT = 0;

        }
    }


}
