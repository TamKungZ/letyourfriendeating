package th.tamkungz.letyourfriendeating.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import th.tamkungz.letyourfriendeating.LetYourFriendEatingMainFunction;

public class FeedFriendCommand {

    // Note: Return a LiteralArgumentBuilder instead of a built LiteralCommandNode.
    public static LiteralArgumentBuilder<CommandSource> register() {
        return LiteralArgumentBuilder.<CommandSource>literal("feedfriend")
            // Command to toggle the feed-friend ability.
            .then(Commands.literal("toggle")
                .executes(context -> {
                    LetYourFriendEatingMainFunction.feedFriendEnabled = !LetYourFriendEatingMainFunction.feedFriendEnabled;
                    StringTextComponent message = new StringTextComponent("Feed Friend toggled " +
                            (LetYourFriendEatingMainFunction.feedFriendEnabled ? "ON" : "OFF"));
                    context.getSource().sendSuccess(message, true);
                    return 1;
                }))
            // Command to display feed statistics.
            .then(Commands.literal("stats")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrException();
                    int count = LetYourFriendEatingMainFunction.getFeedCount(player.getUUID());
                    context.getSource().sendSuccess(new StringTextComponent("You have been fed " + count + " time(s)."), false);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgument.getPlayer(context, "player");
                        int count = LetYourFriendEatingMainFunction.getFeedCount(target.getUUID());
                        context.getSource().sendSuccess(new StringTextComponent(target.getName().getString() +
                                " has been fed " + count + " time(s)."), false);
                        return 1;
                    })));
    }
}
