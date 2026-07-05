package org.example.projekatjava.algorithm;

import org.example.projekatjava.model.*;

import java.util.*;

/**
 * Dijkstra algoritam po vremenu (cilj: najraniji dolazak u ciljni grad).
 * <p>
 * Čvorovi su stanice (A_x_y / Z_x_y), a težina čvora je "najraniji trenutak od kog
 * smo spremni da krenemo iz te stanice". Kod svake relaksacije uzima se sljedeći
 * mogući polazak (uz prelazak dana), računa dolazak i dodaje minimalni transfer
 * prije dalje ekspanzije.
 */
public class DijkstraTime {
    private static final int DAY = 24 * 60;

    /**
     * Pronalazi rutu minimalnog vremena putovanja (najraniji dolazak u ciljni grad).
     *
     * @param country              model države
     * @param departuresByStation  mapa stanica → polasci
     * @param startCityId          polazni grad (npr. "G_0_0")
     * @param goalCityId           ciljni grad (npr. "G_4_7")
     * @return {@link Result} ili {@code null} ako rute nema
     * @throws NullPointerException ako je neka od vrijednosti null
     */
    public Result dijkstraMinTime(Country country,
                                  Map<String, List<Departure>> departuresByStation,
                                  String startCityId,
                                  String goalCityId) {

        Objects.requireNonNull(departuresByStation, "departuresByStation null");
        Objects.requireNonNull(startCityId, "startCityId null");
        Objects.requireNonNull(goalCityId, "goalCityId null");

        String startA = toStation(startCityId, 'A');
        String startZ = toStation(startCityId, 'Z');

        Map<String, Long> dist = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingLong(n -> n.time));
        Map<String, String> parentStation = new HashMap<>();
        Map<String, Departure> parentDeparture = new HashMap<>();

        dist.put(startA, 0L);
        dist.put(startZ, 0L);
        pq.add(new Node(startA, 0));
        pq.add(new Node(startZ, 0));

        long bestGoalArrival = Long.MAX_VALUE;
        String bestGoalLastStation = null;
        Departure bestGoalLastDeparture = null;

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            long curTime = cur.time;
            String curStation = cur.station;

            Long known = dist.get(curStation);
            if (known == null || curTime != known) continue;

            if (curTime >= bestGoalArrival) {
                break;
            }

            List<Departure> outs = departuresByStation.get(curStation);
            if (outs == null || outs.isEmpty()) continue;

            for (Departure d : outs) {
                long depAbs = nextTimeAtOrAfter(d.getDepartureTime(), curTime);
                long arriveCityAbs = depAbs + d.getDuration();

                if (d.getTo().equals(goalCityId)) {
                    if (arriveCityAbs < bestGoalArrival) {
                        bestGoalArrival = arriveCityAbs;
                        bestGoalLastStation = curStation;
                        bestGoalLastDeparture = d;
                    }
                }

                long afterTransfer = arriveCityAbs + d.getMinTransferTime();
                String city = d.getTo();
                String nextA = toStation(city, 'A');
                String nextZ = toStation(city, 'Z');

                if (relax(nextA, afterTransfer, dist)) {
                    parentStation.put(nextA, curStation);
                    parentDeparture.put(nextA, d);
                    pq.add(new Node(nextA, afterTransfer));
                }
                if (relax(nextZ, afterTransfer, dist)) {
                    parentStation.put(nextZ, curStation);
                    parentDeparture.put(nextZ, d);
                    pq.add(new Node(nextZ, afterTransfer));
                }
            }
        }

        if (bestGoalArrival == Long.MAX_VALUE) {
            return null;
        }

        List<Departure> rev = new ArrayList<>();
        {
            String s = bestGoalLastStation;
            Departure last = bestGoalLastDeparture;
            if (last == null) {
                return new Result(Collections.emptyList(), 0, 0, 0);
            }
            rev.add(last);
            while (s != null) {
                Departure pd = parentDeparture.get(s);
                if (pd == null) break;
                rev.add(pd);
                s = parentStation.get(s);
            }
        }
        Collections.reverse(rev);
        int totalPrice = Result.sumPrice(rev);
        int transfers = Math.max(0, rev.size() - 1);
        return new Result(rev, totalPrice, bestGoalArrival, transfers);
    }

    /** PQ čvor: stanica + najraniji ready time. */
    private static final class Node {
        final String station;
        final long time;
        Node(String station, long time) { this.station = station; this.time = time; }
    }

    /** "G_x_y" → "A_x_y" ili "Z_x_y". */
    private static String toStation(String cityId, char type) {
        String[] p = cityId.split("_");
        if (p.length != 3 || !p[0].equals("G")) {
            throw new IllegalArgumentException("Ocekivan format grada G_x_y, dobio: " + cityId);
        }
        return type + "_" + p[1] + "_" + p[2];
    }

    /**
     * Najraniji apsolutni minut polaska za termin HH:mm koji je ≥ {@code currentAbs}.
     * Ako je termin prošao za taj dan, prelazi u naredni.
     */
    private static long nextTimeAtOrAfter(String hhmm, long currentAbs) {
        int sched = parseHHmmToMin(hhmm); // 0..1439
        long k = modDay(currentAbs);      // currentAbs % DAY u [0, DAY)
        if (sched >= k) {
            return currentAbs - k + sched;
        } else {
            return currentAbs - k + sched + DAY;
        }
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

    /** Standardna Dijkstra relaksacija za ready time. */
    private static boolean relax(String station, long cand, Map<String, Long> dist) {
        Long old = dist.get(station);
        if (old == null || cand < old) {
            dist.put(station, cand);
            return true;
        }
        return false;
    }
}
