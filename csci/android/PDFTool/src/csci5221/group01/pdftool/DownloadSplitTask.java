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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import csci5221.group01.pdftool.PdfManager.Submission;


class DownloadSplitTask extends AsyncTask<Void, Void, Exception>
{
	private PdfManager manager;
	private Activity activity;
	private Submission submission;
	private ProgressDialog progress;
	private File outputDir;
	private ArrayList<String> files;
	private URL downloadUrl;
	private URL webUrl;
	
	DownloadSplitTask(Activity activity, PdfManager manager, Submission submission) {
		this.manager = manager;
		this.activity = activity;
		this.submission = submission;
		files = new ArrayList<String>();
	}
	
	protected void onPreExecute()
	{
		progress = new ProgressDialog(activity);
		progress.setTitle("Downloading");
		progress.setMessage("Downloading split PDFs...");
		progress.show();
	}
	
	protected Exception doInBackground(Void... params) {

		outputDir = new File("/sdcard/Download/" + submission.baseName);
		outputDir.mkdirs();
		
		try {
			downloadUrl = new URL(manager.getUri(submission).toString());
			webUrl = new URL(manager.getWebUri(submission).toString());
			URLConnection con = downloadUrl.openConnection();
			ZipInputStream zis = new ZipInputStream(con.getInputStream());
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null) {
				files.add(entry.getName());
				File outputFile = new File(outputDir, entry.getName());
				FileOutputStream fos = new FileOutputStream(outputFile);
				
				int bufSize = 1024;
				byte[] buffer = new byte[bufSize];
				int len = 0;
				while((len = zis.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				
			}
		} catch(IOException e) {
			return e;
		}
		return null;
	}
	
    protected void onPostExecute(Exception e)
    {
        progress.dismiss();
        if (e == null)
        {   
            Intent intent = new Intent(activity, SplitCompleteActivity.class);
            intent.putExtra("files", files.toArray(new String[files.size()]));
            intent.putExtra("outputDir", outputDir.toString() + "/");
            intent.putExtra("downloadUrl", downloadUrl.toString());
            intent.putExtra("webUrl", webUrl.toString());
            
            activity.startActivity(intent);
        }
        else
        {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        
    }
}
