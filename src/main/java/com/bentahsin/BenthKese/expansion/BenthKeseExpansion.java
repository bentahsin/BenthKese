/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.expansion;

import com.bentahsin.BenthKese.BenthKese;
import com.bentahsin.BenthKese.configuration.ConfigurationManager;
import com.bentahsin.BenthKese.configuration.MessageManager;
import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.BenthKese.data.PlayerData;
import com.bentahsin.BenthKese.data.TopPlayerEntry;
import com.bentahsin.BenthKese.expansion.placeholders.*;
import com.bentahsin.BenthKese.services.LimitManager;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.services.storage.YamlStorageService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BenthKeseExpansion extends PlaceholderExpansion {

    private final BenthKese plugin;
    private final IStorageService storageService;
    private final LimitManager limitManager;
    private final MessageManager messageManager;
    private final ConfigurationManager configurationManager;
    private final Economy economy = BenthKese.getEconomy();
    private final Map<String, IPlaceholder> placeholders = new HashMap<>();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public BenthKeseExpansion(BenthKese plugin, IStorageService storageService, LimitManager limitManager, MessageManager messageManager, ConfigurationManager configurationManager) {
        this.plugin = plugin;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.messageManager = messageManager;
        this.configurationManager = configurationManager;
        registerPlaceholders(storageService, limitManager, messageManager);
    }

    private void registerPlaceholders(IStorageService storageService, LimitManager limitManager, MessageManager messageManager) {
        String infiniteText = messageManager.getMessage("limit-info.infinite-text");
        String maxLevelText = messageManager.getMessage("limit-info.max-level-text");
        String yesText = messageManager.getMessage("general.yes");
        String noText = messageManager.getMessage("general.no");

        // Her bir placeholder'ı oluştur ve kaydet
        addPlaceholder(new LimitSeviyeAdiPlaceholder(storageService, limitManager));
        addPlaceholder(new LimitSonrakiSeviyeAdiPlaceholder(storageService, limitManager, maxLevelText));
        addPlaceholder(new LimitSonrakiSeviyeUcretPlaceholder(storageService, limitManager));
        addPlaceholder(new LimitSonrakiSeviyeIlerlemePlaceholder(storageService, limitManager));
        addPlaceholder(new LimitYukseltebilirMiPlaceholder(storageService, limitManager, yesText, noText));
        addPlaceholder(new LimitSeviyeIdPlaceholder(storageService));
        addPlaceholder(new LimitResetSuresiPlaceholder(storageService));
        addPlaceholder(new BakiyeFormattedPlaceholder());
        addPlaceholder(new GenericLimitPlaceholder("limit_gonderme_kullanilan", storageService, limitManager, infiniteText, (pd, ll) -> pd.getDailySent()));
        addPlaceholder(new GenericLimitPlaceholder("limit_gonderme_kalan", storageService, limitManager, infiniteText, (pd, ll) -> ll.getSendLimit() == -1 ? Double.POSITIVE_INFINITY : ll.getSendLimit() - pd.getDailySent()));
        addPlaceholder(new GenericLimitPlaceholder("limit_gonderme_max", storageService, limitManager, infiniteText, (pd, ll) -> ll.getSendLimit() == -1 ? Double.POSITIVE_INFINITY : ll.getSendLimit()));
        addPlaceholder(new GenericLimitPlaceholder("limit_alma_kullanilan", storageService, limitManager, infiniteText, (pd, ll) -> pd.getDailyReceived()));
        addPlaceholder(new GenericLimitPlaceholder("limit_alma_kalan", storageService, limitManager, infiniteText, (pd, ll) -> ll.getReceiveLimit() == -1 ? Double.POSITIVE_INFINITY : ll.getReceiveLimit() - pd.getDailyReceived()));
        addPlaceholder(new GenericLimitPlaceholder("limit_alma_max", storageService, limitManager, infiniteText, (pd, ll) -> ll.getReceiveLimit() == -1 ? Double.POSITIVE_INFINITY : ll.getReceiveLimit()));
        addPlaceholder(new FaizHesapSayisiPlaceholder(storageService));
        addPlaceholder(new FaizHesapMaxPlaceholder(configurationManager));
        addPlaceholder(new FaizHesapDurumPlaceholder(storageService, configurationManager));
        addPlaceholder(new FaizYatirimToplamPlaceholder(storageService));
        addPlaceholder(new FaizToplamKazancPlaceholder(storageService));
        addPlaceholder(new FaizSonrakiKazancPlaceholder(storageService, "faiz_sonraki_kazanc_miktar"));
        addPlaceholder(new FaizSonrakiKazancPlaceholder(storageService, "faiz_sonraki_kazanc_sure"));
        if (!(storageService instanceof YamlStorageService)) { // Sadece SQL'de çalışır
            addPlaceholder(new PlayerRankPlaceholder(storageService));
            // Top 10 listesini dinamik olarak oluştur
            for (int i = 1; i <= 10; i++) {
                // Bakiye Sıralaması
                addPlaceholder(new TopListPlaceholder("top_bakiye_isim_" + i, i, true, storageService::getTopPlayersByBalance));
                addPlaceholder(new TopListPlaceholder("top_bakiye_deger_" + i, i, false, storageService::getTopPlayersByBalance));
                // Seviye Sıralaması
                addPlaceholder(new TopListPlaceholder("top_seviye_isim_" + i, i, true, storageService::getTopPlayersByLimitLevel));
                addPlaceholder(new TopListPlaceholder("top_seviye_deger_" + i, i, false, storageService::getTopPlayersByLimitLevel));
            }
        }
        addPlaceholder(new VergiOranPlaceholder("vergi_yatirma_oran_yuzde", configurationManager));
        addPlaceholder(new VergiOranPlaceholder("vergi_cekme_oran_yuzde", configurationManager));
        addPlaceholder(new VergiOranPlaceholder("vergi_gonderme_oran_yuzde", configurationManager));
        addPlaceholder(new EkonomiItemAdiPlaceholder(configurationManager));
        addPlaceholder(new LimitGondermeAsildiMiPlaceholder(storageService, limitManager));
        addPlaceholder(new FaizHesapAcabilirMiPlaceholder(storageService, configurationManager));
        addPlaceholder(new BakiyeRawPlaceholder());
        addPlaceholder(new SiralamaBakiyeHedefKalanPlaceholder(storageService));
        addPlaceholder(new GenericStatisticPlaceholder("toplam_islem_sayisi", storageService, PlayerData::getTotalTransactions, false));
        addPlaceholder(new GenericStatisticPlaceholder("gonderilen_toplam_para", storageService, PlayerData::getTotalSent, true));
        addPlaceholder(new GenericStatisticPlaceholder("odenen_toplam_vergi", storageService, PlayerData::getTotalTaxPaid, true));
    }

    private void addPlaceholder(IPlaceholder placeholder) {
        this.placeholders.put(placeholder.getIdentifier().toLowerCase(), placeholder);
    }

    @Override
    public @NotNull String getIdentifier() { return "benthkese"; }
    @Override
    public @NotNull String getAuthor() { return plugin.getDescription().getAuthors().toString(); }
    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override
    public boolean persist() { return true; }

    @SuppressWarnings("deprecation")
    private OfflinePlayer getTargetPlayer(String name) {
        Player onlineTarget = Bukkit.getPlayerExact(name);
        if (onlineTarget != null) {
            return onlineTarget;
        }
        return Bukkit.getOfflinePlayer(name);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // Günlük limit sıfırlama
        PlayerData playerData = storageService.getPlayerData(player.getUniqueId());
        if (System.currentTimeMillis() - playerData.getLastResetTime() > TimeUnit.DAYS.toMillis(1)) {
            playerData.resetDailyLimits();
            storageService.savePlayerData(playerData);
        }

        String lowerParams = params.toLowerCase();

        // Önce tam eşleşen, modüler placeholder'ları kontrol et
        IPlaceholder placeholder = placeholders.get(lowerParams);
        if (placeholder != null) {
            return placeholder.getValue(player);
        }

        // Sonra parametreli placeholder'ları kontrol et
        if (lowerParams.startsWith("bakiye_")) {
            return getBakiye(params);
        }
        if (lowerParams.startsWith("seviye_adi_from_id_")) {
            return getSeviyeAdiFromId(params);
        }
        if (lowerParams.startsWith("seviye_adi_")) { // Bu, "bakiye_"den sonra olmalı
            return getSeviyeAdi(params);
        }

        return null;
    }

    private String getBakiye(String params) {
        String playerName = params.substring("bakiye_".length());
        OfflinePlayer target = getTargetPlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            return messageManager.getMessage("general.player-not-found");
        }
        return numberFormat.format(economy.getBalance(target));
    }

    private String getSeviyeAdi(String params) {
        String playerName = params.substring("seviye_adi_".length());
        OfflinePlayer target = getTargetPlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            return messageManager.getMessage("general.player-not-found");
        }
        int levelId = storageService.getPlayerData(target.getUniqueId()).getLimitLevel();
        LimitLevel level = limitManager.getLimitLevel(levelId);
        return level != null ? level.getName() : "Bilinmeyen Seviye";
    }

    private String getSeviyeAdiFromId(String params) {
        String idStr = params.substring("seviye_adi_from_id_".length());
        try {
            int levelId = Integer.parseInt(idStr);
            LimitLevel level = limitManager.getLimitLevel(levelId);
            return level != null ? level.getName() : messageManager.getMessage("general.invalid-level");
        } catch (NumberFormatException e) {
            return messageManager.getMessage("general.invalid-level");
        }
    }

    private String handleTopLevel(String params, boolean isName) {
        try {
            String rankStr = params.substring(params.lastIndexOf('_') + 1);
            int rank = Integer.parseInt(rankStr);
            if (rank < 1 || rank > 10) return "-";

            // Bu kısım cache'lenmeli
            List<TopPlayerEntry> topPlayers = storageService.getTopPlayersByLimitLevel(10);
            if (rank > topPlayers.size()) return "-";

            TopPlayerEntry entry = topPlayers.get(rank - 1);
            return entry.getPlayerName();

        } catch (Exception e) {
            return "-";
        }
    }
}