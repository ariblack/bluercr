package za.co.house4hack.bluercr;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.SeekBar;

public class SeekBarEx extends SeekBar implements
		SeekBar.OnSeekBarChangeListener {
	final int SEEK_POINTS = 0x10000;
	final String TAG = "SeekBarEx";
	public float mMax;
	public float mMin;
	public OnSeekBarExChangeListener delegate = null;

	public interface OnSeekBarExChangeListener {
		public void onSeekChanged(SeekBarEx seekBarEx, float value,
				boolean fromUser);

		public void onStartTrackingTouch(SeekBarEx seekBar);

		public void onStopTrackingTouch(SeekBarEx seekBar);
	}

	public SeekBarEx(Context ctx, AttributeSet attr) {
		super(ctx, attr);

		super.setMax(SEEK_POINTS);

		mMin = 0f;
		mMax = 1.0f;
		initAttributes(attr);
		this.setOnSeekBarChangeListener(this);
	}

	public void setDelegate(OnSeekBarExChangeListener d) {
		delegate = d;
	}

	public void initAttributes(AttributeSet attrSet) {
		TypedArray a;
		a = getContext().obtainStyledAttributes(attrSet, R.styleable.SeekBarEx);

		final int N = a.getIndexCount();

		int i;
		for (i = 0; i < N; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.SeekBarEx_max:
				mMax = a.getFloat(i, 1.0f);
				Log.d(TAG, "maxSet " + mMax);
				break;
			case R.styleable.SeekBarEx_min:
				mMin = a.getFloat(i, 0f);
				Log.d(TAG, "minSet" + mMin);
				break;
			case R.styleable.SeekBarEx_value:
				this.setValue(a.getFloat(i, 0));
				break;
			}
		}

		a.recycle();

	}

	@Override
	public int getProgress() {
		return super.getProgress();
	}

	public float getValue() {
		float r;
		float run;
		r = (float) super.getProgress();
		r = r / (float) SEEK_POINTS;
		run = mMax - mMin;
		r = r * run + mMin;
		return r;
	}

	public void setValue(float v) {
		if (Float.isNaN(v) || Float.isInfinite(v))
			return;
		if (v > mMax)
			v = mMax;
		if (v < mMin)
			v = mMin;
		float run;
		int setv;
		run = mMax - mMin;
		v -= mMin;
		setv = Math.round(v * (float) SEEK_POINTS / run);
		super.setProgress(setv);
	}

	public boolean valueChanged = false;

	public void cancelTracking() {
		if (oldValue != Float.NaN) {
			this.setValue(oldValue);
			oldValue = Float.NaN;
			valueChanged = false;
			acceptTouches = false;
			acceptChange = false;
		}
	}

	// we override these methods so that when we forcully cancel
	// on ontouches moved. We can revert back to the old value
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Log.d(TAG, "SeekBar changed to " + progress);
		if (delegate != null && acceptTouches) {
			valueChanged = true;
			delegate.onSeekChanged(this, this.getValue(), fromUser);
		} else
			cancelTracking();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if (delegate != null)
			delegate.onStartTrackingTouch(this);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (delegate != null && valueChanged)
			delegate.onStopTrackingTouch(this);
		else
			cancelTracking();
		acceptChange = false;
		valueChanged = false;
	}

	public float mY, mX;
	public boolean acceptTouches = true;
	// acceptChange never read todo: delete
	public boolean acceptChange = false;
	public float oldValue = Float.NaN;

	public ScrollView getScrollView() {
		View view;
		view = this;
		int maxUp;
		maxUp = 5;
		while (view != null && maxUp > 0) {
			view = (View) view.getParent();
			ScrollView scroller;
			if (view instanceof ScrollView) {
				scroller = (ScrollView) view;
				return scroller;
			}
			maxUp--;
		}
		return null;
	}

	// **************************************
	// This is the important part in achieving the effect in scroll
	// view to be nice
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action;
		action = event.getAction() & MotionEvent.ACTION_MASK;
		ScrollView scroller = this.getScrollView();
		boolean mayScroll;

		mayScroll = true;
		if (scroller == null)
			mayScroll = false;
		else {
			int scrollAmount = scroller.getMaxScrollAmount();
			if (scrollAmount == 0)
				mayScroll = false;
		}
		switch (action) {
		case MotionEvent.ACTION_CANCEL:
			Log.d(TAG, "got cancel touches");
			cancelTracking();
			super.onTouchEvent(event);
			return true;
		case MotionEvent.ACTION_DOWN:
			mX = event.getX();
			mY = event.getY();
			acceptTouches = true;
			acceptChange = false;
			oldValue = this.getValue();
			valueChanged = false;
			break;
		case MotionEvent.ACTION_MOVE:
			float x;
			float y;
			x = event.getX();
			y = event.getY();
			float dx;
			float dy;
			dx = x - mX;
			dy = y - mY;
			if (dx < 0)
				dx = -dx;
			if (dy < 0)
				dy = -dy;

			y = this.getHeight() / 2 - y;
			float angle;
			float distance;
			distance = dx * dx + dy * dy;
			// I just realized this is wrong it should be
			// angle = (float)Math.acos(Math.abs(dx)/Math.sqrt(distance))
			// I'm leaving it until tested or someone can confirm
			angle = (float) Math.atan(dy / dx);
			int distanceLimit;
			distanceLimit = this.getHeight() / 3;
			distanceLimit *= distanceLimit;
			// if we move at an angle of atleast 45degrees
			// cancel
			if (mayScroll && angle > Math.PI / 4.0) {
				cancelTracking();
			}

			mX += 100000;
			if (y < 0)
				y = -y;
			// if we moved finger too far just cancel
			// cause the person may have wanted to scroll but
			// failed so we revert back to the old value
			if (y > this.getHeight() * 2) {
				cancelTracking();
			} else if (acceptTouches)
				acceptChange = true;
			break;
		default:
			break;
		}
		// if we accept touches do the usual otherwise
		// return false so scrollView can do it's thing
		if (acceptTouches)
			return super.onTouchEvent(event);
		return false;
	}

}