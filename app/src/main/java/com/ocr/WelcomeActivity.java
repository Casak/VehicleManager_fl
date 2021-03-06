package com.ocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WelcomeActivity extends Activity {

	public final static String DIR_TESSDATA = "tessdata";
	public final static String ENGINE_FILE = "eng";
	public final static String RESULT_SUCCESS = "Success!";
	public final static String RESULT_FAIL = "Fail!";

    private EditText mNameEditText;
    private EditText mEmployeeIdEditText;
    private EditText mFacilityEditText;
    private EditText mServiceLineEditText;

    private Context context;

	  private int langSelectIndex = 0;
	  AsyncTask<String, Void, String> copytask;

	  private final Button.OnClickListener startAppListener = new Button.OnClickListener() {
		    @Override
		    public void onClick(View view) {

                String name = mNameEditText.getText().toString();
                String employeeId = mEmployeeIdEditText.getText().toString();
                String facility = mFacilityEditText.getText().toString();
                String serviceLine = mServiceLineEditText.getText().toString();

                if(name.equals("") || employeeId.equals("") || facility.equals("") || serviceLine.equals("")) {
                    Toast.makeText(context, "Please, fill all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
		        //String items[] = {
				//		"From Camera",
				//		"From Gallary"};
        		Intent intent = new Intent(WelcomeActivity.this, ActivityVehicleManager.class);
        		intent.putExtra("recog_text", "");
        		intent.putExtra("name_edit_text", name);
        		intent.putExtra("employee_id_edit_text", employeeId);
        		intent.putExtra("facility_edit_text", facility);
        		intent.putExtra("service_line_edit_text", serviceLine);
		    	//intent = new Intent().setClass(WelcomeActivity.this, PreferencesActivity.class);
		        startActivity(intent);

		        finish();
		    }
		  };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        context = getApplicationContext();
		setContentView(R.layout.welcome);

		createCopyTaskAndExecute();

        mNameEditText = (EditText)findViewById(R.id.name_edit);
        mEmployeeIdEditText = (EditText)findViewById(R.id.employeeId_edit);
        mFacilityEditText = (EditText)findViewById(R.id.facility_edit);
        mServiceLineEditText = (EditText)findViewById(R.id.service_line_edit);

	    // Show an StartApp button.
	    View doneBtnStartApp = findViewById(R.id.btnStartApp);
	    doneBtnStartApp.setOnClickListener(startAppListener);
	}

    private void createCopyTaskAndExecute(){
        copytask = new AsyncTask<String, Void, String> () {
            @Override
            protected String doInBackground(String... params) {

                //Create directory.
                String strDirTessData = getFilesDir().toString() + File.separator + DIR_TESSDATA;
                String strfileTessData = strDirTessData + File.separator + "eng.traineddata";
                File fileTessdataDir = new File(strDirTessData);
				if ( fileTessdataDir.exists() == false ) {
	                //Make "tessdata" directory if not existed.
	                fileTessdataDir.mkdirs();
	            }

                String tempPath = fileTessdataDir.getAbsolutePath() + File.separator + ENGINE_FILE + ".temp";
                String targetPath = fileTessdataDir.getAbsolutePath() + File.separator + ENGINE_FILE + ".traineddata";
                Log.e("==================", targetPath);

                File targetFile = new File(targetPath);
                // If already exists.
                if (targetFile.exists() && targetFile.isFile())
                {
                	Log.e("Log", "the train data file was already existed.");
                    return RESULT_SUCCESS;
                }

                try {
                	Log.e("Log", "Copy Resource(R.raw.eng_traineddata To .traineddata.");
                    // Copy Resource To Target
                    copyResourceToTarget(R.raw.eng_traineddata, tempPath);
                    // Rename file when success.
                    return (new File(tempPath)).renameTo(targetFile) ? RESULT_SUCCESS : RESULT_FAIL;
                }catch(Exception ex){
                    return RESULT_FAIL;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals(RESULT_SUCCESS)){
                    // Initialize Barcode Engine and show capture activity
                    // checkPermissionAndShowTicketCheckActivity();
                    //showTicketCheckActivity();  //Show ticket check activity.
                } else {
                    // show alert dialog and finish.
                    new AlertDialog.Builder(WelcomeActivity.this)
                            .setTitle("Error")
                            .setMessage(R.string.initialize_failed)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    // Finish launch activity.
                                    //LauncherActivity.this.finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        };
        copytask.execute("");
    }

    // Copy Resource to Target.
    private void copyResourceToTarget(int resId, String target) throws Exception{
        InputStream in = getResources().openRawResource(resId);
        FileOutputStream out = new FileOutputStream(target);
        byte[] buff = new byte[10240];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0)
            {
                out.write(buff, 0, read);
            }
        }catch(Exception ex){
            Log.e("Log", "Error while copying file.", ex);
        }finally {
            if (in != null)
            {
                in.close();
            }
            out.close();
        }
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != Activity.RESULT_OK) return;

		String imgPath = "";
		if (requestCode == GlobalConstant.TAKE_GALLERY) {
			Uri imgUri = data.getData();
			imgPath = GlobalConstant.getRealPathFromURI(WelcomeActivity.this, imgUri);
			/*
			Intent intent = new Intent(WelcomeActivity.this, ImageActivity.class);
			intent.putExtra("image_path", imgPath);
			startActivity(intent);*/

		}/* else if (requestCode == GlobalConstant.TAKE_CAMERA) {
			imgPath = GlobalConstant.getCameraTempFilePath();
		}*/

		Log.d(getClass().getName(), "Path:" + imgPath);
	}
}
