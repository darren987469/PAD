package com.darren.pad;

import java.util.List;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppAdapter extends ArrayAdapter<App> {
	private static String TAG = AppAdapter.class.getSimpleName();
	private final Activity mContext;
	private List<App> mApps;
	
	int totalTime;

	public AppAdapter(Activity context, List<App> apps) {
		super(context, R.layout.applist_row, apps);
		mContext = context;
		mApps = apps;
		for(App app: mApps) {
			totalTime += app.getTimeCount();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// reuse views
		if (rowView == null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			rowView = inflater.inflate(R.layout.applist_row, null);
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = (TextView) rowView.findViewById(R.id.app_name);
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.app_icon);
			viewHolder.progress = (ProgressBar) rowView
					.findViewById(R.id.app_progress);
			viewHolder.time = (TextView) rowView.findViewById(R.id.app_time);
			rowView.setTag(viewHolder);
		}

		// fill data
		ViewHolder holder = (ViewHolder) rowView.getTag();
		App app = mApps.get(position);
		holder.name.setText(app.getAppName());
		try {
			holder.icon.setImageDrawable(mContext.getPackageManager()
					.getApplicationIcon(app.getPackageName()));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// mContext.getPackageManager().getApplicationIcon(packagename)
		// TODO: input valid progress
		long time = (long) app.getTimeCount();
		
		int persentage = totalTime == 0? 0 :(int) (time*100/totalTime);
		holder.progress.setProgress(persentage);
		
		int second = (int) time % 60;
		int minute = (int) (time / 60) % 60;
		int hour = (int) (time / (60*60)) % 24;
		String timecount = (hour == 0 ? "" : hour + "h")
				+ (minute == 0 ? "" : minute + "m") + second + "s    " + persentage+"%";
		holder.time.setText(timecount);
		
		return rowView;
	}

	static class ViewHolder {
		public TextView name;
		public ImageView icon;
		public ProgressBar progress;
		public TextView time;
	}
}
