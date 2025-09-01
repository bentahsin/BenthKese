package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.TopPlayerEntry;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class TopListPlaceholder implements IPlaceholder {

    private final String identifier;
    private final int rank;
    private final boolean isName;
    private final Function<Integer, List<TopPlayerEntry>> topListFetcher;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    private List<TopPlayerEntry> cachedTopPlayers;
    private long lastCacheTime = 0;

    public TopListPlaceholder(String identifier, int rank, boolean isName, Function<Integer, List<TopPlayerEntry>> fetcher) {
        this.identifier = identifier;
        this.rank = rank - 1;
        this.isName = isName;
        this.topListFetcher = fetcher;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    private void updateCache() {
        if (System.currentTimeMillis() - lastCacheTime > 300000) { // 5 dakikalık cache
            cachedTopPlayers = topListFetcher.apply(10);
            lastCacheTime = System.currentTimeMillis();
        }
    }

    @Override
    public String getValue(OfflinePlayer player) {
        updateCache();
        if (cachedTopPlayers == null || rank >= cachedTopPlayers.size()) {
            return "-";
        }
        TopPlayerEntry entry = cachedTopPlayers.get(rank);
        if (isName) {
            return entry.getPlayerName();
        } else {
            // Değer metin mi sayı mı kontrol et
            if (entry.getStringValue() != null) {
                return entry.getStringValue();
            } else {
                return numberFormat.format(entry.getValue()) + " ⛁";
            }
        }
    }
}