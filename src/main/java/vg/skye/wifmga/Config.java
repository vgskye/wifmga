package vg.skye.wifmga;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

@me.shedaniel.autoconfig.annotation.Config(name = Wifmga.MOD_ID)
public class Config implements ConfigData {
    public static void init() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
    }
    public static Config get() {
        return AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    // You need a display with small-side resolution of at least 8192 to hit 13.
    // No currently available display is that high-resolution.
    // 8k with 2x MSAA clears that bounds, but who'd render at 16k?
    @ConfigEntry.BoundedDiscrete(min = 1, max = 12)
    @ConfigEntry.Gui.Tooltip
    public int iterations = 5;
    @ConfigEntry.Gui.Tooltip
    public float offset = 1;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int gradientStart = 0xc0101010;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int gradientEnd = 0xd0101010;
}