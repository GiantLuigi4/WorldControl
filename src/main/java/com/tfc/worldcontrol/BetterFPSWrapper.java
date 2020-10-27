package com.tfc.worldcontrol;

import com.tfc.better_fps_graph.API.Profiler;
import net.minecraftforge.fml.ModList;

public class BetterFPSWrapper {
	public static void addSection(String name) {
		if (ModList.get().isLoaded("better_fps_graph"))
			Profiler.addSection(name);
	}
	
	public static void endSection() {
		if (ModList.get().isLoaded("better_fps_graph"))
			Profiler.endSection();
	}
}
