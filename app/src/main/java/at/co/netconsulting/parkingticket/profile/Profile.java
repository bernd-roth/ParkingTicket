package at.co.netconsulting.parkingticket.profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import at.co.netconsulting.parkingticket.R;

public class Profile extends AppCompatActivity {

    EditText profileName, carModel, city, numberPlate, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createObjects();
    }

    private void createObjects() {
        profileName = findViewById(R.id.profileName);
        carModel = findViewById(R.id.carModel);
        city = findViewById(R.id.city);
        numberPlate = findViewById(R.id.numberPlate);
        email = findViewById(R.id.email);
    }
}