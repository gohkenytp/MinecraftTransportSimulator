package minecrafttransportsimulator.blocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import minecrafttransportsimulator.MTS;
import minecrafttransportsimulator.baseclasses.MTSBlockTileEntity;
import minecrafttransportsimulator.baseclasses.MTSTileEntity;
import minecrafttransportsimulator.dataclasses.MTSRegistry;
import minecrafttransportsimulator.minecrafthelpers.BlockHelper;
import minecrafttransportsimulator.minecrafthelpers.ItemStackHelper;
import minecrafttransportsimulator.minecrafthelpers.PlayerHelper;
import minecrafttransportsimulator.packets.general.ChatPacket;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockSurveyFlag extends MTSBlockTileEntity{
	private static final Map<EntityPlayer, int[]> firstPosition = new HashMap<EntityPlayer, int[]>();
	private static final Map<EntityPlayer, Integer> firstDimension = new HashMap<EntityPlayer, Integer>();
	
	public BlockSurveyFlag(){
		super(Material.wood, 2.0F, 10.0F);
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack){
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		if(entity instanceof EntityPlayer){
			linkFlags(world, x, y, z, (EntityPlayer) entity);
		}
	}
	
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ){
		linkFlags(world, x, y, z, player);
		return true;
	}
	
	private void linkFlags(World world, int x, int y, int z, EntityPlayer player){
		if(!world.isRemote){
			TileEntitySurveyFlag tile = ((TileEntitySurveyFlag) BlockHelper.getTileEntityFromCoords(world, x, y, z));
			if(!player.isSneaking() && PlayerHelper.getHeldStack(player) != null){
				if(PlayerHelper.getHeldStack(player).getItem().equals(MTSRegistry.track)){
					if(tile.linkedCurve != null){
						if(!PlayerHelper.isPlayerCreative(player)){
							if(PlayerHelper.getQtyOfItemInInventory(MTSRegistry.track, (short) ItemStackHelper.getItemDamage(PlayerHelper.getHeldStack(player)), player) < Math.round(tile.linkedCurve.pathLength)){
								MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.failure.materials") + " " + String.valueOf((int) Math.round(tile.linkedCurve.pathLength))), (EntityPlayerMP) player);
								return;
							}
						}
						int[] blockingBlock = tile.spawnDummyTracks();
						if(blockingBlock != null){
							MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.failure.blockage") + " X:" + blockingBlock[0] + " Y:" + blockingBlock[1] + " Z:" + blockingBlock[2]), (EntityPlayerMP) player);
						}else{
							if(!PlayerHelper.isPlayerCreative(player)){
								PlayerHelper.removeQtyOfItemInInventory(MTSRegistry.track, ItemStackHelper.getItemDamage(PlayerHelper.getHeldStack(player)), (short) Math.round(tile.linkedCurve.pathLength), player);
							}
						}
					}else{
						MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.failure.nolink")), (EntityPlayerMP) player);
					}
					return;
				}
			}
			if(firstPosition.containsKey(player)){
				if(firstDimension.get(player) != world.provider.dimensionId){
					MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.failure.dimension")), (EntityPlayerMP) player);
					resetMaps(player);
				}else if(Arrays.equals(firstPosition.get(player), new int[]{x, y, z})){
					MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.info.clear")), (EntityPlayerMP) player);
					resetMaps(player);
				}else if(Math.sqrt(Math.pow(x - firstPosition.get(player)[0], 2) + Math.pow(y - firstPosition.get(player)[1], 2) + Math.pow(z - firstPosition.get(player)[2], 2)) > 128){
					MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.failure.distance")), (EntityPlayerMP) player);
					resetMaps(player);
				}else{
					//Make sure flag has not been removed since linking.
					if(BlockHelper.getTileEntityFromCoords(world, firstPosition.get(player)[0], firstPosition.get(player)[1], firstPosition.get(player)[2]) instanceof TileEntitySurveyFlag){
						TileEntitySurveyFlag firstFlag = (TileEntitySurveyFlag) BlockHelper.getTileEntityFromCoords(world, firstPosition.get(player)[0], firstPosition.get(player)[1], firstPosition.get(player)[2]);
						TileEntitySurveyFlag secondFlag = (TileEntitySurveyFlag) BlockHelper.getTileEntityFromCoords(world, x, y, z);
						firstFlag.linkToFlag(new int[]{x, y, z}, true);
						secondFlag.linkToFlag(firstPosition.get(player), false);
						MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.info.link")), (EntityPlayerMP) player);
						resetMaps(player);
					}else{
						MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.info.clear")), (EntityPlayerMP) player);
						resetMaps(player);
					}
				}
			}else{
				firstPosition.put(player, new int[]{x, y, z});
				firstDimension.put(player, world.provider.dimensionId);
				MTS.MFSNet.sendTo(new ChatPacket(PlayerHelper.getTranslatedText("interact.flag.info.set")), (EntityPlayerMP) player);
			}
		}
	}
	
	private static final void resetMaps(EntityPlayer player){
		firstPosition.remove(player);
		firstDimension.remove(player);
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta){
		((TileEntitySurveyFlag) BlockHelper.getTileEntityFromCoords(world, x, y, z)).clearFlagLinking();
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public MTSTileEntity getTileEntity(){
		return new TileEntitySurveyFlag();
	}

	@Override
	protected boolean isBlock3D(){
		return true;
	}

	@Override
	protected void setDefaultBlockBounds(){
		this.setBlockBounds(0.4375F, 0.0F, 0.4375F, 0.5625F, 1F, 0.5625F);
	}

	@Override
	protected void setBlockBoundsFromMetadata(int metadata){
		setDefaultBlockBounds();
	}
}
