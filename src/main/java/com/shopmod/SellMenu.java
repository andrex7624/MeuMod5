package com.shopmod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** /sell: o jogador mete itens e, ao fechar, recebe o dinheiro. Itens não vendáveis são devolvidos. */
public class SellMenu extends ChestMenu {
    private final SimpleContainer box;
    private final ServerPlayer player;

    private SellMenu(int id, Inventory inv, SimpleContainer box, ServerPlayer p) {
        super(MenuType.GENERIC_9x6, id, inv, box, 6);
        this.box = box;
        this.player = p;
    }

    public static void open(ServerPlayer p) {
        SimpleContainer box = new SimpleContainer(54);
        p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new SellMenu(id, inv, box, p),
            Component.literal("Sell - mete os itens aqui e fecha")));
    }

    @Override
    public void removed(Player p) {
        super.removed(p); // devolve o item do cursor
        double total = 0;
        long count = 0;
        for (int i = 0; i < box.getContainerSize(); i++) {
            ItemStack s = box.getItem(i);
            if (s.isEmpty()) continue;
            total += ShopData.sellPrice(s) * s.getCount();
            count += s.getCount();
            box.setItem(i, ItemStack.EMPTY);
        }
        if (count > 0) {
            Economy.add(player.getUUID(), total);
            player.sendSystemMessage(Component.literal("Vendeste " + count + " itens por " + Economy.fmt(total)
                + " | Saldo: " + Economy.fmt(Economy.get(player.getUUID()))).withStyle(ChatFormatting.GREEN));
        }
    }
}
