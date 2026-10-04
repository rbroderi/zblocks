package io.github.rbroderi.zblocks;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class RemovalSettingsTest {
    private static final java.util.function.ToDoubleFunction<String> MATERIALS=name->switch(name){case "obsidian"->50;case "stone"->1.5;case "sand"->.5;default->throw new IllegalArgumentException("Unknown material");};
    @Test void missingSettingsUseObsidianMining(){var rules=RemovalSettings.load(new YamlConfiguration(),MATERIALS,msg->fail(msg));for(var kind:PuzzleBlock.Kind.values())for(var color:PuzzleBlock.Color.values())assertEquals(50,rules.rule(kind,color).miningHardness());}
    @Test void materialKindAndColorOverridesHaveStablePrecedence(){var config=new YamlConfiguration();config.set("removal-hardness.default","sand");config.set("removal-hardness.items.push","obsidian");config.set("removal-hardness.items.push_blue","stone");config.set("removal-hardness.items.bomb",3.25);var rules=RemovalSettings.load(config,MATERIALS,msg->fail(msg));assertEquals(50,rules.rule(PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.RED).hardness());assertEquals(1.5,rules.rule(PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.BLUE).hardness());assertEquals(.5,rules.rule(PuzzleBlock.Kind.START,PuzzleBlock.Color.BASE).hardness());assertEquals(3.25,rules.rule(PuzzleBlock.Kind.BOMB,PuzzleBlock.Color.BASE).hardness());}
    @Test void invalidValuesFailClosedAndTypoIsAccepted(){var config=new YamlConfiguration();config.set("removal-hardness.default",0);config.set("removal-hardness.items.push","obisdian");config.set("removal-hardness.items.bomb","NaN");config.set("removal-hardness.items.start",-2);config.set("removal-hardness.items.hourglass","missing");config.set("removal-hardness.items.unknown",2);var warnings=new ArrayList<String>();var rules=RemovalSettings.load(config,MATERIALS,warnings::add);assertEquals(50,rules.rule(PuzzleBlock.Kind.PUSH,PuzzleBlock.Color.BASE).hardness());assertTrue(rules.rule(PuzzleBlock.Kind.BOMB,PuzzleBlock.Color.BASE).adminOnly());assertTrue(rules.rule(PuzzleBlock.Kind.START,PuzzleBlock.Color.BASE).adminOnly());assertTrue(rules.rule(PuzzleBlock.Kind.HOURGLASS,PuzzleBlock.Color.BASE).adminOnly());assertEquals(0,rules.rule(PuzzleBlock.Kind.CACTUS,PuzzleBlock.Color.BASE).hardness());assertEquals(4,warnings.size());}
    @Test void everyNexoStateUsesTheConfiguredHardness(){
        var config=new YamlConfiguration();config.set("removal-hardness.items.push","obsidian");config.set("removal-hardness.items.push_blue","stone");config.set("removal-hardness.items.activate",3);config.set("removal-hardness.items.bomb",4);config.set("removal-hardness.items.switch_off",2);
        var rules=RemovalSettings.load(config,MATERIALS,msg->fail(msg));var defs=YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(getClass().getResourceAsStream("/nexo-items.yml")));rules.configure(defs);
        assertEquals(69,defs.getKeys(false).size());assertEquals(50,defs.getDouble("zblocks_hourglass.Mechanics.furniture.hardness"));int shapeCount=0;
        for(String id:defs.getKeys(false)){
            double hardness=defs.getDouble(id+".Mechanics.furniture.hardness");
            if(id.startsWith("zblocks_push_blue_"))assertEquals(1.5,hardness);
            else if(id.startsWith("zblocks_push_"))assertEquals(50,hardness);
            else if(id.startsWith("zblocks_shape_")){assertTrue(hardness==3||hardness==4);shapeCount++;}
        }
        assertEquals(2,shapeCount);assertEquals(2,defs.getDouble("zblocks_switch_off_off.Mechanics.furniture.hardness"));
    }
    @Test void survivalAlwaysMinesOverTimeAndCreativeKeepsInstantRemoval(){
        var bedrock=new RemovalSettings.Rule(-1);var obsidian=new RemovalSettings.Rule(50);
        for(var mode:org.bukkit.GameMode.values())assertFalse(bedrock.instantRemoval(mode,false,true));
        assertFalse(bedrock.instantRemoval(org.bukkit.GameMode.SURVIVAL,true,true));assertFalse(obsidian.instantRemoval(org.bukkit.GameMode.SURVIVAL,true,true));
        assertTrue(bedrock.canMine(org.bukkit.GameMode.SURVIVAL,true,true));assertFalse(bedrock.canMine(org.bukkit.GameMode.SURVIVAL,true,false));assertEquals(50,bedrock.miningHardness());
        assertTrue(obsidian.canMine(org.bukkit.GameMode.SURVIVAL,true,true));assertTrue(bedrock.canMine(org.bukkit.GameMode.SURVIVAL,true,true));
        assertFalse(bedrock.instantRemoval(org.bukkit.GameMode.SPECTATOR,true,true));
        assertTrue(obsidian.canMine(org.bukkit.GameMode.SURVIVAL,true,false));assertFalse(obsidian.canMine(org.bukkit.GameMode.SURVIVAL,false,false));assertFalse(obsidian.canMine(org.bukkit.GameMode.ADVENTURE,true,true));
        assertTrue(obsidian.instantRemoval(org.bukkit.GameMode.CREATIVE,false,true));assertFalse(obsidian.instantRemoval(org.bukkit.GameMode.CREATIVE,false,false));assertTrue(bedrock.instantRemoval(org.bukkit.GameMode.CREATIVE,true,true));
    }
}
