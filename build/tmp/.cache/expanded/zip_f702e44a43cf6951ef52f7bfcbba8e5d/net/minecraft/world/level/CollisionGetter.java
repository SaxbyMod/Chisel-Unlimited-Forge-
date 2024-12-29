package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter extends BlockGetter {
    WorldBorder getWorldBorder();

    @Nullable
    BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ);

    default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
        return true;
    }

    default boolean isUnobstructed(BlockState pState, BlockPos pPos, CollisionContext pContext) {
        VoxelShape voxelshape = pState.getCollisionShape(this, pPos, pContext);
        return voxelshape.isEmpty()
            || this.isUnobstructed(null, voxelshape.move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ()));
    }

    default boolean isUnobstructed(Entity pEntity) {
        return this.isUnobstructed(pEntity, Shapes.create(pEntity.getBoundingBox()));
    }

    default boolean noCollision(AABB pCollisionBox) {
        return this.noCollision(null, pCollisionBox);
    }

    default boolean noCollision(Entity pEntity) {
        return this.noCollision(pEntity, pEntity.getBoundingBox());
    }

    default boolean noCollision(@Nullable Entity pEntity, AABB pCollisionBox) {
        return this.noCollision(pEntity, pCollisionBox, false);
    }

    default boolean noCollision(@Nullable Entity pEntity, AABB pCollisionBox, boolean pCheckLiquid) {
        for (VoxelShape voxelshape : pCheckLiquid ? this.getBlockAndLiquidCollisions(pEntity, pCollisionBox) : this.getBlockCollisions(pEntity, pCollisionBox)) {
            if (!voxelshape.isEmpty()) {
                return false;
            }
        }

        if (!this.getEntityCollisions(pEntity, pCollisionBox).isEmpty()) {
            return false;
        } else if (pEntity == null) {
            return true;
        } else {
            VoxelShape voxelshape1 = this.borderCollision(pEntity, pCollisionBox);
            return voxelshape1 == null || !Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(pCollisionBox), BooleanOp.AND);
        }
    }

    default boolean noBlockCollision(@Nullable Entity pEntity, AABB pBoundingBox) {
        for (VoxelShape voxelshape : this.getBlockCollisions(pEntity, pBoundingBox)) {
            if (!voxelshape.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    List<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AABB pCollisionBox);

    default Iterable<VoxelShape> getCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        List<VoxelShape> list = this.getEntityCollisions(pEntity, pCollisionBox);
        Iterable<VoxelShape> iterable = this.getBlockCollisions(pEntity, pCollisionBox);
        return list.isEmpty() ? iterable : Iterables.concat(list, iterable);
    }

    default Iterable<VoxelShape> getBlockCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        return () -> new BlockCollisions<>(this, pEntity, pCollisionBox, false, (p_359944_, p_359945_) -> p_359945_);
    }

    default Iterable<VoxelShape> getBlockAndLiquidCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
        return () -> new BlockCollisions<>(this, CollisionContext.of(pEntity, true), pCollisionBox, false, (p_286215_, p_286216_) -> p_286216_);
    }

    @Nullable
    private VoxelShape borderCollision(Entity pEntity, AABB pBox) {
        WorldBorder worldborder = this.getWorldBorder();
        return worldborder.isInsideCloseToBorder(pEntity, pBox) ? worldborder.getCollisionShape() : null;
    }

    default BlockHitResult clipIncludingBorder(ClipContext pClipContext) {
        BlockHitResult blockhitresult = this.clip(pClipContext);
        WorldBorder worldborder = this.getWorldBorder();
        if (worldborder.isWithinBounds(pClipContext.getFrom()) && !worldborder.isWithinBounds(blockhitresult.getLocation())) {
            Vec3 vec3 = blockhitresult.getLocation().subtract(pClipContext.getFrom());
            Direction direction = Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z);
            Vec3 vec31 = worldborder.clampVec3ToBound(blockhitresult.getLocation());
            return new BlockHitResult(vec31, direction, BlockPos.containing(vec31), false, true);
        } else {
            return blockhitresult;
        }
    }

    default boolean collidesWithSuffocatingBlock(@Nullable Entity pEntity, AABB pBox) {
        BlockCollisions<VoxelShape> blockcollisions = new BlockCollisions<>(this, pEntity, pBox, true, (p_286211_, p_286212_) -> p_286212_);

        while (blockcollisions.hasNext()) {
            if (!blockcollisions.next().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    default Optional<BlockPos> findSupportingBlock(Entity pEntity, AABB pBox) {
        BlockPos blockpos = null;
        double d0 = Double.MAX_VALUE;
        BlockCollisions<BlockPos> blockcollisions = new BlockCollisions<>(this, pEntity, pBox, false, (p_286213_, p_286214_) -> p_286213_);

        while (blockcollisions.hasNext()) {
            BlockPos blockpos1 = blockcollisions.next();
            double d1 = blockpos1.distToCenterSqr(pEntity.position());
            if (d1 < d0 || d1 == d0 && (blockpos == null || blockpos.compareTo(blockpos1) < 0)) {
                blockpos = blockpos1.immutable();
                d0 = d1;
            }
        }

        return Optional.ofNullable(blockpos);
    }

    default Optional<Vec3> findFreePosition(@Nullable Entity pEntity, VoxelShape pShape, Vec3 pPos, double pX, double pY, double pZ) {
        if (pShape.isEmpty()) {
            return Optional.empty();
        } else {
            AABB aabb = pShape.bounds().inflate(pX, pY, pZ);
            VoxelShape voxelshape = StreamSupport.stream(this.getBlockCollisions(pEntity, aabb).spliterator(), false)
                .filter(p_186430_ -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(p_186430_.bounds()))
                .flatMap(p_186426_ -> p_186426_.toAabbs().stream())
                .map(p_186424_ -> p_186424_.inflate(pX / 2.0, pY / 2.0, pZ / 2.0))
                .map(Shapes::create)
                .reduce(Shapes.empty(), Shapes::or);
            VoxelShape voxelshape1 = Shapes.join(pShape, voxelshape, BooleanOp.ONLY_FIRST);
            return voxelshape1.closestPointTo(pPos);
        }
    }
}