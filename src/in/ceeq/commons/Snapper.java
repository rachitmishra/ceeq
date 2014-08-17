package in.ceeq.commons;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Snapper implements SurfaceHolder.Callback {
		
		private SurfaceHolder surfaceHolder;
		private Camera camera;
		private Context ctx;
		
		void TakePicture(Context ctx, SurfaceView surfaceView){
			surfaceHolder = surfaceView.getHolder();
			surfaceHolder.addCallback(this);
			this.ctx = ctx;
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			Parameters parameters = camera.getParameters();
			parameters.setRotation(270);
			camera.setParameters(parameters);
			camera.startPreview();
			camera.takePicture(null, null, Utils.getJpegCallback(ctx));
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

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

	}