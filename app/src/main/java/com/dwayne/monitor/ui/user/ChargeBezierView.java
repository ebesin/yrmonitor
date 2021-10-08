package com.dwayne.monitor.ui.user;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * @author xww
 * @desciption :
 * @date 2019/8/6
 * @time 12:11
 * 博主：威威喵
 * 博客：https://blog.csdn.net/smile_Running
 */
public class ChargeBezierView extends View {

    private Paint mExternalPaint;
    private Paint mInnerPaint;
    private Paint mArcPaint;
    private Paint mCirclePaint;
    private Paint mTextPaint;

    private Paint mMaskPaint;

    private int mWidth;
    private int mHeight;
    // 充电进度值百分制
    private int mProgress;
    private float mArcProgress;
    private float mPaintSize;

    //水波纹于进度条的高度比
    private float rippleScale;
    //用于画进度
    private RectF mRect;

    private Random mRandom;

    private float mCircleX;
    private float mCircleY;
    private float mDefCircleRadius;

    // 对角线的长度
    private float mDiagonal;

    private boolean isFinished = false;

    //水波纹高度坐标
    private float x;
    private float y;

    private void init() {
        mExternalPaint = getPaint(Color.parseColor("#554F94CD"));
        mInnerPaint = getPaint(Color.parseColor("#66B8FF"));
        mArcPaint = getPaint(Color.parseColor("#7FFF00"));
        mArcPaint.setStyle(Paint.Style.STROKE);//空心
        mCirclePaint = getPaint(Color.parseColor("#F8F8FF"));
        mCirclePaint.setStyle(Paint.Style.STROKE);//空心
        mTextPaint = getPaint(Color.parseColor("#FF00ff"));
        mMaskPaint = getPaint(Color.parseColor("#FFFFFF"));
        mMaskPaint.setStyle(Paint.Style.STROKE);

        mRandom = new Random();

        mPaintSize = mTextPaint.getTextSize();
    }

    private Paint getPaint(int color) {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(18f);
        paint.setTextSize(60f);
        paint.setColor(color);
        return paint;
    }

    public ChargeBezierView(Context context) {
        this(context, null);
    }

    public ChargeBezierView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargeBezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mCircleX = mWidth / 2;
        mCircleY = mHeight / 2;

        mDefCircleRadius = mWidth / 4;
        mRect = new RectF(mCircleX - mDefCircleRadius, mCircleY - mDefCircleRadius,
                mCircleX + mDefCircleRadius, mCircleY + mDefCircleRadius);

        mDiagonal = (float) Math.sqrt(Math.pow(mCircleX, 2) + Math.pow(mCircleY, 2));

        rippleScale = 2 * mDefCircleRadius / 100;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isFinished) {
            drawMasked(canvas);
            drawFinished(canvas);
        } else {
            drawExternalRipple(canvas);
            drawMasked(canvas);
            drawProgressText(canvas);
            drawCircle(canvas);
            drawProgress(canvas);
        }
    }

    // 绘制电量圆形轨道
    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(mCircleX, mCircleY, mDefCircleRadius, mCirclePaint);
    }

    private void drawProgress(Canvas canvas) {
        // -90 表示从上半轴 x=0 开始
        canvas.drawArc(mRect, -90, mArcProgress, false, mArcPaint);
    }

    private void drawProgressText(Canvas canvas) {
        canvas.drawText(mProgress + "%", mCircleX - mPaintSize, mCircleY + mTextPaint.getTextSize() / 2, mTextPaint);
    }

    private void drawMasked(Canvas canvas) {
        //绘制一个遮罩层，屏蔽 Path Close 以外的区域
        mMaskPaint.setStrokeWidth(mDiagonal + mDefCircleRadius * 2 - mPaintSize * 1.5f);
        canvas.drawCircle(mCircleX, mCircleY, mDiagonal, mMaskPaint);
    }

    private void drawFinished(Canvas canvas) {
        canvas.drawCircle(mCircleX, mCircleY, mDefCircleRadius, mArcPaint);
        canvas.drawCircle(mCircleX, mCircleY, mDefCircleRadius, mInnerPaint);
        canvas.drawText("电量已满", mCircleX - mTextPaint.getTextSize() * 2f, mCircleY + mTextPaint.getTextSize() / 2, mTextPaint);
    }

    private PointF pExt0;
    private PointF pExt1;
    private PointF pExt2;
    private PointF pExt3;

    private PointF pIn0;
    private PointF pIn1;
    private PointF pIn2;
    private PointF pIn3;

    ValueAnimator externalAnimator;

    // 绘制海浪的波纹效果，分内部和外部两条
    private void drawExternalRipple(Canvas canvas) {

        // 计算进度的 x , y 位置
        y = mCircleY - mDefCircleRadius + (100 - mProgress) * rippleScale;
        x = caculateX(y);

        float rippleY = y;
        float rippleX = mCircleX;

        //内部
        pIn0 = new PointF(rippleX - mDefCircleRadius, rippleY);
        pIn1 = new PointF(rippleX - mRandom.nextInt((int) mDefCircleRadius), rippleY - mRandom.nextInt((int) (mDefCircleRadius / 4)));
        pIn2 = new PointF(rippleX + mRandom.nextInt((int) mDefCircleRadius), rippleY + mRandom.nextInt((int) (mDefCircleRadius / 4)));
        pIn3 = new PointF(rippleX + mDefCircleRadius, rippleY);
        Path inPath = new Path();
        inPath.moveTo(pIn0.x, pIn0.y);
        inPath.cubicTo(pIn1.x, pIn1.y, pIn2.x, pIn2.y, pIn3.x, pIn3.y);
        inPath.lineTo(mCircleX + mDefCircleRadius, mCircleY + mDefCircleRadius);
        inPath.lineTo(mCircleX - mDefCircleRadius, mCircleY + mDefCircleRadius);
        inPath.close();
        canvas.drawPath(inPath, mInnerPaint);

        // 外部
        pExt0 = new PointF(rippleX - mDefCircleRadius, rippleY);
        pExt1 = new PointF(rippleX - mRandom.nextInt((int) mDefCircleRadius), rippleY + mRandom.nextInt((int) (mDefCircleRadius / 3)));
        pExt2 = new PointF(rippleX + mRandom.nextInt((int) mDefCircleRadius), rippleY + mRandom.nextInt((int) (mDefCircleRadius / 3)));
        pExt3 = new PointF(rippleX + mDefCircleRadius, rippleY);
        Path extPath = new Path();
        extPath.moveTo(pExt0.x, pExt0.y);
        extPath.cubicTo(pExt1.x, pExt1.y, pExt2.x, pExt2.y, pExt3.x, pExt3.y);
        extPath.lineTo(mCircleX + mDefCircleRadius, mCircleY + mDefCircleRadius);
        extPath.lineTo(mCircleX - mDefCircleRadius, mCircleY + mDefCircleRadius);
        extPath.close();
        canvas.drawPath(extPath, mExternalPaint);

    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        this.mArcProgress = mProgress * 3.6f;
        if (mProgress <= 100) {
            isFinished = false;
        } else {
            isFinished = true;
        }
        invalidate();
    }

    // 圆的方程式 a2 = b2 + c2
    private float caculateX(float y) {
        x = (float) Math.sqrt(Math.pow(mDefCircleRadius, 2) - y * y);
        return x;
    }
}
