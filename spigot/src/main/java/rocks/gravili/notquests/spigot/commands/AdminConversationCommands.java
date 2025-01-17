/*
 * NotQuests - A Questing plugin for Minecraft Servers
 * Copyright (C) 2021-2022 Alessio Gravili
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

package rocks.gravili.notquests.spigot.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.io.IOUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import rocks.gravili.notquests.spigot.NotQuests;
import rocks.gravili.notquests.spigot.commands.arguments.ConversationSelector;
import rocks.gravili.notquests.spigot.commands.arguments.SpeakerSelector;
import rocks.gravili.notquests.spigot.conversation.Conversation;
import rocks.gravili.notquests.spigot.conversation.ConversationLine;
import rocks.gravili.notquests.spigot.conversation.ConversationManager;
import rocks.gravili.notquests.spigot.conversation.Speaker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static rocks.gravili.notquests.spigot.commands.NotQuestColors.*;

public class AdminConversationCommands {
    protected final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final NotQuests main;
    private final PaperCommandManager<CommandSender> manager;
    private final Command.Builder<CommandSender> conversationBuilder;

    private final ConversationManager conversationManager;


    public AdminConversationCommands(final NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> conversationBuilder, final ConversationManager conversationManager) {
        this.main = main;
        this.manager = manager;
        this.conversationBuilder = conversationBuilder;

        this.conversationManager = conversationManager;


        manager.command(conversationBuilder.literal("create")
                .argument(StringArgument.<CommandSender>newBuilder("Conversation Name").withSuggestionsProvider(
                        (context, lastString) -> {
                            final List<String> allArgs = context.getRawInput();
                            final Audience audience = main.adventure().sender(context.getSender());
                            main.getUtilManager().sendFancyCommandCompletion(audience, allArgs.toArray(new String[0]), "[New Conversation Name]", "");

                            ArrayList<String> completions = new ArrayList<>();

                            completions.add("<Enter new Conversation Name>");
                            return completions;
                        }
                ).single().build(), ArgumentDescription.of("Conversation Name"))
                .flag(
                        manager.flagBuilder("demo")
                                .withDescription(ArgumentDescription.of("Fills the new conversation file with demo data"))
                )
                .meta(CommandMeta.DESCRIPTION, "Creates a new conversation file.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    final String conversationName = context.get("Conversation Name");
                    final boolean demo = context.flags().isPresent("demo");


                    final Conversation existingConversation = main.getConversationManager().getConversation(conversationName);

                    if (existingConversation == null) {
                        File newConversationFile = new File(main.getConversationManager().getConversationsFolder().getPath() + "/" + conversationName + ".yml");

                        try {
                            if (!newConversationFile.exists()) {
                                if (!newConversationFile.createNewFile()) {
                                    audience.sendMessage(miniMessage.deserialize(
                                            errorGradient + "Error: couldn't create conversation file."
                                    ));
                                    return;
                                }
                                InputStream inputStream;
                                if (!demo) {
                                    inputStream = main.getMain().getResource("conversations/empty.yml");
                                } else {
                                    inputStream = main.getMain().getResource("conversations/demo.yml");
                                }

                                //Instead of creating a new language file, we will copy the one from inside of the plugin jar into the plugin folder:
                                if (inputStream != null) {
                                    try (OutputStream outputStream = new FileOutputStream(newConversationFile)) {
                                        IOUtils.copy(inputStream, outputStream);
                                        main.getConversationManager().loadConversationsFromConfig();
                                        audience.sendMessage(miniMessage.deserialize(
                                                successGradient + "The conversation has been created successfully! There are currently no commands to edit them - you have to edit the conversation file. You can find it at " + highlightGradient + "plugins/NotQuests/conversations/" + conversationName + ".yml"
                                        ));
                                    } catch (Exception e) {
                                        audience.sendMessage(miniMessage.deserialize(
                                                errorGradient + "Error: couldn't create conversation file. There was an exception. (2)"
                                        ));
                                        return;
                                    }


                                }
                            }


                        } catch (Exception e) {
                            audience.sendMessage(miniMessage.deserialize(
                                    errorGradient + "Error: couldn't create conversation file. There was an exception."
                            ));
                        }

                    } else {
                        audience.sendMessage(miniMessage.deserialize(
                                errorGradient + "Error: the conversation " + highlightGradient + existingConversation.getIdentifier() + "</gradient> already exists!"
                        ));
                    }


                }));


        if (main.getConfiguration().debug) {
            manager.command(conversationBuilder.literal("test")
                    .senderType(Player.class)
                    .meta(CommandMeta.DESCRIPTION, "Starts a test conversation.")
                    .handler((context) -> {
                        final Audience audience = main.adventure().sender(context.getSender());
                        final Player player = (Player) context.getSender();

                        audience.sendMessage(miniMessage.deserialize(
                                mainGradient + "Playing test conversation..."
                        ));
                        conversationManager.playConversation(player, conversationManager.createTestConversation());
                    }));
        }


        manager.command(conversationBuilder.literal("list")
                .meta(CommandMeta.DESCRIPTION, "Lists all conversations.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    audience.sendMessage(miniMessage.deserialize(
                            highlightGradient + "All conversations:"
                    ));
                    int counter = 1;
                    for (final Conversation conversation : conversationManager.getAllConversations()) {
                        audience.sendMessage(miniMessage.deserialize(
                                highlightGradient + counter + ". </gradient>" + mainGradient + conversation.getIdentifier()
                        ));

                        audience.sendMessage(miniMessage.deserialize(
                                unimportant + "--- Attached to NPC: " + unimportantClose + mainGradient + conversation.getNPCID()
                        ));

                        audience.sendMessage(miniMessage.deserialize(
                                unimportant + "--- Amount of starting conversation lines: " + unimportantClose + mainGradient + conversation.getStartingLines().size()
                        ));
                    }

                }));

        manager.command(conversationBuilder.literal("analyze")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .flag(
                        manager.flagBuilder("printToConsole")
                                .withDescription(ArgumentDescription.of("Prints the output to the console"))
                )
                .meta(CommandMeta.DESCRIPTION, "Analyze specific conversations.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    final Conversation foundConversation = context.get("conversation");

                    boolean printToConsole = context.flags().contains("printToConsole");

                    for (ConversationLine conversationLine : foundConversation.getStartingLines()) {
                        final String analyzed = main.getConversationManager().analyze(conversationLine, "  ");

                        if (printToConsole) {
                            main.getLogManager().info("\n" + analyzed);
                            audience.sendMessage(miniMessage.deserialize(
                                    successGradient + "Analyze Output has been printed to your console!"
                            ));
                        } else {
                            audience.sendMessage(miniMessage.deserialize(
                                    analyzed
                            ));
                        }
                    }
                }));

        manager.command(conversationBuilder.literal("start")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "Starts a conversation.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());
                    final Player player = (Player) context.getSender();

                    final Conversation foundConversation = context.get("conversation");


                    audience.sendMessage(miniMessage.deserialize(
                            mainGradient + "Playing " + foundConversation.getIdentifier() + " conversation..."
                    ));
                    conversationManager.playConversation(player, foundConversation);
                }));


        if (main.getIntegrationsManager().isCitizensEnabled()) {
            manager.command(conversationBuilder.literal("edit")
                    .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                    .literal("npc")
                    .argument(IntegerArgument.<CommandSender>newBuilder("NPC").withSuggestionsProvider((context, lastString) -> {
                        ArrayList<String> completions = new ArrayList<>();
                        completions.add("-1");
                        for (final NPC npc : CitizensAPI.getNPCRegistry().sorted()) {
                            completions.add("" + npc.getId());
                        }
                        final List<String> allArgs = context.getRawInput();
                        final Audience audience = main.adventure().sender(context.getSender());
                        main.getUtilManager().sendFancyCommandCompletion(audience, allArgs.toArray(new String[0]), "[NPC ID]", "");

                        return completions;
                    }).build(), ArgumentDescription.of("ID of the Citizens NPC which should start the conversation (set to -1 to disable)"))
                    .meta(CommandMeta.DESCRIPTION, "Set conversation NPC (-1 = disabled)")
                    .handler((context) -> {
                        final Audience audience = main.adventure().sender(context.getSender());

                        final Conversation foundConversation = context.get("conversation");
                        final int npcID = context.get("NPC");

                        foundConversation.setNPC(npcID);

                        audience.sendMessage(miniMessage.deserialize(
                                mainGradient + "NPC of conversation " + highlightGradient + foundConversation.getIdentifier() + "</gradient> has been set to "
                                        + highlight2Gradient + npcID + "</gradient>!"
                        ));
                    }));
        }


        manager.command(conversationBuilder.literal("edit")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .literal("armorstand")
                .literal("add", "set")
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "Gives you an item to add conversation to an armorstand")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());
                    final Player player = (Player) context.getSender();

                    final Conversation foundConversation = context.get("conversation");


                    ItemStack itemStack = new ItemStack(Material.PAPER, 1);
                    //give a specialitem. clicking an armorstand with that special item will remove the pdb.

                    NamespacedKey key = new NamespacedKey(main.getMain(), "notquests-item");
                    NamespacedKey conversationIdentifierKey = new NamespacedKey(main.getMain(), "notquests-conversation");

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    //Only paper List<Component> lore = new ArrayList<>();
                    List<String> lore = new ArrayList<>();

                    assert itemMeta != null;

                    itemMeta.getPersistentDataContainer().set(conversationIdentifierKey, PersistentDataType.STRING, foundConversation.getIdentifier());
                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 8);


                    //Only paper itemMeta.displayName(Component.text("§dCheck Armor Stand", NamedTextColor.LIGHT_PURPLE));
                    itemMeta.setDisplayName("§dAdd conversation §b" + foundConversation.getIdentifier() + " §dto this Armor Stand");
                    //Only paper lore.add(Component.text("§fRight-click an Armor Stand to see which Quests are attached to it."));
                    lore.add("§fRight-click an Armor Stand to add the conversation §b" + foundConversation.getIdentifier() + " §fto it.");

                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    //Only paper itemMeta.lore(lore);

                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);

                    player.getInventory().addItem(itemStack);

                    audience.sendMessage(miniMessage.deserialize(
                            successGradient + "You have been given an item with which you can add the conversation " + highlightGradient + foundConversation.getIdentifier() + "</gradient> to an armor stand. Check your inventory!"
                    ));
                }));

        manager.command(conversationBuilder.literal("edit")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .literal("armorstand")
                .literal("remove", "delete")
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "Gives you an item to remove all conversations from an armorstand")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());
                    final Player player = (Player) context.getSender();


                    ItemStack itemStack = new ItemStack(Material.PAPER, 1);
                    //give a specialitem. clicking an armorstand with that special item will remove the pdb.

                    NamespacedKey key = new NamespacedKey(main.getMain(), "notquests-item");
                    NamespacedKey conversationIdentifierKey = new NamespacedKey(main.getMain(), "notquests-conversation");

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    //Only paper List<Component> lore = new ArrayList<>();
                    List<String> lore = new ArrayList<>();

                    assert itemMeta != null;

                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 9);


                    //Only paper itemMeta.displayName(Component.text("§dCheck Armor Stand", NamedTextColor.LIGHT_PURPLE));
                    itemMeta.setDisplayName("§dRemove all conversations from this Armor Stand");
                    //Only paper lore.add(Component.text("§fRight-click an Armor Stand to see which Quests are attached to it."));
                    lore.add("§fRight-click an Armor Stand to remove all conversations attached to it.");

                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    //Only paper itemMeta.lore(lore);

                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);

                    player.getInventory().addItem(itemStack);

                    audience.sendMessage(miniMessage.deserialize(
                            successGradient + "You have been given an item with which you remove all conversations from an armor stand. Check your inventory!"
                    ));
                }));


        manager.command(conversationBuilder.literal("edit")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .literal("speakers")
                .literal("add", "create")
                .argument(StringArgument.<CommandSender>newBuilder("Speaker Name").withSuggestionsProvider(
                        (context, lastString) -> {
                            final List<String> allArgs = context.getRawInput();
                            final Audience audience = main.adventure().sender(context.getSender());
                            main.getUtilManager().sendFancyCommandCompletion(audience, allArgs.toArray(new String[0]), "[New Speaker Name]", "");

                            ArrayList<String> completions = new ArrayList<>();

                            completions.add("<Enter new Speaker Name>");
                            return completions;
                        }
                ).single().build(), ArgumentDescription.of("Speaker Name"))
                .flag(main.getCommandManager().speakerColor)
                .meta(CommandMeta.DESCRIPTION, "Adds / creates a new speaker for the conversation.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    final Conversation foundConversation = context.get("conversation");


                    final String speakerName = context.get("Speaker Name");
                    final String speakerColor = context.flags().getValue(main.getCommandManager().speakerColor, "");

                    Speaker speaker = new Speaker(speakerName);
                    if (speakerColor != null && !speakerColor.isBlank()) {
                        speaker.setColor(speakerColor);
                    }

                    if (foundConversation.addSpeaker(speaker, true)) {
                        audience.sendMessage(miniMessage.deserialize(
                                successGradient + "Speaker " + highlightGradient + speaker.getSpeakerName() + "</gradient> was successfully added to conversation "
                                        + highlight2Gradient + foundConversation.getIdentifier() + "</gradient>!"
                        ));
                    } else {
                        audience.sendMessage(miniMessage.deserialize(
                                errorGradient + "Speaker " + highlightGradient + speaker.getSpeakerName() + "</gradient> could not be added to "
                                        + highlight2Gradient + foundConversation.getIdentifier() + "</gradient>! Does it already exist?"
                        ));
                    }


                }));

        manager.command(conversationBuilder.literal("edit")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .literal("speakers")
                .literal("list", "show")
                .meta(CommandMeta.DESCRIPTION, "Adds / creates a new speaker for the conversation.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    final Conversation foundConversation = context.get("conversation");

                    if (foundConversation.getSpeakers().size() == 0) {
                        audience.sendMessage(miniMessage.deserialize(
                                successGradient + "This conversation has no speakers."
                        ));
                    } else {
                        audience.sendMessage(miniMessage.deserialize(highlightGradient + "Speakers of conversation " + highlight2Gradient + foundConversation.getIdentifier() + "</gradient>:</gradient>"));
                        int counter = 0;
                        for (final Speaker speaker : foundConversation.getSpeakers()) {
                            counter++;

                            audience.sendMessage(miniMessage.deserialize(highlightGradient + counter + ".</gradient> " + mainGradient + "Name:</gradient> " + highlight2Gradient + speaker.getSpeakerName() + "</gradient> Color: " + highlight2Gradient + speaker.getColor() + speaker.getColor().replace("<", "").replace(">", "") + "</gradient>"));
                        }
                    }

                }));

        manager.command(conversationBuilder.literal("edit")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .literal("speakers")
                .literal("remove", "delete")
                .argument(SpeakerSelector.of("Speaker", main, "conversation"))

                .meta(CommandMeta.DESCRIPTION, "Adds / creates a new speaker for the conversation.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    final Conversation foundConversation = context.get("conversation");


                    final Speaker speaker = context.get("Speaker");


                    if (foundConversation.hasSpeaker(speaker) && foundConversation.removeSpeaker(speaker, true)) {
                        //TODO: Reload conversation here
                        audience.sendMessage(miniMessage.deserialize(
                                successGradient + "Speaker " + highlightGradient + speaker.getSpeakerName() + "</gradient> was successfully removed from conversation "
                                        + highlight2Gradient + foundConversation.getIdentifier() + "</gradient>!"
                        ));
                    } else {
                        audience.sendMessage(miniMessage.deserialize(
                                errorGradient + "Speaker " + highlightGradient + speaker.getSpeakerName() + "</gradient> could not be removed from "
                                        + highlight2Gradient + foundConversation.getIdentifier() + "</gradient>! Does it exist?"
                        ));
                    }


                }));

        handleLinesCommands();
    }

    public void handleLinesCommands() {
        /*manager.command(conversationBuilder.literal("edit")
                .argument(ConversationSelector.of("conversation", main), ArgumentDescription.of("Name of the Conversation."))
                .literal("lines")
                .literal("add", "create")
                .meta(CommandMeta.DESCRIPTION, "Creates a line for a conversation.")
                .handler((context) -> {
                    final Audience audience = main.adventure().sender(context.getSender());

                    final Conversation foundConversation = context.get("conversation");

                    audience.sendMessage(miniMessage.deserialize(
                            highlightGradient + "Starting lines (max. 3 levels of next):"
                    ));
                    final int npcID = context.get("NPC");

                    foundConversation.setNPC(npcID);

                    audience.sendMessage(miniMessage.deserialize(
                            mainGradient + "NPC of conversation " + highlightGradient + foundConversation.getIdentifier() + "</gradient> has been set to "
                                    + highlight2Gradient + npcID + "</gradient>!"
                    ));


                }));*/
    }
}
