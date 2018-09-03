package com.supcoder.opencvandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.socks.library.KLog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * openCV实现相机
 *
 * @author lee
 */
public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;


    JavaCameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        initView();
        bindEvent();
    }

    private void initView() {
        cameraView = findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
    }

    private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // OpenCV引擎初始化加载成功
                case LoaderCallbackInterface.SUCCESS:
                    KLog.e("OpenCV加载成功");
                    // 连接到Camera
                    cameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void bindEvent() {
        // 注册Camera连接状态事件监听器
        cameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                KLog.e("onCameraViewStarted width -> " + width + "\nheight -> " + height);
            }

            @Override
            public void onCameraViewStopped() {
                KLog.e("onCameraViewStopped");
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                return inputFrame.gray();
            }
        });
    }


    /**
     * OpenCVLoader.initDebug()静态加载OpenCV库
     * OpenCVLoader.initAsync()为动态加载OpenCV库，即需要安装OpenCV Manager
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (!OpenCVLoader.initDebug()) {
                KLog.e("静态加载OpenCV库失败，尝试动态加载！");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
            } else {
                KLog.e("找到了OpenCV的库，开始使用！");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            KLog.e("请求相机权限");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 断开与Camera的连接
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 断开与Camera的连接
        if (cameraView != null) {
            cameraView.disableView();
        }
    }
}
