package com.formlessDragon.ae2gtaddon.client.gui;

import ae2.api.config.ActionItems;
import ae2.client.Point;
import ae2.client.gui.Icon;
import ae2.client.gui.Tooltip;
import ae2.client.gui.WidgetContainer;
import ae2.client.gui.style.Blitter;
import ae2.client.gui.widgets.ActionButton;
import ae2.client.gui.widgets.Scrollbar;
import ae2.client.gui.widgets.SmallTextTooltipButton;
import ae2.client.gui.widgets.ToggleButton;
import ae2.container.SlotSemantics;
import ae2.core.localization.GuiText;
import ae2.core.localization.Tooltips;
import ae2.integration.Integrations;
import ae2.parts.encoding.ProcessingPatternAmountHelper;
import com.formlessDragon.ae2gtaddon.parts.ExtendedEncodingMode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.awt.Rectangle;
import java.util.List;

final class ExtendedProcessingEncodingPanel extends ExtendedEncodingModePanel {
    private static final int PANEL_WIDTH = 131;
    private static final int PANEL_HEIGHT = 82;
    private static final int VISIBLE_ROWS = 4;
    private static final int SLOT_SPACING = 18;
    private static final int INPUT_SLOT_X = 11;
    private static final int SLOT_Y = 7;
    private static final int MULTI_IN_OUTPUT_SLOT_X = 108;
    private static final int MULTI_OUT_OUTPUT_SLOT_X = 55;
    private static final float HALF_SIZE_ICON_SCALE = 0.5F;
    private static final Rectangle MOUSE_WHEEL_AREA = new Rectangle(-1, 1, PANEL_WIDTH, PANEL_HEIGHT);

    private final ExtendedEncodingMode mode;
    private final Blitter background;
    private final ActionButton clearBtn;
    private final ActionButton cycleOutputBtn;
    private final ActionButton clearSecondaryOutputsBtn;
    private final Scrollbar scrollbar;
    private final SmallTextTooltipButton[] amountButtons;
    private final ToggleButton ignoreCircuitButton;
    private final ToggleButton mergeHeiInputsBtn;
    private final ToggleButton autoNameButton;

    ExtendedProcessingEncodingPanel(GuiExtendedPatternEncodingTerm screen, WidgetContainer widgets,
                                   ExtendedEncodingMode mode) {
        super(screen, widgets);
        this.mode = mode;
        this.background = Blitter.texture(new ResourceLocation(
            "ae2gtaddon", "textures/guis/" + mode.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-') + ".png"),
            PANEL_WIDTH, PANEL_HEIGHT).src(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        this.clearBtn = new ActionButton(ActionItems.S_CLOSE, this.container::clear);
        this.clearBtn.setHalfSize(true);
        this.clearBtn.setDisableBackground(true);
        widgets.add(widgetId("processingClearPattern"), this.clearBtn);

        this.cycleOutputBtn = new ActionButton(ActionItems.S_CYCLE_PROCESSING_OUTPUT,
            this.container::cycleProcessingOutput);
        this.cycleOutputBtn.setHalfSize(true);
        this.cycleOutputBtn.setDisableBackground(true);
        widgets.add(widgetId("processingCycleOutput"), this.cycleOutputBtn);

        this.clearSecondaryOutputsBtn = new ActionButton(ActionItems.S_CLEAR_PROCESSING_SECONDARY_OUTPUTS,
            this.container::clearProcessingSecondaryOutputs);
        this.clearSecondaryOutputsBtn.setHalfSize(true);
        this.clearSecondaryOutputsBtn.setDisableBackground(true);
        widgets.add(widgetId("processingClearSecondaryOutputs"), this.clearSecondaryOutputsBtn);

        this.scrollbar = widgets.addScrollBar(widgetId("processingPatternModeScrollbar"), Scrollbar.SMALL);
        this.scrollbar.setRange(0, getScrollableRows(), VISIBLE_ROWS);
        this.scrollbar.setVisible(false);
        this.scrollbar.setCaptureMouseWheel(false);

        this.amountButtons = new SmallTextTooltipButton[]{
            amountButton("/2", ProcessingPatternAmountHelper.Operation.DIVIDE_2, GuiText.ProcessingPatternDivideDescription),
            amountButton("x2", ProcessingPatternAmountHelper.Operation.MULTIPLY_2,
                GuiText.ProcessingPatternMultiplyDescription),
            amountButton("/3", ProcessingPatternAmountHelper.Operation.DIVIDE_3, GuiText.ProcessingPatternDivideDescription),
            amountButton("x3", ProcessingPatternAmountHelper.Operation.MULTIPLY_3,
                GuiText.ProcessingPatternMultiplyDescription),
            amountButton("/5", ProcessingPatternAmountHelper.Operation.DIVIDE_5, GuiText.ProcessingPatternDivideDescription),
            amountButton("x5", ProcessingPatternAmountHelper.Operation.MULTIPLY_5,
                GuiText.ProcessingPatternMultiplyDescription)
        };
        for (int index = 0; index < this.amountButtons.length; index++) {
            widgets.add(widgetId("processingAmountButton" + index), this.amountButtons[index]);
        }

        if (Integrations.hei().isEnabled()) {
            this.ignoreCircuitButton = new ToggleButton(
                Icon.S_SUBSTITUTION_DISABLED,
                Icon.S_SUBSTITUTION_ENABLED,
                this.container::setIgnoreCircuit);
            this.ignoreCircuitButton.setHalfSize(true);
            this.ignoreCircuitButton.setDisableBackground(true);
            this.ignoreCircuitButton.setTooltipOn(List.of(
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImport"),
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImportOn")));
            this.ignoreCircuitButton.setTooltipOff(List.of(
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImport"),
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.IgnoreCircuitHeiImportOff")));
            widgets.add(widgetId("processingIgnoreCircuit"), this.ignoreCircuitButton);

            this.mergeHeiInputsBtn = new ToggleButton(
                Icon.S_MERGE_ENABLED,
                Icon.S_MERGE_DISABLED,
                this.container::setMergeHeiProcessingInputs
            );
            this.mergeHeiInputsBtn.setHalfSize(true);
            this.mergeHeiInputsBtn.setDisableBackground(true);
            this.mergeHeiInputsBtn.setTooltipOn(List.of(
                Tooltips.HeiProcessingMerge.text(),
                Tooltips.HeiProcessingMergeDescEnabled.text()));
            this.mergeHeiInputsBtn.setTooltipOff(List.of(
                Tooltips.HeiProcessingMerge.text(),
                Tooltips.HeiProcessingMergeDescDisabled.text()));
            widgets.add(widgetId("processingMergeHeiInputs"), this.mergeHeiInputsBtn);

            this.autoNameButton = new ToggleButton(
                Icon.PATTERN_AUTO_FILL_ON,
                Icon.PATTERN_AUTO_FILL_OFF,
                this::setAutoNamingItems
            );
            this.autoNameButton.setHalfSize(true);
            this.autoNameButton.setDisableBackground(true);
            this.autoNameButton.setIconScale(HALF_SIZE_ICON_SCALE);
            this.autoNameButton.setTooltipOn(List.of(
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImport"),
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImportOn")));
            this.autoNameButton.setTooltipOff(List.of(
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImport"),
                new TextComponentTranslation("gui.tooltips.ae2gtaddon.AutoNameHeiImportOff")));
            widgets.add(widgetId("processingAutoNaming"), this.autoNameButton);
        } else {
            this.ignoreCircuitButton = null;
            this.mergeHeiInputsBtn = null;
            this.autoNameButton = null;
        }
    }

    private void setAutoNamingItems(boolean autoName) {
        this.container.setAutoNamingItems(autoName);
        if (autoName) {
            // Auto naming keeps every occurrence in its own slot, so use the existing HEI merge switch to match.
            this.container.setMergeHeiProcessingInputs(false);
        }
    }

    @Override
    public void updateBeforeRender() {
        if (!this.visible) {
            return;
        }
        this.screen.repositionSlots(SlotSemantics.PROCESSING_INPUTS);
        this.screen.repositionSlots(SlotSemantics.PROCESSING_OUTPUTS);
        repositionProcessingSlots();

        this.cycleOutputBtn.setVisibility(this.visible && this.container.canCycleProcessingOutputs());
        this.clearSecondaryOutputsBtn.setVisibility(this.visible && this.container.canCycleProcessingOutputs());
        if (this.ignoreCircuitButton != null) {
            this.ignoreCircuitButton.setState(this.container.ignoreCircuit());
        }
        if (this.mergeHeiInputsBtn != null) {
            this.mergeHeiInputsBtn.setState(this.container.isMergeHeiProcessingInputs());
        }
        if (this.autoNameButton != null) {
            this.autoNameButton.setState(this.container.isAutoNamingItems());
        }
    }

    private void repositionProcessingSlots() {
        var inputSlots = this.container.getProcessingInputSlots();
        var outputSlots = this.container.getProcessingOutputSlots();
        if (inputSlots.length == 0 || outputSlots.length == 0) {
            return;
        }

        int scroll = this.scrollbar.getCurrentScroll();
        Point inputOrigin = this.position.move(INPUT_SLOT_X, SLOT_Y);
        Point outputOrigin = this.position.move(
            this.mode == ExtendedEncodingMode.MULTI_IN ? MULTI_IN_OUTPUT_SLOT_X : MULTI_OUT_OUTPUT_SLOT_X,
            SLOT_Y);
        repositionSlots(inputSlots, inputOrigin, this.mode.getInputSlots(), columnsForInputs(), scroll);
        repositionSlots(outputSlots, outputOrigin, this.mode.getOutputSlots(), columnsForOutputs(), scroll);
    }

    private int getScrollableRows() {
        int inputColumns = columnsForInputs();
        int outputColumns = columnsForOutputs();
        int inputRows = (this.mode.getInputSlots() + inputColumns - 1) / inputColumns;
        int outputRows = (this.mode.getOutputSlots() + outputColumns - 1) / outputColumns;
        return Math.max(0, Math.max(inputRows, outputRows) - VISIBLE_ROWS);
    }

    private static void repositionSlots(ae2.container.slot.FakeSlot[] slots, Point origin, int activeSlots,
                                        int columns, int scroll) {
        for (int i = 0; i < slots.length; i++) {
            int row = i / columns;
            int effectiveRow = row - scroll;
            slots[i].xPos = origin.x() + (i % columns) * SLOT_SPACING;
            slots[i].yPos = origin.y() + effectiveRow * SLOT_SPACING;
            slots[i].setActive(i < activeSlots && effectiveRow >= 0 && effectiveRow < VISIBLE_ROWS);
        }
    }

    private int columnsForInputs() {
        return this.mode == ExtendedEncodingMode.MULTI_IN ? 4 : 1;
    }

    private int columnsForOutputs() {
        return this.mode == ExtendedEncodingMode.MULTI_IN ? 1 : 4;
    }

    @Override
    public void drawBackgroundLayer(Rectangle bounds, Point mouse) {
        this.background.dest(
            bounds.x + this.position.x() - 1,
            bounds.y + this.position.y() + 1,
            PANEL_WIDTH,
            PANEL_HEIGHT).blit();
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_PROCESSING;
    }

    @Override
    public ITextComponent getTabTooltip() {
        return new TextComponentTranslation(this.mode == ExtendedEncodingMode.MULTI_IN
            ? "gui.ae2gtaddon.MultiIn"
            : "gui.ae2gtaddon.MultiOut");
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.scrollbar.setVisible(visible);
        this.clearBtn.setVisibility(visible);
        this.cycleOutputBtn.setVisibility(visible && this.container.canCycleProcessingOutputs());
        this.clearSecondaryOutputsBtn.setVisibility(visible && this.container.canCycleProcessingOutputs());
        if (this.ignoreCircuitButton != null) {
            this.ignoreCircuitButton.setVisibility(visible);
        }
        if (this.mergeHeiInputsBtn != null) {
            this.mergeHeiInputsBtn.setVisibility(visible);
        }
        if (this.autoNameButton != null) {
            this.autoNameButton.setVisibility(visible);
        }
        for (SmallTextTooltipButton button : this.amountButtons) {
            button.visible = visible;
            button.enabled = visible;
        }
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        if (!getMouseWheelArea().contains(mousePos.x(), mousePos.y())) {
            return false;
        }
        return this.scrollbar.onMouseWheel(mousePos, delta);
    }

    @Override
    public boolean wantsAllMouseWheelEvents() {
        return true;
    }

    private Rectangle getMouseWheelArea() {
        return new Rectangle(
            this.position.x() + MOUSE_WHEEL_AREA.x,
            this.position.y() + MOUSE_WHEEL_AREA.y,
            MOUSE_WHEEL_AREA.width,
            MOUSE_WHEEL_AREA.height);
    }

    @Override
    public boolean wantsAllMouseDownEvents() {
        return true;
    }

    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        if (this.ignoreCircuitButton != null
            && this.ignoreCircuitButton.visible
            && this.ignoreCircuitButton.isMouseOver()) {
            return new Tooltip(this.ignoreCircuitButton.getTooltipMessage());
        }
        return getEmptyOutputTooltip(mouseX, mouseY);
    }

    private Tooltip getEmptyOutputTooltip(int mouseX, int mouseY) {
        int columns = columnsForOutputs();
        int scroll = this.scrollbar.getCurrentScroll();
        var outputSlots = this.container.getProcessingOutputSlots();
        for (int slotIndex = 0; slotIndex < this.mode.getOutputSlots(); slotIndex++) {
            int effectiveRow = slotIndex / columns - scroll;
            if (effectiveRow < 0 || effectiveRow >= VISIBLE_ROWS) {
                continue;
            }

            var slot = outputSlots[slotIndex];
            if (slot.getStack().isEmpty()
                && new Rectangle(slot.xPos, slot.yPos, SLOT_SPACING, SLOT_SPACING).contains(mouseX, mouseY)) {
                boolean primary = slotIndex == 0;
                String tooltipKey = primary
                    ? "gui.ae2.PatternEncoding.primary_processing_result_tooltip"
                    : "gui.ae2.PatternEncoding.secondary_processing_result_tooltip";
                String hintKey = primary
                    ? "gui.ae2.PatternEncoding.primary_processing_result_hint"
                    : "gui.ae2.PatternEncoding.secondary_processing_result_hint";
                return new Tooltip(List.of(
                    new TextComponentTranslation(tooltipKey),
                    new TextComponentTranslation(hintKey)
                        .setStyle(new Style().setColor(TextFormatting.DARK_GRAY))));
            }
        }
        return null;
    }

    private SmallTextTooltipButton amountButton(String label, ProcessingPatternAmountHelper.Operation operation,
                                                GuiText tooltip) {
        return new SmallTextTooltipButton(8, 8, label,
            () -> List.of(tooltip.text(amountForOperation(operation))),
            () -> this.container.modifyProcessingPatternAmounts(operation),
            SmallTextTooltipButton.BackgroundStyle.TOOLBAR);
    }

    private static int amountForOperation(ProcessingPatternAmountHelper.Operation operation) {
        return switch (operation) {
            case MULTIPLY_2, DIVIDE_2 -> 2;
            case MULTIPLY_3, DIVIDE_3 -> 3;
            case MULTIPLY_5, DIVIDE_5 -> 5;
        };
    }

    private String widgetId(String baseId) {
        return (this.mode == ExtendedEncodingMode.MULTI_IN ? "multiIn" : "multiOut") + baseId;
    }
}
