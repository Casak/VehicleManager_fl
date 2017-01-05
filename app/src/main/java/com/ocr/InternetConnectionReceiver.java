package com.ocr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created on 05.01.2017.
 */

public class InternetConnectionReceiver extends BroadcastReceiver {
    private static InternetConnectionReceiver instance = null;

    final String successResponse = "[{\"success\":\"1\",\"msg\":\"Record successfully inserted\"}]";
    final String failResponse = "[{\"success\":\"2\",\"msg\":\"Record Not inserted\"}]";

    private boolean needsToUpload = false;
    private Context mContext;

    private InternetConnectionReceiver(Context context){
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(needsToUpload)
            upload();
    }


    public void setNeedToUpload(boolean state){
        if(state && !needsToUpload) {
            needsToUpload = true;
            upload();
        }
    }

    public static InternetConnectionReceiver getInstance(Context context){
        return instance == null ? instance = new InternetConnectionReceiver(context) : instance;
    }

    private boolean checkInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void upload(){
        if(!checkInternetConnection()){
            needsToUpload = true;
            return;
        }
        SenderTask task = new SenderTask(new SenderTask.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                if (output.equals(successResponse)) {
                    setNeedToUpload(false);
                    Log.d("Data inserted", ", service response is: " + output);
                }
                else if (output.equals(failResponse)){
                    setNeedToUpload(true);
                    Log.d("Data not inserted", ", service response is: " + output);
                }
                else{
                    setNeedToUpload(true);
                    Log.d("Inserting data problem" , ", service response is: " + output);
                }
            }
        });
        task.execute();
    }
}
