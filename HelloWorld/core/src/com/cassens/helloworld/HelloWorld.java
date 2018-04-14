package com.cassens.helloworld;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class HelloWorld extends ApplicationAdapter {

	private Texture dropImage, bonusDropImage, negDropImage, superNegDropImage;
	private Texture bucketImage;
	private Texture portalLeftImage, portalRightImage;
	private Texture backgroundImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle portalLeft, portalRight;
	private Rectangle bucket; // use this to store the position of the bucket
	private Array<Raindrop> raindrops; // use this for the raindrop positions
	private long lastDropTime; // so we know how long it's been since the last rain drop
	private BitmapFont scoreFont, pauseFont, levelFont;
	private int score, counter;
	private boolean paused, start;
	private double levelFactor;
	private int level;
	
	private class Raindrop extends Rectangle {
		private static final long serialVersionUID = 1L;
		public int type;
	}

	private void spawnRaindrop() {
		Raindrop raindrop = new Raindrop();
		raindrop.x = MathUtils.random(0+30, 800-64-30);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
		
		if(level > 10) {
			
			raindrop.type = ThreadLocalRandom.current().nextInt(1, 4 + 1);
			
		} else {
			raindrop.type = ThreadLocalRandom.current().nextInt(1, 3 + 1);
		}
	}

	@Override
	public void create() {
		// put these into video ram
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bonusDropImage = new Texture(Gdx.files.internal("droplet_green.png"));
		negDropImage = new Texture(Gdx.files.internal("droplet_red.png"));
		superNegDropImage = new Texture(Gdx.files.internal("droplet_grey.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		portalLeftImage = new Texture(Gdx.files.internal("portal_left.png"));
		portalRightImage = new Texture(Gdx.files.internal("portal_right.png"));
		backgroundImage = new Texture(Gdx.files.internal("rainy_mood.jpg"));

		// load the drop sound effect and the rain background "music"
		// use this when it is less than 10 secs
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		// use this when it's greater than 10 sec
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		// This will make sure the camera always shows us an area of our game
		// world that is 800x480 units wide.
		camera.setToOrtho(false, 800, 480);
		
		portalLeft = new Rectangle();
		portalLeft.x = 5;
		portalLeft.y = 5;
		portalRight = new Rectangle();
		portalRight.x = 800-30;
		portalRight.y = 0;

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 0;
		bucket.width = 64;
		bucket.height = 64;
		
		scoreFont = new BitmapFont();
		scoreFont.getData().setScale(2);
		score = 0;
		
		paused = false;
		pauseFont = new BitmapFont();
		pauseFont.getData().setScale(4);
		
		start = true;
		counter = 0;
		
		levelFont= new BitmapFont();
		levelFont.getData().setScale(4);
		levelFactor = 1;
		level = 1;

		raindrops = new Array<Raindrop>();
		spawnRaindrop();

	}

	@Override
	public void render() {
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		batch.setProjectionMatrix(camera.combined);
		
		if (paused) {
			batch.begin();
			batch.draw(backgroundImage, 0, 0);
			scoreFont.draw(batch, "PAUSED", 350, 260);
			batch.end();
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				paused = false;
				start = true;
			}
		}
			
		else {
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
				paused = true;
			if (Gdx.input.isKeyJustPressed(Input.Keys.C))
				score += 1000;
			
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A))
				bucket.x -= 200 * Gdx.graphics.getDeltaTime();
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D))
				bucket.x += 200 * Gdx.graphics.getDeltaTime();
			if (bucket.x < 0+25)
				bucket.x = 800-64-25;
			if (bucket.x > 800-64-25)
				bucket.x = 0+25;
			
			// Handles mouse clicks
			Gdx.input.setInputProcessor(new InputAdapter() {
			    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			        if (button == Buttons.LEFT) {
			            bucket.x -= 100;
			            return true;
			        }
			        else if (button == Buttons.RIGHT) {
			        	bucket.x += 100;
			        	return true;
			        }
			        else
			        	return false;
			    }
			});
	
			// check how much time has passed since we spawned a new raindrop, and creates a
			// new one if necessary
			if (TimeUtils.nanoTime() - lastDropTime > 1000000000*levelFactor)
				spawnRaindrop();
	
			for (Raindrop raindrop : raindrops) {
				
				raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
				// Drop hits the ground
				if (raindrop.y + 64 < 0) {
					if (score >= 10 && (raindrop.type != 3 || raindrop.type != 4))
						score -= 10;
					raindrops.removeValue(raindrop, true);
				}
				// Player catches the drop
				if (raindrop.overlaps(bucket)) {
					dropSound.play();
					if (raindrop.type == 1)
						score += 5;
					else if (raindrop.type == 2)
						score += 10;
					else if (raindrop.type == 3) {
						if (score >= 20)
							score -= 20;
						else
							score = 0;
					// Grey drop
					} else if(raindrop.type == 4) {
			
						score -= 100;
					}
					raindrops.removeValue(raindrop, true);
				}
	
			}
	
			// Start drawing
			batch.begin();
			
			batch.draw(backgroundImage, 0, 0);
			
			scoreFont.draw(batch, Integer.toString(score), 400, 470);
			
			if (start) {
				levelFont.draw(batch, "LEVEL " + level, 300, 260);
				counter++;
				if (counter > 20) {
					start = false;
					counter = 0;
				}
			}
				
			
			if (score/100.0 >= level) {
				level++;
				start = true;
				levelFactor *= 0.95;
			}
			if (score/100.0 < level-1) {
				level--;
				start = true;
				levelFactor /= 0.95;
			}
			
			for (Raindrop raindrop : raindrops) {
				if (raindrop.type == 1)
					batch.draw(dropImage, raindrop.x, raindrop.y);
				else if (raindrop.type == 2)
					batch.draw(bonusDropImage, raindrop.x, raindrop.y);
				else if (raindrop.type == 3)
					batch.draw(negDropImage, raindrop.x, raindrop.y);
				else if(raindrop.type == 4)
					batch.draw(superNegDropImage, raindrop.x, raindrop.y);
					
				}
		
			batch.draw(bucketImage, bucket.x, bucket.y);
			batch.draw(portalLeftImage, portalLeft.x, portalLeft.y);
			batch.draw(portalRightImage, portalRight.x, portalRight.y);
	
			batch.end();
		}

	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bonusDropImage.dispose();
		negDropImage.dispose();
		superNegDropImage.dispose();
		bucketImage.dispose();
		portalLeftImage.dispose();
		portalRightImage.dispose();
		backgroundImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
	
	// Where to go from here
		// 1. add a score that shows as you collect drops, decreases if you miss
		// 2. be able to pause and continue
		// 3. add different types of rain drops
		// 4. move the bucket up and down
		// 5. have different levels
}
