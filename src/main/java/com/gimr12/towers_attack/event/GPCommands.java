package com.gimr12.towers_attack.event;

import com.gimr12.towers_attack.leveling.PlayerLeveling;
import com.gimr12.towers_attack.leveling.PlayerStats;
import com.gimr12.towers_attack.towers_attack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.gimr12.towers_attack.titles.TitleDefinition;
import com.gimr12.towers_attack.titles.TitleManager;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * /gimr leveling level set <lvl> [player]
 * /gimr leveling level add <lvl> [player]
 * /gimr leveling level remove <lvl> [player]
 * /gimr leveling xp add <amount> [player]
 * /gimr leveling xp remove <amount> [player]
 * /gimr leveling reset [player]
 * /gimr leveling resetpoint add <amount> [player]
 *
 * "gimr" credits the mod's creator; "leveling" names the mod's leveling
 * system. Every subcommand defaults to the command's own sender when
 * [player] is omitted. All of these require op permission (level 2) -
 * even the self-targeting form, since they're admin/cheat tools.
 */
@EventBusSubscriber(modid = towers_attack.MOD_ID)
public class GPCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gimr")
                .then(Commands.literal("leveling")
                        .then(levelNode())
                        .then(xpNode())
                        .then(resetNode())
                        .then(resetPointNode())
                )
                .then(titlesNode())
        );
    }

    // ----- xp add / xp remove -----

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> xpNode() {
        return Commands.literal("xp")
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> runOnSelf(ctx, player -> {
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    PlayerLeveling.addGP(player, amount);
                                    return "Added " + amount + " XP to " + player.getGameProfile().getName() + ".";
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runOnTarget(ctx, player -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            PlayerLeveling.addGP(player, amount);
                                            return "Added " + amount + " XP to " + player.getGameProfile().getName() + ".";
                                        })))))
                .then(Commands.literal("remove")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> runOnSelf(ctx, player -> {
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    PlayerLeveling.removeGP(player, amount);
                                    return "Removed " + amount + " XP from " + player.getGameProfile().getName() + ".";
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runOnTarget(ctx, player -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            PlayerLeveling.removeGP(player, amount);
                                            return "Removed " + amount + " XP from " + player.getGameProfile().getName() + ".";
                                        })))));
    }

    // ----- level set / level add / level remove -----

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> levelNode() {
        return Commands.literal("level")
                .then(Commands.literal("set")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("lvl", IntegerArgumentType.integer(1))
                                .executes(ctx -> runOnSelf(ctx, player -> {
                                    int lvl = IntegerArgumentType.getInteger(ctx, "lvl");
                                    PlayerLeveling.setLevel(player, lvl);
                                    return "Set " + player.getGameProfile().getName() + " to level " + lvl + ".";
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runOnTarget(ctx, player -> {
                                            int lvl = IntegerArgumentType.getInteger(ctx, "lvl");
                                            PlayerLeveling.setLevel(player, lvl);
                                            return "Set " + player.getGameProfile().getName() + " to level " + lvl + ".";
                                        })))))
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("lvl", IntegerArgumentType.integer(1))
                                .executes(ctx -> runOnSelf(ctx, player -> {
                                    int lvl = IntegerArgumentType.getInteger(ctx, "lvl");
                                    PlayerLeveling.setLevel(player, PlayerLeveling.getLevel(player) + lvl);
                                    return "Added " + lvl + " level(s) to " + player.getGameProfile().getName()
                                            + " (now level " + PlayerLeveling.getLevel(player) + ").";
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runOnTarget(ctx, player -> {
                                            int lvl = IntegerArgumentType.getInteger(ctx, "lvl");
                                            PlayerLeveling.setLevel(player, PlayerLeveling.getLevel(player) + lvl);
                                            return "Added " + lvl + " level(s) to " + player.getGameProfile().getName()
                                                    + " (now level " + PlayerLeveling.getLevel(player) + ").";
                                        })))))
                .then(Commands.literal("remove")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("lvl", IntegerArgumentType.integer(0))
                                .executes(ctx -> runOnSelf(ctx, player -> {
                                    int lvl = IntegerArgumentType.getInteger(ctx, "lvl");
                                    PlayerLeveling.setLevel(player, PlayerLeveling.getLevel(player) - lvl);
                                    return "Removed " + lvl + " level(s) from " + player.getGameProfile().getName()
                                            + " (now level " + PlayerLeveling.getLevel(player) + ").";
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runOnTarget(ctx, player -> {
                                            int lvl = IntegerArgumentType.getInteger(ctx, "lvl");
                                            PlayerLeveling.setLevel(player, PlayerLeveling.getLevel(player) - lvl);
                                            return "Removed " + lvl + " level(s) from " + player.getGameProfile().getName()
                                                    + " (now level " + PlayerLeveling.getLevel(player) + ").";
                                        })))));
    }

    // ----- reset (admin-forced, bypasses Reset Point cost) -----

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> resetNode() {
        return Commands.literal("reset")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> runOnSelf(ctx, player -> {
                    PlayerStats.forceResetStats(player);
                    return "Reset all stats for " + player.getGameProfile().getName() + ".";
                }))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> runOnTarget(ctx, player -> {
                            PlayerStats.forceResetStats(player);
                            return "Reset all stats for " + player.getGameProfile().getName() + ".";
                        })));
    }

    // ----- resetpoint add (the only way to grant Reset Points) -----

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> resetPointNode() {
        return Commands.literal("resetpoint")
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> runOnSelf(ctx, player -> {
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    PlayerStats.addResetPoints(player, amount);
                                    return "Gave " + amount + " Reset Point(s) to " + player.getGameProfile().getName() + ".";
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runOnTarget(ctx, player -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            PlayerStats.addResetPoints(player, amount);
                                            return "Gave " + amount + " Reset Point(s) to " + player.getGameProfile().getName() + ".";
                                        })))));
    }

    // ----- titles -----
    // ----- titles -----
    // ----- titles -----

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> titlesNode() {
        return Commands.literal("titles")
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            String names = TitleManager.all().stream()
                                    .map(t -> t.id() + " [" + t.rarity().name().toLowerCase() + "]")
                                    .collect(java.util.stream.Collectors.joining(", "));
                            ctx.getSource().sendSuccess(() -> Component.literal(names.isEmpty() ? "No titles loaded." : names), false);
                            return TitleManager.all().size();
                        }))
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            TitleManager.load(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal("Titles reloaded."), true);
                            return 1;
                        }))
                .then(Commands.literal("grant")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("title", StringArgumentType.word())
                                .executes(ctx -> runTitleActionOnSelf(ctx, player -> {
                                    String id = StringArgumentType.getString(ctx, "title");
                                    boolean ok = TitleManager.grant(player, id);
                                    return new ActionResult(ok, ok
                                            ? "Granted title " + id + " to " + player.getGameProfile().getName() + "."
                                            : "Unknown title: " + id);
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runTitleActionOnTarget(ctx, player -> {
                                            String id = StringArgumentType.getString(ctx, "title");
                                            boolean ok = TitleManager.grant(player, id);
                                            return new ActionResult(ok, ok
                                                    ? "Granted title " + id + " to " + player.getGameProfile().getName() + "."
                                                    : "Unknown title: " + id);
                                        })))))
                .then(Commands.literal("remove")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("title", StringArgumentType.word())
                                .executes(ctx -> runTitleActionOnSelf(ctx, player -> {
                                    String id = StringArgumentType.getString(ctx, "title");
                                    boolean ok = TitleManager.remove(player, id);
                                    return new ActionResult(ok, ok
                                            ? "Removed title " + id + " from " + player.getGameProfile().getName() + "."
                                            : "Player does not have that title (or it does not exist): " + id);
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runTitleActionOnTarget(ctx, player -> {
                                            String id = StringArgumentType.getString(ctx, "title");
                                            boolean ok = TitleManager.remove(player, id);
                                            return new ActionResult(ok, ok
                                                    ? "Removed title " + id + " from " + player.getGameProfile().getName() + "."
                                                    : "Player does not have that title (or it does not exist): " + id);
                                        })))))
                .then(Commands.literal("equip")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("title", StringArgumentType.word())
                                .executes(ctx -> runTitleActionOnSelf(ctx, player -> {
                                    String id = StringArgumentType.getString(ctx, "title");
                                    boolean ok = TitleManager.equip(player, id);
                                    return new ActionResult(ok, ok
                                            ? "Equipped " + id + " for " + player.getGameProfile().getName() + "."
                                            : "Player cannot equip that title: " + id);
                                }))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> runTitleActionOnTarget(ctx, player -> {
                                            String id = StringArgumentType.getString(ctx, "title");
                                            boolean ok = TitleManager.equip(player, id);
                                            return new ActionResult(ok, ok
                                                    ? "Equipped " + id + " for " + player.getGameProfile().getName() + "."
                                                    : "Player cannot equip that title: " + id);
                                        })))))
                .then(Commands.literal("unequip")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> runOnSelf(ctx, player -> {
                            TitleManager.unequip(player);
                            return "Unequipped title from " + player.getGameProfile().getName() + ".";
                        }))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> runOnTarget(ctx, player -> {
                                    TitleManager.unequip(player);
                                    return "Unequipped title from " + player.getGameProfile().getName() + ".";
                                }))));
    }

    private record ActionResult(boolean success, String message) {}

    private interface PlayerResultAction {
        ActionResult run(ServerPlayer player);
    }

    private static int runTitleActionOnSelf(CommandContext<CommandSourceStack> ctx, PlayerResultAction action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ActionResult result = action.run(player);
        if (result.success()) {
            ctx.getSource().sendSuccess(() -> Component.literal(result.message()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal(result.message()));
        return 0;
    }

    private static int runTitleActionOnTarget(CommandContext<CommandSourceStack> ctx, PlayerResultAction action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        ActionResult result = action.run(player);
        if (result.success()) {
            ctx.getSource().sendSuccess(() -> Component.literal(result.message()), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal(result.message()));
        return 0;
    }

    // ----- shared plumbing -----

    private interface PlayerAction {
        String run(ServerPlayer player);
    }

    private static int runOnSelf(CommandContext<CommandSourceStack> ctx, PlayerAction action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String message = action.run(player);
        ctx.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int runOnTarget(CommandContext<CommandSourceStack> ctx, PlayerAction action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String message = action.run(player);
        ctx.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }
}