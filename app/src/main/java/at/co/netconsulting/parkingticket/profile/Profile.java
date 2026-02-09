package at.co.netconsulting.parkingticket.profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;

import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.ui.ProfileScreenSetup;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ComposeView composeView = findViewById(R.id.compose_view);
        ProfileScreenSetup.init(composeView, this);
    }
}
