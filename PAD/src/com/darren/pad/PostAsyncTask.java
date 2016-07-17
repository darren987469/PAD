package com.darren.pad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class PostAsyncTask extends AsyncTask<String, Void, String> {
	public static String TAG = PostAsyncTask.class.getSimpleName();
	Context context;
	String data;

	public PostAsyncTask(Context context, String data) {
		this.context = context;
		this.data = data;
	}

	@Override
	protected String doInBackground(String... urls) {
		return POST(urls[0], data);
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {
		//Toast.makeText(context, "Data Sent!", Toast.LENGTH_LONG).show();
		Toast.makeText(context, result, Toast.LENGTH_LONG).show();
	}
	
	// data is json string
	public static String POST(String url, String data) {
		InputStream inputStream = null;
		String result = "";
		try {

			// 1. create HttpClient
			HttpClient httpclient = new DefaultHttpClient();

			// 2. make POST request to the given URL
			HttpPost httpPost = new HttpPost(url);

			// 3. set data to StringEntity
			StringEntity se = new StringEntity(data,HTTP.UTF_8);
			
			// 4. set httpPost Entity
			httpPost.setEntity(se);
			Log.d(TAG, "data:"+data);
			// 5. Set some headers to inform server about the type of the
			// content
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			// 6. Execute POST request to the given URL
			HttpResponse httpResponse = httpclient.execute(httpPost);

			// 7. receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// 8. convert inputstream to string
			if (inputStream != null)
				result = convertInputStreamToString(inputStream);
			else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		// 9. return result
		return result;
	}

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}
}
