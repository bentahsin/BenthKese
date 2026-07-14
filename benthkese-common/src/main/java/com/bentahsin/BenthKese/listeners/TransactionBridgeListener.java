package com.bentahsin.BenthKese.listeners;

import com.bentahsin.BenthKese.events.TransactionLogEvent;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TransactionBridgeListener implements Listener {

    private final IStorageService storageService;

    public TransactionBridgeListener(IStorageService storageService) {
        this.storageService = storageService;
    }

    @EventHandler
    public void onTransactionLog(TransactionLogEvent event) {
        storageService.logTransaction(event.getTransactionData());
    }
}
