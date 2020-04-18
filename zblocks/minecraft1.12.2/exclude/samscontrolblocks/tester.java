package com.lothrazar.samscontrolblocks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Block> ignoreList = new ArrayList<Block>();
		Field[] fields = Blocks.class.getFields();
		for (Field f : fields) {
			//  if (f.isAccessible() && Modifier.isStatic(f.getModifiers()) && f.getName() != "CONCRETE_POWDER") {
         	if (f.getType()==net.minecraft.block.Block.class && Modifier.isStatic(f.getModifiers()) && f.getName() != "CONCRETE_POWDER") {
         		System.out.println(f);
         		/*
		        try {
		        	// && f.getType()==Blocks.class
					ignoreList.add((Block) f.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		    }
		    */
		}
		}
	
	
		//System.out.println(Arrays.toString(ignoreList.toArray()));
	}
}
