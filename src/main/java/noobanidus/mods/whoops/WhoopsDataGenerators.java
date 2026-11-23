package noobanidus.mods.whoops;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Whoops.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WhoopsDataGenerators {
  @SubscribeEvent
  public static void gatherData(GatherDataEvent event) {
    if (event.includeServer()) {
      ExistingFileHelper helper = event.getExistingFileHelper();
      DataGenerator gen = event.getGenerator();
      event.getGenerator().addProvider(true, new BlockTags(gen.getPackOutput(), event.getLookupProvider(), helper));
    }
  }

  public static class BlockTags extends BlockTagsProvider {
    public BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
      super(output, lookupProvider, Whoops.MODID, existingFileHelper);
    }

    @Override
    public String getName() {
      return "Whoops Tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
      tag(Whoops.WHOOPS_BLOCKS).add(ResourceKey.create(Registries.BLOCK, new ResourceLocation("botania", "pure_daisy")))
          .add(ResourceKey.create(Registries.BLOCK, new ResourceLocation("botania", "floating_pure_daisy")));
    }
  }
}
