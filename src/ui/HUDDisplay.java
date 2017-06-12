package ui;

import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jmex.angelfont.BitmapFont;
import com.jmex.angelfont.BitmapFontLoader;
import com.jmex.angelfont.BitmapText;
import com.jmex.angelfont.Rectangle;
import java.net.URL;

public class HUDDisplay extends Node {
    
    BitmapFont font = null;
    BitmapText fragsText = null;
    BitmapText timeText = null;
    
    public HUDDisplay(URL fontFile, URL textureFile) {
        super("hud");

        try {
            font = BitmapFontLoader.load(fontFile.toURI().toURL(), textureFile.toURI().toURL());
        } catch(Exception ex) {
            System.out.println("Unable to load font");
        }
        
        Node fragNode = new Node("fragNode");
        fragsText = new BitmapText(font, false);
        fragsText.setBox(new Rectangle(0, 0, 300, 30));
        fragsText.setSize(22);
        fragsText.setDefaultColor(new ColorRGBA(0f, 0f, 0f, .8f));
        fragsText.setLightCombineMode(LightCombineMode.Off);
        fragNode.attachChild(fragsText);
        fragNode.setLocalTranslation(-20, 50, 0);
        attachChild(fragNode);
        
        Node timeNode = new Node("timeNode");
        timeText = new BitmapText(font, false);
        timeText.setBox(new Rectangle(0, 0, 300, 30));
        timeText.setSize(22);
        timeText.setDefaultColor(new ColorRGBA(0f, 0f, 0f, .8f));
        timeText.setLightCombineMode(LightCombineMode.Off);
        timeNode.attachChild(timeText);
        timeNode.setLocalTranslation(90, 50, 0);
        attachChild(timeNode);
    }
    
    public BitmapFont getFont() {
        return font;
    }

    public void setFont(BitmapFont font) {
        this.font = font;
    }
    
    public void setFrags(int current, int max) {
        setText(fragsText, String.format("frags: %d/%d", current, max));
    }
    
    public void setTime(int min, int sec) {
        setText(timeText, "time: " + min + ":" + (sec < 10 ? "0" + sec : "" + sec));
    }
    
    protected void setText(BitmapText bitmapText, String text) {
        bitmapText.setText(text);
        bitmapText.update();

        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setLocalTranslation(50, 400, 0);
        setCullHint(CullHint.Never);

        if(this.getParent() != null && this.getParent() instanceof Node) {
            ((Node)this.getParent()).updateRenderState();
        }
    }
}
