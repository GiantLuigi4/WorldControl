package com.tfc.worldcontrol;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class StructureThing {
	public final BlockContext[] blocks;
	
	public StructureThing(BlockContext[] blocks) {
		this.blocks = blocks;
	}
	
	public StructureThing(String file) throws IOException {
		Scanner sc = new Scanner(new File("world_control\\structures\\" + file + ".txt" ));
		ArrayList<BlockContext> blockContexts = new ArrayList<>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine().replace("|{", "|Block{" );
			String[] parts = line.split("\\|" );
			BlockPos pos = (BlockPos.fromLong(Long.parseLong(parts[0])));
			String block = parts[1].replace("Block{", "" );
			if (block.contains("[" )) block = block.substring(0, block.indexOf("[" ));
			block = block.replace("}", "" );
			Block b = (ForgeRegistries.BLOCKS.getValue(new ResourceLocation(block)));
			BlockState state = b.getDefaultState();
			if (parts[1].contains("[" )) {
				String states = parts[1].replace("Block{" + block + "}[", "" ).replace("]", "" );
				for (String s : states.split("," )) {
					String name = s.split("=" )[0];
					for (Property<?> p : state.getValues().keySet()) {
						if (p.getName().equals(name)) {
//							System.out.println(name);
//							System.out.println(p.getName());
							String val = s.substring((name + "=" ).length());
//							System.out.println(val);
							try {
								state = with(state, p, val);
							} catch (Throwable err) {
								err.printStackTrace();
							}
						}
					}
				}
			}
			blockContexts.add(new BlockContext(pos, state));
		}
		sc.close();
		this.blocks = blockContexts.toArray(new BlockContext[0]);
	}
	
	private static <A extends Comparable<A>> BlockState with(BlockState state, Property<A> prop, String val) {
		return state.with(prop, (A) prop.parseValue(val).get());
	}
}
