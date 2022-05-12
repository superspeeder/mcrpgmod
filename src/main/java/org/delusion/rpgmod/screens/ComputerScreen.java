package org.delusion.rpgmod.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.delusion.rpgmod.RPGModClient;
import org.delusion.rpgmod.character.CharacterStatType;
import org.delusion.rpgmod.character.CharacterStats;

import java.util.ArrayList;
import java.util.List;

public class ComputerScreen extends Screen {
    public static final Identifier CLIENT_CLICKED_PLAYERSTATS_BUTTON_PACKET_ID = new Identifier("rpgmod", "psbtn_click");
    private static final Identifier TEXTURE = new Identifier("rpgmod", "textures/gui/panel.png");
    private static final Identifier STATSPANEL_TEXTURE = new Identifier("rpgmod", "textures/gui/statspanel.png");
    private static final CharacterStatType CLICKSTAT = new CharacterStatType(0, new Identifier("rpgmod","computer.buttonclick"));
    private TexturedButtonWidget playerStatsButton;
    protected static int backgroundWidth = 176;
    protected static int backgroundHeight = 166;
    protected int x = 0;
    protected int y = 0;
    private Panel currentPanel = Panel.Main;
    private Panel lastPanel = null;
    private TexturedButtonWidget backButton;

    public enum Panel {
        Main(TEXTURE),
        Stats(STATSPANEL_TEXTURE);

        private Identifier background;
        private List<ClickableWidget> clickables = new ArrayList<>();
        private List<Drawable> drawables = new ArrayList<>();

        Panel(Identifier background) {
            this.background = background;
        }

        public Identifier getBackground() {
            return background;
        }

        public void attach(Drawable widget) {
            if (widget instanceof ClickableWidget) {
                clickables.add((ClickableWidget) widget);
            }
            drawables.add(widget);
        }

        public void draw(MatrixStack matrices, int mouseX, int mouseY, float delta, ComputerScreen screen) {
            drawables.forEach(drawable -> {
                drawable.render(matrices, mouseX, mouseY, delta);
            });
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            for (ClickableWidget clickable : clickables) {
                if (clickable.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }

        public void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, ComputerScreen screen) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, background);
            screen.drawTexture(matrices, screen.x, screen.y, 0, 0, backgroundWidth, backgroundHeight);

        }

        public void mouseMoved(double mouseX, double mouseY) {
            for (ClickableWidget clickable : clickables) {
                clickable.mouseMoved(mouseX, mouseY);
            }
        }

        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (ClickableWidget clickable : clickables) {
                if (clickable.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return false;
        }
    }

    public ComputerScreen(Text title) {
        super(title);
        for (Panel panel : Panel.values()) {
            panel.drawables.clear();
            panel.clickables.clear();
        }

        playerStatsButton = new TexturedButtonWidget(15 + x, 15 + y, 18, 18, 0, 0, 18,
                new Identifier("rpgmod", "textures/gui/button_square_16.png"), 18, 36,
                this::onPlayerStatsButtonClick,
                (button, matrices, mouseX, mouseY) -> renderTooltip(matrices,
                        new TranslatableText("tooltip.rpgmod.gui.computer.button.stats"), mouseX, mouseY),
                new TranslatableText("tooltip.rpgmod.gui.computer.button.stats"));
        playerStatsButton.visible = true;
        Panel.Main.attach(playerStatsButton);

        backButton = new TexturedButtonWidget(15 + x, 15 + y, 18, 10, 0, backgroundHeight, 10,
                STATSPANEL_TEXTURE, 256, 256,
                this::onBackButtonClick,
                new TranslatableText("tooltip.rpgmod.gui.back"));
        Panel.Stats.attach(backButton);



//        addSelectableChild(playerStatsButton);

    }

    private void onBackButtonClick(ButtonWidget buttonWidget) {
        if (lastPanel == null) return;
        currentPanel = lastPanel;
    }

    private void onPlayerStatsButtonClick(ButtonWidget buttonWidget) {
        CharacterStats stats = RPGModClient.getCharacterStats();
        MinecraftClient.getInstance().player.sendMessage(Text.of("Button Clicked " + (stats.getStatistic(CLICKSTAT) + 1) + " times"), true);
        ClientPlayNetworking.send(CLIENT_CLICKED_PLAYERSTATS_BUTTON_PACKET_ID, PacketByteBufs.empty());
        currentPanel = Panel.Stats;
        lastPanel = Panel.Main;

    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        currentPanel.drawBackground(matrices, mouseX, mouseY, delta, this);
        super.render(matrices, mouseX, mouseY, delta);

        currentPanel.draw(matrices, mouseX, mouseY, delta, this);

        drawForeground(matrices, mouseX, mouseY, delta);
    }

    Rect2i r = new Rect2i(8, 34, 16, 16);
    private void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        switch (currentPanel) {
            case Main -> {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                itemRenderer.renderGuiItemIcon(RPGModClient.getItemStackForCurrentPlayerHead(), playerStatsButton.x + 1, playerStatsButton.y + 1);

                if (playerStatsButton.isHovered()) {
                    playerStatsButton.renderTooltip(matrices, mouseX, mouseY);
                }
            }

            case Stats -> {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                renderStats(matrices, mouseX, mouseY);
//                RenderSystem.setShaderTexture(0, new Identifier(, "textures/gui/computer.buttonclick.png"));
//                drawTexture(matrices, 8 + x, 34 + y, 0, 0, 16, 16);
            }

        }
    }

    private void renderStats(MatrixStack matrices, int mouseX, int mouseY) {
        int id = 0;

        Runnable postRun = () -> {};

        for (CharacterStatType stype : CharacterStatType.getAll()) {
            int x = 8 + this.x + (id % 9) * 18;
            int y = 34 + this.y + Math.floorDiv(id, 9) * 18;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new Identifier(stype.getInternalName().getNamespace(),
                    "textures/gui/stats/" + stype.getInternalName().getPath() + ".png"));

            drawTexture(matrices, x, y, 0, 0, 16, 16, 16, 16);

            int mrx = mouseX - x;
            int mry = mouseY - y;

            if (mrx >= 0 && mrx < 18 && mry >= 0 && mry < 18) {
                List<Text> text = List.of(stype.getDisplayName().copy().formatted(Formatting.GOLD, Formatting.BOLD).append(Text.of(": ")).append(
                        Text.of(String.valueOf(RPGModClient.getCharacterStats().getStatistic(stype))).copy()
                                .setStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.WHITE)).withItalic(true).withBold(false)
                                )));
                postRun = () -> renderTooltip(matrices, text, mouseX, mouseY);
            }
            id++;
        }

        postRun.run();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return currentPanel.mouseClicked(mouseX, mouseY, button) ||
                super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return currentPanel.mouseReleased(mouseX, mouseY, button) ||
                super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        currentPanel.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - backgroundWidth) / 2;
        this.y = (this.height - backgroundHeight) / 2;
        for (Panel p : Panel.values()) {
            p.clickables.forEach(clickable -> {
                clickable.x += x;
                clickable.y += y;
            });
        }
    }

    public static void onClientClickPSButton(MinecraftServer minecraftServer,
                                             ServerPlayerEntity serverPlayerEntity,
                                             ServerPlayNetworkHandler serverPlayNetworkHandler,
                                             PacketByteBuf packetByteBuf,
                                             PacketSender packetSender) {

        CharacterStats stats = CharacterStats.getStatsFor(serverPlayerEntity.getUuid());
        stats.setStatistic(CLICKSTAT, stats.getStatistic(CLICKSTAT) + 1);
        CharacterStats.onRequest(minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender);
    }
}
