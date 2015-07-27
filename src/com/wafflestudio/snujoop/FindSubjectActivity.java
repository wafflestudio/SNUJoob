package com.wafflestudio.snujoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FindSubjectActivity extends Activity {

	ArrayList<Subject> subjectList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_subject);
		
		subjectList = new ArrayList<Subject>();

		((Button)findViewById(R.id.find_button)).setOnClickListener(findButtonClickEvent);
	}
	
	Button.OnClickListener findButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String keyword = ((EditText)findViewById(R.id.keyword)).getText().toString().replaceAll("\\s+", "");
			if (keyword.equals("")){
				Toast.makeText(FindSubjectActivity.this, "검색어를 입력해주세요.", Toast.LENGTH_LONG).show();
				return;
			}
			new RequestFindSubject().execute(Http.HOME + "/subjects/search.json?keyword=" + keyword);
    		findViewById(R.id.linla_header_progress).setVisibility(View.VISIBLE);
		}
	};

	private OnItemClickListener subjectItemClickListener = new OnItemClickListener() {
		@SuppressWarnings("unchecked")
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long l_position) {
			HashMap<String, String> hashmap = (HashMap<String, String>) parent.getAdapter().getItem(position);
            
			Intent intent = new Intent(FindSubjectActivity.this, DetailSubjectActivity.class);
			intent.putExtra("subjectId", Integer.parseInt(hashmap.get("id")));
			intent.putExtra("subjectName", hashmap.get("subject_name"));
			intent.putExtra("subjectNumber", hashmap.get("subject_number").split(" ")[0]);
			intent.putExtra("lectureNumber", hashmap.get("subject_number").split(" ")[1]);
			intent.putExtra("lecturer", hashmap.get("lecturer"));
			intent.putExtra("classTime", hashmap.get("class_time"));
			intent.putExtra("capacity", Integer.parseInt(hashmap.get("capacity")));
			intent.putExtra("capacityEnrolled", Integer.parseInt(hashmap.get("capacity_enrolled")));
			intent.putExtra("enrolled", Integer.parseInt(hashmap.get("enrolled")));

			startActivity(intent);
        }
    };
    
    private class RequestFindSubject extends AsyncTask<String, Boolean, String> {
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
        		JSONObject jsonResult = null;
        		JSONArray jsonSubjectList = null;
        		SimpleAdapter adapter = null;
        		try {
        			List<HashMap<String, String>> subjectList = new ArrayList<HashMap<String, String>>();
					jsonResult = new JSONObject(result);
					jsonSubjectList = jsonResult.getJSONArray("result");
					
					for (int i = 0 ; i < jsonSubjectList.length() ; i++){
						JSONObject jsonSubject = jsonSubjectList.getJSONObject(i);

	        			HashMap<String, String> hashmap = new HashMap<String, String>();
	        			
						Integer id = jsonSubject.getInt("id");
						String subjectName = jsonSubject.getString("subject_name");
						String subjectNumber = jsonSubject.getString("subject_number") + " " + jsonSubject.getString("lecture_number");
						String lecturer = jsonSubject.getString("lecturer");
						String classTime = jsonSubject.getString("class_time");
						Integer capacity = jsonSubject.getInt("capacity");
						Integer capacityEnrolled = jsonSubject.getInt("capacity_enrolled");
						Integer enrolled = jsonSubject.getInt("enrolled");
						hashmap.put("id", id.toString());
						hashmap.put("subject_name", subjectName);
						hashmap.put("subject_number", subjectNumber);
						hashmap.put("lecturer", lecturer);
						hashmap.put("class_time", classTime);
						hashmap.put("capacity", capacity.toString());
						hashmap.put("capacity_enrolled", capacityEnrolled.toString());
						hashmap.put("enrolled", enrolled.toString());
						subjectList.add(hashmap);
					}
					
					String[] from = {  "subject_name", "subject_number", "lecturer", "class_time" };
					int[] to = { R.id.subject_name, R.id.subject_number, R.id.lecturer, R.id.class_time };
					adapter = new SimpleAdapter(getBaseContext(), subjectList, R.layout.subject_listview_content, from, to);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		ListView listView = (ListView)findViewById(R.id.result_list_view);
        		listView.setAdapter(adapter);
        		listView.setOnItemClickListener(subjectItemClickListener);

        		findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
        	}
    		else {
				Toast.makeText(FindSubjectActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
}