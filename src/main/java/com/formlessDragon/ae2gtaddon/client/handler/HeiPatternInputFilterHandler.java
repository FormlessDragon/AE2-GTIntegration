package com.formlessDragon.ae2gtaddon.client.handler;

import ae2.api.client.PatternRecipeTransferEvent;
import ae2.api.client.PatternRecipeTransferSlot;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import ae2.container.me.patternencode.ContainerPatternEncodingTerm;
import ae2.parts.encoding.EncodingMode;
import com.formlessDragon.ae2gtaddon.integration.hei.ProcessingPatternCircuitImportMode;
import com.formlessDragon.ae2gtaddon.util.Matcher;
import com.formlessDragon.ae2gtaddon.util.Util;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.function.Predicate;

public final class HeiPatternInputFilterHandler {

    @SubscribeEvent
    public void onPatternRecipeTransfer(PatternRecipeTransferEvent event) {
        if (event.getMode() != EncodingMode.PROCESSING) {
            return;
        }

        ContainerPatternEncodingTerm container = event.getContainer();
        boolean ignoreCircuit = container instanceof ProcessingPatternCircuitImportMode importMode
            && importMode.ae2gtaddon$ignoreCircuit();
        boolean assemblyLine = Util.isAssemblyLineRecipe(event.getRecipeCategoryUid());
        if (!ignoreCircuit && !assemblyLine) {
            return;
        }

        List<PatternRecipeTransferSlot> inputSlots = event.getInputSlots();
        for (PatternRecipeTransferSlot slot : inputSlots) {
            if (ignoreCircuit) {
                dropExcludedInput(slot, Matcher.Circuit);
            }
            if (assemblyLine) {
                dropExcludedInput(slot, Matcher.DataItem);
            }
        }
    }

    static void dropExcludedInput(PatternRecipeTransferSlot slot, Predicate<AEKey> isExcluded) {
        GenericStack stack = slot.getStack();
        if (stack != null && stack.what() != null && isExcluded.test(stack.what())) {
            slot.clear();
            return;
        }
        slot.getCandidates().removeIf(key -> key != null && isExcluded.test(key));
    }
}
