package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Limit verilerini almak için yeniden kullanılabilir bir placeholder sınıfı.
 */
public class GenericLimitPlaceholder implements IPlaceholder {

    private final String identifier;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final String infiniteText;
    private final Function<Double, String> formatter;
    private final BiFunction<PlayerData, LimitLevel, Double> valueExtractor;

    public GenericLimitPlaceholder(String identifier, IStorageService storage, LimitManager limits, String infinite, BiFunction<PlayerData, LimitLevel, Double> extractor) {
        this.identifier = identifier;
        this.storageService = storage;
        this.limitManager = limits;
        this.infiniteText = infinite;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
        this.formatter = numberFormat::format;
        this.valueExtractor = extractor;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public String getValue(OfflinePlayer player) {
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
        LimitLevel currentLevel = limitManager.getLimitLevel(playerData.getLimitLevel());
        if (currentLevel == null) return "0";

        double value = valueExtractor.apply(playerData, currentLevel);

        if (value == Double.POSITIVE_INFINITY) {
            return infiniteText;
        }
        return formatter.apply(value);
    }
}