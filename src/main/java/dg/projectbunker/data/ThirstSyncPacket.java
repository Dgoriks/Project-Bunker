package dg.projectbunker.data;

import dg.projectbunker.client.gui.GreetingManager;
import dg.projectbunker.client.gui.ThirstTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record ThirstSyncPacket(int thirstValue) implements CustomPacketPayload {

    public static final Type<ThirstSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("project_bunker", "thirst_sync"));

    public static final StreamCodec<FriendlyByteBuf, ThirstSyncPacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.thirstValue),
            buf -> new ThirstSyncPacket(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ThirstSyncPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Пакет только обновляет значение жажды в HUD, предотвращая ранний показ на экране загрузки
            ThirstTracker.setClientThirst(payload.thirstValue());
        });
    }
}