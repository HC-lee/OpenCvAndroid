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
import com.supcoder.opencvandroid.base.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * openCV实现相机
 *
 * @author lee
 */
public class CameraActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private JavaCameraView cameraView;

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

    @Override
    public void beforeSetViewInit() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    public void initParams() {

    }

    @Override
    public void initView() {
        cameraView = findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
    }


    @Override
    public void bindEvent() {
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


    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
    }

    private void initPermission(){
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean){
                            //OpenCVLoader.initDebug()静态加载OpenCV库
                            if (!OpenCVLoader.initDebug()) {
                                KLog.e("静态加载OpenCV库失败，尝试动态加载！");
                                //OpenCVLoader.initAsync()为动态加载OpenCV库，即需要安装OpenCV Manager
                                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, CameraActivity.this, mLoaderCallback);
                            } else {
                                KLog.e("找到了OpenCV的库，开始使用！");
                                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                            }
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
