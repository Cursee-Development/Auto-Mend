package com.cursee.auto_mending.core.command;

import com.cursee.auto_mending.Constants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AutoMendingCommands {

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandContext, Commands.CommandSelection commandEnvironment) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(Constants.MOD_ID);

        root.then(damageAllEquipmentSlots());

        commandDispatcher.register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> damageAllEquipmentSlots() {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("damage_all_equipment_slots");
        command.executes((commandContext -> damageAllEquipmentSlots(commandContext.getSource())));
        return command;
    }

    public static int damageAllEquipmentSlots(CommandSourceStack source) {

        if (isPlayerOperator(source) && source.getPlayer() != null) {
            ServerPlayer self = source.getPlayer();

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack stack = self.getItemBySlot(equipmentSlot);
                if (!stack.isDamageableItem() || stack.getMaxDamage() < 2) continue;

                stack.setDamageValue(stack.getMaxDamage() - 1);
                self.setItemSlot(equipmentSlot, stack);
                self.sendSystemMessage(Component.literal("[Auto Mending] Damaged item in " + equipmentSlot.getName() + " slot."));
            }
        }

        return 1;
    }

    public static boolean isPlayerOperator(CommandSourceStack source) {

        boolean isPlayer = source.isPlayer();
        Player player = source.getPlayer();
        MinecraftServer server = source.getServer();

        return isPlayer && player != null && source.hasPermission(server.getOperatorUserPermissionLevel());
    }
}
