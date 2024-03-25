package vg.skye.wifmga.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vg.skye.wifmga.Config;
import vg.skye.wifmga.Wifmga;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V"))
    private void blurBackground(DrawContext context, CallbackInfo ci) {
        Wifmga.render();
    }

    @ModifyConstant(method = "renderBackground", constant = @Constant(intValue = 0xc0101010))
    private int overrideGradientStart(int constant) {
        return Config.INSTANCE.gradientStart;
    }
    @ModifyConstant(method = "renderBackground", constant = @Constant(intValue = 0xd0101010))
    private int overrideGradientEnd(int constant) {
        return Config.INSTANCE.gradientEnd;
    }
}
