package com.seybox.worldmode;

import com.seybox.worldmode.util.dataStorageHelper;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class worldmode extends JavaPlugin implements Listener {
    private dataStorageHelper worldmodeHelper;

    private static final String worldmodeDataStorage = "worldmodeList.yml";

    @Override
    public void onDisable() {
        getLogger().info("worldmode Enabled");
        super.onDisable();
    }

    @Override
    public void onEnable() {
        getLogger().info("worldmode Disabled");
        super.onEnable();
        this.worldmodeHelper = new dataStorageHelper(this);
        this.worldmodeHelper.createDataStorage(worldmodeDataStorage);
        Bukkit.getPluginCommand("worldmode").setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this,this);
        runTasks();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("worldmode")){
            if(sender instanceof Player){
                Player player = Bukkit.getPlayer(sender.getName());
                World world = player.getWorld();
                if(player.isOp()) {
                    if(args[0].equals("creative") || args[0].equals("survival"))
                        this.worldmodeHelper.setDataStorage(worldmodeDataStorage,world.getName(),args[0]);
                    else return false;
                    return true;
                }
                player.sendMessage(ChatColor.RED+"[worldmode] 执行该指令需要OP权限");
                return true;
            }
        }
        return false;
    }

    private void runTasks(){
        new BukkitRunnable(){
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    String mode = (String)worldmodeHelper.getDataStorage(worldmodeDataStorage,player.getWorld().getName());
                    if(mode!=null){
                        if(mode.equals("creative") ||  player.getGameMode()!=GameMode.CREATIVE || !player.isOp()) player.setGameMode(GameMode.CREATIVE);
                        if(mode.equals("survival") ||  player.getGameMode()!=GameMode.SURVIVAL || !player.isOp()) player.setGameMode(GameMode.SURVIVAL);
                    }
                }
            }
        }
        .runTaskTimer(this,10,10);
    }

    /*@EventHandler
    private void onPlayerInteract(PlayerInteractEvent event){
        if(event.getMaterial() == Material.CHEST){
            event.getPlayer().sendMessage("chest");
            Chest chest = (Chest)event.getClickedBlock().getState();
            chest.getInventory().clear();
            for (ItemStack item :event.getPlayer().getInventory()) {
                chest.getInventory().addItem(item);
            }
            for(ItemStack item :event.getPlayer().getInventory())
        }
    }*/
}
