package me.merunko.holocraft.Command;

import me.merunko.holocraft.Configuration.MainConfiguration;
import me.merunko.holocraft.GUI.RewardsGUI;
import me.merunko.holocraft.GUI.SubmitterGUI;
import me.merunko.holocraft.Leaderboard.LeaderboardConfiguration;
import me.merunko.holocraft.Leaderboard.LeaderboardUpdater;

import me.merunko.holocraft.Rewards.RewardsConfiguration;
import net.Indyuce.mmoitems.MMOItems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

public class Command implements CommandExecutor {


    LeaderboardConfiguration leaderboardConfiguration;
    MainConfiguration config;
    RewardsConfiguration reward;
    JavaPlugin plugin;
    Logger logger;


    public Command(RewardsConfiguration reward, MainConfiguration config, LeaderboardConfiguration leaderboardConfiguration, JavaPlugin plugin, Logger logger) {
        this.leaderboardConfiguration = leaderboardConfiguration;
        this.config = config;
        this.reward = reward;
        this.plugin = plugin;
        this.logger = logger;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {


        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (commandSender.hasPermission("submitter.reload")) {
                Player player = (Player) commandSender;
                config.load();
                reward.load();
                commandSender.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + "Submitter configuration, rewards are reloaded.");
                logger.info(player + " " + "reloaded the configuration.");
            } else {
                commandSender.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "You don't have permission to run this command!");
            }
            return true;


        } else if (args.length == 2 && args[0].equalsIgnoreCase("open") && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            String targetPlayerName = args[1];
            Player targetPlayer = player.getServer().getPlayer(targetPlayerName);

            if (targetPlayer != null) {
                SubmitterGUI gui = new SubmitterGUI(config);
                targetPlayer.openInventory(gui.createDragDropInventory());
            } else {
                player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "Player not found: " + targetPlayerName);
            }
            return true;


        } else if (args.length == 1 && args[0].equalsIgnoreCase("mmoinspect") && commandSender instanceof Player) {
            if (commandSender.hasPermission("submitter.reload")) {
                Player player = (Player) commandSender;
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();

                if (mainHandItem.getType().equals(Material.AIR)) {
                    player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "Your main hand is empty.");
                } else {
                    if (config.getMMOItemsEnabled()) {
                        String mmoItemId = MMOItems.getID((mainHandItem));
                        String mmoTypeName = MMOItems.getTypeName(mainHandItem);
                        player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + "MMOItems ID: " + ChatColor.GOLD + mmoItemId);
                        player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + "MMOItems Type: " + ChatColor.GOLD + mmoTypeName);
                    } else {
                        player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "MMOItems plugin is not enabled.");
                    }
                }
                return true;
            }


        } else if (args.length == 1 && args[0].equalsIgnoreCase("inspect") && commandSender instanceof Player) {
            if (commandSender.hasPermission("submitter.inspect")) {
                Player player = (Player) commandSender;
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                Material itemType = mainHandItem.getType();
                String mmoItemType = MMOItems.getTypeName(mainHandItem);
                String mmoItemId = MMOItems.getID(mainHandItem);

                if (mainHandItem.getType().equals(Material.AIR)) {
                    player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "Your main hand is empty.");
                } else {
                    if (mmoItemType != null && mmoItemId != null) {
                        int value = config.getMMOItemValue(mmoItemType, mmoItemId);
                        if (value > 0) {
                            value = value * mainHandItem.getAmount();
                            player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + config.getPointName() + ": " + ChatColor.GOLD + value);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + config.getPointName() + ": " + ChatColor.GOLD + "0");
                        }
                    } else {
                        int points = config.getMinecraftItemValue(itemType.toString());
                        if (points > 0) {
                            points = points * mainHandItem.getAmount();
                            player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + config.getPointName() + ": " + ChatColor.GOLD + points);
                        } else {
                            player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + config.getPointName() + ": " + ChatColor.GOLD + "0");
                        }
                    }
                    return true;
                }
            }


        } else if (args.length == 1 && args[0].equalsIgnoreCase("updatelboard") && commandSender instanceof Player) {
            if (commandSender.hasPermission("submitter.updatelboard")) {
                Player player = (Player) commandSender;
                LeaderboardUpdater updater = new LeaderboardUpdater();
                updater.updateLeaderboard(leaderboardConfiguration, logger);
                player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + "Leaderboard Updated.");
                logger.info(player + " " + "updated the leaderboard.");
                return true;
            }


        } else if (args.length == 3
                && args[0].equalsIgnoreCase("set")
                && args[1].equalsIgnoreCase("reward")
                && commandSender instanceof Player) {
            if (commandSender.hasPermission("submitter.setreward")) {
                Player player = (Player) commandSender;
                String arg2 = args[2];
                int rewardPosition;

                if (arg2.matches("top\\d+")) {
                    try {
                        rewardPosition = Integer.parseInt(arg2.substring(3));
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "Invalid reward position format.");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "Invalid reward position format.");
                    return true;
                }

                RewardsGUI rewardGUI = new RewardsGUI(reward, config);
                player.openInventory(rewardGUI.createRewardsSubmitter(arg2, rewardPosition));

                return true;
            }


        } else if (args.length == 4
                && args[0].equalsIgnoreCase("debug")
                && args[1].equalsIgnoreCase("get")
                && args[2].equalsIgnoreCase("reward")
                && commandSender instanceof Player) {
            if (commandSender.hasPermission("submitter.debug")) {
                int rewardPosition = Integer.parseInt(args[3].substring(3));
                List<ItemStack> rewardItems = reward.getRewardItems(rewardPosition);
                List<String> rewardCommands = reward.getRewardCommands(rewardPosition);
                for (String commands : rewardCommands) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commands);
                }

                if (!rewardItems.isEmpty()) {
                    Player player = (Player) commandSender;
                    for (ItemStack item : rewardItems) {
                        String itemName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) ? item.getItemMeta().getDisplayName() : reward.getFormattedDisplayName(item);
                        player.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.GREEN + "You received: " + ChatColor.GOLD + itemName + ChatColor.GREEN + ".");
                        player.getInventory().addItem(item);
                    }
                } else {
                    commandSender.sendMessage(ChatColor.GOLD + "[Submitter] " + ChatColor.RED + "No rewards found for position " + ChatColor.GOLD + rewardPosition + ChatColor.RED + ".");
                }
                return true;
            }
        }

        return false;
    }
}
