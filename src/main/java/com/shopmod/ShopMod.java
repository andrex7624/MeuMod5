package com.shopmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ShopMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> Economy.load());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> Economy.save());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                ShopCommands.register(dispatcher));
    }
}
