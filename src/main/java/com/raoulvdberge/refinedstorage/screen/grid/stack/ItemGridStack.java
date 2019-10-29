package com.raoulvdberge.refinedstorage.screen.grid.stack;

import com.raoulvdberge.refinedstorage.api.storage.tracker.StorageTrackerEntry;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import com.raoulvdberge.refinedstorage.screen.BaseScreen;
import com.raoulvdberge.refinedstorage.util.RenderUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemGridStack implements IGridStack {
    private Logger logger = LogManager.getLogger(getClass());

    private UUID id;
    private ItemStack stack;
    private String cachedName;
    private boolean craftable;
    private String[] oreIds = null;
    @Nullable
    private StorageTrackerEntry entry;

    private Set<String> cachedTags;
    private String cachedModId;
    private String cachedModName;
    private String cachedTooltip;

    public ItemGridStack(ItemStack stack) {
        this.stack = stack;
    }

    public ItemGridStack(UUID id, ItemStack stack, boolean craftable, StorageTrackerEntry entry) {
        this.id = id;
        this.stack = stack;
        this.craftable = craftable;
        this.entry = entry;
    }

    @Nullable
    static String getModNameByModId(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);

        return modContainer.map(container -> container.getModInfo().getDisplayName()).orElse(null);
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean isCraftable() {
        return craftable;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        if (cachedName == null) {
            try {
                cachedName = stack.getDisplayName().getFormattedText();
            } catch (Throwable t) {
                logger.warn("Could not retrieve item name of " + stack.getItem().toString(), t);

                cachedName = "<Error>";
            }
        }

        return cachedName;
    }

    @Override
    public String getModId() {
        if (cachedModId == null) {
            cachedModId = stack.getItem().getCreatorModId(stack);

            if (cachedModId == null) {
                cachedModId = "<Error>";
            }
        }

        return cachedModId;
    }

    @Override
    public String getModName() {
        if (cachedModName == null) {
            cachedModName = getModNameByModId(getModId());

            if (cachedModName == null) {
                cachedModName = "<Error>";
            }
        }

        return cachedModName;
    }

    @Override
    public Set<String> getTags() {
        if (cachedTags == null) {
            cachedTags = new HashSet<>();

            for (ResourceLocation owningTag : ItemTags.getCollection().getOwningTags(stack.getItem())) {
                cachedTags.add(owningTag.getPath());
            }
        }

        return cachedTags;
    }

    @Override
    public String getTooltip() {
        if (cachedTooltip == null) {
            try {
                cachedTooltip = RenderUtils.getTooltipFromItem(stack).stream().collect(Collectors.joining("\n"));
            } catch (Throwable t) {
                logger.warn("Could not retrieve item tooltip of " + stack.getItem().toString(), t);

                cachedTooltip = "<Error>";
            }
        }

        return cachedTooltip;
    }

    @Override
    public int getQuantity() {
        // The isCraftable check is needed so sorting is applied correctly
        return isCraftable() ? 0 : stack.getCount();
    }

    @Override
    public String getFormattedFullQuantity() {
        return API.instance().getQuantityFormatter().format(getQuantity());
    }

    @Override
    public void draw(BaseScreen gui, int x, int y) {
        String text = null;

        if (craftable) {
            text = I18n.format("gui.refinedstorage.grid.craft");
        } else if (stack.getCount() > 1) {
            text = API.instance().getQuantityFormatter().formatWithUnits(getQuantity());
        }

        gui.renderItem(x, y, stack, true, text);
    }

    @Override
    public Object getIngredient() {
        return stack;
    }

    @Nullable
    @Override
    public StorageTrackerEntry getTrackerEntry() {
        return entry;
    }

    @Override
    public void setTrackerEntry(@Nullable StorageTrackerEntry entry) {
        this.entry = entry;
    }
}
