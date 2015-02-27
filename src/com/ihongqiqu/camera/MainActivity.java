package com.ihongqiqu.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.*;

public class MainActivity extends Activity implements Camera.PictureCallback, Camera.ShutterCallback{

    private CameraSurfaceView cameraSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
        Log.d("CameraSurfaceView", "CameraSurfaceView onCreate currentThread : " + Thread.currentThread());
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
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        File file = Utils.getDiskCacheDir(this, "bitmap");
        if (!file.exists()) {
            file.mkdirs();
        }
        File f = new File(file, "picture.jpg");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShutter() {
        Log.d("CameraSurfaceView", "CameraSurfaceView onShutter");
    }
}
