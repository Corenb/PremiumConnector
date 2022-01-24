package eu.horyzon.premiumconnector.sql;

import eu.horyzon.premiumconnector.util.Utils;
import net.md_5.bungee.config.Configuration;

public enum Columns {
    NAME("columnName", "Name"), PREMIUM("columnPremium", "Premium"), BEDROCK("columnBedrock", "Bedrock");

    private String key, value;

    private Columns(String key, String name) {
        this.key = key;
        this.value = name;
    }

	public static void setup(Configuration config) {
		for (Columns column : values()) {
			if (!config.contains(column.key))
				config.set(column.key, column.value);

			column.set(Utils.parseColors(config.getString(column.key, column.value)));
		}
	}

	private void set(String name) {
		this.value = name;
	}

    public String getName() {
        return value;
    }
}