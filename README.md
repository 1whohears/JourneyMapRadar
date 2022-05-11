# JourneyMapRadar
A simple mod that uses Journey Map Waypoints to show Radar Pings.
## Dependencies
https://www.curseforge.com/minecraft/mc-mods/mcheli-minecraft-helicopter-mod or some other version of mcheli

https://www.curseforge.com/minecraft/mc-mods/journeymap

https://www.curseforge.com/minecraft/mc-mods/journey-map-qol
## Features
Note: There are a lot of things I want to add, and I'd appreciate you're suggestions and bug reports. 
### Mcheli Waypoint Radar
If a player rides an mcheli aircraft that has an "entity radar" mode, yellow waypoints ate location of other players in their mcheli aircraft will be sent to the player. 

Players on the same team automatically share radar pings.

You can modify the range and stealth values for each plane in the config file. (**/config/configjmradar.cfg)
### API 
Register an entity with ApiRadarEntity.instance.addRadar(id, range, radarRate, targetType, radarEntity, playerEntity, infoRange) to have an entity send radar data to registered players. Every radar must have a unique string id. 

Target Types: 0 = Players | 1 = Player Riding Mcheli Aircraft | 2 = Mobs

Example code with custom npcs in the scripted init hook:
```
var apiinstance = Java.type("onewhohears.minecraft.jmradar.api.ApiRadarEntity").instance;
var radar = apiinstance.getRadarById("testid");
if (radar == null) radar = apiinstance.addRadar("testid", 1000, 20, 1, npc.getMCEntity(), world.getPlayer("example_name").getMCEntity(), 10);
radar.addPlayer(world.getPlayer("example_name_2").getMCEntity());
```
## Commands
`/jmradar clearpings` removes all ping waypoints.
