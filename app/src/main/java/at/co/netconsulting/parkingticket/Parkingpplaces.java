package at.co.netconsulting.parkingticket;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import at.co.netconsulting.parkingticket.databinding.ActivityParkingpplacesBinding;
import at.co.netconsulting.parkingticket.general.BaseActivity;

public class Parkingpplaces extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_parkingpplaces);
    }
}