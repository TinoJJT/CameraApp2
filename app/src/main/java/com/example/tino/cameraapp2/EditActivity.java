package com.example.tino.cameraapp2;

import android.support.v4.app.Fragment;

public class EditActivity  extends SingleFragmentActivity {



    @Override
    protected Fragment createFragment() {
        return new EditFragment();
    }
}