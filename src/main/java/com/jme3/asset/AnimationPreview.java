package com.jme3.asset;

import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
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
 * Application for reviewing animations.
 *
 * @author Tommi S.E. Laukkanen
 */
public class AnimationPreview extends SimpleApplication implements AnimationListener {

    /**
     * The character model file.
     */
    public static final String CHARACTER_MODEL_FILE = "character/human/male/ogre/male.scene";
    /**
     * Repeat count for animations.
     */
    public static final int ANIMATION_REPEAT_COUNT = 5;
    /**
     * Special pose used for begin and end.
     */
    public static final String REST = "Rest";
    /**
     * Special pose used between animations.
     */
    public static final String STAND = "Stand";
    /**
     * Main mesh name containing AnimControl with all animation names.
     */
    public static final String MAIN_MESH_NAME = "Body";
    /**
     * List of animations loaded from model file.
     */
    private ArrayList<String> animations;
    /**
     * The animation controller.
     */
    private AnimationController animationController;

    /**
     * Main class used to run the animation preview from command line.
     * @param args the command line arguments
     * @throws Exception if exception occurs during execution
     */
    public static void main(String[] args) throws Exception {
        AnimationPreview app = new AnimationPreview();
        final AppSettings appSettings = new AppSettings(true);
        appSettings.setFullscreen(false);
        appSettings.setResolution(640, 480);
        appSettings.setSamples(8);
        appSettings.setVSync(false);
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

        animationController = new AnimationController(player);
        animationController.setAnimationListener(this);

        System.out.println(animationController.getSpatialNamesWithAnimations());

        String mainSpatialName = null;
        for (final String spatialName : animationController.getSpatialNamesWithAnimations()) {
            if (spatialName.startsWith(MAIN_MESH_NAME)) {
                mainSpatialName =  spatialName;
            }
        }

        if (mainSpatialName == null) {
            return;
        }

        System.out.println("Main mesh: " + animationController.getSpatialNamesWithAnimations());

        final AnimControl control = animationController.getAnimControl(mainSpatialName);
        if (control != null) {
            animations = new ArrayList<>(control.getAnimationNames());
            animations.remove(STAND);
            animations.remove(REST);
            System.out.println(animations);
            animationController.animate(REST, 1f, 2f, 1);
        }

        stateManager.attach(new VideoRecorderAppState());

        cam.setLocation(new Vector3f(0, 2f, 3f));
        cam.setRotation(
                new Quaternion().fromAngleAxis(-FastMath.PI * 0.10f, new Vector3f(1, 0, 0)).mult(
                        new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0))));

    }

    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        animationController.update(tpf);
    }

    @Override
    public void onAnimCycleDone(final String animationName) {
        if (animations.size() == 0 && REST.equals(animationController.getAnimationName())) {
            System.exit(0);
        }

        if (REST.equals(animationController.getAnimationName())) {
            animationController.animate(STAND, 0.5f, 2f, 1);
            System.out.println("Playing beginning " + STAND);
        } else if (!STAND.equals(animationName)) {
            animationController.animate(STAND, 1f, 0.5f, 1);
            System.out.println("Playing intermediate " + STAND);
        } else {
            if (animations.size() > 0) {
                final String nextAnimation =  animations.remove(0);
                animationController.animate(nextAnimation, 1.5f, 0.5f, ANIMATION_REPEAT_COUNT);
                System.out.println("Playing: " + nextAnimation);
            } else {
                animationController.animate(REST,  0.5f,  2f, 1);
                System.out.println("Playing final " + REST);
            }
        }
    }

}