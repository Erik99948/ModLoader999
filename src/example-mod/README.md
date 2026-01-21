# Example Mod for ModLoader999

This is an example mod demonstrating how to create mods for ModLoader999.

## Building

### Step 1: Build ModLoader999 First
```bash
cd ..  # Go to ModLoader999 root
mvn clean install
```

### Step 2: Build This Mod
```bash
cd src/example-mod
mvn clean package
```

### Step 3: Install
Copy `target/ExampleMod-1.0.0.modloader999` to:
```
your-server/plugins/ModLoader999/Mods/
```

## Features

- **Custom Items**: Magic Wand, Lucky Diamond
- **Commands**: `/examplemod help|give|info|broadcast`
- **Events**: Welcome message on player join

## Commands

| Command | Description |
|---------|-------------|
| `/examplemod help` | Show help |
| `/examplemod give <item>` | Give item (magic_wand, lucky_diamond) |
| `/examplemod info` | Show mod info |
| `/examplemod broadcast <msg>` | Broadcast message |
