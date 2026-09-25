package com.mygame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Manages fixed laser emission and optical ray tracing.
 * Supports:
 * - Fixed unidirectional emission
 * - Single-pass triangular prism dispersion (exactly 2 split beams: Red & Cyan)
 * - Prism rotation affecting refraction angles in real time
 * - Mirror reflections maintaining beam colors
 * - Player obstruction
 * - Color-matched target receiver activation
 */
public class LaserSystem {
    public static final Color COLOR_RED = Color.valueOf("ff2a44");
    public static final Color COLOR_CYAN = Color.valueOf("38bdf8");

    public static class BeamSegment {
        public final Vector2 start = new Vector2();
        public final Vector2 end = new Vector2();
        public final Color color = new Color();
        public boolean hitMirror = false;
        public boolean hitPrism = false;
        public boolean hitTarget = false;
        public boolean isInternal = false;
    }

    private static class RayJob {
        final Vector2 start = new Vector2();
        final Vector2 dir = new Vector2();
        final Color color = new Color();
        int remainingBounces;
        boolean canHitPrism;

        RayJob(Vector2 start, Vector2 dir, Color color, int remainingBounces, boolean canHitPrism) {
            this.start.set(start);
            this.dir.set(dir).nor();
            this.color.set(color);
            this.remainingBounces = remainingBounces;
            this.canHitPrism = canHitPrism;
        }
    }

    private final Vector2 emitterPos = new Vector2(70f, 360f); // Fixed at left wall
    private final Vector2 emitterDir = new Vector2(1f, 0f);  // Fixed East direction
    private final Array<BeamSegment> activeSegments = new Array<>();
    private final Array<RayJob> rayQueue = new Array<>();

    private final Vector2 tempHit = new Vector2();

    public void update(Array<Mirror> mirrors, Prism prism, Player p1, Player p2, Array<TargetReceiver> targets) {
        activeSegments.clear();
        rayQueue.clear();

        // Reset target power states
        for (TargetReceiver target : targets) {
            target.setPowered(false);
        }

        // Initial beam (Can hit the prism)
        rayQueue.add(new RayJob(emitterPos, emitterDir, Color.WHITE, 12, true));

        int totalProcessed = 0;
        int maxTotalRays = 24;

        while (rayQueue.size > 0 && totalProcessed < maxTotalRays) {
            RayJob job = rayQueue.removeIndex(0);
            totalProcessed++;

            Vector2 currentStart = job.start;
            Vector2 currentDir = job.dir;
            Color currentColor = job.color;

            // 1. Ray intersection with outer screen boundaries
            float wallDist = Float.MAX_VALUE;
            Vector2 wallHit = new Vector2();

            if (currentDir.x > 0.0001f) {
                float t = (GameScreen.WORLD_WIDTH - 2f - currentStart.x) / currentDir.x;
                if (t > 0 && t < wallDist) {
                    wallDist = t;
                    wallHit.set(GameScreen.WORLD_WIDTH - 2f, currentStart.y + currentDir.y * t);
                }
            } else if (currentDir.x < -0.0001f) {
                float t = (2f - currentStart.x) / currentDir.x;
                if (t > 0 && t < wallDist) {
                    wallDist = t;
                    wallHit.set(2f, currentStart.y + currentDir.y * t);
                }
            }

            if (currentDir.y > 0.0001f) {
                float t = (GameScreen.WORLD_HEIGHT - 2f - currentStart.y) / currentDir.y;
                if (t > 0 && t < wallDist) {
                    wallDist = t;
                    wallHit.set(currentStart.x + currentDir.x * t, GameScreen.WORLD_HEIGHT - 2f);
                }
            } else if (currentDir.y < -0.0001f) {
                float t = (2f - currentStart.y) / currentDir.y;
                if (t > 0 && t < wallDist) {
                    wallDist = t;
                    wallHit.set(currentStart.x + currentDir.x * t, 2f);
                }
            }

            Vector2 bestHit = new Vector2(wallHit);
            float bestDist = currentStart.dst(bestHit);
            Mirror hitMirror = null;
            boolean hitPrism = false;
            boolean hitPlayer = false;
            TargetReceiver hitTarget = null;

            // 2. Check collision with Prism (ONLY if this ray is allowed to hit prism)
            if (job.canHitPrism && prism != null && prism.intersects(currentStart, bestHit, tempHit)) {
                float dist = currentStart.dst(tempHit);
                if (dist > 1.5f && dist < bestDist) {
                    bestDist = dist;
                    bestHit.set(tempHit);
                    hitPrism = true;
                    hitMirror = null;
                    hitPlayer = false;
                    hitTarget = null;
                }
            }

            // 3. Check collisions with Mirrors
            for (Mirror mirror : mirrors) {
                if (mirror.intersects(currentStart, bestHit, tempHit)) {
                    float dist = currentStart.dst(tempHit);
                    if (dist > 1.5f && dist < bestDist) {
                        bestDist = dist;
                        bestHit.set(tempHit);
                        hitMirror = mirror;
                        hitPrism = false;
                        hitPlayer = false;
                        hitTarget = null;
                    }
                }
            }

            // 4. Check collision with Player 1 (blocks beam)
            if (p1 != null) {
                Vector2 p1Center = new Vector2(p1.getCenterX(), p1.getCenterY());
                float p1Radius = p1.getSize() / 2f;
                if (Intersector.intersectSegmentCircle(currentStart, bestHit, p1Center, p1Radius * p1Radius)) {
                    float dist = currentStart.dst(p1Center) - p1Radius;
                    if (dist > 1f && dist < bestDist) {
                        bestDist = dist;
                        bestHit.set(currentStart).add(new Vector2(currentDir).scl(dist));
                        hitMirror = null;
                        hitPrism = false;
                        hitPlayer = true;
                        hitTarget = null;
                    }
                }
            }

            // 5. Check collision with Player 2 (blocks beam)
            if (p2 != null) {
                Vector2 p2Center = new Vector2(p2.getCenterX(), p2.getCenterY());
                float p2Radius = p2.getSize() / 2f;
                if (Intersector.intersectSegmentCircle(currentStart, bestHit, p2Center, p2Radius * p2Radius)) {
                    float dist = currentStart.dst(p2Center) - p2Radius;
                    if (dist > 1f && dist < bestDist) {
                        bestDist = dist;
                        bestHit.set(currentStart).add(new Vector2(currentDir).scl(dist));
                        hitMirror = null;
                        hitPrism = false;
                        hitPlayer = true;
                        hitTarget = null;
                    }
                }
            }

            // 6. Check collisions with Targets
            for (TargetReceiver tr : targets) {
                if (tr.checkHit(currentStart, bestHit, currentColor, tempHit)) {
                    float dist = currentStart.dst(tempHit);
                    if (dist > 1f && dist < bestDist) {
                        bestDist = dist;
                        bestHit.set(tempHit);
                        hitTarget = tr;
                        hitMirror = null;
                        hitPrism = false;
                        hitPlayer = false;
                    }
                }
            }

            // Record incoming beam segment
            BeamSegment segment = new BeamSegment();
            segment.start.set(currentStart);
            segment.end.set(bestHit);
            segment.color.set(currentColor);
            segment.hitMirror = (hitMirror != null);
            segment.hitPrism = hitPrism;
            segment.hitTarget = (hitTarget != null);
            activeSegments.add(segment);

            if (job.remainingBounces <= 0 || hitPlayer || hitTarget != null) {
                continue;
            }

            // Handle Prism Light Dispersion into EXACTLY TWO beams (Red & Cyan)
            if (hitPrism) {
                // Dynamic steer angle responding smoothly to Q and E rotations:
                // Equilateral triangular symmetry (3-fold)
                float prismAngle = prism.getAngleDeg();
                float steer = MathUtils.sinDeg(prismAngle * 3f) * 45f;
                float baseAngle = currentDir.angleDeg() + steer;

                Vector2 dirA = new Vector2(1f, 0f).setAngleDeg(baseAngle + 20f).nor();
                Vector2 dirB = new Vector2(1f, 0f).setAngleDeg(baseAngle - 20f).nor();

                // Spawn points outside prism boundary to prevent internal re-collision
                Vector2 center = prism.getCenter();
                float exitDist = prism.getRadius() + 4f;
                Vector2 spawnA = new Vector2(center).add(dirA.x * exitDist, dirA.y * exitDist);
                Vector2 spawnB = new Vector2(center).add(dirB.x * exitDist, dirB.y * exitDist);

                // Internal refraction beam paths through glass
                BeamSegment internalA = new BeamSegment();
                internalA.start.set(bestHit);
                internalA.end.set(spawnA);
                internalA.color.set(COLOR_RED);
                internalA.isInternal = true;
                activeSegments.add(internalA);

                BeamSegment internalB = new BeamSegment();
                internalB.start.set(bestHit);
                internalB.end.set(spawnB);
                internalB.color.set(COLOR_CYAN);
                internalB.isInternal = true;
                activeSegments.add(internalB);

                // Exactly 2 new rays queued (canHitPrism = false ensures NO re-splitting!)
                rayQueue.add(new RayJob(spawnA, dirA, COLOR_RED, job.remainingBounces - 1, false));
                rayQueue.add(new RayJob(spawnB, dirB, COLOR_CYAN, job.remainingBounces - 1, false));
                continue;
            }

            // Handle Mirror reflection
            if (hitMirror != null) {
                Vector2 nextDir = hitMirror.reflect(currentDir).nor();
                Vector2 spawn = new Vector2(bestHit).add(nextDir.x * 1.5f, nextDir.y * 1.5f);
                rayQueue.add(new RayJob(spawn, nextDir, currentColor, job.remainingBounces - 1, job.canHitPrism));
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        // Draw fixed emitter turret housing on the left
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.18f, 0.22f, 0.28f, 1f));
        shapeRenderer.rect(emitterPos.x - 45f, emitterPos.y - 22f, 40f, 44f);
        shapeRenderer.setColor(new Color(0.35f, 0.42f, 0.55f, 1f));
        shapeRenderer.rect(emitterPos.x - 10f, emitterPos.y - 12f, 18f, 24f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(emitterPos.x + 8f, emitterPos.y, 8f);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(emitterPos.x - 45f, emitterPos.y - 22f, 40f, 44f);
        shapeRenderer.circle(emitterPos.x + 8f, emitterPos.y, 8f);
        shapeRenderer.end();

        // Render laser beams
        for (BeamSegment seg : activeSegments) {
            Color c = seg.color;

            // Outer soft glow
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(c.r, c.g, c.b, 0.35f);
            shapeRenderer.line(seg.start.x - 1, seg.start.y, seg.end.x - 1, seg.end.y);
            shapeRenderer.line(seg.start.x + 1, seg.start.y, seg.end.x + 1, seg.end.y);
            shapeRenderer.line(seg.start.x, seg.start.y - 1, seg.end.x, seg.end.y - 1);
            shapeRenderer.line(seg.start.x, seg.start.y + 1, seg.end.x, seg.end.y + 1);

            // Mid vibrant color
            shapeRenderer.setColor(c);
            shapeRenderer.line(seg.start.x, seg.start.y, seg.end.x, seg.end.y);
            shapeRenderer.end();

            // Core bright point
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.WHITE);
            if (seg.hitMirror || seg.hitPrism) {
                shapeRenderer.circle(seg.end.x, seg.end.y, 3.5f);
            }
            shapeRenderer.end();
        }
    }

    public Vector2 getEmitterPos() { return emitterPos; }
}
