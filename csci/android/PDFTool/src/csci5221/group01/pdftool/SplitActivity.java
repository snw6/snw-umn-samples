package csci5221.group01.pdftool;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.ActivityNotFoundException;
import android.graphics.Color;
import android.widget.*;
import csci5221.group01.pdftool.PdfManager.SplitData;
import csci5221.group01.pdftool.PdfManager.Submission;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class SplitActivity extends Activity implements OnClickListener
{
    private static final int REQUEST_CODE = 6384;
    //button and EditText event handlers
    private Button split_chooseFileButton, split_splitButton;
    private TextView split_chooseFileTextView, split_loggedInAsTextView;
    private String file_chosen = "";
    private EditText splitIntoN, splitEveryN;
    private RadioButton split_splitIntoNFilesRB, split_splitEveryNPagesRB;
    private EditText splitStartPage, splitEndPage;
    private PdfManager pdfManager;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split);

        Intent intent = getIntent();
        pdfManager = intent.getParcelableExtra("pdfManager");
        email = intent.getStringExtra("email");

        split_loggedInAsTextView = (TextView) findViewById(R.id.split_loggedInAsTextView);
        splitStartPage = (EditText)findViewById(R.id.split_start_page);
        splitEndPage = (EditText)findViewById(R.id.split_end_page);
        split_chooseFileTextView = (TextView) findViewById(R.id.split_chooseFileTextView);

        split_chooseFileButton = (Button) findViewById(R.id.split_chooseFileButton);
        split_splitButton = (Button) findViewById(R.id.split_splitButton);
        split_chooseFileButton.setOnClickListener(this);
        split_splitButton.setOnClickListener(this);

        split_splitIntoNFilesRB = (RadioButton) findViewById(R.id.split_splitIntoNFilesRB);
        split_splitEveryNPagesRB = (RadioButton) findViewById(R.id.split_splitEveryNPagesRB);
        split_splitIntoNFilesRB.setOnClickListener(this);
        split_splitEveryNPagesRB.setOnClickListener(this);

        splitIntoN = (EditText) findViewById(R.id.split_splitIntoNFilesEditText);
        splitEveryN = (EditText) findViewById(R.id.split_splitEveryNPagesEditText);

        if (email == null)
        {
            split_loggedInAsTextView.setText("Not logged in");
        }
        else
        {
            split_loggedInAsTextView.setText("Logged in as: " + email);
        }

    }

    @Override
    public void onClick(View v)
    {
        // TODO Auto-generated method stub
        if (v == split_chooseFileButton)
        {
            showChooser();
        }
        else if (v == split_splitButton)
        {
            SplitType splitType;
            EditText splitN;
            if (split_splitIntoNFilesRB.isChecked())
            {
                splitType = SplitType.SPLIT_INTO;
                splitN = splitIntoN;
            }
            else if (split_splitEveryNPagesRB.isChecked())
            {
                splitType = SplitType.SPLIT_EVERY;
                splitN = splitEveryN;
            } else {
            	Toast.makeText(SplitActivity.this, "Please select a split method", Toast.LENGTH_LONG).show();
            	return;
            }
            
            int n;
            try {
            	n = Integer.parseInt(splitN.getText().toString());
            	if(n <= 0) {
            		throw new Exception();
            	}
            } catch(Exception e) {
            	Toast.makeText(SplitActivity.this, "Invalid number", Toast.LENGTH_LONG).show();
            	return;
            }
            
            SplitData data = pdfManager.new SplitData(file_chosen, splitType, n);
            
            String startPage = splitStartPage.getText().toString();
            String endPage = splitEndPage.getText().toString();

            try {
            	if(startPage.length() > 0) {
            		data.start = Integer.parseInt(startPage);
            	}
            	if(endPage.length() > 0) {
            		data.end = Integer.parseInt(endPage);
            	}
            } catch(Exception e) {
            	Toast.makeText(SplitActivity.this, "Invalid start/end page", Toast.LENGTH_LONG).show();
            	return;
            }
            
            new SplitTask(this, pdfManager, data).execute();
        }
        else if (v == split_splitIntoNFilesRB)
        {
            //Disabling splitEveryNPages option
            split_splitEveryNPagesRB.setChecked(false);
            splitEveryN.setFocusable(false);
            splitEveryN.setTextColor(Color.GRAY);

            //enabling splitIntoNFiles option
            splitIntoN.setFocusableInTouchMode(true);
            splitIntoN.requestFocus();
            splitIntoN.setTextColor(Color.BLACK);
        }
        else if (v == split_splitEveryNPagesRB)
        {
            //Disabling splitIntoNFiles option
            split_splitIntoNFilesRB.setChecked(false);
            splitIntoN.setFocusable(false);
            splitIntoN.setTextColor(Color.GRAY);

            //Enabling splitEveryNPages option
            splitEveryN.setFocusableInTouchMode(true);
            splitEveryN.requestFocus();
            splitEveryN.setTextColor(Color.BLACK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK)
                {
                    if (data != null)
                    {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        try
                        {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(SplitActivity.this, "File Selected: " + path, Toast.LENGTH_LONG).show();
                            file_chosen = path;
                            Uri uriFile = Uri.parse(path);
                            split_chooseFileTextView.setText(uriFile.getLastPathSegment());
                        }
                        catch (Exception e)
                        {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.split, menu);
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
    


    class SplitTask extends AsyncTask<Void, Void, Submission>
    {

        private PdfManager manager;
        private SplitData data;
        private ProgressDialog progress;
        private SplitActivity activity;

        SplitTask(SplitActivity activity, PdfManager manager, SplitData data)
        {
            this.manager = manager;
            this.data = data;
            this.activity = activity;
        }

        @Override
        protected void onPreExecute()
        {
            progress = new ProgressDialog(activity);
            progress.setTitle("Splitting");
            progress.setMessage("Sending PDF to server...");
            progress.show();
        }

        @Override
        protected Submission doInBackground(Void... params)
        {
            try
            {
                return manager.split(data);
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
                //message = "Success!"; 
            	new DownloadSplitTask(activity, manager, submission).execute();
            }
            else
            {
                message = "Failure";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
            
        }
    }
}
