package com.supcoder.opencvandroid.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;


/**
 * Activity基类
 * @author lee
 */
public abstract class BaseActivity extends AppCompatActivity {


    /**
     * 设置contentView之前的一些初始化操作
     */
    public void beforeSetViewInit() {
    }

    /**
     * 设置contentView的Id 如activity无contentView则返回0
     * @return contentView的Id
     */
    public abstract int getLayoutId();

    /**
     * 初始化参数
     */
    public abstract void initParams();

    /**
     * 初始化布局
     */
    public abstract void initView();

    /**
     * 事件绑定
     */
    public abstract void bindEvent();

    /**
     * 加载数据
     */
    public void loadData() {
    }


    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeSetViewInit();
        // 全屏显示
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        }
        initParams();
        initView();
        bindEvent();
        loadData();
    }
}
