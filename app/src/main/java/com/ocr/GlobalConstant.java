package com.ocr;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class GlobalConstant {
	/** System Common Constants **/

	public final static int TAKE_GALLERY = 51;
	public final static int TAKE_CAMERA = 52;
	
	public final static String HOME_DIR = ".globalconnect";
	public final static String CAMERA_TEMP_FILE_NAME = "camera_temp.jpg";
	
	public static boolean IMAGE_CHANGED = false;
	
	// Directory Paths
	public static String getHomeDirPath(){
		File extStore = Environment.getExternalStorageDirectory();
		File homeStore = new File(String.format("%s/%s", extStore.getPath(), HOME_DIR));
		if (!homeStore.exists()){
			homeStore.mkdir();
		}
		return homeStore.getAbsolutePath();
	}
	
	// Temp File Paths.
	public static String getCameraTempFilePath(){
		return String.format("%s/%s", getHomeDirPath(), CAMERA_TEMP_FILE_NAME);
	}
	
	public static String getRealPathFromURI(Activity activity, Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
		Cursor cursor = activity.managedQuery( contentUri,
                        proj, // Which columns to return
                        null,       // WHERE clause; which rows to return (all rows)
                        null,       // WHERE clause selection arguments (none)
                        null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
	}
	
	@SuppressWarnings("deprecation")
	public synchronized static Bitmap getSafeDecodeBitmap(String strFilePath, int maxSize) {
		try {
			if (strFilePath == null)
				return null;
			// Max image size
			int IMAGE_MAX_SIZE = maxSize;
			
	    	File file = new File(strFilePath);
	    	if (file.exists() == false) {
	    		//DEBUG.SHOW_ERROR(TAG, "[ImageDownloader] SafeDecodeBitmapFile : File does not exist !!");
	    		return null;
	    	}
	    	
	    	BitmapFactory.Options bfo 	= new BitmapFactory.Options();
	    	bfo.inJustDecodeBounds 		= true;
	    	
			BitmapFactory.decodeFile(strFilePath, bfo);
	        
			if (IMAGE_MAX_SIZE > 0) 
		        if(bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE) {
		        	bfo.inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE 
		        						/ (double) Math.max(bfo.outHeight, bfo.outWidth)) / Math.log(0.5)));
		        }
	        bfo.inJustDecodeBounds = false;
	        bfo.inPurgeable = true;
	        bfo.inDither = true;
	        
	        final Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, bfo);
	    	
	        int degree = GetExifOrientation(strFilePath);
	        
	    	return GetRotatedBitmap(bitmap, degree);
		}
		catch(OutOfMemoryError ex)
		{
			ex.printStackTrace();
			
			return null;
		}
	}
	
	public synchronized static int GetExifOrientation(String filepath) 	{
	    int degree = 0;
	    ExifInterface exif = null;
	    
	    try    {
	        exif = new ExifInterface(filepath);
	    } catch (IOException e)  {
	        Log.e("StylePhoto", "cannot read exif");
	        e.printStackTrace();
	    }
	    
	    if (exif != null) {
	        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
	        
	        if (orientation != -1) {
	            // We only recognize a subset of orientation tag values.
	            switch(orientation) {
	                case ExifInterface.ORIENTATION_ROTATE_90:
	                    degree = 90;
	                    break;
	                    
	                case ExifInterface.ORIENTATION_ROTATE_180:
	                    degree = 180;
	                    break;
	                    
	                case ExifInterface.ORIENTATION_ROTATE_270:
	                    degree = 270;
	                    break;
	            }
	        }
	    }
	    
	    return degree;
	}
	
	public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) 	{
	    if ( degrees != 0 && bitmap != null )     {
	        Matrix m = new Matrix();
	        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );
	        try {
	            Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	            if (bitmap != b2) {
	            	bitmap.recycle();
	            	bitmap = b2;
	            }
	        } catch (OutOfMemoryError ex) {
	            // We have no memory to rotate. Return the original bitmap.
	        }
	    }
	    
	    return bitmap;
	}
}