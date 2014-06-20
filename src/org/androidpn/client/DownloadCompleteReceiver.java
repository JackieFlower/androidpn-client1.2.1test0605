
package org.androidpn.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.androidpn.laumanager.DBManager;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

class DownloadCompleteReceiver extends BroadcastReceiver { 
	
    private static final String LOGTAG = LogUtil
            .makeLogTag(DownloadCompleteReceiver.class);
    public static int count = 0;
    private String sourceName = null ;
    private String resourceID =  null;
    private String resourceUri=null;
	private String relatedReID = null;
    private PackageManager pm;
    private DBManager dbManager;
    private String appLabel, pkgName;
    String defalutPath = null;
    String fullPath = null;
    
    public DownloadCompleteReceiver(){
    	
    	defalutPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
    										"/" + Environment.DIRECTORY_DOWNLOADS ;
    	//fullPath = defalutPath + "/";
    	count++;
    	Log.d(LOGTAG,"Download 's count = " + count);
    }
    
    public DownloadCompleteReceiver(String sourceName, String resourceID){
    	this.sourceName = sourceName;
    	this.resourceID = resourceID;
    	
    	defalutPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
    										"/"+Environment.DIRECTORY_DOWNLOADS ;
    	fullPath = defalutPath + "/" + sourceName;
    	
    	count++;
    	Log.d(LOGTAG, count + "");
    }
    
    public DownloadCompleteReceiver(String sourceName, String resourceID,
    		String relatedID){
    	this.sourceName = sourceName;
    	this.resourceID = resourceID;
    	this.relatedReID = relatedID;
    	defalutPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
    										"/"+Environment.DIRECTORY_DOWNLOADS ;
    	fullPath = defalutPath + "/" + sourceName;
    }
    
    public void setSourceName(String name){
    	this.sourceName =  name;
    	fullPath = defalutPath + "/" + sourceName;
    	
    	Log.d(LOGTAG, "new Source name is " + sourceName);
    	
    	Log.d(LOGTAG, "the resource set null~~");
    	
    	setResourceID(null);
    }
    
    public void setResourceID(String id){
    	this.resourceID = id;
    	
    	Log.d(LOGTAG, "the resource set :" + resourceID);
    }
    
    public String getResourceID(){
    	return this.resourceID;
    }

    public String getResourceUri() {
		return resourceUri;
	}

	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}
    public void setRelatedID(String reID){
    	this.relatedReID = reID;
    	Log.d(LOGTAG, "the relatedID set: " + this.relatedReID);
    }
    
    public void SilentInstall_Run(String path){
    	if(path.equals(null) || path.length() <= defalutPath.length()){
    		Log.e(LOGTAG, "the path is illegle");
    		return;
    	}
    	try {
    		Log.e(LOGTAG, "begin to exec the command");
			Runtime.getRuntime().exec("pm install -r " + path);
		} catch (IOException e) {
			Log.e(LOGTAG, "in the silentInstall_Run function, install failed");
			e.printStackTrace();
		}
    	
    }
   
    
    @Override  
    public void onReceive(Context context, Intent intent) { 
    	
    	Log.d(LOGTAG , "the download path is " + this.defalutPath);
    	
    	pm = context.getPackageManager();
    	dbManager = new DBManager(context);
    	
        if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){  
        	
            long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);  
            
            if(downId == NotificationReceiver.idx)
            	Log.d(LOGTAG, "it's the file we want");
            
            
            Log.d(LOGTAG," download complete! id : " + downId);  
            
            Log.d(LOGTAG," path : "+ fullPath);  
            
            if(sourceName.endsWith(".apk") && resourceID != null){
            	 SilentInstall_Run(fullPath);
            	 Log.d(LOGTAG,"after install apk~~~" + fullPath);
            	 parseResourceID(resourceID);
                 Toast.makeText(context, sourceName +"install success!", Toast.LENGTH_SHORT).show();
            }else {
            	if(sourceName.endsWith("png") || sourceName.endsWith("jpg")
            		|| sourceName.endsWith("gif")){
            		
	            	if(resourceID != null)
	            		parseResourceID(resourceID);
	            	else 
	            		parseNullReID();
            	} 
            }
          //---------------------------------------------------------------
            if((sourceName.endsWith(".flv")||sourceName.endsWith(".mp4") )&& resourceID != null){
            	
	           	 Log.d(LOGTAG,"after download viedo~~~" + fullPath);
	           	 parseResourceID(resourceID);
           }else {
           	if(sourceName.endsWith("png") || sourceName.endsWith("jpg")
           		|| sourceName.endsWith("gif")){
           	if(resourceID != null)
           		parseResourceID(resourceID);
           	else 
           		parseNullReID2();
           } 
           }
          //---------------------------------------------------------------
            Intent bdIntent = new Intent(Constants.ACTION_NOTIFICATION_INSTALLED);
            bdIntent.putExtra("DownloadID", downId);
            context.sendBroadcast(bdIntent);
            
            //------------------------------------------------------------
            DownloadManager dManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
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
		    failDownloads.close();
		    //---------------------------------------------------------------
        }  
    }  
    
    private void parseNullReID(){
    	if(resourceID == null){
    		Log.d(LOGTAG, "begin to get the NULL resourceID~~");
    		
    		if(relatedReID != null && (sourceName.endsWith("png")
					||sourceName.endsWith("jpg")
					||sourceName.endsWith("gif"))){
				
				Log.d(LOGTAG, "begin to insert the pic info~~");
				dbManager.updateBackPic(relatedReID, null, defalutPath, sourceName);
			}
    	}
    }
    
    //---------------------------------------------------------------   
    private void parseNullReID2(){
    	if(resourceID == null){
    		Log.d(LOGTAG, "begin to get the NULL resourceID~~");
    		
    		if(relatedReID != null && (sourceName.endsWith("png")
					||sourceName.endsWith("jpg")
					||sourceName.endsWith("gif"))){
				
				Log.d(LOGTAG, "begin to insert the pic related with the tv info~~");
				dbManager.updateBackPic2(relatedReID, null, defalutPath, sourceName);
			}
    	}
    } 
    //---------------------------------------------------------------      
    private void parseResourceID(String resourceID){//解析ID号并更新数据库
    	Log.d(LOGTAG, "begin to parse ResouceID :" + resourceID);
    	
		char InfoType = resourceID.charAt(0);
		//int ItemType = Integer.parseInt(resourceID.charAt(1));
		
		int ItemType = resourceID.charAt(1) - '0';
		int position = resourceID.charAt(2) - '0';
		int value=resourceID.charAt(3) - '0';
		
		Log.d(LOGTAG, "begin to get the position:" + position);
		Log.d(LOGTAG, "begin to get the ItemType:" + ItemType);
		switch(InfoType){
		//APK
		case '1':
			Log.d(LOGTAG, "insertData");
			
			if(sourceName.endsWith(".apk")){
				Log.d(LOGTAG, "insertAPKData, sourceName = " + sourceName);
				if(!fullPath.endsWith(".apk"))
					fullPath = defalutPath + "/" + sourceName;
				
				Log.d(LOGTAG, "begin to get the apkinfo");	
				getAPKInfo();
				Log.d(LOGTAG, "begin to insert APK");
				
				if(dbManager == null){
					Log.d(LOGTAG, "the dbManager is null");
					break;
				}
				dbManager.insertAPKData(resourceID, sourceName, ItemType, 
						position, appLabel, pkgName);
			}else{//If only an address is pasted
				Log.d(LOGTAG, "the type seems not correct,it should be an apk~~~");
				//dbManager.insertAPKData(resourceID, sourceName, ItemType, position);
			}
			break;
		//VIDEO 
		case '2':
			//The default eo and tcase is:
			//show the vidhe background name and path is null;
			dbManager.insertVIDEOData(resourceID, sourceName, defalutPath, ItemType, 1, null, null);
			break;
		//TV Info
		case '3':
			resourceUri = getResourceUri();	
			//int needDown = 
			
			if(value >= 1)
			{
				Log.d(LOGTAG, "insertTVData download type");
				dbManager.insertTVData(resourceID, sourceName, ItemType, fullPath,value,resourceUri,
						null,null, position);
			}else{
				dbManager.insertTVData(resourceID, sourceName, ItemType, null, value, 
						resourceUri, null, null, position);
				Log.d(LOGTAG, "insertTVData link type");
			}
			break;
		//PIC Info
		case '4': 
			//the pic need one more "relateID"
			Log.d(LOGTAG, "begin to insert the pic info");
			
			if(relatedReID != null){
				dbManager.insertPICData(resourceID, sourceName, defalutPath, ItemType,  relatedReID);
				relatedReID = null;
			}else{
				Log.d(LOGTAG, "the pic' related id is null, just drop it");
			}
			break;
			
		default:
			//maybe it's picture~~
			if(relatedReID != null && (sourceName.endsWith("png")
					||sourceName.endsWith("jpg")
					||sourceName.endsWith("gif"))){
				
				Log.d(LOGTAG, "begin to insert the pic info~~");
				dbManager.updateBackPic(relatedReID, null, defalutPath, sourceName);
			}
			break;
		}
    }
    
	public void getAPKInfo(){
		Log.d(LOGTAG, "get the apk infomation");
		if(pm == null)
			Log.d(LOGTAG, "the pm id null");
		else{	
		PackageInfo pi = pm.getPackageArchiveInfo(fullPath, PackageManager.GET_ACTIVITIES);
		
		if(pi != null){
			pi.applicationInfo.sourceDir= fullPath;
			pi.applicationInfo.publicSourceDir = fullPath;
			appLabel = (String) pi.applicationInfo.loadLabel(pm);
			pkgName = pi.applicationInfo.packageName;
		}else{
			Log.d(LOGTAG, "the pi is null, can't get the info");
		}
		}
	}
}  