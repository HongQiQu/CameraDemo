package com.ihongqiqu.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

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
            camera.takePicture(shutter, raw, jpeg);
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
        camera.startPreview();
        camera.autoFocus(this);
        Log.d("CameraSurfaceView", "CameraSurfaceView zoom : " + params.getZoom());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (camera == null) {
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(90);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //设置参数并开始预览
        if (camera != null) {
            preview();
        }
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
}
