package com.latmod.mods.projectex;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ProjectEXConfig {
    public static Configuration config;

    // General
    public static int linkCooldown = 5;
    public static boolean blacklistPowerFlowerFromWatch = true;

    // Collector rates per tier (0..15)
    public static double[] collectorSunEmc = new double[16];
    public static double[] collectorMoonEmc = new double[16];
    public static double[] collectorMaxEmc = new double[16];

    // Relay rates per tier (0..15)
    public static double[] relayMaxEmc = new double[16];
    public static double[] relayTransferEmc = new double[16];

    // Power Flower rates per tier (0..15)
    public static double[] powerFlowerEmc = new double[16];

    // Default rate multipliers based on 16 tiers
    private static final double[] DEFAULT_SUN_EMC = {
        4.0, 12.0, 40.0, 160.0, 640.0, 2560.0, 10240.0, 40960.0,
        163840.0, 655360.0, 2621440.0, 10485760.0, 41943040.0, 167772160.0, 671088640.0, 2684354560.0
    };

    private static final double[] DEFAULT_RELAY_TRANSFER = {
        64.0, 192.0, 640.0, 2560.0, 10240.0, 40960.0, 163840.0, 655360.0,
        2621440.0, 10485760.0, 41943040.0, 167772160.0, 671088640.0, 2684354560.0, 10737418240.0, 42949672960.0
    };

    private static final double[] DEFAULT_POWER_FLOWER_EMC = {
        4.0, 12.0, 40.0, 160.0, 640.0, 2560.0, 10240.0, 40960.0,
        163840.0, 655360.0, 2621440.0, 10485760.0, 41943040.0, 167772160.0, 671088640.0, 2684354560.0
    };

    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            loadConfig();
        }
    }

    public static void loadConfig() {
        try {
            config.load();

            linkCooldown = config.getInt("link_cooldown", "general", 5, 1, 100, "Ticks between EMC link processing");
            blacklistPowerFlowerFromWatch = config.getBoolean("blacklist_power_flower_from_watch", "general", true, "Blacklist Power Flowers from Time Watch");

            for (int i = 0; i < 16; i++) {
                String tierName = EnumTier.VALUES[i].getName();
                collectorSunEmc[i] = config.getFloat("sun_emc_" + tierName, "collector", (float) DEFAULT_SUN_EMC[i], 0.0f, Float.MAX_VALUE, "Sun EMC/s for " + tierName + " collector");
                collectorMoonEmc[i] = collectorSunEmc[i] / 2.0;
                collectorMaxEmc[i] = collectorSunEmc[i] * 10000.0;

                relayTransferEmc[i] = config.getFloat("transfer_emc_" + tierName, "relay", (float) DEFAULT_RELAY_TRANSFER[i], 0.0f, Float.MAX_VALUE, "Transfer EMC/s for " + tierName + " relay");
                relayMaxEmc[i] = relayTransferEmc[i] * 10000.0;

                powerFlowerEmc[i] = config.getFloat("emc_" + tierName, "power_flower", (float) DEFAULT_POWER_FLOWER_EMC[i], 0.0f, Float.MAX_VALUE, "EMC/s for " + tierName + " power flower");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
