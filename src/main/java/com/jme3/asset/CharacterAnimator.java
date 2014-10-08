package com.jme3.asset;

import com.jme3.animation.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;

import java.util.*;

/**
 * Animates multi mesh characters with multiple AnimControllers.
 *
 * @author Tommi S.E. Laukkanen
 */
public class CharacterAnimator {
    /**
     * Character animation controllers.
     */
    final Map<String, AnimControl> animControls = new HashMap<>();
    /**
     * Character animation channels.
     */
    final Map<String, AnimChannel> animChannels = new HashMap<>();

    /**
     * The character animator listener.
     */
    private CharacterAnimatorListener characterAnimatorListener;

    /**
     * Name of the last animation played.
     */
    private String animationName = null;

    /**
     * The animation speed multiplier.
     */
    private float speedMultiplier = 0f;

    /**
     * The animation loop mode.
     */
    private LoopMode loopMode = LoopMode.DontLoop;

    /**
     * The animation max time.
     */
    private float animationMaxTime = 0f;

    /**
     * The animation time.
     */
    private float animationTime = 0f;

    /**
     * Constructor which gets animation controls from character spatial recursively
     * and create animation channels.
     * @param character the character spatial
     */
    public CharacterAnimator(final Spatial character) {
        SceneGraphVisitorAdapter visitor = new SceneGraphVisitorAdapter() {
            @Override
            public void visit(final Geometry geometry) {
                super.visit(geometry);
                checkForAnimControl(geometry);
            }

            @Override
            public void visit(final Node node) {
                super.visit(node);
                checkForAnimControl(node);
            }

            /**
             * Checks whether spatial has animation control and constructs animation channel
             * of it has.
             * @param spatial the sptial
             */
            private void checkForAnimControl(final Spatial spatial) {
                AnimControl animControl = spatial.getControl(AnimControl.class);
                if (animControl == null) {
                    return;
                }
                final AnimChannel animChannel = animControl.createChannel();
                animControls.put(spatial.getName(), animControl);
                animChannels.put(spatial.getName(), animChannel);
            }
        };
        character.depthFirstTraversal(visitor);
    }

    /**
     * Plays animation.
     *
     * @param animationName the animation
     * @param speedMultiplier the speed multiplier (1 = animation native speed, 2 = double speed...)
     * @param blendTime the blend time in seconds
     * @param loopMode the animation loop mode
     */
    public void animate(final String animationName, final float speedMultiplier, final float blendTime,
                        final LoopMode loopMode) {
        this.animationName = animationName;
        this.speedMultiplier = speedMultiplier;
        this.animationTime = 0;
        this.animationMaxTime = 0;
        this.loopMode = loopMode;
        for (final String spatialName : animChannels.keySet()) {
            final AnimControl animControl = animControls.get(spatialName);
            final Animation animation = animControl.getAnim(animationName);
            if (animation != null) {
                final AnimChannel animChannel = animChannels.get(spatialName);
                if (blendTime != 0) {
                    animChannel.setAnim(animationName, blendTime);
                } else {
                    animChannel.setAnim(animationName);
                }
                animChannel.setLoopMode(loopMode);
                animChannel.setSpeed(speedMultiplier);
                this. animationMaxTime = animChannel.getAnimMaxTime();
            }
        }
    }

    /**
     * Gets list of spatial names with animations.
     * @return list of spatial names
     */
    public Collection<String> getSpatialNamesWithAnimations() {
        return animControls.keySet();
    }

    /**
     * Gets animation control with spatial name.
     * @param spatialName the spatial name
     * @return the animation control or null
     */
    public AnimControl getAnimControl(final String spatialName) {
        return animControls.get(spatialName);
    }

    /**
     * Gets last animation played.
     * @return the last animation name or null
     */
    public String getAnimationName() {
        return animationName;
    }

    /**
     * Updates animation manually.
     * @param tpf time per frame
     */
    public void update(final float tpf) {
        if (animationTime > 0f && animationTime > animationMaxTime) {
            if (loopMode == LoopMode.DontLoop) {
                speedMultiplier = 0f;
                characterAnimatorListener.onAnimCycleDone(animationName);
            }
            animationTime = 0f;
        }

        animationTime = animationTime + speedMultiplier * tpf;

        for (final AnimChannel channel : animChannels.values()) {
            channel.setSpeed(0f);
            channel.setTime(animationTime);
        }
    }

    /**
     * Sets character animator listener.
     * @param characterAnimatorListener the character animator listener
     */
    public void setCharacterAnimatorListener(final CharacterAnimatorListener characterAnimatorListener) {
        this.characterAnimatorListener = characterAnimatorListener;
    }
}
