package appeng.client.guidebook;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.navigation.NavigationTree;

public interface PageCollection {
    @Nullable
    ParsedGuidePage getParsedPage(ResourceLocation id);

    @Nullable
    GuidePage getPage(ResourceLocation id);

    byte[] loadAsset(ResourceLocation id);

    NavigationTree getNavigationTree();

    boolean pageExists(ResourceLocation pageId);
}