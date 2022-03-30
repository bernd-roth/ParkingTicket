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
    private String city;
    private int durationParkingticket;
    private String licensePlate;
    private String telephoneNumber;
    private String cancel;

    public ParkscheinCollection(String city, String licensePlate, String telephoneNumber, String cancel) {
        this.city = city;
        this.licensePlate = licensePlate;
        this.telephoneNumber = telephoneNumber;
        this.cancel = cancel;
    }

    public ParkscheinCollection(String city, Integer durationParkingticket, List<Long> nextParkingTickets, String licensePlate, String telephoneNumber) {
        this.city = city;
        this.durationParkingticket = durationParkingticket;
        this.nextParkingTickets = nextParkingTickets;
        this.licensePlate = licensePlate;
        this.telephoneNumber = telephoneNumber;
    }

    public List<Long> getNextParkingTickets() {
        return nextParkingTickets;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDurationParkingticket() {
        return durationParkingticket;
    }

    public void setDurationParkingticket(int durationParkingticket) {
        this.durationParkingticket = durationParkingticket;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getCancel() {
        return cancel;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }
}
