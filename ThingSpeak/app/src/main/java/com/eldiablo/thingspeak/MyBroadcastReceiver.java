package com.eldiablo.thingspeak;

/**
 * Created by eldiablo on 16/07/16.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class MyBroadcastReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            Intent serv = new Intent(context,MainService.class);
            context.startService(serv);

        }

}
