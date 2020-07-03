package com.dongbat.jbump.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.dongbat.jbump.CollisionFilter;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;
import com.dongbat.jbump.World;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * A runnable game to demonstrate the use and functionality of JBump.
 * @author Raymond "Raeleus" Buckley
 */
public class TestBump extends ApplicationAdapter {
    public static final float LINE_WIDTH = .1f;
    public Texture texture;
    public SpriteBatch spriteBatch;
    public ShapeDrawer shapeDrawer;
    public ExtendViewport viewport;
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
        viewport = new ExtendViewport(10, 10, camera);
        shapeDrawer.update();
        
        world = new World<Entity>();
        
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
        
        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeDrawer.update();
        spriteBatch.begin();
        for (Entity entity : entities) {
            entity.draw();
        }
        spriteBatch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
    
            world.move(item, x, y, CollisionFilter.defaultFilter);
            //comment out the following lines to unbind entity position to physics position ------
            Rect rect = world.getRect(item);
            x = rect.x;
            y = rect.y;
            //------
            
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
