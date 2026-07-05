package org.example.projekatjava.model;

import java.util.List;

/**
 * Željeznička stanica – specijalizacija {@link Station}.
 */
public class TrainStation extends Station {
    /**
     * @param name        naziv stanice (npr. "Z_1_2")
     * @param departures  lista voznih polazaka
     */
    public TrainStation(String name, List<Departure> departures){
        super(name, departures);
    }
}
