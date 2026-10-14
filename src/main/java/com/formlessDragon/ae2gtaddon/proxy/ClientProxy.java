package com.formlessDragon.ae2gtaddon.proxy;

import ae2.api.util.AEColor;
import ae2.client.render.StaticItemColor;
import com.formlessDragon.ae2gtaddon.init.Items;
import com.formlessDragon.ae2gtaddon.client.handler.HeiPatternAutoNamingHandler;
import com.formlessDragon.ae2gtaddon.client.handler.HeiPatternInputFilterHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(new HeiPatternAutoNamingHandler());
        MinecraftForge.EVENT_BUS.register(new HeiPatternInputFilterHandler());
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
            new StaticItemColor(AEColor.TRANSPARENT), Items.EXTENDED_ENCODING_TERM.item());
    }
}
