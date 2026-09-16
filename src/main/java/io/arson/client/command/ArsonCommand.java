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
            .then(ClientCommandManager.literal("help").executes(ctx->{String message="Arson commands: list, toggle <module>, settings <module>, save, profile <save|load> <name>, reset <module>";feedback(ctx,message);return 1;}))
            .then(ClientCommandManager.literal("list").executes(ctx->{int enabled=ArsonClient.getInstance().modules().enabledCount();String message="Arson: "+ArsonClient.getInstance().modules().all().size()+" modules, "+enabled+" enabled.";feedback(ctx,message);return enabled;}))
            .then(ClientCommandManager.literal("toggle").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module module:ArsonClient.getInstance().modules().all())builder.suggest(module.id());return builder.buildFuture();}).executes(ctx->{String id=StringArgumentType.getString(ctx,"module");Module module=ArsonClient.getInstance().modules().get(id);if(module==null){error(ctx,"Unknown module: "+id);return 0;}module.toggle();String message=module.name()+": "+(module.enabled()?"enabled":"disabled");feedback(ctx,message);return module.enabled()?1:0;})))
            .then(ClientCommandManager.literal("settings").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module module:ArsonClient.getInstance().modules().all())builder.suggest(module.id());return builder.buildFuture();}).executes(ctx->{Module module=ArsonClient.getInstance().modules().get(StringArgumentType.getString(ctx,"module"));if(module==null){error(ctx,"Unknown module");return 0;}StringBuilder message=new StringBuilder(module.name()+": ");if(module.settings().isEmpty())message.append("no settings");else for(int i=0;i<module.settings().size();i++){if(i>0)message.append(", ");var setting=module.settings().get(i);message.append(setting.id()).append("=").append(setting.get());}feedback(ctx,message.toString());return 1;})))
            .then(ClientCommandManager.literal("reset").then(ClientCommandManager.argument("module",StringArgumentType.word()).suggests((ctx,builder)->{for(Module module:ArsonClient.getInstance().modules().all())builder.suggest(module.id());return builder.buildFuture();}).executes(ctx->{Module module=ArsonClient.getInstance().modules().get(StringArgumentType.getString(ctx,"module"));if(module==null){error(ctx,"Unknown module");return 0;}module.resetSettings();ArsonClient.getInstance().saveConfig();feedback(ctx,module.name()+" settings reset to defaults.");return 1;})))
            .then(ClientCommandManager.literal("save").executes(ctx->{ArsonClient.getInstance().saveConfig();feedback(ctx,"Arson config saved.");return 1;}))
            .then(ClientCommandManager.literal("profile")
                .then(ClientCommandManager.literal("save").then(ClientCommandManager.argument("name",StringArgumentType.word()).executes(ctx->{String name=StringArgumentType.getString(ctx,"name");boolean ok=ConfigManager.saveProfile(Minecraft.getInstance(),ArsonClient.getInstance().modules(),name);feedback(ctx,ok?"Profile saved: "+name:"Invalid profile name");return ok?1:0;})))
                .then(ClientCommandManager.literal("load").then(ClientCommandManager.argument("name",StringArgumentType.word()).executes(ctx->{String name=StringArgumentType.getString(ctx,"name");boolean ok=ConfigManager.loadProfile(Minecraft.getInstance(),ArsonClient.getInstance().modules(),name);feedback(ctx,ok?"Profile loaded: "+name:"Profile not found or invalid");return ok?1:0;})))
            )
        ));
    }
    private static void feedback(CommandContext<FabricClientCommandSource> ctx,String message){ctx.getSource().sendFeedback(Component.literal(message));NotificationCenter.push("Arson",message);}
    private static void error(CommandContext<FabricClientCommandSource> ctx,String message){ctx.getSource().sendError(Component.literal(message));NotificationCenter.push("Arson",message);}
}
