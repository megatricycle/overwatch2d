package com.overwatch2d.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Soldier76 extends Hero {
    public Soldier76(float initialX, float initialY, Player player) {
        super(
            initialX,
            initialY,
            player,
            new Texture(Gdx.files.internal("sprites/soldier76.png")),
                new Texture(Gdx.files.internal("sprites/dead_soldier76.png")),
        4f,
    200,
            new Texture(Gdx.files.internal("portraits/soldier76.png")),
            Gdx.audio.newSound(Gdx.files.internal("sfx/soldier76/spawn.ogg")),
            Gdx.audio.newSound(Gdx.files.internal("sfx/soldier76/respawn.ogg"))
        );

        weapon = new PulseRifle(this);
    }
}
