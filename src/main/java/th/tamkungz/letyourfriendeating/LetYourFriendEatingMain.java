package th.tamkungz.letyourfriendeating;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.food.Food;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("letyourfriendeating")
public class LetYourFriendEatingMain {
    public LetYourFriendEatingMain() {
        // ลงทะเบียน event listener ผ่าน Forge event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRightClickPlayer);
    }
    
    public void onRightClickPlayer(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        // ใช้การเรียก getFoodProperties โดยส่ง stack และ target เพื่อดึงข้อมูลอาหาร
        Food food = stack.getItem().getFoodProperties(stack, target);
        if (food == null) return;
        
        if (!player.level().isClientSide) {
            // เติม hunger & saturation โดยใช้ข้อมูลจาก food
            target.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
            // เล่นเสียง burp
            player.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            Level level = target.level();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + 1, target.getZ(),
                    5, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
