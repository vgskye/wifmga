package vg.skye.wifmga;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

@me.shedaniel.autoconfig.annotation.Config(name = Wifmga.MOD_ID)
public class Config implements ConfigData {
    public static final Config INSTANCE = AutoConfig
            .register(Config.class, JanksonConfigSerializer::new)
            .getConfig();

    @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
    @ConfigEntry.Gui.Tooltip
    public int iterations = 3;
    @ConfigEntry.Gui.Tooltip
    public float offset = 1;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int gradientStart = 0xc0101010;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int gradientEnd = 0xd0101010;
}