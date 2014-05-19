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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import csci5221.group01.pdftool.PdfManager.Submission;

class DownloadResult {
	public Exception e;
	public File pdf;
	DownloadResult() {
		e = null;
		pdf = null;
	}
}
class DownloadMergeTask extends AsyncTask<Void, Void, DownloadResult>
{
	private PdfManager manager;
	private Activity activity;
	private Submission submission;
	private ProgressDialog progress;
	private URL downloadUrl;
	private URL webUrl;
	
	DownloadMergeTask(Activity activity, PdfManager manager, Submission submission) {
		this.manager = manager;
		this.activity = activity;
		this.submission = submission;
	}
	
	protected void onPreExecute()
	{
		progress = new ProgressDialog(activity);
		progress.setTitle("Downloading");
		progress.setMessage("Downloading merged PDF...");
		progress.show();
	}
	

	
	
	protected DownloadResult doInBackground(Void... params) {
		
		DownloadResult result = new DownloadResult();
		
		try {
			downloadUrl = new URL(manager.getUri(submission).toString());
			webUrl = new URL(manager.getWebUri(submission).toString());
			URLConnection con = downloadUrl.openConnection();
			File outputDir = new File("/sdcard/Download");
			outputDir.mkdirs();
			
			File outputFile = new File(outputDir, submission.baseName + ".pdf");
			FileOutputStream fos = new FileOutputStream(outputFile);
			int bufSize = 1024;
			byte[] buffer = new byte[bufSize];
			int len = 0;
			InputStream is = con.getInputStream();
			while((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			is.close();
			
			result.pdf = outputFile;
			
		} catch (Exception e) {
			result.e = e;
		}
		return result;
	}
	
    protected void onPostExecute(final DownloadResult result)
    {
        progress.dismiss();
        String message;
        if (result.e == null)
        {
        	
        	String localPath = result.pdf.toURI().toString();
        	
        	Intent intent = new Intent(activity, MergeCompleteActivity.class);
        	intent.putExtra("downloadUrl", downloadUrl.toString());
        	intent.putExtra("localPath", localPath);
        	intent.putExtra("webUrl", webUrl.toString());
        	activity.startActivity(intent);
        	
        }
        else
        {
            message = result.e.getMessage();
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
        
    }
}
