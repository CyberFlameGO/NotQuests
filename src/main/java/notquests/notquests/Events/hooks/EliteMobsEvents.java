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

package notquests.notquests.Events.hooks;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import notquests.notquests.NotQuests;
import notquests.notquests.Structs.ActiveObjective;
import notquests.notquests.Structs.ActiveQuest;
import notquests.notquests.Structs.Objectives.hooks.KillEliteMobsObjective;
import notquests.notquests.Structs.QuestPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;

public class EliteMobsEvents implements Listener {
    private final NotQuests main;

    public EliteMobsEvents(NotQuests main) {
        this.main = main;
    }

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        final EliteEntity eliteMob = event.getEliteEntity();

        for (final Player player : eliteMob.getDamagers().keySet()) {
            player.sendMessage("§eEliteMob Name: §f" + eliteMob.getName());
            final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(player.getUniqueId());
            if (questPlayer != null) {
                if (questPlayer.getActiveQuests().size() > 0) {
                    for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                        for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                            if (activeObjective.getObjective() instanceof KillEliteMobsObjective killEliteMobsObjective) {
                                if (activeObjective.isUnlocked()) {

                                    //Check conditions

                                    if (!killEliteMobsObjective.getEliteMobToKillContainsName().isBlank()) {
                                        boolean foundOneNotFitting = false;
                                        for (final String namePart : killEliteMobsObjective.getEliteMobToKillContainsName().toLowerCase(Locale.ROOT).split(" ")) {
                                            if (!eliteMob.getName().toLowerCase(Locale.ROOT).contains(namePart)) {
                                                foundOneNotFitting = true;
                                            }
                                        }
                                        if (foundOneNotFitting) {
                                            continue;
                                        }
                                    }

                                    if (killEliteMobsObjective.getMinimumLevel() >= 0 && eliteMob.getLevel() < killEliteMobsObjective.getMinimumLevel()) {
                                        continue;
                                    }

                                    if (killEliteMobsObjective.getMaximumLevel() >= 0 && eliteMob.getLevel() > killEliteMobsObjective.getMaximumLevel()) {
                                        continue;
                                    }

                                    if (killEliteMobsObjective.getMinimumDamagePercentage() != -1 && eliteMob.getDamagers().get(player) < killEliteMobsObjective.getMinimumDamagePercentage()) {
                                        continue;
                                    }

                                    if (!killEliteMobsObjective.getSpawnReason().isBlank() && !eliteMob.getSpawnReason().toString().toLowerCase(Locale.ROOT).equalsIgnoreCase(killEliteMobsObjective.getSpawnReason().toLowerCase(Locale.ROOT))) {
                                        continue;
                                    }

                                    activeObjective.addProgress(1, -1);

                                }

                            }
                        }
                        activeQuest.removeCompletedObjectives(true);
                    }
                    questPlayer.removeCompletedQuests();
                }
            }
        }
    }

}