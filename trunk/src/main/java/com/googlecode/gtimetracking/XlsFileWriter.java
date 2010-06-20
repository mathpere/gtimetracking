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
package com.googlecode.gtimetracking;

import java.io.File;
import java.util.Map;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class XlsFileWriter {

	private final static boolean DEFAULT_AUTOFIT_COLUMNS_WIDTH = true;

	private WritableCellFormat headerCellFormat = new WritableCellFormat();
	private WritableCellFormat valueCellFormat = new WritableCellFormat();

	private String sheetName = "default";
	private boolean autoFitColumnsWidth = DEFAULT_AUTOFIT_COLUMNS_WIDTH;

	private WritableWorkbook workbook;
	private WritableSheet sheet;
	private int lineNumber = 0;

	public void setOutputStream(File output) throws Exception {
		workbook = Workbook.createWorkbook(output);

		sheet = workbook.createSheet(sheetName, 0);

		headerCellFormat.setBackground(Colour.VERY_LIGHT_YELLOW);
		headerCellFormat.setFont(new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false));
		headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

		valueCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	}

	public void setProperties(Map<String, Object> properties) throws Exception {
		if (properties.containsKey("sheetName")) {
			this.sheetName = (String) properties.get("sheetName");
		}
		if (properties.containsKey("autoFitColumnsWidth")) {
			this.autoFitColumnsWidth = (Boolean) properties
					.get("autoFitColumnsWidth");
		}
	}

	public void writeHeaders(String[] headers) throws Exception {
		for (int j = 0; j < headers.length; j++) {
			sheet
					.addCell(new Label(j, lineNumber, headers[j],
							headerCellFormat));
		}
		lineNumber++;
	}

	public void writeNext(Object[] next) throws Exception {

		if (next != null) {
			for (int j = 0; j < next.length; j++) {

				WritableCell cell = null;

				if (next[j] != null) {

					if (next[j] instanceof Long) {

						cell = new jxl.write.Number(j, lineNumber,
								((Long) next[j]), valueCellFormat);

					} else if (next[j] instanceof Integer) {

						cell = new jxl.write.Number(j, lineNumber,
								((Integer) next[j]), valueCellFormat);

					} else if (next[j] instanceof Double) {

						cell = new jxl.write.Number(j, lineNumber,
								((Double) next[j]), valueCellFormat);

					} else {

						cell = new Label(j, lineNumber, next[j].toString(),
								valueCellFormat);

					}

				} else {
					cell = new Label(j, lineNumber, "", valueCellFormat);
				}

				sheet.addCell(cell);
			}
			lineNumber++;

		} else {
			if (autoFitColumnsWidth) {
				int columns = sheet.getColumns();
				for (int i = 0; i < columns; i++) {
					CellView columnView = sheet.getColumnView(i);
					columnView.setAutosize(true);
					sheet.setColumnView(i, columnView);
				}
			}
			workbook.write();
			workbook.close();
		}
	}
}