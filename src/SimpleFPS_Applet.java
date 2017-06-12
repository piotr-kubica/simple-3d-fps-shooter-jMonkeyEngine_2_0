
import ai.MoveCommand;
import ai.RotateByAngleCommand;
import collision.CollisionSystem;
import collision.ICollidable;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.MouseInputAction;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import com.jmex.audio.AudioSystem;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.MusicTrackQueue;
import com.jmex.audio.MusicTrackQueue.RepeatType;
import com.jmex.awt.applet.SimpleJMEApplet;
import event.GameActionEvent;
import event.GameActionListener;
import game.AnimationTimeState;
import game.Enemy;
import game.Figure;
import game.GameObject;
import game.Missile;
import game.Player;
import game.Terrain;
import util.GameTimer;
import java.net.URL;

import java.util.ArrayList;
import java.util.EnumMap;
import javax.swing.ImageIcon;
import ui.GameMenu;
import ui.HUDDisplay;
import ui.event.MenuActionEvent;
import ui.event.MenuActionListener;
import util.ExplosionFactory;
import util.ModelLoader;
import util.ModelLoader3DS;
import util.ModelLoaderMD2;
import util.Randomizer;

public class SimpleFPS_Applet extends SimpleJMEApplet {
    
    public enum GameStatus {
        RUNNING, PAUSED, LOST, WON
    }
    
    // scene dynamic objects
    static final int OGRE_CNT = 5;
    static final int TRIS_CNT = 3;
    static final int MISSILE_CNT = 10;
    static final int MAX_MOVE = 500;
    static final int MIN_MOVE = 50;
    static final int MAX_ROT = 180;
    static final int MIN_ROT = 5;
    static final int MAX_COLLISION_ROT = 90;
    static final int MIN_COLLISION_ROT = 35;
    static final int ROT_SPEED = 3;
    static final int MOVE_SPEED = 110;
    static final int MISSILE_SPEED = 350;
    static final int BG_COLOR = 0x86a1ae;
    static final int MAX_TIME = 90;
    
    CollisionSystem<ICollidable> collisionSys;
    Terrain ter;
    Player player;
    LightState ls;
    
    ArrayList<Enemy> enemies;
    ArrayList<Missile> ammo;
    EnumMap<Enemy.State, AnimationTimeState> animationData;
    ExplosionFactory explosionFactory;
    HUDDisplay hud;
    GameStatus gameStatus = GameStatus.RUNNING;
    GameMenu pauseMenu;
    GameMenu endMenu;
    MusicTrackQueue explosionTrack;
    Node cross;

    int lastTimeSec = 0;
    int pauseTimeMemory = 0;
    boolean timeOver = false;
    
    
    // APPLET
    protected Node rootNode;
    protected InputHandler input;
    protected DisplaySystem display;
    protected Camera cam;
    protected GameTimer timer;
    
    protected void initScene() {
        GameObject.SetDisplay(display);
        KeyBindingManager.getKeyBindingManager().removeAll();
        KeyBindingManager.getKeyBindingManager().set("pause_game", KeyInput.KEY_ESCAPE);

        // init terrain
        URL texMap = SimpleFPS.class.getClassLoader().getResource("assets/ground.jpg");
        URL hMap = SimpleFPS.class.getClassLoader().getResource("assets/terrain.jpg");
        ter = new Terrain(
                new Vector3f(4f, 0.3f, 4f),
                new Vector3f(0, -20, 0),
                new ImageIcon(hMap).getImage(),
                new ImageIcon(texMap));

        ter.setLocalTranslation(0, -25, 0); // height: 30-35
//        ter.setModelBound(new BoundingBox());

        // init light
        ls = display.getRenderer().createLightState();
        ls.setEnabled(true);

        ColorRGBA background = new ColorRGBA();
        background.fromIntARGB(BG_COLOR);
        display.getRenderer().setBackgroundColor(background);

        DirectionalLight light = new DirectionalLight();
        light.setEnabled(true);
        light.setAmbient(ColorRGBA.white);
        light.setDiffuse(ColorRGBA.white);
        light.setDirection(new Vector3f(0, -1, 0));
        ls.attach(light);

        // sets light for terrain
        ter.setRenderState(ls);

        // init fog
        FogState fs = display.getRenderer().createFogState();
        fs.setEnabled(true);
        fs.setDensity(0.2f);
        fs.setColor(background);
        fs.setEnd(400);
        fs.setStart(5);
        fs.setDensityFunction(FogState.DensityFunction.Linear);
        fs.setQuality(FogState.Quality.PerVertex);
        ter.setRenderState(fs);
    }
    
    protected void initSound() {
        AudioTrack explosion = AudioSystem.getSystem()
                .createAudioTrack(SimpleFPS_Applet.class.getClassLoader()
                .getResource("assets/explode.ogg"), false);
        
        explosionTrack = AudioSystem.getSystem().getMusicQueue();
        explosionTrack.setCrossfadeinTime(0);
        explosionTrack.setRepeatType(RepeatType.ONE);
        explosionTrack.addTrack(explosion);
        explosion.setVolume(explosion.getMaxVolume());
    }

    protected Node initEnemyModel(Node model) {
        model.setModelBound(new BoundingBox());
        model.updateModelBound();

        MaterialState ms = display.getRenderer().createMaterialState();
        ms.setEnabled(true);
        ms.setAmbient(ColorRGBA.gray);
        ms.setDiffuse(ColorRGBA.darkGray);
        ms.setSpecular(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
        ms.setShininess(128f);
        ms.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        model.setRenderState(ms);

        model.setRenderState(ls);
        model.lookAt(Vector3f.UNIT_X.mult(-1), Vector3f.UNIT_Y.mult(1));

        // sets light for ogre
        model.setRenderState(ls);
        return model;
    }

    @Override
    public void simpleAppletSetup() {
        // after init applet
        rootNode = getRootNode();
        input = getInputHandler();
        display = DisplaySystem.getDisplaySystem();
        cam = getCamera();
        
        
        initScene();

        // inits player
        player = new Player(10, true, 10, 20, cam, input, 0.01f, 65);
        player.setPosition(ter.getCenterXZPosition());
        ter.attachChild(player);

        // init enemy animation data for keyframe sequences
        animationData = new EnumMap<Enemy.State, AnimationTimeState>(Enemy.State.class);
        animationData.put(Enemy.State.STAND, new AnimationTimeState(0, 39)); // stand: 0 - 39
        animationData.put(Enemy.State.RUN, new AnimationTimeState(40, 45)); // run: 40 - 45

        // init assets
        URL trisMD2 = SimpleFPS.class.getClassLoader().getResource("assets/tris.md2");
        URL trisTexture = SimpleFPS.class.getClassLoader().getResource("assets/abarlith.jpg");
        ModelLoader trisLoader = new ModelLoaderMD2(trisMD2, trisTexture);

        URL ogreMD2 = SimpleFPS.class.getClassLoader().getResource("assets/ogre.md2");
        URL ogreTexture = SimpleFPS.class.getClassLoader().getResource("assets/igdosh.jpg");
        ModelLoader ogreLoader = new ModelLoaderMD2(ogreMD2, ogreTexture);

        enemies = new ArrayList<Enemy>();

        for (int i = 0; i < OGRE_CNT; i++) {
            Node enemy = initEnemyModel(ogreLoader.load(display.getRenderer()));
            enemy.setLocalScale(0.75f);
            enemies.add(new Enemy(enemy, player, animationData, true, 50, 37));
        }
        for (int j = 0; j < TRIS_CNT; j++) {
            Node enemy = initEnemyModel(trisLoader.load(display.getRenderer()));
            enemy.setLocalScale(0.75f);
            enemies.add(new Enemy(enemy, player, animationData, true, 50, 37));
        }

        // add enemies to scene
        for (int i = 0; i < enemies.size(); i++) {
            Vector3f rpos = ter.getRandomXZPosition();
            enemies.get(i).setPosition(rpos);
            ter.attachChild(enemies.get(i));
        }

        // MISSILE
        ammo = new ArrayList<Missile>();
        ModelLoader ml = new ModelLoader3DS(SimpleFPS.class.getClassLoader().getResource("assets/missile.3ds"));

        for (int i = 0; i < MISSILE_CNT; i++) {
            Node missileObj = ml.load(display.getRenderer());
            missileObj.getLocalRotation().fromAngles(0, -90 * FastMath.DEG_TO_RAD, 0);
            missileObj.setLocalScale(0.2f);
            missileObj.setLocalTranslation(0, 7, 0);
            Missile missile = new Missile(missileObj, ter, false);
            missile.setModelBound(new BoundingSphere());
            missile.updateModelBound();
            ammo.add(missile);
            ter.attachChild(missile);

            // missile leaves scene
            missile.addObserver(Missile.Event.MISSED, new GameActionListener() {

                @Override
                public void gameActionPerformed(GameActionEvent e) {
                    synchronized (GameObject.class) {
                        if (e.getEventSource() instanceof Missile) {
                            Missile m = (Missile) e.getEventSource();
                            collisionSys.removeCollidable(m);
                            ter.detachChild(m);
                            ammo.add(m);
                        }
                    }
                }
            });

            // missile hits sth
            missile.addObserver(Missile.Event.HIT_MISSILE, new GameActionListener() {

                @Override
                public void gameActionPerformed(GameActionEvent e) {
                    synchronized (GameObject.class) {
                        if (e.getEventSource() instanceof Missile) {
                            Missile m = (Missile) e.getEventSource();
                            Vector3f pos = m.getPosition();
                            collisionSys.removeCollidable(m);
                            ter.detachChild(m);
                            ammo.add(m);

                            // missile explodes
                            explosionFactory.spawnExplosion(pos.subtract(0, 0, 0));
                            
                            // plays explosion sound
                            if(explosionTrack != null) {
                                explosionTrack.play();
                            }
                        }
                    }
                }
            });
        }

        for (Enemy e : enemies) {

            // enemy scared
            e.addObserver(Enemy.AIEvent.SCARED, new GameActionListener() {

                @Override
                public void gameActionPerformed(GameActionEvent e) {
                    synchronized (GameObject.class) {
                        Enemy e1 = (Enemy) e.getEventSource();
                        Figure e2 = (Figure) e.getParamObj();
                        float angle = e1.figureXZAngle(e2);

                        if (Math.abs(angle) * FastMath.RAD_TO_DEG < 60) {
                            e1.clearCommands();
                            e1.setLinVel(0);
                            e1.setRotVel(Vector3f.ZERO);

                            if (angle < 0) {
                                e1.addCommand(new RotateByAngleCommand(
                                        e1, 90 + angle, ROT_SPEED));
                            } else {
                                e1.addCommand(new RotateByAngleCommand(
                                        e1, 90 - angle, ROT_SPEED));
                            }
                        }
                        // XXX
                        e1.addCommand(new MoveCommand(e1, Randomizer.GetRandom(MIN_MOVE, MAX_MOVE), MOVE_SPEED));
                    }
                }
            });

            // enemy uncaring
            e.addObserver(Enemy.AIEvent.UNCARING, new GameActionListener() {

                @Override
                public void gameActionPerformed(GameActionEvent e) {
//                    System.out.println("enemy uncaring");
                    synchronized (GameObject.class) {
                        Enemy enemy = (Enemy) e.getEventSource();
                        enemy.clearCommands();
                        enemy.setLinVel(0);
                        enemy.setRotVel(Vector3f.ZERO);
                        enemy.addCommand(new RotateByAngleCommand(enemy, Randomizer.GetRandom(-MAX_ROT, MAX_ROT), ROT_SPEED));
                        enemy.addCommand(new MoveCommand(enemy, Randomizer.GetRandom(MIN_MOVE, MAX_MOVE), MOVE_SPEED));
                    }
                }
            });

            // enemy hit by missile
            e.addObserver(Enemy.CollisionEvent.HIT_ENEMY, new GameActionListener() {

                @Override
                public void gameActionPerformed(GameActionEvent e) {
                    synchronized (GameObject.class) {
                        Enemy enemy = (Enemy) e.getEventSource();
                        enemy.clearCommands();
                        enemy.setLinVel(0);
                        enemy.setRotVel(Vector3f.ZERO);
                        enemy.setState(Enemy.State.DEATH);
                        collisionSys.removeCollidable(enemy);
                        ter.detachChild(enemy);
                        
                        // update frags
                        updateHUDFragCount(getFragCount());
                        
                        if(checkWinCondition()) {
                            gameWon();
                        }
                    }
                }
            });
        }

        // PARTICLE SYSTEM
        explosionFactory = new ExplosionFactory(rootNode, "assets/explosion.png");
        explosionFactory.setExplosionSize(50);

        // ---
        rootNode.attachChild(ter);
        // ---
        
        // MISC
        hud = new HUDDisplay(
                SimpleFPS.class.getClassLoader().getResource("assets/font.fnt"), 
                SimpleFPS.class.getClassLoader().getResource("assets/font_0.png"));
        rootNode.attachChild(hud);
        
        pauseMenu = new GameMenu(hud.getFont(), "Game Pause", "Resume", "Restart");
        
        pauseMenu.addObserver(new MenuActionListener() {

            @Override
            public void menuActionPerformed(MenuActionEvent e) {
                if(e.getPosition() == 1) {
                    restartGame();
                } else if(e.getPosition() == 0) {
                    resumeGame();
                }
            }
        });
        
        endMenu = new GameMenu(hud.getFont(), "You win!", "Restart");
        endMenu.addObserver(new MenuActionListener() {
            
            @Override
            public void menuActionPerformed(MenuActionEvent e) {
                System.out.println("restart");
                restartGame();
            }
        });

        player.getInputHandler().addAction(new MouseInputAction() {
            @Override
            public void performAction(InputActionEvent iae) {
                synchronized (GameObject.class) {
                    // left mouse button clicked
                    if (MouseInput.get().isButtonDown(0)) {
                        Vector3f hd = player.getHeadingDirection();
                        Vector3f missilePosition = player.getPosition().add(hd).add(0, 16, 0);

                        if (!ammo.isEmpty()) {
                            synchronized (GameObject.class) {
                                Missile m = ammo.remove(0);
                                ter.attachChild(m);
                                collisionSys.addCollidable(m);

                                m.setDirection(hd);
                                m.setPosition(missilePosition);
                                m.setLinVel(MISSILE_SPEED);
                            }
                        } else {
                            System.out.println("Couldn't fire missile. Empty ammo");
                        }
                    }
                }
            }
        }, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_ALL, 0, false);

        // --- 
        // init collision system
        collisionSys = CollisionSystem.getInstance();
        timer = new GameTimer();
        ter.update(timer.getTimePerFrame());
        
        initSound();
        
        // init cross
        cross = new Node("cross");
        Quad c1 = new Quad("cross_part1", 10, 2);
        Quad c2 = new Quad("cross_part2", 10, 2);
        Quad c3 = new Quad("cross_part3", 2, 10);
        Quad c4 = new Quad("cross_part4", 2, 10);
        c1.setLocalTranslation(-10, 0, 0);
        c2.setLocalTranslation(10, 0, 0);
        c3.setLocalTranslation(0, -10, 0);
        c4.setLocalTranslation(0, 10, 0);
        cross.attachChild(c1);
        cross.attachChild(c2);
        cross.attachChild(c3);
        cross.attachChild(c4);
        
        MaterialState ms = this.display.getRenderer().createMaterialState();
        ms.setAmbient(new ColorRGBA(255 / 255f, 0 / 255f, 0 / 255f, 1.0f));
        ms.setDiffuse(ColorRGBA.black);
        ms.setSpecular(ColorRGBA.black);
        ms.setEnabled(true);
        
        cross.setRenderState(ms);
        cross.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        cross.setLocalTranslation(new Vector3f(display.getWidth() / 2, display.getHeight() / 2 - 4,0));       
        rootNode.attachChild(cross);
        
        updateHUDFragCount(getFragCount());
        updateHUDTime(lastTimeSec);
        
        // !!
//        this.input
        // XXX
        // XXX instead of input = player.getInputHandler()
        setInputHandler(player.getInputHandler());
        this.input.setEnabled(true);
    }

    @Override
    public void simpleAppletUpdate() {
        if (KeyBindingManager.getKeyBindingManager().isValidCommand("pause_game", false)) {
            if(gameStatus == GameStatus.PAUSED) {
                resumeGame();
            } else if(gameStatus == GameStatus.RUNNING) {
                pauseGame();
            }
        }

        synchronized (GameObject.class) {
            collisionSys.detectCollisions();
//            System.out.println("" + ter.maxBoundingWorldVertex());
//            System.out.println("" + ter.minBoundingWorldVertex());
//            System.out.println("" + timer.getTimePerFrame());
            
            //ter.update(timer.getTimePerFrame());
            
            // check if game ended
            if(gameStatus == GameStatus.RUNNING) {
                int t = updateGameTime();
                
                if(t <= 0) {
                    gameLost();
                } else {
                    updateHUDTime(t);
                }
            }
        }
    }
    
    public void updateHUDTime(int seconds) {
        int sec = seconds % 60;
        int min = seconds / 60;
        hud.setTime(min, sec);
    }
    
    public void updateHUDFragCount(int frags) {
        hud.setFrags(frags, enemies.size());
    }
    
    public void restartGame() {
        // reset player
        gameStatus = GameStatus.RUNNING;
        player.setPosition(ter.getCenterXZPosition());
        this.input = player.getInputHandler();
        
        if(rootNode.hasChild(pauseMenu)) {
            rootNode.detachChild(pauseMenu);
        } else if(rootNode.hasChild(endMenu)) {
            rootNode.detachChild(endMenu);
        }
        
        // remove remainig enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            
            if(ter.hasChild(e)) {
                ter.detachChild(e);
            }
            if(collisionSys.hasCollidable(e)) {
                collisionSys.removeCollidable(e);
            }
            e.clearCommands();
            e.setLinVel(0);
            e.setRotVel(Vector3f.ZERO);
            e.setState(Enemy.State.DEATH);
        }
        
        // add enemies to scene
        for (int i = 0; i < enemies.size(); i++) {
            Vector3f rpos = ter.getRandomXZPosition();
            Enemy e = enemies.get(i);
            e.setPosition(rpos);
            e.resetAI();
            ter.attachChild(e);
            collisionSys.addCollidable(e);
        }
        
        // reset frags
        updateHUDFragCount(getFragCount());
        
        // reset timer
        lastTimeSec = pauseTimeMemory = (int)timer.getTimeInSeconds();
        updateHUDTime(lastTimeSec);
    }
    
    public void resumeGame() {
        pauseTimeMemory = (int)timer.getTimeInSeconds() - lastTimeSec;
        gameStatus = GameStatus.RUNNING;
        rootNode.detachChild(pauseMenu);
        this.input = player.getInputHandler();
    }
    
    public void pauseGame() {
        gameStatus = GameStatus.PAUSED;
        rootNode.attachChild(pauseMenu);
        this.input = pauseMenu.getMenuHandler();
    }
    
    public void gameLost() {
        if(gameStatus == GameStatus.LOST) {
            return;
        }
        gameStatus = GameStatus.LOST;
        rootNode.attachChild(endMenu);
        endMenu.setMenuTitle("You lose!");
        this.input = endMenu.getMenuHandler();
    }
    
    public void gameWon() {
        if(gameStatus == GameStatus.WON) {
            return;
        }
        gameStatus = GameStatus.WON;
        rootNode.attachChild(endMenu);
        endMenu.setMenuTitle("You win!");
        this.input = endMenu.getMenuHandler();
    }
    
    public int getFragCount() {
        int cnt = 0;
                        
        for (int i = 0; i < enemies.size(); i++) {
            if(!ter.hasChild(enemies.get(i))) {
                cnt++;
            }
        }
        return cnt;
    }
    
    // returns seconds to end of game
    public int updateGameTime() {
        if(lastTimeSec != ((int)timer.getTimeInSeconds() - pauseTimeMemory)) {
            lastTimeSec = ((int)timer.getTimeInSeconds() - pauseTimeMemory);
        }
        return MAX_TIME - lastTimeSec;
    }
    
    public boolean checkWinCondition() {
        return getFragCount() == enemies.size();
    }
}