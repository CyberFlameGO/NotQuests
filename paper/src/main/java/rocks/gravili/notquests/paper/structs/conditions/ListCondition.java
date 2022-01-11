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

package rocks.gravili.notquests.paper.structs.conditions;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import rocks.gravili.notquests.paper.NotQuests;
import rocks.gravili.notquests.paper.commands.arguments.variables.ListVariableValueArgument;
import rocks.gravili.notquests.paper.structs.QuestPlayer;
import rocks.gravili.notquests.paper.structs.variables.Variable;
import rocks.gravili.notquests.paper.structs.variables.VariableDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ListCondition extends Condition {

    private String variableName;
    private String operator;
    private String expression;

    private HashMap<String, String> additionalStringArguments;


    public final String getOperator(){
        return operator;
    }
    public void setOperator(final String operator){
        this.operator = operator;
    }

    public final String getVariableName(){
        return variableName;
    }

    public void setVariableName(final String variableName){
        this.variableName = variableName;
    }

    public void setExpression(final String expression){
        this.expression = expression;
    }

    public final String getExpression(){
        return expression;
    }


    public ListCondition(NotQuests main) {
        super(main);
        additionalStringArguments = new HashMap<>();
    }


    public final String[] evaluateExpression(final QuestPlayer questPlayer){
        return getExpression().split(",");
    }

    @Override
    public String checkInternally(final QuestPlayer questPlayer) {
        String[] listRequirement = evaluateExpression(questPlayer);


        Variable<?> variable = main.getVariablesManager().getVariableFromString(variableName);

        if(variable == null){
            return "<ERROR>Error: variable </highlight>" + variableName + "<highlight> not found. Report this to the Server owner.";
        }

        if(additionalStringArguments != null && !additionalStringArguments.isEmpty()){
            variable.setAdditionalStringArguments(additionalStringArguments);
        }

        Object value = variable.getValue(questPlayer.getPlayer(), questPlayer);

        if(value == null){
            return "<YELLOW>You don't have any " + variable.getPlural() + "!";
        }

        if(getOperator().equalsIgnoreCase("equals")){
            String[] stringArray;
            if(value instanceof String[] stringArray1){
                stringArray = stringArray1;
            }else if(value instanceof ArrayList<?> arrayList){
                stringArray = arrayList.toArray(new String[0]);
            }else{
                stringArray = (String[])value;
            }
            if(listRequirement.length != stringArray.length){
                return "<YELLOW>The " + variable.getPlural() + " need to be :<highlight>" + Arrays.toString(listRequirement) + "</highlight>.";
            }else{
                for (int i = 0; i < listRequirement.length; i++)
                {
                    if (!listRequirement[i].equals(stringArray[i])) {
                        return "<YELLOW>The " + variable.getPlural() + " need to be :<highlight>" + Arrays.toString(listRequirement) + "</highlight>.";
                    }
                }
            }
        }else if(getOperator().equalsIgnoreCase("equalsIgnoreCase")){
            String[] stringArray;
            if(value instanceof String[] stringArray1){
                stringArray = stringArray1;
            }else if(value instanceof ArrayList<?> arrayList){
                stringArray = arrayList.toArray(new String[0]);
            }else{
                stringArray = (String[])value;
            }
            if(listRequirement.length != stringArray.length){
                return "<YELLOW>The " + variable.getPlural() + " need to be :<highlight>" + Arrays.toString(listRequirement) + "</highlight>.";
            }else{
                for (int i = 0; i < listRequirement.length; i++)
                {
                    if (!listRequirement[i].equalsIgnoreCase(stringArray[i])) {
                        return "<YELLOW>The " + variable.getPlural() + " need to be :<highlight>" + Arrays.toString(listRequirement) + "</highlight>.";
                    }
                }
            }
        }else if(getOperator().equalsIgnoreCase("contains")){
            String[] stringArray;
            if(value instanceof String[] stringArray1){
                stringArray = stringArray1;
            }else if(value instanceof ArrayList<?> arrayList){
                stringArray = arrayList.toArray(new String[0]);
            }else{
                stringArray = (String[])value;
            }

            for (String s : listRequirement) {
                if (Arrays.stream(stringArray).noneMatch(s::equals)) {
                    return "<YELLOW>The " + variable.getPlural() + " need to contain :<highlight>" + Arrays.toString(listRequirement) + "</highlight>.";
                }
            }
        }else if(getOperator().equalsIgnoreCase("containsIgnoreCase")){
            String[] stringArray;
            if(value instanceof String[] stringArray1){
                stringArray = stringArray1;
            }else if(value instanceof ArrayList<?> arrayList){
                stringArray = arrayList.toArray(new String[0]);
            }else{
                stringArray = (String[])value;
            }

            for (String s : listRequirement) {
                if (Arrays.stream(stringArray).noneMatch(s::equalsIgnoreCase)) {
                    return "<YELLOW>The " + variable.getPlural() + " need to contain :<highlight>" + Arrays.toString(listRequirement) + "</highlight>.";
                }
            }
        }else{
            return "<ERROR>Error: variable operator <highlight>" + getOperator() + "</highlight> is invalid. Report this to the Server owner.";
        }

        return "";
    }

    @Override
    public void save(FileConfiguration configuration, final String initialPath) {
        configuration.set(initialPath + ".specifics.variableName", getVariableName());
        configuration.set(initialPath + ".specifics.operator", getOperator());
        configuration.set(initialPath + ".specifics.expression", getExpression());

        for(final String key : additionalStringArguments.keySet()){
            configuration.set(initialPath + ".specifics.additionalStrings." + key, additionalStringArguments.get(key));
        }
    }

    @Override
    public void load(FileConfiguration configuration, String initialPath) {
        this.variableName = configuration.getString(initialPath + ".specifics.variableName");
        this.operator = configuration.getString(initialPath + ".specifics.operator", "");
        this.expression = configuration.getString(initialPath + ".specifics.expression", "");

        final ConfigurationSection additionalStringsConfigurationSection = configuration.getConfigurationSection(initialPath + ".specifics.additionalStrings");
        if (additionalStringsConfigurationSection != null) {
            for (String key : additionalStringsConfigurationSection.getKeys(false)) {
                additionalStringArguments.put(key, configuration.getString(initialPath + ".specifics.additionalStrings." + key, ""));
            }
        }
    }

    @Override
    public void deserializeFromSingleLineString(ArrayList<String> arguments) {
        this.variableName = arguments.get(0);

        this.operator = arguments.get(1);
        setExpression(arguments.get(2));

        if(arguments.size() >= 4){

            Variable<?> variable = main.getVariablesManager().getVariableFromString(variableName);
            if(variable == null || !variable.isCanSetValue() || variable.getVariableDataType() != VariableDataType.LIST){
                return;
            }

            int counter = 0;
            for (String argument : arguments){
                counter++;
                if(counter >= 4){
                    additionalStringArguments.put(variable.getRequiredStrings().get(counter-4).getName(), argument);
                }
            }
        }
    }

    @Override
    public String getConditionDescription(Player player, Object... objects) {
        //description += "\n<GRAY>--- Will quest points be deducted?: No";

        if(getOperator().equalsIgnoreCase("equals")){
            return "<GRAY>-- " + variableName + " needs to be equal " + Arrays.toString(evaluateExpression(main.getQuestPlayerManager().getOrCreateQuestPlayer(player.getUniqueId()))) + "</GRAY>";
        }else if(getOperator().equalsIgnoreCase("equalsIgnoreCase")){
            return "<GRAY>-- " + variableName + " needs to be equal " + Arrays.toString(evaluateExpression(main.getQuestPlayerManager().getOrCreateQuestPlayer(player.getUniqueId()))) + " (case-insensitive)</GRAY>";
        }else if(getOperator().equalsIgnoreCase("contains")){
            return "<GRAY>-- " + variableName + " needs to be contain " + Arrays.toString(evaluateExpression(main.getQuestPlayerManager().getOrCreateQuestPlayer(player.getUniqueId()))) + "</GRAY>";
        }else if(getOperator().equalsIgnoreCase("containsIgnoreCase")){
            return "<GRAY>-- " + variableName + " needs to be contain " + Arrays.toString(evaluateExpression(main.getQuestPlayerManager().getOrCreateQuestPlayer(player.getUniqueId()))) + " (case-insensitive)</GRAY>";
        }
        return "<GRAY>Error: invalid expression.</GRAY>";
    }



    public static void handleCommands(NotQuests main, PaperCommandManager<CommandSender> manager, Command.Builder<CommandSender> builder, ConditionFor conditionFor) {
        for(String variableString : main.getVariablesManager().getVariableIdentifiers()){

            Variable<?> variable = main.getVariablesManager().getVariableFromString(variableString);

            if(variable == null || variable.getVariableDataType() != VariableDataType.BOOLEAN){
                continue;
            }

            manager.command(main.getVariablesManager().registerVariableCommands(variableString, builder)
                    .argument(StringArgument.<CommandSender>newBuilder("operator").withSuggestionsProvider((context, lastString) -> {
                        ArrayList<String> completions = new ArrayList<>();
                        completions.add("equals");
                        completions.add("equalsIgnoreCase");
                        completions.add("contains");
                        completions.add("containsIgnoreCase");


                        final List<String> allArgs = context.getRawInput();
                        main.getUtilManager().sendFancyCommandCompletion(context.getSender(), allArgs.toArray(new String[0]), "[List Operator]", "[...]");

                        return completions;
                    }).build(), ArgumentDescription.of("List operator."))
                    .argument(ListVariableValueArgument.newBuilder("expression", main), ArgumentDescription.of("Expression"))
                    .handler((context) -> {

                        final String expression = context.get("expression");
                        final String operator = context.get("operator");

                        ListCondition listCondition = new ListCondition(main);

                        listCondition.setExpression(expression);
                        listCondition.setOperator(operator);
                        listCondition.setVariableName(variableString);


                        HashMap<String, String> additionalStringArguments = new HashMap<>();
                        for(StringArgument<CommandSender> stringArgument : variable.getRequiredStrings()){
                            additionalStringArguments.put(stringArgument.getName(), context.get(stringArgument.getName()));
                        }
                        listCondition.setAdditionalStringArguments(additionalStringArguments);

                        main.getConditionsManager().addCondition(listCondition, context);
                    })
            );


        }


    }

    private void setAdditionalStringArguments(HashMap<String, String> additionalStringArguments) {
        this.additionalStringArguments = additionalStringArguments;
    }


}