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

    // Регулярное выражение ищет три числа (координаты X Y Z), разделенные пробелами
    private final Pattern coordinatePattern = Pattern.compile("(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)");

    @Override
    public void onEnable() {
        // Включаем отслеживание шагов игрока на сервере
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Плагин на кастомные порталы успешно активирован!");
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerMoveEvent event) {
        // Оптимизация: реагируем только если игрок перешел на новый блок, а не просто повернул голову
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        Block blockUnderPlayer = player.getLocation().getBlock();

        // 1. Проверяем блок под ногами (STRUCTURE_BLOCK — блок конструктора)
        if (blockUnderPlayer.getType() != Material.STRUCTURE_BLOCK) return;

        // 2. Ищем кафедру (LECTERN) в радиусе 3 блоков вокруг конструктора
        Lectern lectern = findLecternNearby(blockUnderPlayer, 3);
        if (lectern == null) {
            player.sendMessage("§c[Портал] Рядом не найдена кафедра с книгой координат!");
            return;
        }

        // 3. Достаем книгу с кафедры
        ItemStack bookItem = lectern.getInventory().getItem(0);
        if (bookItem == null || bookItem.getType() != Material.WRITTEN_BOOK) {
            player.sendMessage("§c[Портал] На кафедре должна лежать подписанная книга!");
            return;
        }

        // 4. Читаем текст первой страницы книги
        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();
        if (!bookMeta.hasPages()) return;
        String firstPageText = bookMeta.getPage(1);

        // 5. Извлекаем координаты из текста страницы
        Matcher matcher = coordinatePattern.matcher(firstPageText.trim());
        if (matcher.find()) {
            try {
                double x = Double.parseDouble(matcher.group(1));
                double y = Double.parseDouble(matcher.group(2));
                double z = Double.parseDouble(matcher.group(3));

                World world = player.getWorld();
                // Добавляем +0.5 к X и Z, чтобы игрок телепортировался ровно в центр блока, а не на его край
                Location targetLocation = new Location(world, x + 0.5, y, z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());

                // 6. Телепортируем игрока
                player.teleport(targetLocation);
                player.sendMessage("§d[Портал] Вы телепортированы на координаты: " + (int)x + " " + (int)y + " " + (int)z);
                
            } catch (NumberFormatException e) {
                player.sendMessage("§c[Портал] Ошибка чтения чисел в книге.");
            }
        } else {
            player.sendMessage("§c[Портал] На первой странице книги не найдены координаты формата: X Y Z (например: 150 70 -300)");
        }
    }

    // Метод сканирует куб вокруг портала в поисках кафедры
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
