package com.brandonmkelley.appletaggerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setAppleTaggerListener();
	}

	private void setAppleTaggerListener() {

		Button button = (Button) findViewById(R.id.taggerSubmit);
		final EditText input = (EditText) findViewById(R.id.taggerInput);
		final TextView results = (TextView) findViewById(R.id.taggerResults);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String url = input.getText().toString();

				final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "", "Loading... Please wait.", true, true);

				customVisionAPIRequest(url, new Callback<String>() {
					@Override
					public void call(String arg) {
						results.setText(arg);
						dialog.dismiss();
					}
				});

			}
		});

	}

	private void customVisionAPIRequest(final String url, final Callback<String> callback) {

		final String subscriptionKey = "GET_YOUR_OWN!";
		final String uriBase = "Copy this from the Custom Vision API.";

		(new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {

				HttpClient httpclient = new DefaultHttpClient();

				try {

					HttpPost request = new HttpPost(uriBase);

					// Request headers.
					request.setHeader("Content-Type", "application/json");
					request.setHeader("Prediction-Key", subscriptionKey);

					// Request body.
					StringEntity reqEntity = new StringEntity("{\"Url\":\"" + url + "\"}");
					request.setEntity(reqEntity);

					// Execute the REST API call and get the response entity.
					HttpResponse response = httpclient.execute(request);
					HttpEntity entity = response.getEntity();

					if (entity != null) {
						// Format and display the JSON response.

						String jsonString = EntityUtils.toString(entity);
						JSONArray predictions = new JSONObject(jsonString).getJSONArray("Predictions");

						ArrayList<String> colorTags = new ArrayList<String>() {{
							add("red"); add("green"); add("yellow");
						}};
						ArrayList<String> countTags = new ArrayList<String>() {{
							add("1"); add("2"); add("3");
						}};

						String colorTag = "none";
						double colorProbability = 0;

						String countTag = "none";
						double countProbability = 0;

						for (int i = 0; i < predictions.length(); i++) {

							JSONObject prediction = predictions.getJSONObject(i);
							String tag = prediction.getString("Tag");
							double probability = prediction.getDouble("Probability");

							if (colorTags.contains(tag) && probability > colorProbability) {
								colorTag = tag;
								colorProbability = probability;
							}

							if (countTags.contains(tag) && probability > countProbability) {
								countTag = tag;
								countProbability = probability;
							}

						}

						return String.format("This picture was identified as having apples:\n    "
								+ colorTag + " in color, with a confidence of %g\n    "
								+ countTag + " in count, with a confidence of %g",
								colorProbability, countProbability);

					}

				}

				catch (Exception e) {
					// Display error message.
					e.printStackTrace();
				}

				return null;

			}

			@Override
			protected void onPostExecute(String result) {
				callback.call(result);
			}

		}).execute();

	}

	private interface Callback<T> {
		void call(T arg);
	}

}
