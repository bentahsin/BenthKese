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
        int rank = storageService.getPlayerBalanceRank(player.getUniqueId());

        if (rank <= 1) {
            return "Zirvedesin!";
        }

        List<TopPlayerEntry> topPlayers = storageService.getTopPlayersByBalance(rank - 1);

        if (topPlayers == null || topPlayers.size() < rank - 1) {
            return "N/A";
        }

        TopPlayerEntry targetPlayer = topPlayers.get(rank - 2);

        double currentPlayerBalance = economy.getBalance(player);

        double difference = targetPlayer.getValue() - currentPlayerBalance;

        if (difference <= 1) {
            return "Çok Yakın!";
        }

        return numberFormat.format(difference) + " ⛁";
    }
}