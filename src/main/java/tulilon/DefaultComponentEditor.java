package tulilon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.*;
import net.minecraft.world.item.Item;
import tulilon.command.DefaultComponentCommand;

import java.util.HashMap;
import java.util.Map;

public class DefaultComponentEditor implements ModInitializer {
	public static final String MOD_ID = "default-component-editor";
	public static Map<Holder<Item>, DataComponentMap> originalComponents = new HashMap<>();
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(DataComponentUtils::applyComponentChanges);
		createCommand();
	}
	public void createCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
				DefaultComponentCommand.register(dispatcher,buildContext));
	}
}
