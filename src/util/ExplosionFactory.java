package util;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.SimpleParticleInfluenceFactory;

/**
 * źródło na podstawie klasy: http://jme2.jmonkeyengine.org/doku.php?id=jme2:simple_fireworks
 * 
 * The ExplosionFactory keeps a pool of a number of the ParticleEffect to reuse them.<br>
 * The color and intensity of the explosion effects are more or less random.<br>
 */
public class ExplosionFactory {

    public static final Logger log = Logger.getLogger(ExplosionFactory.class.getName());
    private static final int POOL_SIZE = 200;
    
    /** pool of explosions */
    private ArrayList<ParticleMesh> explosions;
    
    /** render states for the particle effects */
    private BlendState blendState;
    private TextureState texState;
    private ZBufferState zbufferState;
    
    /** reference to the root node, to attach the particleeffect to the scene */
    private Node rootNode;
    private final ArrayList<Spatial> done;
    private final Random rand;
    private int explosionSize = 100;
    Texture tex;

    /**
     * Sets up the Explosion effect and creates a pool of particles with random colors.
     * 
     * @param root
     */
    public ExplosionFactory(final Node root, String imagePath) {
        final Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        this.explosions = new ArrayList<ParticleMesh>();
        this.rootNode = root;
        this.rand = new Random();
        this.done = new ArrayList<Spatial>();
        
        this.blendState = r.createBlendState();
        this.blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        this.blendState.setDestinationFunction(BlendState.DestinationFunction.One);
        this.blendState.setBlendEnabled(true);

        this.texState = r.createTextureState();
        this.texState.setEnabled(true);
        this.texState.setTexture(tex = TextureManager.loadTexture(ExplosionFactory.class.getClassLoader().getResource(imagePath)));

        this.zbufferState = r.createZBufferState();
        this.zbufferState.setWritable(false);

        // create a pool of 20 different explosion effects
        for (int i = 0; i < POOL_SIZE; i++) {
            createExplosion();
        }
    }

    public void setExplosionSize(int size) {
        if (this.explosionSize == size) {
            return;
        }
        for (ParticleMesh mesh : this.explosions) {
            mesh.removeFromParent();
        }
        this.explosions.clear();
        this.explosionSize = size;
    }

    /**
     * Try to get a inactive particle effect out of the pool.<br>
     * If we can't find one (all are busy), we create a new one and expand the pool.<br>
     */
    ParticleMesh getExplosion() {
        for (ParticleMesh e : this.explosions) {
            if (!e.isActive()) {
                return e;
            }
        }
        return createExplosion();
    }

    /**
     * creates a particle effect with random colors
     * 
     * @return
     */
    private ParticleMesh createExplosion() {
        final ParticleMesh particleGeom = ParticleFactory.buildParticles("explosion", this.explosionSize);
        // add a gravity effect to the particle effect
        particleGeom.addInfluence(SimpleParticleInfluenceFactory.createBasicGravity(new Vector3f(0, -0.05f, 0), true));
        particleGeom.setEmissionDirection(new Vector3f(0.0f, 1.0f, 0.0f));
        // allow to shoot particles in all directions (360°)
        particleGeom.setMaximumAngle(3.1415927f);
        particleGeom.setMinimumAngle(0);
        particleGeom.getParticleController().setSpeed(0.35f);
        particleGeom.setMinimumLifeTime(200.0f);
        particleGeom.setMaximumLifeTime(600.0f);
        particleGeom.setStartSize(12f + this.rand.nextFloat() * 12f);
        particleGeom.setEndSize(2f);
        particleGeom.getParticleController().setControlFlow(false);
        particleGeom.getParticleController().setRepeatType(Controller.RT_CLAMP);
        particleGeom.warmUp(200);
        
        // make some explosion appear more powerful than others
        // more velocity means bigger/wider explosions
        particleGeom.setInitialVelocity(0.1f + this.rand.nextFloat() * 0.05f);

        // random color
        float red = 0.6f + this.rand.nextFloat();
        particleGeom.setStartColor(new ColorRGBA(red, 0.25f, 0.01f, 1f));
        particleGeom.setEndColor(new ColorRGBA(red, 0.25f, 0.01f, 0.0f));
        
        // apply renderstartes
        particleGeom.setRenderState(this.texState);
        particleGeom.setRenderState(this.blendState);
        particleGeom.setRenderState(this.zbufferState);
        // attach the particle effect to the root node
        this.rootNode.attachChild(particleGeom);
        this.rootNode.updateRenderState();
        // add the effect to the pool to reuse it later
        this.explosions.add(particleGeom);
        return particleGeom;
    }

    /**
     * Creates a new explosion at a given location.
     * 
     * @param location
     *            location where the explosion should appear
     */
    public void spawnExplosion(final Vector3f location) {
        final ParticleMesh mesh = getExplosion();
        mesh.getLocalTranslation().set(location);
        mesh.updateWorldData(0);
        mesh.forceRespawn();
    }

    public void respawn() {
        for (Spatial s : this.done) {
            s.clearControllers();
            s.removeFromParent();
            spawnExplosion(s.getLocalTranslation().clone());
        }
        this.done.clear();
    }

    public void addDone(final Spatial work) {
        this.done.add(work);
    }
}