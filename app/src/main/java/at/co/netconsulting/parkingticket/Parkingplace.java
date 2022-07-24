package at.co.netconsulting.parkingticket;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;

import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.general.StaticFields;

public class Parkingplace extends BaseActivity implements GeolocationPermissions.Callback {

    private ArrayList mSelectedItems = new ArrayList();
    private int choiceParking;
    private boolean isGPSEnabled, isCheckedParkingZonesVienna, isCheckedResidentParkingVienna, isCheckedGaragesVienna;
    private String[] items;
    private boolean[] itemsChecked;

    @JavascriptInterface
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parkingpplaces);

        loadSharedPreferences(StaticFields.PARKING_ZONES_VIENNA);
        loadSharedPreferences(StaticFields.RESIDENT_PARKING_VIENNA);
        loadSharedPreferences(StaticFields.GARAGES_VIENNA);
        initializeObjects();

        isGPSEnabled = isGPSEnabled();
        if(!isGPSEnabled) {
            Toast.makeText(getApplicationContext(), R.string.gps_location, Toast.LENGTH_LONG).show();
            finish();
        } else {
            showAlertDialog();
        }
    }

    private void initializeObjects() {
        items = getResources().getStringArray(R.array.geojson);
        itemsChecked = new boolean[]{isCheckedParkingZonesVienna, isCheckedResidentParkingVienna, isCheckedGaragesVienna};
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Parkingplace.this);
        alertDialog.setTitle(R.string.alert_gps_deactivated);

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);

                final WebView browser;
                browser=(WebView)findViewById(R.id.webView);

                final WebSettings webSettings = browser.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setAppCacheEnabled(true);
                webSettings.setDatabaseEnabled(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                webSettings.setGeolocationEnabled(true);

                GeoClient geo = new GeoClient();
                browser.setWebChromeClient(geo);
                String origin = "";
                geo.onGeolocationPermissionsShowPrompt(origin, Parkingplace.this);
                browser.getSettings().setJavaScriptEnabled(true);

                browser.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        browser.loadUrl("javascript:init(" + choiceParking + "," + "12)");
                    }
                });
                browser.loadUrl("file:///android_asset/index.html");
            }
        });
        alertDialog.setMultiChoiceItems(R.array.geojson, itemsChecked,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(which==0) {
                            if (mSelectedItems.contains(1)) {
                                removeSelectedItem(1, StaticFields.PARKING_ZONES_VIENNA);
                            } else {
                                addingSelectedItem(1, StaticFields.PARKING_ZONES_VIENNA);
                            }
                        } else if(which==1) {
                            if(mSelectedItems.contains(2)) {
                                removeSelectedItem(2, StaticFields.RESIDENT_PARKING_VIENNA);
                            } else {
                                addingSelectedItem(2, StaticFields.RESIDENT_PARKING_VIENNA);
                            }
                        } else if(which==2) {
                            if(mSelectedItems.contains(3)) {
                                removeSelectedItem(3, StaticFields.GARAGES_VIENNA);
                            } else {
                                addingSelectedItem(3, StaticFields.GARAGES_VIENNA);
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if(mSelectedItems.size()==0) {
                            //empty map will be returned
                            choiceParking = -1;
                        } else if(mSelectedItems.size()==1) {
                            if(mSelectedItems.contains(1)) {
                                choiceParking = 1;
                            } else if(mSelectedItems.contains(2)) {
                                choiceParking = 2;
                            } else if(mSelectedItems.contains(3)) {
                                choiceParking = 3;
                            }
                        } else if(mSelectedItems.size()==2) {
                            if(mSelectedItems.contains(1) && mSelectedItems.contains(2)) {
                                choiceParking = 12;
                            } else if(mSelectedItems.contains(1) && mSelectedItems.contains(3)) {
                                choiceParking = 13;
                            } else if(mSelectedItems.contains(2) && mSelectedItems.contains(3)) {
                                choiceParking = 23;
                            }
                        } else if(mSelectedItems.size()==3) {
                            choiceParking = 123;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void removeSelectedItem(int i, String parkingOrResidentOrGarage) {
        mSelectedItems.remove(Integer.valueOf(i));
        SharedPreferences preferences = getSharedPreferences(parkingOrResidentOrGarage, 0);
        preferences.edit().remove(parkingOrResidentOrGarage).commit();
    }

    private void addingSelectedItem(int i, String parkingOrResidentorGarage) {
        mSelectedItems.add(i);
        saveSharedPreferences(true, parkingOrResidentorGarage);
    }

    private boolean isGPSEnabled() {
        final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void invoke(String origin, boolean allow, boolean remember) {
    }
    final class GeoClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }
    }
    private void loadSharedPreferences(String sharedPref) {
        SharedPreferences sh;

        switch(sharedPref) {
            case "PARKING_ZONES_VIENNA":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                isCheckedParkingZonesVienna = sh.getBoolean(sharedPref, false);
                if(isCheckedParkingZonesVienna) {
                    mSelectedItems.add(1);
                }
                break;
            case "RESIDENT_PARKING_VIENNA":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                isCheckedResidentParkingVienna = sh.getBoolean(sharedPref, false);
                if(isCheckedResidentParkingVienna) {
                    mSelectedItems.add(2);
                }
                break;
            case "GARAGES_VIENNA":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                isCheckedGaragesVienna = sh.getBoolean(sharedPref, false);
                if(isCheckedGaragesVienna) {
                    mSelectedItems.add(3);
                }
                break;
        }
    }
    private void saveSharedPreferences(boolean input, String sharedPref) {
        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Storing the key and its value as the data fetched from edittext
        myEdit.putBoolean(sharedPref, input);

        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        myEdit.commit();
    }
}