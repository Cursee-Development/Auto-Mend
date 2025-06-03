package com.cursee.auto_mending.mixin;

import com.cursee.auto_mending.core.registry.ModEnchantments;
import com.cursee.auto_mending.core.util.ExperienceHelper;
import com.cursee.auto_mending.core.util.config.AutoMendingConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void auto_mending$tick(CallbackInfo ci) {

        ServerPlayer player = (ServerPlayer) (Object) this;
        ServerLevel level = player.serverLevel();

        // every 5 ticks or 1/4th of a second, so we don't fry CPUs
        if (level == null || level.getGameTime() % 5 != 0) return;

        // player can't pay the experience cost. begone from me
        int cost = AutoMendingConfig.experienceCostInPoints;
        if (cost > 0 && !ExperienceHelper.hasEnoughExperiencePoints(player, cost)) return;

        // early return if the player is empty-handed and maidenless
        if (!player.getArmorSlots().iterator().hasNext()
                && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().isEmpty()) return;

        int repairAmount = AutoMendingConfig.repairAmount;

        for (EquipmentSlot slot : EquipmentSlot.values()) {

            ItemStack stack = player.getItemBySlot(slot);

            // skip items being used (like shields)
            if (player.isUsingItem()) {
                if (slot == EquipmentSlot.MAINHAND && player.getUsedItemHand() == InteractionHand.MAIN_HAND) continue;
                if (slot == EquipmentSlot.OFFHAND && player.getUsedItemHand() == InteractionHand.OFF_HAND) continue;
            }

            // if it ain't broke, don't fix it (literally)
            if (!stack.isDamaged()) continue;

            // make sure we don't over-repair and rip a hole in space-time
            if (stack.getDamageValue() < repairAmount) repairAmount = stack.getDamageValue();

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack
                    .getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .entrySet()) {

                // we only care about ourselves
                if (!entry.getKey().is(ModEnchantments.AUTO_MENDING)) return;

                Enchantment enchant = entry.getKey().value();

                // make sure the enchantment actually repairs things and matches the equipment slot type
                if (!enchant.effects().has(EnchantmentEffectComponents.REPAIR_WITH_XP) || !enchant.matchingSlot(slot)) continue;

                // if we can multiply the repair without going full negative durability
                if ((stack.getDamageValue() - (repairAmount * entry.getIntValue())) >= 0) {
                    repairAmount *= entry.getIntValue(); // channeling that multiplier energy
                }

                // magical duct tape application here:
                stack.setDamageValue(stack.getDamageValue() - repairAmount);
                // player.setItemSlot(slot, stack); // we touch the stack directly

                // scale our cost like a good subscription plan; more bang for your buck
                if (cost > 0) player.giveExperiencePoints(-(int)((float) cost / (float) entry.getIntValue()));
                break; // it's fixed, no need to keep looking
            }
        }
    }
}
