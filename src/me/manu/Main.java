package me.manu;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements CommandExecutor {

    private boolean emManutencao = false;
    private String motdPadrao;
	private String motdManutencao;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        loadConfig();

        getCommand("manutencao").setExecutor(this);
        getCommand("setmotdmanutencao").setExecutor(this);

        setMotdPadrao();

        removeWhitelist();
    }

    @Override
    public void onDisable() {
        if (emManutencao) {
            setMotdPadrao();
            removeWhitelist();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("manutencao")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Uso correto: /manutencao <ligar/desligar>");
                return true;
            }

            if (args[0].equalsIgnoreCase("ligar")) {
                ligarManutencao();
                sender.sendMessage(ChatColor.GREEN + "Modo de manutenção ativado.");
            } else if (args[0].equalsIgnoreCase("desligar")) {
                desligarManutencao();
                sender.sendMessage(ChatColor.GREEN + "Modo de manutenção desativado.");
            } else {
                sender.sendMessage(ChatColor.RED + "Comando inválido. Use /manutencao <ligar/desligar>");
            }

            return true;
        } else if (cmd.getName().equalsIgnoreCase("setmotdmanutencao")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Uso correto: /setmotdmanutencao <mensagem>");
                return true;
            }

            String motd = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
            config.set("motdManutencao", motd);
            saveConfigFile();

            sender.sendMessage(ChatColor.GREEN + "Motd de manutenção configurada com sucesso.");
            return true;
        }

        return false;
    }

    private void ligarManutencao() {
        emManutencao = true;
        setMotdManutencao();
        setWhitelist();
        kickPlayersSemPermissao();
    }

    private void desligarManutencao() {
        emManutencao = false;
        restoreMotdPadrao();
        removeWhitelist();
    }

    private void setMotdManutencao() {
        String motdManutencao = config.getString("motdManutencao");
        if (motdManutencao == null || motdManutencao.isEmpty()) {
            motdManutencao = ChatColor.RED + "Servidor em Manutenção. Volte mais tarde!";
        }

        motdManutencao = ChatColor.translateAlternateColorCodes('&', motdManutencao);

        // Salvar o motd atual para poder restaurá-lo depois
        this.motdPadrao = MinecraftServer.getServer().getMotd();
        MinecraftServer.getServer().setMotd(motdManutencao);
    }

    private void setMotdPadrao() {
        String motdPadrao = "Seja bem-vindo ao servidor!";
        motdPadrao = ChatColor.translateAlternateColorCodes('&', motdPadrao);
        MinecraftServer.getServer().setMotd(motdPadrao);
    }

    private void restoreMotdPadrao() {
        if (motdPadrao != null) {
            MinecraftServer.getServer().setMotd(motdPadrao);
        }
    }

    private void setWhitelist() {
        Bukkit.setWhitelist(true);
    }

    private void removeWhitelist() {
        Bukkit.setWhitelist(false);
    }

    private void kickPlayersSemPermissao() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("manutencao.bypass")) {
                player.kickPlayer(ChatColor.RED + "O servidor está em manutenção. Volte mais tarde!");
            }
        }
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        if (config.contains("motdPadrao")) {
            motdPadrao = config.getString("motdPadrao");
        }

        if (config.contains("motdManutencao")) {
            motdManutencao = config.getString("motdManutencao");
        }
    }

    private void saveConfigFile() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}