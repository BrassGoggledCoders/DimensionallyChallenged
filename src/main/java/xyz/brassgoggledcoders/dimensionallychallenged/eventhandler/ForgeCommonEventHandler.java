package xyz.brassgoggledcoders.dimensionallychallenged.eventhandler;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import xyz.brassgoggledcoders.dimensionallychallenged.DimensionallyChallenged;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.IDimensionalSetting;
import xyz.brassgoggledcoders.dimensionallychallenged.api.setting.LocationStatus;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

@EventBusSubscriber(modid = DimensionallyChallenged.ID, bus = Bus.FORGE)
public class ForgeCommonEventHandler {
    private static final Object2IntMap<ResourceKey<Level>> LEVEL_OFFSET = new Object2IntArrayMap<>();
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.level instanceof ServerLevel serverLevel && !serverLevel.players().isEmpty()) {
            int offset = LEVEL_OFFSET.computeIfAbsent(serverLevel.dimension(), key -> RANDOM.nextInt(10));
            if (serverLevel.getGameTime() % 10 == offset) {
                IDimensionalSetting dimensionalSetting = DimensionallyChallenged.DIMENSIONAL_SETTINGS_MANAGER.get(serverLevel);
                if (dimensionalSetting != null) {
                    List<ServerPlayer> goingUp = Lists.newArrayList();
                    List<ServerPlayer> goingDown = Lists.newArrayList();
                    for (ServerPlayer player : serverLevel.players()) {
                        if (player.getVehicle() == null || player.getVehicle().getControllingPassenger() == player) {
                            LocationStatus status = dimensionalSetting.getStatus(player);
                            if (status == LocationStatus.ABOVE) {
                                goingUp.add(player);
                            } else if (status == LocationStatus.BELOW) {
                                goingDown.add(player);
                            }
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
                Entity teleportingEntity = player.getVehicle() != null ? player.getVehicle() : player;

                Vec3 destination = new Vec3(teleportingEntity.getX() / scale, y, teleportingEntity.getZ() / scale);
                List<Entity> passengers = teleportingEntity.getPassengers();
                teleportingEntity.changeDimension(newLevel, new ITeleporter() {
                    @Override
                    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                        Entity repositionedEntity = repositionEntity.apply(false);
                        if (repositionedEntity != null) {
                            //Teleport all passengers to the other dimension and then make them start riding the entity again
                            for (Entity passenger : passengers) {
                                teleportPassenger(destWorld, destination, repositionedEntity, passenger);
                            }
                        }
                        return repositionedEntity;
                    }

                    @Override
                    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                        return new PortalInfo(destination, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
                    }

                    @Override
                    public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                        return false;
                    }
                });
            }
        }
    }

    private static void teleportPassenger(ServerLevel destWorld, Vec3 destination, Entity repositionedEntity, Entity passenger) {
        if (!passenger.canChangeDimensions()) {
            //If the passenger can't change dimensions just let it peacefully stay after dismounting rather than trying to teleport it
            return;
        }
        //Note: We grab the passengers here instead of in placeEntity as changeDimension starts by removing any passengers
        List<Entity> passengers = passenger.getPassengers();
        passenger.changeDimension(destWorld, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                boolean invulnerable = entity.isInvulnerable();
                //Make the entity invulnerable so that when we teleport it, it doesn't take damage
                // we revert this state to the previous state after teleporting
                entity.setInvulnerable(true);
                Entity repositionedPassenger = repositionEntity.apply(false);
                if (repositionedPassenger != null) {
                    //Force our passenger to start riding the new entity again
                    repositionedPassenger.startRiding(repositionedEntity, true);
                    //Teleport "nested" passengers
                    for (Entity passenger : passengers) {
                        teleportPassenger(destWorld, destination, repositionedPassenger, passenger);
                    }
                    repositionedPassenger.setInvulnerable(invulnerable);
                }
                entity.setInvulnerable(invulnerable);
                return repositionedPassenger;
            }

            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                //This is needed to ensure the passenger starts getting tracked after teleporting
                return new PortalInfo(destination, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        });
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DimensionallyChallenged.DIMENSIONAL_SETTINGS_MANAGER);
    }
}
