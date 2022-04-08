package at.co.netconsulting.parkingticket.pojo;

import java.io.Serializable;
import java.util.TreeMap;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@RequiredArgsConstructor
public class ParkscheinCollection implements Serializable {
    private TreeMap<Long, Integer> nextParkingTickets;
    private String city;
    private String licensePlate;
    private String telephoneNumber;
    private boolean isStop;

    /**
     * Constructor for sending STOP signal by user instantly
     * @param city
     *        provide city
     * @param nextParkingTickets
     *        provide nextParkingTickets, will be calcualted automatically
     * @param licensePlate
     *        provide license plate, will be taken from SharedPreferences, defined in Settings
     * @param telephoneNumber
     *        provide telephone number, will be taken from SharedPreferences, defined in Settings
     */
    public ParkscheinCollection(String city, TreeMap<Long, Integer> nextParkingTickets, String licensePlate, String telephoneNumber, boolean isStop) {
        this.city = city;
        this.nextParkingTickets = nextParkingTickets;
        this.licensePlate = licensePlate;
        this.telephoneNumber = telephoneNumber;
        this.isStop = isStop;
    }

    //Getter
    public TreeMap<Long, Integer> getNextParkingTickets() {
        return nextParkingTickets;
    }

    public String getCity() {
        return city;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    //Setter
    public void setCity(String city) {
        this.city = city;
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
