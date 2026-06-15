package traderush.game.sale;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import traderush.game.offer.ActiveOffer;
import traderush.game.offer.ActiveOfferId;
import traderush.game.offer.ActiveOfferRepository;
import traderush.game.offer.OfferRepository;
import traderush.game.player.PlayerId;
import traderush.game.shop.Shop;
import traderush.game.shop.ShopId;
import traderush.game.shop.ShopRepository;
import traderush.game.team.Team;
import traderush.game.team.TeamService;

public final class SaleService {
    private final ShopRepository shopRepository;
    private final TeamService teamService;
    private final OfferRepository offerRepository;
    private final ActiveOfferRepository activeOfferRepository;
    private final SaleCalculator saleCalculator;
    private final Runnable onStateChanged;

    public SaleService(
            ShopRepository shopRepository,
            TeamService teamService,
            OfferRepository offerRepository,
            ActiveOfferRepository activeOfferRepository,
            SaleCalculator saleCalculator,
            Runnable onStateChanged
    ) {
        this.shopRepository = Objects
                .requireNonNull(
                        shopRepository,
                        "shop repository cannot be null"
                );
        this.teamService = Objects
                .requireNonNull(teamService, "team service cannot be null");
        this.offerRepository = Objects
                .requireNonNull(
                        offerRepository,
                        "offer repository cannot be null"
                );
        this.activeOfferRepository = Objects
                .requireNonNull(
                        activeOfferRepository,
                        "active offer repository cannot be null"
                );
        this.saleCalculator = Objects
                .requireNonNull(
                        saleCalculator,
                        "sale calculator cannot be null"
                );
        this.onStateChanged = Objects
                .requireNonNull(
                        onStateChanged,
                        "state change callback cannot be null"
                );
    }

    public SaleOperationResult<SalePreview> previewSale(
            ShopId shopId,
            List<SaleItemStack> items,
            long currentTick
    ) {
        Shop shop = shopRepository.getById(shopId).orElse(null);
        if (shop == null) {
            return SaleOperationResult.failure(SaleError.SHOP_NOT_FOUND);
        }

        List<ActiveOffer> activeOffers = activeOfferRepository
                .getAllByShopId(shopId)
                .stream()
                .filter(activeOffer -> activeOffer.isAccepting(currentTick))
                .toList();
        if (activeOffers.isEmpty()) {
            return SaleOperationResult.failure(SaleError.NO_ACTIVE_OFFERS);
        }

        SalePreview preview = saleCalculator
                .calculate(
                        shopId,
                        items,
                        activeOffers,
                        offerRepository,
                        currentTick
                );
        return SaleOperationResult.success(preview);
    }

    public SaleOperationResult<SaleTransaction> commitSale(
            PlayerId playerId,
            ShopId shopId,
            List<SaleItemStack> items,
            long currentTick
    ) {
        Team team = teamService.getTeamForPlayer(playerId).orElse(null);
        if (team == null) {
            return SaleOperationResult.failure(SaleError.PLAYER_NOT_IN_TEAM);
        }

        SaleOperationResult<SalePreview> previewResult = previewSale(
                shopId,
                items,
                currentTick
        );
        if (previewResult.isFailure()) {
            return SaleOperationResult.failure(previewResult.error());
        }

        SalePreview preview = previewResult.value();
        if (!preview.hasSellableItems()) {
            return SaleOperationResult.failure(SaleError.NOTHING_TO_SELL);
        }

        Map<ActiveOfferId, Integer> unitsToSellByOfferId = preview.lines()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                SaleLine::activeOfferId,
                                Collectors.summingInt(SaleLine::units)
                        )
                );

        for (Map.Entry<ActiveOfferId, Integer> entry : unitsToSellByOfferId
                .entrySet()) {
            ActiveOffer activeOffer = activeOfferRepository
                    .getById(entry.getKey())
                    .orElse(null);
            if (activeOffer == null) {
                continue;
            }

            activeOffer.acceptUnits(entry.getValue(), currentTick);
            activeOfferRepository.put(activeOffer);
        }

        teamService.awardPoints(team.getId(), preview.totalPoints());

        SaleTransaction transaction = new SaleTransaction(
                SaleId.fromUuid(UUID.randomUUID()),
                shopId,
                team.getId(),
                playerId,
                preview.lines(),
                preview.totalPoints()
        );

        onStateChanged.run();
        return SaleOperationResult.success(transaction);
    }
}
