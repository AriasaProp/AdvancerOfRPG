package com.ariasaproject.advancerofrpg.screen;

import com.ariasaproject.advancerofrpg.ApplicationListener;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.actions.Actions;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Image;
import com.ariasaproject.advancerofrpg.scenes2d.ui.ImageButton;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Label;
import com.ariasaproject.advancerofrpg.scenes2d.ui.List;
import com.ariasaproject.advancerofrpg.scenes2d.ui.ScrollPane;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Skin;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Table;
import com.ariasaproject.advancerofrpg.scenes2d.ui.TextButton;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Value;
import com.ariasaproject.advancerofrpg.scenes2d.ui.VerticalGroup;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Window;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ClickListener;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Scaling;

public class Dashboard extends Scene {

    final InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };
    Actor previousActor = null;

    public Dashboard(ApplicationListener appl) {
        super(appl);
    }

    @Override
    public void render(float delta) {
        appl.stage.act(delta);
        appl.stage.draw();
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
    public void show() {
        appl.stage.addCaptureListener(ignoreTouchDown);
        appl.stage.addAction(Actions.sequence(Actions.fadeIn(0.15f), Actions.removeListener(ignoreTouchDown, true)));
        Skin skin = ApplicationListener.asset.get("uiskin/system/systemskin.json");
        // all ui creation
        // table initialize
        final Table mainTab = new Table(), configTab = new Table();
        // main table generate
        mainTab.setFillParent(true);
        Table table = new Table();
        Label label = new Label("999 ms", skin);
        label.setName("ping");
        table.add(label);
        ImageButton imageButton = new ImageButton(skin, "config");
        imageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                mainTab.addCaptureListener(ignoreTouchDown);
                mainTab.remove();
                appl.stage.addActor(configTab);
                configTab.removeCaptureListener(ignoreTouchDown);
            }
        });
        table.add(imageButton);
        table.right();
        mainTab.add(table).colspan(3).growX().row();
        mainTab.add();
        label = new Label("Adventure RPG", skin, "title");
        label.setAlignment(Align.center);
        mainTab.add(label).pad(new Value.Fixed(20)).growX().center();
        mainTab.add();
        mainTab.row();
        VerticalGroup verticalGroup = new VerticalGroup();
        label = new Label("31/12/9999  99 : 99 ", skin);
        label.setName("date time");
        label.setWrap(false);
        label.setAlignment(Align.center);
        verticalGroup.addActor(label);
        List<String> list = new List<String>(skin);
        list.setItems("First Event", "Second Event", "Third Event", "Fourth Event", "Fifth Event", "More Event");
        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.setFlingTime(1.0f);
        verticalGroup.addActor(scrollPane);
        mainTab.add(verticalGroup).grow();
        table = new Table();
        table.setBackground(skin.getDrawable("account_background"));
        Image image = new Image(skin, "strgrd_pan");
        image.setScaling(Scaling.stretch);
        table.add(image).pad(10).fill(true);
        verticalGroup = new VerticalGroup();
        verticalGroup.align(Align.left);
        label = new Label("Name : Ariasa_Prop", skin);
        verticalGroup.addActor(label);
        label = new Label("Level : 99999", skin);
        verticalGroup.addActor(label);
        table.add(verticalGroup).expandY();
        table.row();
        TextButton textButton = new TextButton("Play", skin, "menu");
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                appl.setScene(new GameView(appl));
            }
        });
        table.add(textButton).growX();
        textButton = new TextButton("World View", skin, "menu");
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                appl.setScene(new WorldView(appl));
            }
        });
        table.add(textButton).growX();
        table.row();
        textButton = new TextButton("Close", skin, "menu");
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                GraphFunc.app.exit();
            }
        });
        table.add(textButton).growX().colspan(2).pad(10);
        mainTab.add(table).grow();
        verticalGroup = new VerticalGroup();
        mainTab.add(verticalGroup).grow();
        label = new Label("Tips = ...", skin);
        label.setEllipsis("...");
        verticalGroup.addActor(label);
        mainTab.row();
        table = new Table();
        textButton = new TextButton("Share", skin, "tab");
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {

            }
        });
        table.add(textButton).expand().fillX();
        textButton = new TextButton("Social Media", skin, "tab");
        table.add(textButton).expand().fillX();
        textButton = new TextButton("Market", skin, "tab");
        table.add(textButton).expand().fillX();
        textButton = new TextButton("Website", skin, "tab");
        table.add(textButton).expand().fillX();
        mainTab.add(table).colspan(3).padTop(15).growX();
        // generate setting table
        configTab.setFillParent(true);
        configTab.setBackground(skin.getDrawable("panel"));
        label = new Label("Configuration", skin, "title");
        label.setAlignment(Align.center);
        configTab.add(label).colspan(2).growX().center();
        imageButton = new ImageButton(skin, "close");
        imageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                configTab.addCaptureListener(ignoreTouchDown);
                configTab.remove();
                appl.stage.addActor(mainTab);
                mainTab.removeCaptureListener(ignoreTouchDown);
            }
        });
        configTab.add(imageButton).pad(new Value.Fixed(4)).uniformX();
        configTab.row();
        table = new Table();
        // general config set
        Window window = new Window("General Configuration", skin);
        window.setName("generalConfig");
        configTab.row();
        configTab.add();
        textButton = new TextButton("Restart", skin, "tab");
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                GraphFunc.app.restart();
            }
        });
        configTab.add(textButton).expandX().colspan(2).right();
        textButton = new TextButton("Set To Default", skin, "tab");
        configTab.add(textButton).expandX().colspan(2).right();
        mainTab.setName("main");
        configTab.setName("config");
        GraphFunc.app.getInput().setInputProcessor(appl.stage);

        // mainTab.addAction(reAct);

        // mainTab.removeCaptureListener(ignoreTouchDown);
        appl.stage.addActor(mainTab);
    }
    /*
     * Action reAct = new Action() { long pingUpdate = 1100;
     *
     * @Override public final boolean act(float delta) { Pool pool = getPool();
     * setPool(null); // Ensure this action can't be returned to the pool inside the
     * delegate action. try { pingUpdate -= (long) (delta * 1000.0f); if (pingUpdate
     * < 0) { pingUpdate = 1100;
     * GraphFunc.app.getNet().sendHttpRequest("https://1.1.1.1/", Net.Method_GET,
     * listener, 1000, null); } } finally { setPool(pool); } return false; }
     *
     * HttpResponseListener listener = new HttpResponseListener() { // for time date
     * final DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm"); final
     * Label[] l = new Label[2]; float re = 1; String da = "";
     *
     * @Override public void handle(long ping, HttpURLConnection r) { try { if (ping
     * >= 0 && r != null && r.getResponseCode() == HttpURLConnection.HTTP_OK) { if
     * (l[0] == null) l[0] = ((Group) getActor()).<Label>findActor("date time");
     * l[0].setText(format.format(r.getDate())); re = Math.min(1, (float) ping /
     * 1000l); da = ping + " ms"; } else { re = 1; da = "Signal Lost"; } } catch
     * (IOException e) { da = e.getMessage(); } if (l[1] == null) l[1] = ((Group)
     * getActor()).<Label>findActor("ping"); l[1].setColor((float) Math.sqrt(re),
     * (float) Math.sqrt(1f - re), 0, 1); l[1].setText(da); } }; };
     */
}
