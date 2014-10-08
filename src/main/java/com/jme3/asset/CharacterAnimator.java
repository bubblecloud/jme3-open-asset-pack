package com.jme3.asset;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
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
     * Name of the last animation played.
     */
    String lastAnimation = null;

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
     * @param animation the animation
     * @param speedMultiplier the speed multiplier (1 = animation native speed, 2 = double speed...)
     * @param blendTime the blend time in seconds
     * @param loopMode the animation loop mode
     */
    public void animate(final String animation, final float speedMultiplier, final float blendTime,
                        final LoopMode loopMode) {
        lastAnimation = animation;
        for (final String spatialName : animChannels.keySet()) {
            final AnimControl animControl = animControls.get(spatialName);
            if (animControl.getAnim(animation) != null) {
                final AnimChannel animChannel = animChannels.get(spatialName);
                if (blendTime != 0) {
                    animChannel.setAnim(animation, blendTime);
                } else {
                    animChannel.setAnim(animation);
                }
                animChannel.setSpeed(speedMultiplier);
                animChannel.setLoopMode(loopMode);
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
    public String getLastAnimation() {
        return lastAnimation;
    }
}
