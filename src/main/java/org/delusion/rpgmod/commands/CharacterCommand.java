package org.delusion.rpgmod.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.delusion.rpgmod.character.CharacterStatType;
import org.delusion.rpgmod.character.CharacterStatTypes;
import org.delusion.rpgmod.character.CharacterStats;

//import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static net.minecraft.server.command.CommandManager.*;

public class CharacterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("character")
                .then(literal("stats")
                        .then(literal("reset")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                                .then(argument("type", IdentifierArgumentType.identifier())
                                        .executes(context -> {
                                            if (CharacterStatType.exists(context.getArgument("type", Identifier.class))) {
                                                CharacterStats.getStatsFor(context.getSource().getPlayer().getUuid())
                                                        .setUserStatsAndSend(context.getSource().getPlayer().getUuid(),
                                                            CharacterStatType.get(context.getArgument("type", Identifier.class)),
                                                            CharacterStatType.get(context.getArgument("type", Identifier.class)).defaultValue());
                                                return 1;
                                            } else return 0;
                                        }))
                                .executes(context -> {
                                    CharacterStats.resetUserStats(context.getSource().getPlayer().getUuid());
                                    return 1;
                                })
                                .then(argument("target", EntityArgumentType.player())
                                        .then(argument("type", IdentifierArgumentType.identifier())
                                                .executes(context -> {
                                                    if (CharacterStatType.exists(context.getArgument("type", Identifier.class))) {
                                                        CharacterStats.getStatsFor(EntityArgumentType.getPlayer(context, "target").getUuid())
                                                                .setUserStatsAndSend(EntityArgumentType.getPlayer(context, "target").getUuid(),
                                                                        CharacterStatType.get(context.getArgument("type", Identifier.class)),
                                                                        CharacterStatType.get(context.getArgument("type", Identifier.class)).defaultValue());
                                                        return 1;
                                                    } else return 0;
                                                }))
                                        .executes(context -> {
                                            CharacterStats.resetUserStats(EntityArgumentType.getPlayer(context, "target").getUuid());
                                            return 1;
                                        })))
                        .then(literal("get")
                                .then(argument("type", IdentifierArgumentType.identifier())
                                        .executes(context -> {
                                            if (CharacterStatType.exists(context.getArgument("type", Identifier.class))) {
                                                context.getSource().sendFeedback(Text.of("You have a " +
                                                        CharacterStats.getStatsFor(context.getSource().getPlayer().getUuid())
                                                                .getStatistic(CharacterStatType.get(context.getArgument("type", Identifier.class))) +
                                                        " in the " + context.getArgument("type", Identifier.class) + " stat"), false);
                                                return 1;
                                            } else return 0;
                                        })
                                        .then(argument("target", EntityArgumentType.player())
                                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                                                .executes(context -> {
                                                    if (CharacterStatType.exists(context.getArgument("type", Identifier.class))) {
                                                        context.getSource().sendFeedback(Text.of(
                                                                EntityArgumentType.getPlayer(context, "target")
                                                                        .getGameProfile().getName() + " has a " +
                                                                CharacterStats.getStatsFor(EntityArgumentType.getPlayer(context, "target").getUuid())
                                                                        .getStatistic(CharacterStatType.get(context.getArgument("type", Identifier.class))) +
                                                                " in the " + context.getArgument("type", Identifier.class) + " stat"), false);
                                                        return 1;
                                                    } else return 0;
                                                }))))
                        .then(literal("set")
                                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                                .then(argument("type", IdentifierArgumentType.identifier())
                                        .then(argument("value", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                if (CharacterStatType.exists(context.getArgument("type", Identifier.class))) {
                                                    CharacterStats.getStatsFor(context.getSource().getPlayer().getUuid())
                                                            .setUserStatsAndSend(context.getSource().getPlayer().getUuid(),
                                                                    CharacterStatType.get(context.getArgument("type", Identifier.class)),
                                                                    IntegerArgumentType.getInteger(context, "value"));
                                                    return 1;
                                                } else return 0;
                                            }))
                                        .then(argument("target", EntityArgumentType.player())
                                                .then(argument("value", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            if (CharacterStatType.exists(context.getArgument("type", Identifier.class))) {
                                                                CharacterStats.getStatsFor(EntityArgumentType.getPlayer(context, "target").getUuid())
                                                                        .setUserStatsAndSend(EntityArgumentType.getPlayer(context, "target").getUuid(),
                                                                                CharacterStatType.get(context.getArgument("type", Identifier.class)),
                                                                                IntegerArgumentType.getInteger(context, "value"));
                                                                return 1;
                                                            } else return 0;
                                                        })))))));
    }
}
