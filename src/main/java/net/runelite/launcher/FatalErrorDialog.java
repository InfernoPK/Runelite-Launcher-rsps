/*
 * Copyright (c) 2019 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLHandshakeException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FatalErrorDialog extends JDialog
{
	private static final AtomicBoolean alreadyOpen = new AtomicBoolean(false);

	private static final Color DARKER_GRAY_COLOR = new Color(30, 30, 30);
	private static final Color DARK_GRAY_COLOR = new Color(40, 40, 40);
	private static final Color DARK_GRAY_HOVER_COLOR = new Color(35, 35, 35);

	private final JPanel rightColumn = new JPanel();
	private final Font font = new Font(Font.DIALOG, Font.PLAIN, 12);

	public FatalErrorDialog(String message)
	{
		String finalMessage = message.replace("{name}", LauncherProperties.getApplicationName()).replace("{link}", LauncherProperties.getWebsiteLink()).replace("{types}", LauncherProperties.getRuneliteTypeManifest());
		if (alreadyOpen.getAndSet(true))
		{
			throw new IllegalStateException("Fatal error during fatal error: " + finalMessage);
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		UIManager.put("Button.select", DARKER_GRAY_COLOR);

		try
		{
			BufferedImage logo = ImageIO.read(SplashScreen.class.getResourceAsStream("runelite_transparent.png"));
			setIconImage(logo);

			JLabel runelite = new JLabel();
			runelite.setIcon(new ImageIcon(logo));
			runelite.setAlignmentX(Component.CENTER_ALIGNMENT);
			runelite.setBackground(DARK_GRAY_COLOR);
			runelite.setOpaque(true);
			rightColumn.add(runelite);
		}
		catch (IOException e)
		{
		}

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				System.exit(-1);
			}
		});

		setTitle("Fatal error starting " + LauncherProperties.getApplicationName());
		setLayout(new BorderLayout());

		Container pane = getContentPane();
		pane.setBackground(DARKER_GRAY_COLOR);

		JPanel leftPane = new JPanel();
		leftPane.setBackground(DARKER_GRAY_COLOR);
		leftPane.setLayout(new BorderLayout());

		JLabel title = new JLabel("There was a fatal error starting " + LauncherProperties.getApplicationName());
		title.setForeground(Color.WHITE);
		title.setFont(font.deriveFont(16.f));
		title.setBorder(new EmptyBorder(10, 10, 10, 10));
		leftPane.add(title, BorderLayout.NORTH);

		leftPane.setPreferredSize(new Dimension(400, 200));
		JTextArea textArea = new JTextArea(finalMessage);
		textArea.setFont(font);
		textArea.setBackground(DARKER_GRAY_COLOR);
		textArea.setForeground(Color.LIGHT_GRAY);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
		textArea.setEditable(false);
		leftPane.add(textArea, BorderLayout.CENTER);

		pane.add(leftPane, BorderLayout.CENTER);

		rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
		rightColumn.setBackground(DARK_GRAY_COLOR);
		rightColumn.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

		addButton("Open logs folder", () ->
		{
			LinkBrowser.open(Launcher.LOGS_DIR.toString());
		});
		addButton("Get help on Discord", () -> LinkBrowser.browse(LauncherProperties.getDiscordInvite()));
		addButton("Troubleshooting steps", () -> LinkBrowser.browse(LauncherProperties.getTroubleshootingLink()));

		pane.add(rightColumn, BorderLayout.EAST);
	}

	public void open()
	{
		addButton("Exit", () -> System.exit(-1));

		pack();
		SplashScreen.stop();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public FatalErrorDialog addButton(String message, Runnable action)
	{
		JButton button = new JButton(message);
		button.addActionListener(e -> action.run());
		button.setFont(font);
		button.setBackground(DARK_GRAY_COLOR);
		button.setForeground(Color.LIGHT_GRAY);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 0, 0, DARK_GRAY_COLOR.brighter()),
			new EmptyBorder(4, 4, 4, 4)
		));
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		button.setFocusPainted(false);
		button.addChangeListener(ev ->
		{
			if (button.getModel().isPressed())
			{
				button.setBackground(DARKER_GRAY_COLOR);
			}
			else if (button.getModel().isRollover())
			{
				button.setBackground(DARK_GRAY_HOVER_COLOR);
			}
			else
			{
				button.setBackground(DARK_GRAY_COLOR);
			}
		});

		rightColumn.add(button);
		rightColumn.revalidate();

		return this;
	}

	public static void showNetErrorWindow(String action, Throwable err)
	{
		if (Objects.equals(err.getMessage(), "No Clients Found"))
		{
			new FatalErrorDialog("{name} was unable to find any clients to display please contact us to get this fixed {types}")
				.open();
			return;
		}
		if (err instanceof VerificationException || err instanceof GeneralSecurityException)
		{
			new FatalErrorDialog("{name} was unable to verify the security of its connection to the internet while " +
				action + ". You may have a misbehaving antivirus, internet service provider, a proxy, or an incomplete" +
				" java installation.")
				.open();
			return;
		}

		if (err instanceof ConnectException)
		{
			new FatalErrorDialog("{name} is unable to connect to a required server while " + action + ". " +
				"Please check your internet connection")
				.open();
			return;
		}

		if (err instanceof UnknownHostException)
		{
			new FatalErrorDialog("{name} is unable to resolve the address of a required server while " + action + ". " +
				"Your DNS resolver may be misconfigured, pointing to an inaccurate resolver, or your internet connection may " +
				"be down. ")
				.addButton("Change your DNS resolver", () -> LinkBrowser.browse(LauncherProperties.getDNSChangeLink()))
				.open();
			return;
		}

		if (err instanceof SSLHandshakeException)
		{
			if (err.getCause() instanceof CertificateException)
			{
				new FatalErrorDialog("{name} was unable to verify the certificate of a required server while " + action + ". " +
					"This can be caused by a firewall, antivirus, malware, misbehaving internet service provider, or a proxy.")
					.open();
			}
			else
			{
				new FatalErrorDialog("{name} was unable to establish a SSL/TLS connection with a required server while " + action + ". " +
					"This can be caused by a firewall, antivirus, malware, misbehaving internet service provider, or a proxy.")
					.open();
			}

			return;
		}

		new FatalErrorDialog("{name} encountered a fatal error while " + action + ".").open();
	}
}
