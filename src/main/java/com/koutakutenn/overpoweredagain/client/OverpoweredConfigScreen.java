package com.koutakutenn.overpoweredagain.client;

import com.koutakutenn.overpoweredagain.OverpoweredAgain;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.LoggerFactory;

public final class OverpoweredConfigScreen extends Screen {
    private final Screen parent;
    private boolean recipe = OverpoweredAgain.CONFIG.legacyRecipe;
    private boolean texture = OverpoweredAgain.CONFIG.legacyTexture;

    public OverpoweredConfigScreen(Screen parent) {
        super(text("title"));
        this.parent = parent;
    }

    private static Component text(String key, Object... args) {
        return Component.translatable("overpowered_again.config." + key, args);
    }

    @Override
    protected void init() {
        int x = width / 2 - 155;
        int y = Math.max(40, height / 3);
        addRenderableWidget(new StringWidget(x, y - 28, 310, 20, title, font));
        Button recipeButton = addRenderableWidget(Button.builder(recipeLabel(), button -> {
            recipe = !recipe;
            button.setMessage(recipeLabel());
        }).bounds(x, y, 310, 20).build());
        // Multiplayer recipes belong to the server. A client cannot change its policy.
        recipeButton.active = minecraft.getConnection() == null || minecraft.hasSingleplayerServer();
        addRenderableWidget(Button.builder(textureLabel(), button -> {
            texture = !texture;
            button.setMessage(textureLabel());
        }).bounds(x, y + 28, 310, 20).build());
        addRenderableWidget(new StringWidget(x, y + 54, 310, 20, text("serverNote"), font));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, height - 34, 310, 20).build());
    }

    private Component recipeLabel() {
        return text("recipe", Component.translatable(recipe ? "options.on" : "options.off"));
    }

    private Component textureLabel() {
        return text("texture", text(texture ? "legacy" : "modern"));
    }

    @Override
    public void onClose() {
        boolean changed = recipe != OverpoweredAgain.CONFIG.legacyRecipe;
        boolean oldRecipe = OverpoweredAgain.CONFIG.legacyRecipe;
        boolean oldTexture = OverpoweredAgain.CONFIG.legacyTexture;
        OverpoweredAgain.CONFIG.legacyRecipe = recipe;
        OverpoweredAgain.CONFIG.legacyTexture = texture;
        try {
            OverpoweredAgain.CONFIG.save();
        } catch (IOException exception) {
            OverpoweredAgain.CONFIG.legacyRecipe = oldRecipe;
            OverpoweredAgain.CONFIG.legacyTexture = oldTexture;
            LoggerFactory.getLogger("overpowered_again").error("Cannot save config", exception);
            addRenderableWidget(new StringWidget(0, height - 62, width, 20, text("saveError"), font));
            return;
        }
        var server = minecraft.getSingleplayerServer();
        if (changed && server != null) {
            server.execute(() -> server.reloadResources(List.copyOf(server.getPackRepository().getSelectedIds()))
                    .exceptionally(exception -> {
                        LoggerFactory.getLogger("overpowered_again").error("Cannot reload recipes", exception);
                        minecraft.execute(() -> {
                            minecraft.gui.chatListener().handleSystemMessage(text("reloadError"), false);
                        });
                        return null;
                    }));
        }
        minecraft.gui.setScreen(parent);
    }
}
