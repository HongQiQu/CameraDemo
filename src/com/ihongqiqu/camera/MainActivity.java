package com.ihongqiqu.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.*;

public class MainActivity extends Activity implements Camera.PictureCallback, Camera.ShutterCallback {

    public static final int FLAG_CHOOCE_PICTURE = 2001;

    private View centerWindowView;
    private int mScreenHeight, mScreenWidth;
    private int viewHeight;

    public static final int ZOOM_FACTOR = 5;
    private int zoomValue = 0;
    private boolean safeToTakePicture = true;

    private CameraPreview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;

    // The first rear facing camera
    int defaultCameraId;

    private ImageView preview_iv;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        preview_iv = (ImageView) findViewById(R.id.preview_iv);
        handler = new Handler();

        centerWindowView = findViewById(R.id.center_window_view);
        Log.d("CameraSurfaceView", "CameraSurfaceView onCreate currentThread : " + Thread.currentThread());
        // 得到屏幕的大小
        WindowManager wManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wManager.getDefaultDisplay();
        mScreenHeight = display.getHeight();
        mScreenWidth = display.getWidth();
        viewHeight = mScreenWidth / 2;
        centerWindowView.getLayoutParams().width = viewHeight;
        centerWindowView.getLayoutParams().height = viewHeight;

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = (CameraPreview) findViewById(R.id.camera_preview);

        // Find the total number of cameras available
        numberOfCameras = Camera.getNumberOfCameras();

        // Find the ID of the default camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shutter_btn:
                takePicture(null, null, this);
                break;
            case R.id.zoom_down_btn:
                zoomDown();
                break;
            case R.id.zoom_up_btn:
                zoomUp();
                break;
            case R.id.torch_switch_btn:
                toggleTorch();
                break;
            case R.id.choose_picture_btn:
                choosePicture();
                break;
            default:

                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the default i.e. the first rear facing camera.
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "启动照相机失败，请检查设备并打开权限", Toast.LENGTH_SHORT).show();
        }
        cameraCurrentlyLocked = defaultCameraId;
        mPreview.setCamera(mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 拍照
     *
     * @param shutter
     * @param raw
     * @param jpeg
     */
    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw,
                            Camera.PictureCallback jpeg) {
        if (mCamera != null) {
            if (safeToTakePicture) {
                mCamera.takePicture(shutter, raw, jpeg);
                safeToTakePicture = false;
            }
        }
    }

    /**
     * 缩小
     */
    public void zoomDown() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters p = mCamera.getParameters();
        if (!p.isZoomSupported()) return;

        if (zoomValue > 0) {
            zoomValue--;
        } else {
            zoomValue = 0;
            return;
        }
        int value = (int) (1F * zoomValue / ZOOM_FACTOR * p.getMaxZoom());
        p.setZoom(value);
        mCamera.setParameters(p);
    }

    /**
     * 放大
     */
    public void zoomUp() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters p = mCamera.getParameters();
        if (!p.isZoomSupported()) return;

        if (zoomValue < ZOOM_FACTOR) {
            zoomValue++;
        } else {
            zoomValue = ZOOM_FACTOR;
            return;
        }
        int value = (int) (1F * zoomValue / ZOOM_FACTOR * p.getMaxZoom());
        p.setZoom(value);
        mCamera.setParameters(p);
    }

    /**
     * 开关闪光灯
     */
    public void toggleTorch() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters p = mCamera.getParameters();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(p.getFlashMode())) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p);
        } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(p.getFlashMode())) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
        } else {
            Toast.makeText(this, "Flash mode setting is not supported.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择图片
     */
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, FLAG_CHOOCE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FLAG_CHOOCE_PICTURE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String imgPath = getUrl(uri);
            Log.d("", "CameraSurfaceView imgPath : " + imgPath);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        // cameraSurfaceView.restartPreview();
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.autoFocus(mPreview);
        }

        if (data == null || data.length <= 0) {
            safeToTakePicture = true;
            return;
        }

        Log.d("CameraSurfaceView", "CameraSurfaceView onPictureTaken data.length : " + data.length);
        Toast.makeText(this, "data.length : " + data.length, Toast.LENGTH_SHORT).show();


        // 保存图片
        final byte[] b = data;
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleAndSaveBitmap(b);
            }
        }).start();

        safeToTakePicture = true;
    }

    @Override
    public void onShutter() {
        Log.d("CameraSurfaceView", "CameraSurfaceView onShutter");
    }

    /**
     * 处理拍照图片并保存
     *
     * @param data
     */
    private synchronized void handleAndSaveBitmap(byte[] data) {
        // 保存图片
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap rightBitmap = Utils.rotate(b, 90);

        Utils.compress(rightBitmap, 2 * 1024 * 1024);

        int cropWidth = (int) (1F * viewHeight / mScreenWidth * (rightBitmap.getWidth() - mPreview.moveX * 2));
        int cropX = rightBitmap.getWidth() / 2 - cropWidth / 2;
        int cropY = rightBitmap.getHeight() / 2 - cropWidth / 2;
        if (rightBitmap.getWidth() < cropWidth + cropX) {
            cropX = rightBitmap.getWidth() - cropWidth;
        }
        if (rightBitmap.getHeight() < cropWidth + cropY) {
            cropY = rightBitmap.getHeight() - cropY;
        }
        Log.d("CameraSurfaceView", "CameraSurfaceView viewWidth   : " + centerWindowView.getWidth());
        Log.d("CameraSurfaceView", "CameraSurfaceView bitmapWidth : " + rightBitmap.getWidth() / 2);

        final Bitmap bmp = Bitmap.createBitmap(rightBitmap, cropX, cropY, cropWidth, cropWidth);

        handler.post(new Runnable() {
            @Override
            public void run() {
                preview_iv.setImageBitmap(bmp);
            }
        });

        // Bitmap bmp = Utils.getCroppedImage(b, centerWindowView);
        File file = Utils.getDiskCacheDir(this, "bitmap");
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file, "picture.jpg");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null && !b.isRecycled()) {
                b.recycle();
                b = null;
            }
            /*if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
                bmp = null;
            }*/
        }
    }

    /**
     * 获取从相册中选择的图片的据对路径
     *
     * @param uri
     * @return
     */
    private String getUrl(Uri uri) {
        if (uri == null) {
            return null;
        }

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        return TextUtils.isEmpty(img_path) ? null : img_path;
    }

}


