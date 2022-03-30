package at.co.netconsulting.parkingticket;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import at.co.netconsulting.parkingticket.general.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText cityInput, telephoneNumberInput, licensePlateInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        initializeObjects();
        loadSharedPreferences("CITY");
        loadSharedPreferences("TELEPHONE_NUMBER");
        loadSharedPreferences("LICENSE_PLATE");
    }

    private void loadSharedPreferences(String sharedPref) {
        String input;
        SharedPreferences sh;

        switch(sharedPref) {
            case "CITY":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, "WIEN");
                cityInput.setText(input);
                break;
            case "TELEPHONE_NUMBER":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, "06646606000");
                telephoneNumberInput.setText(input);
                break;
            case "LICENSE_PLATE":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, "W-XYZ");
                licensePlateInput.setText(input);
                break;
        }
    }

    private void initializeObjects() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_settings);
        cityInput = findViewById(R.id.city);
        telephoneNumberInput = findViewById(R.id.telephone_number);
        licensePlateInput = findViewById(R.id.license_plate);
        saveButton = findViewById(R.id.saveInput);
        saveButton.setEnabled(true);
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }

    public void saveInput(View view) {
        boolean bool_city = isEmpty(cityInput);
        boolean bool_telephoneNumber = isEmpty(telephoneNumberInput);
        boolean bool_license_plate = isEmpty(licensePlateInput);

        if(bool_city && bool_telephoneNumber && bool_license_plate) {
            cityInput.setError("City field must not be empty");
            cityInput.requestFocus();
            telephoneNumberInput.setError("Telephone number field must not be empty");
            licensePlateInput.setError("License plate field must not be empty");
        } else if(bool_city){
            cityInput.setError("City field must not be empty");
            cityInput.requestFocus();
        } else if(bool_telephoneNumber){
            telephoneNumberInput.setError("Telephone number field must not be empty");
            telephoneNumberInput.requestFocus();
        } else if(bool_telephoneNumber){
            licensePlateInput.setError("License plate must not be empty");
            licensePlateInput.requestFocus();
        } else {
            saveSharedPreferences(cityInput.getText().toString(), "CITY");
            saveSharedPreferences(telephoneNumberInput.getText().toString(), "TELEPHONE_NUMBER");
            saveSharedPreferences(licensePlateInput.getText().toString(), "LICENSE_PLATE");
        }
    }

    private void saveSharedPreferences(String input, String sharedPref) {
        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Storing the key and its value as the data fetched from edittext
        myEdit.putString(sharedPref, input);

        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        myEdit.commit();
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;
        return true;
    }
}