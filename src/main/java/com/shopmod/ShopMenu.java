package com.shopmod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

/** Menu da /shop: menu principal (3 linhas) e página de categoria (6 linhas). Totalmente bloqueado. */
public class ShopMenu extends ChestMenu {
    private final ServerPlayer player;
    private final ShopData.Category cat;
    private final int size;

    private ShopMenu(int id, Inventory inv, Container c, int rows, ServerPlayer p, ShopData.Category cat) {
        super(rows == 6 ? MenuType.GENERIC_9x6 : MenuType.GENERIC_9x3, id, inv, c, rows);
        this.player = p;
        this.cat = cat;
        this.size = rows * 9;
    }

    private static Component line(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(Style.EMPTY.withItalic(false).withColor(color));
    }

    private static ItemStack icon(Item item, String name, List<Component> lore) {
        ItemStack s = new ItemStack(item);
        s.set(DataComponents.CUSTOM_NAME, line(name, ChatFormatting.YELLOW));
        if (!lore.isEmpty()) s.set(DataComponents.LORE, new ItemLore(lore));
        return s;
    }

    public static void open(ServerPlayer p, ShopData.Category cat) {
        int rows = cat == null ? 3 : 6;
        SimpleContainer c = new SimpleContainer(rows * 9);

        ItemStack pane = new ItemStack(Items.GLASS);
        pane.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < c.getContainerSize(); i++) c.setItem(i, pane.copy());

        if (cat == null) {
            for (int i = 0; i < ShopData.CATEGORIES.size(); i++) {
                ShopData.Category k = ShopData.CATEGORIES.get(i);
                c.setItem(11 + i, icon(k.icon(), k.name(), List.of(line("Clica para abrir", ChatFormatting.GRAY))));
            }
        } else {
            for (int i = 0; i < 45; i++) c.setItem(i, ItemStack.EMPTY);
            for (int i = 0; i < cat.entries().size() && i < 45; i++) {
                ShopData.Entry en = cat.entries().get(i);
                ItemStack s = new ItemStack(en.item());
                s.set(DataComponents.LORE, new ItemLore(List.of(
                    line("Preço: " + Economy.fmt(en.price()) + " cada", ChatFormatting.GREEN),
                    line("Clique esquerdo: x1", ChatFormatting.GRAY),
                    line("Clique direito: x16", ChatFormatting.GRAY),
                    line("Shift + clique: x64", ChatFormatting.GRAY))));
                c.setItem(i, s);
            }
            c.setItem(49, icon(Items.ARROW, "Voltar", List.of()));
        }

        final int r = rows;
        p.openMenu(new SimpleMenuProvider(
            (id, inv, pl) -> new ShopMenu(id, inv, c, r, p, cat),
            Component.literal(cat == null ? "Shop" : "Shop - " + cat.name())));
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput type, Player p) {
        if (slotId >= 0 && slotId < size) {
            if (type == ContainerInput.PICKUP || type == ContainerInput.QUICK_MOVE) handle(slotId, button, type);
            sendAllDataToRemote();
            return;
        }
        if (type == ContainerInput.QUICK_MOVE || type == ContainerInput.PICKUP_ALL) return;
        super.clicked(slotId, button, type, p);
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) { return ItemStack.EMPTY; }

    private void handle(int slot, int button, ContainerInput type) {
        if (cat == null) {
            int idx = slot - 11;
            if (idx >= 0 && idx < ShopData.CATEGORIES.size()) open(player, ShopData.CATEGORIES.get(idx));
            return;
        }
        if (slot == 49) { open(player, null); return; }
        if (slot < cat.entries().size()) {
            int want = type == ContainerInput.QUICK_MOVE ? 64 : (button == 1 ? 16 : 1);
            buy(cat.entries().get(slot), want);
        }
    }

    private void buy(ShopData.Entry e, int want) {
        int amount = Math.min(want, new ItemStack(e.item()).getMaxStackSize());
        double cost = e.price() * amount;
        if (!Economy.take(player.getUUID(), cost)) {
            player.sendSystemMessage(line("Dinheiro insuficiente! Precisas de " + Economy.fmt(cost)
                + " (tens " + Economy.fmt(Economy.get(player.getUUID())) + ")", ChatFormatting.RED));
            return;
        }
        ItemStack s = new ItemStack(e.item(), amount);
        player.getInventory().add(s);
        if (!s.isEmpty()) Containers.dropItemStack(player.level(), player.getX(), player.getY(), player.getZ(), s);
        player.sendSystemMessage(line("Compraste " + amount + "x " + new ItemStack(e.item()).getHoverName().getString()
            + " por " + Economy.fmt(cost), ChatFormatting.GREEN));
    }
}
