package th.tamkungz.letyourfriendeating;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import th.tamkungz.letyourfriendeating.commands.FeedFriendCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod(LetYourFriendEatingMainFunction.MOD_ID)
public class LetYourFriendEatingMainFunction {
    public static final String MOD_ID = "letyourfriendeating";
    public static boolean feedFriendEnabled = true;

    // Stores feed counts for each player.
    private static final Map<UUID, Integer> feedCounts = new HashMap<>();

    public LetYourFriendEatingMainFunction() {
        // Register this class to receive events.
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static int getFeedCount(UUID playerId) {
        return feedCounts.getOrDefault(playerId, 0);
    }

    public static Map<UUID, Integer> getFeedCountsView() {
        return Collections.unmodifiableMap(feedCounts);
    }

    // Register commands when the server is starting.
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(FeedFriendCommand.register());
    }

    // Listen for player interactions to handle the feeding action.
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        PlayerEntity player = event.getPlayer();
        if (event.getTarget() instanceof PlayerEntity && !event.getWorld().isClientSide()) {
            PlayerEntity target = (PlayerEntity) event.getTarget();
            ActionResultType result = tryFeedPlayer(player, target, event.getHand());
            if (result == ActionResultType.SUCCESS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        }
    }

    // Try to feed the target player.
    private ActionResultType tryFeedPlayer(PlayerEntity feeder, PlayerEntity target, Hand hand) {
        ItemStack stack = feeder.getItemInHand(hand);
        Food food = stack.getItem().getFoodProperties();
        if (feeder == target || stack.isEmpty() || !stack.isEdible() || food == null || feedFriendEnabled == false ||
            target.getFoodData().getFoodLevel() >= 20) {
            return ActionResultType.PASS;
        }

        // Apply the feeding effects.
        feedPlayer(target, food);
        applyFoodEffects(target, food);
        playFoodEffects(target);

        // Update feed count.
        UUID targetId = target.getUUID();
        feedCounts.put(targetId, getFeedCount(targetId) + 1);

        if (!feeder.isCreative()) {
            stack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    // Increase the target's food level.
    private void feedPlayer(PlayerEntity target, Food food) {
        target.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
    }

    // Apply any potion effects from the Food item.
    private void applyFoodEffects(PlayerEntity target, Food food) {
        List<Pair<EffectInstance, Float>> effects = food.getEffects();
        for (Pair<EffectInstance, Float> effectPair : effects) {
            EffectInstance effect = effectPair.getFirst();
            float chance = effectPair.getSecond();
            if (effect != null && target.getRandom().nextFloat() < chance) {
                target.addEffect(new EffectInstance(effect));
            }
        }
    }

    // Play sound and spawn particles to indicate feeding.
    private void playFoodEffects(PlayerEntity target) {
        target.playSound(SoundEvents.PLAYER_BURP, 1.0F, 1.0F);
        if (target.level instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) target.level;
            serverWorld.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + 1, target.getZ(),
                    5, 0.5, 0.5, 0.5, 0.0);
        }
    }
}
