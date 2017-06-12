package game;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.ImageBasedHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Terrain extends GameObject {
    
    private static int TerrainCount;
    private TerrainBlock tb;
            
    public Terrain(Vector3f stepScale, Vector3f origin, 
            Image heightMapImage, ImageIcon textureMap) {
        
        // get heightmap data
        ImageBasedHeightMap hm = new ImageBasedHeightMap(heightMapImage);
        float[] hmData = hm.getHeightMap();
        
        // create terrain block
        tb = new TerrainBlock("terrain" + TerrainCount, hm.getSize(), stepScale, hmData, origin);
        TerrainCount++;
        
        // create texutre
        ProceduralTextureGenerator texGen = new ProceduralTextureGenerator(hm);
        texGen.addTexture(textureMap, 0, 128, 256);
        texGen.createTexture(256);
 
        // set texture params
        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(
            TextureManager.loadTexture(
                texGen.getImageIcon().getImage(),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear,
                true));

        // load texture
        tb.setRenderState(ts);
 
        // set boundings
        tb.setModelBound(new BoundingBox());
        tb.updateModelBound();

        // add to this node
        this.attachChild(tb);
        this.updateWorldBound();
    }
    
    public float getHeight(float x, float z) {
        return tb.getHeight(x, z);
    }
    
    @Override
    public float getSize() {
        return maxBoundingWorldVertex().length();
    }
    
    public Vector3f maxBoundingWorldVertex() {
        BoundingBox bb = (BoundingBox)tb.getWorldBound();
        return bb.getCenter().add(bb.getExtent(null));
    }
    
    public Vector3f minBoundingWorldVertex() {
        BoundingBox bb = (BoundingBox)tb.getWorldBound();
        return bb.getCenter().subtract(bb.getExtent(null));
    }
    
    public Vector3f getCenterXZPosition() {
        Vector3f min = minBoundingWorldVertex();
        Vector3f max = maxBoundingWorldVertex();
        Vector3f diff = max.subtract(min);
        return diff.mult(0.5f);
    }
    
    public Vector3f getRandomXZPosition() {
        Vector3f min = minBoundingWorldVertex();
        Vector3f max = maxBoundingWorldVertex();
        Vector3f diff = max.subtract(min);
        return new Vector3f((float)(Math.random() * diff.x), 0, (float)(Math.random() * diff.z));
    }
}
