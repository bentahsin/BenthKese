package com.bentahsin.BenthKese.expansion.placeholders;

import com.bentahsin.BenthKese.data.InterestAccount;
import com.bentahsin.BenthKese.expansion.IPlaceholder;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import com.bentahsin.BenthKese.utils.TimeUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

public class FaizSonrakiKazancPlaceholder implements IPlaceholder {
    private final IStorageService storageService;
    private final String identifier;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("tr", "TR"));

    public FaizSonrakiKazancPlaceholder(IStorageService storageService, String identifier) {
        this.storageService = storageService;
        this.identifier = identifier;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public String getValue(OfflinePlayer player) {
        Optional<InterestAccount> nextAccountOpt = storageService.getInterestAccounts(player.getUniqueId())
                .stream()
                .min(Comparator.comparingLong(InterestAccount::getEndTime));

        if (!nextAccountOpt.isPresent()) {
            return "Yok";
        }

        InterestAccount nextAccount = nextAccountOpt.get();
        if (identifier.equals("faiz_sonraki_kazanc_miktar")) {
            return numberFormat.format(nextAccount.getFinalAmount()) + " ⛁";
        } else if (identifier.equals("faiz_sonraki_kazanc_sure")) {
            long remaining = nextAccount.getEndTime() - System.currentTimeMillis();
            return remaining > 0 ? TimeUtil.formatDuration(remaining) : "Şimdi";
        }
        return "";
    }
}