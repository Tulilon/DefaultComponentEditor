package tulilon;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.component.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static tulilon.DefaultComponentEditor.originalComponents;

public class DataComponentUtils {
	// pretty bad code, but it works
	public static DataComponentPatch mergePatches(DataComponentPatch original, DataComponentPatch override) {
		var builder = DataComponentPatch.builder();
		Consumer<Map.Entry<DataComponentType<?>, Optional<?>>> consumer = entry -> {
			var type = entry.getKey();
			entry.getValue().ifPresentOrElse(v -> builder.set(TypedDataComponent.createUnchecked(type,v)),() -> builder.remove(type));
		};
		original.entrySet().forEach(consumer);
		override.entrySet().forEach(consumer);
		return builder.build();
	}

	@SuppressWarnings("deprecation")
	public static void applyComponentChanges(MinecraftServer server) {
		var data = ComponentSavedData.getData(server);

		data.getComponents().forEach((itemHolder, patch) -> {
			var item = itemHolder.value();
			var prevComponents = item.components();
			if (!originalComponents.containsKey(itemHolder)) originalComponents.put(itemHolder,prevComponents);
			item.builtInRegistryHolder().bindComponents(PatchedDataComponentMap.fromPatch(originalComponents.get(itemHolder),patch));
			if (patch.isEmpty()) data.removeComponentsOfItem(itemHolder);
		});
		PlayerLookup.all(server).forEach(player -> player.inventoryMenu.broadcastFullState());
	}

	public static boolean isNestedItem(Holder<Item> item, MinecraftServer server) {
		var components = ComponentSavedData.getData(server).getComponentsOfItem(item.value());
		if (components == null) return false;
		// I think currently only BUNDLE_CONTENTS, CHARGED_PROJECTILES and USE_REMAINDER do this (if an item's bundle contents contains itself, the game crashes)
		var useRemainder = components.get(DataComponentMap.EMPTY,DataComponents.USE_REMAINDER);
		if (useRemainder != null && useRemainder.convertInto().item().equals(item)) return true;
		return isNested(item, item, server, DataComponentUtils::getBundleItems) ||
				isNested(item, item, server, DataComponentUtils::getProjectiles);
	}

	/**
	 * checks if the children (childItemGetter) of an item contains {@code originalItem} recursively
	 * @param item the item to check the children of
	 * @param originalItem the item to search for
	 * @param childItemGetter the function to get children of an item
	 */
	private static boolean isNested(Holder<Item> item, Holder<Item> originalItem, MinecraftServer server,
									BiFunction<Holder<Item>, MinecraftServer, List<Holder<Item>>> childItemGetter) {
			var items = childItemGetter.apply(item,server);
			return items.contains(originalItem) || items.stream().anyMatch(i -> isNested(i, originalItem, server, childItemGetter));
	}

	private static List<Holder<Item>> getProjectiles(Holder<Item> item, MinecraftServer server) {
		var components = ComponentSavedData.getData(server).getComponentsOfItem(item.value());
		if (components == null) return new ArrayList<>();
		var projectiles = components.get(DataComponentMap.EMPTY, DataComponents.CHARGED_PROJECTILES);
		if (projectiles == null) return new ArrayList<>();
		return projectiles.items().stream().map(ItemStackTemplate::item).toList();
	}
	//basically the same code, but whatever
	private static List<Holder<Item>> getBundleItems(Holder<Item> item, MinecraftServer server) {
		var components = ComponentSavedData.getData(server).getComponentsOfItem(item.value());
		if (components == null) return new ArrayList<>();
		var bundleContents = components.get(DataComponentMap.EMPTY,DataComponents.BUNDLE_CONTENTS);
		if (bundleContents == null) return new ArrayList<>();
		return bundleContents.items().stream().map(ItemStackTemplate::item).toList();
	}
}
