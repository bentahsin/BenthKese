package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.data.TopPlayerEntry;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SiralamaBakiyeHedefKalanPlaceholder implements IPlaceholder {

    private final IStorageService storageService;
    private final Economy economy = BenthKese.getEconomy();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public SiralamaBakiyeHedefKalanPlaceholder(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "siralama_bakiye_hedef_kalan";
    }

    @Override
    public String getValue(OfflinePlayer player) {
        // Oyuncunun mevcut sıralamasını al
        int rank = storageService.getPlayerBalanceRank(player.getUniqueId());

        // Eğer oyuncu 1. sırada veya sıralamada değilse, özel bir mesaj döndür
        if (rank <= 1) {
            return "Zirvedesin!";
        }

        // Bir üst sıradaki oyuncuyu bulmak için, o sıraya kadar olan listeyi çek
        // Örneğin, oyuncu 5. sıradaysa, ilk 4 oyuncuyu çekeriz.
        List<TopPlayerEntry> topPlayers = storageService.getTopPlayersByBalance(rank - 1);

        // Beklenmedik bir durum veya hata varsa, "N/A" döndür
        if (topPlayers == null || topPlayers.size() < rank - 1) {
            return "N/A";
        }

        // Hedef oyuncu, listenin son elemanıdır (0'dan başladığı için rank - 2).
        TopPlayerEntry targetPlayer = topPlayers.get(rank - 2);

        // Mevcut oyuncunun bakiyesini al
        double currentPlayerBalance = economy.getBalance(player);

        // Aradaki farkı hesapla
        double difference = targetPlayer.getValue() - currentPlayerBalance;

        // Eğer fark çok azsa veya negatifse (aynı bakiyedelerse), farklı bir mesaj göster
        if (difference <= 1) {
            return "Çok Yakın!";
        }

        return numberFormat.format(difference) + " ⛁";
    }
}