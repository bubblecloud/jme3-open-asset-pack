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
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import java.util.ArrayList;

/**
 * Sample 7 - how to load an OgreXML model and play an animation,
 * using channels, a controller, and an AnimEventListener.
 */
public class AnimationPreview extends SimpleApplication implements AnimEventListener {

    public static final String WALK = "Walk";

    public static final String STAND = "Stand";

    private Node player;
    private ArrayList<String> animations;
    private AnimChannel channel;
    private AnimControl control;

    public static void main(String[] args) {
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

        player = (Node) assetManager.loadModel("character/human/male/basic/ogre/male.mesh.xml");
        rootNode.attachChild(player);
        control = player.getControl(AnimControl.class);
        if (control != null) {
            control.addListener(this);
            animations = new ArrayList<>(control.getAnimationNames());
            animations.remove(STAND);
            System.out.println(animations);
            channel = control.createChannel();
            channel.setAnim(STAND);
            channel.setSpeed(1f);
            channel.setLoopMode(LoopMode.DontLoop);
        }
        cam.setLocation(new Vector3f(0, 0.5f, 4));
        cam.setRotation(
                new Quaternion().fromAngleAxis(-FastMath.PI * 0.04f, new Vector3f(1, 0, 0)).mult(
                new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0))));
    }

    private long animChangeTime = System.currentTimeMillis() +  3000;

    @Override
    public void simpleRender(RenderManager rm) {
        super.simpleRender(rm);

        if (System.currentTimeMillis() > animChangeTime) {
            if (animations.size() > 0) {
                final String nextAnimation = animations.remove(0);
                System.out.println("Playing: " + nextAnimation);
                channel.setAnim(nextAnimation, 0.5f);
                channel.setLoopMode(LoopMode.DontLoop);
                animChangeTime = System.currentTimeMillis() + 4000;
            } else {
                System.exit(0);
            }
        }

    }

    @Override
    public void onAnimCycleDone(AnimControl animControl, AnimChannel animChannel, String name) {
        if (!STAND.equals(name)) {
            channel.setAnim(STAND, 0.5f);
            channel.setLoopMode(LoopMode.DontLoop);
            animChangeTime = System.currentTimeMillis() + 500;
        }
    }

    @Override
    public void onAnimChange(AnimControl animControl, AnimChannel animChannel, String s) {

    }
}