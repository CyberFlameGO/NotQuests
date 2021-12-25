/*
 * NotQuests - A Questing plugin for Minecraft Servers
 * Copyright (C) 2021 Alessio Gravili
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rocks.gravili.notquests.paper.structs.actions;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import rocks.gravili.notquests.paper.NotQuests;

import java.util.ArrayList;
import java.util.List;

public class BroadcastMessageAction extends Action {

    private String messageToBroadcast = "";


    public BroadcastMessageAction(final NotQuests main) {
        super(main);
    }

    public static void handleCommands(NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> builder, ActionFor actionFor) {
        manager.command(builder.literal("BroadcastMessage")
                .argument(StringArgument.<CommandSender>newBuilder("Message").withSuggestionsProvider(
                        (context, lastString) -> {
                            final List<String> allArgs = context.getRawInput();
                            main.getUtilManager().sendFancyCommandCompletion(context.getSender(), allArgs.toArray(new String[0]), "<Message to broadcast>", "");

                            ArrayList<String> completions = new ArrayList<>();

                            completions.add("<Message to broadcast>");
                            return completions;

                        }
                ).greedy().build(), ArgumentDescription.of("Message to broadcast"))
                .meta(CommandMeta.DESCRIPTION, "Creates a new BroadcastMessage Action")
                .handler((context) -> {
                    final String messageToBroadcast = context.get("Message");

                    BroadcastMessageAction broadcastMessageAction = new BroadcastMessageAction(main);
                    broadcastMessageAction.setMessageToBroadcast(messageToBroadcast);

                    main.getActionManager().addAction(broadcastMessageAction, context);
                }));
    }

    public final String getMessageToBroadcast() {
        return messageToBroadcast;
    }

    public void setMessageToBroadcast(final String messageToBroadcast) {
        this.messageToBroadcast = messageToBroadcast;
    }


    @Override
    public void execute(final Player player, Object... objects) {
        Bukkit.broadcast(main.parse(
                getMessageToBroadcast()
        ));
    }

    @Override
    public void save(FileConfiguration configuration, String initialPath) {
        configuration.set(initialPath + ".specifics.message", getMessageToBroadcast());
    }

    @Override
    public void load(final FileConfiguration configuration, String initialPath) {
        this.messageToBroadcast = configuration.getString(initialPath + ".specifics.message", "");
    }


    @Override
    public String getActionDescription() {
        return "Broadcasts Message: " + getMessageToBroadcast();
    }
}
