package com.latmod.mods.projectex;

public enum StarTier {
    EIN("ein", 204800000.0, 838860800000.0),
    ZWEI("zwei", 819200000.0, 3355443200000.0),
    DREI("drei", 3276800000.0, 13421772800000.0),
    VIER("vier", 13107200000.0, 53687091200000.0),
    SPHERE("sphere", 52428800000.0, 214748364800000.0),
    OMEGA("omega", 209715200000.0, 858993459200000.0);

    public static final StarTier[] VALUES = values();

    private final String name;
    private final double magnumMaxEmc;
    private final double colossalMaxEmc;

    StarTier(String name, double magnumMaxEmc, double colossalMaxEmc) {
        this.name = name;
        this.magnumMaxEmc = magnumMaxEmc;
        this.colossalMaxEmc = colossalMaxEmc;
    }

    public String getName() {
        return name;
    }

    public double getMagnumMaxEmc() {
        return magnumMaxEmc;
    }

    public double getColossalMaxEmc() {
        return colossalMaxEmc;
    }

    public static StarTier byMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            return EIN;
        }
        return VALUES[meta];
    }
}
