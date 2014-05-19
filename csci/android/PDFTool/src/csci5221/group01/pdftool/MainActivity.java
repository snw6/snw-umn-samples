package csci5221.group01.pdftool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import csci5221.group01.pdftool.PdfManager.Submission;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    private static final String TAG = "MainActivity";
    String token = null;
    private SharedPreferences preferences;
    private String email;
    private TextView main_loggedInAsTextView;
    private PdfManager pdfManager;

    Button loginButton;
    Button downloadLogButton;

    private void onLogin()
    {
        Log.i(TAG, email);
        main_loggedInAsTextView.setText("Logged in as: " + email);
        loginButton.setText(getString(R.string.main_LogoutButtonText));
        downloadLogButton.setEnabled(true);
    }

    private void onLogout()
    {
        main_loggedInAsTextView.setText("Not logged in");
        loginButton.setText(getString(R.string.main_loginButtonText));
        downloadLogButton.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_loggedInAsTextView = (TextView) findViewById(R.id.main_loggedInAsTextView);
        loginButton = (Button) findViewById(R.id.main_loginButton);
        downloadLogButton = (Button) findViewById(R.id.main_viewLogButton);

        preferences = getSharedPreferences(getString(R.string.preferences_file), MODE_PRIVATE);
        email = preferences.getString(getString(R.string.saved_email), null);

        if (email == null)
        {
            //user is not logged in
            onLogout();
        }
        else
        {
        	new LoginTask(this, email, SCOPE).execute();
        }

        String serverUrl = preferences.getString(getString(R.string.saved_server_url), null);
        if (serverUrl == null)
        {
            serverUrl = getString(R.string.default_server_url);
        }
        Log.i(TAG, serverUrl);

        String download_directory = preferences.getString(getString(R.string.saved_download_directory), null);
        if (download_directory == null)
        {
            download_directory = Environment.DIRECTORY_DOWNLOADS;
            Log.i(TAG, download_directory);
        }

        try
        {
            pdfManager = new PdfManager(serverUrl);

            if (token != null)
            {
                pdfManager.setToken(token);
            }
        }
        catch (MalformedURLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Adding Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Merge button is clicked
    public void main_mergeButtonClicked(View v)
    {
        //create a new Intent to launch the MergeActivity screen
        Intent intent = new Intent(MainActivity.this, MergeActivity.class);
        intent.putExtra("pdfManager", pdfManager);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    //Split Button is clicked
    public void main_splitButtonClicked(View v)
    {
        //create a new Intent to launch the SplitActivity screen
        Intent intent = new Intent(this, SplitActivity.class);
        intent.putExtra("pdfManager", pdfManager);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    //View Log/Download files Button is clicked
    public void main_viewLogButtonClicked(View v)
    {
    	//new GetLogTask(this, pdfManager).execute();
    	Intent intent = new Intent(this, HistoryActivity.class);
    	intent.putExtra("pdfManager", pdfManager);
    	startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT)
        {
            if (resultCode == RESULT_OK)
            {
                email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                // Save the email address
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.saved_email), email);
                editor.commit();

                Log.i(TAG, email);
                login();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "You must pick an account", Toast.LENGTH_SHORT).show();
            }
        }
        else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR ||
                requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK)
        {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleAuthorizeResult(int resultCode, Intent data)
    {
        if (data == null)
        {
            Log.i(TAG, "Unknown error, click the button again");
        }
        if (resultCode == RESULT_OK)
        {
            new LoginTask(this, email, SCOPE).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED)
        {
            Log.i(TAG, "User rejected authorization.");
        } else {
        	Log.i(TAG, "Unknown error, click the button again");
        }
        email = null;
        return;
        
        
    }

    /**
     * Checks whether the device currently has a network connection
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        return false;
    }

    public void login()
    {
        if (email == null)
        {
            String[] accountTypes = new String[]{"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
        }
        else
        {
            if (isDeviceOnline())
            {
                if (token == null)
                {
                    new LoginTask(this, email, SCOPE).execute();
                }
                else
                {
                    Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logout()
    {
        new LogoutTask(this, token).execute();
    }

    //Login Button is clicked
    public void main_loginButtonClicked(View v)
    {
        if (loginButton.getText() == getString(R.string.main_loginButtonText))
        {
            login();
        }
        else
        {
            logout();
        }
    }

    /**
     * This method is a hook for background threads and async tasks that need to provide the
     * user a response UI when an exception occurs.
     */
    public void handleException(final Exception e)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (e instanceof GooglePlayServicesAvailabilityException)
                {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                }
                else if (e instanceof UserRecoverableAuthException)
                {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
                else {
                	email = null;
                	onLogout();
                }
            }
        });
    }

    class LogoutTask extends AsyncTask<Void, Void, Exception>
    {

        private MainActivity activity;

        LogoutTask(MainActivity activity, String token)
        {
            this.activity = activity;
        }

        private ProgressDialog progress;

        protected void onPreExecute()
        {
            progress = new ProgressDialog(activity);
            progress.setTitle("Logging out");
            progress.setMessage("Please wait...");
            progress.show();
        }

        protected Exception doInBackground(Void... params)
        {
            if (token != null)
            {
                try
                {
                    GoogleAuthUtil.clearToken(getApplicationContext(), token);
                }
                catch (Exception e)
                {
                    return e;
                }
            }
            return null;
        }

        protected void onPostExecute(Exception e)
        {
            progress.dismiss();

            if (e != null)
            {
                Log.d(TAG, e.toString());
                return;
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.saved_email), "");
            editor.commit();

            token = email = null;
            pdfManager.setToken(null);

            onLogout();
        }
    }

    class LoginTask extends AsyncTask<Void, Void, String>
    {

        private String mEmail;
        private String mScope;
        private MainActivity mActivity;
        private ProgressDialog progress;

        LoginTask(MainActivity activity, String email, String scope)
        {
            this.mEmail = email;
            this.mScope = scope;
            this.mActivity = activity;
        }

        @Override
        protected void onPreExecute()
        {
            progress = new ProgressDialog(mActivity);
            progress.setTitle("Logging in");
            progress.setMessage("Please wait...");
            progress.show();
        }

        @Override
        protected void onPostExecute(String token)
        {
            progress.dismiss();

            if (token == null)
            {
                return;
            }
            pdfManager.setToken(token);

            onLogin();

            Log.i(TAG, token);
        }

        @Override
        protected String doInBackground(Void... params)
        {
            try
            {
                return GoogleAuthUtil.getToken(getApplicationContext(), mEmail, mScope);
            }
            catch (Exception e)
            {
                mActivity.handleException(e);
            }
            return null;
        }
    }
    
	private class GetLogResult {
		Exception e;
		File logFile;
		GetLogResult() {
			e = null;
			logFile = null;
		}
	}
    
    class GetLogTask extends AsyncTask<Void, Void, GetLogResult>
    {
    	private PdfManager manager;
    	private MainActivity activity;
    	private ProgressDialog progress;
    	GetLogTask(MainActivity activity, PdfManager manager) {
    		this.manager = manager;
    		this.activity = activity;
    	}
    	
    	protected void onPreExecute()
    	{
    		progress = new ProgressDialog(activity);
    		progress.setTitle("Fetching");
    		progress.setMessage("Fetching the log...");
    		progress.show();
    	}
    	

    	
    	protected GetLogResult doInBackground(Void... params) {
    		GetLogResult result = new GetLogResult();
    		try {
    			File logFile = new File("/sdcard/Download/pdftool_log.txt");
    			manager.downloadLog(logFile);
    			result.logFile = logFile;
    		} catch (IOException e) {
    			result.e = e;
    		}
    		return result;
    	}
    	
        protected void onPostExecute(final GetLogResult result)
        {
            progress.dismiss();
            String message;
            if (result.e == null)
            {
            	new AlertDialog.Builder(activity)
            		.setTitle("Log fetched")
            		.setMessage("The log has been saved to: \"" + result.logFile.toString() + "\"\nWould you like to open it?")
            		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(result.logFile), "text/plain");
							startActivity(intent);
						}
					})
					.show();
            }
            else
            {
                message = result.e.getMessage();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
            
        }
    }
}
