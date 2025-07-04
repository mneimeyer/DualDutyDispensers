package org.neimeyer.dualdutydispensers.config;

import org.bukkit.configuration.file.FileConfiguration;

public class RedstoneClockConfig extends BlockConfig {

    public RedstoneClockConfig(FileConfiguration config) {
        super(config, "Redstone Clock");
    }

    public int getTickInterval() {
        return config.getInt("tick-interval", 8);
    }
}