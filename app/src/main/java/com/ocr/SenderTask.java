package com.ocr;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class SenderTask extends AsyncTask<String, String, String> {
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int WAIT_RESPONSE_TIMEOUT = 10000;
    private static String TAG = SenderTask.class.getSimpleName();
    private File mFile;
    private AsyncResponse delegate = null;

    interface AsyncResponse {
        void processFinish(String output, String line, BufferedWriter writer) throws IOException;
    }

    SenderTask(AsyncResponse response) {
        delegate = response;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";

        List<HttpPostWrapper> httpPostList;
        HttpClient httpclient = new DefaultHttpClient();
        HttpParams httpParameters = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, WAIT_RESPONSE_TIMEOUT);
        HttpConnectionParams.setTcpNoDelay(httpParameters, true);

        String fileName = "vehicle_manager.csv";
        String path = Environment.getExternalStorageDirectory() + File.separator + "vehicle_manager"
                + File.separator + fileName;

        mFile = new File(path);

        try {
            httpPostList = fetchDataFromFileToHttpResponce(mFile);
            if(mFile.delete())
                if(mFile.createNewFile())
                    Log.d("FILE ", path + " is recreated");
            OutputStream outputStream = new FileOutputStream(mFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.newLine();
            writer.write(ActivityVehicleManager._strCol);
            writer.flush();
            for (HttpPostWrapper post : httpPostList) {
                HttpResponse response = httpclient.execute(post);
                InputStream inputStream = response.getEntity().getContent();
                delegate.processFinish(convertStreamToString(inputStream),
                        post.getLine(), writer);
                Log.d(TAG, result);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<HttpPostWrapper> fetchDataFromFileToHttpResponce(File file) throws IOException {
        List<HttpPostWrapper> result = new ArrayList<HttpPostWrapper>();

        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line;
        if (reader.readLine() != null)  //Skip file header
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",");
                if(!rowData[0].equals("Vehicle ID")) {
                    HttpPostWrapper httpPost = new HttpPostWrapper("http://www.busimanager.com/websevices/sams/addVehicle.php");
                    httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
                    httpPost.setEntity(new UrlEncodedFormEntity(
                            bindValuePair(rowData[0],
                                    rowData[1],
                                    rowData[2],
                                    rowData[3],
                                    rowData[4],
                                    rowData[5],
                                    rowData[6],
                                    rowData[7],
                                    rowData[8],
                                    rowData[9],
                                    rowData[10],
                                    rowData[11],
                                    rowData[12],
                                    rowData[13],
                                    rowData[14],
                                    rowData[15])));
                    httpPost.setLine(line);
                    result.add(httpPost);
                }
            }
        reader.close();
        fileInputStream.close();

        return result;
    }


    private List<NameValuePair> bindValuePair(String vehicleID, String vehicleTime, String oil,
                                              String coolant, String def,
                                              String fuel, String fuelTime,
                                              String defects, String diff,
                                              String mileage, String date,
                                              String name, String employeeId,
                                              String facility, String serviceLine,
                                              String androidID) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(10);
        result.add(new BasicNameValuePair("vehicle_id", vehicleID));
        result.add(new BasicNameValuePair("vehicle_time", vehicleTime));
        result.add(new BasicNameValuePair("oil", oil));
        result.add(new BasicNameValuePair("coolant", coolant));
        result.add(new BasicNameValuePair("def", def));
        result.add(new BasicNameValuePair("fuel", fuel));
        result.add(new BasicNameValuePair("fuel_time", fuelTime));
        result.add(new BasicNameValuePair("defects", defects));
        result.add(new BasicNameValuePair("TimeDiffrence", diff));
        result.add(new BasicNameValuePair("mileage", mileage));
        result.add(new BasicNameValuePair("date", date));
        result.add(new BasicNameValuePair("name", name));
        result.add(new BasicNameValuePair("employee_id", employeeId));
        result.add(new BasicNameValuePair("facility", facility));
        result.add(new BasicNameValuePair("service_line", serviceLine));
        result.add(new BasicNameValuePair("android_id", androidID));
        return result;
    }

    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    class HttpPostWrapper extends HttpPost {
        private String line;

        HttpPostWrapper(String uri) {
            super(uri);
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }
    }
}