package in.ceeq.helpers;

import in.ceeq.exceptions.ExternalStorageNotFoundException;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraHelper {
	private SurfaceHolder sHolder;
	private final SurfaceView sv;
	private Camera camera;
	private Parameters parameters;
	private FilesHelper fm;

	public CameraHelper(SurfaceView sv, Context c) {
		this.sv = sv;
		fm = new FilesHelper(c);
	}

	class TakePicture implements SurfaceHolder.Callback {

		{
			sHolder = sv.getHolder();
			sHolder.addCallback(this);
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {

			parameters = camera.getParameters();
			parameters.setRotation(270);
			camera.setParameters(parameters);
			camera.startPreview();
			camera.takePicture(null, null, getJpegCallback());
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			camera = getFrontCamera();
			try {
				camera.setPreviewDisplay(holder);

			} catch (IOException exception) {
				camera.release();
				camera = null;
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

	}

	public static boolean hasFrontCamera() {

		if (Camera.getNumberOfCameras() >= 2)
			return true;
		else
			return false;
	}

	public Camera getFrontCamera() {

		if (Camera.getNumberOfCameras() >= 2) {
			try {
				camera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
				return camera;
			} catch (RuntimeException e) {
				Log.e("Ceeq Developer", "Camera not Found !");
			}
		}
		return null;
	}

	private PictureCallback getJpegCallback() {

		PictureCallback jpeg = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				FileOutputStream fos;
				try {
					try {
						fos = new FileOutputStream(fm.createFile(
								FilesHelper.CAM_PATH, "cam"));
						fos.write(data);
						fos.close();
					} catch (ExternalStorageNotFoundException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		return jpeg;
	}

	public TakePicture takepicture() {
		return new TakePicture();
	}
}
