package org.example.projekatjava.model;

import java.util.Objects;

/**
 * Jedan polazak (dionica) između dva grada.
 * <p>
 * Važna napomena: {@code from} je ID stanice (npr. "A_1_2" ili "Z_1_2"),
 * dok je {@code to} ID grada (npr. "G_1_2").
 */
public class Departure {
    /** Tip prevoza: "autobus" ili "voz". */
    private String type;
    /** Iz koje stanice se polazi (npr. "A_1_2"). */
    private String from;
    /** U koji grad se dolazi (npr. "G_1_2"). */
    private String to;
    /** Termin polaska u formatu "HH:mm" (lokalno dnevno vrijeme). */
    private String departureTime;
    /** Trajanje vožnje u minutama. */
    private int duration;
    /** Cijena dionice. */
    private int price;
    /** Minimalno vrijeme za transfer nakon dolaska u grad, u minutama. */
    private int minTransferTime;

    /**
     * @param type            "autobus" ili "voz"
     * @param from            polazna stanica (A_x_y ili Z_x_y)
     * @param to              odredišni grad (G_x_y)
     * @param departureTime   vrijeme polaska u "HH:mm"
     * @param duration        trajanje u minutama
     * @param price           cijena dionice
     * @param minTransferTime minimalni transfer u minutama u odredišnom gradu
     */
    public Departure(String type, String from, String to, String departureTime,
                     int duration, int price, int minTransferTime){
        this.type = type;
        this.from = from;
        this.to = to;
        this.departureTime = departureTime;
        this.duration = duration;
        this.price = price;
        this.minTransferTime = minTransferTime;
    }

    /** @return tip prevoza. */
    public String getType(){ return type; }
    /** @param type novi tip prevoza. */
    public void setType(String type){ this.type = type; }

    /** @return polazna stanica (A_x_y ili Z_x_y). */
    public String getFrom() { return from; }
    /** @param from nova polazna stanica. */
    public void setFrom(String from) { this.from = from; }

    /** @return odredišni grad (G_x_y). */
    public String getTo() { return to; }
    /** @param to novi odredišni grad. */
    public void setTo(String to) { this.to = to; }

    /** @return vrijeme polaska u formatu "HH:mm". */
    public String getDepartureTime() { return departureTime; }
    /** @param departureTime novo vrijeme polaska. */
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    /** @return trajanje vožnje u minutama. */
    public int getDuration() { return duration; }
    /** @param duration novo trajanje u minutama. */
    public void setDuration(int duration) { this.duration = duration; }

    /** @return cijena dionice. */
    public int getPrice() { return price; }
    /** @param price nova cijena dionice. */
    public void setPrice(int price) { this.price = price; }

    /** @return minimalno vrijeme transfera u minutama. */
    public int getMinTransferTime() { return minTransferTime; }
    /** @param minTransferTime novi minimalni transfer u minutama. */
    public void setMinTransferTime(int minTransferTime) { this.minTransferTime = minTransferTime; }

    @Override
    public String toString() {
        return "Departure{" +
                "type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", departureTime='" + departureTime + '\'' +
                ", duration=" + duration +
                ", price=" + price +
                ", minTransferTime=" + minTransferTime +
                '}';
    }

    /** Dva polaska su jednaka ako su im sva polja jednaka. */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Departure departure)) return false;
        return duration == departure.duration
                && price == departure.price
                && minTransferTime == departure.minTransferTime
                && Objects.equals(type, departure.type)
                && Objects.equals(from, departure.from)
                && Objects.equals(to, departure.to)
                && Objects.equals(departureTime, departure.departureTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, from, to, departureTime, duration, price, minTransferTime);
    }
}
