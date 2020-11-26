package com.dongbat.jbump.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.dongbat.jbump.*;
import com.dongbat.jbump.Response.Result;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

import static com.dongbat.jbump.test.TestBump.Mode.*;

/**
 * A runnable game to demonstrate the use and functionality of JBump.
 * @author Raymond "Raeleus" Buckley
 */
public class TestBump extends ApplicationAdapter {
    public static final float LINE_WIDTH = .1f;
    public Texture texture;
    public SpriteBatch spriteBatch;
    public ShapeDrawer shapeDrawer;
    public ExtendViewport gameViewport;
    public ScreenViewport uiViewport;
    public OrthographicCamera camera;
    public Array<Entity> entities;
    public static final String MAP =
                    "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" +
                    "+------------------------+-------------------------------------------------------------------------+\n" +
                    "+------------------------+-------------------------------------------------------------------------+\n" +
                    "+-++--++-----------------+----------------+-+-+-+-+-+----------------------------------------------+\n" +
                    "+------------------------+-------------------------------------------------------------------------+\n" +
                    "+-----------+++++++------+------++-----------------------------------------------------------------+\n" +
                    "+-----------------+------+------++-----------------------------------------------------------------+\n" +
                    "+-----------------+-------------++-----------------------------------------------------------------+\n" +
                    "+p----------------+-------------++-----------------------------------------------------------------+\n" +
                    "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
    public World<Entity> world;
    public BitmapFont font;
    public Mode mode;
    public enum Mode {
        POINT, RECT, SEGMENT, SEGMENT_WITH_COORDS, RAY, RAY_WITH_COORDS
    }
    public int debugMagnitude;
    public static final int DEBUG_VALUE_INCREASE = 15;
    public static final float DEBUG_MAX_RECT_SIZE = 4f;
    public static final float DEBUG_SEGMENT_LENGTH = 4f;
    public static final Vector2 tempVector = new Vector2();
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(800, 800);
        new Lwjgl3Application(new TestBump(), config);
    }
    
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
    
        //Create a TextureRegion containing a single, white pixel to enable rendering with ShapeDrawer.
        Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.drawPixel(0, 0);
        texture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegion textureRegion = new TextureRegion(texture, 0, 0, 1, 1);
        shapeDrawer = new ShapeDrawer(spriteBatch, textureRegion);
        
        camera = new OrthographicCamera();
        gameViewport = new ExtendViewport(10, 10, camera);
        uiViewport = new ScreenViewport();
        shapeDrawer.update();
        
        font = new BitmapFont();
        mode = POINT;
        
        world = new World<Entity>(1f);
        entities = new Array<Entity>();
        String[] lines = MAP.split("\n");
        for (int j = 0; j < lines.length; j++) {
            String line = lines[j];
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '+') {
                    entities.add(new WallEntity(i, lines.length - j));
                } else if (line.charAt(i) == 'p') {
                    entities.add(new PlayerEntity(i, lines.length - j));
                }
            }
        }
    }
    
    @Override
    public void render() {
        //act
        float delta = Gdx.graphics.getDeltaTime();
        for (Entity entity : entities) {
            entity.act(delta);
        }
        
        //draw
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gameViewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeDrawer.update();
        spriteBatch.begin();
        for (Entity entity : entities) {
            entity.draw();
        }
        
        //conduct debug test, render shapes, and get text
        String debugText = conductDebugTest();
        
        //draw debug text
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        font.setColor(Color.RED);
        font.draw(spriteBatch, debugText, 10f, uiViewport.getWorldHeight() - 10f);
        spriteBatch.end();
    }
    
    public final ArrayList<Item> items = new ArrayList<Item>();
    public final ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
    
    public String conductDebugTest() {
        if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
            mode = Mode.values()[mode.ordinal() == Mode.values().length - 1 ? 0 : mode.ordinal() + 1];
        }
        
        if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
            debugMagnitude += DEBUG_VALUE_INCREASE;
            debugMagnitude %= 360;
        }
        
        StringBuilder builder = new StringBuilder("Mouse Coords: (");
        tempVector.set(Gdx.input.getX(), Gdx.input.getY());
        builder.append((int) tempVector.x).append(", ").append((int) tempVector.y).append(") Game Coords: (");
        gameViewport.unproject(tempVector);
        builder.append(tempVector.x).append(", ").append(tempVector.y).append(")\nMode: ").append(mode).append("\n");
        float x = tempVector.x;
        float y = tempVector.y;
        
        shapeDrawer.setColor(Color.ORANGE);
        shapeDrawer.setDefaultLineWidth(.05f);
        switch (mode) {
            case POINT: //collision check at mouse coordinates
                world.queryPoint(x, y, CollisionFilter.defaultFilter, items);
                shapeDrawer.circle(x, y, .1f);
                break;
            case RECT: //collision check of rectangle centered at mouse coordinates
                float dimension = debugMagnitude / 360f * DEBUG_MAX_RECT_SIZE;
                world.queryRect(x - dimension / 2, y - dimension / 2, dimension, dimension, CollisionFilter.defaultFilter, items);
                shapeDrawer.rectangle(x - dimension / 2, y - dimension / 2, dimension, dimension);
                break;
            case SEGMENT: //collision check of line segment starting at mouse coordinates
                tempVector.set(DEBUG_SEGMENT_LENGTH, 0);
                tempVector.rotate(debugMagnitude);
                tempVector.add(x, y);
                world.querySegment(x, y, tempVector.x, tempVector.y, CollisionFilter.defaultFilter, items);
                shapeDrawer.line(x, y, tempVector.x, tempVector.y);
                break;
            case SEGMENT_WITH_COORDS: //collision check of line segment with drawing ending at closest collision point
                tempVector.set(DEBUG_SEGMENT_LENGTH, 0);
                tempVector.rotate(debugMagnitude);
                tempVector.add(x, y);
                world.querySegmentWithCoords(x, y, tempVector.x, tempVector.y, CollisionFilter.defaultFilter, infos);
                items.clear();
                for (ItemInfo info : infos) {
                    items.add(info.item);
                }
                if (infos.size() == 0) {
                    shapeDrawer.line(x, y, tempVector.x, tempVector.y);
                } else {
                    shapeDrawer.line(x, y, infos.get(0).x1, infos.get(0).y1);
                }
                break;
            case RAY:
                tempVector.set(1, 0);
                tempVector.rotate(debugMagnitude);
                tempVector.scl(DEBUG_SEGMENT_LENGTH);
                world.queryRay(x, y, tempVector.x, tempVector.y, CollisionFilter.defaultFilter, items);
                shapeDrawer.line(x, y, x + tempVector.x, y + tempVector.y);
                break;
            case RAY_WITH_COORDS: //collision check of line segment with drawing ending at closest collision point
                tempVector.set(DEBUG_SEGMENT_LENGTH, 0);
                tempVector.rotate(debugMagnitude);
                world.queryRayWithCoords(x, y, tempVector.x, tempVector.y, CollisionFilter.defaultFilter, infos);
                items.clear();
                for (ItemInfo info : infos) {
                    items.add(info.item);
                }
                if (infos.size() == 0) {
                    shapeDrawer.line(x, y, x + tempVector.x, y + tempVector.y);
                } else {
                    shapeDrawer.line(x, y, infos.get(0).x1, infos.get(0).y1);
                }
                break;
        }
        
        builder.append("items: ").append(items.size());
        for (Item item : items) {
            builder.append(", ").append(item.userData.getClass().getSimpleName());
        }
        
        return builder.toString();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        uiViewport.update(width, height, true);
        shapeDrawer.update();
    }
    
    @Override
    public void dispose() {
        texture.dispose();
        spriteBatch.dispose();
    }
    
    public static abstract class Entity {
        public float x, y, width, height;
        public Item<Entity> item;
        public abstract void act(float delta);
        public abstract void draw();
    }
    
    /**
     * The player entity that can be controlled via keyboard (WASD). It should not pass through wall entities. The
     * camera will follow this entity as it moves through the level.
     */
    public class PlayerEntity extends Entity {
        public static final float MOVE_SPEED = 5f;
        public PlayerEntity(float x, float y) {
            this.x = x;
            this.y = y;
            width = 1;
            height = 2;
            item = new Item<Entity>(this);
            world.add(item, x, y, width, height);
        }
    
        @Override
        public void act(float delta) {
            if (Gdx.input.isKeyPressed(Keys.A)) {
                x -= MOVE_SPEED * delta;
            } else if (Gdx.input.isKeyPressed(Keys.D)) {
                x +=  MOVE_SPEED * delta;
            }
            
            if (Gdx.input.isKeyPressed(Keys.W)) {
                y += MOVE_SPEED * delta;
            }
            
            if (Gdx.input.isKeyPressed(Keys.S)) {
                y -= MOVE_SPEED * delta;
            }
            
            if (Gdx.input.isKeyJustPressed(Keys.I)) {
                for (Item item : world.getItems()) {
                    Entity entity = (Entity) item.userData;
                    System.out.println(entity.getClass().getSimpleName() + " " + entity.x + " " + entity.y + " " + entity.width + " " + entity.height);
                }
            }
    
            Result result = world.move(item, x, y, CollisionFilter.defaultFilter);
            //comment out the following lines to unbind entity position to physics position ------
            Rect rect = world.getRect(item);
            x = rect.x;
            y = rect.y;
            //------
            for (int i = 0; i < result.projectedCollisions.size(); i++) {
                Collision collision = result.projectedCollisions.get(i);
                System.out.println(collision.normal.x + " " + collision.normal.y);
            }
            
            camera.position.set(x + width / 2f, y + height / 2f, 0);
        }
    
        @Override
        public void draw() {
            //draw where the entity is
            shapeDrawer.setColor(Color.GREEN);
            shapeDrawer.filledRectangle(x, y, width, height);
            
            //draw where the physics simulation is
            shapeDrawer.setColor(Color.LIME);
            shapeDrawer.setDefaultLineWidth(LINE_WIDTH);
            Rect rect = world.getRect(item);
            shapeDrawer.rectangle(rect.x + LINE_WIDTH / 2, rect.y + LINE_WIDTH / 2, rect.w - LINE_WIDTH, rect.h - LINE_WIDTH);
        }
    }
    
    public class WallEntity extends Entity {
        public WallEntity(float x, float y) {
            this.x = x;
            this.y = y;
            width = 1;
            height = 1;
            item = new Item<Entity>(this);
            world.add(item, x, y, width, height);
        }
    
        @Override
        public void act(float delta) {
        
        }
    
        @Override
        public void draw() {
            //draw where the entity is
            shapeDrawer.setColor(Color.GRAY);
            shapeDrawer.filledRectangle(x, y, width, height);
    
            //draw where the physics simulation is
            shapeDrawer.setColor(Color.WHITE);
            shapeDrawer.setDefaultLineWidth(LINE_WIDTH);
            Rect rect = world.getRect(item);
            shapeDrawer.rectangle(rect.x + LINE_WIDTH / 2, rect.y + LINE_WIDTH / 2, rect.w - LINE_WIDTH, rect.h - LINE_WIDTH);
        }
    }
}