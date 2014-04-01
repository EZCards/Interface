package com.smartmobilesofware.ocrapiservice;

import com.smartmobilesofware.ocrapiservice.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SampleActivity extends Activity implements OnClickListener {
	private final int RESPONSE_OK = 200;
	private final int IMAGE_PICKER_REQUEST = 1;
	
	private TextView picNameText;
	//private EditText langCodeField;
	//private EditText apiKeyFiled;
	
	private String apiKey= "2zxVjtp7TN";
	private String langCode= "en";
	private String fileName;


    private ImageView imgPreview;
    private Button btnCapturePicture;
    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final String tag = "myMainTag";
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images
    private static final String IMAGE_DIRECTORY_NAME = "EZ Cards";
    private Uri fileUri; // file url to store image



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		picNameText = (TextView) findViewById(R.id.imageName);
		//langCodeField = (EditText) findViewById(R.id.lanuageCode);
		//apiKeyFiled = (EditText) findViewById(R.id.apiKey);
        imgPreview = (ImageView) findViewById(R.id.imageView);
        btnCapturePicture = (Button) findViewById(R.id.btnCapture);

        /**
         * Capture image button click event
         */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });

        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }

        /*Not using this select picture method.
		final Button pickButton = (Button) findViewById(R.id.picImagebutton);
		pickButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Starting image picker activity
				startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), IMAGE_PICKER_REQUEST);
			}
		});
        */
		final Button convertButton = (Button) findViewById(R.id.convert);
		convertButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {


		//This is concert to text button



       // imgPreview = (ImageView) findViewById(R.id.imageView);
      //  btnCapturePicture = (Button) findViewById(R.id.btnCapture);

		// Checking are all fields set
        fileName = fileUri.getPath();

		if (fileName != null && !apiKey.equals("") && !langCode.equals("")) {
			final ProgressDialog dialog = ProgressDialog.show( SampleActivity.this, "Loading ...", "Converting to text.", true, false);
			final Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					final OCRServiceAPI apiClient = new OCRServiceAPI(apiKey);
					apiClient.convertToText(langCode, fileName);
					
					// Doing UI related code in UI thread
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
							
							// Showing response dialog
							final AlertDialog.Builder alert = new AlertDialog.Builder(SampleActivity.this);
							alert.setMessage(apiClient.getResponseText());
							alert.setPositiveButton(
								"OK",
								new DialogInterface.OnClickListener() {
									public void onClick( DialogInterface dialog, int id) {
									}
								});
							
							// Setting dialog title related from response code
							if (apiClient.getResponseCode() == RESPONSE_OK) {
								alert.setTitle("Success");
							} else {
								alert.setTitle("Faild");
							}
							
							alert.show();
						}
					});
				}
			});
			thread.start();
		} else {
			Toast.makeText(SampleActivity.this, "All data are required.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK) {

            //fileName must be set to the captured image file!!

            fileName = fileUri.getPath();

			//fileName = getRealPathFromURI(data.getData());
			picNameText.setText("Selected: en" + getStringNameFromRealPath(fileName));
		}

        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }


        /*  */

	}

	/*
	 * Returns image real path.
	 */
	private String getRealPathFromURI(final Uri contentUri) {
		final String[] proj = { MediaStore.Images.Media.DATA };
		final Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		
		return cursor.getString(column_index);
	}

	/*
	 * Cuts selected file name from real path to show in screen.
	 */
	private String getStringNameFromRealPath(final String bucketName) {
		return bucketName.lastIndexOf('/') > 0 ? bucketName.substring(bucketName.lastIndexOf('/') + 1) : bucketName;
	}


    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);

        /*This is where the request should be made to OCRServiceAPI*/









        Log.i(tag, "onSaveInstanceState!");
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    private void previewCapturedImage() {
        try {


            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }else {
            return null;
        }

        return mediaFile;
    }
}