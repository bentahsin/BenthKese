package com.bentahsin.BenthKese.configuration;

import com.bentahsin.BenthKese.data.LimitLevel;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.TreeMap;

public class LimitsConfig extends AbstractYamlConfig {

    public Map<Integer, LimitLevel> limitLevels = new TreeMap<>();

    public LimitsConfig(JavaPlugin plugin) {
        super(plugin, "limits.yml");
    }

    @Override
    protected void onLoad(YamlDocument document) {
        Map<Integer, LimitLevel> levels = new TreeMap<>();
        Section levelsSection = document.getSection("limit-levels");

        if (levelsSection != null) {
            for (Object key : levelsSection.getKeys()) {
                int id;
                try {
                    id = Integer.parseInt(String.valueOf(key));
                } catch (NumberFormatException e) {
                    continue;
                }

                Section levelSection = levelsSection.getSection(String.valueOf(key));
                LimitLevel level = new LimitLevel();
                level.name = levelSection.getString("name", level.name);
                level.cost = levelSection.getDouble("cost", level.cost);
                level.sendLimit = levelSection.getDouble("send-limit", level.sendLimit);
                level.receiveLimit = levelSection.getDouble("receive-limit", level.receiveLimit);
                level.setLevel(id);

                levels.put(id, level);
            }
        }

        limitLevels = levels;
    }
}
