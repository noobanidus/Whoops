package noobanidus.mods.whoops;

import com.google.common.eventbus.Subscribe;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Whoops.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WhoopsDataGenerators {
  @SubscribeEvent
  public static void gatherData(GatherDataEvent event) {
    if (event.includeServer()) {
      ExistingFileHelper helper = event.getExistingFileHelper();
      event.getGenerator().addProvider(true, new BlockTags(event.getGenerator(), helper));
    }
  }

  public static class BlockTags extends BlockTagsProvider {
    public BlockTags(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper) {
      super(p_126511_, Whoops.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
      tag(Whoops.WHOOPS_BLOCKS).add(ResourceKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("botania", "pure_daisy")))
          .add(ResourceKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("botania", "floating_pure_daisy")));
    }

    @Override
    public String getName() {
      return "Whoops Tags";
    }
  }
}
