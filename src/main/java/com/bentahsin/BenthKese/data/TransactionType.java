/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.data;

/**
 * Yapılan işlemin türünü belirten enum.
 * Bu, veritabanında string yerine sayı (ordinal) olarak saklanarak verimlilik sağlar.
 */
public enum TransactionType {
    SEND,         // Para gönderme (-)
    RECEIVE,      // Para alma (+)
    DEPOSIT,      // Eşya yatırma (+)
    WITHDRAW,     // Para çekme (-)
    LEVEL_UP,     // Limit yükseltme (-)
    INTEREST_CLAIM, // Faiz kazancı (+)
    INTEREST_BREAK  // Faizden anapara iadesi (+)
}