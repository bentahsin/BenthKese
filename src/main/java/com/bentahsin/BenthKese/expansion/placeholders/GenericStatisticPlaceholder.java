package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

public class GenericStatisticPlaceholder implements IPlaceholder {

    private final String identifier;
    private final IStorageService storageService;
    private final Function<PlayerData, Number> valueExtractor;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    private final boolean isCurrency;

    public GenericStatisticPlaceholder(String id, IStorageService storage, Function<PlayerData, Number> extractor, boolean currency) {
        this.identifier = id;
        this.storageService = storage;
        this.valueExtractor = extractor;
        this.isCurrency = currency;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public String getValue(OfflinePlayer player) {
        PlayerData data = storageService.getPlayerData(player.getUniqueId());
        Number value = valueExtractor.apply(data);
        String formattedValue = numberFormat.format(value);
        return isCurrency ? formattedValue + " ⛁" : formattedValue;
    }
}