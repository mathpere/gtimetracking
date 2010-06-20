/**
 * Copyright 2010 Mathieu Perez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.googlecode.gtimetracking.service;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.googlecode.gtimetracking.ui.ExportWithinDateRange;
import com.googlecode.gtimetracking.ui.LoginForm;
import com.googlecode.gtimetracking.ui.TrackForm;
import com.googlecode.gtimetracking.vo.DateRange;
import com.googlecode.gtimetracking.vo.GCalendarCredentials;
import com.googlecode.gtimetracking.vo.Track;
import com.googlecode.gtimetracking.vo.TrackEvent;
import com.googlecode.gtimetracking.vo.TrackEvent.Event;

public class UIService implements ApplicationEventPublisherAware {

	private final static String WHAT_HAVE_YOU_DONE_TITLE = "What have you done since %1$tD %1$tR ?";

	private TrackForm trackForm;
	private TrayIcon trayIcon;
	private JFrame frame;
	private PopupMenu popupMenu;
	private ExportWithinDateRange exportWithinDateRange;
	private LoginForm loginForm;
	private JFileChooser fileChooser;

	private ApplicationEventPublisher applicationEventPublisher;
	private EncryptService encryptService;

	private ActionListener showLoginFormListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			showGCalendarLoginForm();
		}
	};

	private ActionListener showExportWithinDateRangeFormListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			showExportWithinDateRangeForm();
		}
	};

	private void addMenuItem(String label, ActionListener actionListener) {
		MenuItem menuItem = new MenuItem(label);
		menuItem.addActionListener(actionListener);
		popupMenu.add(menuItem);
	}

	private void addMenuItem(String label, final Event event) {
		MenuItem menuItem = new MenuItem(label);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				publishEvent(event, null);
			}
		});
		popupMenu.add(menuItem);
	}

	public void addUrlMenuItem(String label, final String uri) {

		MenuItem menuItem = new MenuItem(label);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(uri));
				} catch (Exception e1) {
				}
			}
		});
		popupMenu.insert(menuItem, 0);
	}

	public void displayTrayMessage(String caption, String text,
			TrayIcon.MessageType messageType) {
		trayIcon.displayMessage(caption, text, messageType);
	}

	private Image getImage(String name) {
		return new ImageIcon(getClass().getResource(name)).getImage();
	}

	public void initUI() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		trackForm = new TrackForm();
		loginForm = new LoginForm();
		exportWithinDateRange = new ExportWithinDateRange();
		fileChooser = new JFileChooser();

		// Frame
		frame = new JFrame("GTimeTracking");
		frame.setAlwaysOnTop(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

		List<Image> iconsImages = new ArrayList<Image>();
		iconsImages.add(getImage("1276317037_clock.png"));
		iconsImages.add(getImage("1276317042_clock.png"));
		iconsImages.add(getImage("1276317043_clock.png"));
		iconsImages.add(getImage("1276317049_clock.png"));
		iconsImages.add(getImage("1276317050_clock.png"));
		iconsImages.add(getImage("1276317052_clock.png"));
		frame.setIconImages(iconsImages);

		// TrayIcon
		if (SystemTray.isSupported()) {

			SystemTray tray = SystemTray.getSystemTray();

			Image image = Toolkit.getDefaultToolkit().getImage(
					getClass().getResource("1276317050_clock.png"));

			popupMenu = new PopupMenu();
			popupMenu.insertSeparator(0);
			addMenuItem("Login", showLoginFormListener);
			addMenuItem("Export", showExportWithinDateRangeFormListener);
			addMenuItem("Track now", Event.TRACK_NOW);
			addMenuItem("Exit", Event.ON_CLOSE);

			trayIcon = new TrayIcon(image, "Track", popupMenu);
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					publishEvent(Event.DOUBLE_CLICK, null);
				}
			});

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}
		}
	}

	private void publishEvent(Event event, Object value) {
		applicationEventPublisher.publishEvent(new TrackEvent(this, event,
				value));
	}

	public File saveIntoFile(String suggestedFilename) {

		fileChooser.setSelectedFile(new File(suggestedFilename));

		int showSaveDialog = fileChooser.showSaveDialog(null);

		if (showSaveDialog == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else {
			return null;
		}
	}

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Required
	public void setEncryptService(EncryptService encryptService) {
		this.encryptService = encryptService;
	}

	public void showExportWithinDateRangeForm() {

		int response = JOptionPane.showConfirmDialog(null,
				exportWithinDateRange, "Select Date Range",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (response == JOptionPane.OK_OPTION) {
			publishEvent(Event.ON_EXPORT_DATE_RANGE, new DateRange(
					exportWithinDateRange.getFromDate(), exportWithinDateRange
							.getToDate()));
		}
	}

	public void showGCalendarLoginForm() {

		int response = JOptionPane.showConfirmDialog(null, loginForm,
				"Please Enter Name and Password", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (response == JOptionPane.OK_OPTION) {
			publishEvent(Event.ON_SAVE_GCALENDAR_CREDENTIALS,
					new GCalendarCredentials(loginForm.getName(),
							encryptService.encryptPassword(loginForm
									.getPassword())));
		}
	}

	public void showTrackForm(Date startTime) {

		frame.setVisible(true);

		int response = JOptionPane.showConfirmDialog(frame, trackForm, String
				.format(WHAT_HAVE_YOU_DONE_TITLE, startTime),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		frame.setVisible(false);

		if (response == JOptionPane.OK_OPTION) {
			publishEvent(Event.ON_SAVE_TRACK, new Track(trackForm.getSummary(),
					trackForm.getProject(), trackForm.getDescription(),
					startTime, new Date()));
		}
	}
}