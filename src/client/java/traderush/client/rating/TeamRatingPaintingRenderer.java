package traderush.client.rating;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import traderush.platform.entity.TeamRatingPaintingEntity;
import traderush.platform.ui.rating.TeamRatingPaintingSnapshot;
import traderush.platform.ui.rating.TeamRatingRow;

public final class TeamRatingPaintingRenderer
        extends
        EntityRenderer<TeamRatingPaintingEntity, TeamRatingPaintingRenderState> {
    private static final float BOARD_WIDTH = 4.0F;
    private static final float BOARD_HEIGHT = 4.0F;
    private static final float FRONT_Z = -0.04F;
    private static final float TEXT_Z = FRONT_Z - 0.08F;
    private static final float TEXT_SCALE = 0.0125F;
    private static final int CANVAS_WIDTH = 288;
    private static final int TITLE_Y = 0;
    private static final int HEADER_Y = 34;
    private static final int ROW_START_Y = 56;
    private static final int ROW_STRIDE = 24;
    private static final int PLACE_X = 6;
    private static final int TEAM_X = 52;
    private static final int SCORE_RIGHT_X = 282;
    private static final int TEAM_MAX_WIDTH = 178;
    private static final int STATE_MESSAGE_Y = 164;
    private static final int PANEL_COLOR = 0xD9342519;
    private static final int TITLE_COLOR = 0xFFFFD15C;
    private static final int HEADER_COLOR = 0xFFE8C889;
    private static final int TEXT_COLOR = 0xFFF6E7C4;
    private static final int MUTED_TEXT_COLOR = 0xFFD8C8A6;
    private static final int FULL_BRIGHT_LIGHT = 0x00F000F0;

    private final Font font;

    public TeamRatingPaintingRenderer(EntityRendererProvider.Context context) {
        super(context);
        font = context.getFont();
        shadowRadius = 0.0F;
        shadowStrength = 0.0F;
    }

    @Override
    public TeamRatingPaintingRenderState createRenderState() {
        return new TeamRatingPaintingRenderState();
    }

    @Override
    public void extractRenderState(
            TeamRatingPaintingEntity entity,
            TeamRatingPaintingRenderState state,
            float tickDelta
    ) {
        super.extractRenderState(entity, state, tickDelta);
        state.direction = entity.getDirection();
        state.snapshot = RatingPaintingClientState.snapshot();
    }

    @Override
    public void submit(
            TeamRatingPaintingRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitter,
            CameraRenderState cameraState
    ) {
        poseStack.pushPose();
        poseStack.mulPose(
                Axis.YP.rotationDegrees(rotationDegrees(state.direction))
        );
        renderBackground(state, poseStack, submitter);
        renderRating(state, poseStack, submitter);
        poseStack.popPose();

        super.submit(state, poseStack, submitter, cameraState);
    }

    private void renderBackground(
            TeamRatingPaintingRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitter
    ) {
        fillRect(
                poseStack,
                submitter,
                -BOARD_WIDTH / 2.0F,
                -BOARD_HEIGHT / 2.0F,
                BOARD_WIDTH / 2.0F,
                BOARD_HEIGHT / 2.0F,
                FRONT_Z,
                PANEL_COLOR,
                FULL_BRIGHT_LIGHT
        );
    }

    private void renderRating(
            TeamRatingPaintingRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitter
    ) {
        poseStack.pushPose();
        poseStack.translate(1.8F, 1.78F, TEXT_Z);
        poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, -TEXT_SCALE);
        submitCentered(
                submitter,
                poseStack,
                Component.translatable("painting.trade-rush.team_rating.title"),
                TITLE_Y,
                TITLE_COLOR,
                FULL_BRIGHT_LIGHT
        );

        TeamRatingPaintingSnapshot snapshot = state.snapshot;
        if (snapshot == null) {
            submitStateMessage(
                    submitter,
                    poseStack,
                    Component.translatable(
                            "painting.trade-rush.team_rating.loading"
                    ),
                    FULL_BRIGHT_LIGHT
            );
        } else if (!snapshot.runtimeReady()) {
            submitStateMessage(
                    submitter,
                    poseStack,
                    Component.translatable(
                            "painting.trade-rush.team_rating.runtime_not_ready"
                    ),
                    FULL_BRIGHT_LIGHT
            );
        } else if (snapshot.rows().isEmpty()) {
            submitStateMessage(
                    submitter,
                    poseStack,
                    Component.translatable(
                            "painting.trade-rush.team_rating.empty"
                    ),
                    FULL_BRIGHT_LIGHT
            );
        } else {
            submitHeaders(submitter, poseStack, FULL_BRIGHT_LIGHT);
            submitRows(
                    submitter,
                    poseStack,
                    snapshot.rows(),
                    FULL_BRIGHT_LIGHT
            );
        }

        poseStack.popPose();
    }

    private void submitHeaders(
            SubmitNodeCollector submitter,
            PoseStack poseStack,
            int lightCoords
    ) {
        submitText(
                submitter,
                poseStack,
                Component.translatable("painting.trade-rush.team_rating.place"),
                PLACE_X,
                HEADER_Y,
                HEADER_COLOR,
                lightCoords
        );
        submitText(
                submitter,
                poseStack,
                Component.translatable("painting.trade-rush.team_rating.team"),
                TEAM_X,
                HEADER_Y,
                HEADER_COLOR,
                lightCoords
        );
        submitRightAligned(
                submitter,
                poseStack,
                Component.translatable("painting.trade-rush.team_rating.score"),
                SCORE_RIGHT_X,
                HEADER_Y,
                HEADER_COLOR,
                lightCoords
        );
    }

    private void submitRows(
            SubmitNodeCollector submitter,
            PoseStack poseStack,
            List<TeamRatingRow> rows,
            int lightCoords
    ) {
        int count = Math.min(
                rows.size(),
                TeamRatingPaintingSnapshot.MAX_VISIBLE_ROWS
        );

        for (int i = 0; i < count; i++) {
            TeamRatingRow row = rows.get(i);
            int y = ROW_START_Y + i * ROW_STRIDE;

            submitText(
                    submitter,
                    poseStack,
                    Component.literal(Integer.toString(row.place())),
                    PLACE_X,
                    y,
                    TEXT_COLOR,
                    lightCoords
            );
            submitText(
                    submitter,
                    poseStack,
                    Component.literal(fitTeamName(row.teamName())),
                    TEAM_X,
                    y,
                    TEXT_COLOR,
                    lightCoords
            );
            submitRightAligned(
                    submitter,
                    poseStack,
                    Component.literal(Long.toString(row.score())),
                    SCORE_RIGHT_X,
                    y,
                    TEXT_COLOR,
                    lightCoords
            );
        }
    }

    private void submitStateMessage(
            SubmitNodeCollector submitter,
            PoseStack poseStack,
            Component message,
            int lightCoords
    ) {
        submitCentered(
                submitter,
                poseStack,
                message,
                STATE_MESSAGE_Y,
                MUTED_TEXT_COLOR,
                lightCoords
        );
    }

    private void submitCentered(
            SubmitNodeCollector submitter,
            PoseStack poseStack,
            Component component,
            int y,
            int color,
            int lightCoords
    ) {
        submitText(
                submitter,
                poseStack,
                component,
                (CANVAS_WIDTH - font.width(component)) / 2.0F,
                y,
                color,
                lightCoords
        );
    }

    private void submitRightAligned(
            SubmitNodeCollector submitter,
            PoseStack poseStack,
            Component component,
            int rightX,
            int y,
            int color,
            int lightCoords
    ) {
        submitText(
                submitter,
                poseStack,
                component,
                rightX - font.width(component),
                y,
                color,
                lightCoords
        );
    }

    private void submitText(
            SubmitNodeCollector submitter,
            PoseStack poseStack,
            Component component,
            float x,
            int y,
            int color,
            int lightCoords
    ) {
        FormattedCharSequence text = component.getVisualOrderText();
        submitter.submitText(
                poseStack,
                x,
                y,
                text,
                false,
                Font.DisplayMode.NORMAL,
                lightCoords,
                color,
                0,
                0
        );
    }

    private String fitTeamName(String teamName) {
        String normalized = teamName == null ? "" : teamName;
        if (font.width(normalized) <= TEAM_MAX_WIDTH) {
            return normalized;
        }

        String ellipsis = "…";
        int ellipsisWidth = font.width(ellipsis);
        if (ellipsisWidth >= TEAM_MAX_WIDTH) {
            return "";
        }

        return font.plainSubstrByWidth(
                normalized,
                TEAM_MAX_WIDTH - ellipsisWidth
        ) + ellipsis;
    }

    private static void fillRect(
            PoseStack poseStack,
            SubmitNodeCollector submitter,
            float left,
            float bottom,
            float right,
            float top,
            float z,
            int color,
            int lightCoords
    ) {
        submitter.submitCustomGeometry(
                poseStack,
                RenderTypes.textBackground(),
                (pose, consumer) -> {
                    vertex(pose, consumer, left, bottom, z, color, lightCoords);
                    vertex(pose, consumer, left, top, z, color, lightCoords);
                    vertex(pose, consumer, right, top, z, color, lightCoords);
                    vertex(
                            pose,
                            consumer,
                            right,
                            bottom,
                            z,
                            color,
                            lightCoords
                    );
                }
        );
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            int color,
            int lightCoords
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setLight(lightCoords);
    }

    private static float rotationDegrees(Direction direction) {
        Direction safeDirection = direction == null ? Direction.SOUTH
                : direction;
        return 180.0F - safeDirection.get2DDataValue() * 90.0F;
    }
}
