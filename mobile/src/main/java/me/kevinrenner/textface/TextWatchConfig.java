package me.kevinrenner.textface;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class TextWatchConfig extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG_BG_COLOR_CHOOSER = "bg_chooser";
    private static final String TAG_TEXT_COLOR_CHOOSER = "text_chooser";

    private View bgColorPreview;
    private View textColorPreview;

    private GoogleApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_watch_config);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        //Background Color
        findViewById(R.id.config_bgcolor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorDrawable currentColorDrawable = (ColorDrawable) bgColorPreview.getBackground();
                int currentColor = currentColorDrawable.getColor();
                ColorPickerDialogBuilder
                        .with(TextWatchConfig.this)
                        .setTitle("Choose Color")
                        .initialColor(currentColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                //Do something, maybe
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int selectedColor, Integer[] allColors) {
                                onColorSelected(selectedColor, TAG_BG_COLOR_CHOOSER);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .build()
                        .show();
            }
        });

        //Text Color
        findViewById(R.id.config_textcolor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorDrawable currentColorDrawable = (ColorDrawable) textColorPreview.getBackground();
                int currentColor = currentColorDrawable.getColor();
                ColorPickerDialogBuilder
                        .with(TextWatchConfig.this)
                        .setTitle("Choose Color")
                        .initialColor(currentColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                //Do something, maybe
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int selectedColor, Integer[] allColors) {
                                onColorSelected(selectedColor, TAG_TEXT_COLOR_CHOOSER);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .build()
                        .show();
            }
        });

        bgColorPreview = findViewById(R.id.bgcolor_preview);
        bgColorPreview.setBackgroundColor(0x00000000);
        textColorPreview = findViewById(R.id.textcolor_preview);
        textColorPreview.setBackgroundColor(0xFFFFFFFF);
    }

    @Override
    public void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_text_watch_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onColorSelected(int color, String tag) {
        PutDataMapRequest request = PutDataMapRequest.create("/text_watch_config");
        if(TAG_BG_COLOR_CHOOSER.equals(tag)) {
            bgColorPreview.setBackgroundColor(color);
            request.getDataMap().putInt("BG_COLOR", color);
        }
        else {
            textColorPreview.setBackgroundColor(color);
            request.getDataMap().putInt("TEXT_COLOR", color);
        }
        PutDataRequest dataRequest = request.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, dataRequest);
    }

    @Override
    public void onStop() {
        if(apiClient != null && apiClient.isConnected())
            apiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("TEXT_FACE", "connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("TEXT_FACE", "suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("TEXT_FACE", "failed");
    }
}
