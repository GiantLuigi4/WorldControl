package com.tfc.worldcontrol;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;

public class FluidRendererHelperBlockReader implements IBlockDisplayReader {
	BlockState state;
	BlockPos pos1;
	
	public void setState(BlockState state) {
		this.state = state;
	}
	
	public void setPos1(BlockPos pos1) {
		this.pos1 = pos1;
	}
	
	@Override
	public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_) {
		return Minecraft.getInstance().world.func_230487_a_(p_230487_1_, p_230487_2_);
	}
	
	@Override
	public WorldLightManager getLightManager() {
		return Minecraft.getInstance().world.getLightManager();
	}
	
	@Override
	public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
		return Minecraft.getInstance().world.getBlockColor(blockPosIn, colorResolverIn);
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return Minecraft.getInstance().player.getEntityWorld().getTileEntity(pos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (pos.getY() <= pos1.getY()) {
			return state;
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		if (pos.getY() <= pos1.getY()) {
			return state.getFluidState();
		} else {
			return Blocks.AIR.getDefaultState().getFluidState();
		}
	}
}