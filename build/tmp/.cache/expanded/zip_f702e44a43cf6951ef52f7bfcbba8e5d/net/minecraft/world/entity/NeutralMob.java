package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface NeutralMob {
    String TAG_ANGER_TIME = "AngerTime";
    String TAG_ANGRY_AT = "AngryAt";

    int getRemainingPersistentAngerTime();

    void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime);

    @Nullable
    UUID getPersistentAngerTarget();

    void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget);

    void startPersistentAngerTimer();

    default void addPersistentAngerSaveData(CompoundTag pNbt) {
        pNbt.putInt("AngerTime", this.getRemainingPersistentAngerTime());
        if (this.getPersistentAngerTarget() != null) {
            pNbt.putUUID("AngryAt", this.getPersistentAngerTarget());
        }
    }

    default void readPersistentAngerSaveData(Level pLevel, CompoundTag pTag) {
        this.setRemainingPersistentAngerTime(pTag.getInt("AngerTime"));
        if (pLevel instanceof ServerLevel) {
            if (!pTag.hasUUID("AngryAt")) {
                this.setPersistentAngerTarget(null);
            } else {
                UUID uuid = pTag.getUUID("AngryAt");
                this.setPersistentAngerTarget(uuid);
                Entity entity = ((ServerLevel)pLevel).getEntity(uuid);
                if (entity != null) {
                    if (entity instanceof Mob mob) {
                        this.setTarget(mob);
                        this.setLastHurtByMob(mob);
                    }

                    if (entity instanceof Player player) {
                        this.setTarget(player);
                        this.setLastHurtByPlayer(player);
                    }
                }
            }
        }
    }

    default void updatePersistentAnger(ServerLevel pServerLevel, boolean pUpdateAnger) {
        LivingEntity livingentity = this.getTarget();
        UUID uuid = this.getPersistentAngerTarget();
        if ((livingentity == null || livingentity.isDeadOrDying()) && uuid != null && pServerLevel.getEntity(uuid) instanceof Mob) {
            this.stopBeingAngry();
        } else {
            if (livingentity != null && !Objects.equals(uuid, livingentity.getUUID())) {
                this.setPersistentAngerTarget(livingentity.getUUID());
                this.startPersistentAngerTimer();
            }

            if (this.getRemainingPersistentAngerTime() > 0 && (livingentity == null || livingentity.getType() != EntityType.PLAYER || !pUpdateAnger)) {
                this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
                if (this.getRemainingPersistentAngerTime() == 0) {
                    this.stopBeingAngry();
                }
            }
        }
    }

    default boolean isAngryAt(LivingEntity pEntity, ServerLevel pLevel) {
        if (!this.canAttack(pEntity)) {
            return false;
        } else {
            return pEntity.getType() == EntityType.PLAYER && this.isAngryAtAllPlayers(pLevel) ? true : pEntity.getUUID().equals(this.getPersistentAngerTarget());
        }
    }

    default boolean isAngryAtAllPlayers(ServerLevel pLevel) {
        return pLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
    }

    default boolean isAngry() {
        return this.getRemainingPersistentAngerTime() > 0;
    }

    default void playerDied(ServerLevel pLevel, Player pPlayer) {
        if (pLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            if (pPlayer.getUUID().equals(this.getPersistentAngerTarget())) {
                this.stopBeingAngry();
            }
        }
    }

    default void forgetCurrentTargetAndRefreshUniversalAnger() {
        this.stopBeingAngry();
        this.startPersistentAngerTimer();
    }

    default void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
        this.setRemainingPersistentAngerTime(0);
    }

    @Nullable
    LivingEntity getLastHurtByMob();

    void setLastHurtByMob(@Nullable LivingEntity pLivingEntity);

    void setLastHurtByPlayer(@Nullable Player pPlayer);

    void setTarget(@Nullable LivingEntity pLivingEntity);

    boolean canAttack(LivingEntity pEntity);

    @Nullable
    LivingEntity getTarget();
}