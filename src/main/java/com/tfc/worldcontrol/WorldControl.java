package com.tfc.worldcontrol;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("world_control" )
public class WorldControl {
	
	public static final char colorCode;
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	static {
		try {
			InputStream stream = WorldControl.class.getClassLoader().getResourceAsStream("colorCodeChar.txt");
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			stream.close();
			String text = new String(bytes);
			colorCode = text.charAt(text.length() - 1);
		} catch (Throwable ignored) {
			throw new RuntimeException("Could not find the color code character");
		}
	}
	
	public WorldControl() {
		MinecraftForge.EVENT_BUS.register(this);
		new Server().registerSelf(MinecraftForge.EVENT_BUS);
		
		if (FMLEnvironment.dist.isClient()) new Client().registerSelf(MinecraftForge.EVENT_BUS);
	}
}