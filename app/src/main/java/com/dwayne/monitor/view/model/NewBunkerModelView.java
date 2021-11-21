package com.dwayne.monitor.view.model;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.dwayne.monitor.R;
import com.dwayne.monitor.ViewModel.SpraySpeedViewModel;

public class NewBunkerModelView extends ConstraintLayout {
    Context context;
    //风机四个
    ImageView fans[];
    //喷嘴八个
    ImageView sprays[];
    //喷嘴的六种大小
    AnimationDrawable[] animationDrawable_0_speed;
    AnimationDrawable[] animationDrawable_20_speed;
    AnimationDrawable[] animationDrawable_40_speed;
    AnimationDrawable[] animationDrawable_60_speed;
    AnimationDrawable[] animationDrawable_80_speed;
    //风机6种转速
    Animation animation_0_speed;
    Animation animation_20_speed;
    Animation animation_40_speed;
    Animation animation_60_speed;
    Animation animation_80_speed;
    Animation animation_100_speed;

    LinearInterpolator linearInterpolator;

    int speed[];

    public NewBunkerModelView(Context context) {
        super(context);
        this.context = context;
    }

    public NewBunkerModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.new_bunker_model_view, this);
        initView();
    }

    public void initView() {
        fans = new ImageView[4];
        sprays = new ImageView[8];
        fans[0] = findViewById(R.id.fan_no_1);
        fans[1] = findViewById(R.id.fan_no_2);
        fans[2] = findViewById(R.id.fan_no_3);
        fans[3] = findViewById(R.id.fan_no_4);
        sprays[0] = findViewById(R.id.spray_head_1);
        sprays[1] = findViewById(R.id.spray_head_2);
        sprays[2] = findViewById(R.id.spray_head_3);
        sprays[3] = findViewById(R.id.spray_head_4);
        sprays[4] = findViewById(R.id.spray_head_5);
        sprays[5] = findViewById(R.id.spray_head_6);
        sprays[6] = findViewById(R.id.spray_head_7);
        sprays[7] = findViewById(R.id.spray_head_8);
        animationDrawable_0_speed = new AnimationDrawable[8];
        animationDrawable_20_speed = new AnimationDrawable[8];
        animationDrawable_40_speed = new AnimationDrawable[8];
        animationDrawable_60_speed = new AnimationDrawable[8];
        animationDrawable_80_speed = new AnimationDrawable[8];

        animation_0_speed = AnimationUtils.loadAnimation(context, R.anim.image_0_rotation);
        animation_20_speed = AnimationUtils.loadAnimation(context, R.anim.image_20_rotation);
        animation_40_speed = AnimationUtils.loadAnimation(context, R.anim.image_40_rotation);
        animation_60_speed = AnimationUtils.loadAnimation(context, R.anim.image_60_rotation);
        animation_80_speed = AnimationUtils.loadAnimation(context, R.anim.image_80_rotation);
        animation_100_speed = AnimationUtils.loadAnimation(context, R.anim.image_100_rotation);
        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_20_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_20_round);
        }
        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_40_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_40_round);
        }
        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_60_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_60_round);
        }
        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_80_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_80_round);
        }
        speed = new int[8];
        linearInterpolator = new LinearInterpolator();
        animation_0_speed.setInterpolator(linearInterpolator);
        animation_20_speed.setInterpolator(linearInterpolator);
        animation_40_speed.setInterpolator(linearInterpolator);
        animation_60_speed.setInterpolator(linearInterpolator);
        animation_80_speed.setInterpolator(linearInterpolator);
        animation_100_speed.setInterpolator(linearInterpolator);
    }

    public void changeFromSpeed(int[] ints) {
        for (int i = 0; i < fans.length; i++) {
            switch (ints[i]) {
                case 20:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_20));
                    fans[i].startAnimation(animation_20_speed);
                    sprays[2 * i].setImageDrawable(animationDrawable_20_speed[i]);
                    sprays[2 * i + 1].setImageDrawable(animationDrawable_20_speed[i]);
                    animationDrawable_20_speed[i].start();
                    break;
                case 40:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_40));
                    fans[i].startAnimation(animation_40_speed);
                    sprays[2 * i].setImageDrawable(animationDrawable_40_speed[i]);
                    sprays[2 * i + 1].setImageDrawable(animationDrawable_40_speed[i]);
                    animationDrawable_40_speed[i].start();
                    break;
                case 60:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_60));
                    fans[i].startAnimation(animation_60_speed);
                    sprays[2 * i].setImageDrawable(animationDrawable_60_speed[i]);
                    sprays[2 * i + 1].setImageDrawable(animationDrawable_60_speed[i]);
                    animationDrawable_60_speed[i].start();
                    break;
                case 80:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_80));
                    fans[i].startAnimation(animation_80_speed);
                    sprays[2 * i].setImageDrawable(animationDrawable_80_speed[i]);
                    sprays[2 * i + 1].setImageDrawable(animationDrawable_80_speed[i]);
                    animationDrawable_80_speed[i].start();
                    break;
                default:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_20));
                    sprays[i].startAnimation(animation_0_speed);
                    sprays[2 * i].setImageDrawable(animationDrawable_80_speed[i]);
                    sprays[2 * i + 1].setImageDrawable(animationDrawable_0_speed[i]);
                    animationDrawable_80_speed[i].start();
            }
        }
    }
}
