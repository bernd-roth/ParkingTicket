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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.general.StaticFields;

public class SettingsActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText telephoneNumberInput, licensePlateInput, waitMinutes;
    private Button saveButton;
    private RadioGroup radioGroupMinutes;
    private RadioButton radioButton1530, radioButton3015, radioButtonNoAlternateBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        initializeObjects();
        loadSharedPreferences(StaticFields.TELEPHONE_NUMBER);
        loadSharedPreferences(StaticFields.LICENSE_PLATE);
        loadSharedPreferences(StaticFields.WAIT_MINUTES);
        loadSharedPreferences(StaticFields.ALTERNATE_BOOKING);

        waitMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() != 0) {
                    try {
                        Integer hymnNumber = Integer.valueOf(Integer.valueOf(s.toString()));
                        if(!(hymnNumber>=0 && hymnNumber<6))
                            waitMinutes.setError("Please, set waiting minutes between 0 and 5, both are inclusive");
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            };

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadSharedPreferences(String sharedPref) {
        String input;
        SharedPreferences sh;

        switch(sharedPref) {
            case "TELEPHONE_NUMBER":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, StaticFields.DEFAULT_TELEPHONE_NUMBER);
                telephoneNumberInput.setText(input);
                break;
            case "LICENSE_PLATE":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, StaticFields.DEFAULT_NUMBER_PLATE);
                licensePlateInput.setText(input);
                break;
            case "WAIT_MINUTES":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                waitMinutes.setText(String.valueOf(sh.getInt(sharedPref, 0)));
                break;
            case "ALTERNATE_BOOKING":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                String noAlternateBooking = sh.getString(sharedPref, StaticFields.NO_ALTERNATE_BOOKING);
                if(noAlternateBooking.equals(StaticFields.FIFTEEN_THIRTY))
                    radioButton1530.setChecked(true);
                else if(noAlternateBooking.equals(StaticFields.THIRTY_FIFTEEN))
                    radioButton3015.setChecked(true);
                else if(noAlternateBooking.equals(StaticFields.NO_ALTERNATE_BOOKING))
                    radioButtonNoAlternateBooking.setChecked(true);
                break;
        }
    }

    private void initializeObjects() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_settings);
        telephoneNumberInput = findViewById(R.id.telephone_number);
        licensePlateInput = findViewById(R.id.license_plate);
        waitMinutes = findViewById(R.id.waitMinutes);
        saveButton = findViewById(R.id.saveInput);
        saveButton.setEnabled(true);
        radioButton1530 = findViewById(R.id.radioButton1530);
        radioButton3015 = findViewById(R.id.radioButton3015);
        radioButtonNoAlternateBooking = findViewById(R.id.radioButtonNoAlternateBooking);
        radioGroupMinutes = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroupMinutes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
                    case R.id.radioButton1530:
                        saveSharedPreferences(StaticFields.FIFTEEN_THIRTY, "ALTERNATE_BOOKING");
                        break;
                    case R.id.radioButton3015:
                        saveSharedPreferences(StaticFields.THIRTY_FIFTEEN, "ALTERNATE_BOOKING");
                        break;
                    case R.id.radioButtonNoAlternateBooking:
                        saveSharedPreferences(StaticFields.NO_ALTERNATE_BOOKING, "ALTERNATE_BOOKING");
                        break;
                }
            }
        });
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }

    public void saveInput(View view) {
        boolean bool_telephoneNumber = isEmpty(telephoneNumberInput);
        boolean bool_license_plate = isEmpty(licensePlateInput);

        if(bool_telephoneNumber && bool_license_plate) {
            telephoneNumberInput.setError("Telephone number field must not be empty");
            licensePlateInput.setError("License plate field must not be empty");
            telephoneNumberInput.requestFocus();
        } else if(bool_telephoneNumber){
            telephoneNumberInput.setError("Telephone number field must not be empty");
            telephoneNumberInput.requestFocus();
        } else if(bool_telephoneNumber){
            licensePlateInput.setError("License plate must not be empty");
            licensePlateInput.requestFocus();
        } else {
            saveSharedPreferences(telephoneNumberInput.getText().toString(), StaticFields.TELEPHONE_NUMBER);
            saveSharedPreferences(licensePlateInput.getText().toString(), StaticFields.LICENSE_PLATE);
            String inputWaitMinutes = waitMinutes.getText().toString();
            if(inputWaitMinutes==null || inputWaitMinutes.isEmpty()) {
                waitMinutes.setText("0");
                saveSharedPreferencesAsInteger(Integer.valueOf(waitMinutes.getText().toString()), StaticFields.WAIT_MINUTES);
            } else {
                saveSharedPreferencesAsInteger(Integer.valueOf(waitMinutes.getText().toString()), StaticFields.WAIT_MINUTES);
            }
        }
    }

    private void saveSharedPreferencesAsInteger(Integer input, String sharedPref) {
        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Storing the key and its value as the data fetched from edittext
        myEdit.putInt(sharedPref, input);

        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        myEdit.commit();
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