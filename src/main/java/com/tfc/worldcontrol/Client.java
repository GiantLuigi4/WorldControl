package com.tfc.worldcontrol;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import static com.tfc.worldcontrol.WorldControl.colorCode;

public class Client {
	private static final FluidRendererHelperBlockReader reader = new FluidRendererHelperBlockReader();
	private int structX, structY, structZ;
	private StructureThing struct = new StructureThing(new BlockContext[0]);
	private String structName = "";
	
	//MCJTY:https://github.com/McJty/YouTubeModding14/blob/master/src/main/java/com/mcjty/mytutorial/blocks/FancyBakedModel.java
	private static void putVertex(BakedQuadBuilder builder, Vector3d normal,
								  double x, double y, double z, double u, double v, TextureAtlasSprite sprite, double r, double g, double b, float a) {
		
		ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements().asList();
		for (int j = 0; j < elements.size(); j++) {
			VertexFormatElement e = elements.get(j);
			switch (e.getUsage()) {
				case POSITION:
					builder.put(j, (float) x, (float) y, (float) z, 1.0f);
					break;
				case COLOR:
					builder.put(j, (float) r, (float) g, (float) b, a);
					break;
				case UV:
					switch (e.getIndex()) {
						case 0:
							float iu = sprite.getInterpolatedU(u);
							float iv = sprite.getInterpolatedV(v);
							builder.put(j, iu, iv);
							break;
						case 2:
							builder.put(j, 0f, 1f);
							break;
						default:
							builder.put(j);
							break;
					}
					break;
				case NORMAL:
					builder.put(j, (float) normal.x, (float) normal.y, (float) normal.z);
					break;
				default:
					builder.put(j);
					break;
			}
		}
	}
	
	public void registerSelf(IEventBus bus) {
		bus.addListener(this::chatc);
		bus.addListener(this::renderUI);
		bus.addListener(this::renderStruct);
	}
	
	private void chatc(ClientChatEvent event) {
		BetterFPSWrapper.addSection("world_control:Process Chat");
		if (event.getMessage().equals("!WCHelp")) {
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "6Slow Mode Help"), false);
			
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "2!WCHelp"), false);
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "3Shows this, the slow mode help."), false);
			
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "2!SetSpeed [mspt/6.5]"), false);
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "3Slows down the server, or speeds it up."), false);
			
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "2!ToggleSlowMode"), false);
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "3Toggles SlowMode."), false);
			
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "2!SaveStruct [name] [x1] [y1] [z1] [x2] [y2] [z2]"), false);
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "3Saves a structure for future use, either through a preview that you can use to build, or as a structure that you can paste in."), false);
			
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "2!ShowStruct [name]"), false);
			Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "3Shows a preview of a structure."), false);
		} else if (event.getMessage().startsWith("!SaveStruct ")) {
			//!SaveStruct test -236 67 196 -240 77 192
			String[] strings = event.getMessage().split(" ");
			String name = strings[1];
			try {
				int x1 = Integer.parseInt(strings[2]);
				int y1 = Integer.parseInt(strings[3]);
				int z1 = Integer.parseInt(strings[4]);
				int x2 = Integer.parseInt(strings[5]);
				int y2 = Integer.parseInt(strings[6]);
				int z2 = Integer.parseInt(strings[7]);
				File f = new File("world_control\\structures\\" + name + ".txt");
				if (!f.exists()) {
					f.getParentFile().mkdirs();
					f.createNewFile();
				}
				World world = Minecraft.getInstance().player.getEntityWorld();
				StringBuilder struct = new StringBuilder();
				for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
					for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
						for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
							BlockPos pos = new BlockPos(x, y, z);
							BlockState state = world.getBlockState(pos);
							if (!state.toString().equals("Block{minecraft:air}")) {
								struct.append(pos.subtract(new Vector3i(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2))).toLong())
										.append("|")
										.append(state.toString())
										.append("\n");
							}
						}
					}
				}
				FileOutputStream stream = new FileOutputStream(f);
				stream.write(struct.toString().replace("|Block", "|").getBytes());
				stream.close();
				
				Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "2Successfully saved " + colorCode + "a" + name + "!"), false);
			} catch (Throwable err) {
				err.printStackTrace();
				Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(colorCode + "4Failed to save " + colorCode + "c" + name + "."), false);
			}
		} else if (event.getMessage().startsWith("!ShowStruct ")) {
			String[] strings = event.getMessage().split(" ");
			structName = strings[1];
			structX = Minecraft.getInstance().player.getPosition().getX();
			structY = Minecraft.getInstance().player.getPosition().getY();
			structZ = Minecraft.getInstance().player.getPosition().getZ();
			try {
				struct = new StructureThing(structName);
				Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(
						colorCode + "2Now showing structure " + colorCode + "a" + structName +
								colorCode + "2 at " +
								colorCode + "a" + structX + " " + structY + " " + structZ
				), false);
			} catch (Throwable ignored) {
				struct = new StructureThing(new BlockContext[0]);
				Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(
						colorCode + "2Now hiding structure."
				), false);
			}
		}
		BetterFPSWrapper.endSection();
	}
	
	//MCJTY:https://github.com/McJty/YouTubeModding14/blob/master/src/main/java/com/mcjty/mytutorial/blocks/FancyBakedModel.java
	private BakedQuad createQuad(
			Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4,
			TextureAtlasSprite sprite, Direction dir, int tint, float a
	) {
		Vector3d normal = v3.subtract(v2).crossProduct(v1.subtract(v2)).normalize();
		
		BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
		builder.setQuadOrientation(dir);
		putVertex(builder, normal, v1.x, v1.y, v1.z, 0, 0, sprite, 1.0f, 1.0f, 1.0f, a);
		putVertex(builder, normal, v2.x, v2.y, v2.z, 0, 16, sprite, 1.0f, 1.0f, 1.0f, a);
		putVertex(builder, normal, v3.x, v3.y, v3.z, 16, 16, sprite, 1.0f, 1.0f, 1.0f, a);
		putVertex(builder, normal, v4.x, v4.y, v4.z, 16, 0, sprite, 1.0f, 1.0f, 1.0f, a);
		builder.setQuadTint(tint);
		return builder.build();
	}
	
	private void renderUI(RenderGameOverlayEvent event) {
	}
	
	private void renderStruct(RenderWorldLastEvent event) {
		BetterFPSWrapper.addSection("world_control:Render Struct");
		if (!Minecraft.getInstance().player.isSpectator() && !Minecraft.getInstance().gameSettings.hideGUI) {
			Direction[] dirs = new Direction[]{
					Direction.UP,
					Direction.DOWN,
					Direction.NORTH,
					Direction.EAST,
					Direction.SOUTH,
					Direction.WEST,
					null
			};
			event.getMatrixStack().push();
			event.getMatrixStack().translate(
					-Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
					-Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
					-Minecraft.getInstance().getRenderManager().info.getProjectedView().z
			);
			event.getMatrixStack().translate(structX, structY, structZ);
			for (BlockContext context : struct.blocks) {
				event.getMatrixStack().push();
				BlockPos pos = context.pos;
				event.getMatrixStack().translate(pos.getX(), pos.getY(), pos.getZ());
				BlockPos pos1 = pos.add(structX, structY, structZ);
				BlockState state = context.state;
				if (struct.blocks.length <= 20000 || pos1.distanceSq(Minecraft.getInstance().player.getPosition()) <= 200) {
					if (!Minecraft.getInstance().world.getBlockState(pos1).equals(state)) {
//						int col = Minecraft.getInstance().getBlockColors().getColor(state, Minecraft.getInstance().world, pos1, 0);
						int light = LightTexture.packLight(Minecraft.getInstance().world.getLightFor(LightType.BLOCK, pos1), Minecraft.getInstance().world.getLightFor(LightType.SKY, pos1));
						event.getMatrixStack().push();
						event.getMatrixStack().scale(0.5f, 0.5f, 0.5f);
						event.getMatrixStack().translate(0.5f, 0.5f, 0.5f);
						reader.setState(state);
						reader.setPos1(pos1);
//						Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
//								event.getMatrixStack().getLast(),
//								Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getCutout()),
//								null,
//								Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state),
//								((col >> 16) & 0xFF) / 255f,
//								((col >> 8) & 0xFF) / 255f,
//								((col) & 0xFF) / 255f,
//								light, OverlayTexture.NO_OVERLAY,
//								ModelDataManager.getModelData(Minecraft.getInstance().world, pos)
//						);
//						Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, event.getMatrixStack(), Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(),light, OverlayTexture.NO_OVERLAY);
						IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
						for (Direction dir : dirs) {
							for (BakedQuad qd : model.getQuads(state, dir, new Random(pos1.toLong()))) {
								int col = Minecraft.getInstance().getBlockColors().getColor(state, Minecraft.getInstance().world, pos1, qd.getTintIndex());
								Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getCutout()).addQuad(
										event.getMatrixStack().getLast(), qd,
										((col >> 16) & 0xFF) / 255f,
										((col >> 8) & 0xFF) / 255f,
										((col) & 0xFF) / 255f,
										light, OverlayTexture.NO_OVERLAY
								);
							}
						}
						if (!state.getFluidState().isEmpty()) {
							TextureAtlasSprite[] array = ((ForgeHooksClient.getFluidSprites(reader, pos1, state.getFluidState())));
							IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getSolid());
							float scl = 8f;
							int col1 = Minecraft.getInstance().getBlockColors().getColor(state.getFluidState().getBlockState(), Minecraft.getInstance().world, pos1, 0);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									createQuad(
											new Vector3d(0, state.getFluidState().getLevel() / scl, 1),
											new Vector3d(1, state.getFluidState().getLevel() / scl, 1),
											new Vector3d(1, state.getFluidState().getLevel() / scl, 0),
											new Vector3d(0, state.getFluidState().getLevel() / scl, 0),
											array[0], Direction.UP, 0, 0.5f
									),
									((col1 >> 16) & 0xFF) / 255f,
									((col1 >> 8) & 0xFF) / 255f,
									((col1) & 0xFF) / 255f,
									light, OverlayTexture.NO_OVERLAY
							);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									createQuad(
											new Vector3d(0, 0, 0),
											new Vector3d(1, 0, 0),
											new Vector3d(1, 0, 1),
											new Vector3d(0, 0, 1),
											array[0], Direction.UP, 0, 0.5f
									),
									((col1 >> 16) & 0xFF) / 255f,
									((col1 >> 8) & 0xFF) / 255f,
									((col1) & 0xFF) / 255f,
									light, OverlayTexture.NO_OVERLAY
							);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									createQuad(
											new Vector3d(0, state.getFluidState().getLevel() / scl, 0),
											new Vector3d(0, 0, 0),
											new Vector3d(0, 0, 1),
											new Vector3d(0, state.getFluidState().getLevel() / scl, 1),
											array[0], Direction.UP, 0, 0.5f
									),
									((col1 >> 16) & 0xFF) / 255f,
									((col1 >> 8) & 0xFF) / 255f,
									((col1) & 0xFF) / 255f,
									light, OverlayTexture.NO_OVERLAY
							);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									createQuad(
											new Vector3d(1, state.getFluidState().getLevel() / scl, 0),
											new Vector3d(1, 0, 0),
											new Vector3d(0, 0, 0),
											new Vector3d(0, state.getFluidState().getLevel() / scl, 0),
											array[0], Direction.UP, 0, 0.5f
									),
									((col1 >> 16) & 0xFF) / 255f,
									((col1 >> 8) & 0xFF) / 255f,
									((col1) & 0xFF) / 255f,
									light, OverlayTexture.NO_OVERLAY
							);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									createQuad(
											new Vector3d(0, state.getFluidState().getLevel() / scl, 1),
											new Vector3d(0, 0, 1),
											new Vector3d(1, 0, 1),
											new Vector3d(1, state.getFluidState().getLevel() / scl, 1),
											array[0], Direction.UP, 0, 0.5f
									),
									((col1 >> 16) & 0xFF) / 255f,
									((col1 >> 8) & 0xFF) / 255f,
									((col1) & 0xFF) / 255f,
									light, OverlayTexture.NO_OVERLAY
							);
							builder.addQuad(
									event.getMatrixStack().getLast(),
									createQuad(
											new Vector3d(1, state.getFluidState().getLevel() / scl, 1),
											new Vector3d(1, 0, 1),
											new Vector3d(1, 0, 0),
											new Vector3d(1, state.getFluidState().getLevel() / scl, 0),
											array[0], Direction.UP, 0, 0.5f
									),
									((col1 >> 16) & 0xFF) / 255f,
									((col1 >> 8) & 0xFF) / 255f,
									((col1) & 0xFF) / 255f,
									light, OverlayTexture.NO_OVERLAY
							);
						}
						event.getMatrixStack().pop();
						if (!Minecraft.getInstance().world.getBlockState(pos1).isAir()) {
							float amt = 0.01f;
							event.getMatrixStack().translate(-amt / 2f, -amt / 2f, -amt / 2f);
							event.getMatrixStack().scale(1 + amt, 1 + amt, 1 + amt);
							model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(Blocks.RED_STAINED_GLASS.getDefaultState());
							for (Direction dir : dirs) {
								for (BakedQuad qd : model.getQuads(state, dir, new Random(pos1.toLong()))) {
									int col = Minecraft.getInstance().getBlockColors().getColor(state, Minecraft.getInstance().world, pos1, qd.getTintIndex());
									Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getTranslucent()).addQuad(
											event.getMatrixStack().getLast(), qd,
											((col >> 16) & 0xFF) / 255f,
											((col >> 8) & 0xFF) / 255f,
											((col) & 0xFF) / 255f,
											LightTexture.packLight(15, 15), OverlayTexture.NO_OVERLAY
									);
								}
							}
						}
					}
				}
				event.getMatrixStack().pop();
			}
			event.getMatrixStack().translate(-10000, -10000, -10000);
			Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(Blocks.GLASS.getDefaultState(), event.getMatrixStack(), Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), 0, OverlayTexture.NO_OVERLAY);
			event.getMatrixStack().pop();
		}
		BetterFPSWrapper.endSection();
	}
}
