package com.mtkj.cnpc.zxing.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.mtkj.cnpc.R;
import com.mtkj.cnpc.zxing.decoding.CaptureActivityHandler;
import com.mtkj.cnpc.zxing.decoding.InactivityTimer;
import com.mtkj.cnpc.zxing.decoding.RGBLuminanceSource;
import com.mtkj.cnpc.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

@SuppressLint("NewApi")
public class MipcaActivityCapture extends Activity implements Callback, View.OnClickListener{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	
	private static final int REQUEST_CODE = 100;
	private static final int PARSE_BARCODE_SUC = 300;
	private static final int PARSE_BARCODE_FAIL = 303;
	private ProgressDialog mProgress;
	private String photo_path;
	private Bitmap scanBitmap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		ImageButton mImageButton = (ImageButton) findViewById(R.id.button_function);
		mImageButton.setOnClickListener(this);
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button_function:
			//????????????
			
			Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
			if (Build.VERSION.SDK_INT < 19) {
				innerIntent.setAction(Intent.ACTION_GET_CONTENT);
			} else {
				innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			}
	        innerIntent.setType("image/*");
	        Intent wrapperIntent = Intent.createChooser(innerIntent, "?????????????????????");
	        this.startActivityForResult(wrapperIntent, REQUEST_CODE);
			break;
		}
	}
	
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			mProgress.dismiss();
			switch (msg.what) {
			case PARSE_BARCODE_SUC:
				onResultHandler((String)msg.obj, scanBitmap);
				break;
			case PARSE_BARCODE_FAIL:
				Toast.makeText(MipcaActivityCapture.this, (String)msg.obj, Toast.LENGTH_LONG).show();
				break;

			}
		}
		
	};
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case REQUEST_CODE:
				if (VERSION.SDK_INT<19) {
					//???????????????????????????
					Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
					if (cursor.moveToFirst()) {
						photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
					}
					cursor.close();
				}else {
					if(DocumentsContract.isDocumentUri(this,data.getData() )){
					    String wholeID = DocumentsContract.getDocumentId(data.getData());
					    String id = wholeID.split(":")[1];
					    String[] column = { MediaStore.Images.Media.DATA };
					    String sel = MediaStore.Images.Media._ID+"=?";
					    Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
					            sel, new String[] { id }, null);
					    int columnIndex = cursor.getColumnIndex(column[0]);
					    if (cursor.moveToFirst()) {
					    	photo_path = cursor.getString(columnIndex);
					    }
					    cursor.close();
					}
					else{
					    String[] projection = { MediaStore.Images.Media.DATA };
					    Cursor cursor = this.getContentResolver().query(data.getData(), projection, null, null, null);
					    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					    cursor.moveToFirst();
					    photo_path = cursor.getString(column_index);
					}
				}

				

				mProgress = new ProgressDialog(MipcaActivityCapture.this);
				mProgress.setMessage("????????????...");
				mProgress.setCancelable(false);
				mProgress.show();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						Result result = scanningImage(photo_path);
						if (result != null) {
							Message m = mHandler.obtainMessage();
							m.what = PARSE_BARCODE_SUC;
							m.obj = result.getText();
							mHandler.sendMessage(m);
						} else {
							Message m = mHandler.obtainMessage();
							m.what = PARSE_BARCODE_FAIL;
							m.obj = "?????????????????????????????????!";
							mHandler.sendMessage(m);
						}
					}
				}).start();
				
				break;
			
			}
		}
	}
	
	/**
	 * ??????????????????????????????
	 * @param path
	 * @return
	 */
	public Result scanningImage(String path) {
		if(TextUtils.isEmpty(path)){
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //??????????????????????????????

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // ??????????????????
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // ??????????????????
		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	/**
	 * ??????????????????
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		onResultHandler(resultString, barcode);
	}
	
	/**
	 * ????????????????????????
	 * @param resultString
	 * @param bitmap
	 */
	private void onResultHandler(String resultString, Bitmap bitmap){
		if(TextUtils.isEmpty(resultString)){
			Toast.makeText(MipcaActivityCapture.this, "?????????????????????????????????!", Toast.LENGTH_SHORT).show();
			return;
		}
		
//		Toast.makeText(MipcaActivityCapture.this, resultString, Toast.LENGTH_SHORT).show();
		Log.e("resultString", "resultString: "+resultString);
		Intent resultIntent = new Intent();
		resultIntent.putExtra("resultString", resultString);
		setResult(RESULT_OK, resultIntent);
		MipcaActivityCapture.this.finish();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.qrcode);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		} 
	}

	/**
	 * When the beep has finished playing, rewind to queue up_s another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};


}