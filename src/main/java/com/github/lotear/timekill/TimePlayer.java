package com.github.lotear.timekill;

import com.github.noonmaru.tap.ChatType;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class TimePlayer
{
    private final TimeKillProcess timeKillProcess;

    private Player bukkitPlayer;

    private String name;

    private int remainTick;

    private TimePlayer enemy;

    private TimePlayer target;

    private TimeEquipment timeEquipment;

    public TimePlayer(Player onlinePlayer, TimeKillProcess timeKillProcess)
    {
        this.timeKillProcess = timeKillProcess;
        this.remainTick = Config.initTick;
        setPlayer(onlinePlayer);

        initTime();
    }

    void setPlayer(Player onlinePlayer)
    {
        if (onlinePlayer != null)
        {
            this.bukkitPlayer = onlinePlayer;
            this.name = onlinePlayer.getName();

        }
        else
        {
            this.bukkitPlayer = null;
        }
    }

    public int getRemainTick()
    {
        return remainTick;
    }

    public TimePlayer getTarget()
    {
        return target;
    }

    public void onDeath(TimePlayer killer)
    {
        if (enemy == killer)
        {
            enemy.remainTick += this.remainTick;
            eliminated();
        }
        else
        {
            String message = "누군가의 실수로 시간이 증가했습니다!";
            for (TimePlayer onlineTimePlayer : timeKillProcess.getOnlineTimePlayers())
            {
                if (onlineTimePlayer.isAlive() && onlineTimePlayer != killer)
                {
                    onlineTimePlayer.remainTick += Config.bonusTick;
                    onlineTimePlayer.bukkitPlayer.sendMessage(message);
                    killer.remainTick -= Config.bonusTick;
                }
            }
        }

    }

    public void onUpdate()
    {
        int sec = remainTick / 20;
        Packet.INFO.chat(String.format(ChatColor.GREEN + "남은 시간 : %02d:%02d", sec / 60, sec % 60), ChatType.GAME_INFO).sendTo(this.bukkitPlayer);
        remainTick--;

        if (remainTick <= 0)
        {
            eliminated();
            return;
        }
        else
        {
            updateItem();
        }

        Player targetPlayer = this.target.bukkitPlayer;

        if (targetPlayer != null)
        {
            this.bukkitPlayer.setCompassTarget(targetPlayer.getLocation());
        }
    }

    private void initTime()
    {
        updateItem();

        PlayerInventory inv = bukkitPlayer.getInventory();
        ItemStack timeboost = new ItemStack(Material.GHAST_TEAR);
        ItemMeta boostim = timeboost.getItemMeta();
        boostim.setDisplayName("시간의 결정");
        timeboost.setItemMeta(boostim);
        inv.setItem(8, new ItemStack(Material.COMPASS));
        inv.setItem(9, new ItemStack(Material.ARROW, 64));
        inv.addItem(timeboost);
    }

    private void updateItem()
    {
        TimeEquipment timeEquipment = timeKillProcess.getEquipmentManager().getEquipmentByTick(remainTick);

        if (this.timeEquipment != timeEquipment)
        {
            this.timeEquipment = timeEquipment;
            PlayerInventory inv = bukkitPlayer.getInventory();
            inv.setHelmet(timeEquipment.getHelmet());
            inv.setChestplate(timeEquipment.getChest());
            inv.setLeggings(timeEquipment.getLeg());
            inv.setBoots(timeEquipment.getBoots());
            inv.setItem(0, timeEquipment.getSword());
            inv.setItem(1, timeEquipment.getBow());

            bukkitPlayer.sendTitle("", ChatColor.AQUA + "장비가 변화합니다....", 5, 50, 5);
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.BLOCK_ANVIL_USE, 20, 1);
        }
    }

    private void eliminated()
    {
        target.enemy = this.enemy;
        enemy.target = this.target;
        this.enemy = this.target = null;
        this.bukkitPlayer.setGameMode(GameMode.SPECTATOR);
        String deathMessage = ChatColor.RED + name + " 탈락!";
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
        {
            onlinePlayer.sendTitle(" ", deathMessage, 5, 50, 5);
        }
        Bukkit.broadcastMessage(deathMessage);
        timeKillProcess.removeSurvival(this);
    }

    String getName()
    {
        return name;
    }

    public boolean isAlive()
    {
        return this.target != null;
    }

    public Player getBukkitPlayer()
    {
        return bukkitPlayer;
    }

    void link(TimePlayer target, TimePlayer enemy)
    {
        this.target = target;
        this.enemy = enemy;
    }

    public void decreaseRemainTick(int i)
    {
        this.remainTick -= i;
    }
}
