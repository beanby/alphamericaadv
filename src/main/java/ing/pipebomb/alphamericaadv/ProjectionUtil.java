package ing.pipebomb.alphamericaadv;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class ProjectionUtil {

    public static Pair<Integer, Integer> latLongToMC(ServerLevel lvl, double lat, double lng) {
        ProjectionData data = ProjectionData.fromServerLevel(lvl);
        double latR = lat * Math.PI / 180;
        double lngR = lng * Math.PI / 180;

        double k = (1-data.e*Math.sin(latR))/(1+data.e*Math.sin(latR));
        double cValue = Math.pow((k),data.e/2
        );
        double dValue = Math.log(Math.tan((Math.PI+2*latR)/4)*cValue); //natural log fyi

        if (Double.isNaN(cValue) || Double.isNaN(dValue) || Double.isInfinite(dValue)) {
            return null;
        }

        double northing = data.a * dValue;
        double easting  = data.a * lngR;

        double imageX = (easting - data.gT.get(0))/data.gT.get(1); // no rotations allowed please
        double imageZ = (northing - data.gT.get(3))/data.gT.get(5);

        return Pair.of((int)(imageX-data.xPadding),(int)(imageZ-data.zPadding));
    }

    public static Pair<Double, Double> mcToLatLong(ServerLevel lvl, int x, int z) {
        ProjectionData data = ProjectionData.fromServerLevel(lvl);
        double imageX = x + data.xPadding;
        double imageZ = z + data.zPadding;

        double easting = data.gT.getFirst() + imageX * data.gT.get(1); // ???
        double northing  = data.gT.get(3)     + imageZ * data.gT.get(5);

        double tValue = Math.exp(-northing/data.a);
        double xValue = (Math.PI - 4*Math.atan(tValue))/2;

        double lat = xValue + data.c1*Math.sin(2*xValue) + data.c2*Math.sin(4*xValue) + data.c3*Math.sin(6*xValue) + data.c4*Math.sin(8*xValue);
        double lng = easting/data.a;

        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            return null;
        }

        return Pair.of(lat *(180.0/Math.PI),lng *(180.0/Math.PI));
    }

    public static class ProjectionCommand {
        public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("proj");

            LiteralArgumentBuilder<CommandSourceStack> modify = Commands.literal("z_edit_constants").requires(
                    s -> s.hasPermission(1)
            ).then(
                    Commands.argument("newA", DoubleArgumentType.doubleArg()).then(
                            Commands.argument("newRF", DoubleArgumentType.doubleArg())
                                    .executes(s -> {
                                        ProjectionData data = ProjectionData.fromServerLevel(s.getSource().getLevel());
                                        double newA = DoubleArgumentType.getDouble(s, "newA");
                                        double newRF = DoubleArgumentType.getDouble(s, "newRF");
                                        data.editConstants(newA,newRF);
                                        s.getSource().sendSuccess(() -> Component.literal("Set."),false);
                                        return 1;
                                    })
                                    .then(
                                            Commands.argument("paddingX", IntegerArgumentType.integer()).then(
                                                    Commands.argument("paddingZ", IntegerArgumentType.integer())
                                                            .executes(s -> {
                                                                ProjectionData data = ProjectionData.fromServerLevel(s.getSource().getLevel());
                                                                double newA = DoubleArgumentType.getDouble(s, "newA");
                                                                double newRF = DoubleArgumentType.getDouble(s, "newRF");
                                                                int newXP = IntegerArgumentType.getInteger(s, "paddingX");
                                                                int newZP = IntegerArgumentType.getInteger(s, "paddingZ");
                                                                data.editConstants(newA,newRF, newXP, newZP);
                                                                s.getSource().sendSuccess(() -> Component.literal("Set."),false);
                                                                return 1;
                                                            })
                                                            .then(
                                                                    Commands.argument("g0",DoubleArgumentType.doubleArg())
                                                                            .then(Commands.argument("g1",DoubleArgumentType.doubleArg())
                                                                                    .then(Commands.argument("g3",DoubleArgumentType.doubleArg())
                                                                                            .then(Commands.argument("g5",DoubleArgumentType.doubleArg())
                                                                                                    .executes(s -> {
                                                                                                        ProjectionData data = ProjectionData.fromServerLevel(s.getSource().getLevel());
                                                                                                        double newA = DoubleArgumentType.getDouble(s, "newA");
                                                                                                        double newRF = DoubleArgumentType.getDouble(s, "newRF");
                                                                                                        int newXP = IntegerArgumentType.getInteger(s, "paddingX");
                                                                                                        int newZP = IntegerArgumentType.getInteger(s, "paddingZ");
                                                                                                        double g0 = DoubleArgumentType.getDouble(s, "g0");
                                                                                                        double g1 = DoubleArgumentType.getDouble(s, "g1");
                                                                                                        double g3 = DoubleArgumentType.getDouble(s, "g3");
                                                                                                        double g5 = DoubleArgumentType.getDouble(s, "g5");
                                                                                                        data.editConstants(newA, newRF, newXP, newZP, List.of(g0,g1,0.0,g3,0.0,g5));
                                                                                                        s.getSource().sendSuccess(() -> Component.literal("Set."),false);
                                                                                                        return 1;
                                                                                                    })
                                                                                            )
                                                                                    )
                                                                            )
                                                            )
                                            )
                                    )
                    )
            );

            LiteralArgumentBuilder<CommandSourceStack> fromGeo = Commands.literal("from_geo").then(
                    Commands.argument("Latitude", DoubleArgumentType.doubleArg()).then(
                            Commands.argument("Longitude", DoubleArgumentType.doubleArg()).executes(
                                    s -> {
                                        CommandSourceStack source = s.getSource();
                                        double latitude = DoubleArgumentType.getDouble(s, "Latitude");
                                        double longitude = DoubleArgumentType.getDouble(s, "Longitude");

                                        Pair<Integer,Integer> mcCoords = latLongToMC(s.getSource().getLevel(), latitude, longitude);
                                        if (mcCoords == null) {
                                            source.sendFailure(Component.literal("Coordinates provided are out of range. (use degrees)"));
                                            return 0;
                                        }
                                        source.sendSuccess(() -> formatGeoCoords(latitude,longitude, mcCoords.getFirst(), mcCoords.getSecond()), false);
                                        return 1;
                                    }
                            )
                    )
            );

            LiteralArgumentBuilder<CommandSourceStack> fromMC = Commands.literal("from_mc").then(
                    Commands.argument("position", ColumnPosArgument.columnPos()).executes(
                            s -> {
                                CommandSourceStack source = s.getSource();
                                ColumnPos colPos = ColumnPosArgument.getColumnPos(s,"position");

                                Pair<Double,Double> geoCoords = mcToLatLong(source.getLevel(), colPos.x(), colPos.z());

                                if (geoCoords == null) {
                                    source.sendFailure(Component.literal("Could not re-project minecraft coordinates to geo coordinates."));
                                    return 0;
                                }
                                source.sendSuccess(() -> formatMCCoords(geoCoords.getFirst(),geoCoords.getSecond(), colPos.x(), colPos.z()),false);
                                return 1;
                            }
                    )
            );

            return root
                    .then(modify)
                    .then(fromGeo)
                    .then(fromMC);
        }
    }

    private static Component formatGeoCoords(double lat, double lng, int x, int z) {
        Component clipboard = Component.literal("(%s x,%s z)".formatted(x,z)).setStyle(
                Style.EMPTY.withClickEvent(
                        new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,"%s %s".formatted(x,z))
                ).withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Copy to clipboard, (click)").withStyle(Style.EMPTY.withItalic(true)))
                ).withColor(
                        ChatFormatting.GREEN
                ));
        return Component.literal("%.3f°N %.3f°E would be at ".formatted(lat,lng)).append(clipboard);
    }

    private static Component formatMCCoords(double lat, double lng, int x, int z) {
        Component clipboard = Component.literal("%.3f°N %.3f°E".formatted(lat,lng)).setStyle(
                Style.EMPTY.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL,"https://www.google.com/maps/place/%s,%s/".formatted(lat, lng))
                ).withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("View on maps, (click)").withStyle(Style.EMPTY.withItalic(true)))
                ).withColor(
                        ChatFormatting.GREEN
                ));
        return Component.literal("(%s x,%s z) would be at about ".formatted(x,z)).append(clipboard);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getPlayerSpawnCommand() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("player_spawn_location").requires(s -> s.hasPermission(4));
        LiteralArgumentBuilder<CommandSourceStack> set  = Commands.literal("set")
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.argument("latitude",DoubleArgumentType.doubleArg())
                                .then(Commands.argument("longitude",DoubleArgumentType.doubleArg())
                                        .executes(c -> {
                                            Collection<GameProfile> players = GameProfileArgument.getGameProfiles(c, "player");
                                            if (players.size() > 1) {
                                                c.getSource().sendFailure(Component.literal("Please only specify one player"));
                                                return 0;
                                            }
                                            GameProfile plr = players.iterator().next();
                                            UUID playerUUID = plr.getId();

                                            double lat = DoubleArgumentType.getDouble(c, "latitude");
                                            double lng = DoubleArgumentType.getDouble(c, "longitude");

                                            Pair<Integer,Integer> mcPos = latLongToMC(c.getSource().getLevel(),lat,lng);
                                            if (mcPos == null) {
                                                c.getSource().sendFailure(Component.literal("Could not re-project geo-coordinates to minecraft coordinates"));
                                                return 0;
                                            }

                                            PlayerSpawnData data = PlayerSpawnData.fromServerLevel(c.getSource().getLevel());

                                            data.addPlayer(playerUUID,mcPos);
                                            c.getSource().sendSuccess(() -> Component.literal("Added player successfully"),false);
                                            return 1;
                                        }))));
        LiteralArgumentBuilder<CommandSourceStack> unset = Commands.literal("unset")
                .then(Commands.argument("player",GameProfileArgument.gameProfile())
                        .executes(c -> {
                            Collection<GameProfile> players = GameProfileArgument.getGameProfiles(c, "player");
                            if (players.size() > 1) {
                                c.getSource().sendFailure(Component.literal("Please only specify one player"));
                                return 0;
                            }
                            GameProfile plr = players.iterator().next();
                            UUID plrUUID = plr.getId();

                            PlayerSpawnData data = PlayerSpawnData.fromServerLevel(c.getSource().getLevel());

                            if (!data.removePlayer(plrUUID)) {
                                c.getSource().sendFailure(Component.literal("Player is not set"));
                                return 0;
                            }
                            return 1;
                        }));

        return root
                .then(set)
                .then(unset);
    }

    @ParametersAreNonnullByDefault
    public static class PlayerSpawnData extends SavedData {
        Map<UUID,Pair<Integer, Integer>> spawns = new HashMap<>();

        public static PlayerSpawnData fromServerLevel(ServerLevel lvl) {
            return lvl.getDataStorage().computeIfAbsent(new Factory<>(PlayerSpawnData::new,PlayerSpawnData::load),"player_spawn_data");
        }

        public void addPlayer(UUID id, Pair<Integer,Integer> pos) {
            this.spawns.put(id, pos);
            this.setDirty();
        }

        public boolean removePlayer(UUID id) {
            if (this.spawns.remove(id) == null) {
                return false;
            }
            this.setDirty();
            return true;
        }

        @Override
        public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            CompoundTag spawnsTag = new CompoundTag();
            for (Map.Entry<UUID,Pair<Integer,Integer>> entry : this.spawns.entrySet()) {
                CompoundTag plrData = new CompoundTag();
                //plrData.putUUID("uuid",entry.getKey());
                plrData.putInt("x",entry.getValue().getFirst());
                plrData.putInt("z",entry.getValue().getFirst());
                spawnsTag.put(entry.getKey().toString(),plrData);
            }
            tag.put("spawns",spawnsTag);
            return tag;
        }

        public static PlayerSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
            PlayerSpawnData data = new PlayerSpawnData();
            CompoundTag spawnsTag = tag.getCompound("spawns");
            for (String key : spawnsTag.getAllKeys()) {
                UUID id = UUID.fromString(key);
                CompoundTag plrData = spawnsTag.getCompound(key);
                int x = plrData.getInt("x");
                int z = plrData.getInt("z");
                data.spawns.put(id,Pair.of(x,z));
            }
            return data;
        }
    }

    @ParametersAreNonnullByDefault
    public static class ProjectionData extends SavedData {
        public double a;
        public double f;
        public double e;
        public int xPadding;
        public int zPadding;
        public List<Double> gT;
        public double c1;
        public double c2;
        public double c3;
        public double c4;

        ProjectionData(double a, double rF) {
            this.a = a;
            this.f = 1/rF;
            this.e = Math.sqrt(2.0*this.f + this.f * this.f);
        }

        public static ProjectionData fromServerLevel(ServerLevel lvl) {
            return lvl.getDataStorage().computeIfAbsent(new Factory<>(ProjectionData::create, ProjectionData::load), "projection_data");
        }

        public void editConstants(double newA, double newRF) {
            editConstants(newA, newRF, this.xPadding, this.zPadding,this.gT);
        }

        public void editConstants(double newA, double newRF, int newXP, int newZP) {
            editConstants(newA, newRF, newXP, newZP, this.gT);
        }

        public void editConstants(double newA, double newRF, int newXP, int newZP, @Nullable List<Double> newGeoTransforms) {
            this.a = newA;
            this.f = 1/newRF;
            this.e = Math.sqrt(2.0*this.f + this.f * this.f);
            this.xPadding = newXP;
            this.zPadding = newZP;
            if (newGeoTransforms != null) {
                this.gT = newGeoTransforms;
            }


            calculateDerived();

            this.setDirty();
        }

        private void calculateDerived() {//standards amirite
            this.c1 = (Math.pow(this.e,2) + (5.0/24.0)*Math.pow(this.e,4) +   (1.0/12.0)*Math.pow(this.e,6) +    (13.0/360.0)*Math.pow(this.e,8));
            this.c2 =                      ((7.0/48.0)*Math.pow(this.e,4) + (29.0/240.0)*Math.pow(this.e,6) + (811.0/11520.0)*Math.pow(this.e,8));
            this.c3 =                                                       ((7.0/120.0)*Math.pow(this.e,6) +  (81.0/1120.0)*Math.pow(this.e, 8));
            this.c4 =                                                                                      ((4279.0/161280.0)*Math.pow(this.e,8));
        }

        public static ProjectionData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
            ProjectionData data = ProjectionData.create();
            data.a = tag.getDouble("major_axis");
            data.f = tag.getDouble("f");
            data.e = tag.getDouble("ecc");
            data.xPadding = tag.getInt("x_padding");
            data.zPadding = tag.getInt("z_padding");
            data.gT = List.of(
                    tag.getDouble("g0"),
                    tag.getDouble("g1"),
                    0.0,
                    tag.getDouble("g3"),
                    0.0,
                    tag.getDouble("g5")
            );
            return data;
        }

        public static ProjectionData create() {
            return new ProjectionData(0,1);
        }

        @Override
        public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putDouble("major_axis", this.a);
            tag.putDouble("f", this.f);
            tag.putDouble("ecc", this.e);
            tag.putInt("x_padding",this.xPadding);
            tag.putInt("z_padding",this.zPadding);
            tag.putDouble("g0",this.gT.getFirst());
            tag.putDouble("g1",this.gT.get(1));
            tag.putDouble("g3",this.gT.get(3));
            tag.putDouble("g5",this.gT.get(5));
            return tag;
        }
    }
}
