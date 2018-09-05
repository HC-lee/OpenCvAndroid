package com.supcoder.opencvandroid;

import android.content.Intent;
import android.view.View;

import com.supcoder.opencvandroid.base.BaseActivity;

/**
 * @author lee
 */
public class MainActivity extends BaseActivity {


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initParams() {

    }


    @Override
    public void initView() {

    }

    @Override
    public void bindEvent() {
        findViewById(R.id.cameraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ocrRecognizeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, OcrActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.bitmapBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, BitmapActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.ocrCameraBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, OcrCameraActivity.class);
                startActivity(intent);
            }
        });
    }


}



