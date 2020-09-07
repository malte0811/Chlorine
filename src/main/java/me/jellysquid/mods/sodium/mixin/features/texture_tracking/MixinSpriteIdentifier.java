package me.jellysquid.mods.sodium.mixin.features.texture_tracking;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderMaterial.class)
public class MixinSpriteIdentifier {
    @Inject(method = "getSprite", at = @At("RETURN"))
    private void preReturnSprite(CallbackInfoReturnable<TextureAtlasSprite> cir) {
        TextureAtlasSprite sprite = cir.getReturnValue();

        if (sprite != null) {
            SpriteUtil.markSpriteActive(sprite);
        }
    }
}
