package com.latmod.mods.projectex.gui;

import com.latmod.mods.projectex.ProjectEXConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class EMCFormat {

    private static final ThreadLocal<DecimalFormat> FULL_FORMATTER = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setGroupingSeparator(',');
            return new DecimalFormat("#,###", symbols);
        }
    };

    private static final ThreadLocal<DecimalFormat> COMPACT_FORMATTER = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat("0.00", symbols);
        }
    };

    private static String cachedPrefix = "EMC:";
    private static volatile long lastShiftCheckTime = 0L;
    private static volatile boolean cachedShiftState = false;

    static {
        try {
            cachedPrefix = StatCollector.translateToLocal("pe.emc.emc_tooltip_prefix");
        } catch (Throwable ignored) {}
    }

    public static String formatCompact(double number) {
        if (number >= 1_000_000_000_000_000_000_000_000.0) {
            return formatNumber(number / 1_000_000_000_000_000_000_000_000.0) + "Y";
        } else if (number >= 1_000_000_000_000_000_000_000.0) {
            return formatNumber(number / 1_000_000_000_000_000_000_000.0) + "Z";
        } else if (number >= 1_000_000_000_000_000_000.0) {
            return formatNumber(number / 1_000_000_000_000_000_000.0) + "E";
        } else if (number >= 1_000_000_000_000_000.0) {
            return formatNumber(number / 1_000_000_000_000_000.0) + "P";
        } else if (number >= 1_000_000_000_000.0) {
            return formatNumber(number / 1_000_000_000_000.0) + "T";
        } else if (number >= 1_000_000_000.0) {
            return formatNumber(number / 1_000_000_000.0) + "G";
        } else if (number >= 1_000_000.0) {
            return formatNumber(number / 1_000_000.0) + "M";
        } else if (number >= 1_000.0) {
            return formatNumber(number / 1_000.0) + "k";
        } else {
            return formatFull(number);
        }
    }

    private static String formatNumber(double val) {
        return COMPACT_FORMATTER.get().format(val);
    }

    public static String formatFull(double number) {
        return FULL_FORMATTER.get().format(number);
    }

    public static String formatEmc(double number, boolean isShiftDown) {
        if (ProjectEXConfig.overrideEmcFormatter && number >= 1_000_000.0 && !isShiftDown) {
            return formatCompact(number);
        }
        return formatFull(number);
    }

    public static String formatEmc(long number, boolean isShiftDown) {
        return formatEmc((double) number, isShiftDown);
    }

    public static boolean isShiftDownCached() {
        long now = System.currentTimeMillis();
        if (now - lastShiftCheckTime > 50L) {
            lastShiftCheckTime = now;
            try {
                if (Keyboard.isCreated()) {
                    cachedShiftState = Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54);
                } else {
                    cachedShiftState = false;
                }
            } catch (Throwable ignored) {
                cachedShiftState = false;
            }
        }
        return cachedShiftState;
    }

    public static String formatGuiEmc(double emc) {
        boolean shift = isShiftDownCached();
        String formatted = shift ? formatFull(emc) : formatCompact(emc);
        return cachedPrefix + " " + formatted;
    }
}
