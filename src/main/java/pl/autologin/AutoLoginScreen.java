package pl.autologin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class AutoLoginScreen extends Screen {
    private final String serverName;
    private final String serverKey;
    private String savedPassword = "";
    private String savedCommand = "login %s";
    private boolean enabled = true;
    private boolean showPassword = false;

    private TextFieldWidget passwordField;
    private TextFieldWidget commandField;
    private ButtonWidget showButton;
    private ButtonWidget enabledButton;

    public AutoLoginScreen() {
        super(Text.translatable("screen.autologin.title"));
        ServerInfo info = MinecraftClient.getInstance().getCurrentServerEntry();
        this.serverName = info != null ? info.address : "?";
        this.serverKey = AutoLoginConfig.keyFor(info != null ? info.address : null);
        AutoLoginConfig.Entry e = serverKey == null ? null : AutoLoginConfig.get().servers.get(serverKey);
        if (e != null) {
            savedPassword = e.password;
            savedCommand = e.command;
            enabled = e.enabled;
        }
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int w = 200;
        int left = cx - w / 2;
        int top = this.height / 2 - 70;

        passwordField = new TextFieldWidget(this.textRenderer, left, top + 24, w - 64, 20,
                Text.translatable("screen.autologin.password"));
        passwordField.setMaxLength(128);
        passwordField.setText(savedPassword);
        applyMask();
        addDrawableChild(passwordField);

        showButton = ButtonWidget.builder(Text.translatable("screen.autologin.show"), b -> {
            showPassword = !showPassword;
            applyMask();
        }).dimensions(left + w - 60, top + 24, 60, 20).build();
        addDrawableChild(showButton);

        commandField = new TextFieldWidget(this.textRenderer, left, top + 64, w, 20,
                Text.translatable("screen.autologin.command"));
        commandField.setMaxLength(128);
        commandField.setText(savedCommand);
        addDrawableChild(commandField);

        enabledButton = ButtonWidget.builder(enabledText(), b -> {
            enabled = !enabled;
            b.setMessage(enabledText());
        }).dimensions(left, top + 92, w, 20).build();
        addDrawableChild(enabledButton);

        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.autologin.save"), b -> save())
                .dimensions(left, top + 120, w / 2 - 2, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.autologin.delete"), b -> delete())
                .dimensions(left + w / 2 + 2, top + 120, w / 2 - 2, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.autologin.close"), b -> close())
                .dimensions(left, top + 146, w, 20).build());

        setInitialFocus(passwordField);
    }

    private Text enabledText() {
        return Text.translatable(enabled ? "screen.autologin.enabled_on" : "screen.autologin.enabled_off");
    }

    private void applyMask() {
        passwordField.setRenderTextProvider((s, i) ->
                OrderedText.styledForwardsVisitedString(showPassword ? s : "*".repeat(s.length()), Style.EMPTY));
        if (showButton != null) {
            showButton.setMessage(Text.translatable(showPassword ? "screen.autologin.hide" : "screen.autologin.show"));
        }
    }

    private void save() {
        if (serverKey == null) return;
        AutoLoginConfig cfg = AutoLoginConfig.get();
        AutoLoginConfig.Entry e = cfg.servers.computeIfAbsent(serverKey, k -> new AutoLoginConfig.Entry());
        e.password = passwordField.getText();
        String cmd = commandField.getText().trim();
        e.command = cmd.isEmpty() ? "login %s" : cmd;
        e.enabled = enabled;
        cfg.save();
        notifyPlayer("message.autologin.saved");
        close();
    }

    private void delete() {
        if (serverKey == null) return;
        AutoLoginConfig cfg = AutoLoginConfig.get();
        cfg.servers.remove(serverKey);
        cfg.save();
        notifyPlayer("message.autologin.deleted");
        close();
    }

    private void notifyPlayer(String key) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.translatable(key), true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int left = cx - 100;
        int top = this.height / 2 - 70;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, top - 28, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("screen.autologin.server", serverName), cx, top - 14, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("screen.autologin.password"), left, top + 12, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("screen.autologin.command"), left, top + 52, 0xFFFFFFFF);
    }
}
