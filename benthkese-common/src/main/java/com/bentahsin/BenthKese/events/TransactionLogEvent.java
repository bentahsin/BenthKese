/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.events;

import com.bentahsin.BenthKese.data.TransactionData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Kaydedilmesi gereken bir finansal işlem gerçekleştiğinde tetiklenir.
 */
public class TransactionLogEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final TransactionData transactionData;

    public TransactionLogEvent(TransactionData transactionData) {
        this.transactionData = transactionData;
    }

    public TransactionData getTransactionData() {
        return transactionData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
