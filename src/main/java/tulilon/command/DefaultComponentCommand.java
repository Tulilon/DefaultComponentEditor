package tulilon.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Items;
import tulilon.ComponentSavedData;
import tulilon.DataComponentUtils;
import tulilon.Messages;


public class DefaultComponentCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		dispatcher.register(
				Commands.literal("defaultcomponent").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
						.then(Commands.literal("set")
								.then(Commands.argument("input", ItemArgument.item(buildContext))
										.executes(DefaultComponentCommand::executeSet)))
						.then(Commands.literal("get")
								.then(Commands.argument("input", ItemArgument.item(buildContext))
										.executes(DefaultComponentCommand::executeGet)))
						.then(Commands.literal("getall")
								.executes(DefaultComponentCommand::executeGetAll))
						.then(Commands.literal("reset")
								.then(Commands.argument("input", ItemArgument.item(buildContext))
										.executes(DefaultComponentCommand::executeReset)))
						.then(Commands.literal("help")
								.executes(DefaultComponentCommand::executeHelp)));

	}
	private static int executeSet(CommandContext<CommandSourceStack> context) {
		var source = context.getSource();
		ComponentSavedData data = ComponentSavedData.getData(source.getServer());
		var input = ItemArgument.getItem(context,"input");
		var item = input.item();
		DataComponentPatch patch = input.components();

		var prev = data.getComponentsOfItem(item.value());
		if (item.value().equals(Items.AIR)) {
			source.sendFailure(Messages.getInvalidItemMessage(item.value()));
			return 0;
		}
		if (patch.isEmpty()) {
			source.sendFailure(Messages.getNoComponentsMessage(item.value()));
			return 0;
		}

		data.setComponentsOfItem(item.value(), patch);
		if (DataComponentUtils.isNestedItem(item, source.getServer())) {
			source.sendFailure(Messages.getInfiniteLoopMessage());
			data.setComponentsOfItem(item.value(), prev == null ? DataComponentPatch.EMPTY : prev);
			return 0;
		}

		DataComponentUtils.applyComponentChanges(source.getServer());
		source.sendSuccess(() -> Messages.getSetMessage(item.value(),patch,source.registryAccess()), true);

		return 1;
	}
	private static int executeReset(CommandContext<CommandSourceStack> context) {
		var item = ItemArgument.getItem(context,"input").item().value();
		if (item.equals(Items.AIR)) {
			context.getSource().sendFailure(Messages.getInvalidItemMessage(item));
			return 0;
		}
		ComponentSavedData data = ComponentSavedData.getData(context.getSource().getServer());

		data.setComponentsOfItem(item, DataComponentPatch.EMPTY);

		DataComponentUtils.applyComponentChanges(context.getSource().getServer());
		context.getSource().sendSuccess(() -> Messages.getResetMessage(item),true);
		return 1;
	}

	private static int executeGet(CommandContext<CommandSourceStack> context) {

		var source = context.getSource();
		var data = ComponentSavedData.getData(source.getServer());
		var item = ItemArgument.getItem(context,"input").item().value();

		var components = data.getComponentsOfItem(item);

		context.getSource().sendSuccess(() -> Messages.getGetMessage(item,components,source.registryAccess()),false);
		return 1;
	}

	private static int executeGetAll(CommandContext<CommandSourceStack> context) {
		var source = context.getSource();
		source.registryAccess();
		ComponentSavedData data = ComponentSavedData.getData(source.getServer());
		var changes = data.getComponents();
		source.sendSuccess(() -> Messages.getGetAllMessage(changes,source.registryAccess()),false);
		return 1;
	}
	private static int executeHelp(CommandContext<CommandSourceStack> context) {
		context.getSource().sendSuccess(() -> Messages.HELP_MESSAGE,false);
		return 1;
	}
}
