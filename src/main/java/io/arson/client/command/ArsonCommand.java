package io.arson.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
import io.arson.client.module.HudModule;
import io.arson.client.module.Module;
import io.arson.client.notification.NotificationCenter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ArsonCommand {
    private ArsonCommand() {}
    public static void register(){
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,registryAccess)->dispatcher.register(ClientCommandManager.literal("arson")
            .then(ClientCommandManager.literal("help").executes(ctx->{feedback(ctx,"Commands: module <list|toggle|info|settings|reset>, setting <module> <setting> <value>, category <list|info|enable|disable>, reset-category <category>, hud <preset|row-format> <minimal|compact|full>, config <save|load>, profile <list|save|load|delete|duplicate|rename>, clickgui <scale>, plus server and legacy list/toggle/info/settings/reset/save.");return 1;}))
            .then(ClientCommandManager.literal("list").executes(ctx->{feedback(ctx,"Arson: "+ArsonClient.getInstance().modules().all().size()+" modules, "+ArsonClient.getInstance().modules().enabledCount()+" enabled.");return 1;}))
            .then(ClientCommandManager.literal("enabled").executes(ctx->{feedback(ctx,"Enabled: "+ArsonClient.getInstance().modules().all().stream().filter(Module::enabled).map(Module::id).toList());return 1;}))
            .then(ClientCommandManager.literal("favorites").executes(ctx->{feedback(ctx,"Favorites: "+ArsonClient.getInstance().modules().all().stream().filter(Module::favorite).map(Module::id).toList());return 1;}))
            .then(ClientCommandManager.literal("server").executes(ctx->{feedback(ctx,io.arson.client.api.ArsonApi.serverSummary());return 1;}))
            .then(settingCommand()).then(moduleCommands()).then(categoryCommands()).then(summaryCommand()).then(hudCommands()).then(clickGuiCommands()).then(configCommands()).then(profileCommands())
            .then(enableCommand()).then(disableCommand()).then(toggleCommand()).then(infoCommand()).then(settingsCommand()).then(resetCommand())
            .then(ClientCommandManager.literal("save").executes(ctx->{ArsonClient.getInstance().saveConfig();feedback(ctx,"Arson config saved.");return 1;}))
        ));
    }
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> settingCommand() {
        return ClientCommandManager.literal("setting")
            .then(ClientCommandManager.argument("module", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    for (Module module : ArsonClient.getInstance().modules().all()) builder.suggest(module.id());
                    return builder.buildFuture();
                })
                .then(ClientCommandManager.argument("setting", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String moduleId = StringArgumentType.getString(ctx, "module");
                        io.arson.client.api.ArsonApi.module(moduleId).ifPresent(module ->
                            module.settings().forEach(setting -> builder.suggest(setting.id())));
                        return builder.buildFuture();
                    })
                    .then(ClientCommandManager.argument("value", StringArgumentType.greedyString()).executes(ctx -> {
                        String moduleId = StringArgumentType.getString(ctx, "module");
                        String settingId = StringArgumentType.getString(ctx, "setting");
                        String value = StringArgumentType.getString(ctx, "value");
                        boolean applied = io.arson.client.api.ArsonApi.setSetting(moduleId, settingId, value);
                        if (!applied) { error(ctx, "Unknown setting or invalid value"); return 0; }
                        feedback(ctx, moduleId + "." + settingId + " = " + value);
                        return 1;
                    }))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> resetCategoryCommand() {
        return ClientCommandManager.literal("reset-category").then(categoryArgument().executes(ctx -> {
            Module.Category category = category(StringArgumentType.getString(ctx, "category"));
            if (category == null) { error(ctx, "Unknown category"); return 0; }
            int changed = io.arson.client.api.ArsonApi.resetCategory(category);
            feedback(ctx, "Reset " + changed + " module(s) in " + category.displayName());
            return 1;
        }));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> summaryCommand() {
        return ClientCommandManager.literal("summary").then(ClientCommandManager.argument("category",StringArgumentType.word()).suggests((ctx,b)->{for(Module.Category cat:Module.Category.values()) b.suggest(cat.name().toLowerCase()); return b.buildFuture();}).executes(ctx->{Module.Category cat=category(StringArgumentType.getString(ctx,"category")); if(cat==null){error(ctx,"Unknown category");return 0;} feedback(ctx,io.arson.client.api.ArsonApi.moduleSummary(cat)); return 1;}));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> moduleCommands(){return ClientCommandManager.literal("module")
        .then(ClientCommandManager.literal("search").then(ClientCommandManager.argument("query",StringArgumentType.greedyString()).executes(ctx->{String q=StringArgumentType.getString(ctx,"query");var found=io.arson.client.api.ArsonApi.search(q);feedback(ctx,"Search "+q+": "+found.stream().map(Module::id).toList());return 1;}))).then(ClientCommandManager.literal("list").then(ClientCommandManager.argument("category",StringArgumentType.word()).suggests((ctx,b)->{for(Module.Category c:Module.Category.values())b.suggest(c.name().toLowerCase());return b.buildFuture();}).executes(ctx->{String raw=StringArgumentType.getString(ctx,"category");Module.Category c=category(raw);if(c==null){error(ctx,"Unknown category: "+raw);return 0;}feedback(ctx,raw+": "+ArsonClient.getInstance().modules().organized(c).stream().map(Module::id).toList());return 1;})).executes(ctx->{feedback(ctx,"Modules: "+ArsonClient.getInstance().modules().all().stream().map(Module::id).toList());return 1;}))
        .then(toggleCommand()).then(infoCommand()).then(settingsCommand()).then(resetCommand());}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> categoryCommands(){return ClientCommandManager.literal("category")
        .then(ClientCommandManager.literal("list").executes(ctx->{StringBuilder out=new StringBuilder();for(Module.Category c:Module.Category.values()){if(out.length()>0)out.append(" | ");out.append(c.name().toLowerCase()).append("=").append(ArsonClient.getInstance().modules().categoryCount(c)).append("/").append(ArsonClient.getInstance().modules().enabledCount(c));}feedback(ctx,out.toString());return 1;}))
        .then(ClientCommandManager.literal("info").then(categoryArgument().executes(ctx->{Module.Category c=category(StringArgumentType.getString(ctx,"category"));if(c==null){error(ctx,"Unknown category");return 0;}feedback(ctx,c.displayName()+": "+ArsonClient.getInstance().modules().categoryCount(c)+" modules, "+ArsonClient.getInstance().modules().enabledCount(c)+" enabled.");return 1;})))
        .then(ClientCommandManager.literal("enable").then(categoryArgument().executes(ctx->{Module.Category c=category(StringArgumentType.getString(ctx,"category"));if(c==null){error(ctx,"Unknown category");return 0;}int changed=io.arson.client.api.ArsonApi.setEnabled(c,true);feedback(ctx,c.displayName()+": enabled "+changed+" module(s)");return 1;})))
        .then(ClientCommandManager.literal("disable").then(categoryArgument().executes(ctx->{Module.Category c=category(StringArgumentType.getString(ctx,"category"));if(c==null){error(ctx,"Unknown category");return 0;}int changed=io.arson.client.api.ArsonApi.setEnabled(c,false);feedback(ctx,c.displayName()+": disabled "+changed+" module(s)");return 1;})));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> hudCommands(){return ClientCommandManager.literal("hud").then(ClientCommandManager.literal("row-format").then(ClientCommandManager.argument("format",StringArgumentType.word()).suggests((ctx,b)->{for(HudModule.RowFormat f:HudModule.RowFormat.values())b.suggest(f.name().toLowerCase());return b.buildFuture();}).executes(ctx->{try{HudModule.RowFormat f=HudModule.RowFormat.valueOf(StringArgumentType.getString(ctx,"format").toUpperCase(java.util.Locale.ROOT));if(!io.arson.client.api.ArsonApi.setHudRowFormat(f)){error(ctx,"HUD unavailable");return 0;}feedback(ctx,"HUD row format: "+f.name().toLowerCase());return 1;}catch(IllegalArgumentException e){error(ctx,"Unknown row format");return 0;}}))).then(ClientCommandManager.literal("preset").then(ClientCommandManager.argument("name",StringArgumentType.word()).suggests((ctx,b)->{b.suggest("minimal");b.suggest("compact");b.suggest("full");return b.buildFuture();}).executes(ctx->{String name=StringArgumentType.getString(ctx,"name");Module module=ArsonClient.getInstance().modules().get("hud");if(!(module instanceof HudModule hud)){error(ctx,"HUD module unavailable");return 0;}try{hud.applyPreset(name);ArsonClient.getInstance().saveConfig();feedback(ctx,"HUD preset applied: "+name);return 1;}catch(IllegalArgumentException e){error(ctx,e.getMessage());return 0;}})));}

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> clickGuiCommands(){return ClientCommandManager.literal("clickgui").then(ClientCommandManager.literal("scale").then(ClientCommandManager.argument("value",com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(0.75,1.25)).executes(ctx->{double value=com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx,"value");boolean ok=io.arson.client.api.ArsonApi.setClickGuiPanelScale(value);if(!ok){error(ctx,"Invalid ClickGUI scale");return 0;}feedback(ctx,String.format(java.util.Locale.ROOT,"ClickGUI panel scale: %.2f",value));return 1;})));}

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> configCommands(){return ClientCommandManager.literal("config").then(ClientCommandManager.literal("save").executes(ctx->{ArsonClient.getInstance().saveConfig();feedback(ctx,"Arson config saved.");return 1;})).then(ClientCommandManager.literal("load").executes(ctx->{ConfigManager.load(Minecraft.getInstance(),ArsonClient.getInstance().modules());feedback(ctx,"Arson config loaded.");return 1;}));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> profileCommands() {
        return ClientCommandManager.literal("profile")
            .then(ClientCommandManager.literal("list").executes(ctx -> {
                var profiles = io.arson.client.api.ArsonApi.profiles();
                feedback(ctx, profiles.isEmpty() ? "No saved profiles." : "Profiles: " + profiles);
                return 1;
            }))
            .then(ClientCommandManager.literal("save").then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");
                boolean ok = ConfigManager.saveProfile(Minecraft.getInstance(), ArsonClient.getInstance().modules(), name);
                feedback(ctx, ok ? "Profile saved: " + name : "Invalid profile name");
                return ok ? 1 : 0;
            })))
            .then(ClientCommandManager.literal("load").then(profileNameArgument("name").executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");
                boolean ok = ConfigManager.loadProfile(Minecraft.getInstance(), ArsonClient.getInstance().modules(), name);
                feedback(ctx, ok ? "Profile loaded: " + name : "Profile not found or invalid");
                return ok ? 1 : 0;
            })))
            .then(ClientCommandManager.literal("delete").then(profileNameArgument("name").executes(ctx -> {
                String name = StringArgumentType.getString(ctx, "name");
                boolean ok = ConfigManager.deleteProfile(Minecraft.getInstance(), name);
                feedback(ctx, ok ? "Profile deleted: " + name : "Profile not found or invalid");
                return ok ? 1 : 0;
            })))
            .then(ClientCommandManager.literal("duplicate").then(profileNameArgument("source")
                .then(ClientCommandManager.argument("target", StringArgumentType.word()).executes(ctx -> {
                    String source = StringArgumentType.getString(ctx, "source");
                    String target = StringArgumentType.getString(ctx, "target");
                    boolean ok = io.arson.client.api.ArsonApi.duplicateProfile(source, target);
                    feedback(ctx, ok ? "Profile duplicated: " + source + " -> " + target : "Could not duplicate profile");
                    return ok ? 1 : 0;
                }))))
            .then(ClientCommandManager.literal("rename").then(profileNameArgument("source")
                .then(ClientCommandManager.argument("target", StringArgumentType.word()).executes(ctx -> {
                    String source = StringArgumentType.getString(ctx, "source");
                    String target = StringArgumentType.getString(ctx, "target");
                    boolean ok = io.arson.client.api.ArsonApi.renameProfile(source, target);
                    feedback(ctx, ok ? "Profile renamed: " + source + " -> " + target : "Could not rename profile");
                    return ok ? 1 : 0;
                }))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource, String> profileNameArgument(String name) {
        return ClientCommandManager.argument(name, StringArgumentType.word())
            .suggests((ctx, builder) -> {
                for (String profile : io.arson.client.api.ArsonApi.profiles()) builder.suggest(profile);
                return builder.buildFuture();
            });
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource,String> categoryArgument(){return ClientCommandManager.argument("category",StringArgumentType.word()).suggests((ctx,b)->{for(Module.Category c:Module.Category.values())b.suggest(c.name().toLowerCase());return b.buildFuture();});}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> enableCommand(){return ClientCommandManager.literal("enable").then(moduleArgument().executes(ctx->{Module m=module(ctx);if(m==null)return 0;m.setEnabled(true);ArsonClient.getInstance().saveConfig();feedback(ctx,m.name()+": enabled");return 1;}));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> disableCommand(){return ClientCommandManager.literal("disable").then(moduleArgument().executes(ctx->{Module m=module(ctx);if(m==null)return 0;m.setEnabled(false);ArsonClient.getInstance().saveConfig();feedback(ctx,m.name()+": disabled");return 1;}));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> toggleCommand(){return ClientCommandManager.literal("toggle").then(moduleArgument().executes(ctx->{Module m=module(ctx);if(m==null)return 0;m.toggle();ArsonClient.getInstance().saveConfig();feedback(ctx,m.name()+": "+(m.enabled()?"enabled":"disabled"));return 1;}));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> infoCommand(){return ClientCommandManager.literal("info").then(moduleArgument().executes(ctx->{Module m=module(ctx);if(m==null)return 0;feedback(ctx,m.help());return 1;}));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> settingsCommand(){return ClientCommandManager.literal("settings").then(moduleArgument().executes(ctx->{Module m=module(ctx);if(m==null)return 0;StringBuilder out=new StringBuilder(m.name()+": ");for(int i=0;i<m.settings().size();i++){if(i>0)out.append(", ");var s=m.settings().get(i);out.append(s.id()).append("=").append(s.get());if(!s.description().isBlank())out.append(" [").append(s.description()).append("]");}if(m.settings().isEmpty())out.append("no settings");feedback(ctx,out.toString());return 1;}));}
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> resetCommand(){return ClientCommandManager.literal("reset").then(moduleArgument().executes(ctx->{Module m=module(ctx);if(m==null)return 0;m.resetSettings();ArsonClient.getInstance().saveConfig();feedback(ctx,m.name()+" settings reset to defaults.");return 1;}));}
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<FabricClientCommandSource,String> moduleArgument(){return ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,b)->{for(Module m:ArsonClient.getInstance().modules().all())b.suggest(m.id());return b.buildFuture();});}
    private static Module module(CommandContext<FabricClientCommandSource> ctx){String id=StringArgumentType.getString(ctx,"module");Module m=ArsonClient.getInstance().modules().get(id);if(m==null)error(ctx,"Unknown module: "+id);return m;}
    private static Module.Category category(String raw){for(Module.Category c:Module.Category.values())if(c.name().equalsIgnoreCase(raw)||c.displayName().equalsIgnoreCase(raw))return c;return null;}
    private static void feedback(CommandContext<FabricClientCommandSource> ctx,String message){ctx.getSource().sendFeedback(Component.literal(message));NotificationCenter.push("Arson",message);}
    private static void error(CommandContext<FabricClientCommandSource> ctx,String message){ctx.getSource().sendError(Component.literal(message));NotificationCenter.push("Arson",message,1800,NotificationCenter.Priority.HIGH);}
}
