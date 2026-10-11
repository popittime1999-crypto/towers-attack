package com.gimr12.towers_attack;

import com.gimr12.towers_attack.block.ModBlocks;
import com.gimr12.towers_attack.entity.ModEntities;
import com.gimr12.towers_attack.entity.client.GoblinRenderer;
import com.gimr12.towers_attack.item.ModCreativeModeTabs;
import com.gimr12.towers_attack.item.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.gimr12.towers_attack.attachment.ModAttachments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import javax.swing.text.html.parser.Entity;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(towers_attack.MOD_ID)
public class towers_attack {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "gimrstowerattack";
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public towers_attack(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register((modEventBus));
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.register(modEventBus);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        ModAttachments.register(modEventBus);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS){
            event.accept(ModItems.GOBLIN_SPAWN_EGG);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartedEvent event){
        com.gimr12.towers_attack.titles.TitleManager.load(event.getServer());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = towers_attack.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.GOBLIN.get(), GoblinRenderer::new);

        }
    }
}
