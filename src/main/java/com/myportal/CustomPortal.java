package com.myportal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CustomPortal extends JavaPlugin implements Listener {

    private final Pattern coordinatePattern = Pattern.compile("(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Плагин на порталы из укрепленного сланца успешно активирован!");
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        
        Block blockAtFeet = player.getLocation().getBlock();
        Block blockUnderFeet = player.getLocation().subtract(0, 0.1, 0).getBlock();

        Block portalBlock = null;

        // НАСТРОЙКА: Теперь плагин строго ищет Укрепленный глубинный сланец (REINFORCED_DEEPSLATE)
        if (blockAtFeet.getType() == Material.REINFORCED_DEEPSLATE) {
            portalBlock = blockAtFeet;
        } else if (blockUnderFeet.getType() == Material.REINFORCED_DEEPSLATE) {
            portalBlock = blockUnderFeet;
        }

        if (portalBlock == null) return;

        getLogger().info("Игрок " + player.getName() + " наступил на блок укрепленного сланца!");

        Lectern lectern = findLecternNearby(portalBlock, 3);
        if (lectern == null) {
            player.sendMessage("§c[Портал] Рядом не найдена кафедра с книгой координат!");
            return;
        }

        ItemStack bookItem = lectern.getInventory().getItem(0);
        if (bookItem == null || bookItem.getType() != Material.WRITTEN_BOOK) {
            player.sendMessage("§c[Портал] На кафедре должна лежать ПОДПИСАННАЯ книга!");
            return;
        }

        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();
        if (bookMeta == null || !bookMeta.hasPages()) return;
        String firstPageText = bookMeta.getPage(1);

        Matcher matcher = coordinatePattern.matcher(firstPageText.trim());
        if (matcher.find()) {
            try {
                double x = Double.parseDouble(matcher.group(1));
                double y = Double.parseDouble(matcher.group(2));
                double z = Double.parseDouble(matcher.group(3));

                World world = player.getWorld();
                Location targetLocation = new Location(world, x + 0.5, y, z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());

                player.teleport(targetLocation);
                player.sendMessage("§9[Портал] Древняя рамка перенесла вас на: " + (int)x + " " + (int)y + " " + (int)z);
                
            } catch (NumberFormatException e) {
                player.sendMessage("§c[Портал] Ошибка чтения чисел в книге.");
            }
        } else {
            player.sendMessage("§c[Портал] На первой странице книги не найдены координаты формата: X Y Z (например: 150 70 -300)");
        }
    }

    private Lectern findLecternNearby(Block startBlock, int radius) {
        World world = startBlock.getWorld();
        int sx = startBlock.getX();
        int sy = startBlock.getY();
        int sz = startBlock.getZ();

        for (int x = sx - radius; x <= sx + radius; x++) {
            for (int y = sy - radius; y <= sy + radius; y++) {
                for (int z = sz - radius; z <= sz + radius; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() == Material.LECTERN) {
                        return (Lectern) b.getState();
                    }
                }
            }
        }
        return null;
    }
}
