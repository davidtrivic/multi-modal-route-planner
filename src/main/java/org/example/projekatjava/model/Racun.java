package org.example.projekatjava.model;

import org.example.projekatjava.algorithm.Result;

import java.io.*;
import java.util.Calendar;
import java.util.stream.Collectors;

/**
 * Model računa za kupljenu rutu.
 * Klasa na osnovu {@link Result} generiše tekstualni račun
 * i čuva ga kao .txt fajl u direktorijumu {@code ./racuni}.
 */
public class Racun {
    /** Npr. "A_0_0->G_1_0, Z_1_0->G_1_1, ..." */
    private String relacija;
    /** Ukupno vrijeme putovanja (u minutama, apsolutni dolazak). */
    private long vrijeme;
    /** Ukupna cijena rute. */
    private int cijena;
    /** Datum izdavanja računa (DD.MM.YYYY). */
    private String datum;

    /** Putanja do direktorijuma gdje se čuvaju računi. */
    public static final String path = "." + File.separator + "racuni";

    /**
     * Formira račun iz izračunatog rezultata rute.
     *
     * @param result rezultat algoritma
     */
    public Racun(Result result){
        this.relacija = result.getPath().stream()
                .map(d -> d.getFrom()+"->"+d.getTo())
                .collect(Collectors.joining(", "));
        this.vrijeme = result.getArrivalAbsInGoalCity();
        this.cijena = result.getTotalPrice();

        Calendar cal = Calendar.getInstance();
        this.datum = cal.get(Calendar.DATE) + "." + cal.get(Calendar.MONTH) + "." + cal.get(Calendar.YEAR);
    }

    /**
     * Generiše i snima račun u folder {@code ./racuni} kao
     * {@code racunN.txt}, gdje je N sljedeći redni broj.
     * Ako folder ne postoji, očekuje se da je već kreiran na nivou aplikacije.
     */
    public void generisiRacun(){
        try {
            File uFajl = new File(path + File.separator + "racun" + getBrojRacuna() + ".txt");
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(uFajl)));
            pw.println("=======================================================================");
            pw.println("                              R A C U N                                ");
            pw.println("=======================================================================");
            pw.println("Broj racuna: " + getBrojRacuna());
            pw.println("Datum: " + this.datum);
            pw.println("-----------------------------------------------------------------------");
            pw.println("Ruta: " + this.relacija);
            pw.println("Vrijeme: " + this.vrijeme + " minuta");
            pw.println("Cijena: " + this.cijena);
            pw.println("-----------------------------------------------------------------------");
            pw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Računa sljedeći redni broj računa na osnovu broja fajlova u folderu {@code ./racuni}.
     *
     * @return trenutni broj fajlova u folderu (koristi se kao sufiks).
     */
    public int getBrojRacuna(){
        int brRacuna = 0;
        String path = "." + File.separator + "racuni";
        try {
            File dir = new File(path);
            File[] files = dir.listFiles();
            brRacuna = (files == null) ? 0 : files.length;
        } catch(Exception e){
            e.printStackTrace();
        }
        return brRacuna;
    }

    /** @return opis rute kao string. */
    public String getRelacija() { return relacija; }
    /** @param relacija novi opis rute. */
    public void setRelacija(String relacija) { this.relacija = relacija; }

    /** @return vrijeme dolaska (u minutama, apsolutni). */
    public long getVrijeme() { return vrijeme; }
    /** @param vrijeme novo vrijeme dolaska (u minutama). */
    public void setVrijeme(long vrijeme) { this.vrijeme = vrijeme; }

    /** @return ukupna cijena. */
    public int getCijena() { return cijena; }
    /** @param cijena nova cijena. */
    public void setCijena(int cijena) { this.cijena = cijena; }

    /** @return datum izdavanja računa (DD.MM.YYYY). */
    public String getDatum() { return datum; }
    /** @param datum novi datum izdavanja. */
    public void setDatum(String datum) { this.datum = datum; }
}
