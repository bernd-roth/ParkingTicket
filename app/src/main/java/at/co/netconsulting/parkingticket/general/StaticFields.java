package at.co.netconsulting.parkingticket.general;

public class StaticFields {
    //General static fields
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final long MAX_ONE_DAY = 86400000;
    public static final int MIN_ONE_DAY_MINUTES = 0;
    public static final int MAX_ONE_DAY_MINUTES = 1440;
    public static final String STOP_SMS = "Stopp";
    public static final String PARKSCHEIN_POJO = "Parkschein_Pojo";
    public static final String VOICE_MESSAGE = "voiceMessage";
    public static final String DEFAULT_TELEPHONE_NUMBER = "06646606000";
    public static final String DEFAULT_CITY = "Wien";
    public static final String DEFAULT_NUMBER_PLATE = "W-XYZ";
    //Shared Preferences
    public static final String CITY = "CITY";
    public static final String TELEPHONE_NUMBER = "TELEPHONE_NUMBER";
    public static final String LICENSE_PLATE = "LICENSE_PLATE";
    public static final String WAIT_MINUTES = "WAIT_MINUTES";
    public static final String NO_ALTERNATE_BOOKING = "NO_ALTERNATE_BOOKING";
    public static final String FIFTEEN_THIRTY = "FIFTEEN_THIRTY";
    public static final String THIRTY_FIFTEEN = "THIRTY_FIFTEEN";
    public static final String ALTERNATE_BOOKING = "ALTERNATE_BOOKING";
    public static final String NEXT_PARKINGTICKET = "NEXT_PARKINGTICKET";
    public static final String STOP_TIMER_CHECKBOX = "STOP_TIMER_CHECKBOX";
    public static final String DIALOG_YES = "DIALOG_YES";
    public static final String DIALOG_NO = "DIALOG_NO";
    public static final String ALERT_DIALOG = "ALERT_DIALOG";
    //PendingIntent
    public static final int REQUEST_CODE = 0;
    //ForegroundService
    public static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    //Parking zones, Resident parking, Garages
    public static final String PARKING_ZONES_VIENNA = "PARKING_ZONES_VIENNA";
    public static final String RESIDENT_PARKING_VIENNA = "RESIDENT_PARKING_VIENNA";
    public static final String GARAGES_VIENNA = "GARAGES_VIENNA";
    public static final String NO_PARKSCHEIN_RECEIVED = "NO_VOICE_MESSAGE_RECEIVED";
    public static final String VOICE_MESSAGE_PARKING_TICKET_EXPIRED = "VOICE_MESSAGE_PARKING_TICKET_EXPIRED";
}