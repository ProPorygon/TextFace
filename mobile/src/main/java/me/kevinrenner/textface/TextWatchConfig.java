package me.kevinrenner.textface;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

    SharedPreferences preferences;

    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = TextWatchConfig.this.getPreferences(Context.MODE_PRIVATE);
        setContentView(R.layout.activity_text_watch_config);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title));

        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());

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
                        .lightnessSliderOnly()
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                //Do nothing
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
                                //Do nothing
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
                        .lightnessSliderOnly()
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                //Do nothing
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
                                //Do nothing
                            }
                        })
                        .build()
                        .show();
            }
        });

        //Text Font
        Spinner fontSpinner = (Spinner) findViewById(R.id.font_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.fonts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(adapter);
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String font = (String) parent.getItemAtPosition(pos);
                PutDataMapRequest request = PutDataMapRequest.create("/text_watch_config");
                request.getDataMap().putString("FONT", font);
                PutDataRequest dataRequest = request.asPutDataRequest();
                Wearable.DataApi.putDataItem(apiClient, dataRequest);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.saved_font), font);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });

        bgColorPreview = findViewById(R.id.bgcolor_preview);
        bgColorPreview.setBackgroundColor(preferences.getInt(getString(R.string.saved_bg_color), R.string.default_background));
        textColorPreview = findViewById(R.id.textcolor_preview);
        textColorPreview.setBackgroundColor(preferences.getInt(getString(R.string.saved_text_color), R.string.default_text));
        fontSpinner.setSelection(adapter.getPosition(preferences.getString(getString(R.string.saved_font), getString(R.string.default_font))));
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
        SharedPreferences.Editor editor = preferences.edit();
        if(TAG_BG_COLOR_CHOOSER.equals(tag)) {
            bgColorPreview.setBackgroundColor(color);
            request.getDataMap().putInt("BG_COLOR", color);
            editor.putInt(getString(R.string.saved_bg_color), color);
        }
        else {
            textColorPreview.setBackgroundColor(color);
            request.getDataMap().putInt("TEXT_COLOR", color);
            editor.putInt(getString(R.string.saved_text_color), color);
        }
        PutDataRequest dataRequest = request.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, dataRequest);
        editor.commit();
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
