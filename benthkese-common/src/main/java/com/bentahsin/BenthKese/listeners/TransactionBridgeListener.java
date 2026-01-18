package com.bentahsin.BenthKese.listeners;

import com.bentahsin.BenthKese.data.TransactionData;
import com.bentahsin.BenthKese.eventbridge.BenthMessageEvent;
import com.bentahsin.BenthKese.eventbridge.Subscribe;
import com.bentahsin.BenthKese.services.storage.IStorageService;
import org.bukkit.event.Listener;

public class TransactionBridgeListener implements Listener {

    private final IStorageService storageService;

    public TransactionBridgeListener(IStorageService storageService) {
        this.storageService = storageService;
    }

    @Subscribe(channel = "transaction-log")
    public void onTransactionLog(BenthMessageEvent event) {
        TransactionData data = event.getPayload(TransactionData.class);
        storageService.logTransaction(data);
    }
}