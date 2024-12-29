package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
    private final ResourceLocation menuResource;

    public ItemCombinerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, ResourceLocation pMenuResource) {
        super(pMenu, pPlayerInventory, pTitle);
        this.menuResource = pMenuResource;
    }

    protected void subInit() {
    }

    @Override
    protected void init() {
        super.init();
        this.subInit();
        this.menu.addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    public void render(GuiGraphics p_281810_, int p_283312_, int p_283420_, float p_282956_) {
        super.render(p_281810_, p_283312_, p_283420_, p_282956_);
        this.renderFg(p_281810_, p_283312_, p_283420_, p_282956_);
        this.renderTooltip(p_281810_, p_283312_, p_283420_);
    }

    protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
    }

    @Override
    protected void renderBg(GuiGraphics p_282749_, float p_283494_, int p_283098_, int p_282054_) {
        p_282749_.blit(RenderType::guiTextured, this.menuResource, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.renderErrorIcon(p_282749_, this.leftPos, this.topPos);
    }

    protected abstract void renderErrorIcon(GuiGraphics pGuiGraphics, int pX, int pY);

    @Override
    public void dataChanged(AbstractContainerMenu p_169759_, int p_169760_, int p_169761_) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack) {
    }
}