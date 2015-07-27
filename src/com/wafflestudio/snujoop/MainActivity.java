package com.wafflestudio.snujoop;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	
	final static int RESULT_LOGIN = 1;
	final static int RESULT_DETAILSUBJECT = 2;
	final static int RESULT_FINDSUBJECT = 3;

	User user = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		user = new User();
		
		user = new User((Integer)1, new ArrayList<Integer>(), null);
		
		findViewById(R.id.atferLogin).setVisibility(View.GONE);

		((Button)findViewById(R.id.loginButton)).setOnClickListener(loginButtonClickEvent);
		((Button)findViewById(R.id.registerButton)).setOnClickListener(registerButtonClickEvent);
		((Button)findViewById(R.id.findButton)).setOnClickListener(findSubjectButtonClickEvent);
		((Button)findViewById(R.id.unregisterButton)).setOnClickListener(unregisterButtonClickEvent);
	}
	
	Button.OnClickListener loginButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivityForResult(intent, RESULT_LOGIN);
		}
	};
	
	Button.OnClickListener registerButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
			startActivity(intent);
		}
	};
	
	Button.OnClickListener findSubjectButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, FindSubjectActivity.class);
			intent.putExtra("userId", user.getId());
			intent.putExtra("subjectIdList", user.getSubjectIdList());
			intent.putExtra("userToken", user.getToken());
			
			startActivityForResult(intent, RESULT_FINDSUBJECT);
		}
	};
	
	Button.OnClickListener unregisterButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Unregister();
		}
	};

	private AdapterView.OnItemClickListener subjectItemClickListener = new AdapterView.OnItemClickListener() {
        @SuppressWarnings("unchecked")
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long l_position) {
            HashMap<String, String> hashmap = (HashMap<String, String>) parent.getAdapter().getItem(position);
            
			Intent intent = new Intent(MainActivity.this, DetailSubjectActivity.class);
			intent.putExtra("userId", user.getId());
			intent.putExtra("subjectIdList", user.getSubjectIdList());
			intent.putExtra("userToken", user.getToken());
			
			intent.putExtra("subjectId", Integer.parseInt(hashmap.get("id")));
			intent.putExtra("subjectName", hashmap.get("subject_name"));
			intent.putExtra("subjectNumber", hashmap.get("subject_number").split(" ")[0]);
			intent.putExtra("lectureNumber", hashmap.get("subject_number").split(" ")[1]);
			intent.putExtra("lecturer", hashmap.get("lecturer"));
			intent.putExtra("capacity", Integer.parseInt(hashmap.get("capacity")));
			intent.putExtra("capacityEnrolled", Integer.parseInt(hashmap.get("capacity_enrolled")));
			intent.putExtra("enrolled", Integer.parseInt(hashmap.get("enrolled")));

			startActivityForResult(intent, MainActivity.RESULT_DETAILSUBJECT);
        }
    };
	
	protected void onActivityResult(int requestCode, int resultCode, Intent Data){
		switch(requestCode){
		case RESULT_LOGIN:
			user = new User(
						Data.getIntExtra("userId", -1),
						Data.getIntegerArrayListExtra("subjectIdList"),
						Data.getStringExtra("userToken")
					);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login or cancel", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://revreserver.me:11663/users/"
						+ user.getId().toString() + ".json?token=" + user.getToken());
			break;
		case RESULT_DETAILSUBJECT:
			user = new User(
						Data.getIntExtra("userId", -1),
						Data.getIntegerArrayListExtra("subjectIdList"),
						Data.getStringExtra("userToken")
					);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://revreserver.me:11663/users/"
					+ user.getId().toString() + ".json?token=" + user.getToken());
			break;
		case RESULT_FINDSUBJECT:
			user = new User(
					Data.getIntExtra("userId", -1),
					Data.getIntegerArrayListExtra("subjectIdList"),
					Data.getStringExtra("userToken")
				);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://revreserver.me:11663/users/"
					+ user.getId().toString() + ".json?token=" + user.getToken());
			break;
		}
	}
	
    protected void Unregister() {
		if (GCMRegistrar.isRegistered(this)) {
			GCMRegistrar.unregister(this);
			Toast.makeText(this, "해제되었습니다.", Toast.LENGTH_LONG).show();
		}
	}

	private class LoadUserInformation extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        		
            return Http.GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		Log.d("ASYNC", "result = " + result);
        		user.getSubjectIdList().clear();
        		JSONObject jsonResult = null;
        		JSONArray jsonSubjectList = null;
        		SimpleAdapter adapter = null;
        		try {
        			List<HashMap<String, String>> subjectList = new ArrayList<HashMap<String, String>>();
					jsonResult = new JSONObject(result);
					jsonSubjectList = jsonResult.getJSONArray("subjects");
					for (int i = 0 ; i < jsonSubjectList.length() ; i++){
						JSONObject jsonSubject = jsonSubjectList.getJSONObject(i);

	        			HashMap<String, String> hashmap = new HashMap<String, String>();
	        			
						Integer id = jsonSubject.getInt("id");
						String subjectName = jsonSubject.getString("subject_name");
						String subjectNumber = jsonSubject.getString("subject_number") + " " + jsonSubject.getString("lecture_number");
						String lecturer = jsonSubject.getString("lecturer");
						Integer capacity = jsonSubject.getInt("capacity");
						Integer capacityEnrolled = jsonSubject.getInt("capacity_enrolled");
						Integer enrolled = jsonSubject.getInt("enrolled");
						hashmap.put("id", id.toString());
						hashmap.put("subject_name", subjectName);
						hashmap.put("subject_number", subjectNumber);
						hashmap.put("lecturer", lecturer);
						hashmap.put("capacity", capacity.toString());
						hashmap.put("capacity_enrolled", capacityEnrolled.toString());
						hashmap.put("enrolled", enrolled.toString());
						subjectList.add(hashmap);
						
						user.appendMySubjectIdList(jsonSubject.getInt("id"));
					}
					
					String[] from = {  "subject_name", "subject_number", "lecturer" };
					int[] to = { R.id.subjectName, R.id.subjectNumber, R.id.lecturer };
					adapter = new SimpleAdapter(getBaseContext(), subjectList, R.layout.subject_listview_content, from, to);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		ListView listView = (ListView)findViewById(R.id.subjectListView);
        		listView.setAdapter(adapter);
        		listView.setOnItemClickListener(subjectItemClickListener);

        		findViewById(R.id.loginButton).setVisibility(View.GONE);
        		findViewById(R.id.registerButton).setVisibility(View.GONE);
        		findViewById(R.id.unregisterButton).setVisibility(View.GONE);
        		findViewById(R.id.linlaHeaderProgress).setVisibility(View.GONE);
        		findViewById(R.id.developer).setVisibility(View.GONE);
        		findViewById(R.id.atferLogin).setVisibility(View.VISIBLE);
        	}
    		else {
				Toast.makeText(MainActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
}