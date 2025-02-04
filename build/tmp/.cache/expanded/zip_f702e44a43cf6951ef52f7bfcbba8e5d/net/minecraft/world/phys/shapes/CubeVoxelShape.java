package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class CubeVoxelShape extends VoxelShape {
    protected CubeVoxelShape(DiscreteVoxelShape p_82765_) {
        super(p_82765_);
    }

    @Override
    public DoubleList getCoords(Direction.Axis pAxis) {
        return new CubePointRange(this.shape.getSize(pAxis));
    }

    @Override
    protected int findIndex(Direction.Axis pAxis, double pPosition) {
        int i = this.shape.getSize(pAxis);
        return Mth.floor(Mth.clamp(pPosition * (double)i, -1.0, (double)i));
    }
}