package at.co.netconsulting.parkingticket;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import at.co.netconsulting.parkingticket.general.BaseActivity;

public class Parkingpplaces extends BaseActivity {

    private WebView wv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_parkingpplaces);

        initializeObjects();
    }

    private void initializeObjects() {
        wv1=(WebView)findViewById(R.id.webView);
    }
}