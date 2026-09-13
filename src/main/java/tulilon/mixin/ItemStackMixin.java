package tulilon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Mutable
	@Shadow
	@Final
	private PatchedDataComponentMap components;

	@Shadow
	public abstract Item getItem();

	@ModifyExpressionValue(method = "getComponents", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack;components:Lnet/minecraft/core/component/PatchedDataComponentMap;", opcode = Opcodes.GETFIELD))
	private PatchedDataComponentMap updateComponents(PatchedDataComponentMap original) {
		var withoutPatch = original.copy();
		withoutPatch.clearPatch();
		var defaultComponents = getItem().components();
		if (!withoutPatch.toImmutableMap().equals(defaultComponents)) {
			components = PatchedDataComponentMap.fromPatch(defaultComponents,components.asPatch());
			return components;
		}
		return original;
	}
}
