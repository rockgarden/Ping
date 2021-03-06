package com.colinwhite.ping.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * https://gist.github.com/dmide/7506c7d9614eed90805d
 */
class CircularProgressDrawable extends Drawable implements Animatable {
	private static final Interpolator ANGLE_INTERPOLATOR = new LinearInterpolator();
	private static final Interpolator SWEEP_INTERPOLATOR = new DecelerateInterpolator();
	private static final int ANGLE_ANIMATOR_DURATION = 2000;
	private static final int SWEEP_ANIMATOR_DURATION = 600;
	private static final int MIN_SWEEP_ANGLE = 30;
	private static final float BORDER_WIDTH = 7;
	private final RectF fBounds = new RectF();
	private final Paint mPaint;
	private ObjectAnimator mObjectAnimatorSweep;
	private ObjectAnimator mObjectAnimatorAngle;
	private boolean mModeAppearing;
	private float mCurrentGlobalAngleOffset;
	private float mCurrentGlobalAngle;
	private final Property<CircularProgressDrawable, Float> mAngleProperty = new Property<CircularProgressDrawable, Float>(Float.class, "angle") {
		@Override
		public Float get(CircularProgressDrawable object) {
			return object.getCurrentGlobalAngle();
		}

		@Override
		public void set(CircularProgressDrawable object, Float value) {
			object.setCurrentGlobalAngle(value);
		}
	};
	private float mCurrentSweepAngle;
	private final Property<CircularProgressDrawable, Float> mSweepProperty
			= new Property<CircularProgressDrawable, Float>(Float.class, "arc") {
		@Override
		public Float get(CircularProgressDrawable object) {
			return object.getCurrentSweepAngle();
		}

		@Override
		public void set(CircularProgressDrawable object, Float value) {
			object.setCurrentSweepAngle(value);
		}
	};
	private boolean mRunning;

	public CircularProgressDrawable(int color) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(BORDER_WIDTH);
		mPaint.setColor(color);

		setupAnimations();
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		float startAngle = mCurrentGlobalAngle - mCurrentGlobalAngleOffset;
		float sweepAngle = mCurrentSweepAngle;
		if (!mModeAppearing) {
			startAngle = startAngle + sweepAngle;
			sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE;
		} else {
			sweepAngle += MIN_SWEEP_ANGLE;
		}
		canvas.drawArc(fBounds, startAngle, sweepAngle, false, mPaint);
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void start() {
		if (isRunning()) {
			return;
		}
		mRunning = true;
		mObjectAnimatorAngle.start();
		mObjectAnimatorSweep.start();
		invalidateSelf();
	}

	@Override
	public void stop() {
		if (!isRunning()) {
			return;
		}
		mRunning = false;
		mObjectAnimatorAngle.cancel();
		mObjectAnimatorSweep.cancel();
		invalidateSelf();
	}

	@Override
	public boolean isRunning() {
		return mRunning;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		fBounds.left = bounds.left + BORDER_WIDTH / 2f + .5f;
		fBounds.right = bounds.right - BORDER_WIDTH / 2f - .5f;
		fBounds.top = bounds.top + BORDER_WIDTH / 2f + .5f;
		fBounds.bottom = bounds.bottom - BORDER_WIDTH / 2f - .5f;
	}

	private float getCurrentGlobalAngle() {
		return mCurrentGlobalAngle;
	}

	private void setCurrentGlobalAngle(float currentGlobalAngle) {
		mCurrentGlobalAngle = currentGlobalAngle;
		invalidateSelf();
	}

	private float getCurrentSweepAngle() {
		return mCurrentSweepAngle;
	}

	//////////////////////////////////////////////////////////////////////////////
	////////////////            Animation

	private void setCurrentSweepAngle(float currentSweepAngle) {
		mCurrentSweepAngle = currentSweepAngle;
		invalidateSelf();
	}

	private void toggleAppearingMode() {
		mModeAppearing = !mModeAppearing;
		if (mModeAppearing) {
			mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360;
		}
	}

	private void setupAnimations() {
		mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 360f);
		mObjectAnimatorAngle.setInterpolator(ANGLE_INTERPOLATOR);
		mObjectAnimatorAngle.setDuration(ANGLE_ANIMATOR_DURATION);
		mObjectAnimatorAngle.setRepeatMode(ValueAnimator.RESTART);
		mObjectAnimatorAngle.setRepeatCount(ValueAnimator.INFINITE);

		mObjectAnimatorSweep = ObjectAnimator.ofFloat(this, mSweepProperty, 360f - MIN_SWEEP_ANGLE * 2);
		mObjectAnimatorSweep.setInterpolator(SWEEP_INTERPOLATOR);
		mObjectAnimatorSweep.setDuration(SWEEP_ANIMATOR_DURATION);
		mObjectAnimatorSweep.setRepeatMode(ValueAnimator.RESTART);
		mObjectAnimatorSweep.setRepeatCount(ValueAnimator.INFINITE);
		mObjectAnimatorSweep.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				toggleAppearingMode();
			}
		});
	}
}