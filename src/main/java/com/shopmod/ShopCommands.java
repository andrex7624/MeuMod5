package com.shopmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ShopCommands {
    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("shop").executes(ctx -> {
            ShopMenu.open(ctx.getSource().getPlayerOrException(), null);
            return 1;
        }));

        d.register(Commands.literal("sell").executes(ctx -> {
            SellMenu.open(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        LiteralCommandNode<CommandSourceStack> bal = d.register(Commands.literal("balance").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            p.sendSystemMessage(Component.literal("Saldo: " + Economy.fmt(Economy.get(p.getUUID())))
                .withStyle(ChatFormatting.GOLD));
            return 1;
        }));
        d.register(Commands.literal("bal").executes(bal.getCommand()).redirect(bal));

        d.register(Commands.literal("pay")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                    .executes(ctx -> {
                        ServerPlayer from = ctx.getSource().getPlayerOrException();
                        ServerPlayer to = EntityArgument.getPlayer(ctx, "player");
                        double amt = DoubleArgumentType.getDouble(ctx, "amount");
                        if (from.getUUID().equals(to.getUUID())) {
                            from.sendSystemMessage(Component.literal("Não podes pagar a ti próprio.").withStyle(ChatFormatting.RED));
                            return 0;
                        }
                        if (!Economy.take(from.getUUID(), amt)) {
                            from.sendSystemMessage(Component.literal("Dinheiro insuficiente.").withStyle(ChatFormatting.RED));
                            return 0;
                        }
                        Economy.add(to.getUUID(), amt);
                        from.sendSystemMessage(Component.literal("Pagaste " + Economy.fmt(amt) + " a "
                            + to.getName().getString()).withStyle(ChatFormatting.GREEN));
                        to.sendSystemMessage(Component.literal(from.getName().getString() + " enviou-te "
                            + Economy.fmt(amt)).withStyle(ChatFormatting.GREEN));
                        return 1;
                    }))));
    }

    private ShopCommands() {}
}
