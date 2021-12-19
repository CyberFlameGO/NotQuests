package rocks.gravili.notquests.Events.hooks;

import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import rocks.gravili.notquests.NotQuests;
import rocks.gravili.notquests.Structs.ActiveObjective;
import rocks.gravili.notquests.Structs.ActiveQuest;
import rocks.gravili.notquests.Structs.Objectives.hooks.Towny.TownyNationReachTownCountObjective;
import rocks.gravili.notquests.Structs.Objectives.hooks.Towny.TownyReachResidentCountObjective;
import rocks.gravili.notquests.Structs.QuestPlayer;

public class TownyEvents implements Listener {
    private final NotQuests main;

    public TownyEvents(NotQuests main) {
        this.main = main;
    }


    @EventHandler
    public void onTownAddToNation(NationAddTownEvent e) {
        for (final Resident resident : e.getNation().getResidents()) {
            final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(resident.getUUID());
            if (questPlayer != null) {
                if (questPlayer.getActiveQuests().size() > 0) {
                    for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                        for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                            if (activeObjective.isUnlocked()) {
                                if (activeObjective.getObjective() instanceof TownyNationReachTownCountObjective) {
                                    activeObjective.addProgress(1);
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

    @EventHandler
    public void onTownRemoveFromNation(NationAddTownEvent e) {
        for (final Resident resident : e.getNation().getResidents()) {
            final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(resident.getUUID());
            if (questPlayer != null) {
                if (questPlayer.getActiveQuests().size() > 0) {
                    for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                        for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                            if (activeObjective.isUnlocked()) {
                                if (activeObjective.getObjective() instanceof TownyNationReachTownCountObjective) {
                                    activeObjective.removeProgress(1, true);
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

    @EventHandler
    public void onResidentAdd(TownAddResidentEvent e) {
        for (final Resident resident : e.getTown().getResidents()) {
            final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(resident.getUUID());
            if (questPlayer != null) {
                if (questPlayer.getActiveQuests().size() > 0) {
                    for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                        for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                            if (activeObjective.isUnlocked()) {
                                if (activeObjective.getObjective() instanceof TownyReachResidentCountObjective) {
                                    activeObjective.addProgress(1);
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
    @EventHandler
    public void onResidentRemove(TownRemoveResidentEvent e) {
        for (final Resident resident : e.getTown().getResidents()) {
            final QuestPlayer questPlayer = main.getQuestPlayerManager().getQuestPlayer(resident.getUUID());
            if (questPlayer != null) {
                if (questPlayer.getActiveQuests().size() > 0) {
                    for (final ActiveQuest activeQuest : questPlayer.getActiveQuests()) {
                        for (final ActiveObjective activeObjective : activeQuest.getActiveObjectives()) {
                            if (activeObjective.isUnlocked()) {
                                if (activeObjective.getObjective() instanceof TownyReachResidentCountObjective) {
                                    activeObjective.removeProgress(1, true);
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