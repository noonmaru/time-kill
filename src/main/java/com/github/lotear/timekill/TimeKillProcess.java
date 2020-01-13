package com.github.lotear.timekill;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TimeKillProcess
{
    private final Map<UUID, TimePlayer> players = new HashMap<>();

    private final Map<Player, TimePlayer> onlinePlayers = new IdentityHashMap<>();

    private final EquipmentManager equipmentManager = new EquipmentManager();

    private final EventListener listener;

    private final BukkitTask task;

    private final TimeKillPlugin plugin;

    private final Set<TimePlayer> survivals = new HashSet<>();

    public TimeKillProcess(TimeKillPlugin plugin)
    {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
        {
            GameMode mode = onlinePlayer.getGameMode();
            if (mode == GameMode.ADVENTURE || mode == GameMode.SURVIVAL)
            {
                TimePlayer timeplayer = new TimePlayer(onlinePlayer, this);
                players.put(onlinePlayer.getUniqueId(), timeplayer);
                onlinePlayers.put(onlinePlayer, timeplayer);
            }
        }

        listener = new EventListener(this);

        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::onUpdate, 0L, 1L);
        this.survivals.addAll(this.players.values());

        ArrayList<TimePlayer> players = new ArrayList<>(this.survivals);
        Collections.shuffle(players);

        for (int i = 0, size = players.size(); i < size; i++)
        {
            TimePlayer target = players.get((i + 1) % size);
            TimePlayer enemy = players.get((size + i - 1) % size);

            TimePlayer timePlayer = players.get(i);
            timePlayer.link(target, enemy);
        }
    }

    void removeSurvival(TimePlayer player)
    {
        this.survivals.remove(player);
    }

    public void unregister()
    {
        task.cancel();
        HandlerList.unregisterAll(this.listener);

        //게임 종료시 인벤토리 클리어
        for (TimePlayer time : onlinePlayers.values())
        {
            time.getBukkitPlayer().getInventory().clear();
        }
    }

    private void onUpdate()
    {
        int size = survivals.size();

        if (size == 0 || size == 1) // 생존자 없음 무승부
        {
            plugin.stopProcess();

            String message = size == 0 ? ChatColor.GRAY + "무승부!" : ChatColor.GOLD + survivals.iterator().next().getName() + "님이 승리!";

            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            {
                onlinePlayer.sendTitle("게임 종료!", message, 5, 100, 5);
            }
        }

        for (TimePlayer timePlayer : onlinePlayers.values())
        {
            if (timePlayer.isAlive())
            {
                timePlayer.onUpdate();
            }
        }
    }

    public void onJoin(Player p)
    {
        TimePlayer timePlayer = players.get(p.getUniqueId());
        if (timePlayer != null)
        {
            timePlayer.setPlayer(p);
            onlinePlayers.put(p, timePlayer);
        }
        else
        {
            GameMode mode = p.getGameMode();
            if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE)
            {
                p.setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    public void onQuit(Player p)
    {
        TimePlayer timePlayer = onlinePlayers.remove(p);
        if (timePlayer != null)
        {
            timePlayer.setPlayer(null);
        }
    }

    public TimePlayer getTimePlayer(Player player)
    {
        return onlinePlayers.get(player);
    }

    public Collection<TimePlayer> getOnlineTimePlayers()
    {
        return onlinePlayers.values();
    }

    public EquipmentManager getEquipmentManager()
    {
        return equipmentManager;
    }
}
