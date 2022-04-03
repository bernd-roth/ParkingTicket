package at.co.netconsulting.parkingticket.pojo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@RequiredArgsConstructor
public class ParkscheinCollection implements Serializable {
    private List<Long> nextParkingTickets;
    private List<Long> nextVoiceMessage;
    private String city;
    private int durationParkingticket;
    private String licensePlate;
    private String telephoneNumber;

    private boolean isStop;

    /**
     * Constructor for sending STOP signal by user instantly
     * @param city
     *        provide city
     * @param durationParkingticket
     *        provide duration of parkingticket, will be taken from duration view
     * @param nextParkingTickets
     *        provide nextParkingTickets, will be calcualted automatically
     * @param nextVoiceMessage
     *        provide nextVoiceMessage for providing text to speech, if SMS was not sent nor received
     * @param licensePlate
     *        provide license plate, will be taken from SharedPreferences, defined in Settings
     * @param telephoneNumber
     *        provide telephone number, will be taken from SharedPreferences, defined in Settings
     */
    public ParkscheinCollection(String city, Integer durationParkingticket, List<Long> nextParkingTickets, List<Long> nextVoiceMessage, String licensePlate, String telephoneNumber, boolean isStop) {
        this.city = city;
        this.durationParkingticket = durationParkingticket;
        this.nextParkingTickets = nextParkingTickets;
        this.nextVoiceMessage = nextVoiceMessage;
        this.licensePlate = licensePlate;
        this.telephoneNumber = telephoneNumber;
        this.isStop = isStop;
    }

    //Getter
    public List<Long> getNextParkingTickets() {
        return nextParkingTickets;
    }

    public List<Long> getNextVoiceMessage() {
        return nextVoiceMessage;
    }

    public String getCity() {
        return city;
    }

    public int getDurationParkingticket() {
        return durationParkingticket;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    //Setter
    public void setNextVoiceMessage(List<Long> nextVoiceMessage) {
        this.nextVoiceMessage = nextVoiceMessage;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDurationParkingticket(int durationParkingticket) {
        this.durationParkingticket = durationParkingticket;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
