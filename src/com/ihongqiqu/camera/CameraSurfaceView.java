package com.ihongqiqu.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * 自定义相机
 *
 * Created by zhenguo on 2/27/15.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback{

    public static final int ZOOM_FACTOR = 5;

    private SurfaceHolder surfaceHolder;
    private Camera camera;

    private int zoomValue = 0;

    private boolean safeToTakePicture = false;

    Camera.Size mPreviewSize;
    List<Camera.Size> mSupportedPreviewSizes;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * 拍照
     * @param shutter
     * @param raw
     * @param jpeg
     */
    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw,
                            Camera.PictureCallback jpeg) {
        if (camera != null) {
            if (safeToTakePicture) {
                camera.takePicture(shutter, raw, jpeg);
                safeToTakePicture = false;
            }
        }
    }

    /**
     * 缩小
     */
    public void zoomDown() {
        if (camera == null) {
            return;
        }
        Camera.Parameters p = camera.getParameters();
        if (!p.isZoomSupported()) return;

        if (zoomValue > 0) {
            zoomValue--;
        } else {
            zoomValue = 0;
            return;
        }
        int value = (int) (1F * zoomValue / ZOOM_FACTOR * p.getMaxZoom());
        p.setZoom(value);
        camera.setParameters(p);
    }

    /**
     * 放大
     */
    public void zoomUp() {
        if (camera == null) {
            return;
        }
        Camera.Parameters p = camera.getParameters();
        if (!p.isZoomSupported()) return;

        if (zoomValue < ZOOM_FACTOR) {
            zoomValue++;
        } else {
            zoomValue = ZOOM_FACTOR;
            return;
        }
        int value = (int) (1F * zoomValue / ZOOM_FACTOR * p.getMaxZoom());
        p.setZoom(value);
        camera.setParameters(p);
    }

    /**
     * 开关闪光灯
     */
    public void toggleTorch() {
        if (camera == null) {
            return;
        }
        Camera.Parameters p = camera.getParameters();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(p.getFlashMode())) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
        } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(p.getFlashMode())) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
        } else {
            Toast.makeText(getContext(), "Flash mode setting is not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    public Camera.Size getPreviewSize() {
        if (camera != null) {
            Camera.Parameters p = camera.getParameters();
            List<Camera.Size> sizes =  p.getSupportedPictureSizes();
            if (sizes != null && sizes.size() > 0) {
                return sizes.get(0);
            }
        }
        return null;
    }

    /**
     * 设置可以拍照
     */
    public void setSafeToTakePicture() {
        safeToTakePicture = true;
    }

    /**
     * 重新开启预览
     */
    public void restartPreview() {
        if (camera != null) {
            preview();
        }
    }

    private void preview() {
        Camera.Parameters params = camera.getParameters();
        params.setPictureFormat(ImageFormat.JPEG);
        // 这里面的参数只能是几个特定的参数，否则会报错.(176*144,320*240,352*288,480*360,640*480)
        // params.setPreviewSize(640, 480);
        camera.setParameters(params);
        /*Log.d("CameraSurfaceView", "CameraSurfaceView width - height : " + getWidth() + " - " + getHeight());
        Camera.Size size;// = getOptimalPreviewSize(params.getSupportedPreviewSizes(), getWidth(), getHeight());
        size = getPreviewSize();
        if (size != null) {
            Log.d("CameraSurfaceView", "CameraSurfaceView size width - height : " + size.width + " - " + size.height);
            int w = size.width;
            int h = size.height;
            params.setPreviewSize(w, h);
            // params.setPictureSize(size.width, size.height);
        }*/

        if (mPreviewSize != null) {
            Log.d("CameraSurfaceView", "CameraSurfaceView mPreviewSize width - height : " + mPreviewSize.width + " - " + mPreviewSize.height);
            Log.d("CameraSurfaceView", "CameraSurfaceView view         width - height : " + getWidth() + " - " + getHeight());
            // params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            params.setPreviewSize(mPreviewSize.height, mPreviewSize.width);
            requestLayout();
        }

        camera.startPreview();
        camera.autoFocus(this);
        Log.d("CameraSurfaceView", "CameraSurfaceView zoom : " + params.getZoom());
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (camera == null) {
            try {
                camera = Camera.open();
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(90);

                mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("Error", "相机异常");
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        //设置参数并开始预览
        if (camera != null) {
            // stop preview before making changes
            try {
                camera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            Log.d("CameraSurfaceView", "CameraSurfaceView surfaceChanged width - height : " + width + " - " + height);

            preview();
        }
        safeToTakePicture = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

}
