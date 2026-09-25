package com.gimr12.towers_attack.attachment;

import com.gimr12.towers_attack.towers_attack;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge 1.21/1.21.1 attachments do NOT support automatic client sync
 * (that was added in a later NeoForge version) - these are persisted only.
 * Syncing to the client is done manually via SyncPlayerDataPacket,
 * see network/PlayerDataSync.java.
 *
 * copyOnDeath() is set on all of these so a player doesn't lose all their
 * levels/stats on death - without it, entity attachments reset to their
 * default value on respawn.
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, towers_attack.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> GRAND_POINTS =
            ATTACHMENT_TYPES.register("grand_points", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> STRENGTH =
            ATTACHMENT_TYPES.register("strength", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> HP =
            ATTACHMENT_TYPES.register("hp", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> STAMINA =
            ATTACHMENT_TYPES.register("stamina", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> DEF =
            ATTACHMENT_TYPES.register("def", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> LUCK =
            ATTACHMENT_TYPES.register("luck", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> RESET_POINTS =
            ATTACHMENT_TYPES.register("reset_points", () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> KILLED_WITHER =
            ATTACHMENT_TYPES.register("killed_wither", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> KILLED_ENDER_DRAGON =
            ATTACHMENT_TYPES.register("killed_ender_dragon", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath()
                    .build());


    public static final DeferredHolder<AttachmentType<?>, AttachmentType<java.util.Map<String, Integer>>> TITLE_KILL_COUNTS =
            ATTACHMENT_TYPES.register("title_kill_counts", () ->
                    AttachmentType.<java.util.Map<String, Integer>>builder(() -> new java.util.HashMap<>())
                            .serialize(com.mojang.serialization.Codec.unboundedMap(
                                    com.mojang.serialization.Codec.STRING,
                                    com.mojang.serialization.Codec.INT
                            ))
                            .copyOnDeath()
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<java.util.List<String>>> UNLOCKED_TITLES =
            ATTACHMENT_TYPES.register("unlocked_titles", () -> AttachmentType.<java.util.List<String>>builder(() -> new java.util.ArrayList<>())
                    .serialize(com.mojang.serialization.Codec.STRING.listOf())
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> EQUIPPED_TITLE =
            ATTACHMENT_TYPES.register("equipped_title", () -> AttachmentType.builder(() -> "")
                    .serialize(com.mojang.serialization.Codec.STRING)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
