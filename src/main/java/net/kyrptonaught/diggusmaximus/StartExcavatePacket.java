package net.kyrptonaught.diggusmaximus;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class StartExcavatePacket {

    static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(ExcavatePacket.PACKET_ID, (payload, context) -> {
            BlockPos blockPos = BlockPos.fromLong(payload.data.getLong("blockPos"));
            Identifier blockID = Identifier.tryParse(payload.data.getString("blockID"));
            int facingID = payload.data.getInt("facing");
            Direction facing = facingID == -1 ? null : Direction.byId(facingID);
            int shapeKey = payload.data.getInt("shapeSelection");
            context.server().execute(() -> {
                if (DiggusMaximusMod.getOptions().enabled) {
                    if (blockPos.isWithinDistance(context.player().getPos(), 10)) {
                        new Excavate(blockPos, blockID, context.player(), facing).startExcavate(shapeKey);
                    }
                }
            });
        });
    }

    public record ExcavatePacket(NbtCompound data) implements CustomPayload {

        public static final CustomPayload.Id<ExcavatePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(DiggusMaximusMod.MOD_ID, "start_excavate_packet"));
        private static final PacketCodec<PacketByteBuf, ExcavatePacket> PACKET_CODEC = PacketCodecs.NBT_COMPOUND.xmap(ExcavatePacket::new, ExcavatePacket::data).cast();


        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public static void registerSendPacket() {
        PayloadTypeRegistry.playC2S().register(ExcavatePacket.PACKET_ID, ExcavatePacket.PACKET_CODEC);
    }


    @Environment(EnvType.CLIENT)
    public static void sendExcavatePacket(BlockPos blockPos, Identifier blockID, Direction facing, int shapeSelection) {
        NbtCompound packetPayload = new NbtCompound();
        packetPayload.putString("blockID", blockID.toString());
        packetPayload.putLong("blockPos", blockPos.asLong());
        packetPayload.putInt("facing", facing == null ? -1 : facing.getId());
        packetPayload.putInt("shapeSelection", shapeSelection);



        ClientPlayNetworking.send(new ExcavatePacket(packetPayload));
    }
}