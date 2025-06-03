package com.cursee.auto_mending;

import com.cursee.auto_mending.core.command.AutoMendingCommands;
import com.cursee.auto_mending.core.registry.ModEnchantments;
import com.cursee.auto_mending.core.util.config.AutoMendingConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
public class AutoMendingNeoForge {

    public AutoMendingNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ModEnchantments.init();

        NeoForge.EVENT_BUS.addListener((Consumer<RegisterCommandsEvent>) event -> {
            AutoMendingCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        modContainer.registerConfig(ModConfig.Type.COMMON, AutoMendingConfig.SPEC);
    }
}
