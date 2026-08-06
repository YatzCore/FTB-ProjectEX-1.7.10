package com.latmod.mods.projectex;

public enum StarTier {
    EIN("ein", 204800000.0),
    ZWEI("zwei", 819200000.0),
    DREI("drei", 3276800000.0),
    VIER("vier", 13107200000.0),
    SPHERE("sphere", 52428800000.0),
    OMEGA("omega", 209715200000.0);

    public static final StarTier[] VALUES = values();

    private final String name;
    private final double magnumMaxEmc;

    StarTier(String name, double magnumMaxEmc) {
        this.name = name;
        this.magnumMaxEmc = magnumMaxEmc;
    }

    public String getName() {
        return name;
    }

    public double getMagnumMaxEmc() {
        return magnumMaxEmc;
    }

    public double getColossalMaxEmc() {
        return magnumMaxEmc * 400.0;
    }

    public static StarTier byMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            return EIN;
        }
        return VALUES[meta];
    }
}
