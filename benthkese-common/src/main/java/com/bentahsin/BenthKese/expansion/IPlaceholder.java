/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.expansion;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Her bir placeholder'ın uygulaması gereken arayüz.
 * Bu yapı, her placeholder'ı kendi mantığıyla izole bir birim haline getirir.
 */
public interface IPlaceholder {

    /**
     * Placeholder'ın benzersiz tanımlayıcısını döndürür.
     * Örn: "limit_seviye_adi" (ana ön ek olan "benthkese_" olmadan)
     * @return Placeholder tanımlayıcısı.
     */
    @NotNull
    String getIdentifier();

    /**
     * Placeholder istendiğinde değeri hesaplayan ve döndüren metot.
     * @param player Değerin hesaplanacağı oyuncu.
     * @return Placeholder'ın işlenmiş metin değeri.
     */
    String getValue(OfflinePlayer player);
}