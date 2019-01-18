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
import baritone.api.event.events.ChatEvent;
import baritone.utils.Helper;
import net.minecraft.util.text.ITextComponent;

public class ChatControlBehavior extends Behavior {
    public ChatControlBehavior(Baritone baritone) {
        super(baritone);
    }

    public String controlledBy;

    public void onChatReceived(ITextComponent component) {
        if (controlledBy == null || controlledBy.equals("")) {
            return;
        }
        String text = component.getUnformattedText();
        String[] prefixes = {"<" + controlledBy + ">", controlledBy + " whispers: ", controlledBy + ": "};
        for (String str : prefixes) {
            if (text.startsWith(str)) {
                String command = text.substring(str.length());
                Helper.HELPER.logDirect("Executing command " + command + " because of chat message " + text);
                baritone.getGameEventHandler().onSendChatMessage(new ChatEvent(ctx.player(), command));
            }
        }
    }
}
