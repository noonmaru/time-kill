package com.github.lotear.timekill;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.github.noonmaru.tap.command.TabSupport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TimeKillPlugin extends JavaPlugin
{
    private ProtocolManager protocolManager;

    private TimeKillProcess process;

    @Override
    public void onLoad()
    {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onEnable()
    {
        saveDefaultConfig();

        Config.load(getConfig());

        getServer().getScheduler().runTaskTimer(this, new ConfigReloader(new File(getDataFolder(), "config.yml"), Config::load, getLogger()), 0, 1);

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT)
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {

            }
        });
    }

    @Override
    public void onDisable()
    {
        stopProcess();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
            return false;

        String sub = args[0];

        if ("start".equalsIgnoreCase(sub))
        {
            if (startProcess())
            {
                sender.sendMessage("게임을 시작했습니다.");
            }
            else
            {
                sender.sendMessage("게임이 이미 진행중입니다.");
            }
        }
        else if ("stop".equalsIgnoreCase(sub))
        {
            if (stopProcess())
            {
                sender.sendMessage("게임을 종료했습니다.");
            }
            else
            {
                sender.sendMessage("게임 진행중이 아닙니다.");
            }
        }
        else
        {
            sender.sendMessage("알 수 없는 명령입니다.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            return TabSupport.complete(Arrays.asList("start", "stop"), args[0]);
        }

        return Collections.emptyList();
    }

    public boolean startProcess()
    {
        if (process != null)
        {
            return false;
        }

        process = new TimeKillProcess(this);
        return true;
    }

    public boolean stopProcess()
    {
        if (this.process != null)
        {
            process.unregister();
            process = null;
            return true;
        }

        return false;
    }
}
