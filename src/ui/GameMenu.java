package ui;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyInputAction;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jmex.angelfont.BitmapFont;
import com.jmex.angelfont.BitmapText;
import com.jmex.angelfont.Rectangle;
import game.GameObject;
import java.util.ArrayList;
import java.util.LinkedList;
import ui.event.MenuActionListener;
import ui.event.MenuActionEvent;

public class GameMenu extends Node {
    private int currMenuCursor;
    private int prevMenuCursor;
    
    BitmapFont font = null;
    ColorRGBA defaultPositionColor;
    ColorRGBA selectedPositionColor;
    ColorRGBA titleColor;
    int defaultPositionSize;
    int selectedPositionSize;
    int titleSize;
    BitmapText menuTitle;
    ArrayList<BitmapText> bitmapTexts = new ArrayList<BitmapText>();
    LinkedList<MenuActionListener> observers = new LinkedList<MenuActionListener>();
    MenuInputHandler menuHandler = new MenuInputHandler();

    public GameMenu(BitmapFont font, String title, String... menuPositionTexts) {
        this.font = font;
        currMenuCursor = prevMenuCursor = 0;
        defaultPositionColor = new ColorRGBA(0.1f, 0.1f, 0.1f, 0.9f);
        selectedPositionColor = new ColorRGBA(1.0f, 0.1f, 0.1f, 0.9f);
        titleColor = new ColorRGBA(0.2f, 0.6f, 0.3f, 0.9f);
        Rectangle rectangle = new Rectangle(0, 0, 300, 30);
        defaultPositionSize = 24;
        selectedPositionSize = 28;
        titleSize = 34;
        
        Node titleNode = new Node("gameMenuTitleNode");
        menuTitle = new BitmapText(font, false);
        menuTitle.setBox(rectangle);
        menuTitle.setSize(titleSize);
        menuTitle.setDefaultColor(titleColor);
        menuTitle.setLightCombineMode(LightCombineMode.Off);
        menuTitle.setText(title);
        menuTitle.update();
        titleNode.attachChild(menuTitle);
        titleNode.setLocalTranslation(50, 80, 0);
        attachChild(titleNode);
                
        for (int i = 0; i < menuPositionTexts.length; i++) {
            Node textNode = new Node("gameMenuNode" + i);
            BitmapText bitmapText = null;
            bitmapText = new BitmapText(font, false);
            bitmapTexts.add(bitmapText);
            bitmapText.setBox(rectangle);
            bitmapText.setSize(defaultPositionSize);
            bitmapText.setDefaultColor(defaultPositionColor);
            bitmapText.setLightCombineMode(LightCombineMode.Off);
            bitmapText.setText(menuPositionTexts[i]);
            textNode.attachChild(bitmapText);
            textNode.setLocalTranslation(50, i * 30, 0);
            attachChild(textNode);
            bitmapText.update();
        }
        
        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setCullHint(CullHint.Never);
        setLocalTranslation(200, 300, 0);
        BitmapText currBitmapText = bitmapTexts.get(currMenuCursor);
        currBitmapText.setSize(selectedPositionSize);
        currBitmapText.setDefaultColor(selectedPositionColor);
        currBitmapText.update();
    }
    
    public void setMenuTitle(String title) {
        menuTitle.setText(title);
        menuTitle.update();

        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setCullHint(CullHint.Never);

        if(this.getParent() != null && this.getParent() instanceof Node) {
            ((Node)this.getParent()).updateRenderState();
        }
    }
    
    public void positionUp() {
        if(bitmapTexts.size() <= 1) {
            return;
        }
        if(currMenuCursor == 0) {
            currMenuCursor = bitmapTexts.size() - 1;
        } else {
            currMenuCursor--;
        }
        updatePositionSelection();
        prevMenuCursor = currMenuCursor;
    }
    
    public void positionDown() {
        if(bitmapTexts.size() <= 1) {
            return;
        }
        if(currMenuCursor == bitmapTexts.size() - 1) {
            currMenuCursor = 0;
        } else {
            currMenuCursor++;
        }
        updatePositionSelection();
        prevMenuCursor = currMenuCursor;
    }
    
    public void selectPosition() {
        dispatchEvent();
    }
    
    public final void updatePositionSelection() {
        BitmapText prevBitmapText = bitmapTexts.get(prevMenuCursor);
        prevBitmapText.setSize(defaultPositionSize);
        prevBitmapText.setDefaultColor(defaultPositionColor);
        prevBitmapText.update();
        
        BitmapText currBitmapText = bitmapTexts.get(currMenuCursor);
        currBitmapText.setSize(selectedPositionSize);
        currBitmapText.setDefaultColor(selectedPositionColor);
        currBitmapText.update();
        
        setRenderQueueMode(Renderer.QUEUE_ORTHO);
        setCullHint(CullHint.Never);

        if(this.getParent() != null && this.getParent() instanceof Node) {
            ((Node)this.getParent()).updateRenderState();
        }
    }
    
    public boolean addObserver(MenuActionListener observer) {
        return observers.add(observer);
    }
    
    public boolean removeObserver(MenuActionListener observer) {
        return observers.remove(observer);
    }
    
    public boolean containsObserver(MenuActionListener observer) {
        return observers.contains(observer);
    }
    
    protected final void dispatchEvent() {
        for(MenuActionListener o: observers) {
            o.menuActionPerformed(new MenuActionEvent(currMenuCursor));
        }
    }
    
    public MenuInputHandler getMenuHandler() {
        return menuHandler;
    }
    
    public class MenuInputHandler extends InputHandler {
        
        public MenuInputHandler() {
            InputHandler keyboard = new InputHandler();
            KeyBindingManager keyManager = KeyBindingManager.getKeyBindingManager();
            keyManager.set("menu_up", KeyInput.KEY_UP);
            keyManager.set("menu_down", KeyInput.KEY_DOWN);
            keyManager.set("menu_select", KeyInput.KEY_RETURN);
            boolean repeat = false;
            
            keyboard.addAction(new KeyInputAction() {

                @Override
                public void performAction(InputActionEvent iae) {
                    synchronized (GameObject.class) {
                        positionUp();
                    }
                }
            }, "menu_up", repeat);
            
            keyboard.addAction(new KeyInputAction() {

                @Override
                public void performAction(InputActionEvent iae) {
                    synchronized (GameObject.class) {
                        positionDown();
                    }
                }
            }, "menu_down", repeat);
            
            keyboard.addAction(new KeyInputAction() {

                @Override
                public void performAction(InputActionEvent iae) {
                    synchronized (GameObject.class) {
                        selectPosition();
                    }
                }
            }, "menu_select", repeat);
            
            this.addToAttachedHandlers(keyboard);
        }
    }
}
