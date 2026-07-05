package org.example.projekatjava.data;

import org.example.projekatjava.generator.TransportDataGenerator;
import org.example.projekatjava.model.*;

import java.util.*;

/**
 * Zadužena za pretvaranje ulaznih (JSON) podataka u
 * radne strukture aplikacije: mapu polazaka po stanicama i
 * dvo-dimenzionalnu mapu gradova (država).
 */
public class LoadData {

    /**
     * Učitava podatke sa diska, konstruiše sve model-objekte i vraća graf polazaka.
     * <ul>
     *   <li>Parsira JSON u {@link TransportDataGenerator.TransportData}</li>
     *   <li>Kreira {@link Departure} objekte</li>
     *   <li>Razdvaja autobuske i vozne polaske pa ih spaja u jedinstven graf (stanica → polasci)</li>
     *   <li>Gradi matricu gradova ({@link Country#setCountryMap(City[][])}) sa stanicama</li>
     * </ul>
     *
     * @param country instanca modela države koja će biti popunjena matricom gradova
     * @return mapa polazaka: ključ je naziv stanice (npr. "A_1_2" ili "Z_1_2"),
     *         vrijednost je lista polazaka sa te stanice
     * @throws Exception propagira eventualne IO/parsiranje greške iz loadera
     */
    public Map<String, List<Departure>> loadData(Country country) throws Exception {
        TransportDataGenerator.TransportData data = JsonLoader.ucitaj("transport_data.json");

        Departure[] departures = new Departure[data.departures.size()];
        for (int i = 0; i < data.departures.size(); i++) {
            departures[i] = new Departure(
                    data.departures.get(i).type,
                    data.departures.get(i).from,
                    data.departures.get(i).to,
                    data.departures.get(i).departureTime,
                    data.departures.get(i).duration,
                    data.departures.get(i).price,
                    data.departures.get(i).minTransferTime
            );
        }

        Map<String, List<Departure>> busDepartures = new HashMap<>();
        Map<String, List<Departure>> trainDepartures = new HashMap<>();
        for (Departure d : departures) {
            if ("autobus".equals(d.getType())) {
                busDepartures.computeIfAbsent(d.getFrom(), k -> new ArrayList<>()).add(d);
            } else if ("voz".equals(d.getType())) {
                trainDepartures.computeIfAbsent(d.getFrom(), k -> new ArrayList<>()).add(d);
            }
        }

        Map<String, List<Departure>> departuresGraph = mergeDepartures(busDepartures, trainDepartures);

        int rows = data.countryMap.length;
        int cols = data.countryMap[0].length;
        City[][] cities = new City[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int index = i * cols + j;
                cities[i][j] = new City(
                        data.stations.get(index).city,
                        new BusStation(
                                data.stations.get(index).busStation,
                                busDepartures.get(data.stations.get(index).busStation)
                        ),
                        new TrainStation(
                                data.stations.get(index).trainStation,
                                trainDepartures.get(data.stations.get(index).trainStation)
                        )
                );
            }
        }

        country.setCountryMap(cities);
        return departuresGraph;
    }

    /**
     * Spaja dvije mape polazaka (autobuske i vozne) u jednu mapu
     * čiji je ključ naziv stanice, a vrijednost objedinjena lista polazaka.
     *
     * @param busDepartures   stanica → autobuski polasci
     * @param trainDepartures stanica → vozni polasci
     * @return jedinstvena mapa stanica → svi polasci
     */
    public Map<String, List<Departure>> mergeDepartures(Map<String, List<Departure>> busDepartures,
                                                        Map<String, List<Departure>> trainDepartures) {
        Map<String, List<Departure>> departuresGraph = new HashMap<>();

        for (Map.Entry<String, List<Departure>> entry : busDepartures.entrySet()) {
            departuresGraph
                    .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
        }

        for (Map.Entry<String, List<Departure>> entry : trainDepartures.entrySet()) {
            departuresGraph
                    .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                    .addAll(entry.getValue());
        }

        return departuresGraph;
    }
}
