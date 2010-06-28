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

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class LoginForm extends JPanel {

	private final JTextField loginField = new JTextField(20);
	{
		loginField.setText("@gmail.com");
		loginField.setColumns(30);
	}

	public LoginForm() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("<html>- Visit the URL to "
				+ "authorize your OAuth request token.<br />"
				+ "- Enter your email and click OK</html>"));
		add(loginField);
	}

	public String getLogin() {
		return loginField.getText().replaceAll(" ", "");
	}

}