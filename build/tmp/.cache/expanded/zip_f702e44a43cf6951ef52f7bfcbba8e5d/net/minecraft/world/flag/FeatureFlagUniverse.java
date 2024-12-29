package net.minecraft.world.flag;

public class FeatureFlagUniverse {
    private final String id;

    public FeatureFlagUniverse(String pId) {
        this.id = pId;
    }

    @Override
    public String toString() {
        return this.id;
    }
}