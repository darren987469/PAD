package com.darren.pad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ReportActivity extends Activity {
	private static final String TAG = ReportActivity.class.getSimpleName();
	private static final int DIALOG_ADD_REPORT_TARGET = 1;
	private static final int DIALOG_LIST = 2;

	public ListView mListview;
	public List<String> mTargets;
	public int mCurPos;
	public ArrayAdapter<String> mAdapter;
	public Button btnAdd;
	public SharedPreferences sharedpreference;

	public List<String> retrieveReportTarget() {
		String serialized = sharedpreference.getString("target", "");
		ArrayList<String> targets = new ArrayList<>(Arrays.asList(TextUtils
				.split(serialized, ",")));

		return targets;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);

		sharedpreference = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

		addReportTarget();
		mTargets = retrieveReportTarget();
		Log.d(TAG, mTargets.toString());
		btnAdd = (Button) findViewById(R.id.btn_addtarget);
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showDialog(DIALOG_ADD_REPORT_TARGET);
			}
		});

		mListview = (ListView) findViewById(R.id.report_listview);
		mAdapter = new ArrayAdapter<>(this, R.layout.reportlist_row,
				R.id.txtReportTarget, mTargets);
		mListview.setAdapter(mAdapter);
		mListview.setOnItemClickListener(listener);

		// ActivityManager am = (ActivityManager)
		// getSystemService(Context.ACTIVITY_SERVICE);
		// RunningTaskInfo info = am.getRunningTasks(1).get(0);
		// Log.d(TAG, info.description.toString());
		// Log.d(TAG, info.topActivity.toString());
	}

	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			mCurPos = position;
			showDialog(DIALOG_LIST);
		}
	};

	private boolean isEmpty(String str) {
		if (str == null || str.isEmpty())
			return true;
		else
			return false;
	}

	public void addTarget(String target) {
		mTargets.add(target);
		saveTargets();
		// update target list
		mAdapter.notifyDataSetChanged();
	}

	public void deleteTarget() {
		mTargets.remove(mCurPos);
		saveTargets();
		mAdapter.notifyDataSetChanged();
	}

	public void saveTargets() {
		sharedpreference.edit()
				.putString("target", TextUtils.join(",", mTargets)).commit();
	}

	private void addReportTarget() {
		List<String> reportList = new ArrayList<>();
		reportList.add("darren987469@gmail.com");
		reportList.add("gogogay@gmail.com");

		SharedPreferences.Editor editor = sharedpreference.edit();
		editor.putString("target", TextUtils.join(",", reportList));
		editor.commit();
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id, Bundle args) {
		LayoutInflater factory = LayoutInflater.from(this);
		switch (id) {
		case DIALOG_ADD_REPORT_TARGET:
			final View dialogView = factory.inflate(
					R.layout.dialog_add_report_target, null);
			return new AlertDialog.Builder(ReportActivity.this)
					.setTitle("Please enter gmail account.")
					.setView(dialogView)
					.setPositiveButton("Submit",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// TODO
									EditText edtAccount = (EditText) dialogView
											.findViewById(R.id.useraccount_edit);
									String account = edtAccount.getText()
											.toString();
									// check empty account or password
									if (isEmpty(account)) {
										Toast.makeText(
												ReportActivity.this,
												"Account is empty. Please try again.",
												Toast.LENGTH_LONG).show();
										return;
									}
									addTarget(account);
									// success message
									Toast.makeText(
											ReportActivity.this,
											"Successful add target: " + account,
											Toast.LENGTH_LONG).show();
								}
							}).setNegativeButton("Cancel", null).create();
		case DIALOG_LIST:
			return new AlertDialog.Builder(ReportActivity.this)
					.setTitle(mTargets.get(mCurPos))
					.setItems(R.array.select_dialog_items,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									String[] items = getResources()
											.getStringArray(
													R.array.select_dialog_items);
									if (items[which].equals("delete")) {
										Toast.makeText(
												getApplicationContext(),
												"Successfully delete "
														+ mTargets.get(mCurPos),
												Toast.LENGTH_SHORT).show();
										deleteTarget();
									} else if (items[which]
											.equals("send mail")) {
										// TODO generate report
										Toast.makeText(getApplication(),
												items[which],
												Toast.LENGTH_SHORT).show();
									}

								}
							}).create();

		default:
			return null;
		}
	}
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

}
