package com.jiyouliang.monitor.view.map;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.jiyouliang.monitor.R;

/**
 * 地图上Supend容器
 */
public class SupendViewContainer2 extends ConstraintLayout {
    public SupendViewContainer2(Context context) {
        this(context, null);
    }

    public SupendViewContainer2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SupendViewContainer2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_supend_container2, this, true);
    }
}
