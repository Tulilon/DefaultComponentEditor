package tulilon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tulilon.ComponentSavedData;
import tulilon.DataComponentUtils;

@Mixin(targets = "net.minecraft.world.item.ItemStack$1")
public abstract class ItemStackStreamCodecMixin {
	@ModifyExpressionValue(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;asPatch()Lnet/minecraft/core/component/DataComponentPatch;"))
	private DataComponentPatch addDefaultsToPacket(DataComponentPatch original, @Local(argsOnly = true) ItemStack itemStack) {
		var context = PacketContext.get();
		if (context == null) return original;
		var server = context.get(PacketContext.SERVER_INSTANCE);
		if (server != null) {
			var components = ComponentSavedData.getData(server).getComponentsOfItem(itemStack.getItem());
			if (components == null || components.isEmpty()) return original;
			return DataComponentUtils.mergePatches(components,original);
		}

		return original;
	}
}
