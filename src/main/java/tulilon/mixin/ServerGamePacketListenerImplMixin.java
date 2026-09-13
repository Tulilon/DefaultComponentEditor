package tulilon.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tulilon.ComponentSavedData;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Shadow
	public ServerPlayer player;

	public ServerGamePacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
		super(minecraftServer, connection, commonListenerCookie);
	}

	@Inject(method = "handleSetCreativeModeSlot",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;broadcastChanges()V"))
	private void updateComponentsOnClient(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
		var menu = player.inventoryMenu;
		var components = ComponentSavedData.getData(server).getComponentsOfItem(packet.itemStack().getItem());
		if (components != null && !components.isEmpty()) {
			send(new ClientboundContainerSetSlotPacket(menu.containerId,menu.incrementStateId(),packet.slotNum(),packet.itemStack()));
		}
	}
}
