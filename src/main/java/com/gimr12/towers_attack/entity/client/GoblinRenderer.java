package com.gimr12.towers_attack.entity.client;

import com.gimr12.towers_attack.entity.GoblinVariant;
import com.gimr12.towers_attack.entity.custom.GoblinEntity;
import com.gimr12.towers_attack.towers_attack;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class GoblinRenderer extends MobRenderer<GoblinEntity,GoblinModel<GoblinEntity>> {
    private static final Map<GoblinVariant, ResourceLocation> LOCATION_BY_VARIANT =
            Util.make(Maps.newEnumMap(GoblinVariant.class), map -> {
                map.put(GoblinVariant.MALE,
                        ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "textures/entity/goblin/goblin_male.png"));
                map.put(GoblinVariant.FEMALE,
                        ResourceLocation.fromNamespaceAndPath(towers_attack.MOD_ID, "textures/entity/goblin/goblin_female.png"));
            });


    public GoblinRenderer(EntityRendererProvider.Context context) {
        super(context, new GoblinModel<>(context.bakeLayer(GoblinModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(GoblinEntity entity) {
        return LOCATION_BY_VARIANT.get(entity.getVariant());
    }

    @Override
    public void render(GoblinEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if(entity.isBaby()){
            poseStack.scale(0.45f,0.45f,0.45f);
        } else {
            poseStack.scale(1f,1f,1f);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
