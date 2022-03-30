package at.co.netconsulting.parkingticket;

import java.util.ArrayList;
import java.util.List;

public class SingletonParkscheinTicket {
    private static SingletonParkscheinTicket mInstance;
    private ArrayList<List<Long>> list = null;

    public static SingletonParkscheinTicket getInstance() {
        if(mInstance == null)
            mInstance = new SingletonParkscheinTicket();

        return mInstance;
    }

    private SingletonParkscheinTicket() {
        list = new ArrayList<List<Long>>();
    }
    // retrieve array from anywhere
    public ArrayList<List<Long>> getArray() {
        return this.list;
    }
    //Add element to array
    public void addToArray(List<Long> value) {
        list.add(value);
    }
}
