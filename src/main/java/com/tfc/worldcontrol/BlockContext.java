package com.tfc.worldcontrol;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockContext {
	public final BlockPos pos;
	public final BlockState state;
	
	public BlockContext(BlockPos pos, BlockState state) {
		this.pos = pos;
		this.state = state;
	}
}
