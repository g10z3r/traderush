package traderush.platform.offer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.TradeRush;
import traderush.game.offer.LimitedOffer;
import traderush.game.offer.Offer;
import traderush.game.offer.OfferRepository;
import traderush.game.offer.TimedOffer;
import traderush.platform.offer.OfferJsonDto.LimitedOfferDto;
import traderush.platform.offer.OfferJsonDto.TimedOfferDto;

/**
 * Loads offer definitions from {@code data/<namespace>/offers/limited/} and
 * {@code data/<namespace>/offers/timed/} on every datapack reload (server start
 * + {@code /reload}).
 *
 * <p>
 * Because the reload fires during server startup — before
 * {@link traderush.runtime.TradeRushRuntime} exists — parsed offers are cached
 * in a static field. Call {@link #applyCachedOffers(OfferRepository)} once the
 * runtime (and therefore its repository) is available.
 *
 * <p>
 * Files may contain either a single JSON object or a JSON array of objects.
 */
public final class OfferDataLoader
        implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(OfferDataLoader.class);

    private static final String PATH_LIMITED = "offers/limited";
    private static final String PATH_TIMED = "offers/timed";
    private static final Identifier LOADER_ID = TradeRush
            .id("offer_data_loader");
    private static final Gson GSON = new Gson();

    /**
     * Offers parsed during the most recent reload — may predate the runtime.
     */
    private static volatile List<Offer> cachedOffers = List.of();

    private OfferDataLoader() {}

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new OfferDataLoader());
    }

    /**
     * Copies every cached offer into {@code repository}. Call this after the
     * runtime is ready (e.g. inside the {@code TradeRushRuntime} constructor).
     */
    public static void applyCachedOffers(OfferRepository repository) {
        List<Offer> snapshot = cachedOffers;

        for (Offer offer : snapshot) {
            repository.put(offer);
        }

        LOGGER.info(
                "Applied {} cached offer(s) to repository.",
                snapshot.size()
        );
    }

    @Override
    public Identifier getFabricId() {
        return LOADER_ID;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        List<Offer> loaded = new ArrayList<>();
        loaded.addAll(loadLimited(manager));
        loaded.addAll(loadTimed(manager));

        cachedOffers = List.copyOf(loaded);

        LOGGER.info(
                "Offer data loaded: {} offer(s) cached.",
                loaded.size()
        );
    }

    private static List<LimitedOffer> loadLimited(ResourceManager manager) {
        List<LimitedOffer> result = new ArrayList<>();

        Map<Identifier, Resource> resources = manager
                .listResources(
                        PATH_LIMITED,
                        id -> id.getPath().endsWith(".json")
                );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement root = GSON.fromJson(reader, JsonElement.class);

                for (JsonObject obj : asObjectList(root, entry.getKey())) {
                    LimitedOfferDto dto = GSON
                            .fromJson(obj, LimitedOfferDto.class);
                    result.add(OfferDtoMapper.toLimitedOffer(dto));
                }
            } catch (IOException | JsonParseException
                    | IllegalArgumentException e) {
                LOGGER.error(
                        "Failed to load limited offer from {}",
                        entry.getKey(),
                        e
                );
            }
        }

        return result;
    }

    private static List<TimedOffer> loadTimed(ResourceManager manager) {
        List<TimedOffer> result = new ArrayList<>();

        Map<Identifier, Resource> resources = manager
                .listResources(
                        PATH_TIMED,
                        id -> id.getPath().endsWith(".json")
                );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement root = GSON.fromJson(reader, JsonElement.class);

                for (JsonObject obj : asObjectList(root, entry.getKey())) {
                    TimedOfferDto dto = GSON.fromJson(obj, TimedOfferDto.class);
                    result.add(OfferDtoMapper.toTimedOffer(dto));
                }
            } catch (IOException | JsonParseException
                    | IllegalArgumentException e) {
                LOGGER.error(
                        "Failed to load timed offer from {}",
                        entry.getKey(),
                        e
                );
            }
        }

        return result;
    }

    private static List<JsonObject> asObjectList(
            JsonElement root,
            Identifier source
    ) {
        List<JsonObject> objects = new ArrayList<>();

        if (root.isJsonObject()) {
            objects.add(root.getAsJsonObject());
        } else if (root.isJsonArray()) {
            JsonArray array = root.getAsJsonArray();

            for (JsonElement element : array) {
                if (element.isJsonObject()) {
                    objects.add(element.getAsJsonObject());
                } else {
                    LOGGER.warn(
                            "Skipping non-object element in array in {}",
                            source
                    );
                }
            }
        } else {
            LOGGER.warn("Unexpected JSON root element type in {}", source);
        }

        return objects;
    }
}
