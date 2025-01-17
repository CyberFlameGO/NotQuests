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

package rocks.gravili.notquests.paper.structs.objectives;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.structs.ActiveObjective;
import rocks.gravili.notquests.paper.structs.Quest;
import rocks.gravili.notquests.paper.structs.QuestPlayer;

public class TalkToNPCObjective extends Objective {

    private int npcToTalkID = -1;

    private UUID armorStandUUID = null;

    public TalkToNPCObjective(NotQuests main) {
        super(main);
    }

    public static void handleCommands(NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> addObjectiveBuilder) {
        manager.command(addObjectiveBuilder
                .argument(StringArgument.<CommandSender>newBuilder("NPC or Armorstand").withSuggestionsProvider((context, lastString) -> {
                    ArrayList<String> completions = new ArrayList<>();
                    for (final NPC npc : CitizensAPI.getNPCRegistry().sorted()) {
                        completions.add("" + npc.getId());
                    }
                    completions.add("armorstand");
                    final List<String> allArgs = context.getRawInput();
                    main.getUtilManager().sendFancyCommandCompletion(context.getSender(), allArgs.toArray(new String[0]), "[NPC ID / 'armorstand']", "");

                    return completions;
                }).build(), ArgumentDescription.of("ID of the Citizens NPC or 'armorstand' to whom you should talk."))
                .handler((context) -> {
                    final Quest quest = context.get("quest");

                    final String npcIDOrArmorstand = context.get("NPC or Armorstand");


                    if (!npcIDOrArmorstand.equalsIgnoreCase("armorstand")) {
                        if (!main.getIntegrationsManager().isCitizensEnabled()) {
                            context.getSender().sendMessage(main.parse(
                                    "<error>Error: Any kind of NPC stuff has been disabled, because you don't have the Citizens plugin installed on your server. You need to install the Citizens plugin in order to use Citizen NPCs. You can, however, use armor stands as an alternative. To do that, just enter 'armorstand' instead of the NPC ID."
                            ));
                            return;
                        }
                        int npcID;
                        try {
                            npcID = Integer.parseInt(npcIDOrArmorstand);
                        } catch (NumberFormatException e) {
                            context.getSender().sendMessage(
                                    main.parse(
                                            "<error>Invalid NPC ID."
                                    )
                            );
                            return;
                        }

                        TalkToNPCObjective talkToNPCObjective = new TalkToNPCObjective(main);
                        talkToNPCObjective.setNPCtoTalkID(npcID);

                        main.getObjectiveManager().addObjective(talkToNPCObjective, context);
                    } else {//Armorstands
                        if (context.getSender() instanceof Player player) {

                            ItemStack itemStack = new ItemStack(Material.PAPER, 1);
                            //give a specialitem. clicking an armorstand with that special item will remove the pdb.

                            NamespacedKey key = new NamespacedKey(main.getMain(), "notquests-item");
                            NamespacedKey QuestNameKey = new NamespacedKey(main.getMain(), "notquests-questname");

                            ItemMeta itemMeta = itemStack.getItemMeta();
                            List<Component> lore = new ArrayList<>();

                            assert itemMeta != null;

                            itemMeta.getPersistentDataContainer().set(QuestNameKey, PersistentDataType.STRING, quest.getQuestName());
                            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 5);

                            itemMeta.displayName(main.parse("<LIGHT_PURPLE>Add TalkToNPC Objective to Armor Stand"));

                            lore.add(main.parse(
                                    "<WHITE>Right-click an Armor Stand to add the following objective to it:"
                            ));
                            lore.add(main.parse(
                                    "<YELLOW>TalkToNPC <WHITE>Objective of Quest <highlight>" + quest.getQuestName() + "</highlight>."
                            ));

                            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            itemMeta.lore(lore);

                            itemStack.setItemMeta(itemMeta);

                            player.getInventory().addItem(itemStack);

                            context.getSender().sendMessage(
                                    main.parse(
                                            "<success>You have been given an item with which you can add the TalkToNPC Objective to an armor stand. Check your inventory!"
                                    )
                            );


                        } else {
                            context.getSender().sendMessage(
                                    main.parse(
                                            "<error>Must be a player!"
                                    )
                            );
                        }
                    }


                }));

    }

    @Override
    public String getTaskDescriptionInternal(final QuestPlayer questPlayer, final @Nullable ActiveObjective activeObjective) {
        String toReturn = "";
        if (main.getIntegrationsManager().isCitizensEnabled() && getNPCtoTalkID() != -1) {
            final NPC npc = CitizensAPI.getNPCRegistry().getById(getNPCtoTalkID());
            if (npc != null) {
                final String mmNpcName = main.getMiniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(npc.getName().replace("§","&")));

                toReturn = main.getLanguageManager().getString("chat.objectives.taskDescription.talkToNPC.base", questPlayer, activeObjective, Map.of(
                        "%NAME%", mmNpcName
                ));
            } else {
                toReturn = "    <GRAY>The target NPC is currently not available!";
            }
        } else {
            if (getNPCtoTalkID() != -1) {
                toReturn += "    <RED>Error: Citizens plugin not installed. Contact an admin.";
            } else { //Armor Stands
                final UUID armorStandUUID = getArmorStandUUID();
                if (armorStandUUID != null) {
                    toReturn = main.getLanguageManager().getString("chat.objectives.taskDescription.talkToNPC.base", questPlayer, activeObjective, Map.of(
                            "%NAME%", main.getArmorStandManager().getArmorStandName(armorStandUUID)
                    ));
                } else {
                    toReturn += "    <GRAY>The target Armor Stand is currently not available!";
                }
            }
        }
        return toReturn;
    }

    @Override
    public void save(FileConfiguration configuration, String initialPath) {
        configuration.set(initialPath + ".specifics.NPCtoTalkID", getNPCtoTalkID());
        if (getArmorStandUUID() != null) {
            configuration.set(initialPath + ".specifics.ArmorStandToTalkUUID", getArmorStandUUID().toString());
        } else {
            configuration.set(initialPath + ".specifics.ArmorStandToTalkUUID", null);
        }
    }

    @Override
    public void load(FileConfiguration configuration, String initialPath) {
        npcToTalkID = configuration.getInt(initialPath + ".specifics.NPCtoTalkID", -1);
        if (npcToTalkID != -1) {
            armorStandUUID = null;
        } else {
            final String armorStandUUIDString = configuration.getString(initialPath + ".specifics.ArmorStandToTalkUUID");
            if (armorStandUUIDString != null) {
                armorStandUUID = UUID.fromString(armorStandUUIDString);
            } else {
                armorStandUUID = null;
            }

        }
    }

    @Override
    public void onObjectiveUnlock(final ActiveObjective activeObjective, final boolean unlockedDuringPluginStartupQuestLoadingProcess) {
    }

    @Override
    public void onObjectiveCompleteOrLock(final ActiveObjective activeObjective, final boolean lockedOrCompletedDuringPluginStartupQuestLoadingProcess, final boolean completed) {
    }

    public final int getNPCtoTalkID() {
        return npcToTalkID;
    }

    public void setNPCtoTalkID(final int npcToTalkID) {
        this.npcToTalkID = npcToTalkID;
    }

    public final UUID getArmorStandUUID() {
        return armorStandUUID;
    }

    public void setArmorStandUUID(final UUID armorStandUUID) {
        this.armorStandUUID = armorStandUUID;
    }
}