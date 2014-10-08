package com.jme3.asset;

/**
 * Character animator listener.
 *
 * @author Tommi S.E. Laukkanen
 */
public interface CharacterAnimatorListener {
    /**
     * Invoked when animation cycle completes.
     * @param animationName the name of the animation which completed
     */
    public void onAnimCycleDone(final String animationName);
}
