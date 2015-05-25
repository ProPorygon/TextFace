package me.kevinrenner.textface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TextWatchFaceService extends CanvasWatchFaceService {
    private Typeface WATCH_TEXT_TYPEFACE = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    private Time mDisplayTime;

    private Paint mBackgroundColorPaint;
    private Paint mTextColorPaint;

    private boolean mHasTimeZoneReceiverBeenRegistered = false;
    private boolean mIsInMuteMode;
    private boolean mIsLowBitAmbient;

    private float mXOffSet;
    private float mYOffSet;

    private int mBackgroundColor = Color.parseColor("black");
    private int mTextColor = Color.parseColor("white");
    private int mBackgroundColorAmbient = Color.parseColor("black");
    private int mTextColorAmbient = Color.parseColor("white");

    final BroadcastReceiver mTimeZoneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mDisplayTime.clear(intent.getStringExtra("time-zone"));
            mDisplayTime.setToNow();
        }
    };

    public class TextWatchFaceEngine extends Engine {
        private void initBackground() {
            mBackgroundColorPaint = new Paint();
            mBackgroundColorPaint.setColor(mBackgroundColor);
        }

        private void initDisplayText() {
            mTextColorPaint = new Paint();
            mTextColorPaint.setColor(mTextColor);
            mTextColorPaint.setTypeface(WATCH_TEXT_TYPEFACE);
            mTextColorPaint.setAntiAlias(true);
            mTextColorPaint.setTextSize(getResources().getDimension(R.dimen.text_size));
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(TextWatchFaceService.this)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setShowSystemUiTime(false)
                    .build());
            mDisplayTime = new Time();
            initBackground();
            initDisplayText();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if(visible) {
                if(!mHasTimeZoneReceiverBeenRegistered) {
                    IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                    TextWatchFaceService.this.registerReceiver(mTimeZoneBroadcastReceiver, filter);
                    mHasTimeZoneReceiverBeenRegistered = true;
                }
                mDisplayTime.clear(TimeZone.getDefault().getID());
                mDisplayTime.setToNow();
            }
            else {
                if(mHasTimeZoneReceiverBeenRegistered) {
                    TextWatchFaceService.this.unregisterReceiver(mTimeZoneBroadcastReceiver);
                    mHasTimeZoneReceiverBeenRegistered = false;
                }
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mYOffSet = getResources().getDimension(R.dimen.y_offset);
            if(insets.isRound()) {
                mXOffSet = getResources().getDimension(R.dimen.x_offset_round);
            }
            else {
                mXOffSet = getResources().getDimension(R.dimen.x_offset_square);
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            if(properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false)) {
                mIsLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if(inAmbientMode) {
                mTextColorPaint.setColor(mTextColorAmbient);
                mBackgroundColorPaint.setColor(mBackgroundColorAmbient);
            }
            else {
                mTextColorPaint.setColor(mTextColor);
                mBackgroundColorPaint.setColor(mBackgroundColor);
            }
            if(mIsLowBitAmbient) {
                mTextColorPaint.setAntiAlias(!inAmbientMode);
            }
            invalidate();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean isDeviceMuted = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if(mIsInMuteMode != isDeviceMuted) {
                mIsInMuteMode = isDeviceMuted;
                int alpha = (isDeviceMuted) ? 100 : 255;
                mTextColorPaint.setAlpha(alpha);
                invalidate();
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        private void drawBackground(Canvas canvas, Rect bounds) {
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundColorPaint);
        }

        private String getHours() {
            String output = "time";
            if(mDisplayTime.hour % 12 == 0)
                output = "Twelve";
            if(mDisplayTime.hour == 1)
                output = "One";
            if(mDisplayTime.hour == 2)
                output = "Two";
            if(mDisplayTime.hour == 3)
                output = "Three";
            if(mDisplayTime.hour == 4)
                output = "Four";
            if(mDisplayTime.hour == 5)
                output = "Five";
            if(mDisplayTime.hour == 6)
                output = "Six";
            if(mDisplayTime.hour == 7)
                output = "Seven";
            if(mDisplayTime.hour == 8)
                output = "Eight";
            if(mDisplayTime.hour == 9)
                output = "Nine";
            if(mDisplayTime.hour == 10)
                output = "Ten";
            if(mDisplayTime.hour == 11)
                output = "Eleven";
            return output;
        }

        private String getMinutesTens() {
            int tensValue = mDisplayTime.minute/10;
            String output = "time";
            if(tensValue == 0)
                output = "O";
            if(tensValue == 2)
                output = "Twenty";
            if(tensValue == 3)
                output = "Thirty";
            if(tensValue == 4)
                output = "Forty";
            if(tensValue == 5)
                output = "Fifty";
            return output;
        }

        private String getMinutesOnes() {
            int onesValue = mDisplayTime.minute%10;
            String output = "time";
            if(onesValue == 0)
                output = "";
            if(onesValue == 1)
                output = "One";
            if(onesValue == 2)
                output = "Two";
            if(onesValue == 3)
                output = "Three";
            if(onesValue == 4)
                output = "Four";
            if(onesValue == 5)
                output = "Five";
            if(onesValue == 6)
                output = "Six";
            if(onesValue == 7)
                output = "Seven";
            if(onesValue == 8)
                output = "Eight";
            if(onesValue == 9)
                output = "Nine";
            if(mDisplayTime.minute == 0)
                output = "Clock";
            return output;
        }

        private String getTeens() {
            int minute = mDisplayTime.minute;
            String output = "time";
            if(minute == 10)
                output = "Ten";
            if(minute == 11)
                output = "Eleven";
            if(minute == 12)
                output = "Twelve";
            if(minute == 13)
                output = "Thirteen";
            if(minute == 14)
                output = "Fourteen";
            if(minute == 15)
                output = "Fifteen";
            if(minute == 16)
                output = "Sixteen";
            if(minute == 17)
                output = "Seventeen";
            if(minute == 18)
                output = "Eighteen";
            if(minute == 19)
                output = "Nineteen";
            return output;
        }

        private void drawTimeText(Canvas canvas) {
            String hours = getHours();
            String minutesTens;
            String minutesOnes = "";
            if(mDisplayTime.minute > 9 && mDisplayTime.minute < 20) {
                minutesTens = getTeens();
            }
            else {
                minutesTens = getMinutesTens();
                minutesOnes = getMinutesOnes();
            }
            canvas.drawText(hours, mXOffSet, mYOffSet, mTextColorPaint);
            canvas.drawText(minutesTens, mXOffSet, mYOffSet + 50, mTextColorPaint);
            canvas.drawText(minutesOnes, mXOffSet, mYOffSet + 100, mTextColorPaint);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            mDisplayTime.setToNow();
            drawBackground(canvas, bounds);
            drawTimeText(canvas);
        }

    }

    @Override
    public Engine onCreateEngine() {
        return new TextWatchFaceEngine();
    }

}
