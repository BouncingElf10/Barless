package com.bouncingelf10.barless;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarlessClient implements ClientModInitializer {
	public static final String MOD_ID = "barless";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final boolean IS_MAC = Util.getPlatform() == Util.OS.OSX;

	@Override
	public void onInitializeClient() {

	}
}