package me.kevinrenner.textface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.TimeZone;

public class TextWatchFaceService extends CanvasWatchFaceService {

    private Typeface WATCH_TEXT_TYPEFACE;

    private Time mDisplayTime;

    private Paint mBackgroundColorPaint;
    private Paint mTextColorPaint;

    private boolean mHasTimeZoneReceiverBeenRegistered = false;
    private boolean mIsInMuteMode;
    private boolean mIsLowBitAmbient;

    private float mXOffSet;
    private float mYOffSet;

    private int mBackgroundColor;
    private int mTextColor;
    private int mBackgroundColorAmbient = Color.parseColor("black");
    private int mTextColorAmbient = Color.parseColor("white");

    private String mTextFont;

    final BroadcastReceiver mTimeZoneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mDisplayTime.clear(intent.getStringExtra("time-zone"));
            mDisplayTime.setToNow();
        }
    };

    public class TextWatchFaceEngine extends Engine implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
        private GoogleApiClient apiClient;

        private void initBackground() {
            mBackgroundColor = Color.parseColor("black");
            mBackgroundColorPaint = new Paint();
            mBackgroundColorPaint.setColor(mBackgroundColor);
        }

        private void initDisplayText() {
            mTextColor = Color.parseColor("white");
            mTextColorPaint = new Paint();
            mTextColorPaint.setColor(mTextColor);
            WATCH_TEXT_TYPEFACE = Typeface.create("sans-serif-light", 0);
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
            apiClient = new GoogleApiClient.Builder(TextWatchFaceService.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
            Wearable.DataApi.getDataItems(apiClient).setResultCallback(onConnectedResultCallback);
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
                apiClient.connect();
            }
            else {
                if(mHasTimeZoneReceiverBeenRegistered) {
                    TextWatchFaceService.this.unregisterReceiver(mTimeZoneBroadcastReceiver);
                    mHasTimeZoneReceiverBeenRegistered = false;
                }
                releaseGoogleApiClient();
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

        private void releaseGoogleApiClient() {
            if(apiClient != null && apiClient.isConnected()) {
                Wearable.DataApi.removeListener(apiClient, onDataChangedListener);
                apiClient.disconnect();
            }
        }

        private void drawBackground(Canvas canvas, Rect bounds) {
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundColorPaint);
        }

        private String getHours() {
            String output = "";
            int hour = mDisplayTime.hour % 12;
            if(hour == 0)
                output = "Twelve";
            if(hour == 1)
                output = "One";
            if(hour == 2)
                output = "Two";
            if(hour == 3)
                output = "Three";
            if(hour == 4)
                output = "Four";
            if(hour == 5)
                output = "Five";
            if(hour == 6)
                output = "Six";
            if(hour == 7)
                output = "Seven";
            if(hour == 8)
                output = "Eight";
            if(hour == 9)
                output = "Nine";
            if(hour == 10)
                output = "Ten";
            if(hour == 11)
                output = "Eleven";
            return output;
        }

        private String getMinutesTens() {
            int tensValue = mDisplayTime.minute/10;
            String output = "";
            if(mDisplayTime.minute == 0)
                output = "o'Clock";
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
            String output = "";
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
            if(mDisplayTime.minute > 0 && mDisplayTime.minute <= 9)
                minutesTens = getMinutesOnes();
            else if(mDisplayTime.minute > 9 && mDisplayTime.minute < 20) {
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

        private void setPrefs() {
            if(!isInAmbientMode() && isVisible()) {
                mBackgroundColorPaint.setColor(mBackgroundColor);
                mTextColorPaint.setColor(mTextColor);
                WATCH_TEXT_TYPEFACE = Typeface.create(mTextFont, 0);
                mTextColorPaint.setTypeface(WATCH_TEXT_TYPEFACE);
                invalidate();
            }
        }

        private void setTypeFace(String name) {
            String typeface = "sans-serif-light";
            if("Thin".equals(name))
                typeface = "sans-serif-thin";
            if("Light".equals(name))
                typeface = "sans-serif-light";
            if("Condensed".equals(name))
                typeface = "sans-serif-condensed";
            if("Regular".equals(name))
                typeface = "sans-serif";
            if("Medium".equals(name))
                typeface = "sans-serif-medium";
            mTextFont = typeface;
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            mDisplayTime.setToNow();
            drawBackground(canvas, bounds);
            drawTimeText(canvas);
        }

        @Override
        public void onDestroy() {
            releaseGoogleApiClient();
            super.onDestroy();
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(apiClient, onDataChangedListener);
            Wearable.DataApi.getDataItems(apiClient).setResultCallback(onConnectedResultCallback);
        }

        private final DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEventBuffer) {
                for(DataEvent event : dataEventBuffer) {
                    if(event.getType() == DataEvent.TYPE_CHANGED) {
                        DataItem item = event.getDataItem();
                        processConfigurationFor(item);
                    }
                }
                dataEventBuffer.release();
                setPrefs();
            }
        };

        private void processConfigurationFor(DataItem item) {
            if("/text_watch_config".equals(item.getUri().getPath())) {
                DataMap map = DataMapItem.fromDataItem(item).getDataMap();
                if(map.containsKey("BG_COLOR")) {
                    mBackgroundColor = map.getInt("BG_COLOR");
                }
                if(map.containsKey("TEXT_COLOR")) {
                    mTextColor = map.getInt("TEXT_COLOR");
                }
                if(map.containsKey("FONT")) {
                    setTypeFace(map.getString("FONT"));
                }
            }
        }

        private final ResultCallback<DataItemBuffer> onConnectedResultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for(DataItem dataItem : dataItems) {
                    processConfigurationFor(dataItem);
                }
                dataItems.release();
                setPrefs();
            }
        };

        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new TextWatchFaceEngine();
    }

}
