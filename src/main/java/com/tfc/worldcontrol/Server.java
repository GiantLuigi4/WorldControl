package com.tfc.worldcontrol;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import static com.tfc.worldcontrol.WorldControl.colorCode;

public class Server {
	public static boolean isSlow = false;
	public static int speed = 10;
	
	public void registerSelf(IEventBus bus) {
		bus.addListener(this::onServerTick);
		bus.addListener(this::chats);
	}
	
	private void onServerTick(TickEvent.WorldTickEvent event) {
		if (isSlow && event.side.isServer()) {
			long start = Util.milliTime();
			while ((Util.milliTime() - start) <= speed) ;
		}
	}
	
	private void chats(ServerChatEvent event) {
		if (event.getMessage().equals("!ToggleSlowMode") && event.getPlayer().hasPermissionLevel(4)) {
			isSlow = !isSlow;
			event.getPlayer().sendStatusMessage(new StringTextComponent(colorCode + "eSlow mode is now " + ("" + isSlow).replace("true", colorCode + "4on").replace("false", colorCode + "2off") + "."), false);
			event.setCanceled(event.isCancelable());
			return;
		} else if (event.getMessage().startsWith("!SetSpeed ") && event.getPlayer().hasPermissionLevel(4)) {
			speed = Integer.parseInt(event.getMessage().replace("!SetSpeed ", ""));
			event.getPlayer().sendStatusMessage(new StringTextComponent(colorCode + "eSet the MSPT to " + colorCode + "b" + ((int) (speed * 6.5f)) + "."), false);
			event.setCanceled(event.isCancelable());
			return;
		} else if (event.getMessage().startsWith("!PlaceStruct ") && event.getPlayer().hasPermissionLevel(4)) {
			String[] strings = event.getMessage().split(" ");
			String structName = strings[1];
			int structX = event.getPlayer().getPosition().getX();
			int structY = event.getPlayer().getPosition().getY();
			int structZ = event.getPlayer().getPosition().getZ();
			StructureThing struct;
			try {
				struct = new StructureThing(structName);
				for (BlockContext context : struct.blocks)
					event.getPlayer().world.setBlockState(context.pos.add(structX, structY, structZ), context.state);
			} catch (Throwable ignored) {
				Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(
						colorCode + "4Failed to place structure."
				), false);
				return;
			}
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(
					colorCode + "2Successfully placed structure."
			), false);
			return;
		} else if (event.getMessage().equals("!WCHelp")) {
			event.setCanceled(event.isCancelable());
			return;
		} else if (event.getMessage().startsWith("!SaveStruct ")) {
			event.setCanceled(event.isCancelable());
			return;
		} else if (event.getMessage().startsWith("!ShowStruct ")) {
			event.setCanceled(event.isCancelable());
			return;
		}
	}
}
