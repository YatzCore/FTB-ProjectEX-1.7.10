package com.latmod.mods.projectex;

public enum EnumMatter {
    MAGENTA("magenta", 12441600),
    PINK("pink", 37324800),
    PURPLE("purple", 111974400),
    VIOLET("violet", 335923200),
    BLUE("blue", 1007769600),
    CYAN("cyan", 2000000000),
    GREEN("green", 2000000000),
    LIME("lime", 2000000000),
    YELLOW("yellow", 2000000000),
    ORANGE("orange", 2000000000),
    WHITE("white", 2000000000),
    FADING("fading", 2000000000);

    public static final EnumMatter[] VALUES = values();

    private final String name;
    private final int burnTime;

    EnumMatter(String name, int burnTime) {
        this.name = name;
        this.burnTime = burnTime;
    }

    public String getName() {
        return name;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public static EnumMatter byMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            return MAGENTA;
        }
        return VALUES[meta];
    }
}
