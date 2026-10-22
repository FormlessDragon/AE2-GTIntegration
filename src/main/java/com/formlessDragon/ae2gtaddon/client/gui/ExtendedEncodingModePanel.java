package com.formlessDragon.ae2gtaddon.client.gui;

import ae2.client.Point;
import ae2.client.gui.ICompositeWidget;
import ae2.client.gui.Icon;
import ae2.client.gui.WidgetContainer;
import com.formlessDragon.ae2gtaddon.container.ContainerExtendedEncodingTerm;
import net.minecraft.util.text.ITextComponent;

import java.awt.Rectangle;

abstract class ExtendedEncodingModePanel implements ICompositeWidget {
    protected final GuiExtendedPatternEncodingTerm screen;
    protected final ContainerExtendedEncodingTerm container;
    protected final WidgetContainer widgets;
    protected Point position = Point.ZERO;
    protected int width;
    protected int height;
    protected boolean visible;

    ExtendedEncodingModePanel(GuiExtendedPatternEncodingTerm screen, WidgetContainer widgets) {
        this.screen = screen;
        this.container = screen.getContainer();
        this.widgets = widgets;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.position.x(), this.position.y(), this.width, this.height);
    }

    @Override
    public boolean hitTest(Point mousePos) {
        return false;
    }

    abstract Icon getIcon();

    public abstract ITextComponent getTabTooltip();
}
