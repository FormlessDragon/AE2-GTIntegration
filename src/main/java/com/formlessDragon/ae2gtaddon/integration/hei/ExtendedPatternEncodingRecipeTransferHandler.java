package com.formlessDragon.ae2gtaddon.integration.hei;

import ae2.api.client.PatternImportPriorityContext;
import ae2.api.crafting.PatternDetailsHelper;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import ae2.client.patternimport.PatternImportPrioritySelector;
import ae2.container.me.common.GridInventoryEntry;
import ae2.container.me.common.IClientRepo;
import ae2.container.me.patternencode.ContainerPatternEncodingTerm;
import ae2.container.slot.FakeSlot;
import ae2.core.network.InitNetwork;
import ae2.core.network.serverbound.InventoryActionPacket;
import ae2.helpers.InventoryAction;
import ae2.integration.modules.hei.GenericIngredientHelper;
import ae2.integration.modules.hei.HeiBookmarkHelper;
import com.formlessDragon.ae2gtaddon.container.ContainerExtendedEncodingTerm;
import com.formlessDragon.ae2gtaddon.parts.ExtendedEncodingMode;
import com.formlessDragon.ae2gtaddon.util.Matcher;
import com.formlessDragon.ae2gtaddon.util.Util;
import com.google.common.math.LongMath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.TooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ExtendedPatternEncodingRecipeTransferHandler
        implements IRecipeTransferHandler<ContainerExtendedEncodingTerm> {
    private static final int CRAFTING_GRID_SIZE = 9;

    private static RecipePlan planProcessingRecipe(ContainerExtendedEncodingTerm container,
                                                    IRecipeLayout recipeLayout,
                                                    String recipeCategoryUid,
                                                    boolean autoName) {
        PatternImportPriorityContext context = ExtendedPatternImportPriorityContext.create(container,
                HeiBookmarkHelper.getBookmarkedStacks());

        List<List<GenericStack>> inputs = getGenericInputs(recipeLayout);
        if (Util.isAssemblyLineRecipe(recipeCategoryUid)) {
            inputs = filterIngredients(inputs, Matcher.DataItem);
        }
        if (container.ignoreCircuit()) {
            inputs = filterIngredients(inputs, Matcher.Circuit);
        }

        RecipeInputs plannedInputs = planInputs(inputs, context, autoName,
            container.isMergeHeiProcessingInputs());
        List<GenericStack> outputs = planOutputs(getGenericOutputs(recipeLayout), context);
        ExtendedEncodingMode mode = ExtendedEncodingMode.select(plannedInputs.stacks(), outputs,
            container.getExtendedMode());
        validateAmounts(plannedInputs.stacks());
        validateAmounts(outputs);
        return new RecipePlan(mode, plannedInputs, outputs);
    }

    private static List<List<GenericStack>> getGenericInputs(IRecipeLayout recipeLayout) {
        return GenericIngredientHelper.getIngredients(recipeLayout, true, false, CRAFTING_GRID_SIZE);
    }

    private static List<List<GenericStack>> getGenericOutputs(IRecipeLayout recipeLayout) {
        return GenericIngredientHelper.getIngredients(recipeLayout, false, false, CRAFTING_GRID_SIZE);
    }

    private static List<GenericStack> planOutputs(List<List<GenericStack>> possibleInputsBySlot,
                                                 PatternImportPriorityContext context) {
        List<GenericStack> encodedInputs = new ObjectArrayList<>();
        for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
            if (!genericIngredient.isEmpty()) {
                GenericStack selected = PatternImportPrioritySelector.selectIngredient(genericIngredient, context,
                        false);
                if (isValidStack(selected)) {
                    addOrMerge(encodedInputs, selected);
                }
            }
        }

        return encodedInputs;
    }

    private static RecipeInputs planInputs(List<List<GenericStack>> possibleInputsBySlot,
                                           PatternImportPriorityContext context,
                                           boolean autoName,
                                           boolean mergeInputs) {
        List<GenericStack> selectedInputs = new ObjectArrayList<>();
        List<List<GenericStack>> selectedIngredients = new ObjectArrayList<>();
        for (List<GenericStack> genericIngredient : possibleInputsBySlot) {
            if (!genericIngredient.isEmpty()) {
                GenericStack selected = PatternImportPrioritySelector.selectIngredient(genericIngredient, context,
                        false);
                if (isValidStack(selected)) {
                    selectedInputs.add(selected);
                    selectedIngredients.add(genericIngredient);
                }
            }
        }

        if (autoName) {
            selectedInputs = HeiPatternItemAutoNamer.autoNameRepeatedItems(selectedInputs);
        }

        List<GenericStack> encodedInputs = new ObjectArrayList<>();
        List<List<AEKey>> candidatesByEncodedSlot = new ObjectArrayList<>();
        for (int index = 0; index < selectedInputs.size(); index++) {
            GenericStack selected = selectedInputs.get(index);
            List<GenericStack> genericIngredient = selectedIngredients.get(index);
            if (mergeInputs) {
                addOrMergeInput(encodedInputs, candidatesByEncodedSlot, selected, genericIngredient);
            } else {
                addInput(encodedInputs, candidatesByEncodedSlot, selected, genericIngredient);
            }
        }

        List<List<String>> candidateKeyTags = new ObjectArrayList<>(candidatesByEncodedSlot.size());
        for (List<AEKey> candidates : candidatesByEncodedSlot) {
            List<String> tags = new ObjectArrayList<>(candidates.size());
            for (AEKey candidate : candidates) {
                tags.add(candidate.toTagGeneric().toString());
            }
            candidateKeyTags.add(tags);
        }
        return new RecipeInputs(encodedInputs, candidateKeyTags);
    }

    private static void validateAmounts(List<GenericStack> stacks) {
        if (stacks.isEmpty() || stacks.stream().anyMatch(stack -> stack.amount() <= 0
                || stack.amount() > PatternDetailsHelper.MAX_PROCESSING_PATTERN_AMOUNT)) {
            throw new IllegalArgumentException("Recipe contains empty or oversized ingredients");
        }
    }

    private static void writeSlots(ContainerExtendedEncodingTerm container, FakeSlot[] slots,
                                    List<GenericStack> stacks) {
        if (stacks.size() > slots.length) {
            throw new IllegalArgumentException("Recipe exceeds the selected layout");
        }
        for (int index = 0; index < slots.length; index++) {
            setFilter(container, slots[index], index < stacks.size()
                ? GenericStack.wrapInItemStack(stacks.get(index)) : ItemStack.EMPTY);
        }
    }

    private record RecipeInputs(List<GenericStack> stacks, List<List<String>> candidates) {
    }

    private record RecipePlan(ExtendedEncodingMode mode, RecipeInputs inputs, List<GenericStack> outputs) {
    }

    private static void addOrMergeInput(List<GenericStack> stacks, List<List<AEKey>> candidatesByEncodedSlot,
                                        GenericStack newStack, List<GenericStack> possibleInputs) {
        List<AEKey> newCandidates = getCandidateKeys(possibleInputs);
        if (!isValidStack(newStack)) {
            return;
        }
        if (!newCandidates.contains(newStack.what())) {
            newCandidates.add(newStack.what());
        }
        for (int i = 0; i < stacks.size(); i++) {
            GenericStack existingStack = stacks.get(i);
            if (isValidStack(existingStack) && Objects.equals(existingStack.what(), newStack.what())) {
                long newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
                stacks.set(i, new GenericStack(newStack.what(), newAmount));
                candidatesByEncodedSlot.set(i, intersectCandidates(candidatesByEncodedSlot.get(i), newCandidates));

                long overflow = newStack.amount() - (newAmount - existingStack.amount());
                if (overflow > 0) {
                    stacks.add(new GenericStack(newStack.what(), overflow));
                    candidatesByEncodedSlot.add(new ObjectArrayList<>(newCandidates));
                }
                return;
            }
        }

        stacks.add(newStack);
        candidatesByEncodedSlot.add(new ObjectArrayList<>(newCandidates));
    }

    private static void addInput(List<GenericStack> stacks, List<List<AEKey>> candidatesByEncodedSlot,
                                 GenericStack newStack, List<GenericStack> possibleInputs) {
        List<AEKey> candidates = getCandidateKeys(possibleInputs);
        if (!isValidStack(newStack)) {
            return;
        }
        if (!candidates.contains(newStack.what())) {
            candidates.add(newStack.what());
        }
        stacks.add(newStack);
        candidatesByEncodedSlot.add(new ObjectArrayList<>(candidates));
    }

    private static List<AEKey> getCandidateKeys(List<GenericStack> possibleInputs) {
        List<AEKey> candidates = new ObjectArrayList<>();
        for (GenericStack possibleInput : possibleInputs) {
            if (isValidStack(possibleInput) && !candidates.contains(possibleInput.what())) {
                candidates.add(possibleInput.what());
            }
        }
        return candidates;
    }

    private static List<AEKey> intersectCandidates(List<AEKey> first, List<AEKey> second) {
        List<AEKey> result = new ObjectArrayList<>();
        for (AEKey key : first) {
            if (second.contains(key)) {
                result.add(key);
            }
        }
        return result;
    }

    private static void addOrMerge(List<GenericStack> stacks, GenericStack newStack) {
        if (!isValidStack(newStack)) {
            return;
        }
        for (int i = 0; i < stacks.size(); i++) {
            GenericStack existingStack = stacks.get(i);
            if (isValidStack(existingStack) && Objects.equals(existingStack.what(), newStack.what())) {
                long newAmount = LongMath.saturatedAdd(existingStack.amount(), newStack.amount());
                stacks.set(i, new GenericStack(newStack.what(), newAmount));

                long overflow = newStack.amount() - (newAmount - existingStack.amount());
                if (overflow > 0) {
                    stacks.add(new GenericStack(newStack.what(), overflow));
                }
                return;
            }
        }

        stacks.add(newStack);
    }

    private static void setFilter(ContainerExtendedEncodingTerm container, FakeSlot slot, ItemStack stack) {
        if (slot == null) {
            return;
        }
        InitNetwork.sendToServer(new InventoryActionPacket(
                container.windowId,
                InventoryAction.SET_FILTER,
                slot.slotNumber,
                stack));
    }

    @Override
    @NotNull
    public Class<ContainerExtendedEncodingTerm> getContainerClass() {
        return ContainerExtendedEncodingTerm.class;
    }

    private static String getRecipeCategoryUid(IRecipeLayout recipeLayout) {
        IRecipeCategory<?> category = recipeLayout.getRecipeCategory();
        return category.getUid();
    }

    static List<List<GenericStack>> filterIngredients(
            List<List<GenericStack>> inputs,
            Predicate<AEKey> isExcluded
    ) {
        List<List<GenericStack>> filteredInputs = new ObjectArrayList<>(inputs.size());
        for (List<GenericStack> candidates : inputs) {
            List<GenericStack> filteredCandidates = new ObjectArrayList<>(candidates.size());
            for (GenericStack candidate : candidates) {
                if (isValidStack(candidate) && !isExcluded.test(candidate.what())) {
                    filteredCandidates.add(candidate);
                }
            }
            filteredInputs.add(filteredCandidates);
        }
        return filteredInputs;
    }

    private static boolean isValidStack(@Nullable GenericStack stack) {
        return stack != null && stack.what() != null && stack.amount() > 0;
    }

    @Override
    public IRecipeTransferError transferRecipe(@NotNull ContainerExtendedEncodingTerm container,
                                               @NotNull IRecipeLayout recipeLayout,
                                               @NotNull EntityPlayer player,
                                               boolean maxTransfer,
                                               boolean doTransfer) {
        String recipeCategoryUid = getRecipeCategoryUid(recipeLayout);
        if (VanillaRecipeCategoryUid.INFORMATION.equals(recipeCategoryUid)
                || VanillaRecipeCategoryUid.FUEL.equals(recipeCategoryUid)) {
            return null;
        }

        boolean assemblyLine = Util.isAssemblyLineRecipe(recipeCategoryUid);
        boolean autoName = HeiPatternItemAutoNamer.isEnabled(container.isAutoNamingItems(), recipeCategoryUid);
        RecipePlan plan;
        try {
            plan = planProcessingRecipe(container, recipeLayout, recipeCategoryUid, autoName);
        } catch (IllegalArgumentException exception) {
            return new CapacityError();
        }
        if (doTransfer) {
            if (assemblyLine) {
                // Assembly lines always use auto naming; the panel reflects the new state on the next frame.
                container.setAutoNamingItems(true);
                // Repeated assembly line inputs have to stay in separate slots, so make the existing switch match.
                container.setMergeHeiProcessingInputs(false);
            }
            container.prepareProcessingRecipe(plan.mode());
            writeSlots(container, container.getActiveProcessingInputSlots(), plan.inputs().stacks());
            writeSlots(container, container.getActiveProcessingOutputSlots(), plan.outputs());
            container.setHeiProcessingRecipe(recipeCategoryUid, plan.inputs().candidates());
        }
        return null;
    }

    private static final class CapacityError implements IRecipeTransferError {
        @Override
        @NotNull
        public Type getType() {
            return Type.USER_FACING;
        }

        @Override
        public void showError(@NotNull Minecraft minecraft, int mouseX, int mouseY, @NotNull IRecipeLayout recipeLayout,
                              int recipeX, int recipeY) {
            TooltipRenderer.drawHoveringText(minecraft,
                new TextComponentTranslation("gui.ae2gtaddon.RecipeDoesNotFit").getFormattedText(), mouseX, mouseY);
        }
    }

    private static final class ExtendedPatternImportPriorityContext implements PatternImportPriorityContext {
        @Nullable
        private final IClientRepo clientRepo;
        private final List<GenericStack> bookmarkedStacks;
        private final List<AEKey> bookmarkedKeys;
        private final List<GridInventoryEntry> repoEntries;

        private ExtendedPatternImportPriorityContext(ContainerExtendedEncodingTerm container,
                                                     List<GenericStack> bookmarkedStacks) {
            Objects.requireNonNull(container, "container");
            this.clientRepo = container.getClientRepo();
            int bookmarkedCount = bookmarkedStacks != null ? bookmarkedStacks.size() : 0;
            this.bookmarkedStacks = new ObjectArrayList<>(bookmarkedCount);
            this.bookmarkedKeys = new ObjectArrayList<>(bookmarkedCount);
            if (bookmarkedStacks != null) {
                for (GenericStack bookmarkedStack : bookmarkedStacks) {
                    if (bookmarkedStack != null) {
                        this.bookmarkedStacks.add(bookmarkedStack);
                        this.bookmarkedKeys.add(bookmarkedStack.what());
                    }
                }
            }
            this.repoEntries = new ObjectArrayList<>();
            if (this.clientRepo != null) {
                this.repoEntries.addAll(this.clientRepo.getAllEntries());
            }
        }

        private static PatternImportPriorityContext create(ContainerExtendedEncodingTerm container,
                                                           List<GenericStack> bookmarkedStacks) {
            return new ExtendedPatternImportPriorityContext(container, bookmarkedStacks);
        }

        @Override
        public ContainerPatternEncodingTerm getContainer() {
            return null;
        }

        @Override
        @Nullable
        public IClientRepo getClientRepo() {
            return this.clientRepo;
        }

        @Override
        public List<GenericStack> getBookmarkedStacks() {
            return Collections.unmodifiableList(this.bookmarkedStacks);
        }

        @Override
        @Nullable
        public GridInventoryEntry getClientRepoEntry(GenericStack candidate) {
            if (candidate == null || candidate.what() == null) {
                return null;
            }
            GridInventoryEntry best = null;
            for (GridInventoryEntry entry : this.repoEntries) {
                if (entry != null && Objects.equals(entry.what(), candidate.what())) {
                    best = best == null ? entry : mergeEntries(best, entry);
                }
            }
            return best;
        }

        @Override
        public boolean isBookmarked(GenericStack candidate) {
            return candidate != null && this.bookmarkedKeys.contains(candidate.what());
        }

        @Override
        public boolean isCraftable(GenericStack candidate) {
            GridInventoryEntry entry = getClientRepoEntry(candidate);
            return entry != null && entry.craftable();
        }

        @Override
        public boolean isStored(GenericStack candidate) {
            GridInventoryEntry entry = getClientRepoEntry(candidate);
            return entry != null && entry.storedAmount() > 0;
        }

        private static GridInventoryEntry mergeEntries(GridInventoryEntry left, GridInventoryEntry right) {
            return new GridInventoryEntry(
                    left.serial(),
                    left.what(),
                    Math.max(left.storedAmount(), right.storedAmount()),
                    Math.max(left.requestableAmount(), right.requestableAmount()),
                    left.craftable() || right.craftable());
        }
    }
}
