package tulilon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static net.minecraft.network.chat.Component.literal;

// can't use translations because server side :(
public class Messages {
	public final static Component HELP_MESSAGE = Component.literal("""
			set default components of an item:
			§7/defaultcomponent set <item with components>§r
			reset default components of an item:
			§7/defaultcomponent reset <item>§r
			get default components of an item:
			§7/defaultcomponent get <item>§r
			get all default component changes:
			§7/defaultcomponent getall§r
			examples:
			set dirt max stack size to 16:
			§7/defaultcomponent set dirt[minecraft:max_stack_size=16]§r
			reset default components of dirt:
			§7/defaultcomponent reset dirt§r
			get default components of dirt:
			§7/defaultcomponent get dirt§r""");

	public static Component getSetMessage(Item item, DataComponentPatch patch,RegistryAccess registryAccess) {
		var itemText = literal(getItemID(item)).withStyle(ChatFormatting.AQUA);
		var patchText = patchToString(patch,registryAccess);
		return literal("Set default components of item ").append(itemText).append(" to: ").append(patchText);
	}
	public static Component getNoComponentsMessage(Item item) {
		var itemText = literal(getItemID(item)).withStyle(ChatFormatting.AQUA);
		return literal("No components provided for item ").append(itemText);
	}
	public static Component getInvalidItemMessage(Item item) {
		var itemText = literal(getItemID(item)).withStyle(ChatFormatting.AQUA);
		return literal("Item cannot be ").append(itemText);
	}
	public static Component getInfiniteLoopMessage() {
		return literal("couldn't set default components because the components contain the item itself");
	}
	// great name
	public static Component getGetMessage(Item item, @Nullable DataComponentPatch patch,RegistryAccess registryAccess) {
		var itemText = literal(getItemID(item)).withStyle(ChatFormatting.AQUA);
		if (patch == null || patch.isEmpty()) {
			return literal("Item ").append(itemText).append(" has no default component changes");
		} else {
			var patchText = patchToString(patch,registryAccess);
			return literal("Item ").append(itemText).append(" has the following default component changes: ").append(patchText);
		}
	}
	public static Component getGetAllMessage(Map<Holder<Item>, DataComponentPatch> changes,RegistryAccess registryAccess) {
		if (changes.isEmpty() || changes.values().stream().allMatch(DataComponentPatch::isEmpty)) {
			return literal("There are no default component changes");
		}
		var text = literal("Current default component changes:\n");
		changes.forEach((itemHolder, patch) -> {
			if (patch == null || patch.isEmpty()) return;
			var itemText = literal(getItemID(itemHolder.value())).withStyle(ChatFormatting.AQUA);
			var patchText = patchToString(patch,registryAccess);
			text.append(itemText).append(": ").append(patchText).append("\n");
		});
		return text;
	}

	public static Component getResetMessage(Item item) {
		var itemText = Component.literal(getItemID(item)).withStyle(ChatFormatting.AQUA);
		return literal("Reset default component of item ").append(itemText);
	}

	public static MutableComponent patchToString(DataComponentPatch patch, RegistryAccess access) {
		var result = DataComponentPatch.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,access), patch);
		return NbtUtils.toPrettyComponent(result.getOrThrow()).copy();
	}
	private static String getItemID(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}
}
