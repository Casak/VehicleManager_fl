package com.ocr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.ocr.WelcomeActivity;

/**
 * Activity to recognize image from gallery selected
 */
public class ActivityVehicleManager extends Activity implements View.OnFocusChangeListener{

	private static final String TAG = CaptureActivity.class.getSimpleName();
	
	private EditText mVehicleEditText;
	private EditText mOilEditText;
	private EditText mMileageEditText;
	private EditText mDefEditText;
	private EditText mCoolantEditText;
	private EditText mFuelEditText;
	private String name;
	private String employeeId;
	private String facility;
	private String serviceLine;

	private int[] mNumberButtonIds = {R.id.num0, R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5,
			R.id.num6, R.id.num7, R.id.num8, R.id.num9};
	private String _strCol = "Vehicle ID" + ", " +
			"Vehicle Time" + ", " +
			"OIL" + ", " +
			"COOLANT" + ", " +
			"DEF" + ", " +
			"FUEL" + ", " +
			"FUEL Time" + ", " +
			"DEFECTS" + ", " +
			"Time Diff" + ", " +
			"Mileage" + ", " +
			"Date" + ", " +
			"Name" + ", " +
			"EmployeeID" + ", " +
			"Facility" + ", " +
			"ServiceLine" + ", " +
			"AndroidID" +  "\n";
	private String _strTimeVehilce;
	private String _strTimeFuel;

	private EditText mCurFocusText;
	
	private Date _dateVehicle;
	private Date _dateFuel;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.activity_vehiclemanager);
		mVehicleEditText = (EditText)findViewById(R.id.vehicle_id_edit);
		mVehicleEditText.setOnFocusChangeListener(this);
		try	{
			String strRecogText = getIntent().getExtras().getString("recog_text");
			mVehicleEditText.setText(strRecogText);
			name = getIntent().getExtras().getString("name_edit_text");
			employeeId = getIntent().getExtras().getString("employee_id_edit_text");
			facility = getIntent().getExtras().getString("facility_edit_text");
			serviceLine = getIntent().getExtras().getString("service_line_edit_text");
		}catch(Exception e)	{
			e.printStackTrace();
		}

		mOilEditText = (EditText)findViewById(R.id.oil_edit);
		mMileageEditText = (EditText)findViewById(R.id.mileage_edit);
		mDefEditText = (EditText)findViewById(R.id.def_edit);
		mCoolantEditText = (EditText)findViewById(R.id.coolant_edit);
		mFuelEditText = (EditText)findViewById(R.id.fuel_edit);
		//mOilEditText.setText("0");
		//mDefEditText.setText("0");
		//mCoolantEditText.setText("0");
		//mFuelEditText.setText("0");
		
		mOilEditText.setOnFocusChangeListener(this);
		mMileageEditText.setOnFocusChangeListener(this);
		mDefEditText.setOnFocusChangeListener(this);
		mCoolantEditText.setOnFocusChangeListener(this);
		mFuelEditText.setOnFocusChangeListener(this);

		for(int i = 0; i < mNumberButtonIds.length; i++)
		{
			final int index = i;
			findViewById(mNumberButtonIds[i]).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCurFocusText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0 + index));
				}
			});
		}
		findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurFocusText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			}
		});
		
        //android:entries="@array/defects_array"
        //android:prompt="@string/defects" 
		String[] strArrayDefects = getResources().getStringArray(R.array.defects_array);
		// Create the adapter and set it to the AutoCompleteTextView 

		mVehicleEditText.requestFocus();
		mCurFocusText = mVehicleEditText;
		
		_dateVehicle = Calendar.getInstance().getTime();
		_dateFuel = Calendar.getInstance().getTime();
		
		findViewById(R.id.next_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(ActivityVehicleManager.this, "Next Clicked", Toast.LENGTH_LONG).show();
				if( saveVehicleInformation() == false )
					return;
				
			      Toast toast = Toast.makeText(ActivityVehicleManager.this, "saved the vehicle information.", Toast.LENGTH_SHORT);
			      toast.setGravity(Gravity.CENTER, 0, 0);
			      toast.show();
			      
			    mVehicleEditText.setText("");
				mOilEditText.setText("");
				mMileageEditText.setText("");
				mDefEditText.setText("");
				mCoolantEditText.setText("");
				mFuelEditText.setText("");

				_dateVehicle = Calendar.getInstance().getTime();
				_dateFuel = Calendar.getInstance().getTime();
			}
		});
		
		findViewById(R.id.btn_ocr).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clickButtonOCR();
			}
		});
		

	}

	public void hideKeyboard(View editText) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}


	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus)
		{
			mCurFocusText = (EditText)v;
			if( mCurFocusText.equals(mVehicleEditText)){
				_dateVehicle = Calendar.getInstance().getTime();
			}
			else if(mCurFocusText.equals(mFuelEditText)){
				_dateFuel = Calendar.getInstance().getTime();
			}
			InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
			imm.hideSoftInputFromWindow(mCurFocusText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
			//imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN.SHOW_FORCED, 0);
		}
		hideKeyboard(v);
	}
	
	private void clickButtonOCR(){
		try {
	        Intent intent = new Intent(ActivityVehicleManager.this, CaptureActivity.class);
	        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	         //Show the default page on a clean install, and the what's new page on an upgrade.
		    startActivity(intent);
		    finish();
	
		    } catch (NullPointerException e) {
		      Log.w(TAG, e);
		    }
	}
	
	private boolean saveVehicleInformation()
	{
		String strFolderPathSave = "";
		//File storageDirectory = this.getCacheDir();//getStorageDirectory();
		File storageDirectory = Environment.getExternalStorageDirectory();//.getExternalStorage();//("vehicle_manager", Context.MODE_PRIVATE); //Creating an internal dir;
		if (storageDirectory.toString() == null)
			return false;
		
		strFolderPathSave = storageDirectory.toString() + File.separator + "vehicle_manager";
		File fileSave = new File(strFolderPathSave);
		if(!fileSave.exists())
		{
			fileSave.mkdirs();
		}     
		  
		String strVehicleID = mVehicleEditText.getText().toString();
		if( strVehicleID.equals(""))
		{
			Toast toast = Toast.makeText(ActivityVehicleManager.this, "Please enter the vehicle ID.", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		}

		String strOil = mOilEditText.getText().toString();
		if( strOil.equals(""))
			strOil = "0";
		String strMileage = mMileageEditText.getText().toString();
		if( strOil.equals(""))
			strOil = "0";
		String strCoolant = mCoolantEditText.getText().toString();
		if( strCoolant.equals(""))
			strCoolant = "0";
		String strDef = mDefEditText.getText().toString();
		if( strDef.equals(""))
			strDef = "0";
		String strFuel = mFuelEditText.getText().toString();
		if( strFuel.equals(""))
			strFuel = "0";


		String strDefects = "";

		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		_strTimeVehilce = df.format(_dateVehicle);	
		_strTimeFuel = df.format(_dateFuel);
		long diffMil = _dateFuel.getTime() - _dateVehicle.getTime();
		long diffSec = TimeUnit.MILLISECONDS.toSeconds(diffMil); 
		int diffMinutes = (int)(diffSec / 60);
		int diffSeconds = (int)(diffSec % 60);
		
		String strDiff;
		if( diffMinutes == 0)
			strDiff = "00" + ":" + Long.toString(diffSeconds);
		else
			strDiff = Long.toString(diffMinutes) + ":" + Long.toString(diffSeconds);
		
		//java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
		String strSaveFile = "vehicle_manager.csv";//df.format(Calendar.getInstance().getTime());
		String strSavePath = strFolderPathSave + File.separator + strSaveFile;
		File file = new File(strSavePath);
		if(!file.exists()){
			try{
				file.createNewFile();
				
				FileWriter outWritter = new FileWriter(file);
				outWritter.write(_strCol);
				outWritter.close();
				
		    }catch (IOException e) {
		        e.printStackTrace();
		    }			
		}

		try {
			String strLine = strVehicleID + ", " +
					_strTimeVehilce + ", " +
					strOil + ", " +
					strCoolant + ", " +
					strDef + ", " +
					strFuel + ", " +
					_strTimeFuel + ", " +
					strDefects + ", " +
					strDiff + ", " +
					strMileage + ", " +
					_strTimeVehilce.split(" ")[0] + ", " +
					name + ", " +
					employeeId + ", " +
					facility + ", " +
					serviceLine + ", " +
					Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + "\n";
			  
			FileWriter outWritter = new FileWriter(file, true);
			BufferedWriter bufferWritter = new BufferedWriter(outWritter);
			bufferWritter.write(strLine);
			bufferWritter.close();
	    }catch (IOException e) {
	        e.printStackTrace();
	    }
		  
		return true;
	  }
	
	  /** Finds the proper location on the SD card where we can save files. */
	  private File getStorageDirectory() {
	    //Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));
	    
	    String state = null;
	    try {
	      state = Environment.getExternalStorageState();
	    } catch (RuntimeException e) {
	      showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
	    }
	    
	    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

	      // We can read and write the media
	      //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
	      // For Android 2.2 and above
	      
	      try {
	        return getExternalFilesDir(Environment.MEDIA_MOUNTED);
	      } catch (NullPointerException e) {
	        // We get an error here if the SD card is visible, but full
	        showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
	      }
	      
	      //        } else {
	      //          // For Android 2.1 and below, explicitly give the path as, for example,
	      //          // "/mnt/sdcard/Android/data/eloya.hebrewocr/files/"
	      //          return new File(Environment.getExternalStorageDirectory().toString() + File.separator + 
	      //                  "Android" + File.separator + "data" + File.separator + getPackageName() + 
	      //                  File.separator + "files" + File.separator);
	      //        }
	    
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    	// We can only read the media
	      showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
	    } else {
	    	// Something else is wrong. It may be one of many other states, but all we need
	      // to know is we can neither read nor write
	    	showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable or corrupted.");
	    }
	    return null;
	  }
	  
	  /**
	   * Displays an error message dialog box to the user on the UI thread.
	   * 
	   * @param title The title for the dialog box
	   * @param message The error message to be displayed
	   */
	  void showErrorMessage(String title, String message) {
		  new AlertDialog.Builder(this)
		    .setTitle(title)
		    .setMessage(message)
		    .setOnCancelListener(new FinishListener(this))
		    .setPositiveButton( "Done", new FinishListener(this))
		    .show();
	  }  
	  
 
}