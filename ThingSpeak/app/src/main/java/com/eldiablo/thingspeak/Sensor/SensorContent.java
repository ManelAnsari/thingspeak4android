package com.eldiablo.thingspeak.Sensor;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */




public class SensorContent {
    /**
     * An array of sample (Sensor) items.
     */

    public static final List<SensorItem> ITEMS = new ArrayList<SensorItem>();

    /**
     * A map of sample (Sensor) items, by ID.
     */
    public static final Map<String, SensorItem> ITEM_MAP = new HashMap<String, SensorItem>();

    private static final int COUNT = 25;


    static {
    }



    public static void addItem(SensorItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.Sensorname, item);
    }

    public static SensorItem createSensorItem(int id,JSONObject json) {
        try {
            return new SensorItem(id,String.valueOf(json.get("Sensorname")), String.valueOf(json.get("vendor")),(int) json.get("version"), (int) json.get("type"),(Double) json.get("maxRange"),(Double) json.get("resolution"),(Double) json.get("power"),(int) json.get("minDelay"),false,"","","");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A Sensor item representing a piece of content.
     */
    public static int getSelected(){
        int count=0;
        for (int i=0;i<ITEMS.size();i++){
            if(ITEMS.get(i).selected){
                count++;
            }
        }
        return count;
    }
    public static class SensorItem {
        public final int id;
        public final String Sensorname;
        public final String vendor;
        public final int version;
        public final int type;
        public final Double maxRange;
        public final Double resolution;
        public final Double power;
        public final int minDelay;
        public Boolean selected;
        public String api_channel;
        public String api_write;
        public String api_read;
        public SensorItem(int id,String Sensorname, String vendor, int version, int type, Double maxRange,Double resolution,Double power,int minDelay,boolean selected,String api_channel,String api_write,String api_read) {
            this.id=id;
            this.Sensorname=Sensorname;
            this.vendor=vendor;
            this.version=version;
            this.type=type;
            this.maxRange=maxRange;
            this.resolution=resolution;
            this.power=power;
            this.minDelay=minDelay;
            this.selected=selected;
            this.api_channel=api_channel;
            this.api_write=api_write;
            this.api_read=api_read;
        }

        @Override
        public String toString() {
            return Sensorname;
        }
    }
}
