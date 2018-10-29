package de.sabbertran.proxysuite.bukkit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.sabbertran.proxysuite.bukkit.commands.BunCommand;
import de.sabbertran.proxysuite.bukkit.portals.PortalHandler;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

public class ProxySuiteBukkit extends JavaPlugin {

    private HashMap<String, BukkitTask> pendingWarmupTeleports;
    private HashMap<String, Location> pendingLocationTeleports;
    private HashMap<String, String> pendingPlayerTeleports;
    private HashMap<String, World> pendingSpawnTeleports;
    private PortalHandler portalHandler;
    private boolean requestPortals;

    private WorldEditPlugin worldEdit;
    private boolean worldguardLoaded;
    private Chat chat;

    @Override
    public void onEnable() {
        pendingWarmupTeleports = new HashMap<>();
        pendingLocationTeleports = new HashMap<>();
        pendingPlayerTeleports = new HashMap<>();
        pendingSpawnTeleports = new HashMap<>();

        portalHandler = new PortalHandler(this);

        getConfig().addDefault("ProxySuite.HideLoginMessage", true);
        getConfig().addDefault("ProxySuite.HideLogoutMessage", true);
        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "proxysuite:channel");
        getServer().getMessenger().registerIncomingPluginChannel(this, "proxysuite:channel", new PMessageListener(this));

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
            if (chatProvider != null)
                chat = chatProvider.getProvider();
        }

        worldguardLoaded = getServer().getPluginManager().getPlugin("WorldGuard") != null;

        getServer().getPluginManager().registerEvents(new Events(this), this);

        getCommand("bun").setExecutor(new BunCommand(this));

        Plugin we = getServer().getPluginManager().getPlugin("WorldEdit");
        if (we != null)
            worldEdit = (WorldEditPlugin) we;
        else
            getLogger().info("WorldEdit is not installed on your server! You need to install WorldEdit in order to create" +
                    " portals");

        requestPortals = true;

        getLogger().log(Level.INFO, "{0} {1} by {2} enabled", new Object[]{getDescription().getName(), getDescription().getVersion(), getDescription().getAuthors().get(0)});
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "{0} {1} by {2} disabled", new Object[]{getDescription().getName(), getDescription().getVersion(), getDescription().getAuthors().get(0)});
    }

    public void setPortal(Block b1, Block b2, Material material) {
        if (b1.getWorld() == b2.getWorld() && (b1.getX() == b2.getX() || b1.getY() == b2.getY())) {
            int topBlockX = (b1.getX() < b2.getX() ? b2.getX() : b1.getX());
            int bottomBlockX = (b1.getX() > b2.getX() ? b2.getX() : b1.getX());

            int topBlockY = (b1.getY() < b2.getY() ? b2.getY() : b1.getY());
            int bottomBlockY = (b1.getY() > b2.getY() ? b2.getY() : b1.getY());

            int topBlockZ = (b1.getZ() < b2.getZ() ? b2.getZ() : b1.getZ());
            int bottomBlockZ = (b1.getZ() > b2.getZ() ? b2.getZ() : b1.getZ());

            for (int x = bottomBlockX; x <= topBlockX; x++) {
                for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                    for (int y = bottomBlockY; y <= topBlockY; y++) {
                        Block block = b1.getWorld().getBlockAt(x, y, z);
                        b1.setType(material, false);
                    }
                }
            }
        }
    }

    public Chat getChat() {
        return chat;
    }

    public HashMap<String, BukkitTask> getPendingWarmupTeleports() {
        return pendingWarmupTeleports;
    }

    public HashMap<String, Location> getPendingLocationTeleports() {
        return pendingLocationTeleports;
    }

    public HashMap<String, String> getPendingPlayerTeleports() {
        return pendingPlayerTeleports;
    }

    public HashMap<String, World> getPendingSpawnTeleports() {
        return pendingSpawnTeleports;
    }

    public PortalHandler getPortalHandler() {
        return portalHandler;
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public boolean isWorldguardLoaded() {
        return worldguardLoaded;
    }

    public boolean isRequestPortals() {
        return requestPortals;
    }

    public void setRequestPortals(boolean requestPortals) {
        this.requestPortals = requestPortals;
    }

    public void cancelWarmup(Player player) {
        if(!pendingWarmupTeleports.containsKey(player.getName())) {
            return;
        }

        BukkitTask warmup = pendingWarmupTeleports.remove(player.getName());
        warmup.cancel();

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("SendConfigMessage");
            out.writeUTF(player.getName());
            out.writeUTF("teleport.warmup.cancelled");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, null, e);
        }
        player.sendPluginMessage(this, "ProxySuite", b.toByteArray());
    }

    public void teleportRequest(Player player, Location destination) {
        Objects.requireNonNull(player);
        if (player.isOnline()) {
            player.teleport(destination);
        } else {
            pendingLocationTeleports.put(player.getName(), destination);
        }
    }
}
