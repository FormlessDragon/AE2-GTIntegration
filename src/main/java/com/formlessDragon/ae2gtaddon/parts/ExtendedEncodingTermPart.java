package com.formlessDragon.ae2gtaddon.parts;

import ae2.api.config.Settings;
import ae2.api.config.YesNo;
import ae2.api.parts.IPartItem;
import ae2.api.parts.IPartModel;
import ae2.api.util.IConfigManagerBuilder;
import ae2.core.AppEng;
import ae2.container.ISubGui;
import ae2.helpers.IPatternTerminalGuiHost;
import ae2.helpers.IPatternTerminalLogicHost;
import ae2.items.parts.PartModels;
import ae2.parts.PartModel;
import ae2.parts.reporting.AbstractTerminalPart;
import com.formlessDragon.ae2gtaddon.AEGTAddon;
import com.formlessDragon.ae2gtaddon.container.ExtendedEncodingTermGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ExtendedEncodingTermPart extends AbstractTerminalPart
    implements IPatternTerminalLogicHost, IPatternTerminalGuiHost {
    @PartModels
    public static final ResourceLocation MODEL_OFF = AppEng.makeId("part/pattern_encoding_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = AppEng.makeId("part/pattern_encoding_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final ExtendedEncodingLogic logic = new ExtendedEncodingLogic(this);

    public ExtendedEncodingTermPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.PATTERN_AUTO_FILL, YesNo.NO);
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        super.addAdditionalDrops(drops, wrenched);
        for (var stack : this.logic.getBlankPatternInv()) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
        for (var stack : this.logic.getEncodedPatternInv()) {
            if (!stack.isEmpty()) {
                drops.add(stack);
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.logic.getBlankPatternInv().clear();
        this.logic.getEncodedPatternInv().clear();
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.logic.readFromNBT(data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.logic.writeToNBT(data);
    }

    @Override
    public boolean onUseWithoutItem(EntityPlayer player, Vec3d pos) {
        if (!player.world.isRemote) {
            openEncodingTermGui(player, false);
        }
        return true;
    }

    @Override
    public void returnToMainContainer(EntityPlayer player, ISubGui subGui) {
        openEncodingTermGui(player, true);
    }

    private void openEncodingTermGui(EntityPlayer player, boolean returnedFromSubScreen) {
        EnumFacing side = getSide();
        int encodedSide = side == null ? EnumFacing.VALUES.length : side.ordinal();
        BlockPos pos = getHost().getLocation().getPos();
        player.openGui(AEGTAddon.instance, ExtendedEncodingTermGuiHandler.guiId(returnedFromSubScreen),
            player.world, pos.getX(), (encodedSide << 8) | (pos.getY() & 255), pos.getZ());
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public ExtendedEncodingLogic getLogic() {
        return this.logic;
    }

    @Override
    public void markForSave() {
        getHost().markForSave();
    }
}
