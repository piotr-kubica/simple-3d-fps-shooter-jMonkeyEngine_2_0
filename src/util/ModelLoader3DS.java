package util;

import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.model.converters.FormatConverter;
import com.jmex.model.converters.MaxToJme;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModelLoader3DS extends ModelLoader {
    
    public ModelLoader3DS(URL model) {
        super(model);
    }

    @Override
    public Node load(Renderer renderer) {
        File file = null;
        Node modelObj = null;
        
        try {
            file = new File(model.toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(ModelLoader3DS.class.getName()).log(Level.SEVERE, null, ex);
        }
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,
                new SimpleResourceLocator(file.getParentFile().toURI()));
        try {
            if(modelData == null) {
                FormatConverter converter = new MaxToJme();
                modelData = new ByteArrayOutputStream();
                converter.convert(new FileInputStream(file), modelData);
            }
            modelObj = (Node) BinaryImporter.getInstance().load(new ByteArrayInputStream(modelData.toByteArray()));
        } catch(IOException e) {
            e.printStackTrace();
        }
        ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,
                new SimpleResourceLocator(file.getParentFile().toURI()));
        return modelObj;
    }
}
