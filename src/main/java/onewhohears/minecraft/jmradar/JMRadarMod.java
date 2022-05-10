package onewhohears.minecraft.jmradar;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.client.ClientCommandHandler;
import onewhohears.minecraft.jmradar.api.ApiRadarEntity;
import onewhohears.minecraft.jmradar.command.JMRadarCommand;
import onewhohears.minecraft.jmradar.config.ConfigManager;
import onewhohears.minecraft.jmradar.events.EventPlayerTick;
import onewhohears.minecraft.jmradar.events.EventServerTick;

@Mod(modid = JMRadarMod.MOD_ID, name = JMRadarMod.MOD_NAME,
	version = JMRadarMod.MOD_VERSION, dependencies = JMRadarMod.MOD_DEPENDENCIES)
public class JMRadarMod {
	
	public static boolean mcHeliRadar = true;
	public static final String mcHeliPrefix = "!P-";
	
	public static final String MOD_ID = "jmradar";
	public static final String MOD_NAME = "Journey Map Radar 1.7.10";
	public static final String MOD_VERSION = "0.3.0";
	public static final String MOD_DEPENDENCIES = "journeymap, mcheli, journeymap_api_1.7.10";
	
    public static Logger logger;
    
    @SidedProxy(clientSide = "onewhohears.minecraft.jmradar.ClientProxy", 
    		    serverSide = "onewhohears.minecraft.jmradar.CommonProxy")
    public static CommonProxy proxy;
    
    public static FMLEventChannel Channel;
    public static FMLEventChannel ChannelPlayer;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("JMR_Server");
        ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("JMR_Player");
        proxy.load();
        ConfigManager.init(event.getModConfigurationDirectory().toString()+"/config");
        FMLCommonHandler.instance().bus().register(new ConfigManager());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	if (event.getSide() == Side.CLIENT) {
        	ClientCommandHandler.instance.registerCommand(new JMRadarCommand());
    	}
    	FMLCommonHandler.instance().bus().register(new EventPlayerTick());
    	FMLCommonHandler.instance().bus().register(new EventServerTick());
    }
    
    @EventHandler
    public void started(FMLServerStartedEvent event) {
    	new ApiRadarEntity();
    }
    
}
