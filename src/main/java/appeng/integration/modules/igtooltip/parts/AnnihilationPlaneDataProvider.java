package appeng.integration.modules.igtooltip.parts;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;

public class AnnihilationPlaneDataProvider
        implements BodyProvider<AnnihilationPlanePart>, ServerDataProvider<AnnihilationPlanePart> {
    private static final String TAG_ENCHANTMENTS = "planeEnchantments";

    @Override
    public void buildTooltip(AnnihilationPlanePart plane, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        if (serverData.contains(TAG_ENCHANTMENTS, Tag.TAG_COMPOUND)) {
            tooltip.addLine(InGameTooltip.EnchantedWith.text());

            var enchantments = serverData.getCompound(TAG_ENCHANTMENTS);
            for (var enchantmentId : enchantments.getAllKeys()) {
                var enchantment = BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(enchantmentId));
                var level = enchantments.getInt(enchantmentId);
                if (enchantment != null) {
                    tooltip.addLine(enchantment.getFullname(level));
                }
            }
        }
    }

    @Override
    public void provideServerData(Player player, AnnihilationPlanePart plane, CompoundTag serverData) {
        var enchantments = plane.getEnchantments();
        if (enchantments != null && !enchantments.isEmpty()) {
            var enchantmentsTag = new CompoundTag();
            for (var entry : enchantments.entrySet()) {
                enchantmentsTag.putInt(entry.getKey().getRegisteredName(), entry.getIntValue());
            }
            serverData.put(TAG_ENCHANTMENTS, enchantmentsTag);
        }
    }
}
