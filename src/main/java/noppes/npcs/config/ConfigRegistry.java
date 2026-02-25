package noppes.npcs.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigRegistry {
    public static Configuration config;

    public static int StartBlockID = 3500;
    public static int StartItemID = 12000;

    public static void init(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            StartBlockID = config.get("General", "Start Block ID", 3500).getInt();
            StartItemID = config.get("General", "Start Item ID", 12000).getInt();
        } finally {
            ConfigRegistry.save();
        }
    }

    public static void save(){
        if (config.hasChanged()) {
            config.save();
        }
    }
}
