package net.minecraft.client.gui.screens.reporting;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.NameReport;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NameReportScreen extends AbstractReportScreen<NameReport.Builder> {
    private static final Component TITLE = Component.translatable("gui.abuseReport.name.title");
    private static final Component COMMENT_BOX_LABEL = Component.translatable("gui.abuseReport.name.comment_box_label");
    @Nullable
    private MultiLineEditBox commentBox;

    private NameReportScreen(Screen pLastScreen, ReportingContext pReportingContext, NameReport.Builder pReportBuilder) {
        super(TITLE, pLastScreen, pReportingContext, pReportBuilder);
    }

    public NameReportScreen(Screen pLastScreen, ReportingContext pReportingContext, UUID pReportedProfileId, String pReportedName) {
        this(pLastScreen, pReportingContext, new NameReport.Builder(pReportedProfileId, pReportedName, pReportingContext.sender().reportLimits()));
    }

    public NameReportScreen(Screen pLastScreen, ReportingContext pReportingContext, NameReport pReport) {
        this(pLastScreen, pReportingContext, new NameReport.Builder(pReport, pReportingContext.sender().reportLimits()));
    }

    @Override
    protected void addContent() {
        Component component = Component.literal(this.reportBuilder.report().getReportedName()).withStyle(ChatFormatting.YELLOW);
        this.layout
            .addChild(
                new StringWidget(Component.translatable("gui.abuseReport.name.reporting", component), this.font),
                p_357694_ -> p_357694_.alignHorizontallyCenter().padding(0, 8)
            );
        this.commentBox = this.createCommentBox(280, 9 * 8, p_357693_ -> {
            this.reportBuilder.setComments(p_357693_);
            this.onReportChanged();
        });
        this.layout.addChild(CommonLayouts.labeledElement(this.font, this.commentBox, COMMENT_BOX_LABEL, p_299823_ -> p_299823_.paddingBottom(12)));
    }

    @Override
    public boolean mouseReleased(double p_297585_, double p_300170_, int p_297299_) {
        if (super.mouseReleased(p_297585_, p_300170_, p_297299_)) {
            return true;
        } else {
            return this.commentBox != null ? this.commentBox.mouseReleased(p_297585_, p_300170_, p_297299_) : false;
        }
    }
}