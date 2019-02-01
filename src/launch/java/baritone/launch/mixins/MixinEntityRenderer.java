/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.launch.mixins;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.RenderEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(
            method = "renderWorldPass",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    args = {"ldc=hand"}
            )
    )
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            ibaritone.getGameEventHandler().onRenderPass(new RenderEvent(partialTicks));
        }
    }

    EntityPlayerSP stash;
    PlayerControllerMP stash2;

    @Inject(
            method = "updateCameraAndRender",
            at = @At(
                    "HEAD"
            )
    )
    private void ohgod(CallbackInfo ci) {
        Baritone baritone = (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone.getFreecamBehavior().enabled()) {
            stash = Minecraft.getMinecraft().player;
            stash2 = Minecraft.getMinecraft().playerController;
            Minecraft.getMinecraft().player = baritone.getFreecamBehavior().getEntity();
            Minecraft.getMinecraft().playerController = new PlayerControllerMP(Minecraft.getMinecraft(), stash.connection);
            Minecraft.getMinecraft().playerController.setGameType(GameType.CREATIVE);
        }
    }

    @Inject(method = "updateCameraAndRender",
            at = @At(
                    "RETURN"
            )
    )
    private void ohgod2(CallbackInfo ci) {
        if (stash != null) {
            Minecraft.getMinecraft().player = stash;
            Minecraft.getMinecraft().playerController = stash2;
            stash = null;
            stash2 = null;
        }
    }


    @Inject(
            method = "updateRenderer",
            at = @At(
                    "HEAD"
            )
    )
    private void ohgod3(CallbackInfo ci) {
        Baritone baritone = (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        if (baritone.getFreecamBehavior().enabled()) {
            stash = Minecraft.getMinecraft().player;
            stash2 = Minecraft.getMinecraft().playerController;
            Minecraft.getMinecraft().player = baritone.getFreecamBehavior().getEntity();
            Minecraft.getMinecraft().playerController = new PlayerControllerMP(Minecraft.getMinecraft(), stash.connection);
            Minecraft.getMinecraft().playerController.setGameType(GameType.CREATIVE);
        }
    }

    @Inject(method = "updateRenderer",
            at = @At(
                    "RETURN"
            )
    )
    private void ohgod4(CallbackInfo ci) {
        if (stash != null) {
            Minecraft.getMinecraft().player = stash;
            Minecraft.getMinecraft().playerController = stash2;
            stash = null;
            stash2 = null;
        }
    }

    @Redirect(
            method = "getMouseOver",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/Minecraft.getRenderViewEntity()Lnet/minecraft/entity/Entity;"
            )
    )
    private Entity getRenderViewEntity() {
        return Minecraft.getMinecraft().player;
    }
}
