package com.bentahsin.BenthKese.data;

/**
 * Liderlik tablolarındaki tek bir sırayı temsil eden veri nesnesi.
 * Oyuncu adını ve ilgili değeri (bakiye, seviye vb.) tutar.
 */
public class TopPlayerEntry {
    private final String playerName;
    private final double value;
    private final String stringValue;

    public TopPlayerEntry(String playerName, double value) {
        this.playerName = playerName;
        this.value = value;
        this.stringValue = null;
    }

    public TopPlayerEntry(String playerName, String stringValue) {
        this.playerName = playerName;
        this.value = 0;
        this.stringValue = stringValue;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getValue() {
        return value;
    }

    public String getStringValue() {
        return stringValue;
    }
}