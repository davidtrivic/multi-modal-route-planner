package org.example.projekatjava.algorithm;

import org.example.projekatjava.model.Country;
import org.example.projekatjava.model.Departure;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generiše Top-K različitih ruta prema zadatom kriterijumu
 * (vrijeme, cijena ili broj presjedanja).
 * <p>
 * Implementacija radi iterativno:
 * svaku već pronađenu najbolju rutu "zabranjuje" uklanjanjem njenih dionica iz grafa
 * (edge banning), pa na filtrisanom grafu ponovo poziva odgovarajući algoritam.
 * Na kraju dobijeni kandidati se dodatno sortiraju stabilnim komparatorom
 * (primarni kriterijum, pa pomoćni vezani za ostale metrike).
 */
public class Top5Routes {
    /** Kriterijumi rangiranja. */
    public enum Criterion { TIME, PRICE, TRANSFERS }

    /**
     * Stavka Top liste. Umotava puni {@link Result} da pozivalac
     * ima pristup svim metrikama i putanji.
     */
    public static final class Route {
        /** Puni proračun rezultata. */
        public final Result result;

        public Route(Result r) { this.result = r; }

        /** @return dionice rute. */
        public List<Departure> getPath() { return result.getPath(); }
        /** @return apsolutni dolazak u ciljni grad. */
        public long getArrivalAbs() { return result.getArrivalAbsInGoalCity(); }
        /** @return ukupna cijena. */
        public int getTotalPrice() { return result.getTotalPrice(); }
        /** @return broj presjedanja. */
        public int getTransfers() { return result.getTransfers(); }
    }

    /**
     * Vraća do K najboljih ruta prema kriterijumu.
     * Različitost ruta postiže se zabranjivanjem (ban) svih dionica već pronađene rute.
     *
     * @param country   model države (prosljeđuje se DijkstraTime-u)
     * @param graph     originalni graf (stanica → polasci)
     * @param startCity polazni grad (npr. "G_0_0")
     * @param goalCity  ciljni grad (npr. "G_2_2")
     * @param criterion kriterijum: vrijeme, cijena, presjedanja
     * @param K         maksimalan broj ruta
     * @return lista ruta, sortirana stabilnim komparatorom po kriterijumu
     */
    public List<Route> topK(Country country,
                            Map<String, List<Departure>> graph,
                            String startCity,
                            String goalCity,
                            Criterion criterion,
                            int K) {

        K = Math.max(1, K);
        List<Route> results = new ArrayList<>();
        if (graph == null || graph.isEmpty()) return results;

        Set<String> banned = new HashSet<>();
        Set<String> seenSignatures = new HashSet<>();
        Map<String, List<Departure>> working = graph;

        for (int i = 0; i < K; i++) {
            Route best = computeBest(country, working, startCity, goalCity, criterion);
            if (best == null || best.getPath() == null || best.getPath().isEmpty()) break;

            String sig = signature(best.getPath());
            if (!seenSignatures.add(sig)) break;

            results.add(best);

            for (Departure d : best.getPath()) banned.add(edgeId(d));
            working = filteredGraph(graph, banned);
        }

        results.sort(comparatorFor(criterion));
        return results;
    }

    /** Stabilni komparatori po primarnom kriterijumu, uz pomoćne kriterijume. */
    private static Comparator<Route> comparatorFor(Criterion c) {
        return switch (c) {
            case TIME      -> Comparator.comparingLong((Route r) -> travelMinutes(r.getPath()))
                    .thenComparingInt(Route::getTotalPrice)
                    .thenComparingInt(Route::getTransfers);
            case PRICE     -> Comparator.comparingInt(Route::getTotalPrice)
                    .thenComparingLong(r -> travelMinutes(r.getPath()))
                    .thenComparingInt(Route::getTransfers);
            case TRANSFERS -> Comparator.comparingInt(Route::getTransfers)
                    .thenComparingLong(r -> travelMinutes(r.getPath()))
                    .thenComparingInt(Route::getTotalPrice);
        };
    }

    /** Interno pozivanje odgovarajućeg algoritma prema kriterijumu. */
    private Route computeBest(Country country,
                              Map<String, List<Departure>> graph,
                              String startCity,
                              String goalCity,
                              Criterion criterion) {

        Result r = switch (criterion) {
            case TIME      -> new DijkstraTime().dijkstraMinTime(country, graph, startCity, goalCity);
            case PRICE     -> new DijkstraPrice().dijkstraMinPrice(graph, startCity, goalCity);
            case TRANSFERS -> new BFS().bfsMinTransfer(graph, startCity, goalCity);
        };
        if (r == null || r.getPath() == null || r.getPath().isEmpty()) return null;
        return new Route(r);
    }


    /** Jedinstveni identifikator dionice, korišćen za “banovanje”. */
    private static String edgeId(Departure d) {
        return d.getFrom() + "|" + d.getTo() + "|" + d.getDepartureTime()
                + "|" + d.getDuration() + "|" + d.getPrice() + "|" + d.getMinTransferTime();
    }

    /** Potpis putanje (konkatenacija edgeId-eva), da sprečimo duplikate. */
    private static String signature(List<Departure> path) {
        return path.stream().map(Top5Routes::edgeId).collect(Collectors.joining("||"));
    }

    /** Vraća novi graf bez zabranjenih dionica. */
    private static Map<String, List<Departure>> filteredGraph(Map<String, List<Departure>> graph, Set<String> banned) {
        Map<String, List<Departure>> out = new HashMap<>();
        for (Map.Entry<String, List<Departure>> e : graph.entrySet()) {
            List<Departure> filtered = e.getValue().stream().filter(d -> !banned.contains(edgeId(d))).toList();
            out.put(e.getKey(), filtered);
        }
        return out;
    }

    /**
     * Izračunava ukupne minute putovanja za putanju
     * koristeći jedinstvene helpere iz {@link Result}.
     */
    private static long travelMinutes(List<Departure> path) {
        if (path == null || path.isEmpty()) return Long.MAX_VALUE;
        long arrival = Result.simulateArrivalAbs(path);
        long startAbs = Result.nextTimeAtOrAfter(path.get(0).getDepartureTime(), 0);
        return Math.max(0, arrival - startAbs);
    }
}
