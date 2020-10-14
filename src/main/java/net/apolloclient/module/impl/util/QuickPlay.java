/*⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤
 Copyright (C) 2020-2021 developed by Icovid and Apollo Development Team.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published
 by the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see https://www.gnu.org/licenses/.

 Contact: Icovid#3888 @ https://discord.com
 ⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤*/

package net.apolloclient.module.impl.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.apolloclient.Apollo;
import net.apolloclient.event.Priority;
import net.apolloclient.event.bus.SubscribeEvent;
import net.apolloclient.event.impl.client.input.KeyPressedEvent;
import net.apolloclient.module.bus.EventHandler;
import net.apolloclient.module.bus.Instance;
import net.apolloclient.module.bus.Module;
import net.apolloclient.module.bus.event.InitializationEvent;
import net.apolloclient.utils.DataUtil;
import net.apolloclient.utils.ApolloFontRenderer;
import net.apolloclient.utils.GLRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Choose a game to play in a good-looking menu.
 *
 * @author SLLCoding
 * @since b0.2
 */
@Module(name = QuickPlay.NAME, description = QuickPlay.DESCRIPTION, author = QuickPlay.AUTHOR, recommendedServersIP = QuickPlay.RECOMMENDED_SERVERS)
public class QuickPlay {

    public static final String NAME                = "QuickPlay";
    public static final String DESCRIPTION         = "Choose a game to play in a good-looking menu.";
    public static final String AUTHOR              = "SLLCoding";
    public static final String RECOMMENDED_SERVERS = "hypixel.net";

    @Instance public static final QuickPlay instance = new QuickPlay();

    public static final ArrayList<Game> games = new ArrayList<>();

    @EventHandler(priority = Priority.HIGH)
    public void setup (InitializationEvent event) throws Exception {
        Apollo.EVENT_BUS.register(this);

        JsonObject response = new Gson().fromJson(DataUtil.getDataFromUrlOrLocal("quickplay-games.json"), JsonObject.class);
        JsonObject version = response.getAsJsonObject("version");

        Apollo.log("[Quickplay] Using games list v"
                + version.get("version").getAsString() + " ("
                + version.get("date").getAsString() + ")");

        response
                .getAsJsonArray("games")
                .forEach(game -> games.add(new Game(game.getAsJsonObject())));
    }

    @SubscribeEvent
    public void onKeyPress(KeyPressedEvent event) throws Exception {
        if (event.keyCode == Keyboard.KEY_LMENU)
        {
            if (Minecraft.getMinecraft().currentScreen == null)
            {
                Minecraft.getMinecraft().displayGuiScreen(new QuickplayGui());
            }
        }
    }

    public static class Game {

        public final String name;
        public final String icon;
        public final ArrayList<Mode> modes = new ArrayList<>();

        public Game(JsonObject data) {
            name = data.get("name").getAsString();
            icon = data.get("icon").getAsString();
            data.getAsJsonArray("modes").forEach(mode -> modes.add(new Mode(mode.getAsJsonObject())));
        }

        public static class Mode {

            public final String name;
            public final String command;
            public final boolean enabled;

            public Mode(JsonObject data) {
                name    = data.get("name").getAsString    ();
                command = data.get("command").getAsString ();
                enabled = data.get("enabled").getAsBoolean();
            }
        }
    }

}

class QuickplayGui extends GuiScreen {

    private ApolloFontRenderer fontRenderer = new ApolloFontRenderer(ApolloFontRenderer.ROBOTO, 14);
    private List<QuickPlay.Game> games = QuickPlay.games;
    private static ResourceLocation image;
    private URL url;
    private JsonObject gameMappings = new Gson().fromJson(DataUtil.getDataFromUrlOrLocal("game-name-mappings.json"), JsonObject.class);

    public QuickplayGui() throws Exception {
        Apollo.EVENT_BUS.register(this);

        URL url = new URL("https://static.icovid.dev/quickplay/icons-round.png");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");

        BufferedImage imageBuffer = ImageIO.read(connection.getInputStream());
        Minecraft.getMinecraft().addScheduledTask(() -> {
            DynamicTexture dynamicTexture = new DynamicTexture(imageBuffer);
            image = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("icons-round.png", dynamicTexture);
        });
    }

    /** Called when gui is opened **/
    public void initGui() {

    }

    /** Called when gui is closed **/
    public void closeGui() {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        String[] grid = generateGrid();
        int x = this.width / 4, y = this.height / 8;
        int xOffset = 264, yOffset = 211;
        int xReset = 0;
        int gridWidth = 4;
        int panelHeight = 4;
        Color panelColor = new Color(1, 1, 1, 100);
        this.drawDefaultBackground();

        for (int i = 0; i < panelHeight; i ++)
        {
            if (i % 2 == 0)
            {
                panelColor = new Color(100, 100, 100, 100);
            }
            else
            {
                panelColor = new Color(1, 1, 1, 100);
            }
            GLRenderer.drawRectangle(x, y + (this.height * yOffset / 1200) * i, x + ((this.height * 143 / 1200) * gridWidth), this.height * (140 + (yOffset / 3)) / 1200, panelColor);
        }

        int i = 0;
        for (int xx = 0; xx < grid.length - 3; xx++)
        {
            if ((x + ((xx - xReset) * xOffset)) >= (x + (gridWidth * xOffset)))
            {
                xReset = xx;
                y += this.height * yOffset / 1200;
                i += 1;
            }
            Color color = new Color(23, 23, 23, 150);

            GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            GlStateManager.enableBlend();

            Minecraft.getMinecraft().getTextureManager().bindTexture(image);
            drawScaledCustomSizeModalRect((x + ((xx - xReset) * (this.height * xOffset / 1200))) + this.height / 15, y + this.height / 65, xx - xReset, i,1, 1, this.height * 156 / 1200, this.height * 156 / 1200, 4.0f, 4.0f);

            GlStateManager.bindTexture(0);
            drawScaledCustomSizeModalRect((x + ((xx - xReset) * (this.height * xOffset / 1200))) + this.height / 14, y + this.height / 7, xx - xReset, i,1, 1, this.height * 156 / 1200, this.height * 20 / 1200, 4.0f, 4.0f);
            GlStateManager.color(1f, 1f, 1f, 1f);

            Minecraft.getMinecraft().getTextureManager().bindTexture(image);
            drawScaledCustomSizeModalRect((x + ((xx - xReset) * (this.height * xOffset / 1200))) + this.height / 14, y + this.height / 50, xx - xReset, i,1, 1, this.height * 143 / 1200, this.height * 143 / 1200, 4.0f, 4.0f);
            GlStateManager.bindTexture(0);

            fontRenderer.drawString((x + ((xx - xReset) * (this.height * xOffset / 1200))) + this.height / 13, y + this.height / 6.95f, QuickPlay.games.get(xx).name);
        }
    }

    private String[] generateGrid()
    {
        String[] grid = new String[games.size()];
        for (int x = 0; x < grid.length; x++)
        {
            grid[x] = QuickPlay.games.get(x).name;
        }

        return grid;
    }
}

