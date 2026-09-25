package com.gimr12.towers_attack.titles;

import com.gimr12.towers_attack.attachment.ModAttachments;
import com.gimr12.towers_attack.leveling.PlayerLeveling;
import com.gimr12.towers_attack.network.PlayerDataSync;
import com.gimr12.towers_attack.network.SyncTitleDataPacket;
import com.gimr12.towers_attack.towers_attack;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Server-side title registry, config loader, progress tracker and stat applier. */
public final class TitleManager {
    private static final Map<String, TitleDefinition> TITLES = new ConcurrentHashMap<>();
    private static final GsonBuilder GSON = new GsonBuilder().setPrettyPrinting();

    private TitleManager() {}

    public static void load(MinecraftServer server) {
        Path baseDir = server.getServerDirectory().resolve("config").resolve(towers_attack.MOD_ID).resolve("titles");
        Path vanillaDir = baseDir.resolve("vanilla");
        Path customDir = baseDir.resolve("custom");
        try {
            Files.createDirectories(vanillaDir);
            Files.createDirectories(customDir);
            createExampleConfigs(vanillaDir);

            Map<String, TitleDefinition> loaded = new HashMap<>();
            for (Path dir : List.of(vanillaDir, customDir)) {
                try (var stream = Files.list(dir)) {
                    stream.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json"))
                            .sorted()
                            .forEach(path -> {
                                try {
                                    String fallback = path.getFileName().toString().replaceFirst("\\.json$", "");
                                    TitleDefinition title = TitleDefinition.fromJson(Files.readString(path, StandardCharsets.UTF_8), fallback);
                                    if (!title.id().isBlank()) loaded.put(title.id(), title);
                                } catch (Exception ex) {
                                    towers_attack.LOGGER.error("Could not load title config {}", path, ex);
                                }
                            });
                }
            }
            TITLES.clear();
            TITLES.putAll(loaded);
            towers_attack.LOGGER.info("Loaded {} title(s) from {} (vanilla + custom)", TITLES.size(), baseDir);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                sync(player);
                applyTitleAttributes(player);
            }
        } catch (IOException ex) {
            towers_attack.LOGGER.error("Could not create/load title config directories", ex);
        }
    }

    private static void createExampleConfigs(Path dir) throws IOException {
        // 75 default titles: 20 common, 20 rare, 15 epic, 10 mythic, 10 legendary.
        // Mixed requirement types (kills, mining, crafting, vanilla stats, pure level)
        // so it's not all combat, each with its own display color. createIfMissing()
        // never overwrites an existing file.
        createIfMissing(dir, "ancient_prospector.json", """
                {
                  "id": "ancient_prospector",
                  "name": "Ancient Prospector",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "mine_block": {
                      "minecraft:ancient_debris": 60
                    }
                  },
                  "stats": {
                    "strength": 4,
                    "stamina": 3
                  },
                  "description": "Dug through the deep dark's oldest secrets.",
                  "color": "#AA00AA"
                }
                """);
        createIfMissing(dir, "angler.json", """
                {
                  "id": "angler",
                  "name": "Angler",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:fish_caught": 15
                    }
                  },
                  "stats": {
                    "luck": 1
                  },
                  "description": "Patience by the water, finally rewarded.",
                  "color": "#55FFFF"
                }
                """);
        createIfMissing(dir, "archer_supreme.json", """
                {
                  "id": "archer_supreme",
                  "name": "Archer Supreme",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "craft_item": {
                      "minecraft:bow": 10
                    }
                  },
                  "stats": {
                    "stamina": 2,
                    "strength": 1
                  },
                  "description": "Strings snap before their aim does.",
                  "color": "#00AAAA"
                }
                """);
        createIfMissing(dir, "archmage.json", """
                {
                  "id": "archmage",
                  "name": "Archmage",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 22,
                  "level_scaling": 0.02,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:enchant_item": 500
                    }
                  },
                  "stats": {
                    "luck": 16,
                    "stamina": 6
                  },
                  "description": "Enchanted gear has become second nature.",
                  "color": "#FFAA00"
                }
                """);
        createIfMissing(dir, "armorer.json", """
                {
                  "id": "armorer",
                  "name": "Armorer",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "craft_item": {
                      "minecraft:shield": 15
                    }
                  },
                  "stats": {
                    "def": 3
                  },
                  "description": "Nobody gets through this guard easily.",
                  "color": "#AA00AA"
                }
                """);
        createIfMissing(dir, "baker.json", """
                {
                  "id": "baker",
                  "name": "Baker",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "craft_item": {
                      "minecraft:bread": 20
                    }
                  },
                  "stats": {
                    "hp": 1
                  },
                  "description": "Kept the pantry stocked, loaf by loaf.",
                  "color": "#FF55FF"
                }
                """);
        createIfMissing(dir, "beacon_keeper.json", """
                {
                  "id": "beacon_keeper",
                  "name": "Beacon Keeper",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "craft_item": {
                      "minecraft:beacon": 5
                    }
                  },
                  "stats": {
                    "def": 6,
                    "hp": 3
                  },
                  "description": "Lit a light that the whole base can see.",
                  "color": "#AAAA00"
                }
                """);
        createIfMissing(dir, "bedrock_breaker.json", """
                {
                  "id": "bedrock_breaker",
                  "name": "Bedrock Breaker",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 22,
                  "level_scaling": 0.02,
                  "requirements": {
                    "mine_block": {
                      "minecraft:ancient_debris": 250
                    }
                  },
                  "stats": {
                    "strength": 16,
                    "def": 8
                  },
                  "description": "Went deeper and mined more than most ever attempt.",
                  "color": "#00FFFF"
                }
                """);
        createIfMissing(dir, "bedrock_lord.json", """
                {
                  "id": "bedrock_lord",
                  "name": "Bedrock Lord",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 30,
                  "level_scaling": 0.025,
                  "requirements": {
                    "mine_block": {
                      "minecraft:ancient_debris": 600
                    }
                  },
                  "stats": {
                    "strength": 30,
                    "def": 16
                  },
                  "description": "Has mined more of the deep dark than seems physically reasonable.",
                  "color": "#FFD700"
                }
                """);
        createIfMissing(dir, "blaze_extinguisher.json", """
                {
                  "id": "blaze_extinguisher",
                  "name": "Blaze Extinguisher",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:blaze": 50
                    }
                  },
                  "stats": {
                    "def": 2,
                    "hp": 1
                  },
                  "description": "Put out more fires than a bucket of water.",
                  "color": "#00AA00"
                }
                """);
        createIfMissing(dir, "bone_breaker.json", """
                {
                  "id": "bone_breaker",
                  "name": "Bone Breaker",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:skeleton": 20
                    }
                  },
                  "stats": {
                    "hp": 2
                  },
                  "description": "Turned a rattling menace back into a pile of bones.",
                  "color": "#55FF55"
                }
                """);
        createIfMissing(dir, "bookbinder.json", """
                {
                  "id": "bookbinder",
                  "name": "Bookbinder",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "craft_item": {
                      "minecraft:book": 15
                    }
                  },
                  "stats": {
                    "luck": 1
                  },
                  "description": "Turned paper and leather into knowledge.",
                  "color": "#FFAA00"
                }
                """);
        createIfMissing(dir, "boom_dodger.json", """
                {
                  "id": "boom_dodger",
                  "name": "Boom Dodger",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:creeper": 15
                    }
                  },
                  "stats": {
                    "hp": 2
                  },
                  "description": "Survived enough green surprises to stop flinching.",
                  "color": "#AAAAFF"
                }
                """);
        createIfMissing(dir, "bringer_of_ruin.json", """
                {
                  "id": "bringer_of_ruin",
                  "name": "Bringer of Ruin",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 25,
                  "level_scaling": 0.02,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:wither": 3
                    }
                  },
                  "stats": {
                    "strength": 14,
                    "hp": 12,
                    "def": 8
                  },
                  "description": "The wither storms remember this name.",
                  "color": "#AA00FF"
                }
                """);
        createIfMissing(dir, "cattle_wrangler.json", """
                {
                  "id": "cattle_wrangler",
                  "name": "Cattle Wrangler",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:cow": 25
                    }
                  },
                  "stats": {
                    "hp": 1
                  },
                  "description": "Turned the pasture into a quieter place.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "chicken_chaser.json", """
                {
                  "id": "chicken_chaser",
                  "name": "Chicken Chaser",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:chicken": 30
                    }
                  },
                  "stats": {
                    "stamina": 1
                  },
                  "description": "An easy start, but a start nonetheless.",
                  "color": "#AAFF55"
                }
                """);
        createIfMissing(dir, "coal_baron.json", """
                {
                  "id": "coal_baron",
                  "name": "Coal Baron",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "mine_block": {
                      "minecraft:coal_ore": 200
                    }
                  },
                  "stats": {
                    "stamina": 2
                  },
                  "description": "Never runs low on fuel, not anymore.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "dedicated_survivor.json", """
                {
                  "id": "dedicated_survivor",
                  "name": "Dedicated Survivor",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:play_time": 144000
                    }
                  },
                  "stats": {
                    "hp": 2,
                    "def": 1
                  },
                  "description": "Two full hours in, and just getting started.",
                  "color": "#00AA00"
                }
                """);
        createIfMissing(dir, "deep_diver_stat.json", """
                {
                  "id": "deep_diver_stat",
                  "name": "Deep Diver",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:swim_one_cm": 50000
                    }
                  },
                  "stats": {
                    "stamina": 2,
                    "hp": 1
                  },
                  "description": "The ocean floor is basically a second home now.",
                  "color": "#00AAAA"
                }
                """);
        createIfMissing(dir, "deep_miner.json", """
                {
                  "id": "deep_miner",
                  "name": "Deep Miner",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "mine_block": {
                      "minecraft:deepslate": 300
                    }
                  },
                  "stats": {
                    "strength": 2,
                    "def": 1
                  },
                  "description": "Went past regular stone and kept going.",
                  "color": "#5555FF"
                }
                """);
        createIfMissing(dir, "depth_dweller.json", """
                {
                  "id": "depth_dweller",
                  "name": "Depth Dweller",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 25,
                  "level_scaling": 0.02,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:warden": 8
                    }
                  },
                  "stats": {
                    "stamina": 14,
                    "def": 12,
                    "strength": 6
                  },
                  "description": "Walked the deep dark and made it flinch.",
                  "color": "#00FFAA"
                }
                """);
        createIfMissing(dir, "depth_walker.json", """
                {
                  "id": "depth_walker",
                  "name": "Depth Walker",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 15,
                  "level_scaling": 0.015,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:warden": 3
                    }
                  },
                  "stats": {
                    "stamina": 5,
                    "def": 4,
                    "luck": 2
                  },
                  "description": "Made a sound the darkness couldn't ignore - and survived.",
                  "color": "#55FFFF"
                }
                """);
        createIfMissing(dir, "diamond_baron.json", """
                {
                  "id": "diamond_baron",
                  "name": "Diamond Baron",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "mine_block": {
                      "minecraft:diamond_ore": 120
                    }
                  },
                  "stats": {
                    "luck": 5,
                    "strength": 2
                  },
                  "description": "Found more diamonds than most see in a lifetime.",
                  "color": "#AAAA00"
                }
                """);
        createIfMissing(dir, "dragon_slayer.json", """
                {
                  "id": "dragon_slayer",
                  "name": "Dragon Slayer",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 30,
                  "level_scaling": 0.025,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:ender_dragon": 1
                    }
                  },
                  "stats": {
                    "strength": 20,
                    "hp": 20,
                    "def": 14,
                    "stamina": 10,
                    "luck": 6
                  },
                  "description": "Ended the ancient guardian of the End.",
                  "color": "#FF4500"
                }
                """);
        createIfMissing(dir, "enchanter.json", """
                {
                  "id": "enchanter",
                  "name": "Enchanter",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 5,
                  "level_scaling": 0.01,
                  "requirements": {
                    "craft_item": {
                      "minecraft:enchanting_table": 5
                    }
                  },
                  "stats": {
                    "luck": 3
                  },
                  "description": "Built the altar; the real magic came after.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "end_conqueror.json", """
                {
                  "id": "end_conqueror",
                  "name": "End Conqueror",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 35,
                  "level_scaling": 0.025,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:ender_dragon": 3
                    }
                  },
                  "stats": {
                    "strength": 28,
                    "hp": 26,
                    "def": 18,
                    "stamina": 12,
                    "luck": 8
                  },
                  "description": "The End itself remembers this name.",
                  "color": "#FF0000"
                }
                """);
        createIfMissing(dir, "enderman_exile.json", """
                {
                  "id": "enderman_exile",
                  "name": "Enderman Exile",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:enderman": 60
                    }
                  },
                  "stats": {
                    "strength": 2,
                    "luck": 1
                  },
                  "description": "Stared into the void and it blinked first.",
                  "color": "#00AAAA"
                }
                """);
        createIfMissing(dir, "eternal_veteran.json", """
                {
                  "id": "eternal_veteran",
                  "name": "Eternal Veteran",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 40,
                  "level_scaling": 0.025,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:play_time": 7200000
                    }
                  },
                  "stats": {
                    "strength": 15,
                    "hp": 15,
                    "def": 15,
                    "stamina": 10,
                    "luck": 10
                  },
                  "description": "A hundred hours in this world - there's nothing left to prove.",
                  "color": "#AA0000"
                }
                """);
        createIfMissing(dir, "firework_master.json", """
                {
                  "id": "firework_master",
                  "name": "Firework Master",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "craft_item": {
                      "minecraft:firework_rocket": 300
                    }
                  },
                  "stats": {
                    "luck": 4,
                    "stamina": 2
                  },
                  "description": "The sky answers to this crafter's whims.",
                  "color": "#AA5500"
                }
                """);
        createIfMissing(dir, "first_steps.json", """
                {
                  "id": "first_steps",
                  "name": "First Steps",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 5,
                  "level_scaling": 0.005,
                  "requirements": {},
                  "stats": {
                    "hp": 1
                  },
                  "description": "Reached level 5 - the adventure is just starting.",
                  "color": "#FF55FF"
                }
                """);
        createIfMissing(dir, "fletcher.json", """
                {
                  "id": "fletcher",
                  "name": "Fletcher",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "craft_item": {
                      "minecraft:arrow": 50
                    }
                  },
                  "stats": {
                    "stamina": 1
                  },
                  "description": "A quiver never stays empty for long.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "gale_breaker.json", """
                {
                  "id": "gale_breaker",
                  "name": "Gale Breaker",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:breeze": 40
                    }
                  },
                  "stats": {
                    "stamina": 4,
                    "strength": 3
                  },
                  "description": "Outlasted a whirlwind of wind charges.",
                  "color": "#FF55FF"
                }
                """);
        createIfMissing(dir, "glassblower.json", """
                {
                  "id": "glassblower",
                  "name": "Glassblower",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "craft_item": {
                      "minecraft:glass": 30
                    }
                  },
                  "stats": {
                    "def": 1
                  },
                  "description": "Sand into windows, one furnace run at a time.",
                  "color": "#AAAAAA"
                }
                """);
        createIfMissing(dir, "gold_rusher.json", """
                {
                  "id": "gold_rusher",
                  "name": "Gold Rusher",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "mine_block": {
                      "minecraft:gold_ore": 100
                    }
                  },
                  "stats": {
                    "luck": 2,
                    "strength": 1
                  },
                  "description": "Struck gold often enough to make it a habit.",
                  "color": "#00AA00"
                }
                """);
        createIfMissing(dir, "grand_enchanter.json", """
                {
                  "id": "grand_enchanter",
                  "name": "Grand Enchanter",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 28,
                  "level_scaling": 0.025,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:enchant_item": 1500
                    }
                  },
                  "stats": {
                    "luck": 25,
                    "stamina": 10
                  },
                  "description": "Enchantment tables bow to this level of mastery.",
                  "color": "#FF00AA"
                }
                """);
        createIfMissing(dir, "guardian_breaker.json", """
                {
                  "id": "guardian_breaker",
                  "name": "Guardian Breaker",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:guardian": 50
                    }
                  },
                  "stats": {
                    "strength": 2,
                    "stamina": 1
                  },
                  "description": "Made the monument a little less hostile.",
                  "color": "#5555FF"
                }
                """);
        createIfMissing(dir, "guardian_lord.json", """
                {
                  "id": "guardian_lord",
                  "name": "Guardian Lord",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:elder_guardian": 8
                    }
                  },
                  "stats": {
                    "strength": 4,
                    "def": 3,
                    "hp": 3
                  },
                  "description": "Broke a monument's oldest curse.",
                  "color": "#AA5500"
                }
                """);
        createIfMissing(dir, "harbinger_of_undeath.json", """
                {
                  "id": "harbinger_of_undeath",
                  "name": "Harbinger of Undeath",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 22,
                  "level_scaling": 0.02,
                  "requirements": {
                    "kill_tag": {
                      "minecraft:undead": 1200
                    }
                  },
                  "stats": {
                    "hp": 18,
                    "strength": 10,
                    "def": 4
                  },
                  "description": "An army of the dead couldn't stop this one.",
                  "color": "#AA00FF"
                }
                """);
        createIfMissing(dir, "high_jumper.json", """
                {
                  "id": "high_jumper",
                  "name": "High Jumper",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:jump": 500
                    }
                  },
                  "stats": {
                    "stamina": 1
                  },
                  "description": "Left the ground more times than most bother to count.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "hoglin_wrangler.json", """
                {
                  "id": "hoglin_wrangler",
                  "name": "Hoglin Wrangler",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:hoglin": 40
                    }
                  },
                  "stats": {
                    "strength": 3
                  },
                  "description": "Stood their ground against a charging tusk.",
                  "color": "#AA5500"
                }
                """);
        createIfMissing(dir, "horde_breaker.json", """
                {
                  "id": "horde_breaker",
                  "name": "Horde Breaker",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:zombie": 400
                    }
                  },
                  "stats": {
                    "hp": 6,
                    "def": 2
                  },
                  "description": "An entire horde, and not one left standing.",
                  "color": "#AA00AA"
                }
                """);
        createIfMissing(dir, "iron_prospector.json", """
                {
                  "id": "iron_prospector",
                  "name": "Iron Prospector",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "mine_block": {
                      "minecraft:iron_ore": 60
                    }
                  },
                  "stats": {
                    "strength": 1
                  },
                  "description": "Found the first vein and didn't stop digging.",
                  "color": "#55FFFF"
                }
                """);
        createIfMissing(dir, "lumberjack.json", """
                {
                  "id": "lumberjack",
                  "name": "Lumberjack",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "mine_block": {
                      "minecraft:oak_log": 100
                    }
                  },
                  "stats": {
                    "stamina": 1
                  },
                  "description": "A tree fell, then another, then a whole forest.",
                  "color": "#55FF55"
                }
                """);
        createIfMissing(dir, "marathoner.json", """
                {
                  "id": "marathoner",
                  "name": "Marathoner",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:sprint_one_cm": 200000
                    }
                  },
                  "stats": {
                    "stamina": 3
                  },
                  "description": "Covered serious ground at a dead sprint.",
                  "color": "#5555FF"
                }
                """);
        createIfMissing(dir, "master_enchanter.json", """
                {
                  "id": "master_enchanter",
                  "name": "Master Enchanter",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:enchant_item": 100
                    }
                  },
                  "stats": {
                    "luck": 8
                  },
                  "description": "Every item that passes through their hands leaves changed.",
                  "color": "#AA5500"
                }
                """);
        createIfMissing(dir, "net_hero_smith.json", """
                {
                  "id": "net_hero_smith",
                  "name": "Netherite Smith",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 20,
                  "level_scaling": 0.02,
                  "requirements": {
                    "craft_item": {
                      "minecraft:lodestone": 60
                    }
                  },
                  "stats": {
                    "strength": 12,
                    "luck": 6
                  },
                  "description": "Marked every corner of the map as their own.",
                  "color": "#AA00FF"
                }
                """);
        createIfMissing(dir, "night_owl.json", """
                {
                  "id": "night_owl",
                  "name": "Night Owl",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:play_time": 36000
                    }
                  },
                  "stats": {
                    "hp": 1
                  },
                  "description": "Spent real hours in this world already.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "night_shift.json", """
                {
                  "id": "night_shift",
                  "name": "Night Shift",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:play_time": 216000
                    }
                  },
                  "stats": {
                    "stamina": 2
                  },
                  "description": "Three hours logged and still going strong.",
                  "color": "#AA5500"
                }
                """);
        createIfMissing(dir, "obsidian_breaker.json", """
                {
                  "id": "obsidian_breaker",
                  "name": "Obsidian Breaker",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "mine_block": {
                      "minecraft:obsidian": 150
                    }
                  },
                  "stats": {
                    "strength": 5,
                    "def": 2
                  },
                  "description": "Cracked through the hardest stone there is, again and again.",
                  "color": "#AA00AA"
                }
                """);
        createIfMissing(dir, "piglin_bane.json", """
                {
                  "id": "piglin_bane",
                  "name": "Piglin Bane",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:piglin": 80
                    }
                  },
                  "stats": {
                    "strength": 2,
                    "def": 1
                  },
                  "description": "Gold wasn't worth their lives.",
                  "color": "#00AA00"
                }
                """);
        createIfMissing(dir, "raid_captain_bane.json", """
                {
                  "id": "raid_captain_bane",
                  "name": "Raid Captain's Bane",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "kill_tag": {
                      "minecraft:illager": 150
                    }
                  },
                  "stats": {
                    "strength": 4,
                    "def": 3,
                    "stamina": 2
                  },
                  "description": "Broke a raid before it broke a village.",
                  "color": "#5555FF"
                }
                """);
        createIfMissing(dir, "raid_ender.json", """
                {
                  "id": "raid_ender",
                  "name": "Raid Ender",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:raid_win": 10
                    }
                  },
                  "stats": {
                    "strength": 5,
                    "def": 4
                  },
                  "description": "Sent ten full raids back where they came from.",
                  "color": "#00AAAA"
                }
                """);
        createIfMissing(dir, "raid_legend.json", """
                {
                  "id": "raid_legend",
                  "name": "Raid Legend",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 30,
                  "level_scaling": 0.025,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:raid_win": 100
                    }
                  },
                  "stats": {
                    "strength": 28,
                    "def": 20
                  },
                  "description": "A hundred raids ended before they began.",
                  "color": "#FFA500"
                }
                """);
        createIfMissing(dir, "redstone_tinkerer.json", """
                {
                  "id": "redstone_tinkerer",
                  "name": "Redstone Tinkerer",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "mine_block": {
                      "minecraft:redstone_ore": 120
                    }
                  },
                  "stats": {
                    "luck": 2
                  },
                  "description": "Started wiring contraptions nobody asked for.",
                  "color": "#FFAA00"
                }
                """);
        createIfMissing(dir, "sandcastle_builder.json", """
                {
                  "id": "sandcastle_builder",
                  "name": "Sandcastle Builder",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "mine_block": {
                      "minecraft:sand": 150
                    }
                  },
                  "stats": {
                    "def": 1
                  },
                  "description": "Turned a beach into building material.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "settling_in.json", """
                {
                  "id": "settling_in",
                  "name": "Settling In",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.005,
                  "requirements": {},
                  "stats": {
                    "strength": 1,
                    "def": 1
                  },
                  "description": "Reached level 10 and things are starting to click.",
                  "color": "#AAFF55"
                }
                """);
        createIfMissing(dir, "shieldsmith.json", """
                {
                  "id": "shieldsmith",
                  "name": "Shieldsmith",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 4,
                  "level_scaling": 0.01,
                  "requirements": {
                    "craft_item": {
                      "minecraft:anvil": 8
                    }
                  },
                  "stats": {
                    "strength": 2,
                    "def": 1
                  },
                  "description": "Every anvil built is another forge online.",
                  "color": "#55AAFF"
                }
                """);
        createIfMissing(dir, "stone_breaker.json", """
                {
                  "id": "stone_breaker",
                  "name": "Stone Breaker",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "mine_block": {
                      "minecraft:stone": 200
                    }
                  },
                  "stats": {
                    "strength": 1
                  },
                  "description": "Cleared their first real tunnel through the rock.",
                  "color": "#55FFFF"
                }
                """);
        createIfMissing(dir, "time_served.json", """
                {
                  "id": "time_served",
                  "name": "Time Served",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 10,
                  "level_scaling": 0.015,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:play_time": 720000
                    }
                  },
                  "stats": {
                    "hp": 5,
                    "def": 3
                  },
                  "description": "Ten solid hours in this world - a real commitment.",
                  "color": "#5555FF"
                }
                """);
        createIfMissing(dir, "timeless_wanderer.json", """
                {
                  "id": "timeless_wanderer",
                  "name": "Timeless Wanderer",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 20,
                  "level_scaling": 0.02,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:play_time": 2160000
                    }
                  },
                  "stats": {
                    "hp": 14,
                    "strength": 6,
                    "def": 6
                  },
                  "description": "Thirty hours deep into this world and still going.",
                  "color": "#FFAA00"
                }
                """);
        createIfMissing(dir, "undead_slayer.json", """
                {
                  "id": "undead_slayer",
                  "name": "Undead Slayer",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "kill_tag": {
                      "minecraft:undead": 150
                    }
                  },
                  "stats": {
                    "hp": 3
                  },
                  "description": "The dead have learned to fear this name.",
                  "color": "#AA00AA"
                }
                """);
        createIfMissing(dir, "undying_reaper.json", """
                {
                  "id": "undying_reaper",
                  "name": "Undying Reaper",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "kill_tag": {
                      "minecraft:undead": 500
                    }
                  },
                  "stats": {
                    "hp": 7,
                    "strength": 3
                  },
                  "description": "Word of this hunter has reached the graveyards themselves.",
                  "color": "#55FFFF"
                }
                """);
        createIfMissing(dir, "undying_sovereign.json", """
                {
                  "id": "undying_sovereign",
                  "name": "Undying Sovereign",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 30,
                  "level_scaling": 0.025,
                  "requirements": {
                    "kill_tag": {
                      "minecraft:undead": 2500
                    }
                  },
                  "stats": {
                    "hp": 30,
                    "strength": 14,
                    "def": 8
                  },
                  "description": "Command of the dead itself bends to this will.",
                  "color": "#DC143C"
                }
                """);
        createIfMissing(dir, "veteran_ten.json", """
                {
                  "id": "veteran_ten",
                  "name": "Seasoned Adventurer",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 15,
                  "level_scaling": 0.01,
                  "requirements": {},
                  "stats": {
                    "strength": 1,
                    "hp": 1,
                    "def": 1
                  },
                  "description": "Reached level 15 through steady, honest effort.",
                  "color": "#AA5500"
                }
                """);
        createIfMissing(dir, "village_champion.json", """
                {
                  "id": "village_champion",
                  "name": "Village Champion",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 22,
                  "level_scaling": 0.02,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:raid_win": 40
                    }
                  },
                  "stats": {
                    "strength": 16,
                    "def": 12
                  },
                  "description": "Villages sleep easier knowing this name.",
                  "color": "#FF5555"
                }
                """);
        createIfMissing(dir, "villager_friend.json", """
                {
                  "id": "villager_friend",
                  "name": "Villager's Friend",
                  "rarity": "rare",
                  "command_only": false,
                  "minimum_player_level": 3,
                  "level_scaling": 0.01,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:traded_with_villager": 50
                    }
                  },
                  "stats": {
                    "luck": 3
                  },
                  "description": "The village trusts this face at the trading post.",
                  "color": "#00AA00"
                }
                """);
        createIfMissing(dir, "wanderer.json", """
                {
                  "id": "wanderer",
                  "name": "Wanderer",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:walk_one_cm": 50000
                    }
                  },
                  "stats": {
                    "stamina": 1
                  },
                  "description": "Put a lot of ground behind them, on foot.",
                  "color": "#55FF55"
                }
                """);
        createIfMissing(dir, "warden_bane.json", """
                {
                  "id": "warden_bane",
                  "name": "Warden's Bane",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 35,
                  "level_scaling": 0.025,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:warden": 20
                    }
                  },
                  "stats": {
                    "stamina": 24,
                    "def": 20,
                    "strength": 10
                  },
                  "description": "The deep dark falls silent when they walk.",
                  "color": "#AA0000"
                }
                """);
        createIfMissing(dir, "web_walker.json", """
                {
                  "id": "web_walker",
                  "name": "Web Walker",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:spider": 20
                    }
                  },
                  "stats": {
                    "stamina": 1
                  },
                  "description": "No longer flinches at the sight of eight legs.",
                  "color": "#AAAAFF"
                }
                """);
        createIfMissing(dir, "wither_bane.json", """
                {
                  "id": "wither_bane",
                  "name": "Wither's Bane",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 20,
                  "level_scaling": 0.02,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:wither": 1
                    }
                  },
                  "stats": {
                    "strength": 10,
                    "hp": 10,
                    "def": 6
                  },
                  "description": "Struck down one of the Overworld's own bosses.",
                  "color": "#AA00FF"
                }
                """);
        createIfMissing(dir, "wither_champion.json", """
                {
                  "id": "wither_champion",
                  "name": "Wither Champion",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 32,
                  "level_scaling": 0.025,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:wither": 8
                    }
                  },
                  "stats": {
                    "strength": 26,
                    "hp": 22,
                    "def": 16
                  },
                  "description": "Made a habit of what most fear to attempt once.",
                  "color": "#DC143C"
                }
                """);
        createIfMissing(dir, "world_ender.json", """
                {
                  "id": "world_ender",
                  "name": "World's Edge Walker",
                  "rarity": "legendary",
                  "command_only": false,
                  "minimum_player_level": 30,
                  "level_scaling": 0.025,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:walk_one_cm": 5000000
                    }
                  },
                  "stats": {
                    "stamina": 30,
                    "hp": 10
                  },
                  "description": "Has walked farther than most maps can even render.",
                  "color": "#FF4500"
                }
                """);
        createIfMissing(dir, "world_traveler.json", """
                {
                  "id": "world_traveler",
                  "name": "World Traveler",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 12,
                  "level_scaling": 0.015,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:walk_one_cm": 500000
                    }
                  },
                  "stats": {
                    "stamina": 7
                  },
                  "description": "Has walked farther than most players ever will.",
                  "color": "#AA00AA"
                }
                """);
        createIfMissing(dir, "world_walker.json", """
                {
                  "id": "world_walker",
                  "name": "World Walker",
                  "rarity": "mythic",
                  "command_only": false,
                  "minimum_player_level": 20,
                  "level_scaling": 0.02,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:walk_one_cm": 2000000
                    }
                  },
                  "stats": {
                    "stamina": 18,
                    "hp": 6
                  },
                  "description": "Has walked an almost unreasonable distance.",
                  "color": "#FF00FF"
                }
                """);
        createIfMissing(dir, "zombie_basher.json", """
                {
                  "id": "zombie_basher",
                  "name": "Zombie Basher",
                  "rarity": "common",
                  "command_only": false,
                  "minimum_player_level": 1,
                  "level_scaling": 0.005,
                  "requirements": {
                    "kill_entity": {
                      "minecraft:zombie": 20
                    }
                  },
                  "stats": {
                    "strength": 1
                  },
                  "description": "Cracked its first skull against the risen dead.",
                  "color": "#55FF55"
                }
                """);
        createIfMissing(dir, "unbreakable_will.json", """
                {
                  "id": "unbreakable_will",
                  "name": "Unbreakable Will",
                  "rarity": "epic",
                  "command_only": false,
                  "minimum_player_level": 15,
                  "level_scaling": 0.015,
                  "requirements": {
                    "custom_stat": {
                      "minecraft:damage_taken": 3000
                    }
                  },
                  "stats": {
                    "hp": 10,
                    "def": 8
                  },
                  "description": "Concept by Big Bear. Survive enough punishment that giving up simply stopped being an option.",
                  "color": "#AA55FF"
                }
                """);
    }

    private static void createIfMissing(Path path, String name, String contents) throws IOException {
        Path file = path.resolve(name);
        if (!Files.exists(file)) Files.writeString(file, contents, StandardCharsets.UTF_8);
    }

    public static List<TitleDefinition> all() {
        return TITLES.values().stream()
                .sorted(Comparator.comparing((TitleDefinition t) -> t.rarity().ordinal()).thenComparing(TitleDefinition::name))
                .toList();
    }

    public static TitleDefinition get(String id) {
        return TITLES.get(id);
    }

    public static String getEquipped(Player player) {
        return player.getData(ModAttachments.EQUIPPED_TITLE.get());
    }

    public static boolean isUnlocked(Player player, String id) {
        TitleDefinition title = get(id);
        if (title == null) return false;
        if (getUnlocked(player).contains(id)) return true;
        return !title.commandOnly() && requirementsMet(player, title);
    }

    public static Set<String> getUnlocked(Player player) {
        return new HashSet<>(player.getData(ModAttachments.UNLOCKED_TITLES.get()));
    }

    public static boolean requirementsMet(Player player, TitleDefinition title) {
        if (title.commandOnly()) return false;
        int level = PlayerLeveling.getLevel(player);
        if (level < title.minimumPlayerLevel()) return false;
        for (Map.Entry<String, Integer> e : title.killEntities().entrySet()) {
            if (getKillCount(player, e.getKey()) < title.requiredKills(e.getValue(), level)) return false;
        }
        for (Map.Entry<String, Integer> e : title.killTags().entrySet()) {
            if (getKillCount(player, "#" + e.getKey()) < title.requiredKills(e.getValue(), level)) return false;
        }
        for (Map.Entry<String, Integer> e : title.mineBlocks().entrySet()) {
            if (getVanillaStatValue(player, "mine_block", e.getKey()) < title.requiredKills(e.getValue(), level)) return false;
        }
        for (Map.Entry<String, Integer> e : title.craftItems().entrySet()) {
            if (getVanillaStatValue(player, "craft_item", e.getKey()) < title.requiredKills(e.getValue(), level)) return false;
        }
        for (Map.Entry<String, Integer> e : title.customStats().entrySet()) {
            if (getVanillaStatValue(player, "custom_stat", e.getKey()) < title.requiredKills(e.getValue(), level)) return false;
        }
        return true;
    }

    /**
     * Reads a value straight from vanilla's own per-player statistics (blocks mined,
     * items crafted, playtime, distance walked, etc.) - no custom tracking needed,
     * Minecraft already counts all of this natively per player.
     */
    public static int getVanillaStatValue(Player player, String category, String idStr) {
        if (!(player instanceof ServerPlayer serverPlayer)) return 0;
        try {
            ResourceLocation id = ResourceLocation.parse(idStr);
            Stat<?> stat = switch (category) {
                case "mine_block" -> {
                    Block block = BuiltInRegistries.BLOCK.get(id);
                    yield block != null ? Stats.BLOCK_MINED.get(block) : null;
                }
                case "craft_item" -> {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    yield item != null ? Stats.ITEM_CRAFTED.get(item) : null;
                }
                case "custom_stat" -> Stats.CUSTOM.get(id);
                default -> null;
            };
            return stat != null ? serverPlayer.getStats().getValue(stat) : 0;
        } catch (Exception e) {
            // Same defensive guard as the client-side copy in TitleScreen - a bad/
            // unregistered stat id must not be able to break requirement checking.
            return 0;
        }
    }

    public static void recordKill(Player player, LivingEntity entity) {
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        Map<String, Integer> counts = new HashMap<>(player.getData(ModAttachments.TITLE_KILL_COUNTS.get()));
        counts.merge(entityId, 1, Integer::sum);

        entity.getType().builtInRegistryHolder().tags().forEach(tag -> {
            counts.merge("#" + tag.location(), 1, Integer::sum);
        });
        player.setData(ModAttachments.TITLE_KILL_COUNTS.get(), Map.copyOf(counts));

        checkUnlocks(player);
        if (player instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }


    public static void checkUnlocks(Player player) {
        Set<String> unlocked = getUnlocked(player);
        boolean changed = false;
        for (TitleDefinition title : all()) {
            if (!title.commandOnly() && requirementsMet(player, title) && unlocked.add(title.id())) {
                changed = true;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Title unlocked: " + title.name()), true);
                announceIfPrestigious(player, title);
            }
        }
        if (changed) {
            player.setData(ModAttachments.UNLOCKED_TITLES.get(), List.copyOf(unlocked));
            // recordKill() already syncs unconditionally right after calling this, but
            // checkUnlocks() is also called from PlayerLeveling (leveling up, admin level
            // commands) which does NOT sync title data on its own - sync here too so a
            // level-gated title unlocking still reaches the client.
            if (player instanceof ServerPlayer serverPlayer) {
                sync(serverPlayer);
            }
        }
    }

    /** Mythic and Legendary titles are server-wide prestige - announce them to everyone. */
    private static void announceIfPrestigious(Player player, TitleDefinition title) {
        if (title.rarity() != TitleRarity.MYTHIC && title.rarity() != TitleRarity.LEGENDARY) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        net.minecraft.network.chat.Component announcement = net.minecraft.network.chat.Component.literal(
                player.getGameProfile().getName() + " has unlocked the "
                        + title.rarity().name().toLowerCase() + " title \"" + title.name() + "\"!"
        ).withStyle(style -> style.withColor(title.rarity().color()));

        server.getPlayerList().broadcastSystemMessage(announcement, false);
    }

    public static int getKillCount(Player player, String key) {
        return player.getData(ModAttachments.TITLE_KILL_COUNTS.get()).getOrDefault(key, 0);
    }

    public static boolean equip(ServerPlayer player, String id) {
        TitleDefinition title = get(id);
        if (title == null || !isUnlocked(player, id)) return false;
        player.setData(ModAttachments.EQUIPPED_TITLE.get(), id);
        Set<String> unlocked = getUnlocked(player);
        if (unlocked.add(id)) player.setData(ModAttachments.UNLOCKED_TITLES.get(), List.copyOf(unlocked));
        applyTitleAttributes(player);
        updateTeam(player);
        sync(player);
        return true;
    }

    public static boolean unequip(ServerPlayer player) {
        player.setData(ModAttachments.EQUIPPED_TITLE.get(), "");
        applyTitleAttributes(player);
        updateTeam(player);
        sync(player);
        return true;
    }

    private static final String TEAM_PREFIX = "gt_";

    /**
     * Puts (or removes) the player from a scoreboard team matching their equipped
     * title. This is how one change gives colored tab-list names, a colored prefix
     * above the player's head, AND colored join/leave messages all at once - vanilla
     * itself builds all three from the player's team prefix/name, so there's no need
     * for separate hooks into each of those systems individually.
     */
    public static void updateTeam(ServerPlayer player) {
        net.minecraft.world.scores.Scoreboard scoreboard = player.getServer().getScoreboard();
        String username = player.getGameProfile().getName();

        net.minecraft.world.scores.PlayerTeam current = scoreboard.getPlayersTeam(username);
        if (current != null && current.getName().startsWith(TEAM_PREFIX)) {
            scoreboard.removePlayerFromTeam(username, current);
        }

        String equippedId = getEquipped(player);
        if (equippedId == null || equippedId.isEmpty()) return;

        TitleDefinition title = get(equippedId);
        if (title == null) return;

        String teamName = TEAM_PREFIX + Integer.toHexString(title.id().hashCode());
        net.minecraft.world.scores.PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setAllowFriendlyFire(true);
            team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.ALWAYS);
            team.setPlayerPrefix(net.minecraft.network.chat.Component.literal(title.name() + " ")
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(title.rarity().color()))));
        } else {
            // Keep the prefix in sync in case the title's name/rarity changed since the team was created.
            team.setPlayerPrefix(net.minecraft.network.chat.Component.literal(title.name() + " ")
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(title.rarity().color()))));
        }
        scoreboard.addPlayerToTeam(username, team);
    }

    public static boolean grant(ServerPlayer player, String id) {
        TitleDefinition title = get(id);
        if (title == null) return false;
        Set<String> unlocked = getUnlocked(player);
        unlocked.add(id);
        player.setData(ModAttachments.UNLOCKED_TITLES.get(), List.copyOf(unlocked));
        sync(player);
        return true;
    }

    public static boolean remove(ServerPlayer player, String id) {
        Set<String> unlocked = getUnlocked(player);
        boolean changed = unlocked.remove(id);
        player.setData(ModAttachments.UNLOCKED_TITLES.get(), List.copyOf(unlocked));
        if (id.equals(getEquipped(player))) unequip(player);
        else sync(player);
        return changed;
    }

    /** Applies only the equipped title's bonuses, using separate modifier IDs. */
    public static void applyTitleAttributes(Player player) {
        TitleAttributes.apply(player, get(getEquipped(player)));
    }

    public static void sync(ServerPlayer player) {
        List<String> definitions = all().stream().map(TitleDefinition::toJson).toList();
        Map<String, Integer> counts = new HashMap<>(player.getData(ModAttachments.TITLE_KILL_COUNTS.get()));

        // Also push live values for every mine_block/craft_item/custom_stat requirement
        // across all loaded titles, through this same already-proven sync pipeline -
        // this is what was actually missing before. The client was never being told
        // these values at all (always defaulting to 0), regardless of whether vanilla's
        // own stat-sync packet reaches it promptly on its own.
        for (TitleDefinition title : all()) {
            for (String key : title.mineBlocks().keySet()) {
                counts.put("stat:mine_block:" + key, getVanillaStatValue(player, "mine_block", key));
            }
            for (String key : title.craftItems().keySet()) {
                counts.put("stat:craft_item:" + key, getVanillaStatValue(player, "craft_item", key));
            }
            for (String key : title.customStats().keySet()) {
                counts.put("stat:custom_stat:" + key, getVanillaStatValue(player, "custom_stat", key));
            }
        }

        PacketDistributorHelper.send(player, new SyncTitleDataPacket(
                definitions,
                counts,
                new ArrayList<>(getUnlocked(player)),
                getEquipped(player)
        ));
    }

    public static String formatId(String id) {
        return id.replace(':', '_').replace(' ', '_').toLowerCase();
    }

    private static final class PacketDistributorHelper {
        static void send(ServerPlayer player, SyncTitleDataPacket packet) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
        }
    }
}
