package com.jmolsmobile.landscapevideocapture.preview;

import java.io.IOException;

import com.jmolsmobile.landscapevideocapture.CLog;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

/**
 * @author Jeroen Mols
 */
public class CapturePreview implements SurfaceHolder.Callback {

	private boolean							mPreviewRunning	= false;
	private final Camera					mCamera;
	private final CapturePreviewInterface	mInterface;
	private final int						mPreviewWidth;
	private final int						mPreviewHeight;

	public CapturePreview(CapturePreviewInterface capturePreviewInterface, Camera camera, SurfaceHolder holder,
			int width, int height) {
		mInterface = capturePreviewInterface;
		mCamera = camera;
		mPreviewWidth = width;
		mPreviewHeight = height;

		initalizeSurfaceHolder(holder);
	}

	private void initalizeSurfaceHolder(final SurfaceHolder surfaceHolder) {
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Necessary for older API's
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		// NOP
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		final Camera.Parameters params = mCamera.getParameters();
		params.setPreviewSize(mPreviewWidth, mPreviewHeight);
		params.setPreviewFormat(ImageFormat.NV21);

		try {
			setCameraParameters(params);
		} catch (final RuntimeException e) {
			e.printStackTrace();
			CLog.d(CLog.PREVIEW, "Failed to show preview - invalid parameters set to camera preview");
			mInterface.onCapturePreviewFailed();
			return;
		}

		try {
			startPreview(holder);
		} catch (final IOException e) {
			e.printStackTrace();
			CLog.d(CLog.PREVIEW, "Failed to show preview - unable to connect camera to preview (IOException)");
			mInterface.onCapturePreviewFailed();
		} catch (final RuntimeException e) {
			e.printStackTrace();
			CLog.d(CLog.PREVIEW, "Failed to show preview - unable to start camera preview (RuntimeException)");
			mInterface.onCapturePreviewFailed();
		}
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		// NOP
	}

	public void releasePreviewResources() {
		if (mPreviewRunning) {
			try {
				stopPreview();
				setPreviewRunning(false);
			} catch (final Exception e) {
				e.printStackTrace();
				CLog.e(CLog.PREVIEW, "Failed to clean up preview resources");
			}
		}
	}

	protected void setCameraParameters(final Camera.Parameters params) {
		mCamera.setParameters(params);
	}

	protected void startPreview(final SurfaceHolder holder) throws IOException {
		mCamera.setPreviewDisplay(holder);
		mCamera.startPreview();
		setPreviewRunning(true);
	}

	protected void setPreviewRunning(boolean running) {
		mPreviewRunning = running;
	}

	protected void stopPreview() throws Exception {
		mCamera.stopPreview();
		mCamera.setPreviewCallback(null);
	}

}