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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import csci5221.group01.pdftool.PdfManager.MergeData;
import csci5221.group01.pdftool.PdfManager.Submission;
import android.app.DownloadManager.Request;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class MergeActivity extends Activity implements OnClickListener
{
    private static final int REQUEST_CODE = 6384;
    //button and EditText event handlers
    private TextView loggedInAsTextView;
    private Button mergeButton;
    private Button addPdfButton;
    private TableLayout pdfFileList;
    private PdfManager pdfManager;
    private String email;
    private ArrayList<String> pdfPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge);

        Intent intent = getIntent();
        pdfManager = intent.getParcelableExtra("pdfManager");
        email = intent.getStringExtra("email");

        pdfPaths = new ArrayList<String>();
        pdfFileList = (TableLayout) findViewById(R.id.merge_pdfFileList);

        addPdfButton = (Button) findViewById(R.id.merge_addPdfButton);
        addPdfButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                showChooser();
            }
        });

        loggedInAsTextView = (TextView) findViewById(R.id.merge_loggedInAsTextView);

        mergeButton = (Button) findViewById(R.id.merge_mergeButton);

        mergeButton.setOnClickListener(this);

        if (email == null)
        {
            loggedInAsTextView.setText("Not logged in");
        }
        else
        {
            loggedInAsTextView.setText("Logged in as: " + email);
        }
    }

    public void onClick(View v)
    {
        if (v == mergeButton)
        {
            MergeData data = pdfManager.new MergeData(pdfPaths.toArray(new String[pdfPaths.size()]));
            new MergeTask(this, pdfManager, data).execute();
        }
    }

    private void removePdf(int i)
    {
        pdfPaths.remove(i);
        pdfFileList.removeViewAt(i);
        if (pdfPaths.size() < 2)
        {
            mergeButton.setEnabled(false);
        }
    }

    private void addPdf(String path)
    {
        pdfPaths.add(path);

        if (pdfPaths.size() >= 2)
            mergeButton.setEnabled(true);

        TableRow row = new TableRow(this);
        Button button = new Button(this);
        button.setText("Remove");

        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                TableRow row = (TableRow) v.getParent();
                int i = pdfFileList.indexOfChild(row);
                removePdf(i);
            }
        });

        TextView text = new TextView(this);
        Uri uri = Uri.parse(path);
        text.setText(uri.getLastPathSegment());

        row.addView(button);
        row.addView(text);
        pdfFileList.addView(row);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK && data != null)
                {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();
                    //Log.i(TAG, "Uri = " + uri.toString());
                    try
                    {
                        // Get the file path from the URI
                        final String path = FileUtils.getPath(this, uri);
                        addPdf(path);
                    }
                    catch (Exception e)
                    {
                        Log.e("FileSelectorTestActivity", "File select error", e);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.merge, menu);
        return true;
    }

    private void showChooser()
    {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(target, getString(R.string.choose_file));
        try
        {
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e)
        {
            // The reason for the existence of aFileChooser
        }
    }
    

    


    class MergeTask extends AsyncTask<Void, Void, Submission>
    {
        private PdfManager manager;
        private MergeData data;
        private ProgressDialog progress;
        private MergeActivity activity;

        MergeTask(MergeActivity activity, PdfManager manager, MergeData data)
        {
            this.activity = activity;
            this.manager = manager;
            this.data = data;
        }

        protected void onPreExecute()
        {
            progress = new ProgressDialog(activity);
            progress.setTitle("Merging");
            progress.setMessage("Sending PDFs to server...");
            progress.show();
        }

        protected Submission doInBackground(Void... params)
        {
            try
            {
                return manager.merge(data);
            }
            catch (final Exception e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        protected void onPostExecute(Submission submission)
        {
            progress.dismiss();
            if (submission == null)
            {
                return;
            }
            String message;
            if (submission.statusCode == 0)
            {
            	new DownloadMergeTask(activity, manager, submission).execute();
            }
            else
            {
                message = "Failure";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
            
        }
    }
}
