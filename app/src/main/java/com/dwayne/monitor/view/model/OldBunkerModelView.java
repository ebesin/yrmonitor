package com.dwayne.monitor.view.model;

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

public class OldBunkerModelView extends ConstraintLayout {
    Context context;
    //9个喷嘴
    private ImageView[] spray_large_heads;
    //风机
    private ImageView large_fan;

    //控制旋转文件
    Animation animation_0_speed;
    Animation animation_20_speed;
    Animation animation_40_speed;
    Animation animation_60_speed;
    Animation animation_80_speed;
    Animation animation_100_speed;
    LinearInterpolator linearInterpolator;

    AnimationDrawable[] animationDrawable;

    private int[] speed;


    public OldBunkerModelView(Context context) {
        super(context);
        this.context = context;
    }

    public OldBunkerModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.old_bunker_model_view,this);
        initView();
    }

    public OldBunkerModelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initView(){
        spray_large_heads = new ImageView[9];
        speed = new int[9];
        animationDrawable = new AnimationDrawable[9];
        spray_large_heads[0] = findViewById(R.id.spray_large_head_1);
        spray_large_heads[1] = findViewById(R.id.spray_large_head_2);
        spray_large_heads[2] = findViewById(R.id.spray_large_head_3);
        spray_large_heads[3] = findViewById(R.id.spray_large_head_4);
        spray_large_heads[4] = findViewById(R.id.spray_large_head_5);
        spray_large_heads[5] = findViewById(R.id.spray_large_head_6);
        spray_large_heads[6] = findViewById(R.id.spray_large_head_7);
        spray_large_heads[7] = findViewById(R.id.spray_large_head_8);
        spray_large_heads[8] = findViewById(R.id.spray_large_head_9);

        large_fan = findViewById(R.id.large_fan_1);

        animation_0_speed = AnimationUtils.loadAnimation(context, R.anim.image_0_rotation);
        animation_20_speed = AnimationUtils.loadAnimation(context, R.anim.image_20_rotation);
        animation_40_speed = AnimationUtils.loadAnimation(context, R.anim.image_40_rotation);
        animation_60_speed = AnimationUtils.loadAnimation(context, R.anim.image_60_rotation);
        animation_80_speed = AnimationUtils.loadAnimation(context, R.anim.image_80_rotation);
        animation_100_speed = AnimationUtils.loadAnimation(context, R.anim.image_100_rotation);
        linearInterpolator = new LinearInterpolator();

        animation_0_speed.setInterpolator(linearInterpolator);
        animation_20_speed.setInterpolator(linearInterpolator);
        animation_40_speed.setInterpolator(linearInterpolator);
        animation_60_speed.setInterpolator(linearInterpolator);
        animation_80_speed.setInterpolator(linearInterpolator);
        animation_100_speed.setInterpolator(linearInterpolator);

        large_fan.setImageDrawable(getResources().getDrawable(R.drawable.ic_large_fan_20));
        large_fan.startAnimation(animation_60_speed);
    }

    public void changeFromSpeed(int[] ints) {
        System.out.println("changefronspeed===============");
        for (int i = 0; i < spray_large_heads.length; i++) {
            switch (ints[i]) {
                case 20:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_20_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xFFFFFFFF);
                    animationDrawable[i].start();
                    break;
                case 40:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_40_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xff0000ff);
                    animationDrawable[i].start();
                    break;
                case 60:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_60_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xffFFFF00);
                    animationDrawable[i].start();
                    break;
                case 80:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_80_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xffFFA500);
                    animationDrawable[i].start();
                    break;
                case 100:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_100_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xffff0000);
                    animationDrawable[i].start();
                    break;
                default:
                    spray_large_heads[i].setColorFilter(0xff1296DB);
                    spray_large_heads[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_method_draw_image));
                    break;
            }
        }
    }

}
