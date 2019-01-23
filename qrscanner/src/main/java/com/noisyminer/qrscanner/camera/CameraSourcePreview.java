/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noisyminer.qrscanner.camera;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.RequiresPermission;
import com.google.android.gms.common.images.Size;

import java.io.IOException;

public class CameraSourcePreview extends SurfaceView {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        getHolder().addCallback(new SurfaceCallback());
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) {
        try {
            if (cameraSource == null) {
                stop();
            }

            mCameraSource = cameraSource;

            if (mCameraSource != null) {
                mStartRequested = true;
                startIfReady();
            }
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "onStart : " + e);
        }
    }

    public void stop() {
        try {
            if (mCameraSource != null) {
                mCameraSource.stop();
            }
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "onStop : " + e);
        }
    }

    public void release() {
        try {
            if (mCameraSource != null) {
                mCameraSource.release();
                mCameraSource = null;
            }
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "onRelease : " + e);
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException, SecurityException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(getHolder());
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Log.e(TAG,"Do not have permission to start the camera", se);
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera source.", e);
            } catch (Exception e) {
                Log.e(TAG, "Exception on Surface created $e");
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            if (mCameraSource == null) return;
            if (mCameraSource.mCamera == null) return;

            CameraSource.SizePair size = CameraSource.selectSizePair(mCameraSource.mCamera, width, height);
            Size p = size.previewSize();


            if (p.getWidth() >= height && p.getHeight() >= width) {
                setMeasuredDimension(p.getHeight(), p.getWidth());
                return;
            }

            float ratio = (p.getHeight() >= p.getWidth()) ? (float) p.getHeight() / p.getWidth() : (float) p.getWidth() / p.getHeight();

            float camHeight = width * ratio;
            float newCamHeight = camHeight;
            float newHeightRatio = 1f;
            float newWidth = (float) width;

            if (camHeight < height) {
                while (newCamHeight < height) {
                    newHeightRatio = (float) height / p.getWidth();
                    newCamHeight *= newHeightRatio;
                    newWidth *= newHeightRatio;
                }
                setMeasuredDimension((int) newWidth, (int) newCamHeight);
            } else {
                setMeasuredDimension(width, (int) newCamHeight);
            }
        } catch (Exception e) {
            Log.d(getClass().getSimpleName(), "onMeasure : " + e);
        }
    }
}
