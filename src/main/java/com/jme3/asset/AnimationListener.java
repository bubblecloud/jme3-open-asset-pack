package com.jme3.asset;

/**
 * Animation listener.
 *
 * @author Tommi S.E. Laukkanen
 */
public interface AnimationListener {
    /**
     * Invoked when animation cycle completes.
     * @param animationName the name of the animation which completed
     */
    public void onAnimCycleDone(final String animationName);
}
