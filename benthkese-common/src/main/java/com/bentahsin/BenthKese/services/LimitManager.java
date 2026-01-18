/*
 * BenthKese - A modern economy and limit system for Spigot.
 * Copyright (c) 2025 bentahsin.
 *
 * This project is licensed under the MIT License.
 * See the LICENSE file in the project root for full license information.
 */
package com.bentahsin.BenthKese.services;

import com.bentahsin.BenthKese.configuration.LimitsConfig;
import com.bentahsin.BenthKese.data.LimitLevel;

import java.util.Collections;
import java.util.Map;

public class LimitManager {

    private final LimitsConfig limitsConfig;

    public LimitManager(LimitsConfig limitsConfig) {
        this.limitsConfig = limitsConfig;
    }

    public LimitLevel getLimitLevel(int level) {
        if (limitsConfig.limitLevels == null) return null;
        return limitsConfig.limitLevels.get(level);
    }

    public LimitLevel getNextLevel(int currentLevel) {
        if (limitsConfig.limitLevels == null) return null;
        return limitsConfig.limitLevels.get(currentLevel + 1);
    }

    public Map<Integer, LimitLevel> getAllLevels() {
        if (limitsConfig.limitLevels == null) return Collections.emptyMap();
        return Collections.unmodifiableMap(limitsConfig.limitLevels);
    }
}