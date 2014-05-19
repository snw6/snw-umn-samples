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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Paint.Join;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

enum SplitType
{
    SPLIT_INTO,
    SPLIT_EVERY
};



public class PdfManager implements Parcelable
{
	
	private static final String POST_TASK_TYPE = "task_type";
	private static final String POST_USER_AGENT = "user_agent";
	

    public static final Parcelable.Creator<PdfManager> CREATOR =
            new Parcelable.Creator<PdfManager>()
            {

                public PdfManager createFromParcel(Parcel source)
                {
                    return new PdfManager(source);
                }

                public PdfManager[] newArray(int size)
                {
                    return null;
                }
            };

    private static String TAG = "PdfManager";
    private Uri serverUri;
    private String token = null;

    public PdfManager(String serverRoot) throws MalformedURLException
    {
        this.serverUri = Uri.parse(serverRoot);
    }

    private PdfManager(Parcel in)
    {
        String[] data = new String[2];
        in.readStringArray(data);
        serverUri = Uri.parse(data[0]);
        token = data[1];
    }
    
    public Uri getUri(Submission submission, int[] files) {
    	Uri.Builder builder = serverUri.buildUpon();
    	builder.appendEncodedPath("downloadFiles.php");
        
    	if(submission.type == SubmissionType.SPLIT) {
    		builder.appendQueryParameter("archiveType", "zip");
    	}
    	
    	if(files != null) {
    		String indices = "";
    		for(int i = 0; i < files.length; i++) {
    			if(i != 0) {
    				indices += ',';	
    			}
    			indices += Integer.toString(files[i]);
    		}
    		builder.appendQueryParameter("files", indices);
    	}
    	
    	builder.appendQueryParameter("taskId", submission.taskId);
    	
    	Log.i(TAG, builder.build().toString());
    	
    	return builder.build();
    }
    
    public Uri getWebUri(Submission submission) {
    	Uri.Builder builder = serverUri.buildUpon();
    	builder.appendEncodedPath("viewTask.php");
    	builder.appendQueryParameter("taskId", submission.taskId);
    	
    	Log.i(TAG, builder.build().toString());
    	
    	return builder.build();
    }
    
    public void downloadLog(File outputFile) throws IOException {
    	MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    	entity.addTextBody("token", token);
    	
        HttpClient httpClient = new DefaultHttpClient();
        Uri postUri = serverUri.buildUpon().appendEncodedPath("getLog.php").build();
        HttpPost httpPost = new HttpPost(postUri.toString());
        
        httpPost.setEntity(entity.build());
        HttpResponse response = httpClient.execute(httpPost);
        
        if (response.getStatusLine().getStatusCode() == 200) {
        	HttpEntity he = response.getEntity();
        	FileOutputStream fos = new FileOutputStream(outputFile);
        	he.writeTo(fos);
        	he.consumeContent();        	
        } else {
        	throw new IOException(response.getStatusLine().getReasonPhrase());
        }
    }
    
    public Submission[] getHistory() throws IOException {
    	assert(token != null);
    	MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    	entity.addTextBody("token", token);
    	
        HttpClient httpClient = new DefaultHttpClient();
        Uri postUri = serverUri.buildUpon().appendEncodedPath("getHistory.php").build();
        HttpPost httpPost = new HttpPost(postUri.toString());
        
        httpPost.setEntity(entity.build());
        HttpResponse response = httpClient.execute(httpPost);
        
        if (response.getStatusLine().getStatusCode() == 200) {
        	String json = EntityUtils.toString(response.getEntity());
        	Submission[] result = new Gson().fromJson(json, Submission[].class);
        	return result;
        } else {
        	throw new IOException(response.getStatusLine().getReasonPhrase());
        }
    }
    
    public Uri getUri(Submission submission) {
    	return getUri(submission, null);
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    /**
     * @param data
     * @return
     * @throws IOException
     */
    public Submission split(SplitData data) throws Exception
    {
        String pdfPath = data.pdfPath;
        SplitType splitType = data.splitType;
        int n = data.n;
        int start = data.start;
        int end = data.end;

        if (n <= 0)
        {
            throw new IllegalArgumentException("n must be positive");
        }
        else if (start != 0 && end != 0 && start > end)
        {
            throw new IllegalArgumentException("start is not greater than end");
        }

        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
        File file = new File(pdfPath);
        if (file.canRead() == false)
        {
            throw new IOException("Cannot read from supplied file");
        }

        entity.addBinaryBody("files[]", new File(pdfPath), ContentType.DEFAULT_BINARY, file.getName());
        entity.addTextBody(POST_TASK_TYPE, "split");
        switch (splitType)
        {
            case SPLIT_INTO:
                entity.addTextBody("into", Integer.toString(n));
                break;
            case SPLIT_EVERY:
                entity.addTextBody("every", Integer.toString(n));
                break;
        }

        if (start > 0)
        {
            entity.addTextBody("start", Integer.toString(start));
        }
        if (end > 0)
        {
            entity.addTextBody("end", Integer.toString(end));
        }

        return submitTask(entity, SubmissionType.SPLIT);
    }

    /**
     * @param data
     * @return
     * @throws IOException
     */
    public Submission merge(MergeData data) throws Exception
    {
        String[] pdfPaths = data.pdfPaths;

        if (pdfPaths.length < 2)
        {
            throw new IllegalArgumentException("Must merge at least 2 PDFs");
        }

        MultipartEntityBuilder entity = MultipartEntityBuilder.create();
        for (int i = 0; i < pdfPaths.length; i++)
        {
            File file = new File(pdfPaths[i]);
            if (file.canRead() == false)
            {
                throw new IOException("Cannot read from supplied file");
            }
            entity.addBinaryBody("files[]", new File(pdfPaths[i]), ContentType.DEFAULT_BINARY, file.getName());
        }
        entity.addTextBody(POST_TASK_TYPE, "merge");
        return submitTask(entity, SubmissionType.MERGE);
    }

    private Submission submitTask(MultipartEntityBuilder entity, SubmissionType type) throws Exception
    {
        HttpClient httpClient = new DefaultHttpClient();
        Uri postUri = serverUri.buildUpon().appendEncodedPath("submitTask.php").build();
        Log.i(TAG, postUri.toString());
        HttpPost httpPost = new HttpPost(postUri.toString());

        if (token != null)
        {
            entity.addTextBody("token", token);
        }
        entity.addTextBody(POST_USER_AGENT, "android");
        
        entity.addTextBody("files", "nothing"); // TODO
        
        httpPost.setEntity(entity.build());
        HttpResponse response = httpClient.execute(httpPost);

        if (response.getStatusLine().getStatusCode() == 200)
        {
        	try {
	            String json = EntityUtils.toString(response.getEntity());
	            Log.i(TAG, json);
	            Submission result = new Gson().fromJson(json, Submission.class);
	            result.type = type;
	            return result;
        	} catch(Exception e) {
        		throw e;
        	}
        }
        else
        {
            throw new IOException(response.getStatusLine().getReasonPhrase());
        }
    }

    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(new String[]{serverUri.toString(), token});
    }

    /**
     * @author Tyler
     */
    public class SplitData
    {
        String pdfPath;
        SplitType splitType;
        int n;
        int start;
        int end;

        public SplitData(String pdfPath, SplitType splitType, int n, int start, int end)
        {
            this.pdfPath = pdfPath;
            this.splitType = splitType;
            this.n = n;
            this.start = start;
            this.end = end;
        }

        public SplitData(String pdfPath, SplitType splitType, int n)
        {
            this(pdfPath, splitType, n, 0, 0);
        }
    }

    /**
     * @author Tyler
     */
    public class MergeData
    {
        String pdfPaths[];

        public MergeData(String pdfPaths[])
        {
            this.pdfPaths = pdfPaths;
        }
    }

    public enum SubmissionType {
    	@SerializedName("split")
    	SPLIT,
    	@SerializedName("merge")
    	MERGE,
    }
    
    /**
     * @author Tyler
     */
    class Submission
    {
        int statusCode;
        String errorCode;
        String taskId;
        int fileCount;
        String baseName;
        SubmissionType type;
    }
}
