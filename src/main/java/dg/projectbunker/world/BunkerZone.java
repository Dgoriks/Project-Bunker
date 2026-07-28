package dg.projectbunker.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class BunkerZone {

    private final BlockPos controllerPos;
    private final Set<BlockPos> interior;
    private final Set<BlockPos> boundingWalls;

    public BunkerZone(BlockPos controllerPos, Set<BlockPos> interior, Set<BlockPos> boundingWalls) {
        this.controllerPos = controllerPos.immutable();
        this.interior = new HashSet<>();
        for (BlockPos pos : interior) {
            this.interior.add(pos.immutable());
        }
        this.boundingWalls = new HashSet<>();
        for (BlockPos pos : boundingWalls) {
            this.boundingWalls.add(pos.immutable());
        }
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public Set<BlockPos> getInterior() {
        return interior;
    }

    public Set<BlockPos> getBoundingWalls() {
        return boundingWalls;
    }

    public boolean contains(BlockPos pos) {
        return interior.contains(pos) || boundingWalls.contains(pos);
    }

    public boolean isWall(BlockPos pos) {
        return boundingWalls.contains(pos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("Controller", NbtUtils.writeBlockPos(controllerPos));

        ListTag interiorList = new ListTag();
        for (BlockPos pos : interior) {
            interiorList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Interior", interiorList);

        ListTag wallsList = new ListTag();
        for (BlockPos pos : boundingWalls) {
            wallsList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Walls", wallsList);

        return tag;
    }

    public static BunkerZone load(CompoundTag tag) {
        BlockPos controller = NbtUtils.readBlockPos(tag, "Controller").orElse(BlockPos.ZERO);

        Set<BlockPos> interior = new HashSet<>();
        ListTag interiorList = tag.getList("Interior", Tag.TAG_COMPOUND);
        for (int i = 0; i < interiorList.size(); i++) {
            NbtUtils.readBlockPos(interiorList.getCompound(i), "").ifPresent(interior::add);
        }

        Set<BlockPos> walls = new HashSet<>();
        ListTag wallsList = tag.getList("Walls", Tag.TAG_COMPOUND);
        for (int i = 0; i < wallsList.size(); i++) {
            NbtUtils.readBlockPos(wallsList.getCompound(i), "").ifPresent(walls::add);
        }

        return new BunkerZone(controller, interior, walls);
    }
}
