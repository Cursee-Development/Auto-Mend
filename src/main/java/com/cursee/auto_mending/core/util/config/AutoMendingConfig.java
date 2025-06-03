package com.cursee.auto_mending.core.util.config;

import com.cursee.auto_mending.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class AutoMendingConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue REPAIR_AMOUNT = BUILDER
            .comment("The amount of damage an item must have before it's repaired by the enchantment. Minimum: 1, Maximum: 2147483647")
            .comment("Item damage can be confusing; using a shovel with no enchantments once will have 1 damage.")
            .defineInRange("repairAmount", 5, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue EXPERIENCE_COST_IN_POINTS = BUILDER
            .comment("The amount of experience points a player must have for the enchantment to repair items. Minimum: 0, Maximum: 2147483647")
            .defineInRange("experienceCostInPoints", 5, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Actually accessed values
    public static int repairAmount;
    public static int experienceCostInPoints;

    @SubscribeEvent
    private static void onLoad(final ModConfigEvent event) {
        repairAmount = REPAIR_AMOUNT.getAsInt();
        experienceCostInPoints = EXPERIENCE_COST_IN_POINTS.getAsInt();
    }
}
