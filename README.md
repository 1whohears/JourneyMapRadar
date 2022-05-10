# JourneyMapRadar
A simple mod that uses Journey Map Waypoints to show Radar Pings
## Dependencies
https://www.curseforge.com/minecraft/mc-mods/mcheli-minecraft-helicopter-mod

https://www.curseforge.com/minecraft/mc-mods/journeymap

https://www.curseforge.com/minecraft/mc-mods/journey-map-qol
## Features
### Mcheli Waypoint Radar
If a player rides any mcheli aircraft that has an "entity radar" waypoints displaying the location of other players in their mcheli aircraft will be sent to the player. 

Waypoints of players on your team will be automatically shared to you.

You can modify the range and stealth values for each plane in the config file. (**/config/configjmradar.cfg)
### API
Register an entity with a player name and other parameters with ApiRadarEntity.instance.addRadar(...) to have an entity send radar data to that player. At least that's what it's supposed to do. It's not finished and probably doesn't work, but it's not the main point of this mod anyway.
## Commands
`/jmradar clearpings` removes all ping waypoints.
