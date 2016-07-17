package com.darren.pad;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {
	private static final String TAG = SettingActivity.class.getSimpleName();
	public static final String DARREN_URL = "http://192.168.0.100/adduser";
	public final String EXTRA_SERVER_URL = "server_url";
	public String serverUrl;
	public SharedPreferences sharepreference;
	Button btn;
	TextView textview;
	EditText edtServerUrl;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		btn = (Button) findViewById(R.id.btn_post);
		btn.setOnClickListener(listener);
		textview = (TextView) findViewById(R.id.textView1);
		if (!isConnected()) {
			Toast.makeText(this, "Internet is not connected",
					Toast.LENGTH_SHORT).show();
		}
		
		sharepreference = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
		serverUrl = sharepreference.getString(EXTRA_SERVER_URL, DARREN_URL);
		textview.setText("Current server url: "+serverUrl +"\nChange server url in following edit text.");
		edtServerUrl = (EditText) findViewById(R.id.edtServerUrl);
	}

	View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			JSONObject data = new JSONObject();
			try {
				data.accumulate(MainActivity.EXTRA_USE_COUNT, new JSONArray(sharepreference.getString(MainActivity.EXTRA_USE_COUNT, "")));
				data.accumulate(MainActivity.EXTRA_APP_USAGE, new JSONArray(sharepreference.getString(MainActivity.EXTRA_APP_USAGE, "")));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "send data:" + data.toString());
			if(!isEmpty(edtServerUrl.getText().toString())){
				serverUrl = edtServerUrl.getText().toString() + "/adduser";
				sharepreference.edit().putString(EXTRA_SERVER_URL, serverUrl).commit();
			}
			textview.setText("Current server url: "+serverUrl +"\nChange server url in following edit text.");
			new PostAsyncTask(getApplicationContext(),data.toString())
					.execute(serverUrl);
		}
	};
	
	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}
	
	private boolean isEmpty(String str) {
		if (str == null || str.isEmpty())
			return true;
		else
			return false;
	}
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

}
