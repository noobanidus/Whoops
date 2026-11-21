package noobanidus.mods.whoops;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(Whoops.MODID)
@SuppressWarnings("WeakerAccess")
public class Whoops {
  public static final TagKey<Block> WHOOPS_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("whoops", "whoops_blocks"));
  public static final String MODID = "whoops";

  @SuppressWarnings("unused")
  public final static Logger LOG = LogManager.getLogger(MODID);

  private static final double MARGIN = 0.05;

  private static final int[][] OFFSETS = {
      {-1, -1}, {-1, 0}, {-1, 1},
      {0, -1}, {0, 1},
      {1, -1}, {1, 0}, {1, 1}
  };

  public Whoops() {
    MinecraftForge.EVENT_BUS.addListener(Whoops::onDaisyRightClick);
  }

  public static void onDaisyRightClick(PlayerInteractEvent.RightClickBlock event) {
    if (event.getUseBlock() == Event.Result.DENY || event.getUseItem() == Event.Result.DENY) {
      return;
    }

    InteractionHand curHand = event.getHand();
    if (curHand != InteractionHand.MAIN_HAND) {
      return;
    }

    Player player = event.getPlayer();
    if (player instanceof FakePlayer || player.isCrouching()) {
      return;
    }

    ItemStack item = player.getItemInHand(curHand);
    if (item.isEmpty() || !(item.getItem() instanceof BlockItem iBlock)) {
      return;
    }

    BlockPos pos = event.getPos();
    Level level = event.getWorld();
    BlockState curStateAt = level.getBlockState(pos);
    if (!curStateAt.is(WHOOPS_BLOCKS)) {
      return;
    }

    BlockState state = iBlock.getBlock().defaultBlockState();
    Vec3 eye = player.getEyePosition();

    BlockPos chosen = null;
    boolean potentialEntityObstruction = false;
    List<BlockPos> otherPotentials = new ArrayList<>();

    for (int[] offset : OFFSETS) {
      BlockPos potential = pos.offset(offset[0], 0, offset[1]);

      if (!state.canSurvive(level, potential)) {
        continue;
      }

      if (!level.isEmptyBlock(potential) && !level.getBlockState(potential)
          .canBeReplaced(new DirectionalPlaceContext(level, potential, Direction.DOWN, item, Direction.DOWN))) {
        continue;
      }

      if (!level.isUnobstructed(state, potential, CollisionContext.empty())) {
        potentialEntityObstruction = true;
        continue;
      }

      // Claude came up with this one. It means that this potential position is less likely to directly block line of sight of the pure daisy and thus prevent easy clicking 8 times to place a ring.
      boolean pastX = offset[0] == 0 || (offset[0] > 0 ? eye.x > pos.getX() + 1 - MARGIN : eye.x < pos.getX() + MARGIN);
      boolean pastZ = offset[1] == 0 || (offset[1] > 0 ? eye.z > pos.getZ() + 1 - MARGIN : eye.z < pos.getZ() + MARGIN);

      if (!(pastX && pastZ)) {
        chosen = potential;
        break;
      }

      otherPotentials.add(potential);
    }

    if (chosen == null && !otherPotentials.isEmpty()) {
      Vec3 hit = event.getHitVec().getLocation();
      VoxelShape shape = curStateAt.getShape(level, pos);
      Vec3 centre = shape.isEmpty() ? Vec3.atCenterOf(pos) : shape.bounds().move(pos).getCenter();

      chosen = otherPotentials.get(0);

      for (BlockPos candidate : otherPotentials) {
        AABB box = new AABB(candidate).inflate(MARGIN);
        if (box.clip(eye, hit).isEmpty() && box.clip(eye, centre).isEmpty()) {
          chosen = candidate;
          break;
        }
      }
    }

    if (chosen != null) {
      if (!level.isClientSide()) {
        iBlock.place(new DirectionalPlaceContext(level, chosen, Direction.DOWN, item, Direction.DOWN));
      }
      event.setCanceled(true);
      event.setCancellationResult(InteractionResult.SUCCESS);
    } else if (potentialEntityObstruction) {
      event.setCanceled(true);
      event.setCancellationResult(InteractionResult.FAIL);
    }
  }
}