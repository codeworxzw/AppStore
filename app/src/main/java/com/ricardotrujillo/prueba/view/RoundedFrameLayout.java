package com.ricardotrujillo.prueba.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import com.ricardotrujillo.prueba.R;

public class RoundedFrameLayout extends FrameLayout {
    private final static float CORNER_RADIUS = 15.0f;

    Bitmap maskBitmap;
    DisplayMetrics metrics;
    private Paint paint, maskPaint;
    private float cornerRadius;
    private int radius = 0;

    public RoundedFrameLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoundedFrameLayout,
                0, 0);

        try {

            if (radius == 0) radius = a.getInteger(R.styleable.RoundedFrameLayout_radius, 0);

        } finally {

            a.recycle();
        }

        metrics = context.getResources().getDisplayMetrics();
        //cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CORNER_RADIUS, metrics);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        setWillNotDraw(false);
    }

    @Override
    public void draw(Canvas canvas) {

        float newShadowRadius = (float) radius / 100f;

        cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) canvas.getWidth() * newShadowRadius / 2.9f, metrics);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (radius > 0) {

                setOutlineProvider(new CustomOutline(canvas.getWidth(), canvas.getHeight(), (float) canvas.getWidth() * newShadowRadius));

            } else {

                setOutlineProvider(new CustomOutline(canvas.getWidth(), canvas.getHeight(), (float) canvas.getWidth() * 0.14f));
            }
        }

        Bitmap offscreenBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas offscreenCanvas = new Canvas(offscreenBitmap);

        super.draw(offscreenCanvas);

        maskBitmap = createMask(canvas.getWidth(), canvas.getHeight());

        offscreenCanvas.drawBitmap(maskBitmap, 0f, 0f, maskPaint);

        canvas.drawBitmap(offscreenBitmap, 0f, 0f, paint);
    }

    private Bitmap createMask(int width, int height) {

        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(mask);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);

        canvas.drawRect(0, 0, width, height, paint);

        float multipler = 2.2f;

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(new RectF(0, 0, width, height), cornerRadius, cornerRadius, paint);

        return mask;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class CustomOutline extends ViewOutlineProvider {

        int width;
        int height;
        float radius;

        CustomOutline(int width, int height, float radius) {

            this.width = width;
            this.height = height;
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {

            outline.setRoundRect(0, 0, width, height, radius);
        }
    }
}