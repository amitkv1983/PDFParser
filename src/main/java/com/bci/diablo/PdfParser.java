
package com.bci.diablo;

import com.bci.diablo.entity.SummaryRow;
import com.bci.diablo.entity.Table;
import com.bci.diablo.entity.TableCell;
import com.bci.diablo.entity.TableRow;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.*;


public class PdfParser {

	private static final Logger logger = LoggerFactory.getLogger(PdfParser.class);

	/**
	 * -in: source <br/>
	 * -out: target <br/>
	 * -el: except lines. Ex: 1,2,3-1,6@8 #line 6 in page 8 <br/>
	 * -p: page <br/>
	 * -ep: except page <br/>
	 * -h: help
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(PdfParser.class.getResource("/com/bci/diablo/log4j.properties"));
		if (args.length == 1 && "-h".equals(args[0])) {
			printHelp();
		} else {
			extractTables(args);
		}
	}

	private static void extractTables(String[] args) {
		try {
			List<Integer> pages = getPages(args);
			List<Integer> exceptPages = getExceptPages(args);
			List<Integer[]> exceptLines = getExceptLines(args);
			String in = getIn(args);
			String out = getOut(args);

			PDFTableExtractor extractor = (new PDFTableExtractor())
					.setSource(in);
			// page
			for (Integer page : pages) {
				extractor.addPage(page);
			}
			// except page
			for (Integer exceptPage : exceptPages) {
				extractor.exceptPage(exceptPage);
			}
			// except lines
			List<Integer> exceptLineIdxs = new ArrayList<>();
			Multimap<Integer, Integer> exceptLineInPages = LinkedListMultimap
					.create();
			for (Integer[] exceptLine : exceptLines) {
				if (exceptLine.length == 1) {
					exceptLineIdxs.add(exceptLine[0]);
				} else if (exceptLine.length == 2) {
					int lineIdx = exceptLine[0];
					int pageIdx = exceptLine[1];
					exceptLineInPages.put(pageIdx, lineIdx);
				}
			}
			if (!exceptLineIdxs.isEmpty()) {
				extractor.exceptLine(Ints.toArray(exceptLineIdxs));
			}
			if (!exceptLineInPages.isEmpty()) {
				for (int pageIdx : exceptLineInPages.keySet()) {
					extractor.exceptLine(pageIdx,
							Ints.toArray(exceptLineInPages.get(pageIdx)));
				}
			}
			// begin parsing pdf file
			List<Table> tables = extractor.extract();
			ArrayList<SummaryRow> sumRow = new ArrayList<SummaryRow>();
			for (Table table : tables) {
				for (TableRow row : table.getRows()) {
//					System.out.println();
					if (row.getIdx() >1 && row.getIdx() < table.getRows().size() -1 ) // Skip table name and header column
					{
						List<TableCell> cells = row.getCells();
						if (cells.size() == 4) {
							SummaryRow summaryrow = new SummaryRow();
							summaryrow.setSeverity(cells.get(0).getContent());
							summaryrow.setSubtest(cells.get(1).getContent());
							summaryrow.setOccurrences(Integer.parseInt(cells.get(2).getContent()));
							summaryrow.setRecommendations(cells.get(3).getContent());
							sumRow.add(summaryrow);
						}
					}
				}
			}

			Gson gson = new Gson();
			System.out.println(gson.toJson(sumRow));

/*			Writer writer = new OutputStreamWriter(new FileOutputStream(out),"UTF-8");
			try {
				for (Table table : tables) {
					writer.write("Page: " + (table.getPageIdx() + 1) + "\n");
					writer.write(table.toHtml());

					// writer.write(table.toJson());
				}
			} finally {
				try {
					writer.close();
				} catch (Exception e) {
				}
			}*/
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	private static void printHelp() {
		StringBuilder help = new StringBuilder();
		help.append("Argument list: \n")
				.append("\t-in: (required) absolute pdf location path. Ex: \"/Users/thoqbk/table.pdf\"\n")
				.append("\t-out: (required) absolute output file. Ex: \"/Users/thoqbk/table.html\"\n")
				.append("\t-el: except lines. For example, to exept lines 1,2,3 and -1 (last line) in all pages and line 4 in page 8, the value shoud be: \"1,2,3,-1,4@8\"\n")
				.append("\t-p: only parse these pages. Ex: 1,2,3\n")
				.append("\t-ep: all pages except these pages. Ex: 1,2\n")
				.append("\t-h: help\n").append("---");
		logger.info(help.toString());
	}

	private static List<Integer> getPages(String[] args) throws IOException {
		String retVal = getArg(args, "in", null);
		File file = new File(retVal);  // The PDF file from where you would like to extract
        PDDocument document = PDDocument.load(file);
        List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i <= document.getNumberOfPages(); i++){

    	    PDFTextStripper s = new PDFTextStripper();
    	    s.setStartPage(i);
    	    s.setEndPage(i);
    	    String contents = s.getText(document);
    	    if(contents.contains("Summary Table")){
//    	    	System.out.println(i);
    	    	list.add(i-1);
    	    }
    	   
    	}
		return list;
		}

	private static List<Integer> getExceptPages(String[] args) {
		return getInts(args, "ep");
	}

	private static List<Integer> getInts(String[] args, String name) {
		List<Integer> retVal = new ArrayList<>();
		String intsInString = getArg(args, name);
		if (intsInString != null) {
			String[] intInStrings = intsInString.split(",");
			for (String intInString : intInStrings) {
				try {
					retVal.add(Integer.parseInt(intInString.trim()));
				} catch (Exception e) {
					throw new RuntimeException("Invalid argument (-" + name
							+ "): " + intsInString, e);
				}
			}
		}
		return retVal;
	}

	private static List<Integer[]> getExceptLines(String[] args) {
		List<Integer[]> retVal = new ArrayList<>();
		String exceptLinesInString = getArg(args, "el");
		if (exceptLinesInString == null) {
			return retVal;
		}
		// ELSE:
		String[] exceptLineStrings = exceptLinesInString.split(",");
		for (String exceptLineString : exceptLineStrings) {
			if (exceptLineString.contains("@")) {
				String[] exceptLineItems = exceptLineString.split("@");
				if (exceptLineItems.length != 2) {
					throw new RuntimeException(
							"Invalid except lines argument (-el): "
									+ exceptLinesInString);
				} else {
					try {
						int lineIdx = Integer.parseInt(exceptLineItems[0]
								.trim());
						int pageIdx = Integer.parseInt(exceptLineItems[1]
								.trim());
						retVal.add(new Integer[] { lineIdx, pageIdx });
					} catch (Exception e) {
						throw new RuntimeException(
								"Invalid except lines argument (-el): "
										+ exceptLinesInString, e);
					}
				}
			} else {
				try {
					int lineIdx = Integer.parseInt(exceptLineString.trim());
					retVal.add(new Integer[] { lineIdx });
				} catch (Exception e) {
					throw new RuntimeException(
							"Invalid except lines argument (-el): "
									+ exceptLinesInString, e);
				}
			}
		}
		return retVal;
	}

	private static String getOut(String[] args) {
		String retVal = getArg(args, "out", null);
		if (retVal == null) {
//			throw new RuntimeException("Missing output location");
		}
		return retVal;
	}

	private static String getIn(String[] args) {
		String retVal = getArg(args, "in", null);
		if (retVal == null) {
			throw new RuntimeException("Missing input file");
		}
		return retVal;
	}

	private static String getArg(String[] args, String name, String defaultValue) {
		int argIdx = -1;
		for (int idx = 0; idx < args.length; idx++) {
			if (("-" + name).equals(args[idx])) {
				argIdx = idx;
				break;
			}
		}
		if (argIdx == -1) {
			return defaultValue;
		} else if (argIdx < args.length - 1) {
			return args[argIdx + 1].trim();
		} else {
			throw new RuntimeException(
					"Missing argument value. Argument name: " + name);
		}
	}

	private static String getArg(String[] args, String name) {
		return getArg(args, name, null);
	}
}
