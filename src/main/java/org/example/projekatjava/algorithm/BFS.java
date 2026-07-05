package org.example.projekatjava.algorithm;

import org.example.projekatjava.model.*;

import java.util.*;

/**
 * BFS pretraga sa ciljem minimizacije broja presjedanja.
 * <p>
 * Pretraga se obavlja po stanicama (A_x_y / Z_x_y) po nivoima:
 * svaki nivo N predstavlja rute sa tačno N dionica. Pri
 * ekspanziji nivoa računa se realan dolazak i minimalni transfer,
 * te bira najraniji dolazak među kandidatima koji stižu u ciljni grad
 * na istom nivou (dakle, uz isti broj presjedanja).
 */
public class BFS {
    private static final int DAY = 24 * 60;

    /**
     * Pronalazi rutu sa najmanje presjedanja između dva grada.
     *
     * @param graph       mapa stanica → polasci sa te stanice
     * @param startCityId polazni grad (npr. "G_0_0")
     * @param goalCityId  ciljni grad (npr. "G_4_7")
     * @return {@link Result} ili {@code null} ako rute nema
     * @throws NullPointerException ako je bilo koji parametar null
     */
    public Result bfsMinTransfer(Map<String, List<Departure>> graph,
                                 String startCityId,
                                 String goalCityId) {

        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(startCityId, "startCityId");
        Objects.requireNonNull(goalCityId, "goalCityId");

        String startA = toStation(startCityId, 'A');
        String startZ = toStation(startCityId, 'Z');

        Map<String, Long> curLevel = new HashMap<>();
        curLevel.put(startA, 0L);
        curLevel.put(startZ, 0L);

        Map<String, String> parentStation = new HashMap<>();
        Map<String, Departure> parentDeparture = new HashMap<>();

        int level = 0;
        while (!curLevel.isEmpty()) {
            boolean foundAtThisLevel = false;
            long bestGoalArrival = Long.MAX_VALUE;
            String bestGoalLastStation = null;
            Departure bestGoalLastDeparture = null;

            Map<String, Long> nextLevel = new HashMap<>();

            for (Map.Entry<String, Long> e : curLevel.entrySet()) {
                String station = e.getKey();
                long readyAt = e.getValue();

                List<Departure> outs = graph.getOrDefault(station, Collections.emptyList());
                if (outs.isEmpty()) continue;

                for (Departure d : outs) {
                    long depAbs = nextTimeAtOrAfter(d.getDepartureTime(), readyAt);
                    long arriveCityAbs = depAbs + d.getDuration();

                    if (d.getTo().equals(goalCityId)) {
                        foundAtThisLevel = true;
                        if (arriveCityAbs < bestGoalArrival) {
                            bestGoalArrival = arriveCityAbs;
                            bestGoalLastStation = station;
                            bestGoalLastDeparture = d;
                        }
                        continue;
                    }

                    long afterTransfer = arriveCityAbs + d.getMinTransferTime();
                    String city = d.getTo();
                    String nextA = toStation(city, 'A');
                    String nextZ = toStation(city, 'Z');

                    if (relaxNext(nextLevel, nextA, afterTransfer)) {
                        setParent(level + 1, nextA, station, d, parentStation, parentDeparture);
                    }
                    if (relaxNext(nextLevel, nextZ, afterTransfer)) {
                        setParent(level + 1, nextZ, station, d, parentStation, parentDeparture);
                    }
                }
            }

            if (foundAtThisLevel) {
                List<Departure> path = reconstructPath(parentStation, parentDeparture,
                        bestGoalLastStation, bestGoalLastDeparture, level);
                int transfers = Math.max(0, path.size() - 1);
                int totalPrice = Result.sumPrice(path);
                return new Result(path, totalPrice, bestGoalArrival, transfers);
            }

            curLevel = nextLevel;
            level++;
        }

        return null;
    }


    private static void setParent(int level, String childStation, String parentSt,
                                  Departure via, Map<String, String> parentStation,
                                  Map<String, Departure> parentDeparture) {
        String key = key(level, childStation);
        parentStation.put(key, parentSt);
        parentDeparture.put(key, via);
    }

    private static String key(int level, String station) {
        return level + "|" + station;
    }

    /**
     * Rekonstrukcija putanje: poznata je stanica iz koje je krenula POSLJEDNJA
     * dionica ka cilju i sama dionica. Vraća listu dionica redom.
     */
    private static List<Departure> reconstructPath(Map<String, String> parentStation,
                                                   Map<String, Departure> parentDeparture,
                                                   String lastStation, Departure lastDep, int levelOfLast) {
        List<Departure> rev = new ArrayList<>();
        if (lastDep == null) return Collections.emptyList();
        rev.add(lastDep);

        String curStation = lastStation;
        int curLevel = levelOfLast;

        while (curLevel > 0) {
            String key = key(curLevel, curStation);
            Departure pd = parentDeparture.get(key);
            String ps = parentStation.get(key);
            if (pd == null || ps == null) break;
            rev.add(pd);
            curStation = ps;
            curLevel--;
        }
        Collections.reverse(rev);
        return rev;
    }

    /** "G_x_y" → "A_x_y" ili "Z_x_y". */
    private static String toStation(String cityId, char type) {
        String[] p = cityId.split("_");
        if (p.length != 3 || !"G".equals(p[0])) {
            throw new IllegalArgumentException("Ocekivan format G_x_y, dobio: " + cityId);
        }
        return type + "_" + p[1] + "_" + p[2];
    }

    /** Najraniji apsolutni minut polaska HH:mm ≥ currentAbs (sa prelaskom dana). */
    private static long nextTimeAtOrAfter(String hhmm, long currentAbs) {
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

    /** U sljedećem nivou čuvamo samo najraniji 'ready time' po stanici. */
    private static boolean relaxNext(Map<String, Long> nextLevel, String station, long candTime) {
        Long old = nextLevel.get(station);
        if (old == null || candTime < old) {
            nextLevel.put(station, candTime);
            return true;
        }
        return false;
    }
}
