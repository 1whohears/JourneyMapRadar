package onewhohears.minecraft.jmradar.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import onewhohears.minecraft.jmradar.JMRadarMod;

public class ConfigManager {
	
	public static Configuration config;
	
	public static final String CATEGORY_MCHELI_RANGE = "Mcheli Range";
	public static final float minRange = 0, maxRange = 100000;
	public static double defaultMcheliRange;
	private static String[] mcheliRangeStrings;
	
	public static final String CATEGORY_MCHELI_STEALTH = "Mcheli Stealth";
	public static final float minStealth = 0, maxStealth = 1;
	public static double defaultMcheliStealth;
	private static String[] mcheliStealthStrings;
	
	private static List<McheliData> mcheliList = new ArrayList<McheliData>();
	
	public static void init(String configDir) {
		if (config == null) {
			File path = new File(configDir + JMRadarMod.MOD_ID + ".cfg");
			config = new Configuration(path);
			loadConfig();
		}
	}
	
	private static void loadConfig() {
		defaultMcheliRange = config.getFloat("Default Mcheli Range", CATEGORY_MCHELI_RANGE, 500, minRange, maxRange, 
				"The default range of an mcheli aircraft radar.");
		mcheliRangeStrings = config.getStringList("Aircraft Range Overrides", CATEGORY_MCHELI_RANGE, getDefaultRanges(), 
				"Set custom ranges for aircraft using their display name. <aircraft name>=<range>");
		parseRanges();
		defaultMcheliStealth = config.getFloat("Default Mcheli Stealth", CATEGORY_MCHELI_STEALTH, 1.0f, minStealth, maxStealth, 
				"The default stealth of a mcheli aircraft.");
		mcheliStealthStrings = config.getStringList("Aircraft Stealth Overrides", CATEGORY_MCHELI_STEALTH, getDefaultStealth(), 
				"Set custom stealth for aircraft using their display name. <aircraft name>=<stealth>");
		parseStealth();
		if (config.hasChanged()) config.save();
	}
	
	@SubscribeEvent
	public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equalsIgnoreCase(JMRadarMod.MOD_ID)) loadConfig();
	}
	
	private static String[] getDefaultRanges() {
		return new String[] {
				"E767=6000",
				"B-2A Spirit=2000",
				"C5A galaxy=2000",
				"F-22A Raptor=1500",
				"Su-47 Berkut=1200",
				"F-35A Lightning II=1100",
				"F-35B Lightning II=1100",
				"F-35C Lightning II=1100",
				"F-15E Strike Eagle=650",
				"F-15 S/MTD=650",
				"F-14D Super Tomcat=600"
		};
	}
	
	private static void parseRanges() {
		for (int i = 0; i < mcheliRangeStrings.length; ++i) {
			if (mcheliRangeStrings[i].contains("=")) {
				String[] data = mcheliRangeStrings[i].split("=");
				if (data.length == 2) {
					float num = -1;
					try {
						num = Float.parseFloat(data[1]);
					} catch (NumberFormatException e) {
						printRangeParseError(data[1]+" is not a float.");
					}
					mcheliList.add(new McheliData(data[0], num));
				} else printRangeParseError(mcheliRangeStrings[i]+" has more than one =");
			} else printRangeParseError(mcheliRangeStrings[i]+" doesn't have =");
		}
	}
	
	private static void printRangeParseError(String error) {
		System.out.println("jmradar.ConfigManager: Range Override Parse Error: "+error);
	}
	
	public static double getPlaneRange(String name) {
		McheliData data = getMcheliDatabyName(name);
		if (data == null) return defaultMcheliRange;
		return data.getRange();
	}
	
	private static String[] getDefaultStealth() {
		return new String[] {
				"B-2A Spirit=0.5",
				"F-22A Raptor=0.65",
				"Su-47 Berkut=0.75",
				"F-35A Lightning II=0.75",
				"F-35B Lightning II=0.75",
				"F-35C Lightning II=0.75",
				"F-15E Strike Eagle=0.9",
				"F-15 S/MTD=0.9",
				"F-14D Super Tomcat=0.9"
		};
	}
	
	private static void parseStealth() {
		for (int i = 0; i < mcheliStealthStrings.length; ++i) {
			if (mcheliStealthStrings[i].contains("=")) {
				String[] data = mcheliRangeStrings[i].split("=");
				if (data.length == 2) {
					float num = -1;
					try {
						num = Float.parseFloat(data[1]);
					} catch (NumberFormatException e) {
						printRangeParseError(data[1]+" is not a float.");
					}
					mcheliList.add(new McheliData(num, data[0]));
				} else printRangeParseError(mcheliStealthStrings[i]+" has more than one =");
			} else printRangeParseError(mcheliStealthStrings[i]+" doesn't have =");
		}
	}
	
	/**
	 * Factor multiplied to a plane's range to know if it can see this plane or not
	 * lower stealth value is better
	 * @param name
	 * @return default is 1.0
	 */
	public static double getPlaneStealth(String name) {
		McheliData data = getMcheliDatabyName(name);
		if (data == null) return defaultMcheliStealth;
		return data.getStealth();
	}
	
	private static McheliData getMcheliDatabyName(String name) {
		for (int i = 0; i < mcheliList.size(); ++i) {
			if (mcheliList.get(i).getName().equals(name)) return mcheliList.get(i);
		}
		return null;
	}
	
}
