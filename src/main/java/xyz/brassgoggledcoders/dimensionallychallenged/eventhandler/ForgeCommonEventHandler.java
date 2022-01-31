package xyz.brassgoggledcoders.dimensionallychallenged.eventhandler;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.dimensionallychallenged.DimensionallyChallenged;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSetting;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.LocationStatus;

import java.util.List;

@EventBusSubscriber(modid = DimensionallyChallenged.ID, bus = Bus.FORGE)
public class ForgeCommonEventHandler {

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.world instanceof IServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.world;
            IDimensionalSetting dimensionalSetting = DimensionallyChallenged.DIMENSIONAL_SETTINGS_MANAGER.get(serverWorld);
            if (dimensionalSetting != null) {
                if (!serverWorld.players().isEmpty()) {
                    List<ServerPlayerEntity> goingUp = Lists.newArrayList();
                    List<ServerPlayerEntity> goingDown = Lists.newArrayList();
                    for (ServerPlayerEntity player : serverWorld.players()) {
                        LocationStatus status = dimensionalSetting.getStatus(player);
                        if (status == LocationStatus.ABOVE) {
                            goingUp.add(player);
                        } else if (status == LocationStatus.BELOW) {
                            goingDown.add(player);
                        }
                    }
                    teleportPlayers(dimensionalSetting, LocationStatus.ABOVE, serverWorld, goingUp);
                    teleportPlayers(dimensionalSetting, LocationStatus.BELOW, serverWorld, goingDown);
                }
            }
        }
    }

    private static void teleportPlayers(IDimensionalSetting dimensionalSetting, LocationStatus status,
                                        IServerWorld serverWorld, List<ServerPlayerEntity> playerEntities) {
        if (!playerEntities.isEmpty()) {
            IServerWorld newWorld = status.findNewLevel(serverWorld, dimensionalSetting);
            if (newWorld != null) {
                double scale = DimensionType.getTeleportationScale(serverWorld.getLevel().dimensionType(), newWorld.dimensionType());
                double y = (status == LocationStatus.ABOVE) ? 5 : newWorld.getHeight() - 5;
                for (ServerPlayerEntity player : playerEntities) {
                    player.teleportTo(newWorld.getLevel(), player.getX() / scale, y, player.getZ() / scale,
                            player.yRot, player.xRot);
                }
            }
        }

    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DimensionallyChallenged.DIMENSIONAL_SETTINGS_MANAGER);
    }
}
