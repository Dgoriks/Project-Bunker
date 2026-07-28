package dg.projectbunker.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class BunkerSavedData extends SavedData {

    private static final String DATA_NAME = "project_bunker_zones";
    private final Map<BlockPos, BunkerZone> zones = new HashMap<>();

    public BunkerSavedData() {}

    public static SavedData.Factory<BunkerSavedData> factory() {
        return new SavedData.Factory<>(
                BunkerSavedData::new,
                BunkerSavedData::load,
                null
        );
    }

    public static BunkerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public void addZone(BunkerZone zone) {
        zones.put(zone.getControllerPos(), zone);
        setDirty();
    }

    public void removeZone(BlockPos controllerPos) {
        if (zones.remove(controllerPos) != null) {
            setDirty();
        }
    }

    public BunkerZone getZoneByController(BlockPos controllerPos) {
        return zones.get(controllerPos);
    }

    /** Проверяет, находится ли позиция внутри любой защищённой зоны (стены + интерьер). */
    public boolean isProtected(BlockPos pos) {
        for (BunkerZone zone : zones.values()) {
            if (zone.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Удаляет ВСЕ зоны, где данная позиция является стеной или частью интерьера.
     * Возвращает список удалённых зон для логирования/уведомлений.
     */
    public List<BunkerZone> invalidateZonesAt(BlockPos pos) {
        List<BunkerZone> invalidated = new ArrayList<>();
        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, BunkerZone> entry : zones.entrySet()) {
            BunkerZone zone = entry.getValue();
            if (zone.isWall(pos) || zone.getInterior().contains(pos)) {
                invalidated.add(zone);
                toRemove.add(entry.getKey());
            }
        }

        for (BlockPos key : toRemove) {
            zones.remove(key);
        }

        if (!toRemove.isEmpty()) {
            setDirty();
        }

        return invalidated;
    }

    public Collection<BunkerZone> getAllZones() {
        return Collections.unmodifiableCollection(zones.values());
    }

    public static BunkerSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        BunkerSavedData data = new BunkerSavedData();
        ListTag list = tag.getList("Zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            BunkerZone zone = BunkerZone.load(list.getCompound(i));
            data.zones.put(zone.getControllerPos(), zone);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BunkerZone zone : zones.values()) {
            list.add(zone.save());
        }
        tag.put("Zones", list);
        return tag;
    }
}