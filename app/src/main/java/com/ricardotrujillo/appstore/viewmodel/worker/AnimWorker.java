package com.ricardotrujillo.appstore.viewmodel.worker;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.ricardotrujillo.appstore.App;
import com.ricardotrujillo.appstore.viewmodel.Constants;
import com.ricardotrujillo.appstore.viewmodel.event.SplashDoneEvent;
import com.squareup.picasso.Callback;

import javax.inject.Inject;

public class AnimWorker {

    App app;

    @Inject
    BusWorker busWorker;

    @Inject
    public AnimWorker(App app) {

        this.app = app;

        inject();

        busWorker.register(this);
    }

    public static ColorStateList getPressedColorSelector(int normalColor, int pressedColor) {

        return new ColorStateList(
                new int[][]
                        {
                                new int[]{}
                        },
                new int[]
                        {
                                pressedColor
                        }
        );
    }

    public static ColorDrawable getColorDrawableFromColor(int color) {

        return new ColorDrawable(color);
    }

    void inject() {

        app.getAppComponent().inject(this);
    }

    public int dpToPx(int dp) {

        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @TargetApi(21)
    public RippleDrawable getPressedColorRippleDrawable(int normalColor, int pressedColor) {

        return new RippleDrawable(getPressedColorSelector(normalColor, pressedColor), getColorDrawableFromColor(normalColor), null);
    }

    @TargetApi(17)
    public Bitmap blur(Context context, Bitmap image, float blurRadius) {
        if (null == image) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(context);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(blurRadius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    @TargetApi(21)
    public void enterReveal(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = view.getMeasuredWidth() / 2;
            int cy = view.getMeasuredHeight() - view.getMeasuredHeight();

            // get the final radius for the clipping circle
            int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, view.getWidth());

            // make the view visible and start the animation
            view.setVisibility(View.VISIBLE);
            anim.setDuration(Constants.REVEAL_ANIMATION);
            anim.setInterpolator(new LinearInterpolator());
            anim.start();
        }
    }

    @TargetApi(21)
    public void exitReveal(final View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = view.getMeasuredWidth() / 2;
            int cy = view.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int initRadius = Math.max(cx, cy);
            int finalRadius = 0;

            if (view.isAttachedToWindow()) {

                // create the animator for this view (the start radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(view, cx, cy, initRadius, finalRadius);

                // make the view visible and start the animation
                view.setVisibility(View.VISIBLE);
                anim.setDuration(Constants.REVEAL_ANIMATION_SPLASH);
                anim.setInterpolator(new LinearInterpolator());
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                anim.start();
            }
        }
    }

    public void animateAvatar(final View v, final Callback callback) {

        v.animate()
                .scaleXBy(0.3f)
                .scaleYBy(0.3f)
                .rotationBy(180)
                .setDuration(Constants.AVATAR_ANIMATION_DURATION_IN)
                .setInterpolator(new AnticipateInterpolator(3f))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        //v.setRotation(0);

                        v.animate()
                                .scaleYBy(-0.3f)
                                .scaleXBy(-0.3f)
                                .rotationBy(180)
                                .setDuration(Constants.AVATAR_ANIMATION_DURATION_OUT)
                                .setInterpolator(new OvershootInterpolator(3f))
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {

                                        v.setRotation(0);

                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    public int alterColor(int color, float factor) {

        int a = (color & (0xFF << 24)) >> 24;
        int r = (int) (((color & (0xFF << 16)) >> 16) * factor);
        int g = (int) (((color & (0xFF << 8)) >> 8) * factor);
        int b = (int) ((color & 0xFF) * factor);

        return Color.argb(a, r, g, b);
    }

    public ColorDrawable getColorDrawable(Palette palette) {

        return new ColorDrawable(palette
                .getLightVibrantColor(palette
                        .getVibrantColor(palette
                                .getDarkVibrantColor(palette
                                        .getDarkMutedColor(palette
                                                .getMutedColor(0x000000)))))); //default 0x000000
    }

    public ColorDrawable getDarkColorDrawable(Palette palette) {

        return new ColorDrawable(palette
                .getDarkVibrantColor(palette
                        .getDarkMutedColor(palette
                                .getMutedColor(palette
                                        .getLightVibrantColor(palette
                                                .getVibrantColor(0x000000)))))); //default 0x000000
    }

    public void startAlphaAnimation(View v, long duration, int visibility) {

        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public void animateSlash(final View view, final View root) {

        view.setScaleX(10f);
        view.setScaleY(10f);
        view.setAlpha(0f);

        //view.animate()
        //        .setDuration(400)
        //        .alpha(1f);


        view.animate()
                .setDuration(3000)
                .setInterpolator(new BounceInterpolator())
                .scaleX(1f)
                .alpha(1f)
                .scaleY(1f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                dismissSplash(root);

                busWorker.post(new SplashDoneEvent());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    void dismissSplash(View root) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            exitReveal(root);

        } else {

            fadeOut(root);
        }
    }

    void fadeOut(final View view) {

        view.animate()
                .setDuration(Constants.REVEAL_ANIMATION_SPLASH)
                .alpha(0f)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

}
