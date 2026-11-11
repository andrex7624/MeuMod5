package com.shopmod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aqui editas os preços! Máximo 5 categorias e 45 itens por categoria. */
public final class ShopData {
    public record Entry(Item item, double price) {}
    public record Category(String name, Item icon, List<Entry> entries) {}

    private static Entry e(Item i, double p) { return new Entry(i, p); }

    public static final List<Category> CATEGORIES = List.of(
        new Category("Recursos", Items.DIAMOND, List.of(
            e(Items.COAL, 10), e(Items.COPPER_INGOT, 8), e(Items.IRON_INGOT, 25),
            e(Items.GOLD_INGOT, 50), e(Items.REDSTONE, 12), e(Items.LAPIS_LAZULI, 15),
            e(Items.QUARTZ, 20), e(Items.AMETHYST_SHARD, 30), e(Items.EMERALD, 150),
            e(Items.DIAMOND, 300), e(Items.NETHERITE_INGOT, 6000), e(Items.NETHERITE_SCRAP, 1500)
        )),
        new Category("Armaduras", Items.DIAMOND_CHESTPLATE, List.of(
            e(Items.IRON_HELMET, 150), e(Items.IRON_CHESTPLATE, 240), e(Items.IRON_LEGGINGS, 210), e(Items.IRON_BOOTS, 120),
            e(Items.DIAMOND_HELMET, 1500), e(Items.DIAMOND_CHESTPLATE, 2400), e(Items.DIAMOND_LEGGINGS, 2100), e(Items.DIAMOND_BOOTS, 1200),
            e(Items.NETHERITE_HELMET, 9000), e(Items.NETHERITE_CHESTPLATE, 14000), e(Items.NETHERITE_LEGGINGS, 12000), e(Items.NETHERITE_BOOTS, 8000),
            e(Items.SHIELD, 200)
        )),
        new Category("Armas/Ferramentas", Items.DIAMOND_SWORD, List.of(
            e(Items.DIAMOND_SWORD, 800), e(Items.DIAMOND_AXE, 900), e(Items.DIAMOND_PICKAXE, 1000), e(Items.DIAMOND_SHOVEL, 400),
            e(Items.NETHERITE_SWORD, 7000), e(Items.NETHERITE_AXE, 7500), e(Items.NETHERITE_PICKAXE, 8000), e(Items.NETHERITE_SHOVEL, 6500),
            e(Items.BOW, 100), e(Items.CROSSBOW, 150), e(Items.ARROW, 5)
        )),
        new Category("Comida", Items.COOKED_BEEF, List.of(
            e(Items.BREAD, 5), e(Items.COOKED_BEEF, 10), e(Items.COOKED_PORKCHOP, 10),
            e(Items.COOKED_SALMON, 8), e(Items.GOLDEN_CARROT, 20), e(Items.GOLDEN_APPLE, 400)
        )),
        new Category("Extras", Items.TOTEM_OF_UNDYING, List.of(
            e(Items.TOTEM_OF_UNDYING, 2500), e(Items.ENDER_PEARL, 150), e(Items.EXPERIENCE_BOTTLE, 80),
            e(Items.FIREWORK_ROCKET, 30), e(Items.OBSIDIAN, 40), e(Items.COBBLESTONE, 2),
            e(Items.STONE, 3), e(Items.OAK_LOG, 4), e(Items.GLASS, 5), e(Items.SAND, 3)
        ))
    );

    /** Preços de venda no /sell (por unidade). Só estes itens são comprados. */
    public static final Map<Item, Double> SELL = new LinkedHashMap<>();
    private static void s(Item i, double p) { SELL.put(i, p); }

    static {
        s(Items.COBBLESTONE, 0.5); s(Items.STONE, 0.5); s(Items.DEEPSLATE, 0.6); s(Items.DIRT, 0.2);
        s(Items.SAND, 0.5); s(Items.GRAVEL, 0.4); s(Items.NETHERRACK, 0.3); s(Items.END_STONE, 1);
        s(Items.OAK_LOG, 1); s(Items.SPRUCE_LOG, 1); s(Items.BIRCH_LOG, 1); s(Items.OBSIDIAN, 10);
        s(Items.COAL, 3); s(Items.COPPER_INGOT, 2); s(Items.IRON_INGOT, 8); s(Items.GOLD_INGOT, 15);
        s(Items.REDSTONE, 3); s(Items.LAPIS_LAZULI, 4); s(Items.QUARTZ, 5); s(Items.EMERALD, 50);
        s(Items.DIAMOND, 100); s(Items.NETHERITE_INGOT, 2500);
        s(Items.COAL_BLOCK, 27); s(Items.IRON_BLOCK, 72);
        s(Items.GOLD_BLOCK, 135); s(Items.REDSTONE_BLOCK, 27); s(Items.LAPIS_BLOCK, 36);
        s(Items.EMERALD_BLOCK, 450); s(Items.DIAMOND_BLOCK, 900); s(Items.NETHERITE_BLOCK, 22500);
        s(Items.WHEAT, 1); s(Items.CARROT, 1); s(Items.POTATO, 1); s(Items.BONE, 1);
        s(Items.BONE_MEAL, 0.3); s(Items.BONE_BLOCK, 3); s(Items.ROTTEN_FLESH, 0.3); s(Items.STRING, 1); s(Items.GUNPOWDER, 3); s(Items.ENDER_PEARL, 40);
    }

    private static final Map<Item, Double> BUY = new HashMap<>();
    static {
        for (Category c : CATEGORIES) for (Entry en : c.entries()) BUY.put(en.item(), en.price());
    }

    /**
     * Preço de venda de QUALQUER item:
     * 1) preço definido em SELL; 2) 40% do preço da loja; 3) padrão ($5 se não empilha, senão $0.25).
     */
    public static double sellPrice(ItemStack s) {
        Item i = s.getItem();
        Double p = SELL.get(i);
        if (p != null) return p;
        Double b = BUY.get(i);
        if (b != null) return b * 0.4;
        return s.getMaxStackSize() == 1 ? 5.0 : 0.25;
    }

    private ShopData() {}
}
