package com.bentahsin.BenthKese.configuration;

import com.bentahsin.BenthKese.data.LimitLevel;
import com.bentahsin.configuration.annotation.ConfigHeader;
import com.bentahsin.configuration.annotation.ConfigPath;
import com.bentahsin.configuration.annotation.ConfigVersion;
import com.bentahsin.configuration.annotation.PostLoad;

import java.util.Map;
import java.util.TreeMap;

@ConfigHeader("BenthKese Limit Seviyeleri")
@ConfigVersion(1)
public class LimitsConfig {

    @ConfigPath("limit-levels")
    public Map<Integer, LimitLevel> limitLevels = new TreeMap<>();

    @PostLoad
    public void postLoad() {
        if (limitLevels != null) {
            limitLevels.forEach((id, level) -> level.setLevel(id));
        }
    }
}