/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.utils;

import java.util.concurrent.TimeUnit;

public final class TimeUtil {

    /**
     * "10d", "1h", "30m", "45s" gibi bir metni milisaniyeye çevirir.
     * @param timeString Çevrilecek metin.
     * @return Milisaniye cinsinden süre. Geçersiz formatta ise -1 döner.
     */
    public static long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return -1;
        }

        try {
            char unit = timeString.charAt(timeString.length() - 1);
            long value = Long.parseLong(timeString.substring(0, timeString.length() - 1));

            return switch (Character.toLowerCase(unit)) {
                case 's' -> value * 1000;
                case 'm' -> value * 60 * 1000;
                case 'h' -> value * 60 * 60 * 1000;
                case 'd' -> value * 24 * 60 * 60 * 1000;
                default -> -1;
            };
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     * Milisaniyeyi "X gün Y saat Z dakika S saniye" formatında okunabilir bir metne çevirir.
     * @param millis Çevrilecek milisaniye.
     * @return Formatlanmış metin.
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            return "Geçersiz Süre";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" gün ");
        if (hours > 0) sb.append(hours).append(" saat ");
        if (minutes > 0) sb.append(minutes).append(" dakika ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append(" saniye");

        return sb.toString().trim();
    }
}