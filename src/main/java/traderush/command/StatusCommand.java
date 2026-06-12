package traderush.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import traderush.TradeRush;

public class StatusCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(TradeRush.COMMAND_ROOT)
				.then(Commands.literal("status")
						.executes(context -> {
							context.getSource().sendSuccess(() -> Component.literal("Hello World!"), false);
							return 1;
						})));
	}
}
