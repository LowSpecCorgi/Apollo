/*⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤
 Copyright (C) 2020-2021 developed by Icovid and Apollo Development Team

 MixinMinecraft.java is part of Apollo Client. 9/3/20, 12:06 AM

 MixinMinecraft.java can not be copied and/or distributed without the express
 permission of Icovid

 Contact: Icovid#3888 @ https://discord.com
 ⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤⏤*/

package io.apollo.mixinminecraft;

import io.apollo.Apollo;
import io.apollo.events.impl.GameLoopEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** MixinBootstrap Events for Minecraft.class.
 * @author isXander | isXander#4285
 * @since 1.0.0 **/
@Mixin(Minecraft.class) public class MixinMinecraft {

    // Called on game start
    @Inject(method = "startGame", at = @At("RETURN"))
    private void onGameStart(CallbackInfo info) { Apollo.INSTANCE.postInitialisation(); }

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
}
