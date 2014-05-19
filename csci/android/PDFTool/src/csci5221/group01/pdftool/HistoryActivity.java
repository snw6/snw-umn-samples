/*
 * Copyright (C) 2014 Seth West, Tyler Schloesser, Vikram Reddy, Roman Dovgopol, and Jacob Dison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package csci5221.group01.pdftool;

import java.io.File;
import java.io.IOException;

import csci5221.group01.pdftool.PdfManager.Submission;
import csci5221.group01.pdftool.PdfManager.SubmissionType;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends Activity {
	
	PdfManager pdfManager;
	TableLayout historyTable;
	LayoutInflater inflater;
	
	private static final String TAG = "HistoryActivity";
	
	public class DownloadOnClickListener implements OnClickListener {
		private Submission submission;
		private Activity activity;
		private PdfManager manager;
		public DownloadOnClickListener(Submission submission, Activity activity, PdfManager manager) {
	       	this.submission = submission;
	       	this.activity = activity;
	       	this.manager = manager;
	       	
	  	}
	  	public void onClick(View v) {
	  		if(submission.type == SubmissionType.MERGE) {
	  			new DownloadMergeTask(activity, manager, submission).execute();
	  		} else {
	  			new DownloadSplitTask(activity, manager, submission).execute();
	  		}
	  	}
	};
	
    protected void onCreate(Bundle savedInstanceState)
    {        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_history);
    	
    	Intent intent = getIntent();
    	pdfManager = intent.getParcelableExtra("pdfManager");
    	
    	historyTable = (TableLayout)findViewById(R.id.history_table);
    	
    	inflater = LayoutInflater.from(this);
    	
    	
    	new FetchHistoryTask(pdfManager, this).execute();
    }
    
    
    public void addSubmission(Submission sub) {
    	TableRow row = (TableRow)inflater.inflate(R.layout.history_table_row, historyTable, false);
    	Button downloadButton = (Button)row.findViewById(R.id.history_table_row_download_button);
    	TextView typeText = (TextView)row.findViewById(R.id.history_table_row_type2);
    	TextView baseText = (TextView)row.findViewById(R.id.history_table_row_base);

    	downloadButton.setOnClickListener(new DownloadOnClickListener(sub, this, pdfManager));
    	
    	String type = "split";
    	if(sub.type == SubmissionType.MERGE) {
    		type = "merge";
    	}
    	typeText.setText(type);
    	baseText.setText(sub.baseName);
    	historyTable.addView(row, 0);
    }
    
    private class FetchHistoryResult {
    	public Submission[] subs;
    	public Exception e;
    	FetchHistoryResult() {
    		subs = null;
    		e = null;
    	}
    }
    
    class FetchHistoryTask extends AsyncTask<Void, Void, FetchHistoryResult> {
    	
    	PdfManager manager;
    	HistoryActivity activity;
    	
    	private ProgressDialog progress;
    	
    	FetchHistoryTask(PdfManager manager, HistoryActivity activity) {
    		this.manager = manager;
    		this.activity = activity;
    	}
    	
    	protected void onPreExecute()
    	{
    		progress = new ProgressDialog(activity);
    		progress.setTitle("Downloading");
    		progress.setMessage("Fetching history...");
    		progress.show();
    	}
    	
		protected FetchHistoryResult doInBackground(Void... params) {
			FetchHistoryResult result = new FetchHistoryResult();
			try {
				result.subs = manager.getHistory();
			} catch (IOException e) {
				result.e = e;
			}
			return result;
		}
		
		protected void onPostExecute(final FetchHistoryResult result) {
			progress.dismiss();
			if(result.e != null) {
				Toast.makeText(getApplicationContext(), result.e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
			
			for(Submission sub : result.subs) {
				activity.addSubmission(sub);
			}
		}
    	
    }
}
