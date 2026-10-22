package com.formlessDragon.ae2gtaddon.mixin.ae2;

import ae2.container.me.patternencode.ContainerPatternEncodingTerm;
import com.formlessDragon.ae2gtaddon.integration.hei.AutoPatternItemNaming;
import com.formlessDragon.ae2gtaddon.integration.hei.ProcessingPatternCircuitImportMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ContainerPatternEncodingTerm.class, remap = false)
public abstract class MixinContainerPatternEncodingTerm
    implements ProcessingPatternCircuitImportMode, AutoPatternItemNaming {
    @Unique
    private boolean ae2gtaddon$ignoreCircuit;

    @Unique
    private boolean ae2gtaddon$autoNamePatternItems;

    @Override
    public boolean ae2gtaddon$ignoreCircuit() {
        return this.ae2gtaddon$ignoreCircuit;
    }

    @Override
    public void ae2gtaddon$setIgnoreCircuit(boolean ignoreCircuit) {
        this.ae2gtaddon$ignoreCircuit = ignoreCircuit;
    }

    @Override
    public boolean ae2gtaddon$autoNamePatternItems() {
        return this.ae2gtaddon$autoNamePatternItems;
    }

    @Override
    public void ae2gtaddon$setAutoNamePatternItems(boolean autoName) {
        this.ae2gtaddon$autoNamePatternItems = autoName;
    }
}
