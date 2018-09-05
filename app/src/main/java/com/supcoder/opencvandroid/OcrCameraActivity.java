package com.supcoder.opencvandroid;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.socks.library.KLog;
import com.supcoder.opencvandroid.base.BaseActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 实时识别相机中的文字
 *
 * @author lee
 */
public class OcrCameraActivity extends BaseActivity {

    private TessBaseAPI baseAPI;

    private JavaCameraView cameraView;

    private boolean isRecognizing;

    private int mWidth, mHeight;

    private TextView recognizedTv, timeTv;

    private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    cameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    public int getLayoutId() {
        return R.layout.activity_ocr_camera;
    }

    @Override
    public void initParams() {

    }

    @Override
    public void initView() {
        cameraView = findViewById(R.id.cameraView);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        recognizedTv = findViewById(R.id.recognizedTv);
        timeTv = findViewById(R.id.timeTv);
    }

    @Override
    public void bindEvent() {
        cameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                KLog.e("onCameraViewStarted width -> " + width + "\nheight -> " + height);
                mWidth = width;
                mHeight = height;

            }

            @Override
            public void onCameraViewStopped() {
                KLog.e("onCameraViewStopped");
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat frame = inputFrame.rgba();
                if (!isRecognizing) {
                    isRecognizing = true;
                    final Mat finalFrame = frame;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            recognizeTextImage(finalFrame);
                            isRecognizing = false;
                        }
                    }).start();
                }


                return frame;


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();
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

    private void initPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, OcrCameraActivity.this, mLoaderCallback);
                            } else {
                                KLog.e("找到了OpenCV的库，开始使用！");
                                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                            }

                            try {
                                initTessBaseAPI();
                            } catch (IOException e) {
                                e.printStackTrace();
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

    private void initTessBaseAPI() throws IOException {
        baseAPI = new TessBaseAPI();
        String dataPath = Environment.getExternalStorageDirectory() + File.separator + "tesseract" + File.separator;
        File dir = new File(dataPath + "tessdata" + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
            InputStream inputStream = getResources().openRawResource(R.raw.eng);

            File file = new File(dir, "eng.traineddata");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
            inputStream.close();
            outputStream.close();
        }
        boolean isSuccess = baseAPI.init(dataPath, "eng");
        if (isSuccess) {
            KLog.d("初始化ocr成功");
        } else {
            KLog.d("初始化ocr失败");
        }
    }

    private void recognizeTextImage(Mat frame) {
        long startTime = System.currentTimeMillis();
        if (frame == null) {
            KLog.e("没有获取到图片路径");
            return;
        }
        Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bmp);
        baseAPI.setImage(bmp);
        final String recognizedText = baseAPI.getUTF8Text();
        final long timeConsume = System.currentTimeMillis() - startTime;
        if (!TextUtils.isEmpty(recognizedText)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recognizedTv.setText(recognizedText);
                    timeTv.setText("(" + timeConsume + "ms)");
                }
            });
        } else {
            KLog.e("识别失败");
        }
    }


}
