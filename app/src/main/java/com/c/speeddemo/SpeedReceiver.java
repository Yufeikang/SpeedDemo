package com.c.speeddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 接受广播
 */
public class SpeedReceiver extends BroadcastReceiver {

    public SpeedReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String speed = intent.getExtras().getString("SPEED", "1000");
        Log.e("TAG", "onReceive: " + speed);

        if (speed != null) {
            Intent mIntent = new Intent(context,DashBoardActivity.class);
            mIntent.putExtra("DASH_SPEED",speed);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
        }
    }
}
