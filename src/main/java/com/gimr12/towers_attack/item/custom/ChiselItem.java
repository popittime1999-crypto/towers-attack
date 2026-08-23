package com.gimr12.towers_attack.item.custom;

import com.gimr12.towers_attack.block.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

public class ChiselItem extends Item  {
    private static final Map<Block, Block> CHISEL_MAP =
            Map.of(
                    Blocks.STONE, Blocks.STONE_BRICKS,
                    Blocks.ANDESITE, Blocks.POLISHED_ANDESITE,
                    Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE,
                    Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE,
                    Blocks.DIORITE, Blocks.POLISHED_DIORITE,
                    Blocks.GRANITE, Blocks.POLISHED_GRANITE,
                    ModBlocks.BADCRYSTAL_BLOCK.get(), ModBlocks.BADCRYSTAL_BAR_BLOCK.get(),
                    ModBlocks.POORCRYSTAL_BLOCK.get(), ModBlocks.POORCRYSTAL_BAR_BLOCK.get(),
                    ModBlocks.FINECRYSTAL_BLOCK.get(), ModBlocks.FINECRYSTAL_BAR_BLOCK.get()

            );

    public ChiselItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Block clickedBlock = level.getBlockState(context.getClickedPos()).getBlock();

        if(CHISEL_MAP.containsKey(clickedBlock)){
            if(!level.isClientSide()){
                level.setBlockAndUpdate(context.getClickedPos(), CHISEL_MAP.get(clickedBlock).defaultBlockState());

                context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), context.getPlayer(),
                        item -> context.getPlayer().onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

                level.playSound(null, context.getClickedPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
