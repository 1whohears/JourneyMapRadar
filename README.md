# JourneyMapRadar
A simple mod that uses Journey Map Waypoints to show Radar Pings.

There are still plenty of things I want to add and I'm sure plenty of things don't work. 

I'd appreciate you're suggestions and bug reports.
## Dependencies
https://www.curseforge.com/minecraft/mc-mods/mcheli-minecraft-helicopter-mod or some other variant of mcheli

https://www.curseforge.com/minecraft/mc-mods/journeymap

https://www.curseforge.com/minecraft/mc-mods/journey-map-qol
## Features
### Mcheli Waypoint Radar
If a player rides any mcheli aircraft that has an "entity radar" waypoints displaying the location of other players in their mcheli aircraft will be sent to the player. 

Radar pings from players on your team will be automatically shared to you.

You can modify the range and stealth values for each plane in the config file. (**/config/configjmradar.cfg)
### API (WIP)
Register an entity with a player name and other parameters with ApiRadarEntity.instance.addRadar(...) to have an entity send radar data to that player. 
## Commands
`/jmradar clearpings` removes all ping waypoints.
