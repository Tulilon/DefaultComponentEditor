package tulilon;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.component.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Map;
import java.util.Optional;
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
		// I think currently only BUNDLE_CONTENTS and USE_REMAINDER do this (if an item's bundle contents contains itself, the game crashes,use remainder doesn't update client side)
		var useRemainder = components.get(DataComponentMap.EMPTY,DataComponents.USE_REMAINDER);
		if (useRemainder != null && useRemainder.convertInto().item().equals(item)) return true;
		return isBundleNested(item,item,server);
	}
	private static boolean isBundleNested(Holder<Item> item,Holder<Item> originalItem, MinecraftServer server) {
		var components = ComponentSavedData.getData(server).getComponentsOfItem(item.value());
		if (components == null) return false;
		var bundleContents = components.get(DataComponentMap.EMPTY,DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null) {
			var bundleItems = bundleContents.items().stream().map(ItemStackTemplate::item).toList();
			return bundleItems.contains(originalItem) || bundleItems.stream().anyMatch(i -> isBundleNested(i, originalItem, server));
		}
		return false;
	}
}
