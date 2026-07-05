package org.example.projekatjava.data;

import org.example.projekatjava.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Učitava agregirane informacije o prodatim kartama iz direktorijuma {@code racuni}.
 * <p>
 * Kroz sve fajlove u folderu broji ukupan broj računa i sabira ukupan prihod.
 * Očekuje se jednostavan TXT format koji generiše {@link org.example.projekatjava.model.Racun}.
 */
public class LoadRacuni {
    /** Broj pronađenih računa (fajlova). */
    private int brojRacuna;
    /** Zbir vrijednosti stavke "Cijena:" kroz sve račune. */
    private int ukupanPrihod;

    /** Relativna putanja do foldera sa računima. */
    public static final String path = "." + File.separator + "racuni";

    /**
     * Na konstrukciji skenira direktorijum i popunjava metrike.
     * <p>
     * Implementacija je tolerantna na prazan ili nepostojeći direktorijum.
     */
    public LoadRacuni() {
        this.brojRacuna = 0;
        this.ukupanPrihod = 0;
        try {
            File dir = new File(path);
            File[] files = dir.listFiles();
            if (files == null) files = new File[0];

            for (File file : files) {
                brojRacuna++;
                try (BufferedReader br = new BufferedReader(new FileReader(path + File.separator + file.getName()))) {
                    String price = null;
                    for (int i = 0; i < 8; i++) {
                        price = br.readLine();
                    }
                    price = br.readLine();
                    ukupanPrihod += Integer.parseInt(price.substring(8));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** @return ukupan broj računa (fajlova) u folderu. */
    public int getBrojRacuna() {
        return brojRacuna;
    }

    /** Postavlja broj računa (uglavnom za testove). */
    public void setBrojRacuna(int brojRacuna) {
        this.brojRacuna = brojRacuna;
    }

    /** @return ukupan prihod sabran iz svih računa. */
    public int getUkupanPrihod() {
        return ukupanPrihod;
    }

    /** Ručno postavlja ukupan prihod (uglavnom za testove). */
    public void setUkupanPrihod(int ukupanPrihod) {
        this.ukupanPrihod = ukupanPrihod;
    }

    @Override
    public String toString() {
        return "LoadRacuni{" +
                "brojRacuna=" + brojRacuna +
                ", ukupanPrihod=" + ukupanPrihod +
                '}';
    }
}
