package at.co.netconsulting.parkingticket.general;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import at.co.netconsulting.parkingticket.MainActivity;
import at.co.netconsulting.parkingticket.Parkingplace;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.SettingsActivity;

public class BaseActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_main) {
            intent = new Intent(this, MainActivity.class);
        } else if (itemId == R.id.action_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (itemId == R.id.action_parkingplaces_overview) {
            intent = new Intent(this, Parkingplace.class);
        } else {
            return super.onOptionsItemSelected(item);
        }
        this.startActivity(intent);
        return true;
    }
}
