package csci5221.group01.pdftool;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MergeCompleteActivity extends Activity {
	
	private String downloadUrl, webUrl;
	String localPath;
	
	private RadioButton directDownload, webPage;
	private TextView mergedPdfUrl;
	
	private static final String TAG = "MergeCompleteActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_merge_complete);
    	
        Intent intent = getIntent();
        downloadUrl = intent.getStringExtra("downloadUrl");
        webUrl = intent.getStringExtra("webUrl");
        localPath = intent.getStringExtra("localPath");
    	
    	Button shareButton = (Button) findViewById(R.id.merged_pdf_share_button);
    	shareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, mergedPdfUrl.getText());
				intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "PDF");
				startActivity(Intent.createChooser(intent, "Share"));
			}
		});
    	
    	Button openButton = (Button)findViewById(R.id.merged_pdf_open_button);
    	openButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Log.i(TAG, localPath.toString());
				intent.setDataAndType(Uri.parse(localPath), "application/pdf");
				startActivity(intent);
			}
		});
    	
    	TextView mergedPdfName = (TextView)findViewById(R.id.merged_pdf_name);
    	mergedPdfName.setText(localPath);
    	
    	mergedPdfUrl = (TextView)findViewById(R.id.merged_pdf_url);
    	mergedPdfUrl.setText(downloadUrl);
    	
    	Button copyLink = (Button)findViewById(R.id.merged_pdf_copy_link_button);
    	copyLink.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
				ClipData clip = ClipData.newPlainText("link", mergedPdfUrl.getText());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(v.getContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();
			}
		});
    	
    	directDownload = (RadioButton)findViewById(R.id.merged_pdf_direct_download);
    	webPage = (RadioButton)findViewById(R.id.merged_pdf_web_page);
    	directDownload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				webPage.setChecked(false);
				mergedPdfUrl.setText(downloadUrl);
			}
		});
		directDownload.setChecked(true);

		
		webPage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				directDownload.setChecked(false);
				mergedPdfUrl.setText(webUrl);
			}
		});
    }
}
