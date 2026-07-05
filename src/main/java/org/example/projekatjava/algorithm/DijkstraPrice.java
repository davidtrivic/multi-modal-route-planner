package org.example.projekatjava.algorithm;

import java.util.*;
import org.example.projekatjava.model.Departure;

/**
 * Dijkstra algoritam po cijeni (težina grane = cijena dionice).
 * <p>
 * Čvorovi grafa su stanice (A_x_y, Z_x_y). Polazne stanice su i A i Z
 * od startnog grada, a kao cilj prihvatamo bilo koju stanicu
 * (A ili Z) u ciljnom gradu. Putanja i dolazak računaju se helperima iz {@link Result}.
 */
public class DijkstraPrice {

    /** Čvor za PQ sa akumuliranom cijenom. */
    private static final class NodePrice implements Comparable<NodePrice> {
        private final String station;
        private final double totalPrice;

        public NodePrice(String station, double totalPrice) {
            this.station = station;
            this.totalPrice = totalPrice;
        }

        public String getStation() { return station; }
        public double getTotalPrice() { return totalPrice; }

        /** Poredi dva objekta tipa NodePrice po ukupnoj cijeni. */
        @Override
        public int compareTo(NodePrice o) {
            return Double.compare(this.totalPrice, o.totalPrice);
        }
    }

    /**
     * Pronalazi najjeftiniju rutu između dva grada.
     *
     * @param graph           mapa: stanica → polasci sa te stanice
     * @param startCityName   npr. {@code "G_0_0"}
     * @param targetCityName  npr. {@code "G_4_7"}
     * @return {@link Result} ili {@code null} ako rute nema
     */
    public Result dijkstraMinPrice(Map<String, List<Departure>> graph,
                                   String startCityName,
                                   String targetCityName) {

        Set<String> startStations = new HashSet<>();
        startStations.add(toStation(startCityName, 'A'));
        startStations.add(toStation(startCityName, 'Z'));

        Map<String, Double> dist = new HashMap<>();
        Map<String, Departure> prev = new HashMap<>();
        PriorityQueue<NodePrice> pq = new PriorityQueue<>();

        for (String station : graph.keySet()) dist.put(station, Double.POSITIVE_INFINITY);
        for (String start : startStations) {
            dist.put(start, 0.0);
            pq.add(new NodePrice(start, 0.0));
        }

        while (!pq.isEmpty()) {
            NodePrice currentNode = pq.poll();
            String current = currentNode.getStation();

            if (stationBelongsToCity(current, targetCityName)) {
                List<Departure> path = reconstructPath(prev, current);
                if (path.isEmpty()) return null;
                int total = (int) Math.round(dist.getOrDefault(current, Double.POSITIVE_INFINITY));
                long arrival = Result.simulateArrivalAbs(path);
                int transfers = Math.max(0, path.size() - 1);
                return new Result(path, total, arrival, transfers);
            }

            for (Departure edge : graph.getOrDefault(current, Collections.emptyList())) {
                for (String neighbor : getStationsInCity(edge.getTo(), graph)) {
                    double newPrice = dist.get(current) + edge.getPrice();

                    if (newPrice < dist.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                        dist.put(neighbor, newPrice);
                        prev.put(neighbor, edge);
                        pq.add(new NodePrice(neighbor, newPrice));
                    }
                }
            }
        }

        return null;
    }

    /** "G_x_y" → "A_x_y" ili "Z_x_y". */
    private static String toStation(String cityId, char type) {
        String[] p = cityId.split("_");
        if (p.length != 3 || !p[0].equals("G")) throw new IllegalArgumentException("Ocekivan format G_x_y, dobio: " + cityId);
        return type + "_" + p[1] + "_" + p[2];
    }

    /** Vraća sve stanice koje pripadaju datom gradu. */
    private List<String> getStationsInCity(String cityName, Map<String, List<Departure>> graph) {
        List<String> stations = new ArrayList<>();
        for (String station : graph.keySet()) if (stationBelongsToCity(station, cityName)) stations.add(station);
        return stations;
    }

    /** Da li stanica pripada gradu. */
    private boolean stationBelongsToCity(String stationName, String cityName) {
        String[] s = stationName.split("_"), c = cityName.split("_");
        return s.length == 3 && c.length == 3 && s[1].equals(c[1]) && s[2].equals(c[2]);
    }

    /**
     * Rekonstruiše putanju koristeći mapu prethodnika: ključ je stanica,
     * vrijednost je dionica koja je dovela do nje.
     */
    private List<Departure> reconstructPath(Map<String, Departure> prev, String targetStation) {
        List<Departure> path = new ArrayList<>();
        String current = targetStation;
        while (prev.containsKey(current)) {
            Departure d = prev.get(current);
            path.add(d);
            current = d.getFrom();
        }
        Collections.reverse(path);
        return path;
    }
}
