package net.bfsr.client.gui.multiplayer;

import lombok.extern.log4j.Log4j2;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.input.InputBox;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringOffsetType;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.core.Core;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.packet.PacketHandshake;
import net.bfsr.network.packet.PacketLoginStart;
import net.bfsr.network.status.EnumConnectionState;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Log4j2
public class GuiConnect extends Gui {
    private final StringObject connectingText = new StringObject(FontType.XOLONIUM, 20, StringOffsetType.CENTERED);

    public GuiConnect(Gui parentGui) {
        super(parentGui);
        connectingText.setColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    @Override
    protected void initElements() {
        int buttonOffsetX = -150;
        int fontSize = 20;
        int offsetX = 24;

        InputBox hostInputBox = new InputBox(TextureRegister.guiButtonBase, Lang.getString("gui.connect.host"),
                fontSize, offsetX, 0).setStringObject("127.0.0.1:25565");
        registerGuiObject(hostInputBox.atCenter(buttonOffsetX, -100));
        InputBox usernameInputBox = new InputBox(TextureRegister.guiButtonBase, Lang.getString("gui.connect.username"),
                fontSize, offsetX, 0).setStringObject("Krogenit");
        registerGuiObject(usernameInputBox.atCenter(buttonOffsetX, -50));
        InputBox passwordInputBox = new InputBox(TextureRegister.guiButtonBase, Lang.getString("gui.connect.password"),
                fontSize, offsetX, 0).setStringObject("test");
        registerGuiObject(passwordInputBox.atCenter(buttonOffsetX, 0));

        registerGuiObject(new Button(Lang.getString("gui.connect.connect"), () -> {
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
                            Core.getCore().addFutureTask(() -> setErrorMessage("Wrong port number"));
                            return;
                        }

                        String playerName = usernameInputBox.getString();

                        if (playerName.length() < 3) {
                            Core.getCore().addFutureTask(() -> setErrorMessage("Username must be at least 3 characters"));
                            return;
                        }

                        String password = passwordInputBox.getString();

                        if (password.length() < 3) {
                            Core.getCore().addFutureTask(() -> setErrorMessage("Password must be at least 3 characters"));
                            return;
                        }

                        try {
                            Core.getCore().clearNetwork();
                            inetaddress = InetAddress.getByName(host);
                            NetworkManagerClient networkManager = NetworkManagerClient.provideLanClient(inetaddress, port, parentGui);
                            networkManager.scheduleOutboundPacket(new PacketHandshake(5, inetaddress.toString(), port, EnumConnectionState.LOGIN));
                            networkManager.scheduleOutboundPacket(new PacketLoginStart(playerName, password, false));
                            Core.getCore().setNetworkManager(networkManager);
                        } catch (UnknownHostException unknownhostexception) {
                            log.error("Couldn't connect to server", unknownhostexception);
                            Core.getCore().addFutureTask(() -> setErrorMessage(Lang.getString("connect.failed") + " Unknown Host"));
                            Core.getCore().clearNetwork();
                        } catch (Exception e) {
                            log.error("Couldn't connect to server", e);
                            String s = e.toString();

                            if (inetaddress != null) {
                                String s1 = inetaddress + ":" + port;
                                s = s.replaceAll(s1, "");
                            }

                            String finalS = s;
                            Core.getCore().addFutureTask(() -> setErrorMessage(Lang.getString("connect.failed") + " " + finalS));
                            Core.getCore().clearNetwork();
                        }
                    } else {
                        Core.getCore().addFutureTask(() -> setErrorMessage("Incorrect Host Name"));
                    }
                });
                t.start();
            }
        }).atCenter(buttonOffsetX, 50));
        registerGuiObject(new Button(Lang.getString("gui.back"), () -> {
            Core.getCore().clearNetwork();
            Core.getCore().setCurrentGui(parentGui);
        }).atCenter(buttonOffsetX, 200));
        registerGuiObject(connectingText.atCenter(0, 150));
    }

    @Override
    public void update() {
        super.update();

        if (connectingText.getColor().w > 0.0f) {
            connectingText.getColor().w -= 0.0025f;
            if (connectingText.getColor().w < 0.0f) connectingText.getColor().w = 0.0f;
            connectingText.compile();
        }
    }

    private void setErrorMessage(String text) {
        connectingText.getColor().w = 1.0f;
        connectingText.update(text);
    }

    @Override
    public void clear() {
        super.clear();
        connectingText.clear();
    }
}
