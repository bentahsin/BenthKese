package com.bentahsin.BenthKese.data;

import com.bentahsin.configuration.annotation.ConfigPath;

public class LimitLevel {
    public transient int level;

    public String name = "&7Başlangıç";
    public double cost = 1000.0;

    @ConfigPath("send-limit")
    public double sendLimit = 5000.0;

    @ConfigPath("receive-limit")
    public double receiveLimit = 10000.0;

    public LimitLevel() {}

    public String getName() { return name; }
    public double getCost() { return cost; }
    public double getSendLimit() { return sendLimit; }
    public double getReceiveLimit() { return receiveLimit; }

    public void setLevel(int level) { this.level = level; }
    public int getLevel() { return level; }
}