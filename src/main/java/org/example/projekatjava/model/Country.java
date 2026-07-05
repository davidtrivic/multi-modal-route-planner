package org.example.projekatjava.model;

/**
 * Model države kao 2D mape gradova.
 * Svaka ćelija predstavlja jedan {@link City} ili je {@code null}
 * ako na toj poziciji nema grada.
 */
public class Country {
    /** Dvodimenzionalna mreža gradova. */
    private City[][] countryMap;

    /** Prazan konstruktor. */
    public Country(){

    }

    /**
     * Kreira državu sa zadatom mapom gradova.
     *
     * @param countryMap 2D matrica gradova (može sadržati {@code null} polja)
     */
    public Country(City[][] countryMap){
        this.countryMap = countryMap;
    }

    /**
     * @return 2D matrica gradova.
     */
    public City[][] getCountryMap() {
        return countryMap;
    }

    /**
     * Postavlja 2D matricu gradova.
     *
     * @param countryMap nova matrica gradova
     */
    public void setCountryMap(City[][] countryMap) {
        this.countryMap = countryMap;
    }
}
