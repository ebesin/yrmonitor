package com.dwayne.monitor.view.model;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.dwayne.monitor.R;

public class OldBunkerModelView extends ConstraintLayout {
    public OldBunkerModelView(Context context) {
        super(context);
    }

    public OldBunkerModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.old_bunker_model_view,this);
    }

    public OldBunkerModelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
