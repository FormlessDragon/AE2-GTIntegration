package com.formlessDragon.ae2gtaddon.container;

import ae2.client.gui.style.GuiStyleManager;
import ae2.core.gui.PatternContainerGuiReturnContext;
import ae2.core.gui.locator.PartLocator;
import com.formlessDragon.ae2gtaddon.client.gui.GuiExtendedPatternEncodingTerm;
import com.formlessDragon.ae2gtaddon.parts.ExtendedEncodingTermPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

public class ExtendedEncodingTermGuiHandler implements IGuiHandler {
    public static final int GUI_ID = 0;
    private static final int RETURNED_FROM_SUBSCREEN_FLAG = 1 << 30;
    private static final String STYLE =
        "/screens/terminals/ae2gtaddon_extended_pattern_encoding_terminal.json";

    public static int guiId(boolean returnedFromSubScreen) {
        return returnedFromSubScreen ? GUI_ID | RETURNED_FROM_SUBSCREEN_FLAG : GUI_ID;
    }

    @Override
    public @Nullable Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return createContainer(id, player, world, x, y, z);
    }

    @Override
    public @Nullable Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        ContainerExtendedEncodingTerm container = createContainer(id, player, world, x, y, z);
        if (container == null) {
            return null;
        }
        return new GuiExtendedPatternEncodingTerm(container, player.inventory, container.getGuiTitle(),
            GuiStyleManager.loadStyleDoc(STYLE));
    }

    private static @Nullable ContainerExtendedEncodingTerm createContainer(int id, EntityPlayer player, World world,
                                                                          int x, int y, int z) {
        if (player == null || world == null) {
            return null;
        }
        PartLocator locator = partLocator(x, y, z);
        if (locator == null) {
            return null;
        }
        ExtendedEncodingTermPart part = locator.locate(player, ExtendedEncodingTermPart.class);
        if (part == null) {
            return null;
        }
        ContainerExtendedEncodingTerm container = new ContainerExtendedEncodingTerm(player.inventory, part);
        container.setLocator(locator);
        container.setReturnedFromSubScreen((id & RETURNED_FROM_SUBSCREEN_FLAG) != 0);
        container.setGuiTitle(defaultGuiTitle(container.getTarget()));
        return PatternContainerGuiReturnContext.initializeContainer(container);
    }

    private static @Nullable PartLocator partLocator(int x, int y, int z) {
        int side = y >> 8;
        if (side == EnumFacing.VALUES.length) {
            return new PartLocator(new BlockPos(x, y & 255, z), null);
        }
        if (side < 0 || side > EnumFacing.VALUES.length) {
            return null;
        }
        return new PartLocator(new BlockPos(x, y & 255, z), EnumFacing.VALUES[side]);
    }

    private static @Nullable ITextComponent defaultGuiTitle(Object host) {
        if (host instanceof ExtendedEncodingTermPart part && part.hasCustomName()) {
            String customName = part.getCustomName();
            return customName == null || customName.isEmpty() ? null : new TextComponentString(customName);
        }
        return null;
    }
}
