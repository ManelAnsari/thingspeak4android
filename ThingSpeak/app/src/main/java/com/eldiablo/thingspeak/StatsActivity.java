package com.eldiablo.thingspeak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.LineChartView;

public class StatsActivity extends AppCompatActivity {

    public ScrollView v;
    public  JSONArray vector;
    public  LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    public RelativeLayout rel;
    public final List<String> sensori=new ArrayList<>();
    public Integer counter;
    public class ValueTouchListener implements LineChartOnValueSelectListener {



        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {


          String string="";
            switch(lineIndex){
                case 0:
                    string="X";
                    break;
                case 1:
                    string="Y";
                    break;
                case 2:
                    string="Z";
                    break;

            }
            Toast.makeText(getApplicationContext(), "Axis: " + string +"\nDate: " + Arrays.toString(value.getLabelAsChars()).replaceAll("\\[|\\]|,|\\s", "") + "\nValues: "+value, Toast.LENGTH_SHORT).show();
            System.out.println("selected point "+lineIndex+" "+pointIndex);
        }
    }

    public void creaGrafico(JSONObject oggetto ,JSONArray feeds){
        if(Stats2Activity.charts.size()>counter){
            System.out.println("charts eccedenti");
            Stats2Activity.charts.clear();
        }
        System.out.println("creo grafico");
        List<PointValue> valuesx = new ArrayList<PointValue>();
        List<PointValue> valuesy = new ArrayList<PointValue>();
        List<PointValue> valuesz = new ArrayList<PointValue>();
        List<String>     valuesd = new ArrayList<String>();
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
             //   e.printStackTrace();
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
            //    e.printStackTrace();
                val2="0.0";
            }
            String val3= null;
            try {
                val3 = el.getString("field3");
            } catch (JSONException e) {
            //    e.printStackTrace();
                val3="0.0";
            }


               /* valuesx.add(new PointValue(Float.parseFloat(String.valueOf(i)),Float.parseFloat(val1)));
                valuesy.add(new PointValue(Float.parseFloat(String.valueOf(i)),Float.parseFloat(val2)));
                valuesz.add(new PointValue(Float.parseFloat(String.valueOf(i)),Float.parseFloat(val3)));*/
                valuesx.add(new PointValue(Integer.parseInt(String.valueOf(i)),Float.parseFloat(val1)).setLabel(data));
                valuesy.add(new PointValue(Integer.parseInt(String.valueOf(i)),Float.parseFloat(val2)).setLabel(data));
                valuesz.add(new PointValue(Integer.parseInt(String.valueOf(i)),Float.parseFloat(val3)).setLabel(data));
                valuesd.add(new String(data));

        }

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line linex = new Line(valuesx).setColor(Color.BLUE).setCubic(false);
        Line liney = new Line(valuesy).setColor(Color.RED).setCubic(false);
        Line linez = new Line(valuesz).setColor(Color.GREEN).setCubic(false);

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
         //   e.printStackTrace();
        }

        axisY.setName(chan);
        axisX.setName("Time");
        axisY.setTextSize(12);
        axisX.setValues(null);
        data.setAxisXBottom(axisX);

        data.setAxisYLeft(axisY);

        chart.setInteractive(true);
        chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        chart.setZoomEnabled(true);
        chart.setMaximumViewport(new Viewport(chart.getMaximumViewport()));
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
        LinearLayout ll= new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(50, 50, 50, 50);
        ll.addView(chart, lp);
        chart.setOnValueTouchListener(new ValueTouchListener());
        Stats2Activity.charts.add(ll);
        counter++;
        System.out.println("counter "+counter);
        System.out.println("sensori "+sensori.size());

        if(counter==1){
            Intent myIntent = new Intent(this, Stats2Activity.class);
            startActivity(myIntent);
            StatsActivity.this.finish();
        }
        /*buttonGraph.addView(chart);
        TextView prova=new TextView(this);
        prova.setText("ciao ciao ciao ciao");
        buttonGraph.addView(prova);
        setContentView(rel);*/

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

                String THINGSPEAK_API_KEY = getSharedPreferences(strings[0],Context.MODE_PRIVATE).getString("read", "");
                THINGSPEAK_START_TIME=strings[2];
                THINGSPEAK_CHANNEL=strings[1];
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

        protected void onPostExecute(String response) {
            // We completely ignore the response
            // Ideally we should confirm that our update was successful

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
                else{
                    System.out.println("feeddddd " + array);
                    //myHelpMethod(res, array);
                   creaGrafico(res,array);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TYPE_EVENT = 0;

        }
    }

    public void initStat(String date){
        int i=0;
       vector=new JSONArray();
        
        for (String name : sensori) {
            i++;
            SharedPreferences pref = getSharedPreferences(name, Context.MODE_PRIVATE);

            SharedPreferences stats=getSharedPreferences("STATS" + i, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=stats.edit();
            editor.putString("index", String.valueOf(i));
            editor.putString("name", name);
            editor.putString("id",pref.getString("id", ""));
            editor.putString("date", date);
            editor.commit();
            JSONObject stat = new JSONObject();
            try {
                stat.put("index",i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                stat.put("name",name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                stat.put("id",pref.getString("id", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                stat.put("date", date);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            vector.put(stat);
            //Intent myIntent = new Intent(this, Stats2Activity.class);
           // myIntent.putExtra("jsonarray",vector.toString());
         //  startActivity(myIntent);

            new GetThingspeakTask().execute(name, pref.getString("id", ""), date);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        counter=0;

        // ScrollView buttonGraph=new ScrollView(this);
        SharedPreferences prefs=getSharedPreferences("SENSORI", Context.MODE_PRIVATE);
        System.out.println(prefs.getAll());


        for (String el: prefs.getAll().keySet()){
            if(el.contains("read")||el.contains("write")){

            } else if (el.contains("_type")){

            }
            else{
                sensori.add(el);

            }

        }



        Button bDay = (Button) findViewById(R.id.bDay);

        bDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("update DAY");
                long yourDateMillis = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000);
                Time yourDate = new Time();
                yourDate.set(yourDateMillis);

                String formattedDate = yourDate.format("%Y-%m-%dT%H:%M:%SZ");
                System.out.println("one day ago " + formattedDate);
               initStat(formattedDate);
            }
        });
        Button bWeek = (Button) findViewById(R.id.bWeek);
      //  bWeek.setText("Last Week");
        bWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("update WEEK");
                long yourDateMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
                Time yourDate = new Time();
                yourDate.set(yourDateMillis);

                String formattedDate = yourDate.format("%Y-%m-%dT%H:%M:%SZ");
                System.out.println("one week ago " + formattedDate);
                initStat(formattedDate);
            }
        });
        Button bMonth = (Button) findViewById(R.id.bMonth);
       // bMonth.setText("Last Month");
        bMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("update MONTH");
                long yourDateMillis = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000);
                yourDateMillis = yourDateMillis - (10 * 24 * 60 * 60 * 1000);
                yourDateMillis = yourDateMillis - (10 * 24 * 60 * 60 * 1000);


                Time yourDate = new Time();
                yourDate.set(yourDateMillis);

                String formattedDate = yourDate.format("%Y-%m-%dT%H:%M:%SZ");
                System.out.println("one month ago " + formattedDate);
               initStat(formattedDate);

            }
        });




       // creaGrafico(a, b);
    }

}
