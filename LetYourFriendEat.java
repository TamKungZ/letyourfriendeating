package th.tamkungz.letyourfriendeating;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.item.Food;
import com.mojang.datafixers.util.Pair;

import java.util.List;

@Mod(LetYourFriendEat.MOD_ID)
public class LetYourFriendEat {
    public static final String MOD_ID = "letyourfriendeating";

    public LetYourFriendEat() {
        MinecraftForge.EVENT_BUS.addListener(this::onEntityInteract);
    }

    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        PlayerEntity player = event.getPlayer();
        if (event.getTarget() instanceof PlayerEntity && !event.getWorld().isClientSide()) {
            PlayerEntity target = (PlayerEntity) event.getTarget();
            event.setCancellationResult(tryFeedPlayer(player, target, event.getHand()));
        }
    }

    private ActionResultType tryFeedPlayer(PlayerEntity feeder, PlayerEntity target, Hand hand) {
        ItemStack stack = feeder.getItemInHand(hand);
        Food food = stack.getItem().getFoodProperties();
        if (feeder == target || stack.isEmpty() || !stack.isEdible() || food == null || target.getFoodData().getFoodLevel() >= 20)
            return ActionResultType.PASS;

        feedPlayer(target, food);
        applyFoodEffects(target, food);
        playFoodEffects(target);
        if (!feeder.isCreative()) stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    private void feedPlayer(PlayerEntity target, Food food) {
        target.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
    }

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