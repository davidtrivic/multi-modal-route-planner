package org.example.projekatjava.graph;

import org.example.projekatjava.model.Departure;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import java.util.*;

/**
 * Pomoćni prikaz grafa ruta korištenjem GraphStream-a.
 * <p>
 * Klasa:
 * <ul>
 *   <li>gradi usmjereni graf iz liste {@link Departure} (čvor = grad, grana = polazak),</li>
 *   <li>prikazuje graf sa prilagođenim CSS stilom,</li>
 *   <li>omogućava naglašavanje (highlight) konkretne rute: čvorovi i grane dobijaju CSS klasu {@code path}.</li>
 * </ul>
 *
 * Čvorovi su identifikovani nazivom grada (npr. {@code G_1_2}), dok su ulazni polasci vezani za stanice
 * {@code A_x_y}/{@code Z_x_y}. Pomoću {@link #toCityFromStation(String)} stanice se mapiraju na gradske čvorove.
 */
public class GraphStream {
    /** Interni multigraf (dozvoljeno je više grana između istih čvorova). */
    private final Graph graph = new MultiGraph("GraphStream");

    /**
     * Preslikavanje stabilnog ključa polaska ({@link #edgeKey(Departure)}) u ID grane u GraphStream-u.
     * <p>Čuvamo da bismo kasnije precizno highlight-ovali baš one grane koje su korištene u ruti.</p>
     */
    private final Map<String, String> depIdToEdgeId = new HashMap<>();

    /**
     * Prikazuje prozor sa grafom i primjenjuje vizuelni stil (paleta Deep Sea Blue).
     * <p>
     * Metoda setuje:
     * <ul>
     *   <li>{@code org.graphstream.ui=javafx} – integracija sa JavaFX okvirom,</li>
     *   <li>CSS (boje, debljine linija, tipke strelica, tekst),</li>
     *   <li>{@code ui.quality} i {@code ui.antialias} za glađe crtanje,</li>
     *   <li>auto-layout kako bi se čvorovi rasporedili automatski.</li>
     * </ul>
     * Napomena: Graf treba prethodno izgraditi pozivom {@link #buildFrom(Map)}.
     */
    public void showGraph() {
        System.setProperty("org.graphstream.ui", "javafx");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        // Paleta (Deep Sea Blue)
        String TRUE_BLUE   = "#0466c8";
        String SAPPHIRE    = "#0353a4";
        String YALE_BLUE   = "#023e7d";
        String OXFORD_BLUE = "#002855";
        String OXF_BLUE_3  = "#001233";
        String DELFT_BLUE  = "#33415c";
        String SLATE_GRAY  = "#7d8597";
        String COOL_GRAY   = "#979dac";
        String WHITE       = "#ffffff";
        String PAYNES_GRAY = "#5c677d";

        String css =
                "graph {" +
                        "   fill-color: " + OXFORD_BLUE + ";" +
                        "   padding: 30px;" +
                        "}" +
                        "node {" +
                        "   size: 13px;" +
                        "   fill-color: " + YALE_BLUE + ";" +
                        "   stroke-mode: plain;" +
                        "   stroke-color: " + OXF_BLUE_3 + ";" +
                        "   stroke-width: 2px;" +
                        "   text-alignment: above;" +
                        "   text-size: 14px;" +
                        "   text-color: " + COOL_GRAY + ";" +
                        "   text-background-mode: rounded-box;" +
                        "   text-background-color: " + OXF_BLUE_3 + ";" +
                        "   text-padding: 2px, 2px;" +
                        "}" +
                        "edge {" +
                        "   size: 2px;" +
                        "   fill-color: " + PAYNES_GRAY + ";" +
                        "   arrow-shape: arrow;" +
                        "   arrow-size: 10px, 6px;" +
                        "}" +
                        "node.path { size: 16px; fill-color: " + WHITE + "; stroke-color: " + TRUE_BLUE + "; }" +
                        "edge.path { size: 4px;  fill-color: " + WHITE + "; }";

        graph.setAttribute("ui.stylesheet", css);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        Viewer viewer = graph.display();
        if (viewer != null) viewer.enableAutoLayout();
    }

    /**
     * Izgradi graf iz ulazne mape: stanica → lista polazaka.
     * <p>
     * Za svaki {@link Departure} dodaje čvor za polazni i dolazni grad (ako ne postoje),
     * te kreira usmjerenu granu. ID grane je stabilan (baziran na podacima polaska) kako bi se
     * kasnije mogla precizno naglasiti (highlight).
     *
     * @param departuresByStation mapa stanica ({@code A_x_y}/{@code Z_x_y}) u listu polazaka
     */
    public void buildFrom(Map<String, List<Departure>> departuresByStation) {
        depIdToEdgeId.clear();

        for (Map.Entry<String, List<Departure>> entry : departuresByStation.entrySet()) {
            for (Departure d : entry.getValue()) {
                String fromCity = toCityFromStation(d.getFrom());
                String toCity   = d.getTo();

                ensureNode(fromCity);
                ensureNode(toCity);

                String eId = edgeId(d);

                if (graph.getEdge(eId) == null) {
                    try {
                        Edge e = graph.addEdge(eId, fromCity, toCity, true);
                        depIdToEdgeId.put(edgeKey(d), eId);
                    } catch (IdAlreadyInUseException ignored) {
                        // Ako već postoji isti eId (ne bi trebalo), preskoči
                    }
                }
            }
        }
    }

    /**
     * Uklanja CSS klasu {@code path} sa svih čvorova i grana.
     * <p>Koristi se prije novog poziva {@link #highlightRoute(List)} kako bi se resetovalo stanje.</p>
     */
    public void clearHighlight() {
        for (Node n : graph) {
            n.removeAttribute("ui.class");
        }
        for (Edge e : graph.edges().toList()) {
            e.removeAttribute("ui.class");
        }
    }

    /**
     * Naglašava (highlight) zadatu rutu: čvorovi i grane dobijaju CSS klasu {@code path}.
     * <p>
     * Ako određena grana nije pronađena (npr. nije ranije dodana u graf),
     * metoda će samo naglasiti čvorove.
     *
     * @param route lista dionica (polazaka) koje čine rutu; očekuje se da su povezane redom
     */
    public void highlightRoute(List<Departure> route) {
        if (route == null || route.isEmpty()) return;

        clearHighlight();

        for (Departure d : route) {
            String fromCity = toCityFromStation(d.getFrom());
            String toCity   = d.getTo();

            Node nf = ensureNode(fromCity);
            Node nt = ensureNode(toCity);
            addClass(nf, "path");
            addClass(nt, "path");

            String eId = depIdToEdgeId.getOrDefault(edgeKey(d), edgeId(d));
            Edge e = graph.getEdge(eId);
            if (e != null) addClass(e, "path");
        }
    }

    /**
     * Vraća postojeći ili kreira novi čvor sa datim ID-jem i postavlja mu labelu.
     *
     * @param id identifikator čvora (npr. {@code G_1_2})
     * @return instanca čvora
     */
    private Node ensureNode(String id) {
        Node n = graph.getNode(id);
        if (n == null) {
            n = graph.addNode(id);
            n.setAttribute("ui.label", id);
        }
        return n;
    }

    /**
     * Pretvara ID stanice ({@code A_x_y}/{@code Z_x_y}) u ID grada ({@code G_x_y}).
     *
     * @param stationId identifikator stanice
     * @return odgovarajući gradski identifikator
     */
    private static String toCityFromStation(String stationId) {
        return "G" + stationId.substring(1);
    }

    /**
     * Stabilan ključ za {@link Departure}; koristi se kao ključ mape {@link #depIdToEdgeId}.
     *
     * @param d polazak
     * @return jedinstveni string ključ baziran na polju polaska
     */
    private static String edgeKey(Departure d) {
        return d.getFrom() + "|" + d.getTo() + "|" + d.getDepartureTime()
                + "|" + d.getDuration() + "|" + d.getPrice() + "|" + d.getMinTransferTime();
    }

    /**
     * ID grane unutar GraphStream-a izveden iz {@link #edgeKey(Departure)}.
     * <p>Održava jedinstvenost i čitljivost u okviru biblioteke.</p>
     *
     * @param d polazak
     * @return identifikator grane u grafu
     */
    private static String edgeId(Departure d) {
        return "E|" + edgeKey(d);
    }

    /**
     * Dodaje CSS klasu elementu (čvoru ili grani) ako već nije prisutna.
     *
     * @param el  element grafa (node/edge)
     * @param ime naziv CSS klase (npr. {@code "path"})
     */
    private static void addClass(Element el, String ime) {
        String cur = (String) el.getAttribute("ui.class");
        if (cur == null || cur.isBlank()) {
            el.setAttribute("ui.class", ime);
        } else if (!Arrays.asList(cur.split("\\s+")).contains(ime)) {
            el.setAttribute("ui.class", cur + " " + ime);
        }
    }
}
