package com.ihongqiqu.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.io.*;

public class MainActivity extends Activity implements Camera.PictureCallback, Camera.ShutterCallback{

    private CameraSurfaceView cameraSurfaceView;
    private View centerWindowView;

    private int mScreenHeight, mScreenWidth;
    private int viewHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
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
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shutter_btn:
                cameraSurfaceView.takePicture(this, null, this);
                break;
            case R.id.zoom_down_btn:
                cameraSurfaceView.zoomDown();
                break;
            case R.id.zoom_up_btn:
                cameraSurfaceView.zoomUp();
                break;
            case R.id.torch_switch_btn:
                cameraSurfaceView.toggleTorch();
                break;
            default:

                break;
        }

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d("CameraSurfaceView", "CameraSurfaceView onPictureTaken currentThread : " + Thread.currentThread());
        cameraSurfaceView.restartPreview();

        // TODO 保存图片 目前再主线程中进行 需要后台进行
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap rightBitmap = Utils.rotate(b, 90);

        Utils.compress(rightBitmap, 2 * 1024 * 1024);

        Camera.Size size = cameraSurfaceView.getPreviewSize();
        if (size != null) {
            Log.d("CameraSurfaceView", "CameraSurfaceView cameraSize : " + size.width + " - " + size.height);
            Log.d("CameraSurfaceView", "CameraSurfaceView screenSize : " + mScreenWidth + " - " + mScreenHeight);
            Log.d("CameraSurfaceView", "CameraSurfaceView bitmapSize : " + b.getWidth() + " - " + b.getHeight());
            Log.d("CameraSurfaceView", "CameraSurfaceView bitmapSize2 : " + rightBitmap.getWidth() + " - " + rightBitmap.getHeight());
        } else {
            // return;
        }

        int cropWidth = (int) (1F * viewHeight / mScreenWidth * rightBitmap.getWidth());
        int cropX = rightBitmap.getWidth() / 4;
        int cropY = (rightBitmap.getHeight() - cropWidth) / 2;
        if (rightBitmap.getWidth() < cropWidth + cropX) {
            cropX = rightBitmap.getWidth() - cropWidth;
        }
        if (rightBitmap.getHeight() < cropWidth + cropY) {
            cropY = rightBitmap.getHeight() - cropY;
        }
        Log.d("CameraSurfaceView", "CameraSurfaceView viewWidth   : " + centerWindowView.getWidth());
        Log.d("CameraSurfaceView", "CameraSurfaceView bitmapWidth : " + rightBitmap.getWidth() / 2);

        Bitmap bmp = Bitmap.createBitmap(rightBitmap, cropX, cropY, cropWidth, cropWidth);
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
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
                bmp = null;
            }
        }

        cameraSurfaceView.setSafeToTakePicture();
    }

    @Override
    public void onShutter() {
        Log.d("CameraSurfaceView", "CameraSurfaceView onShutter");
    }
}
