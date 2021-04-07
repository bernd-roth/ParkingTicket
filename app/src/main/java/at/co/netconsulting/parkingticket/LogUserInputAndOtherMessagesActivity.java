package at.co.netconsulting.parkingticket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import at.co.netconsulting.parkingticket.CalculateBookings.CalculateBookings;
import at.co.netconsulting.parkingticket.CalculateBookings.LogMessages;
import at.co.netconsulting.parkingticket.general.BaseActivity;

public class LogUserInputAndOtherMessagesActivity extends BaseActivity {
    TextView tv;
    Long hour, minute, second;
    String s = "", formattedMinute;
    int counter = 0;
    LogMessages logMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_user_input_and_other_messages);

        TextView tv = (TextView)findViewById(R.id.textViewInputMessage);

        //Calculate bookings treemap will be displayed
        TreeMap<Long, Integer> map = CalculateBookings.getTreeMap();
        for(Map.Entry<Long, Integer> entry : map.entrySet()) {
            Long key = entry.getKey();
            Integer value = entry.getValue();

            second = (key / 1000) % 60;
            minute = (key / (1000 * 60)) % 60;
            hour = (key / (1000 * 60 * 60)) % 24;

            if(String.valueOf(minute).length()==1)
                formattedMinute = "0" + Long.toString(minute);
            else
                formattedMinute = Long.toString(minute);

            if(counter==0){
                s += value + " Minuten Parkschein wird um " + hour + ":" + formattedMinute + " gebucht\n";
                counter++;
            } else
                s += value + " Minuten Parkschein wird um " + hour + ":" + formattedMinute + " gebucht\n";
        }

        //LogMessages will be displayed
        logMessages = new LogMessages();
        ArrayList log = logMessages.getLogMessages();

        for(int i = 0; i<log.size(); i++)
            s += log.get(i) + "\n";

        tv.setText(s);
    }
}