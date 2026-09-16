package io.arson.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.arson.client.ArsonClient;
import io.arson.client.config.ConfigManager;
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
            .then(ClientCommandManager.literal("help").executes(ctx->{feedback(ctx,"Commands: list, toggle <module>, info <module>, settings <module>, reset <module>, save, profile <save|load> <name>");return 1;}))
            .then(ClientCommandManager.literal("list").executes(ctx->{feedback(ctx,"Arson: "+ArsonClient.getInstance().modules().all().size()+" modules, "+ArsonClient.getInstance().modules().enabledCount()+" enabled.");return 1;}))
            .then(ClientCommandManager.literal("toggle").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module m:ArsonClient.getInstance().modules().all())builder.suggest(m.id());return builder.buildFuture();}).executes(ctx->{Module m=module(ctx);if(m==null)return 0;m.toggle();ArsonClient.getInstance().saveConfig();feedback(ctx,m.name()+": "+(m.enabled()?"enabled":"disabled"));return 1;})))
            .then(ClientCommandManager.literal("info").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module m:ArsonClient.getInstance().modules().all())builder.suggest(m.id());return builder.buildFuture();}).executes(ctx->{Module m=module(ctx);if(m==null)return 0;feedback(ctx,m.name()+" ["+m.category().displayName()+"]");return 1;})))
            .then(ClientCommandManager.literal("settings").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module m:ArsonClient.getInstance().modules().all())builder.suggest(m.id());return builder.buildFuture();}).executes(ctx->{Module m=module(ctx);if(m==null)return 0;StringBuilder out=new StringBuilder(m.name()+": ");for(int i=0;i<m.settings().size();i++){if(i>0)out.append(", ");out.append(m.settings().get(i).id()).append("=").append(m.settings().get(i).get());}if(m.settings().isEmpty())out.append("no settings");feedback(ctx,out.toString());return 1;})))
            .then(ClientCommandManager.literal("reset").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module m:ArsonClient.getInstance().modules().all())builder.suggest(m.id());return builder.buildFuture();}).executes(ctx->{Module m=module(ctx);if(m==null)return 0;m.resetSettings();ArsonClient.getInstance().saveConfig();feedback(ctx,m.name()+" settings reset to defaults.");return 1;})))
            .then(ClientCommandManager.literal("save").executes(ctx->{ArsonClient.getInstance().saveConfig();feedback(ctx,"Arson config saved.");return 1;}))
            .then(ClientCommandManager.literal("profile").then(ClientCommandManager.literal("save").then(ClientCommandManager.argument("name",StringArgumentType.word()).executes(ctx->{String n=StringArgumentType.getString(ctx,"name");boolean ok=ConfigManager.saveProfile(Minecraft.getInstance(),ArsonClient.getInstance().modules(),n);feedback(ctx,ok?"Profile saved: "+n:"Invalid profile name");return ok?1:0;}))).then(ClientCommandManager.literal("load").then(ClientCommandManager.argument("name",StringArgumentType.word()).executes(ctx->{String n=StringArgumentType.getString(ctx,"name");boolean ok=ConfigManager.loadProfile(Minecraft.getInstance(),ArsonClient.getInstance().modules(),n);feedback(ctx,ok?"Profile loaded: "+n:"Profile not found or invalid");return ok?1:0;}))))
        ));
    }
    private static Module module(CommandContext<FabricClientCommandSource> ctx){String id=StringArgumentType.getString(ctx,"module");Module m=ArsonClient.getInstance().modules().get(id);if(m==null)error(ctx,"Unknown module: "+id);return m;}
    private static void feedback(CommandContext<FabricClientCommandSource> ctx,String message){ctx.getSource().sendFeedback(Component.literal(message));NotificationCenter.push("Arson",message);}
    private static void error(CommandContext<FabricClientCommandSource> ctx,String message){ctx.getSource().sendError(Component.literal(message));NotificationCenter.push("Arson",message,1800,NotificationCenter.Priority.HIGH);}
}
