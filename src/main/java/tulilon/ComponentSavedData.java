package tulilon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ComponentSavedData extends SavedData {
	private final Map<Holder<Item>, DataComponentPatch> components;

	public static final Codec<ComponentSavedData> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(Codec.unboundedMap(
					Item.CODEC,
					DataComponentPatch.CODEC).fieldOf("components").xmap(HashMap::new, map -> map)
					.forGetter(ComponentSavedData::getComponents)
			).apply(instance,ComponentSavedData::new));

	private static final SavedDataType<ComponentSavedData> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(DefaultComponentEditor.MOD_ID,"default_components"),
			ComponentSavedData::new,
			CODEC,
			null
	);
	public ComponentSavedData() {
		components = new HashMap<>();
	}

	public ComponentSavedData(Map<Holder<Item>, DataComponentPatch> components) {
		this.components = components;
	}

	public HashMap<Holder<Item>, DataComponentPatch> getComponents() {
		return new HashMap<>(components);
	}

	public void setComponentsOfItem(Item item, DataComponentPatch patch) {
		components.put(BuiltInRegistries.ITEM.wrapAsHolder(item), patch);
		setDirty();
	}
	public @Nullable DataComponentPatch getComponentsOfItem(Item item) {
		return components.get(BuiltInRegistries.ITEM.wrapAsHolder(item));
	}
	public void removeComponentsOfItem(Holder<Item> itemHolder) {
		components.remove(itemHolder);
		setDirty();
	}

	public static ComponentSavedData getData(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

}
