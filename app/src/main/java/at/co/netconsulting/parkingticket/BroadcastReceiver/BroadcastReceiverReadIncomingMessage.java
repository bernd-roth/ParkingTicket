package at.co.netconsulting.parkingticket.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class BroadcastReceiverReadIncomingMessage extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String str = "";
            if (bundle != null)
            {
                // Retrieve the SMS.
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i=0; i<msgs.length; i++)
                {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    // In case of a particular App / Service.
                    //if(msgs[i].getOriginatingAddress().equals("+91XXX"))
                    //{
                    str += "SMS from " + msgs[i].getOriginatingAddress();
                    str += " :";
                    str += msgs[i].getMessageBody().toString();
                    str += "n";
                    //}
                }
                // Display the SMS as Toast.
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        }
    }
