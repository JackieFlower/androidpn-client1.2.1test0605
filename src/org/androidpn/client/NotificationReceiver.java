package org.androidpn.client;

import java.util.ArrayList;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

	private static final int SUCCESS = 1;

	private static final int FAILED = 0;
	
	private long downloadId = -1;
	
    private DownloadCompleteReceiver downloadCompleteReceiver;

    private static final String LOGTAG = LogUtil
            .makeLogTag(NotificationReceiver.class);
    
    public static DownloadManager dManager ;
    
    //DownloadCompleteReceiver receiver;
    
    public static Resource[] arrays ;
    
    public static int count = 0;
    
    public static int idx = 0;

    public static boolean isFirst = true;
    
    public static boolean downPic = false;
    
    public NotificationReceiver() {
    	downloadCompleteReceiver =  new DownloadCompleteReceiver();
    	//arrays = new Resource[];
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(LOGTAG, "NotificationReceiver.onReceive()...");
    	
        String action = intent.getAction();
        
        Log.d(LOGTAG, "action=" + action);	
        
	   dManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
   	 	
        if (Constants.ACTION_SHOW_NOTIFICATION.equals(action) && isFirst == true) {
        	
        	Log.d(LOGTAG, "in the ACTION_SHOW_NOTIFICATION");
        	isFirst = false;
        	String notificationConID = intent.
        			getStringExtra(Constants.NOTIFICATION_CONFIGURATION);
        	
        	String notificationDevID =  intent.
        			getStringExtra(Constants.NOTIFICATION_DEVICEID);
        	
        	ArrayList<Resource> lists = (intent.
        			getParcelableArrayListExtra(Constants.NOTIFICATION_RESOURCES_LIST));
        	    	
        	//类型转换，如果使用toArray()函数就会报出转换异常的错误～～
        	arrays = new Resource[lists.size()];

        	/*现将数据设的小一些，便于测试，以后再修改*/
        	count = lists.size();

        	idx = 0;
        	
        	lists.toArray(arrays);
        	
        	Log.i(LOGTAG, "begin to write data to the sharedprefence");
        	
        	SharedPreferences sharedPrefs = XmppManager.getSharedPrefs();
            Editor editor = sharedPrefs.edit();
            editor.putString(Constants.NOTIFICATION_CONFIGURATION,
            		notificationConID);
            editor.putString(Constants.NOTIFICATION_DEVICEID,
            		notificationDevID);
            editor.commit();
            
            Log.i(LOGTAG, "ConfigueID and deviceID registered successfully"); 
        	
            String notificationFrom = intent
            		.getStringExtra(Constants.NOTIFICATION_FROM);
            String packetId = intent
    				.getStringExtra(Constants.PACKET_ID);
            
            Log.d(LOGTAG, "notificationConID = " + notificationConID);
            Log.d(LOGTAG, "notificationDevID = " + notificationDevID);
            Log.d(LOGTAG, "get the resource ID");
            Log.d(LOGTAG, "notificationFrom = " + notificationFrom);
            Log.d(LOGTAG, "packetId = " + packetId);
            
            PreparedForDownload(idx, context);
            
            downPic = false;
        }else if(Constants.ACTION_NOTIFICATION_INSTALLED.equals(action)){
        	long id = intent.getLongExtra("DownloadID", 0);
        	
        	if(id == downloadId)
        		Log.d(LOGTAG, "the previous file download successful");
        	
        	if(downPic == false && (arrays[idx].getPicUri() != null || 
        			arrays[idx].getPicUri().length() != 0)){
        		
        		Log.d(LOGTAG, "begin to download the related pic info, Good Luck~~");
        		DownloadPic(idx, context);
        		downPic = true;
        	}else{
        		Log.d(LOGTAG, "begin to download a new resource, Good Luck~~");
        		//idx的初始值为0，当其大小为count时已经下载完毕
        		idx++;
            	if(idx >= count){
            		isFirst = true;
            		downPic = false;
            		count = 0;
            		idx = 0;
            	}else{
            		unregisterDownloadCompleteReceiver(context);
            		PreparedForDownload(idx, context);
            		downPic = false;
            	}
        	}
        }
    }
    
    private void PreparedForDownload(int index, Context context){
        Resource res = arrays[index];
         String sname = null;
        int idex = res.getResourceUri().lastIndexOf("/");// 返回String 对象中子字符串"/"最后出现的位置。
        
        if(idex >= 0 && idex < res.getResourceUri().length() - 2){
            sname = res.getResourceUri().substring(idex + 1);//该子字符串从指定索引处的字符开始，直到此字符串末尾
            Log.d(LOGTAG, "notificationSourceName=" + sname);
            
            downloadCompleteReceiver.setSourceName(sname);
            
            downloadCompleteReceiver.setResourceID(res.getResourceId());
            downloadCompleteReceiver.setResourceUri(res.getResourceUri());
            
            downloadCompleteReceiver.setRelatedID(null);
            
            registerDownloadCompleteReceiver(context);
     //-----------------------------------------------------------------------------------       
            if(!sname.endsWith(".apk") && !sname.endsWith(".flv") &&!sname.endsWith(".mp4")
            		){
            	System.out.println(" SYSTEM.OUT.-------Case 1:don't need to download");
            	Log.e(LOGTAG, "Case 1:don't need to download");
                Intent intent = new Intent();
                
                intent.putExtra("resourceID", res.getResourceId());
                intent.putExtra("uri", res.getResourceUri());
                intent.setAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                context.sendBroadcast(intent);
            }else if(sname.endsWith(".flv")||sname.endsWith(".mp4")){
            	int needDownload = res.getResourceId().charAt(3) - '0';
            	Log.e(LOGTAG, "needDownload = " + needDownload +
            			"resId = " + res.getResourceId());
            	
            	if(needDownload == 0){
            		System.out.println(" SYSTEM.OUT.-------Case 2: need == 0, don't need to download");
            		Log.e(LOGTAG, "Case 2: need == 0, don't need to download");
                    Intent intent = new Intent();
                    intent.putExtra("resourceID", res.getResourceId());
                    intent.putExtra("uri", res.getResourceUri());
                    //intent.setAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                    intent.setAction(Constants.ACTION_NOTIFICATION_NO_NEED_DOWNLOAD);
                    context.sendBroadcast(intent);
            	}else if(needDownload > 0){
            		Log.e(LOGTAG, "the video resource need to download, the resourceId = "
            				+ res.getResourceId() + ", the resourceUri = " + res.getResourceUri());	
            		download(context, res.getResourceUri(), sname);
            	}
            }
            else{
            	Log.e(LOGTAG, "It must be apk, need to download, resourceUri = "+ res.getResourceUri());
            	
                download(context, res.getResourceUri(), sname);
            }
  //----------------------------------------------------------------------------------- 
            
        }
    }
    
    private void DownloadPic(int index, Context context){
    	Resource res = arrays[index];
      	String sname = null;
     	int idex = res.getPicUri().lastIndexOf("/");
     	
     	if(idex >= 0 && idex < res.getPicUri().length() - 2){
     		sname = res.getPicUri().substring(idex + 1);
         	Log.d(LOGTAG, "picName=" + sname);
         	
         	downloadCompleteReceiver.setSourceName(sname);
         	
         	//downloadCompleteReceiver.setResourceID(res.getPicUri());
         	
         	Log.d(LOGTAG, "set the pic resourceId :null " );
         	downloadCompleteReceiver.setResourceID(null);
         	
         	Log.d(LOGTAG, " the pic resourceId :" + downloadCompleteReceiver.getResourceID() );
         	
         	downloadCompleteReceiver.setRelatedID(res.getResourceId());
         	
         	registerDownloadCompleteReceiver(context);
         	
         	download(context, res.getPicUri(), sname);
         	
         	/*Notifier notifier = new Notifier(context);
         	
             notifier.notify("index = " + index, "sname = " + sname,
             		 res.getResourceId(), res.getPicUri(), res.getPicUri(), 
                      null, null);*/
     	}
    }

    private int download(Context context, final String uri, final String source_name){
    	 if (uri != null
                 && uri.length() > 0
                 && (uri.startsWith("http:") || uri.startsWith("https:")
                         || uri.startsWith("tel:") || uri
                         .startsWith("geo:"))){
    		 
    		 Log.e(LOGTAG, "begin to download the resource");
    		 System.out.println(" SYSTEM.OUT.-------begin to download the resource");
    		 DownloadManager.Request down = new DownloadManager.Request(Uri.parse(uri));
    		 down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|
    				 						DownloadManager.Request.NETWORK_WIFI); 
    		 
    		 down.setShowRunningNotification(true);
    		 
    		 down.setVisibleInDownloadsUi(false); 
    		 
    		 down.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,source_name);
    		 
    		 System.out.println("the destination dir a is " + Environment.DIRECTORY_DOWNLOADS + "  " + source_name);
    		
    		 downloadId = dManager.enqueue(down);
   		 //--------------------------------------------------------------

    		    /*// Create a query for paused downloads.
    		    // DownloadManager dManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    		    Query failDownloadQuery = new Query();
    		    failDownloadQuery.setFilterByStatus(DownloadManager.STATUS_FAILED);

    		    // Query the Download Manager for fail downloads.
    		    Cursor failDownloads = dManager.query(failDownloadQuery);

    		    // Find the column indexes for the data we require.
    		    int downloadsId=failDownloads.getColumnIndex(DownloadManager.COLUMN_ID);
    		    int downloadsuri=failDownloads.getColumnIndex(DownloadManager.COLUMN_URI);

    		    // Iterate over the result Cursor.
    		    while (failDownloads.moveToNext()) {
    		      // Extract the data we require from the Cursor.
    		      
    		     int id=failDownloads.getInt(downloadsId);	
    		      dManager.remove(id);
    		    }

    		    // Close the result Cursor.
    		    failDownloads.close();*/
    		 
    		//--------------------------------------------------------------  		 
       		    		 
    		 Log.d(LOGTAG, "download begin! the download ID is " + downloadId);
    		 
    	 }else{
    		 Log.e(LOGTAG, "NOT download the resource");
    		 return FAILED;
    	 }
    	 return SUCCESS;
    }
    
    private void initDownload(Context context){
    	//dManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
    	
   	 //	DownloadManager.Query parseDownloadQuery = new DownloadManager.Query();
   	 	
	   //	parseDownloadQuery.setFilterByStatus(DownloadManager.STATUS_PAUSED);
	   	 
	   //	Cursor pausedDownloads = dManager.query(parseDownloadQuery);
	   	
	   //	int reasonIdx = pausedDownloads.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
	   	
	   	
	   	
	   //	while(pausedDownloads.moveToNext()){
	   		
	   //	}
 
   	 	//dManager.remove();
    }
    
    //注册信息
    private void registerDownloadCompleteReceiver(Context context){
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    	filter.addAction(Constants.ACTION_NOTIFICATION_NO_NEED_DOWNLOAD);
    	context.registerReceiver(downloadCompleteReceiver, filter);
    	Log.d(LOGTAG,"download complete receiver registerd!");
    }
    
    private void unregisterDownloadCompleteReceiver(Context context){
   	 	context.unregisterReceiver(downloadCompleteReceiver);
   }
    
    //下载状态查询
    public int[] getBytesAndStatus(long downloadId) { 
        int[] bytesAndStatus = new int[] { -1, -1, 0 }; 
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId); 
        Cursor c = null; 
        try { 
            c = dManager.query(query); 
            if (c != null && c.moveToFirst()) { 
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)); 
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)); 
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)); 
            } 
        } finally { 
            if (c != null) { 
                c.close(); 
            } 
        } 
        return bytesAndStatus; 
    } 
}
