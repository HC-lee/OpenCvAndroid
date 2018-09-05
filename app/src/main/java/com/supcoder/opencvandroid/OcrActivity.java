package com.supcoder.opencvandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.socks.library.KLog;
import com.supcoder.opencvandroid.base.BaseActivity;
import com.supcoder.opencvandroid.utils.img.Glide4Engine;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.listener.OnCheckedListener;
import com.zhihu.matisse.listener.OnSelectedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

/**
 * @author lee
 */
public class OcrActivity extends BaseActivity {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private TessBaseAPI baseAPI;
    private String path = "";
    private Uri fileUri;

    private Button chooseImgBtn, recognizeBtn;
    private TextView recognizedTv, timeTv;
    private ImageView chooseImg;

    private RxPermissions rxPermissions;


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
        return R.layout.activity_ocr;
    }

    @Override
    public void initParams() {
        rxPermissions = new RxPermissions(this);
        initTessBase();
    }


    @Override
    public void initView() {
        chooseImgBtn = findViewById(R.id.chooseImgBtn);
        recognizeBtn = findViewById(R.id.recognizeBtn);

        recognizedTv = findViewById(R.id.recognizedTv);
        timeTv = findViewById(R.id.timeTv);

        chooseImg = findViewById(R.id.chooseImg);
    }


    private boolean isRecognizing = false;

    @Override
    public void bindEvent() {
        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });

        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecognizing) {
                    isRecognizing = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            recognizeTextImage();
                            isRecognizing = false;
                        }
                    }).start();
                } else {
                    Toast.makeText(OcrActivity.this, "正在识别图片中", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void recognizeTextImage() {
        long startTime = System.currentTimeMillis();
        if (fileUri == null) {
            KLog.e("没有获取到图片路径");
            return;
        }
        Bitmap bmp = BitmapFactory.decodeFile(fileUri.getPath());
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


    private void compressPic() {
        Luban.compress(this, new File(path))
                .putGear(Luban.THIRD_GEAR)
                .launch(new OnCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(File file) {
                        path = file.getPath();
                        fileUri = Uri.fromFile(file);
                        chooseImg.setImageURI(fileUri);
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
                            Matisse.from(OcrActivity.this)
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


    private void initTessBase() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
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
}
