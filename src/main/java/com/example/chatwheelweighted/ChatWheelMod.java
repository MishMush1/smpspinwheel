
package com.example.chatwheelweighted;

import net.fabricmc.api.ClientModInitializer;

public class ChatWheelMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ChatListener.register();
    }
}
