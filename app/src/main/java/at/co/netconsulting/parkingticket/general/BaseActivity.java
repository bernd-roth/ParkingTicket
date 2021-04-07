package at.co.netconsulting.parkingticket.general;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import at.co.netconsulting.parkingticket.LogUserInputAndOtherMessagesActivity;
import at.co.netconsulting.parkingticket.MainActivity;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.SettingsActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainmenu:
                Intent intentMainActivity = new Intent(BaseActivity.this, MainActivity.class);
                startActivity(intentMainActivity);
                return true;
            case R.id.settings:
                Intent intentSettings = new Intent(BaseActivity.this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.logging:
                Intent intentLog = new Intent(BaseActivity.this, LogUserInputAndOtherMessagesActivity.class);
                startActivity(intentLog);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
