package org.example.projekatjava.model;

import java.util.*;

/**
 * Apstraktna stanica (autobuska ili željeznička) sa listom polazaka.
 * Specifične tipove definišu {@link BusStation} i {@link TrainStation}.
 */
public abstract class Station {
    private String name;
    private List<Departure> departures = new ArrayList<>();

    /**
     * @param name        naziv stanice (npr. "A_0_0" ili "Z_0_0")
     * @param departures  početna lista polazaka (može biti prazna)
     */
    public Station(String name, List<Departure> departures){
        this.name = name;
        this.departures = departures;
    }

    /** @return naziv stanice. */
    public String getName() { return name; }

    /** @param name novi naziv stanice. */
    public void setName(String name) { this.name = name; }

    /** @return neizmjenjiv ili izmjenjiv spisak polazaka (zavisno od implementacije pozivaoca). */
    public List<Departure> getDepartures() { return departures; }

    /** @param departures nova lista polazaka. */
    public void setDepartures(List<Departure> departures) { this.departures = departures; }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", departures=" + departures +
                '}';
    }
}
