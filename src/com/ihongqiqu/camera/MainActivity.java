package com.ihongqiqu.camera;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    private CameraSurfaceView cameraSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shutter_btn:
                cameraSurfaceView.takePicture();
                break;
            default:

                break;
        }

    }

}
