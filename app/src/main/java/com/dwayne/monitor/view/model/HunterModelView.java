package com.dwayne.monitor.view.model;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.dwayne.monitor.R;

public class HunterModelView extends ConstraintLayout {
    public HunterModelView(Context context) {
        super(context);
    }

    public HunterModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.hunter_model_view,this);
    }

    public HunterModelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
