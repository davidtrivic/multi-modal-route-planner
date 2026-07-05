package org.example.projekatjava.model;

import java.util.List;

/**
 * Autobuska stanica – specijalizacija {@link Station}.
 */
public class BusStation extends Station {

    /**
     * @param name        naziv stanice (npr. "A_1_2")
     * @param departures  lista autobusnih polazaka
     */
    public BusStation(String name, List<Departure> departures){
        super(name, departures);
    }
}
