package com.supcoder.opencvandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.socks.library.KLog;
import com.supcoder.opencvandroid.base.BaseActivity;
import com.supcoder.opencvandroid.utils.img.Glide4Engine;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.listener.OnCheckedListener;
import com.zhihu.matisse.listener.OnSelectedListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

/**
 * 图像处理的Activity
 *
 * @author lee
 */
public class BitmapActivity extends BaseActivity {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private String path = "";
    private Uri fileUri;

    private Button chooseBtn, greyBtn, binarizationBtn;

    private ImageView originalImg, processedImg;

    private RxPermissions rxPermissions;


    private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // OpenCV引擎初始化加载成功
                case LoaderCallbackInterface.SUCCESS:
                    KLog.e("OpenCV加载成功");
                    // 连接到Camera
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            path = Matisse.obtainPathResult(data).get(0);
            compressPic();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_bitmap;
    }

    @Override
    public void initParams() {
        rxPermissions = new RxPermissions(this);
    }

    @Override
    public void initView() {
        chooseBtn = findViewById(R.id.chooseBtn);
        greyBtn = findViewById(R.id.greyBtn);
        binarizationBtn = findViewById(R.id.binarizationBtn);
        originalImg = findViewById(R.id.originalImg);
        processedImg = findViewById(R.id.processedImg);
    }

    @Override
    public void bindEvent() {
        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });
        greyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmapGrey(getBitMBitmap(fileUri));
            }
        });

        binarizationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmapBinarization(getBitMBitmap(fileUri));
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
                                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, BitmapActivity.this, mLoaderCallback);
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


    private void compressPic() {
        Luban.compress(this, new File(path))
                .putGear(Luban.CUSTOM_GEAR)
                .setMaxSize(1000)
                .launch(new OnCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(File file) {
                        path = file.getPath();
                        fileUri = Uri.fromFile(file);
                        originalImg.setImageBitmap(getBitMBitmap(fileUri));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void choosePic() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Matisse.from(BitmapActivity.this)
                                    .choose(MimeType.ofAll(), false)
                                    .countable(true)
                                    .capture(true)
                                    .captureStrategy(new CaptureStrategy(true, "com.supcoder.opencvandroid.fileprovider"))
                                    .maxSelectable(1)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                    .thumbnailScale(0.85f)
                                    .imageEngine(new Glide4Engine())
                                    .setOnSelectedListener(new OnSelectedListener() {
                                        @Override
                                        public void onSelected(
                                                @NonNull List<Uri> uriList, @NonNull List<String> pathList) {
                                            // DO SOMETHING IMMEDIATELY HERE
                                            Log.e("onSelected", "onSelected: pathList=" + pathList);

                                        }
                                    })
                                    .originalEnable(true)
                                    .maxOriginalSize(10)
                                    .setOnCheckedListener(new OnCheckedListener() {
                                        @Override
                                        public void onCheck(boolean isChecked) {
                                            // DO SOMETHING IMMEDIATELY HERE
                                            Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                                        }
                                    })
                                    .forResult(REQUEST_CODE_CHOOSE);
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


    private void bitmapBinarization(Bitmap bmp) {
        if (bmp == null) {
            return;
        }
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        //获取lena彩色图像所对应的像素数据
        Utils.bitmapToMat(bmp, rgbMat);
        //将彩色图像数据转换为灰度图像数据并存储到grayMat中
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //创建一个灰度图像
        Bitmap grayBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.RGB_565);
        //将矩阵grayMat转换为灰度图像
        Utils.matToBitmap(grayMat, grayBmp);
        processedImg.setImageBitmap(grayBmp);
    }

    private void bitmapGrey(Bitmap bmp) {
        if (bmp == null) {
            return;
        }
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        //获取lena彩色图像所对应的像素数据
        Utils.bitmapToMat(bmp, rgbMat);
        //将彩色图像数据转换为灰度图像数据并存储到grayMat中
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //创建一个灰度图像
        Bitmap grayBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.RGB_565);
        //将矩阵grayMat转换为灰度图像
        Utils.matToBitmap(grayMat, grayBmp);
        processedImg.setImageBitmap(grayBmp);
    }



    /**
     * 根据路径 转bitmap
     *
     * @param urlpath
     * @return
     */
    public Bitmap getBitMBitmap(Uri urlpath) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), urlpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


}
