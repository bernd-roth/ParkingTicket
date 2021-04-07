package at.co.netconsulting.parkingticket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Map;

import at.co.netconsulting.parkingticket.general.BaseActivity;

import static at.co.netconsulting.parkingticket.statics.StaticVariables.*;

public class SettingsActivity extends BaseActivity {

    EditText editTextTelephoneNumber, editTextLicensePlate;
    Button buttonSaveTelephoneNumber, buttonSaveLicensePlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialComponents();
        loadPrefs();
    }

    private void initialComponents() {
        editTextTelephoneNumber = (EditText) findViewById(R.id.editTextTelephoneNumber);
        editTextLicensePlate = (EditText) findViewById(R.id.editTextLicensePlate);
        buttonSaveTelephoneNumber = (Button) findViewById(R.id.buttonSaveTelephoneNumber);
        buttonSaveLicensePlate = (Button) findViewById(R.id.buttonSaveLicensePlate);
    }

    public void control_handler(View view) {
        Button whichControlWasClicked = (Button)view;

        switch(whichControlWasClicked.getText().toString()) {
            case BUTTON_TELEPHONE_NUMBER:
                saveTelephoneNumber();
                break;
            case BUTTON_LICENSE_PLATE:
                saveLicensePlate();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + whichControlWasClicked.getText().toString());
        }
    }

    private void saveTelephoneNumber() {
        SharedPreferences sharedpreferences = getSharedPreferences("PREF_TELEPHONE_NUMBER", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("PREF_TELEPHONE_NUMBER", editTextTelephoneNumber.getText().toString());
        editor.commit();
    }

    private void saveLicensePlate() {
        SharedPreferences sharedpreferences = getSharedPreferences("PREF_LICENSE_PLATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("PREF_LICENSE_PLATE", editTextLicensePlate.getText().toString());
        editor.commit();
    }

    private void loadPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences("PREF_TELEPHONE_NUMBER", MODE_PRIVATE);
        String telephone_number = sharedPreferences.getString("PREF_TELEPHONE_NUMBER", "06646606000");
        editTextTelephoneNumber.setText(String.valueOf(telephone_number));

        sharedPreferences = getSharedPreferences("PREF_LICENSE_PLATE", MODE_PRIVATE);
        String license_plate = sharedPreferences.getString("PREF_LICENSE_PLATE", "W-12345678");
        editTextLicensePlate.setText(license_plate);
    }
}