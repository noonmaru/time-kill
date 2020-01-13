package com.github.lotear.timekill;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EventListener implements Listener
{
    private final TimeKillProcess timeKillProcess;

    public EventListener(TimeKillProcess timeKillProcess)
    {
        this.timeKillProcess = timeKillProcess;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        Player p = ev.getPlayer();
        timeKillProcess.onJoin(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev)
    {
        Player p = ev.getPlayer();
        timeKillProcess.onQuit(p);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent ev)
    {
        Player victim = ev.getEntity();
        Player killer = victim.getKiller();

        if (killer != null)
        {
            TimePlayer timekiller = timeKillProcess.getTimePlayer(killer);
            TimePlayer timevictim = timeKillProcess.getTimePlayer(victim);
            if (timekiller != null && timevictim != null && timekiller.isAlive() && timevictim.isAlive())
            {
                timevictim.onDeath(timekiller);
            }
        }
    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent ev)
    {
        Player p = ev.getPlayer();
        Action action = ev.getAction();
        ItemStack useItem = ev.getPlayer().getInventory().getItemInMainHand();
        try
        {
            if (useItem.getType().equals(Material.GHAST_TEAR))
            {
                if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK))
                {
                    p.sendTitle("§a시간 §b가속!!!", " ", 5, 50, 5);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2), false);
                    p.getInventory().remove(useItem);
                    TimePlayer timePlayer = timeKillProcess.getTimePlayer(p);
                    timePlayer.decreaseRemainTick(20 * 30);

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            ItemStack timeBoost = new ItemStack(Material.GHAST_TEAR);
                            ItemMeta boostim = timeBoost.getItemMeta();
                            boostim.setDisplayName("시간의 결정");
                            timeBoost.setItemMeta(boostim);
                            p.getInventory().addItem(timeBoost);
                        }
                    }.runTaskLater(TimeKillPlugin.getInstance(), 20 * 20L);
                }
            }
            else if (useItem.getType().equals(Material.BONE))
            {
                if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK))
                {
                    double horizontal = 3.0D;
                    double vertical = 1.0;
                    double yaw = p.getLocation().getYaw();
                    double yawAngle = Math.toRadians(yaw);
                    double x = -Math.sin(yawAngle) * horizontal;
                    double y = vertical;
                    double z = Math.cos(yawAngle) * horizontal;
                    p.setVelocity(new Vector(x, y, z));
                    int amount = useItem.getAmount();
                    useItem.setAmount(amount - 1);
                }
            }

        }
        catch (NullPointerException e)
        {

        }
    }

    @EventHandler
    public void onPlayerDropCancel(EntityDamageEvent ev)
    {
        Player player = (Player) ev.getEntity();

        if (ev.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            ev.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        TimePlayer timePlayer = timeKillProcess.getTimePlayer(player);

        if (timePlayer != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        TimePlayer timePlayer = timeKillProcess.getTimePlayer(player);

        if (timePlayer != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventorySwap(PlayerSwapHandItemsEvent event)
    {
        Player player = event.getPlayer();
        TimePlayer timePlayer = timeKillProcess.getTimePlayer(player);

        if (timePlayer != null)
            event.setCancelled(true);
    }
}
