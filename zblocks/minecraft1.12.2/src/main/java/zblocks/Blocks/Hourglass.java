package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zblocks.ZblockConfig;
import zblocks.Blocks.Interfaces.Resettable;
import zblocks.Utility.StaticUtils;

public class Hourglass extends Block {

	// private boolean isActivated = false;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;

	public Hourglass(String name, Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, activated);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state == this.blockState.getBaseState().withProperty(activated, true)) {
			return iACTIVATED;
		} else {
			return iDISABLED;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int i) {
		if (i == iACTIVATED) {
			return this.blockState.getBaseState().withProperty(activated, true);
		} else {
			return this.blockState.getBaseState().withProperty(activated, false);
		}
	}

	// For rendering of block underneath
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	// For correct lighting around the block
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	// resets all resettable blocks in 100 x 100 x100 radius of this hourglass
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else {
			playerIn.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 50, 1));
			StaticUtils.playSound(worldIn, pos, "warp", SoundCategory.BLOCKS, 1f);
			BlockPos northWest = pos.north(ZblockConfig.RESET_RANGE).west(ZblockConfig.RESET_RANGE).up(ZblockConfig.RESET_RANGE / 2);
			BlockPos southEast = pos.south(ZblockConfig.RESET_RANGE).east(ZblockConfig.RESET_RANGE).down(ZblockConfig.RESET_RANGE / 2);
			for (BlockPos bPos : BlockPos.getAllInBoxMutable(northWest, southEast)) {
				Block block = worldIn.getBlockState(bPos).getBlock();
				if (block instanceof Resettable) {
					bPos = bPos.toImmutable();
					((Resettable) block).reset(worldIn, bPos);
					IBlockState old = worldIn.getBlockState(bPos);
					worldIn.notifyBlockUpdate(bPos, old, worldIn.getBlockState(bPos), 3);
				}
			}
			return true;
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}
