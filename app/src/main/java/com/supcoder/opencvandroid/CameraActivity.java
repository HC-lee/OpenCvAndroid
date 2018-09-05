package com.supcoder.opencvandroid;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.socks.library.KLog;
import com.supcoder.opencvandroid.base.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * openCV实现相机
 *
 * @author lee
 */
public class CameraActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private JavaCameraView cameraView;

    private RadioGroup directionRadioGroup, processRadioGroup;

    private CheckBox colorCheckBox;

    private int processType = 0;

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

        colorCheckBox = findViewById(R.id.colorCheckBox);
        directionRadioGroup = findViewById(R.id.directionRadioGroup);
        processRadioGroup = findViewById(R.id.processRadioGroup);


        directionRadioGroup.check(R.id.rearRadio);

        colorCheckBox.setChecked(true);

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
                Mat frame = new Mat();
                if (colorCheckBox.isChecked()) {
                    frame = inputFrame.rgba();
                } else {
                    frame = inputFrame.gray();
                }
                process(frame);
                if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
                }
                return frame;
            }
        });


        directionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.preRadio:
                        cameraView.setCameraIndex(1);
                        break;
                    case R.id.rearRadio:
                        cameraView.setCameraIndex(0);
                        break;
                    default:
                        break;
                }
                if (cameraView != null) {
                    cameraView.disableView();
                }
                cameraView.enableView();
            }
        });

        processRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.invertRadio:
                        processType = 1;
                        break;
                    case R.id.edgeRadio:
                        processType = 2;
                        break;
                    case R.id.sobelRadio:
                        processType = 3;
                        break;
                    case R.id.blurRadio:
                        processType = 4;
                        break;
                    default:
                        processType = 0;
                        break;
                }
            }
        });

        colorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                colorCheckBox.setText(b?"彩色":"灰色");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
    }

    private void initPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
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


    private void process(Mat frame) {
        switch (processType) {
            case 0:
                break;
            case 1:
                Core.bitwise_not(frame, frame);
                break;
            case 2:
                Mat edges = new Mat();
                Imgproc.Canny(frame, edges, 100, 200, 3, false);
                Mat result = Mat.zeros(frame.size(), frame.type());
                frame.copyTo(result, edges);
                result.copyTo(frame);
                edges.release();
                result.release();
                break;
            case 3:
                Mat gradx = new Mat();
                Imgproc.Sobel(frame, gradx, CvType.CV_32F, 1, 0);
                Core.convertScaleAbs(gradx, gradx);
                gradx.copyTo(frame);
                gradx.release();
                break;
            case 4:
                Mat temp = new Mat();
                Imgproc.blur(frame, temp, new Size(15, 15));
                temp.copyTo(frame);
                temp.release();
                break;

            default:
                break;
        }
    }
}
