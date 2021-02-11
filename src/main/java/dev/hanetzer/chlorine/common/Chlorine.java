package dev.hanetzer.chlorine.common;

import dev.hanetzer.chlorine.common.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Chlorine.modID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Chlorine {
    public static final String modID = "chlorine";

    public static final Logger log = LogManager.getLogger(modID);

    public static boolean ftbChunksLoaded;
    public Chlorine() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Config::register);
        ftbChunksLoaded = ModList.get().isLoaded("ftbchunks");
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.DISPLAYTEST,
                ()->Pair.of(()->FMLNetworkConstants.IGNORESERVERONLY, (s,b)->true)
        );
    }
}
