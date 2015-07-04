package me.kevinrenner.textface;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

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

    private View bgColorPreview;
    private View textColorPreview;

    private GoogleApiClient apiClient;

    SharedPreferences preferences;

    AdView mAdView;

    private int mBGColor;
    private int mTextColor;
    private String mTextFont;
    private boolean mShowDate;

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
                                mBGColor = selectedColor;
                                bgColorPreview.setBackgroundColor(selectedColor);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(getString(R.string.saved_bg_color), selectedColor);
                                editor.commit();
                                putData();
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
                                mTextColor = selectedColor;
                                textColorPreview.setBackgroundColor(selectedColor);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(getString(R.string.saved_text_color), selectedColor);
                                editor.commit();
                                putData();
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
                mTextFont = font;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.saved_font), font);
                editor.commit();
                putData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });

        //Show Date
        findViewById(R.id.dateCheckBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                if(checkBox.isChecked())
                    mShowDate = true;
                else
                    mShowDate = false;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("DATE", mShowDate);
                editor.commit();
                putData();
            }
        });

        bgColorPreview = findViewById(R.id.bgcolor_preview);
        bgColorPreview.setBackgroundColor(preferences.getInt(getString(R.string.saved_bg_color), Color.parseColor(getString(R.string.default_background))));
        mBGColor = preferences.getInt(getString(R.string.saved_bg_color), Color.parseColor(getString(R.string.default_background)));
        textColorPreview = findViewById(R.id.textcolor_preview);
        textColorPreview.setBackgroundColor(preferences.getInt(getString(R.string.saved_text_color), Color.parseColor(getString(R.string.default_text))));
        mTextColor = preferences.getInt(getString(R.string.saved_text_color), Color.parseColor(getString(R.string.default_text)));
        Log.d("", mTextColor + "");
        fontSpinner.setSelection(adapter.getPosition(preferences.getString(getString(R.string.saved_font), getString(R.string.default_font))));
        mTextFont = preferences.getString(getString(R.string.saved_font), getString(R.string.default_font));
        mShowDate = preferences.getBoolean("DATE", false);
        CheckBox checkBox = (CheckBox) findViewById(R.id.dateCheckBox);
        checkBox.setChecked(preferences.getBoolean("DATE", false));
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

        if (id == R.id.action_license) {
            AlertDialog dialog =new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.action_license))
                    .setMessage(Html.fromHtml(getResources().getString(R.string.licenses)))
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            dialog.show();
            ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            return true;

        }
        if(id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(getString(R.string.about) + BuildConfig.VERSION_NAME)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
        }
        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void putData() {
        PutDataMapRequest request = PutDataMapRequest.create("/text_watch_config");
        request.getDataMap().putInt("BG_COLOR", mBGColor);
        request.getDataMap().putInt("TEXT_COLOR", mTextColor);
        request.getDataMap().putString("FONT", mTextFont);
        request.getDataMap().putBoolean("DATE", mShowDate);
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
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
