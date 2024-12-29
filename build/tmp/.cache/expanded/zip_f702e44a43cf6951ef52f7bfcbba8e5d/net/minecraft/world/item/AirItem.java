package net.minecraft.world.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

public class AirItem extends Item {
    private final Block block;

    public AirItem(Block pBlock, Item.Properties pProperties) {
        super(pProperties);
        this.block = pBlock;
    }

    @Override
    public void appendHoverText(ItemStack p_40372_, Item.TooltipContext p_329845_, List<Component> p_40374_, TooltipFlag p_40375_) {
        super.appendHoverText(p_40372_, p_329845_, p_40374_, p_40375_);
        this.block.appendHoverText(p_40372_, p_329845_, p_40374_, p_40375_);
    }

    @Override
    public Component getName(ItemStack p_365938_) {
        return this.getName();
    }
}