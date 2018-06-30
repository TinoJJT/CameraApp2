package com.example.tino.cameraapp2;

import android.support.v4.app.Fragment;
import android.util.Log;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}