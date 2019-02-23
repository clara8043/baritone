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

package baritone.process;

import baritone.Baritone;
import baritone.api.process.PathingCommand;
import baritone.api.utils.ISchematic;
import baritone.utils.BaritoneProcessHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.EmptyChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BackfillProcess extends BaritoneProcessHelper {

    public HashMap<BlockPos, IBlockState> blocksToReplace = new HashMap<>();
    boolean active;
    int ticks;

    public BackfillProcess(Baritone baritone) {
        super(baritone, 10);
    }

    @Override
    public boolean isActive() {
        if (!Baritone.settings().backfill.get()) {
            return false;
        }
        amIBreakingABlockHMMMMMMM();
        for (BlockPos pos : new ArrayList<>(blocksToReplace.keySet())) {
            if (ctx.world().getChunk(pos) instanceof EmptyChunk) {
                blocksToReplace.remove(pos);
            }
        }
        baritone.getInputOverrideHandler().clearAllKeys();
        System.out.println(blocksToReplace);

        if (toFillIn() == null) {
            active = false;
            return false;
        }
        if (ticks++ % 20 == 0) {
            active = true;
        }
        return active;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        baritone.getInputOverrideHandler().clearAllKeys();
        if (!baritone.getBuilderProcess().isActive()) {
            BlockPos pos = toFillIn();
            System.out.println("To fill in " + pos);
            if (pos == null) {
                return null;
            }
            IBlockState shouldBe = blocksToReplace.get(pos);
            ISchematic cancer = new ISchematic() {

                @Override
                public IBlockState desiredState(int x, int y, int z) {
                    return shouldBe;
                }

                @Override
                public int widthX() {
                    return 1;
                }

                @Override
                public int heightY() {
                    return 1;
                }

                @Override
                public int lengthZ() {
                    return 1;
                }
            };
            baritone.getBuilderProcess().build("Backfill", cancer, pos);
        }
        PathingCommand cmd = baritone.getBuilderProcess().onTick(calcFailed, isSafeToCancel);
        System.out.println("Got command " + cmd);
        if (cmd != null) {
            System.out.println(cmd.commandType + " " + cmd.goal);
        } else {
            return onTick(calcFailed, isSafeToCancel);
        }
        return cmd;
    }

    public void amIBreakingABlockHMMMMMMM() {
        if (!ctx.getSelectedBlock().isPresent()) {
            return;
        }
        blocksToReplace.put(ctx.getSelectedBlock().get(), ctx.world().getBlockState(ctx.getSelectedBlock().get()));
    }

    public BlockPos toFillIn() {
        for (BlockPos pos : blocksToReplace.keySet().stream().sorted(Comparator.comparingInt(BlockPos::hashCode)).collect(Collectors.toList())) {
            if (ctx.world().getBlockState(pos).getBlock() == Blocks.AIR && ctx.world().mayPlace(Blocks.DIRT, pos, false, EnumFacing.UP, null)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void onLostControl() {
        Baritone.settings().backfill.value = false; // i guess?
    }

    @Override
    public String displayName() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
