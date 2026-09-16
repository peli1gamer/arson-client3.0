package io.arson.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.arson.client.ArsonClient;
import io.arson.client.module.Module;
import io.arson.client.notification.NotificationCenter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.network.chat.Component;

/** Small local command surface for module discovery and state changes. */
public final class ArsonCommand {
    private ArsonCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("arson")
                .then(ClientCommandManager.literal("list")
                    .executes(ctx -> {
                        int enabled = ArsonClient.getInstance().modules().enabledCount();
                        String message = "Arson: " + ArsonClient.getInstance().modules().all().size() + " modules, " + enabled + " enabled.";
                        ctx.getSource().sendFeedback(Component.literal(message));
                        NotificationCenter.push("Arson", message);
                        return enabled;
                    }))
                .then(ClientCommandManager.literal("toggle")
                    .then(ClientCommandManager.argument("module", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (Module module : ArsonClient.getInstance().modules().all()) builder.suggest(module.id());
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String id = StringArgumentType.getString(ctx, "module");
                            Module module = ArsonClient.getInstance().modules().get(id);
                            if (module == null) {
                                String message = "Unknown module: " + id;
                                ctx.getSource().sendError(Component.literal(message));
                                NotificationCenter.push("Arson", message);
                                return 0;
                            }
                            module.toggle();
                            String message = module.name() + ": " + (module.enabled() ? "enabled" : "disabled");
                            ctx.getSource().sendFeedback(Component.literal(message));
                            NotificationCenter.push(module.name(), module.enabled() ? "Enabled" : "Disabled");
                            return module.enabled() ? 1 : 0;
                        })))
                .then(ClientCommandManager.literal("save")
                    .executes(ctx -> {
                        ArsonClient.getInstance().saveConfig();
                        String message = "Arson config saved.";
                        ctx.getSource().sendFeedback(Component.literal(message));
                        NotificationCenter.push("Arson", message);
                        return 1;
                    }))
        ));
    }
}
