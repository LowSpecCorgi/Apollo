/*⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤
Copyright (C) 2020-2021 developed by Icovid and Apollo Development Team

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

package net.apolloclient.mixins.client;

import net.apolloclient.Apollo;
import net.apolloclient.event.impl.client.GameLoopEvent;
import net.apolloclient.event.impl.client.input.KeyPressedEvent;
import net.apolloclient.event.impl.client.input.KeyReleasedEvent;
import net.apolloclient.event.impl.client.input.LeftClickEvent;
import net.apolloclient.event.impl.client.input.RightClickEvent;
import net.apolloclient.event.impl.hud.GuiSwitchEvent;
import net.apolloclient.event.impl.world.LoadWorldEvent;
import net.apolloclient.event.impl.world.SinglePlayerJoinEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Event target ejections for Minecraft.class.
 *
 * @author Icovid | Icovid#3888
 * @since b0.2
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {

  /**
   * Edits the Title bar
   *
   * @param callbackInfo is unused
   */
  @Inject(method = "createDisplay", at = @At("RETURN"))
  public void createDisplay(CallbackInfo callbackInfo) {
    Display.setTitle(Apollo.NAME + " " + Apollo.VERSION);
  }

  /**
   * Post {@link Apollo} start.
   *
   * @param callbackInfo unused
   */
  @Inject(method = "startGame", at = @At("RETURN"))
  private void onGameStart(CallbackInfo callbackInfo) {
    Apollo.INSTANCE.postInitialization();
  }

  /**
   * Post {@link Apollo} shutdown.
   *
   * @param callbackInfo unused
   */
  @Inject(method = "shutdown", at = @At("HEAD"))
  private void shutdown(CallbackInfo callbackInfo) {
    Apollo.INSTANCE.stopClient();
  }

  /**
   * Post {@link GameLoopEvent} every tick.
   *
   * @param callbackInfo unused
   * @author Nora Cos | #Nora#0001
   */
  @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;skipRenderWorld:Z", shift = At.Shift.AFTER))
  private void runGameLoop(CallbackInfo callbackInfo) {
    new GameLoopEvent().post();
  }

  /**
   * Post {@link KeyPressedEvent} or {@link KeyReleasedEvent} at key press.
   *
   * @param callbackInfo unused
   */
  @Inject(method = "dispatchKeypresses", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
  private void runTickKeyboard(CallbackInfo callbackInfo) {
    Apollo.EVENT_BUS.post(
        Keyboard.getEventKeyState()
            ? new KeyPressedEvent(Keyboard.isRepeatEvent(), Keyboard.getEventKey())
            : new KeyReleasedEvent(Keyboard.isRepeatEvent(), Keyboard.getEventKey()));
  }

  /**
   * Post {@link LeftClickEvent} at left mouse click.
   *
   * @param callbackInfo unused
   */
  @Inject(method = "clickMouse", at = @At("RETURN"))
  private void leftClickMouse(CallbackInfo callbackInfo) {
    new LeftClickEvent().post();
  }

  /**
   * Post {@link RightClickEvent} at right mouse click.
   *
   * @param callbackInfo unused
   */
  @Inject(method = "rightClickMouse", at = @At("RETURN"))
  private void rightClickMouse(CallbackInfo callbackInfo) {
    new RightClickEvent().post();
  }

  /**
   * Post {@link SinglePlayerJoinEvent} when joining single player world.
   *
   * @param folderName name of folder world file is located in.
   * @param worldName name of world
   * @param worldSettingsIn settings of world
   * @param callbackInfo unused
   */
  @Inject(method = "launchIntegratedServer", at = @At("HEAD"))
  private void joinSinglePlayer(String folderName, String worldName, WorldSettings worldSettingsIn, CallbackInfo callbackInfo) {
    new SinglePlayerJoinEvent(folderName, worldName, worldSettingsIn).post();
  }

  /**
   * Post {@link LoadWorldEvent} when new world is loaded for player.
   *
   * @param worldClient world client used.
   * @param callbackInfo unused
   */
  @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;)V", at = @At("HEAD"))
  private void loadWorld(WorldClient worldClient, CallbackInfo callbackInfo) {
    new LoadWorldEvent(worldClient).post();
  }

  /**
   * Post {@link GuiSwitchEvent} when a GUI is opened.
   *
   * @param guiScreenIn gui opened.
   * @param callbackInfo unused
   */
  @Inject(method = "displayGuiScreen", at = @At("HEAD"))
  private void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo callbackInfo) {
    new GuiSwitchEvent(guiScreenIn).post();
  }
}
