package com.dwayne.monitor.view.model;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.dwayne.monitor.R;

public class NewBunkerModelView extends ConstraintLayout {

    public NewBunkerModelView(Context context) {
        super(context);
    }

    public NewBunkerModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.new_bunker_model_view,this);
    }

    public NewBunkerModelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
}
