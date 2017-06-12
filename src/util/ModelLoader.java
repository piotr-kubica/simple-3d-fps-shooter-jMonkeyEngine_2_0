package util;

import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import java.io.ByteArrayOutputStream;
import java.net.URL;

public abstract class ModelLoader {
    
    protected URL model;
    protected URL texture;
    protected ByteArrayOutputStream modelData;
    
    public ModelLoader(URL model) {
        this.model = model;
    }
    
    public ModelLoader(URL model, URL texture) {
        this(model);
        this.texture = texture;
    }
    
    public abstract Node load(Renderer renderer);
}
