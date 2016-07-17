package com.darren.pad;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
	private static String TAG = MainActivity.class.getSimpleName();

	public static final String PREFS_NAME = "PADPrefs";
	public static final String EXTRA_SCREEN_ON = "screen_on";
	public static final String EXTRA_USE_COUNT = "usecount";
	public static final String EXTRA_FIRST_ALARM = "first_alarm";
	public static final String EXTRA_TRACKING = "tracking";
	public static final String EXTRA_APP_USAGE = "appusage";
	public static int PERIOD = 5;

	public static List<App> mApps;
	public static ArrayAdapter<App> mAdapter;

	public ListView appListView;
	public static SharedPreferences sharedPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreference = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		setContentView(R.layout.activity_main);

		mApps = retrieveApps();
		sortApps(mApps);
		mAdapter = new AppAdapter(this, mApps);
		// TODO uuid used to identify user
		// generate uuid if none
		// if (sharedPreference.getString("uuid", null) == null) {
		// String uuid = UUID.randomUUID().toString();
		// Log.d(TAG,"uuid:"+uuid);
		// }

		// there is no app information stored in sharedpreference, init it
		if (mApps.size() == 0) {
			Log.d(TAG, "init apps");
			mApps = getInstalledApps();
			saveApps();
		}

		if (sharedPreference.getString(EXTRA_USE_COUNT, null) == null) {
			Log.d(TAG, "init use count");
			// init use count data
			String KEY_DATE = "date";
			String KEY_USE_COUNT = "count";

			JSONArray useCounts = new JSONArray();
			JSONObject record = new JSONObject();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.TAIWAN);
			String today = sdf.format(new Date());
			try {
				record.accumulate(KEY_DATE, today);
				record.accumulate(KEY_USE_COUNT, "1");
				useCounts.put(record);
				sharedPreference.edit()
						.putString(EXTRA_USE_COUNT, useCounts.toString())
						.commit();
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "init use count error");
			}
		}
		appListView = (ListView) findViewById(R.id.appListView);
		appListView.setAdapter(mAdapter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView();
		if (checkAlarm()) {
			cancelAlarm();
		}
		setupAlarm();
	}

	public void refreshView() {
		// retrieve new records and then update view
		mApps = retrieveApps();
		sortApps(mApps);
		mAdapter.clear();
		mAdapter.addAll(mApps);
		mAdapter.notifyDataSetChanged();
	}

	public static void addUseCount() {
		JSONArray useCounts;
		JSONObject latestRecord;
		String KEY_DATE = "date";
		String KEY_USE_COUNT = "count";
		try {
			// get use count data from sharedpreference
			useCounts = new JSONArray(sharedPreference.getString(
					MainActivity.EXTRA_USE_COUNT, ""));
			latestRecord = useCounts.getJSONObject(useCounts.length()-1);

			// get today's datetime and compare with latestRecord
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.TAIWAN);
			String today = sdf.format(new Date());
			if (latestRecord.get(KEY_DATE).equals(today)) {
				// update latest record and useCounts
				int count = Integer.parseInt(latestRecord
						.getString(KEY_USE_COUNT)) + 1;
				// replace old record with updated record
				latestRecord.put(KEY_USE_COUNT, Integer.toString(count));
				useCounts.put(useCounts.length()-1, latestRecord);
				sharedPreference.edit()
						.putString(EXTRA_USE_COUNT, useCounts.toString())
						.commit();
			} else {
				// create new record and put it in useCount
				JSONObject record = new JSONObject();
				record.accumulate(KEY_DATE, today);
				record.accumulate(KEY_USE_COUNT, "1");
				// append new record to the end of useCounts 
				useCounts.put(record);
				sharedPreference.edit()
						.putString(EXTRA_USE_COUNT, useCounts.toString())
						.commit();
			}
			Log.d(TAG,
					"addUseCount(), count:"
							+ sharedPreference.getString(
									MainActivity.EXTRA_USE_COUNT, ""));
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "addUseCount() error: no useCounts data.");
		}
	}

	public static void recordAppUsage(ComponentName compName, Context context) {
		boolean appExists = false;
		mApps = retrieveApps();
		for (int i = 0; i < mApps.size(); i++) {
			if (compName.getPackageName().equals(mApps.get(i).getPackageName())) {
				// app is in appList, record it
				App app = mApps.get(i);
				mApps.remove(i);
				app.addCount(PERIOD);
				mApps.add(app);
				appExists = true;
				Log.d(TAG, "Record app:" + app.getAppName() + ", times:"
						+ app.getTimes().toString());
				appExists = true;
				break;
			}
		}

		// new app, add into app list
		if (!appExists) {
			PackageManager pm = context.getPackageManager();
			try {
				String appName = (String) pm.getApplicationLabel(pm
						.getApplicationInfo(compName.getPackageName(), 0));
				App app = new App(appName, compName.getPackageName());
				app.addCount(PERIOD);
				mApps.add(app);
				// mAdapter.notifyDataSetChanged();
				Log.d(TAG, "Add new app:" + app.toString() + "into app list");
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		saveApps();
	}

	public void setupAlarm() {
		Log.d(TAG, "setup alarm service");
		sharedPreference.edit().putBoolean(EXTRA_FIRST_ALARM, true).commit();

		Intent intent = new Intent("com.darren.pad.alarmaction");
		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager) getSystemService("alarm");
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		long l = 1000 * PERIOD;
		am.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), l, pIntent);
	}

	public void cancelAlarm() {
		Log.d(TAG, "cancel alarm service");
		Intent intent = new Intent("com.darren.pad.alarmaction");
		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		((AlarmManager) getSystemService("alarm")).cancel(pIntent);
		sharedPreference.edit().putBoolean(EXTRA_FIRST_ALARM, false).commit();
	}

	public boolean checkAlarm() {
		boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(),
				0, new Intent("com.darren.pad.alarmaction"),
				PendingIntent.FLAG_NO_CREATE) != null);
		return alarmUp;
	}

	public List<App> sortApps(List<App> apps) {
		Collections.sort(apps, new ListComparator());
		return apps;
	}

	/**
	 * Get installed apps.
	 * 
	 * @return
	 */
	public List<App> getInstalledApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> apps = getPackageManager().queryIntentActivities(
				mainIntent, 0);
		List<App> localAppList = new ArrayList<>();
		for (ResolveInfo app : apps) {
			App a = new App(app.loadLabel(getPackageManager()).toString(),
					app.activityInfo.packageName);
			localAppList.add(a);
		}
		return localAppList;
	}

	public static void saveApps() {
		JSONArray data = new JSONArray();
		for (App app : mApps) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("name", app.getAppName());
				obj.put("package", app.getPackageName());

				obj.put("count",
						new JSONArray(Arrays.asList(app.getTimes().toArray())));
				data.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e(TAG, "saveApps error()");
			}
		}
		sharedPreference.edit().putString(EXTRA_APP_USAGE, data.toString())
				.commit();
	}

	public static List<App> retrieveApps() {
		ArrayList<App> appList = new ArrayList<>();
		try {
			JSONArray data = new JSONArray(sharedPreference.getString(
					EXTRA_APP_USAGE, ""));
			for (int i = 0; i < data.length(); i++) {

				JSONObject obj = data.getJSONObject(i);
				// recover app times from jsonarray to arraylist
				JSONArray timeArr = obj.getJSONArray("count");
				ArrayList<String> times = new ArrayList<>();
				for (int j = 0; j < timeArr.length(); j++) {
					times.add(timeArr.getString(j));
				}
				App app = new App(obj.getString("name"),
						obj.getString("package"), times);
				appList.add(app);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "retrieveApps() error");
		}
		return appList;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(getApplicationContext(),
					SettingActivity.class);
			startActivity(intent);
			Log.d(TAG, "start setting activity");
			return true;
		}
		// TODO
		// else if (id == R.id.action_report) {
		// Intent intent = new Intent(getApplicationContext(),
		// ReportActivity.class);
		// startActivity(intent);
		// }
		return super.onOptionsItemSelected(item);
	}

}
