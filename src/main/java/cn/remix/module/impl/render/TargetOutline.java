package cn.remix.module.impl.render;

import cn.remix.event.base.annotation.EventTarget;
import cn.remix.event.impl.TickEvent;
import cn.remix.module.Category;
import cn.remix.module.Module;
import cn.remix.module.value.impl.BoolValue;
import cn.remix.module.value.impl.ColorValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class TargetOutline extends Module {

    private static TargetOutline instance;

    private final ColorValue color = new ColorValue("Color", new Color(0, 255, 0));
    private final BoolValue throughWalls = new BoolValue("Through Walls", true);

    private final Map<Entity, Team> teams = new HashMap<>();

    public TargetOutline() {
        super("TargetOutline", Category.Render);
    }

    @Override
    public void onEnable() {
        instance = this;
        teams.clear();
    }

    @Override
    public void onDisable() {
        instance = null;
        if (mc.world == null) return;
        Scoreboard sb = mc.world.getScoreboard();
        for (Team t : teams.values()) {
            try { sb.removeTeam(t); } catch (Exception ignored) {}
        }
        for (Entity e : teams.keySet()) e.setGlowing(false);
        teams.clear();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.world == null || mc.player == null) return;

        LivingEntity target = getTarget();

        teams.keySet().removeIf(e -> {
            if (e != target) {
                e.setGlowing(false);
                try {
                    Team t = teams.get(e);
                    if (t != null) mc.world.getScoreboard().removeTeam(t);
                } catch (Exception ignored) {}
                return true;
            }
            return false;
        });

        if (target == null) return;

        if (!teams.containsKey(target)) {
            try {
                Scoreboard sb = mc.world.getScoreboard();
                String teamName = "nexus_" + target.getId();
                Team team = sb.getTeam(teamName);
                if (team == null) team = sb.addTeam(teamName);

                team.setColor(closestFormatting(color.getValue()));

                String name = target.getNameForScoreboard();
                sb.addScoreHolderToTeam(name, team);
                teams.put(target, team);
                target.setGlowing(true);
            } catch (Exception ignored) {}
        }
    }

    private LivingEntity getTarget() {
        try {
            cn.remix.module.impl.combat.Aura aura =
                    getModule(cn.remix.module.impl.combat.Aura.class);
            if (aura != null && aura.isEnabled() && aura.getTarget() != null) {
                return aura.getTarget();
            }
        } catch (Exception ignored) {}

        if (mc.targetedEntity instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    private Formatting closestFormatting(Color color) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        Formatting best = Formatting.GREEN;
        int bestDist = Integer.MAX_VALUE;
        for (Formatting f : Formatting.values()) {
            if (f.getColorValue() == null) continue;
            int fr = (f.getColorValue() >> 16) & 0xFF;
            int fg = (f.getColorValue() >> 8) & 0xFF;
            int fb = f.getColorValue() & 0xFF;
            int dist = (r - fr) * (r - fr) + (g - fg) * (g - fg) + (b - fb) * (b - fb);
            if (dist < bestDist) {
                bestDist = dist;
                best = f;
            }
        }
        return best;
    }

    // ─── для MixinLivingEntityRenderer ───

    public static TargetOutline getInstance() {
        return instance;
    }

    public LivingEntity getCurrentTarget() {
        return getTarget();
    }

    public int getOutlineColor() {
        return color.getValue().getRGB();
    }

    public boolean isThroughWalls() {
        return throughWalls.getValue();
    }
}