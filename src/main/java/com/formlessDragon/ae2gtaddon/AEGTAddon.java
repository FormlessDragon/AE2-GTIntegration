package com.formlessDragon.ae2gtaddon;

import com.formlessDragon.ae2gtaddon.container.ExtendedEncodingTermGuiHandler;
import com.formlessDragon.ae2gtaddon.proxy.IProxy;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "after:ae2")
public class AEGTAddon {
    public static AEGTAddon instance;

    @SidedProxy(modId = Tags.MOD_ID, clientSide = "com.formlessDragon.ae2gtaddon.proxy.ClientProxy", serverSide = "com.formlessDragon.ae2gtaddon.proxy.CommonProxy")
    public static IProxy proxy;

    public AEGTAddon() {
        instance = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ExtendedEncodingTermGuiHandler());
        proxy.init();
    }

}
