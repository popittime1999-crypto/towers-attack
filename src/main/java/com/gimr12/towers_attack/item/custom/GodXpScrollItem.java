package com.gimr12.towers_attack.item.custom;

import com.gimr12.towers_attack.leveling.PlayerLeveling;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GodXpScrollItem extends Item {
    private final int gpAmount;

    public GodXpScrollItem(Properties properties, int gpAmount) {
        super(properties);
        this.gpAmount = gpAmount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerLeveling.addGP(player, this.gpAmount);

            player.displayClientMessage(Component.literal("Gained " + this.gpAmount + " XP Points!"), true);

            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
