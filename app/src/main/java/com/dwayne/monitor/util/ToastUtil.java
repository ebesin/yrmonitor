package com.dwayne.monitor.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dwayne.monitor.R;

public class ToastUtil {

    public static void showToast(Context context, String text) {
        View mToastView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.toast_layout, null, false);
        TextView mTvToast = mToastView.findViewById(R.id.tv_toast);
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        mTvToast.setText(text);
        toast.setView(mToastView);
        toast.setGravity(Gravity.CENTER, 0, 200);
        toast.show();
    }
}

