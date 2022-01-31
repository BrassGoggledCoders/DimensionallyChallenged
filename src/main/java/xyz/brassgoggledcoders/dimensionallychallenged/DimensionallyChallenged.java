package xyz.brassgoggledcoders.dimensionallychallenged;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.brassgoggledcoders.dimensionallychallenged.setting.DimensionalSettingsManager;

@Mod(DimensionallyChallenged.ID)
public class DimensionallyChallenged {
    public static final String ID = "dimensionally_challenged";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final DimensionalSettingsManager DIMENSIONAL_SETTINGS_MANAGER = new DimensionalSettingsManager();
}
