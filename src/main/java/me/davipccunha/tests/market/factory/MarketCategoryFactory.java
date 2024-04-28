package me.davipccunha.tests.market.factory;

import me.davipccunha.tests.market.model.GlobalMarketCategory;
import me.davipccunha.tests.market.model.GlobalMarketCategoryType;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.configuration.file.FileConfiguration;

public class MarketCategoryFactory {
    public static GlobalMarketCategory createCategory(FileConfiguration config, String categoryType) {
        final String key = "market-categories." + categoryType + ".";
        try {
            final GlobalMarketCategoryType type = GlobalMarketCategoryType.valueOf(categoryType);
            final String[] icon = config.getString(key + "icon").split(":");
            final int iconID = NumberUtils.toInt(icon[0]);
            final short iconData = NumberUtils.toShort(icon[1]);

            return new GlobalMarketCategory(type, iconID, iconData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category type");
        }
    }
}
