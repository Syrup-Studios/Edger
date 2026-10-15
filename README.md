[![upstream Ledger Discord](https://img.shields.io/discord/764543203772334100?label=upstream%20Ledger%20Discord)](https://discord.gg/UxHnDWr)

For the original Ledger guide, visit the [upstream wiki](https://quiltservertools.github.io/Ledger/latest/).

## Edger

A world change logging tool for the Fabric modloader.

The [upstream Ledger community](https://discord.gg/GtwDTTr3pe) is on Discord.

### Build

Use the Java version set by `java.version` in `stonecutter.properties.yaml` to assemble the Stonecutter Fabric target with `./gradlew :1.20.1-fabric:assemble`. Set the Minecraft target with `minecraft.version`. Find the jar in `versions/1.20.1-fabric/build/libs`. Mod metadata, plugin versions, and dependency versions are also in that file.

### Install

Put Edger in your mods folder along with Fabric API and fabric-language-kotlin. When you launch your server, the config file will be created automatically.

### Configuration

Edger's configuration file is found at `config/ledger.toml`.

#### Message theme

Found under `[color]`

Edger allows for the customisation of the colors used in messages sent in game. By default, Edger uses the blue theme. More themes can be found in the [themes](./themes.md) file.

#### Search settings

Found under `[search]`

`pageSize` [Default: 8] controls the number of actions displayed per page

#### Database settings

Found under `[database]`

`maxQueueSize` [Default: 50] is the number of items logged before writing to the database

`queueTimeoutSec` [Default: 5] is the maximum amount of time to wait for the queue to fill before writing

`updateSchema` [Default: false] adds the missing time index to existing databases at startup. This can be slow on large databases. Set it to `true` for one restart, then set it back to `false` after the index is created.

#### Filters

These allow you to control what is and is not logged

Found under `[actions]`

All listed here are arrays and are formatted like so:
```
array = []
blocks = ["minecraft:air", "minecraft:dirt"]
```

`typeBlacklist` [Default: empty] controls what action types are logged. Hover over the action in a chat message to see the type

`worldBlacklist` [Default: empty] controls in which dimensions events are logged. Provide

`objectBlacklist` [Default: empty] controls which objects are logged. These can be item types, block types or entities

`sourceBlacklist` [Default: empty] controls which sources are logged. Examples are `"lava"` and `"gravity"`

### Commands

#### Inspect

`/lg inspect` - toggles inspect mode
`/lg inspect [on|off]` - enables or disables inspect mode
`/lg inspect <pos>` - inspects the block at a given position

### Search

`/lg search <args>` - searches with the given arguments

### Rollback

`/lg rollback <args>` - rollbacks with filters specified

### Page

`/lg page <index>`

When viewing results, you can use this command to quickly skip to a certain page.

### Permissions

All Edger commands support the LuckPerms API.

The permission nodes are formatted as: `ledger.<command>`

All Edger commands have fallback on permission level 3 if you do not have a permissions mod installed.

### Contribute

Edger is written in Kotlin. Write contributions in Kotlin where possible. Mixins must be written in Java.

### Fork and license

Edger is a modified fork of Ledger by Potatoboy9999, QuiltServerTools, and its contributors. Syrup Studios modified it on 2026-10-15 to use the Edger name and the `net.syrupstudios.edger` package. Edger uses the `edger` mod ID. See [NOTICE](NOTICE) for attribution and modification details, and [LICENSE.md](LICENSE.md) for the LGPL-3.0 license.
