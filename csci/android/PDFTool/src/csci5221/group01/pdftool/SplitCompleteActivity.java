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

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;



public class SplitCompleteActivity extends Activity {

	public class OpenOnClickListener implements OnClickListener {
		private File file;
		public OpenOnClickListener(File file) {
	       	this.file = file;
	       	
	  	}
	  	public void onClick(View v) {
	  		if(file.exists() == false) {
	  			Log.e(TAG, file.toString() + " doesn't exist?");
	  			return;
	  		}
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "application/pdf");
			Log.i(TAG, file.toString());
			startActivity(intent);
	  	}
	};
	
	private String downloadUrl;
	private String webUrl;
	private String[] files;
	private String outputDir;
	private TableLayout pdfTable;
	private TextView splitPdfsUrl;
	
	private static final String TAG = "SplitCompleteActivity";
	
    protected void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_split_complete);
    	
    	Intent intent = getIntent();
    	outputDir = intent.getStringExtra("outputDir");
    	downloadUrl = intent.getStringExtra("downloadUrl");
    	webUrl = intent.getStringExtra("webUrl");
    	files = intent.getStringArrayExtra("files");
    	Log.i(TAG, "outputDir: " + outputDir);
    	
    	TableLayout pdfTable = (TableLayout)findViewById(R.id.split_pdf_table);
    	
    	for(String file : files) {
    		TableRow row = new TableRow(this);
    		TextView text = new TextView(this);
    		Button button = new Button(this);
    		button.setText("Open");
    		button.setOnClickListener(new OpenOnClickListener(new File(outputDir + file)));
    		text.setText(file);
    		row.addView(text);
    		row.addView(button);
    		pdfTable.addView(row);
    	}
    	
    	splitPdfsUrl = (TextView)findViewById(R.id.split_pdfs_url2);
    	splitPdfsUrl.setText(downloadUrl);
    	
    	Button shareButton = (Button) findViewById(R.id.split_pdfs_share_button);
    	shareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, splitPdfsUrl.getText());
				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "PDFs");
				startActivity(Intent.createChooser(intent, "Share"));
			}
		});
    	
    	Button openDirButton = (Button)findViewById(R.id.split_pdfs_open_dir_button);
    	openDirButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				Uri uri = Uri.parse(outputDir);
				intent.setDataAndType(uri, "text/csv");
				startActivity(Intent.createChooser(intent, "Open folder"));
			}
		});
    	
    	Button copyLink = (Button)findViewById(R.id.split_pdfs_copy_button);
    	copyLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				ClipData clip = ClipData.newPlainText("link", splitPdfsUrl.getText());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(v.getContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();
			}
		});
    	
    	final RadioButton directDownload = (RadioButton)findViewById(R.id.split_pdfs_direct_download);
    	final RadioButton webPage = (RadioButton)findViewById(R.id.split_pdfs_web_page);
    	directDownload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				webPage.setChecked(false);
				splitPdfsUrl.setText(downloadUrl);
			}
		});
		directDownload.setChecked(true);

		
		webPage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				directDownload.setChecked(false);
				splitPdfsUrl.setText(webUrl);
			}
		});
    }
}
