package onewhohears.minecraft.jmradar;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.client.ClientCommandHandler;
import onewhohears.minecraft.jmradar.api.ApiMcheliBvr;
import onewhohears.minecraft.jmradar.api.ApiRadarEntity;
import onewhohears.minecraft.jmradar.command.ClearPingsCommand;
import onewhohears.minecraft.jmradar.command.JMRadarCommand;
import onewhohears.minecraft.jmradar.config.ConfigManager;
import onewhohears.minecraft.jmradar.events.EventPlayerTick;
import onewhohears.minecraft.jmradar.events.EventServerTick;

@Mod(modid = JMRadarMod.MOD_ID, name = JMRadarMod.MOD_NAME,
	version = JMRadarMod.MOD_VERSION, dependencies = JMRadarMod.MOD_DEPENDENCIES)
public class JMRadarMod {
	
	public static boolean mcHeliRadar = true;
	
	public static final String MOD_ID = "jmradar";
	public static final String MOD_NAME = "Journey Map Radar 1.7.10";
	public static final String MOD_VERSION = "0.3.1";
	public static final String MOD_DEPENDENCIES = "required-after:journeymap;required-after:mcheli@[1.0.3,);required-after:journeymap_api_1.7.10@[0.8.7,)";
	
    public static Logger logger;
    
    @SidedProxy(clientSide = "onewhohears.minecraft.jmradar.ClientProxy", 
    		    serverSide = "onewhohears.minecraft.jmradar.CommonProxy")
    public static CommonProxy proxy;
    
    public static FMLEventChannel Channel;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("JMR_Server");
        proxy.load();
        ConfigManager.init(event.getModConfigurationDirectory().toString()+"/");
        FMLCommonHandler.instance().bus().register(new ConfigManager());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	if (event.getSide() == Side.CLIENT) {
        	ClientCommandHandler.instance.registerCommand(new ClearPingsCommand());
    	}
    	FMLCommonHandler.instance().bus().register(new EventPlayerTick());
    	FMLCommonHandler.instance().bus().register(new EventServerTick());
    }
    
    @EventHandler
    public void started(FMLServerStartedEvent event) {
    	new ApiRadarEntity();
    	new ApiMcheliBvr();
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
    	event.registerServerCommand(new JMRadarCommand());
    }
    
    @EventHandler
    public void stopped(FMLServerStoppedEvent event) {
    	ApiRadarEntity.instance = null;
    	ApiMcheliBvr.instance = null;
    }
    
}
