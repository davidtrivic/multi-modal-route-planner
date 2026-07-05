package org.example.projekatjava.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.example.projekatjava.generator.TransportDataGenerator.TransportData;

/**
 * Pomoćna klasa za učitavanje ulaznih podataka iz JSON fajla.
 * <p>
 * Koristi Jackson {@link ObjectMapper} da deserijalizuje fajl u
 * {@link TransportData} koji generiše modul generatora podataka.
 */
public class JsonLoader {

    /**
     * Učitava i parsira JSON dokument u {@link TransportData}.
     *
     * @param putanja apsolutna ili relativna putanja do JSON fajla
     * @return popunjena instanca {@link TransportData}
     * @throws IOException ako fajl ne postoji, nije čitljiv ili JSON nije validan
     */
    public static TransportData ucitaj(String putanja) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(putanja), TransportData.class);
    }
}
