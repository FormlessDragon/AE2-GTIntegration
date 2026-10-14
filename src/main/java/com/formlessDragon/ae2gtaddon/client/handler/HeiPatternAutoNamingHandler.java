package com.formlessDragon.ae2gtaddon.client.handler;

import ae2.api.client.PatternRecipeTransferEvent;
import ae2.api.client.PatternRecipeTransferSlot;
import ae2.api.stacks.GenericStack;
import ae2.container.me.patternencode.ContainerPatternEncodingTerm;
import ae2.parts.encoding.EncodingMode;
import com.formlessDragon.ae2gtaddon.integration.hei.AutoPatternItemNaming;
import com.formlessDragon.ae2gtaddon.integration.hei.HeiPatternItemAutoNamer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public final class HeiPatternAutoNamingHandler {

    @SubscribeEvent
    public void onPatternRecipeTransfer(PatternRecipeTransferEvent event) {
        if (event.getMode() != EncodingMode.PROCESSING) {
            return;
        }

        ContainerPatternEncodingTerm container = event.getContainer();
        if (!(container instanceof AutoPatternItemNaming naming)) {
            return;
        }
        if (!HeiPatternItemAutoNamer.isEnabled(naming.ae2gtaddon$autoNamePatternItems(),
            event.getRecipeCategoryUid())) {
            return;
        }

        if (container.isMergeHeiProcessingInputs()) {
            container.setMergeHeiProcessingInputs(false);
        }

        List<PatternRecipeTransferSlot> inputSlots = event.getInputSlots();
        List<GenericStack> planned = new ObjectArrayList<>(inputSlots.size());
        for (PatternRecipeTransferSlot slot : inputSlots) {
            planned.add(slot.getStack());
        }

        List<GenericStack> renamed = HeiPatternItemAutoNamer.autoNameRepeatedItems(planned);
        for (int index = 0; index < renamed.size(); index++) {
            GenericStack replacement = renamed.get(index);
            if (replacement != null && replacement != planned.get(index)) {
                inputSlots.get(index).setStack(replacement);
            }
        }
    }
}
