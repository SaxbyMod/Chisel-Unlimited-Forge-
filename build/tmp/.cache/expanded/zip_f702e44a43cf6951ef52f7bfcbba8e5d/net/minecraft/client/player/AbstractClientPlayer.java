package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayer extends Player {
    @Nullable
    private PlayerInfo playerInfo;
    protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final ClientLevel clientLevel;
    public float walkDistO;
    public float walkDist;

    public AbstractClientPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pClientLevel.getSharedSpawnPos(), pClientLevel.getSharedSpawnAngle(), pGameProfile);
        this.clientLevel = pClientLevel;
    }

    @Override
    public boolean isSpectator() {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo != null && playerinfo.getGameMode() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo != null && playerinfo.getGameMode() == GameType.CREATIVE;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }

        return this.playerInfo;
    }

    @Override
    public void tick() {
        this.walkDistO = this.walkDist;
        this.deltaMovementOnPreviousTick = this.getDeltaMovement();
        super.tick();
    }

    public Vec3 getDeltaMovementLerped(float pPatialTick) {
        return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), (double)pPatialTick);
    }

    public PlayerSkin getSkin() {
        PlayerInfo playerinfo = this.getPlayerInfo();
        return playerinfo == null ? DefaultPlayerSkin.get(this.getUUID()) : playerinfo.getSkin();
    }

    public float getFieldOfViewModifier(boolean pIsFirstPerson, float pFovEffectScale) {
        float f = 1.0F;
        if (this.getAbilities().flying) {
            f *= 1.1F;
        }

        float f1 = this.getAbilities().getWalkingSpeed();
        if (f1 != 0.0F) {
            float f2 = (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / f1;
            f *= (f2 + 1.0F) / 2.0F;
        }

        if (this.isUsingItem()) {
            if (this.getUseItem().is(Items.BOW)) {
                float f3 = Math.min((float)this.getTicksUsingItem() / 20.0F, 1.0F);
                f *= 1.0F - Mth.square(f3) * 0.15F;
            } else if (pIsFirstPerson && this.isScoping()) {
                return 0.1F;
            }
        }

        return net.minecraftforge.client.event.ForgeEventFactoryClient.fireFovModifierEvent(this, f, pFovEffectScale).getNewFovModifier();
    }
}
