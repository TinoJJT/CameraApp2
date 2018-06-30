package com.example.tino.cameraapp2;

import android.support.v4.app.Fragment;

public class CameraActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new Camera2BasicFragment();
    }
}
