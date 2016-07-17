package com.darren.pad;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

public class CounterReceiver extends BroadcastReceiver {
	static String TAG = CounterReceiver.class.getSimpleName();

	// MainActivity mainActivity;

	public CounterReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "ALARM SERVICE");

		SharedPreferences sharedPreference = context.getSharedPreferences(
				MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
		if (sharedPreference.getBoolean(MainActivity.EXTRA_TRACKING, true)
				&& (!sharedPreference.getBoolean(
						MainActivity.EXTRA_FIRST_ALARM, true))) {
			PowerManager pm = (PowerManager) context.getSystemService("power");
			KeyguardManager km = (KeyguardManager) context
					.getSystemService("keyguard");
			// phone screen on
			if (pm.isScreenOn() && (!km.inKeyguardRestrictedInputMode())) {
				ActivityManager am = (ActivityManager) context
						.getSystemService(Context.ACTIVITY_SERVICE);
				ComponentName compName = am.getRunningTasks(1).get(0).topActivity;

				MainActivity.recordAppUsage(compName, context);

				// screen was off, now is on. Use count + 1
				if (!sharedPreference.getBoolean(MainActivity.EXTRA_SCREEN_ON,
						false)) {
					sharedPreference.edit()
							.putBoolean(MainActivity.EXTRA_SCREEN_ON, true)
							.commit();
					MainActivity.addUseCount();
				}
				return;
			}
			// phone screen off
			if (sharedPreference
					.getBoolean(MainActivity.EXTRA_SCREEN_ON, false)) {
				sharedPreference.edit()
						.putBoolean(MainActivity.EXTRA_SCREEN_ON, false)
						.commit();
			}
			Log.d(TAG, "screen off");
		}

		if (sharedPreference.getBoolean(MainActivity.EXTRA_FIRST_ALARM, true)) {
			Log.i(TAG, "ALARM SERVICE FIRST ALRAM");
			sharedPreference.edit()
					.putBoolean(MainActivity.EXTRA_FIRST_ALARM, false).commit();
			return;
		}
		return;
	}

}
