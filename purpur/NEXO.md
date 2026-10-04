# Install and build ZBlocks 3.0.26

Requires Purpur 26.3, Java 25, and Nexo. Tested against nexo-1.29-dev-3d4dcd610.jar and Purpur API 26.3.build.2643-experimental (server testing uses build 2641).

Put the built ZBlocks JAR and Nexo in the server plugins directory. ServerBlocks is no longer required. Keep existing puzzle data and configuration. Restart the server, wait for Nexo resource pack generation and hosting, then reconnect and accept its pack. ZBlocks contributes its bundled models, textures and sounds to Nexo; clients need no ZBlocks mod. Optional Jade tooltips require the separately installed Nexo Jade Bridge and Fabric companion.

## Build

Install your own Nexo JAR into your local Maven repository; it is a provided dependency and is not redistributed:

```text
mvn install:install-file -Dfile=/path/to/nexo-1.29-dev-3d4dcd610.jar -DgroupId=com.nexomc -DartifactId=nexo -Dversion=1.29-dev-3d4dcd610 -Dpackaging=jar
mvn -f purpur/pom.xml package
```

Output: `purpur/target/zblocks-3.0.26.jar`. Maven runs the 44 JUnit tests. Java 25 must be selected for Maven.

## Play

`/zblocks give <type> [color]` gives puzzle items to admins; neutral items omit Base from their display names. Left-click push blocks with a non-pickaxe to push, or hold a pickaxe attack to mine. Survival removal follows configured hardness (default obsidian); bedrock settings require admins. Pushes slide visually between grid cells. Pressure bases supply redstone with matching push blocks above them. Activation blocks toggle with right-click, melee weapons or player projectiles, and also receive redstone input. Nearby transient blocks become solid while activation is enabled; their inactive models show passable 25%-opaque copies of their solid form. Both transient item types always have visible inventory icons.

Ridged edges support six placement directions based on the clicked face, saved across restarts. Blocks can be stacked on registered puzzle blocks; holding a block allows ordinary placement without sneaking. Hourglasses reset the local puzzle with validation for obstructions. Bombs use timed fuses, moving fuse-tip sparks/smoke, audio and explosion particles; they destroy registered broken, subtle_broken and secret_broken walls, while preserving broken_edge. Jukebox and cactus are retired; old loaded entries are released as vanilla jukeboxes or green concrete.

## Validation and limits

44 automated tests pass, covering puzzle state/model definitions, protection, persistence, cardinal movement, six-direction edge transforms and saved orientation, removal settings, sound keys, fuse-tip geometry and transient inventory icons. The bundled ghost models also pass checks for solid-state geometry and alpha 64/255 texture copies. Tests do not replace in-game checks for Nexo interactions, stacking, held attacks, particles, transparency or client rendering. Those behaviors have been iterated with server feedback and should be verified on the target server.
