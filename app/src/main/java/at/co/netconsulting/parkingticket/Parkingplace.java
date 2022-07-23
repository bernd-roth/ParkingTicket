package at.co.netconsulting.parkingticket;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class Parkingplace extends BaseActivity implements GeolocationPermissions.Callback {

    private ArrayList mSelectedItems = new ArrayList();
    private int choiceParking;
    private boolean isGPSEnabled;

    @JavascriptInterface
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parkingpplaces);

        isGPSEnabled = isGPSEnabled();
        if(!isGPSEnabled) {
            Toast.makeText(getApplicationContext(), R.string.gps_location, Toast.LENGTH_LONG).show();
        }
        showAlertDialog();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Parkingplace.this);
        alertDialog.setTitle(R.string.alert_gps_deactivated);
        String[] items = {String.valueOf(R.string.kurzparkzone_wien), String.valueOf(R.string.anrainerparken_wien)};
        int checkedItem = 1;
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

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
        alertDialog.setMultiChoiceItems(R.array.geojson, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            if(which==0) {
                                mSelectedItems.add(1);
                            } else if(which==1) {
                                mSelectedItems.add(2);
                            }
                        }
                        else if (mSelectedItems.contains(0)) {
                            mSelectedItems.remove(Integer.valueOf(1));
                        } else if(mSelectedItems.contains(1)) {
                            mSelectedItems.remove(Integer.valueOf(2));
                        }
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        if(mSelectedItems.size()==0) {
                            choiceParking = -1;
                        } else if(mSelectedItems.size()==1) {
                            if(mSelectedItems.contains(1)) {
                                choiceParking = 1;
                            } else if(mSelectedItems.contains(2)) {
                                choiceParking = 2;
                            }
                        } else if(mSelectedItems.size()==2) {
                            choiceParking = 3;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
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
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }
    }

}