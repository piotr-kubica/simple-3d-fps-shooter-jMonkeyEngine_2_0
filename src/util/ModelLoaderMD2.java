package util;

import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.converters.Md2ToJme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class ModelLoaderMD2 extends ModelLoader {

    public ModelLoaderMD2(URL model, URL texture) {
        super(model, texture);
    }
    
    @Override
    public Node load(Renderer renderer) {
        Node modelObj = null;
        
        try {
            if(modelData == null) {
                Md2ToJme converter = new Md2ToJme();
                modelData = new ByteArrayOutputStream();
                converter.convert(model.openStream(), modelData);
            }
            modelObj = (Node)BinaryImporter.getInstance().load(new ByteArrayInputStream(modelData.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // sets texture
        TextureState ts = renderer.createTextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.loadTexture(texture));
        modelObj.setRenderState(ts);
        
        return modelObj;
    }
    
}
