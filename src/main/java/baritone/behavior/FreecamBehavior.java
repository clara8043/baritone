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

package baritone.behavior;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.event.events.TickEvent;
import baritone.utils.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.world.GameType;

public class FreecamBehavior extends Behavior implements Helper {
    public FreecamBehavior(Baritone baritone) {
        super(baritone);
    }

    private boolean enabled = false;
    private EntityPlayerSP memer = null;
    private EntityPlayerSP realPlayer = null;

    public boolean enabled() {
        return enabled;
    }

    public class FreecamPlayerUwu extends EntityPlayerSP {
        public FreecamPlayerUwu() {
            super(Minecraft.getMinecraft(), ctx.world(), ctx.player().connection, ctx.player().getStatFileWriter(), ctx.player().getRecipeBook());
        }

        public boolean isSpectator() {
            return true;
        }

    }

    private void enable() {
        memer = new FreecamPlayerUwu();
        realPlayer = ctx.player();
        memer.posX = ctx.player().posX;
        memer.posY = ctx.player().posY;
        memer.posZ = ctx.player().posZ;
        memer.lastTickPosX = memer.posX;
        memer.lastTickPosY = memer.posY;
        memer.lastTickPosZ = memer.posZ;
        memer.rotationYaw = ctx.player().rotationYaw;
        memer.rotationPitch = ctx.player().rotationPitch;
        memer.prevRotationYaw = memer.rotationYaw;
        memer.prevRotationPitch = memer.rotationPitch;
        memer.movementInput = new MovementInputFromOptions(mc.gameSettings);

        GameType.SPECTATOR.configurePlayerCapabilities(memer.capabilities);
        mc.setRenderViewEntity(memer);
        logDirect("Setting render view entity " + memer);
        logDirect("Spectator: " + memer.isSpectator());

        enabled = true;
    }

    private void disable() {
        mc.setRenderViewEntity(null);
        enabled = false;
        memer = null;
    }

    public void setEnabled(boolean enabled) {
        if (enabled ^ this.enabled) {
            if (enabled) {
                enable();
            } else {
                disable();
            }
        }
    }

    public EntityPlayerSP getEntity() {
        return memer;
    }

    public EntityPlayerSP realPlayer() {
        return realPlayer;
    }

    @Override
    public void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.OUT) {
            return;
        }
        setEnabled(baritone == BaritoneAPI.getProvider().getPrimaryBaritone() && Baritone.settings().f.get());
        if (enabled()) {
            if (realPlayer.movementInput.getClass() == MovementInputFromOptions.class) {
                realPlayer.movementInput = new NoMovementInput();
            }

            memer.movementInput.updatePlayerMoveState();
            memer.moveRelative(memer.movementInput.moveStrafe, memer.movementInput.jump ? 1 : memer.movementInput.sneak ? -1 : 0, memer.movementInput.moveForward, 1);
            memer.lastTickPosX = memer.posX;
            memer.lastTickPosY = memer.posY;
            memer.lastTickPosZ = memer.posZ;
            memer.posX += memer.motionX;
            memer.posY += memer.motionY;
            memer.posZ += memer.motionZ;
            memer.motionX = 0;
            memer.motionY = 0;
            memer.motionZ = 0;
        } else {
            if (realPlayer != null && realPlayer.movementInput.getClass() == NoMovementInput.class) {
                realPlayer.movementInput = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
            }
        }

    }

    public class NoMovementInput extends MovementInput { // we extend this so that we can tell by class if we set it, or if another class set it
    }
}
