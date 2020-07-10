package ru.krogenit.bfsr.client.gui.multiplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector4f;
import ru.krogenit.bfsr.client.font.GUIText;
import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.client.gui.button.Button;
import ru.krogenit.bfsr.client.gui.button.ButtonBase;
import ru.krogenit.bfsr.client.gui.input.EnumInputType;
import ru.krogenit.bfsr.client.gui.input.InputBox;
import ru.krogenit.bfsr.client.language.Lang;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.packet.PacketHandshake;
import ru.krogenit.bfsr.network.packet.PacketLoginStart;
import ru.krogenit.bfsr.network.status.EnumConnectionState;
import ru.krogenit.bfsr.world.WorldClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GuiConnect extends Gui {

	private static final Logger logger = LogManager.getLogger();

	private final Gui previousGui;
	private GUIText connectingText;
	private final Core core;
	private final Vector2f messagePos = new Vector2f(0, 100);
	private String errorMessage;

	public GuiConnect(Gui gui) {
		this.previousGui = gui;
		this.core = Core.getCore();
	}

	@Override
	public void update(double delta) {
		super.update(delta);

		if (errorMessage != null) {
			setErrorMessage(errorMessage);
			errorMessage = null;
		}

		if (connectingText != null) {
			if (connectingText.getColor().w > 0) {
				connectingText.getColor().w -= 0.0025f;
			} else {
				connectingText.clear();
				connectingText = null;
			}
		}
	}

	@Override
	public void init() {
		super.init();
		InputBox input = new InputBox(TextureLoader.getTexture(TextureRegister.guiButtonBase), new Vector2f(center.x, center.y - 100), "gui.connect.host", EnumParticlePositionType.Gui, new Vector2f(1.0f, 1.0f), new Vector2f(), new Vector2f(-120, -16), EnumInputType.Any);
		input.setText("127.0.0.1:25565");
		inputBoxes.add(input);
		input = new InputBox(TextureLoader.getTexture(TextureRegister.guiButtonBase), new Vector2f(center.x, center.y - 50), "gui.connect.username", EnumParticlePositionType.Gui, new Vector2f(1.0f, 1.0f), new Vector2f(), new Vector2f(-120, -16), EnumInputType.Any);
		input.setText("Krogenit");
		inputBoxes.add(input);
		input = new InputBox(TextureLoader.getTexture(TextureRegister.guiButtonBase), new Vector2f(center.x, center.y - 0), "gui.connect.password", EnumParticlePositionType.Gui, new Vector2f(1.0f, 1.0f), new Vector2f(), new Vector2f(-120, -16), EnumInputType.Any);
		input.setText("test");
		inputBoxes.add(input);

		ButtonBase b = new ButtonBase(0, new Vector2f(center.x, center.y + 50), "gui.connect.connect");
		buttons.add(b);
		b = new ButtonBase(1, new Vector2f(center.x, center.y + 200), "gui.back");
		buttons.add(b);
	}

	@Override
	protected void onButtonLeftClick(Button b) {
		if (b.getId() == 0) {
			if (connectingText == null) {
				errorMessage = Lang.getString("gui.connecting");
				Thread t = new Thread(() -> {
					InetAddress inetaddress = null;
					String[] inputString = inputBoxes.get(0).getString().split(":");
					if (inputString.length > 1) {
						String host = inputString[0];
						int port;

						try {
							port = Integer.parseInt(inputString[1]);
						} catch (Exception e) {
							errorMessage = "Wrong port number";
							return;
						}

						String plaerName = inputBoxes.get(1).getString();

						if (plaerName.length() < 3) {
							errorMessage = "Username must be at least 3 characters";
							return;
						}

						String password = inputBoxes.get(2).getString();

						if (password.length() < 3) {
							errorMessage = "Password must be at least 3 characters";
							return;
						}

						try {
							core.clearNetwork();
							inetaddress = InetAddress.getByName(host);
							NetworkManagerClient networkManager = NetworkManagerClient.provideLanClient(inetaddress, port, previousGui);
							networkManager.scheduleOutboundPacket(new PacketHandshake(5, inetaddress.toString(), port, EnumConnectionState.LOGIN));
							networkManager.scheduleOutboundPacket(new PacketLoginStart(plaerName, password, false));
							core.setNetworkManager(networkManager);
						} catch (UnknownHostException unknownhostexception) {
							logger.error("Couldn't connect to server", unknownhostexception);
							errorMessage = Lang.getString("connect.failed") + " Unknown Host";
						} catch (Exception exception) {
							logger.error("Couldn't connect to server", exception);
							String s = exception.toString();

							if (inetaddress != null) {
								String s1 = inetaddress.toString() + ":" + port;
								s = s.replaceAll(s1, "");
							}

							errorMessage = Lang.getString("connect.failed") + " " + s;
						}
					} else {
						errorMessage = "Incorrect Host Name";
					}
				});
				t.start();
			}
		} else {
			core.clearNetwork();
			Core.getCore().setCurrentGui(previousGui);
		}
	}

	private void setErrorMessage(String text) {
		if (connectingText != null) connectingText.clear();
		connectingText = new GUIText(text, new Vector2f(center.x + messagePos.x, center.y + messagePos.y*Transformation.guiScale.y), new Vector4f(1f, 1f, 1f, 1f), EnumParticlePositionType.Gui);
	}

	@Override
	public void clear() {
		super.clear();
		if (connectingText != null) connectingText.clear();
	}
}
