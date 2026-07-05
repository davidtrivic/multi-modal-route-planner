package org.example.projekatjava.algorithm;

import org.example.projekatjava.model.Departure;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reprezentuje rezultat bilo kog pretraživačkog algoritma rute
 * (Dijkstra po vremenu/cijeni, BFS po presjedanjima).
 * <p>
 * Sadrži izabranu putanju (lista dionica), ukupnu cijenu, apsolutni
 * minut dolaska u ciljni grad (bez završnog transfera) i broj presjedanja.
 * <p>
 * Klasa takođe nudi skup pomoćnih funkcija
 * za računanje dolazaka, trajanja i sabiranja cijena koji se koriste
 * u svim algoritmima da bi rezultati bili konzistentni.
 */
public final class Result {
    /** Izabrane dionice redom. */
    private List<Departure> path;
    /** Ukupna cijena rute. */
    private int totalPrice;
    /** Apsolutni minut dolaska u ciljni GRAD (bez završnog transfera). */
    private long arrivalAbsInGoalCity;
    /** Broj presjedanja (= broj dionica - 1, minimalno 0). */
    private int transfers;

    /**
     * Kreira rezultat.
     *
     * @param path                   lista dionica; čuva se kao nepromjenjiva kopija
     * @param totalPrice             ukupna cijena
     * @param arrivalAbsInGoalCity   apsolutni minut dolaska u ciljni grad
     * @param transfers              broj presjedanja
     */
    public Result(List<Departure> path, int totalPrice, long arrivalAbsInGoalCity, int transfers) {
        this.path = path == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(path));
        this.totalPrice = totalPrice;
        this.arrivalAbsInGoalCity = arrivalAbsInGoalCity;
        this.transfers = transfers;
    }


    private static final int DAY = 24 * 60;

    /**
     * Računa sumu cijena nad datom putanjom.
     *
     * @param path dionice
     * @return zbir cijena, ili 0 ako je putanja null
     */
    public static int sumPrice(List<Departure> path) {
        if (path == null) return 0;
        return path.stream().mapToInt(Departure::getPrice).sum();
    }

    /**
     * Simulira apsolutno vrijeme dolaska na kraju putanje, uključujući prelazak dana
     * i minimalne transfere između dionica.
     *
     * @param path dionice redom
     * @return apsolutni minut dolaska; {@link Long#MAX_VALUE} za praznu putanju
     */
    public static long simulateArrivalAbs(List<Departure> path) {
        if (path == null || path.isEmpty()) return Long.MAX_VALUE;
        long ready = 0;
        for (Departure d : path) {
            long depAbs = nextTimeAtOrAfter(d.getDepartureTime(), ready);
            long arrAbs = depAbs + d.getDuration();
            ready = arrAbs + d.getMinTransferTime();
        }
        Departure last = path.get(path.size() - 1);
        long depAbs = nextTimeAtOrAfter(last.getDepartureTime(), ready - last.getMinTransferTime() - last.getDuration());
        return depAbs + last.getDuration();
    }

    /**
     * Minut polaska prve dionice.
     *
     * @param path dionice
     * @return Polazni minut ili 0 za praznu putanju
     */
    public static long firstDepartureAbs(List<Departure> path) {
        if (path == null || path.isEmpty()) return 0;
        Departure first = path.get(0);
        return nextTimeAtOrAfter(first.getDepartureTime(), 0);
    }

    /**
     * Ukupno trajanje putovanja u minutama
     * (od polaska prve dionice do dolaska u cilj).
     *
     * @return trajanje u minutama (nikad negativno)
     */
    public long totalTravelMinutes() {
        if (path == null || path.isEmpty()) return 0;
        long startAbs = firstDepartureAbs(path);
        long total = arrivalAbsInGoalCity - startAbs;
        return Math.max(total, 0);
    }

    /**
     * Najraniji apsolutni minut polaska za dati termin HH:mm koji je &ge; {@code currentAbs}.
     * Prelazi u naredni dan po potrebi.
     */
    public static long nextTimeAtOrAfter(String hhmm, long currentAbs) {
        int sched = parseHHmmToMin(hhmm);
        long k = modDay(currentAbs);
        if (sched >= k) return currentAbs - k + sched;
        return currentAbs - k + sched + DAY;
    }

    private static int parseHHmmToMin(String hhmm) {
        int h = Integer.parseInt(hhmm.substring(0, 2));
        int m = Integer.parseInt(hhmm.substring(3, 5));
        return h * 60 + m;
    }

    private static long modDay(long t) {
        long r = t % DAY;
        return (r >= 0) ? r : (r + DAY);
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Result r)) return false;
        return totalPrice == r.totalPrice &&
                arrivalAbsInGoalCity == r.arrivalAbsInGoalCity &&
                Objects.equals(path, r.path) &&
                transfers == r.transfers;
    }

    @Override public int hashCode() { return Objects.hash(path, totalPrice, arrivalAbsInGoalCity, transfers); }

    @Override public String toString() {
        String sig = path.stream().map(d -> d.getFrom() + "->" + d.getTo()).collect(Collectors.joining(", "));
        return "Result{Cijena=" + totalPrice + ", Vrijeme=" + arrivalAbsInGoalCity +
                ", Presjedanja=" + transfers + ", Putanja=[" + sig + "]}";
    }

    /* Getteri/setteri */

    /** @return nepromjenjiva lista dionica (putanja). */
    public List<Departure> getPath() { return path; }

    /** Postavlja putanju (bez zaštitne kopije — koristi oprezno u testovima/GUI-u). */
    public void setPath(List<Departure> path) { this.path = path; }

    /** @return ukupna cijena. */
    public int getTotalPrice() { return totalPrice; }

    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    /** @return apsolutni minut dolaska u ciljni grad. */
    public long getArrivalAbsInGoalCity() { return arrivalAbsInGoalCity; }

    public void setArrivalAbsInGoalCity(long arrivalAbsInGoalCity) { this.arrivalAbsInGoalCity = arrivalAbsInGoalCity; }

    /** @return broj presjedanja. */
    public int getTransfers() { return transfers; }

    public void setTransfers(int transfers) { this.transfers = transfers; }
}
