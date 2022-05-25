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
	
	public static final String GENERAL_MCHELI_RADAR = "General Mcheli Radar";
	public static int mcheliPingAgeBuffer;
	public static int maxMcheliBvrMissileAge;
	public static boolean bvrMode;
	public static boolean looseTargetKillMissiles;
	public static int defaultPingColor;
	public static String radarPrefix;
	
	public static final String CATEGORY_MCHELI_RANGE = "Mcheli Range";
	public static final float minRange = 0, maxRange = 100000;
	public static double defaultMcheliRange;
	private static String[] mcheliRangeStrings;
	
	public static final String CATEGORY_MCHELI_STEALTH = "Mcheli Stealth";
	public static final float minStealth = 0, maxStealth = 1;
	public static double defaultMcheliStealth;
	private static String[] mcheliStealthStrings;
	
	public static final String CATEGORY_MCHELI_RADAR_RATE = "Mcheli Radar Rate";
	public static final int minRate = 40, maxRate = 1000;
	public static int defaultMcheliRadarRate;
	private static String[] mcheliRadarRateStrings;
	
	public static final String CATEGORY_BVR_RANGE = "Mcheli BVR Missile Range";
	public static double defaultMaxBvrMissileRange;
	private static String[] missileRangeStrings;
 	
	private static List<McheliData> mcheliList = new ArrayList<McheliData>();
	private static List<MissileData> missileList = new ArrayList<MissileData>();
	
	public static void init(String configDir) {
		if (config == null) {
			File path = new File(configDir + JMRadarMod.MOD_ID + ".cfg");
			config = new Configuration(path);
			loadConfig();
		}
	}
	
	private static void loadConfig() {
		// general
		mcheliPingAgeBuffer = config.getInt("Mcheli Ping Age Buffer", GENERAL_MCHELI_RADAR, 10, 0, 100, 
				"Ticks added to a ping's max age before it disapears. "
				+ "Useful if the server is being janky so people's pings doesn't disapear when they shouldn't.");
		maxMcheliBvrMissileAge = config.getInt("Max Mcheli BVR Missile Age", GENERAL_MCHELI_RADAR, 600, 0, 4800, 
				"Ticks before an Mcheli BVR Missile Dies by running out of fuel. "
				+ "The Mcheli Mod Might kill the missile before your value anyway.");
		bvrMode = config.getBoolean("Beyond Visual Range Mode", GENERAL_MCHELI_RADAR, true, 
				"Use a command to launch missiles at radar pings.");
		looseTargetKillMissiles = config.getBoolean("Kill Missiles if Loose Track on Radar", GENERAL_MCHELI_RADAR, false, 
				"Disable this if the server is laggy and pings don't refresh in time. "
				+ "This means you only need to lock onto a target once to shoot them with a bvr missile.");
		defaultPingColor = config.getInt("Default Ping Color", GENERAL_MCHELI_RADAR, 0xe3b016, 0, 0xffffff, 
				"The default color radar pings will be displayed as.");
		radarPrefix = config.getString("Radar Ping Prefix", GENERAL_MCHELI_RADAR, "!P-", 
				"The first part of the name of a ping waypoint.");
		// range
		defaultMcheliRange = config.getFloat("Default Mcheli Range", CATEGORY_MCHELI_RANGE, 800f, minRange, maxRange, 
				"The default range of an mcheli aircraft radar.");
		mcheliRangeStrings = config.getStringList("Aircraft Range Overrides", CATEGORY_MCHELI_RANGE, getDefaultRanges(), 
				"Set custom ranges for mcheli aircraft using their display name. <aircraft name>=<range>");
		parseRanges();
		// stealth
		defaultMcheliStealth = config.getFloat("Default Mcheli Stealth", CATEGORY_MCHELI_STEALTH, 1.0f, minStealth, maxStealth, 
				"The default stealth of a mcheli aircraft.");
		mcheliStealthStrings = config.getStringList("Aircraft Stealth Overrides", CATEGORY_MCHELI_STEALTH, getDefaultStealth(), 
				"Set custom stealth for mcheli aircraft using their display name. "
				+ "Your_Stealth * Enemy_Range is the distance an enemy can see you. "
				+ "<aircraft name>=<stealth>");
		parseStealth();
		// radar rate
		defaultMcheliRadarRate = config.getInt("Default Mcheli Radar Rate", CATEGORY_MCHELI_RADAR_RATE, 60, minRate, maxRate, 
				"Number of ticks before your mcheli aircraft gets new pings.");
		mcheliRadarRateStrings = config.getStringList("Aircraft Radar Rate Overrides", CATEGORY_MCHELI_RADAR_RATE, getDefaultRates(), 
				"Set custom radar rate for mcheli aircraft using their display name. "
				+ "<aircraft name>=<rate>");
		parseRadarRates();
		// bvr range
		defaultMaxBvrMissileRange = config.getFloat("Default BVR Missile Range", CATEGORY_BVR_RANGE, 1000f, minRange, maxRange, 
				"The default range of a BVR missile radar.");
		missileRangeStrings = config.getStringList("Missile Range Overrides", CATEGORY_BVR_RANGE, getDefaultMissileRanges(), 
				"Set custom ranges for missiles using their display name. "
				+ "<missile name>=<range>");
		parseMissileRanges();
		if (config.hasChanged()) config.save();
	}
	
	@SubscribeEvent
	public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equalsIgnoreCase(JMRadarMod.MOD_ID)) loadConfig();
	}
	
	private static String[] getDefaultRanges() {
		return new String[] {
				"E767=8000",
				"B-2A Spirit=3000",
				"C5A galaxy=3000",
				"F-22A Raptor=2000",
				"Su-47 Berkut=1600",
				"F-35A Lightning II=1500",
				"F-35B Lightning II=1500",
				"F-35C Lightning II=1500",
				"F-15E Strike Eagle=950",
				"F-15 S/MTD=950",
				"F-14D Super Tomcat=900"
		};
	}
	
	private static void parseRanges() {
		for (int i = 0; i < mcheliRangeStrings.length; ++i) {
			if (mcheliRangeStrings[i].contains("=")) {
				String[] data = mcheliRangeStrings[i].split("=");
				if (data.length == 2) {
					double num = defaultMcheliRange;
					try {
						num = Double.parseDouble(data[1]);
					} catch (NumberFormatException e) {
						printRangeParseError(data[1]+" is not a double.");
					}
					mcheliList.add(addRangeData(data[0], num));
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
				String[] data = mcheliStealthStrings[i].split("=");
				if (data.length == 2) {
					double num = defaultMcheliStealth;
					try {
						num = Double.parseDouble(data[1]);
					} catch (NumberFormatException e) {
						printStealthParseError(data[1]+" is not a double.");
					}
					mcheliList.add(addStealthData(num, data[0]));
				} else printStealthParseError(mcheliStealthStrings[i]+" has more than one =");
			} else printStealthParseError(mcheliStealthStrings[i]+" doesn't have =");
		}
	}
	
	private static void printStealthParseError(String error) {
		System.out.println("jmradar.ConfigManager: Stealth Override Parse Error: "+error);
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
	
	private static McheliData addRangeData(String name, double range) {
		McheliData data = getMcheliDatabyName(name);
		if (data == null) return new McheliData(name, range);
		data.setRange(range);
		return data;
	}
	
	private static McheliData addStealthData(double stealth, String name) {
		McheliData data = getMcheliDatabyName(name);
		if (data == null) return new McheliData(stealth, name);
		data.setStealth(stealth);
		return data;
	}
	
	private static McheliData addRadarRateData(String name, int rate) {
		McheliData data = getMcheliDatabyName(name);
		if (data == null) return new McheliData(name, rate);
		data.setRadarRate(rate);
		return data;
	}
	
	public static int getAircraftRadarRate(String name) {
		McheliData data = getMcheliDatabyName(name);
		if (data == null) return defaultMcheliRadarRate;
		return data.getRadarRate();
	}
	
	private static String[] getDefaultRates() {
		return new String[] {
				"E767=70",
				"B-2A Spirit=80",
				"C5A galaxy=80",
				"F-22A Raptor=50",
				"Su-47 Berkut=50",
				"F-35A Lightning II=50",
				"F-35B Lightning II=50",
				"F-35C Lightning II=50"
		};
	}
	
	private static void parseRadarRates() {
		for (int i = 0; i < mcheliRadarRateStrings.length; ++i) {
			if (mcheliRadarRateStrings[i].contains("=")) {
				String[] data = mcheliRadarRateStrings[i].split("=");
				if (data.length == 2) {
					int num = defaultMcheliRadarRate;
					try {
						num = Integer.parseInt(data[1]);
					} catch (NumberFormatException e) {
						printRadarRateParseError(data[1]+" is not an Integer.");
					}
					mcheliList.add(addRadarRateData(data[0], num));
				} else printRadarRateParseError(mcheliRadarRateStrings[i]+" has more than one =");
			} else printRadarRateParseError(mcheliRadarRateStrings[i]+" doesn't have =");
		}
	}
	
	private static void printRadarRateParseError(String error) {
		System.out.println("jmradar.ConfigManager: Radar Rate Override Parse Error: "+error);
	}
	
	public static double getBvrMissileMaxRange(String name) {
		MissileData data = getMissileDataByName(name);
		if (data == null) return defaultMaxBvrMissileRange;
		return data.getRange();
	}
	
	private static String[] getDefaultMissileRanges() {
		return new String[] {
				"AIM-120 AMRAAM=1800",
				"AIM-9 Sidewinder=1600",
				"AIM-92 Stinger=1400",
				"AIM-9X=1400",
				"AIM-7 Sparrow=1200",
				"R-77=1800",
				"R-73=1600",
				"R-73M2=1400",
				"R-27AE AAM=1400",
				"R-77-1 AAM=1400",
				"R-60=1200"
		};
	}
	
	private static void parseMissileRanges() {
		for (int i = 0; i < missileRangeStrings.length; ++i) {
			if (missileRangeStrings[i].contains("=")) {
				String[] data = missileRangeStrings[i].split("=");
				if (data.length == 2) {
					double num = defaultMaxBvrMissileRange;
					try {
						num = Double.parseDouble(data[1]);
					} catch (NumberFormatException e) {
						printMissileRangeParseError(data[1]+" is not a double.");
					}
					missileList.add(addMissileRangeData(data[0], num));
				} else printMissileRangeParseError(missileRangeStrings[i]+" has more than one =");
			} else printMissileRangeParseError(missileRangeStrings[i]+" doesn't have =");
		}
	}
	
	private static void printMissileRangeParseError(String error) {
		System.out.println("jmradar.ConfigManager: BVR Missile Range Override Parse Error: "+error);
	}
	
	private static MissileData addMissileRangeData(String name, double range) {
		MissileData data = getMissileDataByName(name);
		if (data == null) return new MissileData(name, range);
		data.setRange(range);
		return data;
	}
	
	private static MissileData getMissileDataByName(String name) {
		for (int i = 0; i < missileList.size(); ++i) {
			if (missileList.get(i).getName().equals(name)) return missileList.get(i);
		}
		return null;
	}
	
}
