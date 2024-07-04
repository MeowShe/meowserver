package io.meowresearch.mcserver.s4;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import io.meowresearch.mcserver.s4.entity.GuardEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MeowServerS4 implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("meow-server-s4");
	public static final EntityType<GuardEntity> NPC = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of("meow", "defender"),
			EntityType.Builder.create(GuardEntity::new, SpawnGroup.CREATURE).dimensions(0.6f,1.8f).build()
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		PolymerEntityUtils.registerType(NPC);
		FabricDefaultAttributeRegistry.register(NPC, GuardEntity.createDefenderAttributes().build());
		LOGGER.info("Meow Server S4 Plugin initialized");
	}
}