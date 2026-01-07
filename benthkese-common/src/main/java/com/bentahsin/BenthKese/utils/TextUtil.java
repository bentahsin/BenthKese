package com.bentahsin.BenthKese.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Metin (String) işlemleri için genel amaçlı yardımcı metotlar içerir.
 * Bu sınıfın bir örneği oluşturulamaz (non-instantiable).
 */
public final class TextUtil {

    /**
     * Bu sınıfın bir örneğinin oluşturulmasını engellemek için private constructor.
     */
    private TextUtil() {
    }

    /**
     * Verilen bir metindeki tüm placeholder'ları haritadaki değerlerle değiştirir.
     *
     * @param text         Değiştirilecek metin.
     * @param placeholders Değiştirilecek {key}-value çiftlerini içeren harita.
     * @return Placeholder'ları değiştirilmiş yeni metin.
     */
    public static String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    /**
     * Verilen bir listedeki her metindeki tüm placeholder'ları haritadaki değerlerle değiştirir.
     *
     * @param list         Değiştirilecek metinleri içeren liste.
     * @param placeholders Değiştirilecek {key}-value çiftlerini içeren harita.
     * @return Placeholder'ları değiştirilmiş yeni metin listesi.
     */
    public static List<String> replacePlaceholders(List<String> list, Map<String, String> placeholders) {
        if (list == null || placeholders == null || placeholders.isEmpty()) {
            return list;
        }
        return list.stream()
                .map(line -> replacePlaceholders(line, placeholders))
                .collect(Collectors.toList());
    }
}