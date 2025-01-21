package net.bfsr.client.gui.connect;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.Client;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.Gui;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Log4j2
public class GuiConnect extends Gui {
    private final Label connectingText = new Label(Font.XOLONIUM_FT, 20).setColor(1.0f, 1.0f,
            1.0f, 0.0f);

    public GuiConnect(Gui parentGui) {
        super(parentGui);

        int fontSize = 20;
        int offsetX = 24;

        InputBox hostInputBox = new InputBox(TextureRegister.guiButtonBase, Lang.getString("gui.connect.host"),
                fontSize, offsetX, 0).setString("192.168.2.2:34000");
        add(hostInputBox.atCenter(0, 50));
        InputBox usernameInputBox = new InputBox(TextureRegister.guiButtonBase, Lang.getString("gui.connect.username"),
                fontSize, offsetX, 0).setString("Krogenit");
        add(usernameInputBox.atCenter(0, 0));
        InputBox passwordInputBox = new InputBox(TextureRegister.guiButtonBase, Lang.getString("gui.connect.password"),
                fontSize, offsetX, 0).setString("test");
        add(passwordInputBox.atCenter(0, -50));

        add(new Button(Lang.getString("gui.connect.connect"), () -> {
            if (connectingText.getColor().w <= 0.01f) {
                setErrorMessage(Lang.getString("gui.connecting"));
                Thread t = new Thread(() -> {
                    InetAddress inetaddress = null;
                    String[] inputString = hostInputBox.getString().split(":");
                    if (inputString.length > 1) {
                        String host = inputString[0];
                        int port;

                        try {
                            port = Integer.parseInt(inputString[1]);
                        } catch (NumberFormatException e) {
                            Client.get().addFutureTask(() -> setErrorMessage("Wrong port number"));
                            return;
                        }

                        String playerName = usernameInputBox.getString();

                        if (playerName.length() < 3) {
                            Client.get().addFutureTask(() -> setErrorMessage("Username must be at least 3 characters"));
                            return;
                        }

                        String password = passwordInputBox.getString();

                        if (password.length() < 3) {
                            Client.get().addFutureTask(() -> setErrorMessage("Password must be at least 3 characters"));
                            return;
                        }

                        try {
                            inetaddress = InetAddress.getByName(host);
                            Client.get().connectToServer(inetaddress, port, playerName);
                        } catch (UnknownHostException unknownhostexception) {
                            log.error("Couldn't connect to server", unknownhostexception);
                            Client.get().addFutureTask(() -> setErrorMessage(Lang.getString("connect.failed") + " Unknown Host"));
                            Client.get().clearNetwork();
                        } catch (Exception e) {
                            log.error("Couldn't connect to server", e);
                            String s = e.toString();

                            if (inetaddress != null) {
                                String s1 = inetaddress + ":" + port;
                                s = s.replaceAll(s1, "");
                            }

                            String finalS = s;
                            Client.get().addFutureTask(() -> setErrorMessage(Lang.getString("connect.failed") + " " + finalS));
                            Client.get().clearNetwork();
                        }
                    } else {
                        Client.get().addFutureTask(() -> setErrorMessage("Incorrect Host Name"));
                    }
                });
                t.start();
            }
        }).atCenter(0, -100));
        add(new Button(Lang.getString("gui.back"), () -> {
            Client.get().clearNetwork();
            Client.get().openGui(parentGui);
        }).atCenter(0, -250));
        add(connectingText.atCenter(0, -150));
    }

    @Override
    public void update() {
        super.update();

        float colorAlpha = connectingText.getColorAlpha();
        if (colorAlpha > 0.0f) {
            colorAlpha -= 0.0025f;
            if (colorAlpha < 0.0f) {
                colorAlpha = 0.0f;
            }

            connectingText.setColorAlpha(colorAlpha);
        }
    }

    private void setErrorMessage(String text) {
        connectingText.getColor().w = 1.0f;
        connectingText.setString(text);
    }
}