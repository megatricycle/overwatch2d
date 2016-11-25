package com.overwatch2d.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by geeca on 11/17/16.
 */
public class HostScreen implements Screen{
    private static Overwatch2D game = null;
    private OrthographicCamera camera;
    private Stage stage;
    private static ArrayList<Player> p = new ArrayList<Player>();

    HostScreen(final Overwatch2D gam) {
        Overwatch2D.createServer();

        float w = Gdx.graphics.getWidth(),
                h = Gdx.graphics.getHeight();

        game = gam;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        camera.update();

        stage = new Stage(new ExtendViewport(w, h, camera));

        Image background = new Image(new Texture(Gdx.files.internal("background/hostGame.jpg")));
        background.setSize(w, h);

        stage.addActor(background);

        TextButton.TextButtonStyle textStyle = new TextButton.TextButtonStyle();
        textStyle.font = game.font;

        Label.LabelStyle style = new Label.LabelStyle(game.font, Color.WHITE);
        Label waitingLabel = new Label("Waiting for players..", style);

        waitingLabel.setPosition(500, 500);
        stage.addActor(waitingLabel);

        TextButton begin = new TextButton("Begin Game", textStyle);
        begin.setPosition(890, begin.getHeight()/2);

        stage.addActor(begin);

        final Image beginGradient = new Image(new Texture(Gdx.files.internal("effects/orange.jpg")));
        beginGradient.setScale(0.45f);
        beginGradient.setPosition(890, begin.getHeight()/2);
        beginGradient.setColor(1, 1, 1, 0);

        stage.addActor(beginGradient);

        beginGradient.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent e, float x, float y) {
                startGame();

                dispose();
            }

            @Override
            public void enter(InputEvent e, float x, float y, int pointer, Actor fromActor) {
                beginGradient.setVisible(true);
                beginGradient.addAction(Actions.fadeIn(0.1f));
            }

            @Override
            public void exit(InputEvent e, float x, float y, int pointer, Actor fromActor) {
                beginGradient.addAction(Actions.fadeOut(0.1f));
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public static void setPlayers(ArrayList<Player> players) {
        p = players;

        System.out.println("Host screen:");
        System.out.println(players);
    }

    public static void startGame() {
        System.out.println(p);

        game.setScreen(new GameScreen(game, p, "Host"));

        Overwatch2D.getServer().startGame();
    }
}
