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
package com.googlecode.gtimetracking.ui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class LoginForm extends JPanel {

	private final JTextField nameField = new JTextField(20);
	private final JPasswordField passwordField = new JPasswordField(20);

	public LoginForm() {
		setLayout(new GridLayout(2, 2, 5, 5));
		add(new JLabel("Name:"));
		add(nameField);
		add(new JLabel("Password:"));
		add(passwordField);
	}

	public String getName() {
		return nameField.getText();
	}

	public String getPassword() {
		return new String(passwordField.getPassword());
	}
}