package at.co.netconsulting.parkingticket;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import at.co.netconsulting.parkingticket.general.BaseActivity;

public class Parkingplace extends BaseActivity implements GeolocationPermissions.Callback {

    @JavascriptInterface
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parkingpplaces);

        final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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
            geo.onGeolocationPermissionsShowPrompt(origin, this);
            browser.getSettings().setJavaScriptEnabled(true);

            browser.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    String messagea, messageb, messagec, messaged, messagee, messagef, messageg = null;
                    browser.loadUrl("javascript:AndroidFunction(" + null + "," + null + "," + null + "," + null + "," + null + "," + null + "," + null + "," + 12 + ")");
                    }
                });
            browser.loadUrl("file:///android_asset/index.html");
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