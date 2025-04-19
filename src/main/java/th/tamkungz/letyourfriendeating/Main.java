package th.tamkungz.letyourfriendeating;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    // Toggle for the feature
    private boolean enabled = true;

    // Cooldown tracker (500 ms) and feed count tracker
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> counts   = new HashMap<>();

    private final Map<Material, FoodValue> foodValues = new HashMap<>();
    private static class FoodValue {
        final int nutrition;
        final float saturation;

        FoodValue(int n, float s) {
            nutrition = n;
            saturation = s;
        }
    }
    private void loadFoodValues() {
        foodValues.clear();
        var section = getConfig().getConfigurationSection("food-values");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key);
                int nutrition = section.getInt(key + ".nutrition");
                float saturation = (float) section.getDouble(key + ".saturation");
                foodValues.put(mat, new FoodValue(nutrition, saturation));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material in config: " + key);
            }
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadFoodValues();

        getServer().getPluginManager().registerEvents(this, this);  // Register event listener
        getLogger().info("LetYourFriendEating enabled.");
    }

    @EventHandler
    public void onFeedAttempt(PlayerInteractEntityEvent event) {
        if (!enabled) return;
        if (!(event.getRightClicked() instanceof Player target)) return;  // Only feed other players
        Player feeder = event.getPlayer();
        if (feeder.equals(target)) return;  // Can't feed yourself

        UUID fid = feeder.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(fid, 0L) + 500 > now) return;  // 500 ms cooldown
        cooldowns.put(fid, now);

        if (tryFeedPlayer(feeder, target)) {
            event.setCancelled(true);  // Prevent default interaction
        }
    }

    private boolean tryFeedPlayer(Player feeder, Player target) {
        var handItem = feeder.getInventory().getItemInMainHand();
        Material mat = handItem.getType();

        // Define nutrition & saturation per item
        int nutrition;
        float saturation;
        switch (mat) {
            case APPLE:
                nutrition = 3;
                saturation = 2.4f;
                break;
            case BREAD:
                nutrition = 5;
                saturation = 6.0f;
                break;
            case COOKED_BEEF:
                nutrition = 8;
                saturation = 12.8f;
                break;
            case COOKED_CHICKEN:
                nutrition = 6;
                saturation = 7.2f;
                break;
            default:
                return false;  // Not a supported edible item
        }

        // Apply hunger & saturation (max 20) :contentReference[oaicite:0]{index=0}
        target.setFoodLevel(Math.min(target.getFoodLevel() + nutrition, 20));
        target.setSaturation(Math.min(target.getSaturation() + saturation, 20f));

        // Track feed count & consume item
        counts.put(target.getUniqueId(), counts.getOrDefault(target.getUniqueId(), 0) + 1);
        if (feeder.getGameMode() != GameMode.CREATIVE) {
            handItem.setAmount(handItem.getAmount() - 1);
        }

        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p) || !cmd.getName().equalsIgnoreCase("feedfriend")) {
            return false;
        }

        if (args.length == 0) {
            p.sendMessage("Usage: /feedfriend <toggle|stats> [player]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle":
                enabled = !enabled;
                p.sendMessage("Feed Friend is now " + (enabled ? "ON" : "OFF"));
                break;

            case "stats":
                Player t = (args.length > 1) ? Bukkit.getPlayerExact(args[1]) : p;
                if (t == null) {
                    p.sendMessage("Player not found.");
                } else {
                    int count = counts.getOrDefault(t.getUniqueId(), 0);
                    p.sendMessage(t.getName() + " was fed " + count + " time(s).");
                }
                break;

            default:
                p.sendMessage("Unknown subcommand.");
        }
        return true;
    }
}
