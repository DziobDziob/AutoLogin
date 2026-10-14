package pl.autologin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AutoLoginClient implements ClientModInitializer {
    public static KeyBinding openKey;

    private int pendingTicks = -1;
    private String pendingServerKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("autologin", "main"));
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autologin.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, category));

        // Po wejsciu na serwer zaplanuj wyslanie komendy logowania
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerInfo info = handler.getServerInfo();
            if (info == null) return; // singleplayer
            String key = AutoLoginConfig.keyFor(info.address);
            AutoLoginConfig.Entry e = AutoLoginConfig.get().servers.get(key);
            if (e != null && e.enabled && !e.password.isEmpty()) {
                pendingServerKey = key;
                pendingTicks = Math.max(0, AutoLoginConfig.get().delayTicks);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> pendingTicks = -1);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.player == null || client.currentScreen != null) continue;
                if (client.getCurrentServerEntry() == null) {
                    client.player.sendMessage(Text.translatable("message.autologin.no_server"), true);
                } else {
                    client.setScreen(new AutoLoginScreen());
                }
            }

            if (pendingTicks > 0) {
                pendingTicks--;
            } else if (pendingTicks == 0) {
                pendingTicks = -1;
                sendLogin(client);
            }
        });
    }

    private void sendLogin(MinecraftClient client) {
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null || client.player == null || pendingServerKey == null) return;
        AutoLoginConfig.Entry e = AutoLoginConfig.get().servers.get(pendingServerKey);
        if (e == null || !e.enabled || e.password.isEmpty()) return;

        String cmd = e.command == null || e.command.isBlank() ? "login %s" : e.command.trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        cmd = cmd.replace("%s", e.password);
        handler.sendChatCommand(cmd);
    }
}
