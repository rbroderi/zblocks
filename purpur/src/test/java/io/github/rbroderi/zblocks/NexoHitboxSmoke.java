package io.github.rbroderi.zblocks;
import com.nexomc.nexo.mechanics.furniture.hitbox.FurnitureHitbox;
import java.io.InputStreamReader;
import org.bukkit.configuration.file.YamlConfiguration;
/** Exercises the installed Nexo parser, rather than just checking YAML key spelling. */
public final class NexoHitboxSmoke {
    public static void main(String[] args){
        var defs=YamlConfiguration.loadConfiguration(new InputStreamReader(NexoHitboxSmoke.class.getResourceAsStream("/nexo-items.yml")));
        RemovalSettings.load(new YamlConfiguration(),name->50,message->{throw new AssertionError(message);}).configure(defs);
        int cubes=0,shapes=0;
        for(String id:defs.getKeys(false)){
            var section=defs.getConfigurationSection(id+".Mechanics.furniture.hitbox");var hitbox=new FurnitureHitbox(section);
            if(!hitbox.getBarriers().isEmpty())throw new AssertionError("Backing block ownership violated: "+id);
            if(id.startsWith("zblocks_shape_")){
                if(hitbox.getInteractions().isEmpty()||hitbox.getGhasts().isEmpty())throw new AssertionError("Partial shape did not parse: "+id);
                shapes++;
            }else{
                if(!hitbox.getInteractions().isEmpty()||!hitbox.getGhasts().isEmpty())throw new AssertionError("Native cube still intercepts placement: "+id);
                cubes++;
            }
        }
        System.out.println("Exact Nexo parser: "+cubes+" visual/native definitions have no packet hitboxes; "+shapes+" partial shapes retain interaction and collision.");
    }
}
