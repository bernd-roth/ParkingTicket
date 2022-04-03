package at.co.netconsulting.parkingticket.pojo;

import java.util.List;

public class PairResult {
    private List<Long> nextParkingTicket;
    private List<Long> voiceMessage;

    public PairResult(List<Long> nextParkingTicket, List<Long> voiceMessage) {
        this.nextParkingTicket = nextParkingTicket;
        this.voiceMessage = voiceMessage;
    }

    public List<Long> getNextParkingTicket() {
        return nextParkingTicket;
    }

    public void setNextParkingTicket(List<Long> nextParkingTicket) {
        this.nextParkingTicket = nextParkingTicket;
    }

    public List<Long> getVoiceMessage() {
        return voiceMessage;
    }

    public void setVoiceMessage(List<Long> voiceMessage) {
        this.voiceMessage = voiceMessage;
    }
}
