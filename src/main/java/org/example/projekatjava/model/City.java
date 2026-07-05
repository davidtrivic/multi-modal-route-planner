package org.example.projekatjava.model;

/**
 * Grad sa pripadajućom autobuskom i željezničkom stanicom.
 */
public class City {
    private String name;
    private BusStation busStation;
    private TrainStation trainStation;

    /**
     * @param name          naziv grada (npr. "G_0_0")
     * @param busStation    autobuska stanica (može biti {@code null})
     * @param trainStation  željeznička stanica (može biti {@code null})
     */
    public City(String name, BusStation busStation, TrainStation trainStation){
        this.name = name;
        this.busStation = busStation;
        this.trainStation = trainStation;
    }

    /** @return naziv grada. */
    public String getName() { return name; }

    /** @param name novi naziv grada. */
    public void setName(String name) { this.name = name; }

    /** @return autobuska stanica ili {@code null}. */
    public BusStation getBusStation() { return busStation; }

    /** @param busStation nova autobuska stanica. */
    public void setBusStation(BusStation busStation) { this.busStation = busStation; }

    /** @return željeznička stanica ili {@code null}. */
    public TrainStation getTrainStation() { return trainStation; }

    /** @param trainStation nova željeznička stanica. */
    public void setTrainStation(TrainStation trainStation) { this.trainStation = trainStation; }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", busStation=" + busStation +
                ", trainStation=" + trainStation +
                '}';
    }
}
