package io.github.rbroderi.zblocks;
/** Grid movement is based on the adjacent player cell, never diagonal camera yaw. */
final class PuzzleInteraction {
    static int[] pushDirection(int playerX,int playerZ,int blockX,int blockZ){
        int dx=blockX-playerX,dz=blockZ-playerZ;
        return Math.abs(dx)+Math.abs(dz)==1?new int[]{dx,dz}:null;
    }
    static int[] pushSide(double playerX,double playerZ,int blockX,int blockZ){
        double dx=blockX+.5-playerX,dz=blockZ+.5-playerZ;
        if(Math.max(Math.abs(dx),Math.abs(dz))<.5)return null;
        return Math.abs(dx)>=Math.abs(dz)?new int[]{dx>0?1:-1,0}:new int[]{0,dz>0?1:-1};
    }
    static boolean pickaxe(org.bukkit.Material material){return material!=null&&material.name().endsWith("_PICKAXE");}
    static boolean weapon(org.bukkit.Material material){if(material==null)return false;String n=material.name();return n.endsWith("_SWORD")||n.endsWith("_AXE")||n.endsWith("_SPEAR")||n.equals("TRIDENT")||n.equals("MACE");}
    static boolean building(boolean sneaking,boolean blockItem){return blockItem;}
}
