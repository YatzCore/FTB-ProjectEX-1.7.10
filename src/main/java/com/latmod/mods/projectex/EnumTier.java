package com.latmod.mods.projectex;

public enum EnumTier {
    BASIC("basic"),
    DARK("dark"),
    RED("red"),
    MAGENTA("magenta"),
    PINK("pink"),
    PURPLE("purple"),
    VIOLET("violet"),
    BLUE("blue"),
    CYAN("cyan"),
    GREEN("green"),
    LIME("lime"),
    YELLOW("yellow"),
    ORANGE("orange"),
    WHITE("white"),
    FADING("fading"),
    FINAL("final");

    public static final EnumTier[] VALUES = values();

    private final String name;

    EnumTier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EnumTier byMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            return BASIC;
        }
        return VALUES[meta];
    }
}
