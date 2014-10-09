package com.jme3.asset;

import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import java.util.ArrayList;

/**
 * Application for reviewing character animations.
 *
 * @author Tommi S.E. Laukkanen
 */
public class AnimationPreview extends SimpleApplication implements CharacterAnimatorListener {

    /**
     * The character model file.
     */
    public static final String CHARACTER_MODEL_FILE = "character/human/male/basic/ogre/male.scene";
    /**
     * Repeat count for animations.
     */
    public static final int ANIMATION_REPEAT_COUNT = 3;
    /**
     * Special pose used for begin and end.
     */
    public static final String REST = "Rest";
    /**
     * Special pose used between animations.
     */
    public static final String STAND = "Stand";
    public static final String MAIN_MESH_NAME = "male";

    /**
     * List of animations loaded from model file.
     */
    private ArrayList<String> animations;
    /**
     * Number of times current animation has played so far.
     */
    private int playCounter = 0;
    private CharacterAnimator animator;

    /**
     * Main class used to run the animation preview from command line.
     * @param args the command line arguments
     * @throws Exception if exception occurs during execution
     */
    public static void main(String[] args) throws Exception {
        AnimationPreview app = new AnimationPreview();
        final AppSettings appSettings = new AppSettings(true);
        appSettings.setFullscreen(true);
        appSettings.setResolution(1920, 1080);
        appSettings.setSamples(4);
        appSettings.setVSync(true);
        app.setSettings(appSettings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        viewPort.setBackgroundColor(ColorRGBA.LightGray);

        final DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.LightGray);
        dl.setDirection(new Vector3f(-0.1f, -1f, -1).normalizeLocal());
        rootNode.addLight(dl);
        final AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.LightGray);
        rootNode.addLight(al);

        final Node player = (Node) assetManager.loadModel(CHARACTER_MODEL_FILE);
        rootNode.attachChild(player);

        animator = new CharacterAnimator(player);
        animator.setCharacterAnimatorListener(this);

        System.out.println(animator.getSpatialNamesWithAnimations());

        String mainSpatialName = null;
        for (final String spatialName : animator.getSpatialNamesWithAnimations()) {
            if (spatialName.startsWith(MAIN_MESH_NAME)) {
                mainSpatialName =  spatialName;
            }
        }

        if (mainSpatialName == null) {
            return;
        }

        System.out.println("Main mesh: " + animator.getSpatialNamesWithAnimations());

        final AnimControl control = animator.getAnimControl(mainSpatialName);
        if (control != null) {
            animations = new ArrayList<>(control.getAnimationNames());
            animations.remove(STAND);
            animations.remove(REST);
            System.out.println(animations);
            animator.animate(REST, 1f, 2f, LoopMode.DontLoop);
        }

        //stateManager.attach(new VideoRecorderAppState());

        cam.setLocation(new Vector3f(0, 1.5f, 3f));
        cam.setRotation(
                new Quaternion().fromAngleAxis(-FastMath.PI * 0.04f, new Vector3f(1, 0, 0)).mult(
                        new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0))));

    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        animator.update(tpf);
    }

    @Override
    public void onAnimCycleDone(final String animationName) {
        if (animations.size() == 0 && REST.equals(animator.getAnimationName())) {
            System.exit(0);
        }

        if (REST.equals(animator.getAnimationName())) {
            animator.animate(STAND, 1f, 1f, LoopMode.DontLoop);
            System.out.println("Playing beginning " + STAND);
        } else if (!STAND.equals(animationName) && playCounter >= ANIMATION_REPEAT_COUNT) {
            animator.animate(STAND, 2f, 0.5f, LoopMode.DontLoop);
            System.out.println("Playing intermediate " + STAND);
        } else {
            if (animations.size() > 0 || playCounter < ANIMATION_REPEAT_COUNT) {
                final String nextAnimation;
                if (playCounter > 0 && playCounter < ANIMATION_REPEAT_COUNT) {
                    nextAnimation = animationName;
                    animator.animate(nextAnimation, 1.5f, 0.5f, LoopMode.DontLoop);
                } else {
                    playCounter = 0;
                    nextAnimation =  animations.remove(0);
                    animator.animate(nextAnimation, 1.5f, 0.5f, LoopMode.DontLoop);
                }
                System.out.println("Playing: " + nextAnimation);
                playCounter++;
            } else {
                animator.animate(REST,  1f,  1f, LoopMode.DontLoop);
                System.out.println("Playing final " + REST);
            }
        }
    }

}