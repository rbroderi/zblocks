package io.github.rbroderi.zblocks;
import java.util.*;
import io.github.rbroderi.zblocks.internal.*;
import static io.github.rbroderi.zblocks.PuzzleBlock.Kind.*;
import java.util.function.ToDoubleFunction;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
/** Validated per-kind and per-color mining policy; invalid values fail closed. */
final class RemovalSettings {
    record Rule(double hardness){
        boolean adminOnly(){return hardness<0;}
        double miningHardness(){return adminOnly()?50:hardness;}
        boolean canMine(org.bukkit.GameMode mode,boolean canUse,boolean admin){return mode==org.bukkit.GameMode.SURVIVAL&&(adminOnly()?admin:canUse);}
        boolean instantRemoval(org.bukkit.GameMode mode,boolean admin,boolean canUse){return mode==org.bukkit.GameMode.CREATIVE&&(admin||!adminOnly()&&canUse);}
    }
    private final Rule fallback;private final Map<String,Rule> rules;
    private RemovalSettings(Rule fallback,Map<String,Rule> rules){this.fallback=fallback;this.rules=Map.copyOf(rules);}
    static RemovalSettings load(ConfigurationSection config,java.util.function.Consumer<String> warning){
        return load(config,name->{Material m=Material.matchMaterial(name);if(m==null||!m.isBlock())throw new IllegalArgumentException("Unknown block material: "+name);return m.getHardness();},warning);
    }
    static RemovalSettings load(ConfigurationSection config,ToDoubleFunction<String> materials,java.util.function.Consumer<String> warning){
        Rule fallback=parse(config.get("removal-hardness.default","obsidian"),materials,warning);
        Map<String,Rule> rules=new HashMap<>();var section=config.getConfigurationSection("removal-hardness.items");
        if(section!=null)for(String key:section.getKeys(false)){
            String normalized=key.toLowerCase(Locale.ROOT).replace('-','_');
            boolean valid=false;for(var kind:PuzzleBlock.Kind.values()){
                String k=kind.name().toLowerCase(Locale.ROOT);if(normalized.equals(k))valid=true;
                if(kind==PuzzleBlock.Kind.PUSH||kind==PuzzleBlock.Kind.DEPRESS||kind==PuzzleBlock.Kind.START)for(var color:PuzzleBlock.Color.values())if(normalized.equals(k+"_"+color.name().toLowerCase(Locale.ROOT)))valid=true;
            }
            if(!valid){warning.accept("Unknown removal-hardness item: "+key);continue;}
            rules.put(normalized,parse(section.get(key),materials,warning));
        }
        return new RemovalSettings(fallback,rules);
    }
    private static Rule parse(Object value,ToDoubleFunction<String> materials,java.util.function.Consumer<String> warning){
        try{
            String name=String.valueOf(value).trim().toLowerCase(Locale.ROOT);if(name.equals("bedrock"))return new Rule(-1);
            if(name.equals("obisdian"))name="obsidian";
            double hardness;try{hardness=Double.parseDouble(name);}catch(NumberFormatException ex){hardness=materials.applyAsDouble(name);}
            if(!Double.isFinite(hardness)||hardness<0||hardness>100000)throw new IllegalArgumentException("Hardness must be between 0 and 100000");
            return new Rule(hardness);
        }catch(RuntimeException ex){warning.accept("Invalid removal hardness '"+value+"': "+ex.getMessage()+"; using bedrock protection");return new Rule(-1);}
    }
    void configure(org.bukkit.configuration.file.YamlConfiguration definitions){
        for(String id:definitions.getKeys(false)){
            var kind=PuzzleModels.kindFromNexoId(id.replace("zblocks_mining_","zblocks_"));var color=kind==null?PuzzleBlock.Color.BASE:PuzzleModels.colorFromNexoId(id.replace("zblocks_mining_","zblocks_"));
            if(kind==null&&id.startsWith("zblocks_shape_"))for(var candidate:new PuzzleBlock.Kind[]{ACTIVATE,BOMB}){
                var zero=new Position(new UUID(0,0),0,0,0);var sample=new PuzzleBlock(UUID.randomUUID(),candidate,PuzzleBlock.Color.BASE,zero,zero,"minecraft:air","minecraft:air");
                if(id.equals("zblocks_shape_"+EntityHitboxes.shapeKey(PuzzleHitboxes.boxes(sample))))kind=candidate;
            }
            if(kind!=null){definitions.set(id+".Mechanics.furniture.hardness",rule(kind,color).miningHardness());
                // Full cubes have a real gameplay-owned block. A second packet hitbox
                // intercepts adjacent placement and is unnecessary for collision/use.
                if(!id.startsWith("zblocks_shape_"))definitions.set(id+".Mechanics.furniture.hitbox.interactions",List.of());
            }
        }
        }
    Rule rule(PuzzleBlock.Kind kind,PuzzleBlock.Color color){String k=kind.name().toLowerCase(Locale.ROOT);return rules.getOrDefault(k+"_"+color.name().toLowerCase(Locale.ROOT),rules.getOrDefault(k,fallback));}
}
