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
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
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
	private WritableCellFormat subtotalCellFormat = new WritableCellFormat();
	private WritableCellFormat grandtotalCellFormat = new WritableCellFormat();

	private String sheetName = "default";
	private boolean autoFitColumnsWidth = DEFAULT_AUTOFIT_COLUMNS_WIDTH;

	private WritableWorkbook workbook;
	private WritableSheet sheet;
	private int lineNumber = 0;

	private int[] columnsSize = null;

	public void setColumnsSize(int[] columnsSize) {
		this.columnsSize = columnsSize;
	}

	public void setOutputStream(File output) throws Exception {
		workbook = Workbook.createWorkbook(output);

		sheet = workbook.createSheet(sheetName, 0);

		headerCellFormat.setBackground(Colour.VERY_LIGHT_YELLOW);
		headerCellFormat.setFont(new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD, false));
		// headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

		// valueCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

		subtotalCellFormat.setBackground(Colour.GRAY_25);
		subtotalCellFormat.setFont(new WritableFont(WritableFont.ARIAL, 11,
				WritableFont.BOLD, false));
		// subtotalCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

		grandtotalCellFormat.setBackground(Colour.GRAY_80);
		grandtotalCellFormat.setFont(new WritableFont(WritableFont.ARIAL, 11,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.WHITE));
		// grandtotalCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
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
			sheet.addCell(new Label(j, lineNumber, headers[j], headerCellFormat));
		}
		lineNumber++;
	}

	private void writeNext(Object[] next, WritableCellFormat writableCellFormat)
			throws Exception {

		if (next != null) {
			for (int j = 0; j < next.length; j++) {

				WritableCell cell = null;

				if (next[j] != null) {

					if (next[j] instanceof Long) {

						cell = new jxl.write.Number(j, lineNumber,
								((Long) next[j]), writableCellFormat);

					} else if (next[j] instanceof Integer) {

						cell = new jxl.write.Number(j, lineNumber,
								((Integer) next[j]), writableCellFormat);

					} else if (next[j] instanceof Double) {

						cell = new jxl.write.Number(j, lineNumber,
								((Double) next[j]), writableCellFormat);

					} else {

						cell = new Label(j, lineNumber, next[j].toString(),
								writableCellFormat);

					}

				} else {
					cell = new Label(j, lineNumber, "", writableCellFormat);
				}

				sheet.addCell(cell);
			}
			lineNumber++;

		} else {

			int columns = sheet.getColumns();

			for (int i = 0; i < columns; i++) {
				CellView columnView = sheet.getColumnView(i);

				if (columnsSize != null && columnsSize[i] > 0) {
					columnView.setSize(columnsSize[i]);
					sheet.setColumnView(i, columnView);
				} else if (autoFitColumnsWidth) {
					columnView.setAutosize(true);
					sheet.setColumnView(i, columnView);
				}
			}

			workbook.write();
			workbook.close();
		}
	}

	public void writeSubtotal(Object[] next) throws Exception {
		writeNext(next, subtotalCellFormat);
	}

	public void writeTotal(Object[] next) throws Exception {
		writeNext(next, grandtotalCellFormat);
	}

	public void writeValue(Object[] next) throws Exception {
		writeNext(next, valueCellFormat);
	}
}