package traderush.platform.ui.shop;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import traderush.TradeRush;
import traderush.game.offer.ActiveOffer;
import traderush.game.offer.ItemRequirement;
import traderush.game.offer.Offer;
import traderush.game.offer.OfferId;
import traderush.game.offer.OfferOperationResult;
import traderush.game.offer.OfferUnit;
import traderush.game.player.PlayerId;
import traderush.game.shop.Shop;
import traderush.game.shop.ShopId;
import traderush.game.team.Team;
import traderush.game.team.TeamOperationResult;
import traderush.runtime.TradeRushRuntime;

public final class ShopNetworking {

    private static boolean payloadTypesRegistered;
    private static boolean serverHandlersRegistered;

    private ShopNetworking() {}

    public static void registerPayloadTypes() {
        if (payloadTypesRegistered) {
            return;
        }

        PayloadTypeRegistry.clientboundPlay()
                .register(ShopOffersPayload.TYPE, ShopOffersPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(
                        ShopTradeResultPayload.TYPE,
                        ShopTradeResultPayload.CODEC
                );
        PayloadTypeRegistry.serverboundPlay()
                .register(
                        ShopTradeActionPayload.TYPE,
                        ShopTradeActionPayload.CODEC
                );

        payloadTypesRegistered = true;
    }

    public static void registerServerHandlers() {
        if (serverHandlersRegistered) {
            return;
        }

        ServerPlayNetworking.registerGlobalReceiver(
                ShopTradeActionPayload.TYPE,
                (payload, context) -> context.server()
                        .execute(() -> handleTrade(payload, context.player()))
        );

        serverHandlersRegistered = true;
    }

    // ── send offers
    // ───────────────────────────────────────────────────────────

    public static void sendOffers(ServerPlayer player, ShopId shopId) {
        if (!ServerPlayNetworking.canSend(player, ShopOffersPayload.TYPE)) {
            return;
        }

        TradeRushRuntime runtime;

        try {
            runtime = TradeRush.runtime();
        } catch (IllegalStateException ignored) {
            return;
        }

        Shop shop = runtime.shopService()
                .listShops()
                .stream()
                .filter(s -> s.getId().equals(shopId))
                .findFirst()
                .orElse(null);

        if (shop == null) {
            return;
        }

        long serverTick = player.level().getGameTime();
        List<ShopOfferEntry> entries = buildOfferEntries(
                runtime,
                shopId,
                serverTick
        );

        ServerPlayNetworking.send(
                player,
                new ShopOffersPayload(
                        shopId.toString(),
                        shop.getName(),
                        serverTick,
                        entries
                )
        );
    }

    // ── trade handler
    // ─────────────────────────────────────────────────────────

    private static void handleTrade(
            ShopTradeActionPayload payload,
            ServerPlayer player
    ) {
        if (!(player.containerMenu instanceof ShopOffersMenu)) {
            return;
        }

        TradeRushRuntime runtime;

        try {
            runtime = TradeRush.runtime();
        } catch (IllegalStateException ignored) {
            sendTradeResult(player, false, "Server not ready.", 0);
            return;
        }

        Optional<Offer> offerOpt = runtime.offerRepository()
                .getById(parseOfferId(payload.offerId()));

        if (offerOpt.isEmpty()) {
            sendTradeResult(player, false, "Offer not found.", 0);
            return;
        }

        Offer offer = offerOpt.get();

        if (offer.getUnits().isEmpty()) {
            sendTradeResult(player, false, "Offer has no requirements.", 0);
            return;
        }

        OfferUnit unit = offer.getUnits().get(0);
        ShopId shopId = parseShopId(payload.shopId());
        long currentTick = player.level().getGameTime();
        Optional<ActiveOffer> activeOffer = runtime.offerService()
                .findAcceptingActiveOffer(
                        shopId,
                        offer.getId(),
                        currentTick
                );

        if (activeOffer.isEmpty()) {
            sendTradeResult(player, false, "Offer is not active.", 0);
            return;
        }

        if (!hasRequiredItems(player, unit)) {
            sendTradeResult(
                    player,
                    false,
                    "Not enough items in inventory.",
                    0
            );
            return;
        }

        removeRequiredItems(player, unit);

        long reward = activeOffer.get().getRewardPerUnit();

        PlayerId playerId = PlayerId.fromUuid(player.getUUID());
        Optional<Team> team = runtime.teamService().getTeamForPlayer(playerId);

        if (team.isEmpty()) {
            sendTradeResult(
                    player,
                    false,
                    "You are not in a team — items removed but no points awarded.",
                    0
            );
            return;
        }

        TeamOperationResult<Team> awardResult = runtime.teamService()
                .awardPoints(team.get().getId(), reward);

        if (!awardResult.isSuccess()) {
            sendTradeResult(
                    player,
                    false,
                    "Could not award points to your team.",
                    0
            );
            return;
        }

        sendTradeResult(player, true, "Trade complete!", reward);
    }

    private static List<ShopOfferEntry> buildOfferEntries(
            TradeRushRuntime runtime,
            ShopId shopId,
            long currentTick
    ) {
        return runtime.offerRepository()
                .getAll()
                .stream()
                .map(offer -> {
                    OfferOperationResult<ActiveOffer> result = runtime
                            .offerService()
                            .ensureActiveOffer(
                                    shopId,
                                    offer.getId(),
                                    currentTick
                            );

                    if (result.isFailure()) {
                        return null;
                    }

                    return ShopOfferEntry.from(offer, result.value());
                })
                .filter(entry -> entry != null)
                .toList();
    }

    // ── inventory helpers
    // ─────────────────────────────────────────────────────

    private static boolean hasRequiredItems(
            ServerPlayer player,
            OfferUnit unit
    ) {
        for (ItemRequirement req : unit.requirements()) {
            Item item = resolveItem(req.itemId().value());

            if (item == null) {
                return false;
            }

            int available = countItems(player, item);

            if (available < req.quantity()) {
                return false;
            }
        }

        return true;
    }

    private static void removeRequiredItems(
            ServerPlayer player,
            OfferUnit unit
    ) {
        for (ItemRequirement req : unit.requirements()) {
            Item item = resolveItem(req.itemId().value());

            if (item == null) {
                continue;
            }

            int toRemove = req.quantity();
            net.minecraft.world.entity.player.Inventory inv = player
                    .getInventory();

            for (int i = 0; i < inv.getContainerSize() && toRemove > 0; i++) {
                ItemStack stack = inv.getItem(i);

                if (stack.getItem() == item) {
                    int take = Math.min(stack.getCount(), toRemove);
                    stack.shrink(take);
                    toRemove -= take;
                }
            }
        }
    }

    private static int countItems(ServerPlayer player, Item item) {
        int total = 0;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.getItem() == item) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private static Item resolveItem(String itemId) {
        try {
            Identifier id = Identifier.parse(itemId);
            return BuiltInRegistries.ITEM.getValue(id);
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── misc helpers
    // ──────────────────────────────────────────────────────────

    private static OfferId parseOfferId(String value) {
        try {
            return OfferId.fromString(value);
        } catch (Exception ignored) {
            return OfferId.fromName(value);
        }
    }

    private static ShopId parseShopId(String value) {
        return ShopId.fromString(value);
    }

    private static void sendTradeResult(
            ServerPlayer player,
            boolean success,
            String message,
            long points
    ) {
        if (!ServerPlayNetworking
                .canSend(player, ShopTradeResultPayload.TYPE)) {
            return;
        }

        ServerPlayNetworking.send(
                player,
                new ShopTradeResultPayload(success, message, points)
        );
    }
}
