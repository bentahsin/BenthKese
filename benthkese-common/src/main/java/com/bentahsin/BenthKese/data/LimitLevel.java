package com.bentahsin.BenthKese.data;

public class LimitLevel {
    public int level;

    public String name = "&7Başlangıç";
    public double cost = 1000.0;

    public double sendLimit = 5000.0;

    public double receiveLimit = 10000.0;

    public LimitLevel() {}

    public String getName() { return name; }
    public double getCost() { return cost; }
    public double getSendLimit() { return sendLimit; }
    public double getReceiveLimit() { return receiveLimit; }

    public void setLevel(int level) { this.level = level; }
    public int getLevel() { return level; }
}