package com.jme3.asset;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
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
public class AnimationPreview extends SimpleApplication implements AnimEventListener {

    /**
     * The character model file.
     */
    public static final String CHARACTER_MODEL_FILE = "character/human/male/basic/ogre/male.mesh.xml";
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

    /**
     * List of animations loaded from model file.
     */
    private ArrayList<String> animations;
    /**
     * Animation channel used to play the animations.
     */
    private AnimChannel channel;
    /**
     * Number of times current animation has played so far.
     */
    private int playCounter = 0;

    /**
     * Main class used to run the animation preview from command line.
     * @param args the command line arguments
     * @throws Exception if exception occurs during execution
     */
    public static void main(String[] args) throws Exception {
        AnimationPreview app = new AnimationPreview();
        final AppSettings appSettings = new AppSettings(true);
        appSettings.setSamples(4);
        app.setSettings(appSettings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        viewPort.setBackgroundColor(ColorRGBA.White);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -1f, -1).normalizeLocal());
        rootNode.addLight(dl);
        rootNode.addLight(new AmbientLight());

        final Node player = (Node) assetManager.loadModel(CHARACTER_MODEL_FILE);
        rootNode.attachChild(player);
        final AnimControl control = player.getControl(AnimControl.class);
        if (control != null) {
            control.addListener(this);
            animations = new ArrayList<>(control.getAnimationNames());
            animations.remove(STAND);
            animations.remove(REST);
            System.out.println(animations);
            channel = control.createChannel();
            channel.setAnim(REST);
            channel.setSpeed(2f);
            channel.setLoopMode(LoopMode.DontLoop);
        }
        cam.setLocation(new Vector3f(0, 0.5f, 3f));
        cam.setRotation(
                new Quaternion().fromAngleAxis(-FastMath.PI * 0.04f, new Vector3f(1, 0, 0)).mult(
                new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0))));

        //stateManager.attach(new VideoRecorderAppState());
    }

    @Override
    public void onAnimCycleDone(AnimControl animControl, AnimChannel animChannel, String name) {
        if (animations.size() == 0 && REST.equals(channel.getAnimationName())) {
            System.exit(0);
        }

        if ( REST.equals(channel.getAnimationName())) {
            channel.setAnim(STAND, 1f);
            channel.setSpeed(1f);
            channel.setLoopMode(LoopMode.DontLoop);
            System.out.println("Playing beginning " + STAND);
        } else if (!STAND.equals(name) && playCounter >= ANIMATION_REPEAT_COUNT) {
            channel.setAnim(STAND, 0.5f);
            channel.setSpeed(2f);
            channel.setLoopMode(LoopMode.DontLoop);
            System.out.println("Playing intermediate " + STAND);
        } else {
            if (animations.size() > 0 || playCounter < ANIMATION_REPEAT_COUNT) {
                final String nextAnimation;
                if (playCounter > 0 && playCounter < ANIMATION_REPEAT_COUNT) {
                    nextAnimation = name;
                    channel.setAnim(nextAnimation);
                } else {
                    playCounter = 0;
                    nextAnimation =  animations.remove(0);
                    channel.setAnim(nextAnimation, 0.5f);
                }
                channel.setLoopMode(LoopMode.DontLoop);
                channel.setSpeed(1f);
                System.out.println("Playing: " + nextAnimation);
                playCounter++;
            } else {
                channel.setAnim(REST, 1f);
                channel.setLoopMode(LoopMode.DontLoop);
                System.out.println("Playing final " + REST);
            }
        }
    }

    @Override
    public void onAnimChange(AnimControl animControl, AnimChannel animChannel, String s) {

    }
}