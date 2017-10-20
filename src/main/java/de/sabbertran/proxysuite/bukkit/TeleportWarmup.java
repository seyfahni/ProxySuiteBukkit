package de.sabbertran.proxysuite.bukkit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportWarmup extends BukkitRunnable {
    private Location destination;
    private Player player;
    private ProxySuiteBukkit main;

    public TeleportWarmup(Location destination, Player player, ProxySuiteBukkit main) {
        this.destination = destination;
        this.player = player;
        this.main = main;
    }


    @Override
    public void run() {
        main.teleportRequest(player, destination);
        main.getPendingWarmupTeleports().remove(player.getName());
    }
}
