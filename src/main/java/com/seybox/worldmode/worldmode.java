package com.seybox.worldmode;

import com.seybox.worldmode.util.dataStorageHelper;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

public class worldmode extends JavaPlugin implements Listener {
    private dataStorageHelper worldmodeHelper;

    private static final String worldmodeDataStorage = "worldmodeList.yml";
    private static final String worldmodeWhiteListStorage = "worldmodeWhiteList.yml";

    private HashMap<String,String> backList;

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
        this.worldmodeHelper.createDataStorage(worldmodeWhiteListStorage);
        this.backList = new HashMap<String, String>();
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
                    if(args[0].equals("creative") || args[0].equals("survival")) {
                        this.worldmodeHelper.setDataStorage(worldmodeDataStorage, world.getName(), args[0]);
                        player.sendMessage(ChatColor.RED + "[worldmode] Set world " + ChatColor.LIGHT_PURPLE + world.getName() + ChatColor.RED + " to " + ChatColor.BLUE + args[0] + ChatColor.RED + " mode sucessfully.");
                        return true;
                    }
                    else if(args[0].equals("list")){
                        for (World w: Bukkit.getWorlds()) {
                            String wm = (String)this.worldmodeHelper.getDataStorage(worldmodeDataStorage,w.getName());
                            if(wm!=null) player.sendMessage(ChatColor.RED + "[worldmode] " + w.getName() + " " + ChatColor.BLUE + wm);
                        }
                        return true;
                    }
                    else if(args[0].equals("addwhitelist")){
                        if(args[1]!=null){
                            if(this.worldmodeHelper.getDataStorage(worldmodeWhiteListStorage,world.getName())==null){
                                this.worldmodeHelper.setDataStorage(worldmodeWhiteListStorage,world.getName(),args[1]);
                            } else {
                                try {
                                    String wl = (String) this.worldmodeHelper.getDataStorage(worldmodeWhiteListStorage, world.getName());
                                    String[] lst = wl.split("\\.");
                                    String[] nlst = new String[lst.length+1];
                                    System.arraycopy(lst,0,nlst,0,lst.length);
                                    nlst[lst.length] = args[1];
                                    String rec = "";
                                    for (String s : nlst) {
                                        if (s.length() > 0) rec += s + ".";
                                    }
                                    this.worldmodeHelper.setDataStorage(worldmodeWhiteListStorage, world.getName(), rec);
                                    player.sendMessage(ChatColor.RED + "[worldmode] 白名单玩家：" + rec);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    getLogger().info(e.getMessage());
                                }
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "[worldmode] 需要输入用户名");
                            return true;
                        }
                    }
                    else if(args[0].equals("delwhitelist")) {
                        if(args[1]!=null){
                            if(this.worldmodeHelper.getDataStorage(worldmodeWhiteListStorage,world.getName())!=null){
                                try {
                                    String wl = (String) this.worldmodeHelper.getDataStorage(worldmodeWhiteListStorage, world.getName());
                                    String[] lst = wl.split("\\.");
                                    String rec = "";
                                    for (String s : lst) {
                                        if (s.length() > 0 && !s.equals(args[1])) rec += s + ".";
                                    }
                                    this.worldmodeHelper.setDataStorage(worldmodeWhiteListStorage, world.getName(), rec);
                                    player.sendMessage(ChatColor.RED + "[worldmode] 白名单玩家：" + rec);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    getLogger().info(e.getMessage());
                                }
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED + "[worldmode] 本世界不存在白名单");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "[worldmode] 需要输入用户名");
                            return true;
                        }
                    }
                    else if(args[0].equals("rmwhitelist")){
                        this.worldmodeHelper.setDataStorage(worldmodeWhiteListStorage,world.getName(),null);
                        player.sendMessage(ChatColor.RED + "[worldmode] 已清除本世界白名单");
                        return true;
                    }
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
                        if(mode.equals("creative") &&  player.getGameMode()!=GameMode.CREATIVE && !player.isOp()) player.setGameMode(GameMode.CREATIVE);
                        if(mode.equals("survival") &&  player.getGameMode()!=GameMode.SURVIVAL && !player.isOp()) player.setGameMode(GameMode.SURVIVAL);
                    }
                    String whitelist = (String)worldmodeHelper.getDataStorage(worldmodeWhiteListStorage,player.getWorld().getName());
                    if(whitelist!=null){
                        String[] lst = whitelist.split("\\.");
                        boolean ok = false;
                        for (String s : lst) {
                            if(s!=null && s.equals(player.getName())){
                                ok = true;
                            }
                        }
                        if(!ok && !backList.containsKey(player.getName())){
                            backList.put(player.getName(),"back");
                            back2Main(player);
                        }
                    }
                }
            }
        }
        .runTaskTimer(this,10,10);
    }

    private void back2Main(final Player p){
        p.sendMessage(ChatColor.RED + "[worldmode] 你不在本世界白名单内，十秒后将返回主城");
        new BukkitRunnable(){
            public void run(){
                Bukkit.getServer()
                        .dispatchCommand(
                                Bukkit.getConsoleSender(),
                                "world tp " + p.getName() + " main");
                p.sendMessage(ChatColor.RED + "[worldmode] 你已回到主城");
                backList.remove(p.getName());
            }
        }.runTaskLater(this,200);
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
