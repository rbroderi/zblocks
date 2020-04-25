package zblocks.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zblocks.Blocks.Interfaces.Colored;
import zblocks.Blocks.Interfaces.Matchable;

public class TransientPuzzleBlock extends Block implements Matchable{
	//private Class<DepressPuzzleBlock> matchType = *.class;
	public static IProperty<Boolean> activated = PropertyBool.create("activated");
	public static final int iACTIVATED = 1, iDISABLED = 0;
	public TransientPuzzleBlock(String name,Material material) {
		super(material);
		setUnlocalizedName(name);
		setRegistryName(name);
		this.setDefaultState(this.blockState.getBaseState().withProperty(activated, false));
	}
	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, activated);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		if (state.equals(this.blockState.getBaseState().withProperty(activated, true))) {
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


	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) {
		return null;
	}
	
	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public boolean matches(Matchable other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getMatchType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getTrait() {
		// TODO Auto-generated method stub
		return null;
	}
	
}