package de.sabbertran.proxysuite.bukkit.commands;

import de.sabbertran.proxysuite.bukkit.ProxySuiteBukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class BunCommand implements CommandExecutor {
    private final ProxySuiteBukkit main;

    public BunCommand(ProxySuiteBukkit main) {
        this.main = main;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String st, String[] args) {
        if (!(sender instanceof Player) && args.length > 1 && main.getServer().getOnlinePlayers().size() > 0) {
            String player = args[0];
            StringBuilder sb = new StringBuilder((args.length - 1) * 2);
            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]);
                sb.append(" ");
            }

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                out.writeUTF("ExecuteCommand");
                out.writeUTF(player);
                out.writeUTF(sb.toString().trim());
            } catch (IOException e) {
                main.getLogger().log(Level.SEVERE, null, e);
            }
            main.getServer().getOnlinePlayers().iterator().next().sendPluginMessage(main, "proxysuite:channel", b.toByteArray());
        }
        return true;
    }
}
