package at.co.netconsulting.parkingticket.general;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import at.co.netconsulting.parkingticket.MainActivity;
import at.co.netconsulting.parkingticket.Parkingplace;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.SettingsActivity;

public class BaseActivity extends Activity {

    private Intent intent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main:
                intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.action_parkingplaces_overview:
                intent = new Intent(this, Parkingplace.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
