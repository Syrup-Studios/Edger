# Edger 0.1.0

- Edger is a Fabric server mod for Minecraft 1.20.1 and a fork of Ledger by Potatoboy9999, QuiltServerTools, and Ledger contributors.
- Uses the `net.syrupstudios.edger` package and `edger` mod ID. Existing Ledger client integrations and add-ons must update for Edger's package and network namespace.
- Added a time index for searches with `before:` and `after:`. To add it to an existing database, set `updateSchema = true` for one startup. Set it back to `false` after the index is created.
- The config file remains `config/ledger.toml`, and the database remains `ledger.sqlite`.
