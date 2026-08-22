package com.latmod.mods.projectex;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ProjectEXConfig {
    public static Configuration config;

    // General
    public static int linkCooldown = 5;
    public static boolean blacklistPowerFlowerFromWatch = true;
    public static int powerFlowerUpdateInterval = 20;
    public static boolean overrideEmcFormatter = true;

    // Item & Block EMC values (Exact 1.12.2 values)
    public static long[] matterEmc = new long[12];
    public static long starShardEmc = 6871947673739264L;
    public static long knowledgeBookEmc = 159711488L;
    public static long stoneTableEmc = 10508L;
    public static long alchemyTableEmc = 18943L;
    public static long energyLinkEmc = 467380L;
    public static long personalLinkEmc = 13673232L;
    public static long refinedLinkEmc = 123059088L;
    public static long compressedRefinedLinkEmc = 738354528L;
    public static long[] collectorEmc = new long[16];
    public static long[] relayEmc = new long[16];
    public static long[] powerFlowerEmcValue = new long[16];
    public static long[] magnumStarEmc = new long[6];
    public static long[] colossalStarEmc = new long[6];
    public static long finalStarEmc = 0L;

    // Collector rates per tier (0..15)
    public static double[] collectorSunEmc = new double[16];
    public static double[] collectorMoonEmc = new double[16];
    public static double[] collectorMaxEmc = new double[16];

    // Relay rates per tier (0..15)
    public static double[] relayMaxEmc = new double[16];
    public static double[] relayTransferEmc = new double[16];

    // Power Flower rates per tier (0..15)
    public static double[] powerFlowerEmc = new double[16];

    // Exact 1.12.2 Collector Generation Rates
    private static final double[] DEFAULT_SUN_EMC = {
        4.0, 12.0, 40.0, 160.0, 640.0, 2560.0, 10240.0, 40960.0,
        163840.0, 655360.0, 2621440.0, 10485760.0, 41943040.0, 167772160.0, 671088640.0, 1000000000000.0
    };

    // Exact 1.12.2 Relay Transfer Rates
    private static final double[] DEFAULT_RELAY_TRANSFER = {
        64.0, 192.0, 640.0, 2560.0, 10240.0, 40960.0, 163840.0, 655360.0,
        2621440.0, 10485760.0, 41943040.0, 167772160.0, 671088640.0, 2684354560.0, 10737418240.0, 1000000000000.0
    };

    // Exact 1.12.2 Power Flower Rates: collectorOutput * 18 + relayBonus * 30
    private static final double[] DEFAULT_POWER_FLOWER_EMC = {
        102.0, 306.0, 1020.0, 4080.0, 16020.0, 68580.0, 296820.0, 1187280.0,
        4749120.0, 18996480.0, 75985920.0, 303943680.0, 1215774720.0, 4863098880.0, 19452395520.0, 48000000000000.0
    };

    static {
        for (int i = 0; i < 16; i++) {
            collectorSunEmc[i] = DEFAULT_SUN_EMC[i];
            collectorMoonEmc[i] = DEFAULT_SUN_EMC[i] / 2.0;
            collectorMaxEmc[i] = DEFAULT_SUN_EMC[i] * 10000.0;
            relayTransferEmc[i] = DEFAULT_RELAY_TRANSFER[i];
            relayMaxEmc[i] = DEFAULT_RELAY_TRANSFER[i] * 10000.0;
            powerFlowerEmc[i] = DEFAULT_POWER_FLOWER_EMC[i];
        }
    }

    // Exact 1.12.2 Matter EMC Formula: 3 * prev + 6 * AeternalisFuel (49,152)
    private static final long[] DEFAULT_MATTER_EMC = {
        1449984L,        // Magenta (0)
        4399104L,        // Pink (1)
        13246464L,       // Purple (2)
        39788544L,       // Violet (3)
        119414784L,      // Blue (4)
        358293504L,      // Cyan (5)
        1074929664L,     // Green (6)
        3224838144L,     // Lime (7)
        9674563584L,     // Yellow (8)
        29023739904L,    // Orange (9)
        87071268864L,    // White (10)
        261213855744L    // Fading (11)
    };

    // Exact 1.12.2 Collector EMC Formula: Collector[N-1] + Matter[N]
    private static final long[] DEFAULT_COLLECTOR_EMC = {
        428040L,             // Basic (0)
        567304L,             // Dark (1)
        1034248L,            // Red (2)
        2484232L,            // Magenta (3)
        6883336L,            // Pink (4)
        20129800L,           // Purple (5)
        59918344L,           // Violet (6)
        179333128L,          // Blue (7)
        537626632L,          // Cyan (8)
        1612556296L,         // Green (9)
        4837394440L,         // Lime (10)
        14511958024L,        // Yellow (11)
        43535697928L,        // Orange (12)
        130606966792L,       // White (13)
        391820822536L,       // Fading (14)
        6872339494561800L    // Final (15)
    };

    // Exact 1.12.2 Relay EMC Formula: Relay[N-1] + Matter[N]
    private static final long[] DEFAULT_RELAY_EMC = {
        411656L,             // Basic (0)
        550920L,             // Dark (1)
        1017864L,            // Red (2)
        2467848L,            // Magenta (3)
        6866952L,            // Pink (4)
        20113416L,           // Purple (5)
        59901960L,           // Violet (6)
        179316744L,          // Blue (7)
        537610248L,          // Cyan (8)
        1612539912L,         // Green (9)
        4837378056L,         // Lime (10)
        14511941640L,        // Yellow (11)
        43535681544L,        // Orange (12)
        130606950408L,       // White (13)
        391820806152L,       // Fading (14)
        6872339477891416L    // Final (15)
    };

    // Exact 1.12.2 Magnum Star EMC (204.8M * 4^i)
    private static final long[] DEFAULT_MAGNUM_STAR_EMC = {
        204800000L, 819200000L, 3276800000L, 13107200000L, 52428800000L, 209715200000L
    };

    // Exact 1.12.2 Colossal Star EMC (838.86B * 4^i)
    private static final long[] DEFAULT_COLOSSAL_STAR_EMC = {
        838860800000L, 3355443200000L, 13421772800000L, 53687091200000L, 214748364800000L, 858993459200000L
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
            powerFlowerUpdateInterval = config.getInt("power_flower_update_interval", "general", 20, 1, 200, "Interval in ticks between Power Flower EMC updates (default 20 = 1 sec)");
            overrideEmcFormatter = config.getBoolean("override_emc_formatter", "general", true, "Overrides default EMC tooltip formatter with compact SI units (M, G, T, P, E) unless Shift is held");

            // Matter EMC values
            for (int i = 0; i < 12; i++) {
                String matterName = EnumMatter.VALUES[i].getName();
                String raw = config.getString("matter_" + matterName, "emc_values", String.valueOf(DEFAULT_MATTER_EMC[i]), "EMC value for " + matterName + " matter");
                try {
                    matterEmc[i] = Long.parseLong(raw.trim());
                } catch (NumberFormatException e) {
                    matterEmc[i] = DEFAULT_MATTER_EMC[i];
                }
            }

            // Star EMC values
            for (int i = 0; i < 6; i++) {
                String tierName = StarTier.VALUES[i].getName();
                String rawMagnum = config.getString("magnum_star_" + tierName, "emc_values", String.valueOf(DEFAULT_MAGNUM_STAR_EMC[i]), "EMC value for Magnum Star " + tierName);
                try {
                    magnumStarEmc[i] = Long.parseLong(rawMagnum.trim());
                } catch (NumberFormatException e) {
                    magnumStarEmc[i] = DEFAULT_MAGNUM_STAR_EMC[i];
                }

                String rawColossal = config.getString("colossal_star_" + tierName, "emc_values", String.valueOf(DEFAULT_COLOSSAL_STAR_EMC[i]), "EMC value for Colossal Star " + tierName);
                try {
                    colossalStarEmc[i] = Long.parseLong(rawColossal.trim());
                } catch (NumberFormatException e) {
                    colossalStarEmc[i] = DEFAULT_COLOSSAL_STAR_EMC[i];
                }
            }

            // Star Shard EMC
            String rawShard = config.getString("final_star_shard", "emc_values", String.valueOf(starShardEmc), "EMC value for Final Star Shard");
            try {
                starShardEmc = Long.parseLong(rawShard.trim());
            } catch (NumberFormatException e) {
                starShardEmc = 6871947673739264L;
            }

            // Final Star EMC
            String rawFinalStar = config.getString("final_star", "emc_values", "0", "EMC value for Final Star (0 = craft only, no EMC value in table)");
            try {
                finalStarEmc = Long.parseLong(rawFinalStar.trim());
            } catch (NumberFormatException e) {
                finalStarEmc = 0L;
            }

            // Collector / Relay / Power Flower EMC
            for (int i = 0; i < 16; i++) {
                collectorEmc[i] = DEFAULT_COLLECTOR_EMC[i];
                relayEmc[i] = DEFAULT_RELAY_EMC[i];
                // Power Flower EMC = 18 * Collector + 6 * Relay + Energy Link
                powerFlowerEmcValue[i] = 18L * collectorEmc[i] + 6L * relayEmc[i] + energyLinkEmc;
            }

            // Rates
            for (int i = 0; i < 16; i++) {
                String tierName = EnumTier.VALUES[i].getName();
                collectorSunEmc[i] = config.get("collector", "sun_emc_" + tierName, DEFAULT_SUN_EMC[i]).getDouble(DEFAULT_SUN_EMC[i]);
                collectorMoonEmc[i] = collectorSunEmc[i] / 2.0;
                collectorMaxEmc[i] = collectorSunEmc[i] * 10000.0;

                relayTransferEmc[i] = config.get("relay", "transfer_emc_" + tierName, DEFAULT_RELAY_TRANSFER[i]).getDouble(DEFAULT_RELAY_TRANSFER[i]);
                relayMaxEmc[i] = relayTransferEmc[i] * 10000.0;

                powerFlowerEmc[i] = config.get("power_flower", "emc_" + tierName, DEFAULT_POWER_FLOWER_EMC[i]).getDouble(DEFAULT_POWER_FLOWER_EMC[i]);
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
