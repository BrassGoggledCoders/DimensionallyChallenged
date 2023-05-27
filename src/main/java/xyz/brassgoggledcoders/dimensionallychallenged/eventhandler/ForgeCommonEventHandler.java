package xyz.brassgoggledcoders.dimensionallychallenged.eventhandler;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.DimensionType;
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
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel serverLevel) {
            IDimensionalSetting dimensionalSetting = DimensionallyChallenged.DIMENSIONAL_SETTINGS_MANAGER.get(serverLevel);
            if (dimensionalSetting != null) {
                if (!serverLevel.players().isEmpty()) {
                    List<ServerPlayer> goingUp = Lists.newArrayList();
                    List<ServerPlayer> goingDown = Lists.newArrayList();
                    for (ServerPlayer player : serverLevel.players()) {
                        LocationStatus status = dimensionalSetting.getStatus(player);
                        if (status == LocationStatus.ABOVE) {
                            goingUp.add(player);
                        } else if (status == LocationStatus.BELOW) {
                            goingDown.add(player);
                        }
                    }
                    teleportPlayers(dimensionalSetting, LocationStatus.ABOVE, serverLevel, goingUp);
                    teleportPlayers(dimensionalSetting, LocationStatus.BELOW, serverLevel, goingDown);
                }
            }
        }
    }

    private static void teleportPlayers(IDimensionalSetting dimensionalSetting, LocationStatus status,
                                        ServerLevel serverLevel, List<ServerPlayer> playerEntities) {
        for (ServerPlayer player : playerEntities) {
            ServerLevel newLevel = status.findPlacement(serverLevel, player, dimensionalSetting);
            if (newLevel != null) {
                double scale = DimensionType.getTeleportationScale(serverLevel.getLevel().dimensionType(), newLevel.dimensionType());
                double y = (status == LocationStatus.ABOVE) ? 5 : newLevel.getHeight() - 5;
                    player.teleportTo(newLevel.getLevel(), player.getX() / scale, y, player.getZ() / scale,
                            player.getYRot(), player.getXRot());
            }
        }

    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DimensionallyChallenged.DIMENSIONAL_SETTINGS_MANAGER);
    }
}
